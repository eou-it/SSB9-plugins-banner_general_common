/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.AddressGeographicAreasView
import net.hedtech.banner.general.overall.AddressViewService
import net.hedtech.banner.general.overall.IntegrationConfigurationService
import net.hedtech.banner.general.overall.ldm.v6.AddressV6
import net.hedtech.banner.general.overall.AddressView
import net.hedtech.banner.general.overall.ldm.v6.HedmCountry
import net.hedtech.banner.general.system.Nation
import net.hedtech.banner.general.utility.IsoCodeService
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional

@Transactional
class AddressCompositeService extends LdmService {

    static final String COLLEGE_ADDRESS = "STVCOLL"
    AddressViewService addressViewService
    IntegrationConfigurationService integrationConfigurationService
    IsoCodeService isoCodeService

    /**
     * GET /api/addresses/<guid>
     *
     * @param guid
     * @return
     */
    @Transactional(readOnly = true)
    def get(String guid) {
        AddressView addressView
        addressView = AddressView.get(guid)
        if (!addressView) {
            throw new ApplicationException("address", new NotFoundException())
        }
        getDecorators([addressView]).get(0)
    }

    /**
     * GET /api/addresses
     *
     * @return
     */
    @Transactional(readOnly = true)
    Long count(Map params) {
        return AddressView.count()
    }

    /**
     * GET /api/addresses
     *
     * @param map
     * @return
     */
    def list(Map map) {
        RestfulApiValidationUtility.correctMaxAndOffset(map, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        int max = (map?.max as Integer)
        int offset = ((map?.offset ?: '0') as Integer)
        List<AddressView> addressesView = addressViewService.fetchAll(max, offset)
        return getDecorators(addressesView)
    }

    private List<AddressV6> getDecorators(List<AddressView> addressesView) {
        def dataMap = [:]
        dataMap.put("isInstitutionUsingISO2CountryCodes", integrationConfigurationService.isInstitutionUsingISO2CountryCodes())
        dataMap.put("defaultISO3CountryCodeForAddress", integrationConfigurationService.getDefaultISO3CountryCodeForAddress(dataMap.get("isInstitutionUsingISO2CountryCodes")))
        Nation nation = Nation.findByScodIso(integrationConfigurationService.getDefaultISOCountryCodeForAddress())
        if(!nation) {
            dataMap.put("defaultTitleForDefaultCountryCode", "Unknown")
        } else {
            dataMap.put("defaultTitleForDefaultCountryCode", nation.nation)
        }

        List<AddressV6> addresses = []
        List pidmsOrCodes = []
        addressesView.collect { address ->
            if (!address.sourceTable.equals(COLLEGE_ADDRESS)) {
                pidmsOrCodes << address.pidmOrCode
            }
        }
        List<AddressGeographicAreasView> geographicAreasView
        if (pidmsOrCodes?.size() > 0) {
            geographicAreasView = AddressGeographicAreasView.fetchAllByPidm(pidmsOrCodes)
        }
        Map geographicAreasGUID = [:]
        geographicAreasView.each { geographicArea ->
            String geoAreaKey = geographicArea.geographicAreasSource + geographicArea.pidmOrCode + geographicArea.atypCode + geographicArea.addressSequenceNumber
            List<String> guids = geographicAreasGUID.get(geoAreaKey)
            if (guids) {
                guids << geographicArea.id
            } else {
                geographicAreasGUID.put(geoAreaKey, [geographicArea.id])
            }
        }
        addressesView.each { address ->
            String iso3CountryCode = getISO3CountryCode(address, dataMap.get("isInstitutionUsingISO2CountryCodes"))
            if (!iso3CountryCode) {
                iso3CountryCode = dataMap.get("defaultISO3CountryCodeForAddress")
            }
            validateRegion(address)
            if (iso3CountryCode?.equals(HedmCountry.GBR.toString())) {
                validateSubRegion(address)
            }
            String addressKey = address.sourceTable + address.pidmOrCode + address.atypCode + address.sequenceNumber
            addresses << getDecorator(address, geographicAreasGUID.get(addressKey), iso3CountryCode, dataMap.get("defaultTitleForDefaultCountryCode"))
        }
        return addresses
    }


    private AddressV6 getDecorator(AddressView addressView, List<String> geographicAreasGUIDs, String iso3CountryCode,
                                   String defaultCountryTitle) {
        AddressV6 addressV6 = new AddressV6(addressView, iso3CountryCode, defaultCountryTitle)
        addressV6.geographicAreas = []
        geographicAreasGUIDs.each { guid ->
            addressV6.geographicAreas << ["id": guid]
        }
        return addressV6
    }


    private void validateRegion(AddressView addressView) {
        if (!isISOCodeAvailableForState(addressView)) {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("soaxref.region.mapping.not.found.message", [addressView.stateCode]))
        }
    }


    private void validateSubRegion(AddressView addressView) {
        if (!isISOCodeAvailableForCounty(addressView)) {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("soaxref.sub.region.mapping.not.found.message", [addressView.countyCode]))
        }
    }

    private String getISO3CountryCode(AddressView addressView, boolean institutionUsingISO2CountryCodes) {
        String isoCountryCode
        if (addressView.sourceTable == COLLEGE_ADDRESS) {
            isoCountryCode = addressView.countryCode
        } else {
            if (addressView.countryCode) {
                isoCountryCode = Nation.findByCode(addressView.countryCode).scodIso
                if (isoCountryCode == null) {
                    throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("country.code.not.mapped.to.iso.code.message", [addressView.countryCode]))
                }
            }
        }

        if (institutionUsingISO2CountryCodes) {
            isoCountryCode = isoCodeService.getISO3CountryCode(isoCountryCode)
        }

        return isoCountryCode
    }


    private boolean isISOCodeAvailableForState(AddressView addressView) {
        boolean available = true
        if (addressView.sourceTable != COLLEGE_ADDRESS) {
            if (addressView.stateCode != null && addressView.countryRegionCode == null) {
                available = false
            }
        }
        return available
    }

    private boolean isISOCodeAvailableForCounty(AddressView addressView) {
        boolean available = true
        if (addressView.sourceTable != COLLEGE_ADDRESS) {
            if (addressView.countyCode != null && addressView.countrySubRegionCode == null) {
                available = false
            }
        }
        return available
    }

}

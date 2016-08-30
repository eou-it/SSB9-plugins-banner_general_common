/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.AddressGeographicAreasView
import net.hedtech.banner.general.overall.AddressGeographicAreasViewService
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

    private static final List<String> VERSIONS = [GeneralValidationCommonConstants.VERSION_V6]

    AddressViewService addressViewService
    IntegrationConfigurationService integrationConfigurationService
    IsoCodeService isoCodeService
    AddressGeographicAreasViewService addressGeographicAreasViewService

    /**
     * GET /api/addresses
     *
     * @param map
     * @return
     */
    def list(Map map) {
        String acceptVersion = getAcceptVersion(VERSIONS)

        RestfulApiValidationUtility.correctMaxAndOffset(map, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        int max = (map?.max as Integer)
        int offset = ((map?.offset ?: '0') as Integer)
        List<AddressView> addressesView = addressViewService.fetchAll(max, offset)
        return createAddressDataModels(addressesView)
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
     * GET /api/addresses/<guid>
     *
     * @param guid
     * @return
     */
    @Transactional(readOnly = true)
    def get(String guid) {
        String acceptVersion = getAcceptVersion(VERSIONS)

        AddressView addressView
        addressView = addressViewService.fetchByGuid(guid?.trim()?.toString())
        if (!addressView) {
            throw new ApplicationException("address", new NotFoundException())
        }
        createAddressDataModels([addressView]).get(0)
    }


    private List<AddressV6> createAddressDataModels(List<AddressView> addressesView) {
        boolean institutionUsingISO2CountryCodes = integrationConfigurationService.isInstitutionUsingISO2CountryCodes()
        String defaultISO3CountryCode = integrationConfigurationService.getDefaultISO3CountryCodeForAddress(institutionUsingISO2CountryCodes)
        String defaultCountryTitle = getDefaultCountryTitle()

        List pidmsOrCodes = preparePidmOrCodeList(addressesView)

        List<AddressGeographicAreasView> geographicAreasView = getAddressGeographicAreasToMap(pidmsOrCodes)

        Map geographicAreasGUID = constructGeographicAreasMap(geographicAreasView)

        List<AddressV6> addresses = []
        addressesView.each { address ->
            String addressKey = address.sourceTable + address.pidmOrCode + address.addressTypeCode + address.sequenceNumber
            addresses << createAddressDataModelV6(address, geographicAreasGUID.get(addressKey), institutionUsingISO2CountryCodes, defaultISO3CountryCode, defaultCountryTitle, null)
        }
        return addresses
    }

    private Map constructGeographicAreasMap(List<AddressGeographicAreasView> geographicAreasView) {
        Map geographicAreasGUID = [:]
        geographicAreasView.each { geographicArea ->
            String geoAreaKey = geographicArea.geographicAreasSource + geographicArea.pidmOrCode + geographicArea.addressAtypCode + geographicArea.addressSequenceNumber
            List<String> guids = geographicAreasGUID.get(geoAreaKey)
            if (guids) {
                guids << geographicArea.id
            } else {
                geographicAreasGUID.put(geoAreaKey, [geographicArea.id])
            }
        }
        geographicAreasGUID
    }

    private List<AddressGeographicAreasView> getAddressGeographicAreasToMap(List pidmsOrCodes) {
        List<AddressGeographicAreasView> geographicAreasView
        if (pidmsOrCodes) {
            geographicAreasView = addressGeographicAreasViewService.fetchAllByPidmOrCodeInList(pidmsOrCodes)
        }
        geographicAreasView
    }

    private List preparePidmOrCodeList(List<AddressView> addressesView) {
        List pidmsOrCodes = []
        addressesView.collect { address ->
            pidmsOrCodes << address.pidmOrCode
        }
        pidmsOrCodes
    }


    AddressV6 createAddressDataModelV6(AddressView addressView, List<String> geographicAreaGUIDs, boolean institutionUsingISO2CountryCodes, String defaultISO3CountryCode, String defaultCountryTitle, String hedmAddressType) {
        String iso3CountryCode = getISO3CountryCode(addressView, institutionUsingISO2CountryCodes)
        if (!iso3CountryCode) {
            iso3CountryCode = defaultISO3CountryCode
        }

        validateRegion(addressView)

        if (iso3CountryCode?.equals(HedmCountry.GBR.toString())) {
            validateSubRegion(addressView)
        }

        return createAddressDataModelV6(addressView, geographicAreaGUIDs, iso3CountryCode, defaultCountryTitle, hedmAddressType)
    }


    AddressV6 createAddressDataModelV6(AddressView addressView, List<String> geographicAreasGUIDs, String iso3CountryCode,
                                       String defaultCountryTitle, String hedmAddressType) {
        AddressV6 addressV6 = new AddressV6(addressView, iso3CountryCode, defaultCountryTitle, hedmAddressType)
        if(geographicAreasGUIDs){
            addressV6.geographicAreas = []
            geographicAreasGUIDs.each { guid ->
                addressV6.geographicAreas << ["id": guid]
            }
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
        if (addressView.countryCode) {
            isoCountryCode = Nation.findByCode(addressView.countryCode).scodIso
            if (isoCountryCode == null) {
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("country.code.not.mapped.to.iso.code.message", [addressView.countryCode]))
            }
        }

        if (institutionUsingISO2CountryCodes) {
            isoCountryCode = isoCodeService.getISO3CountryCode(isoCountryCode)
        }

        return isoCountryCode
    }


    private boolean isISOCodeAvailableForState(AddressView addressView) {
        boolean available = true
        if (addressView.stateCode != null && addressView.countryRegionCode == null) {
            available = false
        }
        return available
    }

    private boolean isISOCodeAvailableForCounty(AddressView addressView) {
        boolean available = true
        if (addressView.countyCode != null && addressView.countrySubRegionCode == null) {
            available = false
        }
        return available
    }

    String getDefaultCountryTitle() {
        String temp
        Nation nation = Nation.findByScodIso(integrationConfigurationService.getDefaultISOCountryCodeForAddress())
        if (!nation) {
            temp = "Unknown"
        } else {
            temp = nation.nation
        }
        return temp
    }

}

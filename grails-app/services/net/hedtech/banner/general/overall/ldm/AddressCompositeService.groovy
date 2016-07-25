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
        int max=(map?.max as Integer)
        int offset=((map?.offset ?:'0') as Integer)
        List<AddressView> addressesView = addressViewService.fetchAll(max,offset)
        return getDecorators(addressesView)
    }

    private List<AddressV6> getDecorators(List<AddressView> addressesView) {
        def dataMap = [:]
        dataMap.put("isInstitutionUsingISO2CountryCodes", integrationConfigurationService.isInstitutionUsingISO2CountryCodes())
        dataMap.put("getDefaultISOCountryCodeForAddress", integrationConfigurationService.getDefaultISOCountryCodeForAddress())
        dataMap.put("defaultTitleForDefaultCountryCode", Nation.findByScodIso(dataMap.get("getDefaultISOCountryCodeForAddress")).nation)
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
            if(guids){
                guids << geographicArea.id
            } else {
                geographicAreasGUID.put(geoAreaKey, [geographicArea.id])
            }
        }
        String nationISO
        addressesView.each { address ->
            validateRegion(address)
            nationISO = getNationISO(address, dataMap)
            validateSubRegion(address, nationISO)
            String addressKey = address.sourceTable + address.pidmOrCode + address.atypCode + address.sequenceNumber
            addresses << getDecorator(address, geographicAreasGUID.get(addressKey), nationISO, dataMap)
        }
        return addresses
    }


    private AddressV6 getDecorator(AddressView addressView, List<String> geographicAreasGUIDs, String nationISO, def dataMap) {
        AddressV6 addressV6 = new AddressV6(addressView, nationISO, dataMap)
        addressV6.geographicAreas = []
        geographicAreasGUIDs.each { guid ->
            addressV6.geographicAreas << ["id": guid]
        }
        return addressV6
    }


    private String getNationISO(AddressView addressView, def dataMap) {
        String nationISO
        if (addressView.sourceTable==COLLEGE_ADDRESS) {
            nationISO=addressView.countryCode
        }
        else {
            if (addressView.countryCode) {
                nationISO = (Nation.findByCode(addressView.countryCode)).scodIso
                if(nationISO == null) {
                    throw new ApplicationException('Country ISO', new BusinessLogicValidationException("country.code.not.mapped.to.iso.code.message", [addressView.countryCode]))
                }
            }
        }

        if(!nationISO){
            nationISO = dataMap.get("getDefaultISOCountryCodeForAddress")
            //addressView.countryTitle = dataMap.get("defaultTitleForDefaultCountryCode")
        }

        if( dataMap.get("isInstitutionUsingISO2CountryCodes") ){
            nationISO=isoCodeService.getISO3CountryCode(nationISO)
        }

        return nationISO
    }


    private void validateRegion(AddressView addressView) {
        if(addressView.countryRegionCode == null & addressView.stateCode != null & addressView.sourceTable != COLLEGE_ADDRESS){
            throw new ApplicationException('Country Region', new BusinessLogicValidationException("soaxref.region.mapping.not.found.message", [addressView.stateCode]))
        }
    }


    private void validateSubRegion(AddressView addressView, String nationISO) {
        if(addressView.countrySubRegionCode == null & addressView.countyCode != null & addressView.sourceTable != COLLEGE_ADDRESS){
            if(nationISO?.equals(CountryName.GBR.toString())){
                throw new ApplicationException('Country Sub Region', new BusinessLogicValidationException("soaxref.sub.region.mapping.not.found.message", [addressView.countyCode]))
            }
        }
    }
}

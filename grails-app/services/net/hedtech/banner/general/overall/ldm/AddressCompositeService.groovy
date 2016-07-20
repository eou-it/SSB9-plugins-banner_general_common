/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
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
        dataMap.put("getDefaultISO3CountryCodeForAddress", integrationConfigurationService.getDefaultISO3CountryCodeForAddress())
        List<AddressV6> addresses = []
        List pidmsOrCodes = []
        addressesView.collect { address ->
            pidmsOrCodes << address.pidmOrCode
        }
        List<AddressGeographicAreasView> geographicAreasView = AddressGeographicAreasView.fetchAllByPidm(pidmsOrCodes)
        Map geographicAreasGUID = [:]
        geographicAreasView.each { geographicArea ->
            String geoAreaKey = geographicArea.pidmOrCode + geographicArea.atypCode + geographicArea.addressSequenceNumber + geographicArea.geographicAreasSource
            List<String> guids = geographicAreasGUID.get(geoAreaKey)
            if(guids){
                guids << geographicArea.id
            } else {
                geographicAreasGUID.put(geoAreaKey, [geographicArea.id])
            }
        }

        addressesView.each { address ->
            String addressKey = address.pidmOrCode + address.atypCode + address.sequenceNumber + address.sourceTable
            addresses << getDecorator(address, geographicAreasGUID.get(addressKey), dataMap)
        }
        return addresses
    }


    private AddressV6 getDecorator(AddressView addressView, List<String> geographicAreasGUIDs, def dataMap) {
        AddressV6 addressV6 = new AddressV6(addressView, getNationISO(addressView, dataMap))
        addressV6.geographicAreas = []
        geographicAreasGUIDs.each { guid ->
            addressV6.geographicAreas << ["id": guid]
        }
        return addressV6
    }


    private getNationISO(AddressView addressView, def dataMap) {
        String nationISO
        if (addressView.sourceTable==COLLEGE_ADDRESS) {
            nationISO=addressView.countryCode
        }
        else {
            if (addressView.countryCode) {
                nationISO = (Nation.findByCode(addressView.countryCode)).scodIso
            }
        }

        if( dataMap.get("isInstitutionUsingISO2CountryCodes") ){
            nationISO=isoCodeService.getISO3CountryCode(nationISO)
        }

        if(!nationISO){
            nationISO = dataMap.get("getDefaultISO3CountryCodeForAddress")
        }
        return nationISO
    }
}

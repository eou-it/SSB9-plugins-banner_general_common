/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
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
        getDecorator(addressView)
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
        List<AddressV6> addresses = []
        RestfulApiValidationUtility.correctMaxAndOffset(map, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        int max=(map?.max as Integer)
        int offset=((map?.offset ?:'0') as Integer)
        addressViewService.fetchAll(max,offset).each{
            address ->
            addresses << getDecorator(address)
        }

        return addresses
    }

    private AddressV6 getDecorator(AddressView address) {
        List<String> addressLines = []
        String nationISO
        if(address.addressLine1) {
            addressLines << address.addressLine1
        }
        if(address.addressLine2) {
            addressLines << address.addressLine2
        }
        if(address.addressLine3) {
            addressLines << address.addressLine3
        }
        if(address.addressLine4) {
            addressLines << address.addressLine4
        }
        if(!addressLines){
            addressLines=["."]
        }
        if (address.sourceTable==COLLEGE_ADDRESS) {
            nationISO=address.countryCode
        }
        else {
            if (address.countryCode) {
                nationISO = (Nation.findByCode(address.countryCode)).scodIso
            }
        }
        if(integrationConfigurationService.isInstitutionUsingISO2CountryCodes()){
            nationISO=isoCodeService.getISO3CountryCode(nationISO)
        }
        return new AddressV6(address.id, addressLines, nationISO)
    }
}

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
        List<AddressView> addressesView = addressViewService.fetchAll(max,offset)
        addressesView.each{
            address ->
            addresses << getDecorator(address)
        }

        return addresses
    }

    private AddressV6 getDecorator(AddressView addressView) {
        return new AddressV6(addressView, getNationISO(addressView))
    }

    private getNationISO(AddressView addressView) {
        String nationISO
        if (addressView.sourceTable==COLLEGE_ADDRESS) {
            nationISO=addressView.countryCode
        }
        else {
            if (addressView.countryCode) {
                nationISO = (Nation.findByCode(addressView.countryCode)).scodIso
            }
        }
        if(integrationConfigurationService.isInstitutionUsingISO2CountryCodes()){
            nationISO=isoCodeService.getISO3CountryCode(nationISO)
        }
        return nationISO
    }
}

/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.personalinformation

class PersonalInformationCompositeService {

    def addressRolePrivilegesCompositeService
    def countyService
    def stateService
    def nationService

    def getAddressValidationObjects(roles, map){
        def newAddress = map

        // inner entities need to be actual domain objects
        newAddress.addressType = addressRolePrivilegesCompositeService.fetchAddressType(roles, map.addressType.code)
        if(map.county?.code)
            newAddress.county = countyService.fetchCounty(map.county.code)
        if(map.state?.code)
            newAddress.state = stateService.fetchState(map.state.code)
        if(map.nation?.code)
            newAddress.nation = nationService.fetchNation(map.nation.code)

        newAddress
    }
}
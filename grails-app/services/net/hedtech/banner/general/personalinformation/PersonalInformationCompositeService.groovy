/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.personalinformation

class PersonalInformationCompositeService {

    def addressRolePrivilegesCompositeService
    def countyService
    def stateService
    def nationService
    def relationshipService

    def getPersonValidationObjects(roles, map){
        // inner entities need to be actual domain objects
        if(map.addressType?.code)
            map.addressType = addressRolePrivilegesCompositeService.fetchAddressType(roles, map.addressType.code)
        if(map.county?.code)
            map.county = countyService.fetchCounty(map.county.code)
        if(map.state?.code)
            map.state = stateService.fetchState(map.state.code)
        if(map.nation?.code)
            map.nation = nationService.fetchNation(map.nation.code)
        if(map.relationship?.code)
            map.relationship = relationshipService.fetchRelationship(map.relationship.code)

        map
    }
}
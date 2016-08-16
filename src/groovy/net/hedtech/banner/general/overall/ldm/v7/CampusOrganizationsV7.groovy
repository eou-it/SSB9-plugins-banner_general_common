/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall.ldm.v7


class CampusOrganizationsV7 {
    String guid
    String name
    String type
    String code


    CampusOrganizationsV7(String guid, String name, String type, String code) {
        this.guid = guid
        this.name = name
        this.type = type
        this.code = code
    }
}

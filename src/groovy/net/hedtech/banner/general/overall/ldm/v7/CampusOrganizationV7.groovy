/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall.ldm.v7

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


class CampusOrganizationV7 {

    String guid
    String name
    Type type
    String code

    @ToString(includeFields = true, includeNames = true)
    @EqualsAndHashCode
    public static class Type {

        String id

        Type(String id) {
            this.id = id
        }
    }

    CampusOrganizationV7(String guid, String name, String type, String code) {
        this.guid = guid
        this.name = name
        if(type){
            this.type = new Type(type)
        }
        this.code = code
    }

}

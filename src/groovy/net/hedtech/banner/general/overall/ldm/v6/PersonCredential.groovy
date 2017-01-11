/*******************************************************************************
 Copyright 2016-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm.v6

class PersonCredential {

    String type
    String value


    def PersonCredential(String type, String value) {
        this.type = type
        this.value = value
    }

}

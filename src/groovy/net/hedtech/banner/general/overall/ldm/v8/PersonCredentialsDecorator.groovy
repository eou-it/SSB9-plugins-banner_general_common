/*******************************************************************************
 Copyright 2016-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm.v8

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includeFields = true)
class PersonCredentialsDecorator {

    String guid
    List<net.hedtech.banner.general.overall.ldm.v6.PersonCredential> credentials = []


    def PersonCredentialsDecorator(String guid) {
        this.guid = guid
    }

}

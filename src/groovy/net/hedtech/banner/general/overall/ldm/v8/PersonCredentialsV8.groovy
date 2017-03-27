/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm.v8

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.overall.ldm.v6.CredentialV6

@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includeFields = true)
class PersonCredentialsV8 {

    String id
    List<CredentialV6> credentials = []


    def PersonCredentialsV8(String guid) {
        this.id = guid
    }

}

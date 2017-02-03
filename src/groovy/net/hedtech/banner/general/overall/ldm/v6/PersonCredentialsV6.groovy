/*******************************************************************************
 Copyright 2016-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm.v6

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includeFields = true)
class PersonCredentialsV6 {

    String guid
    List<CredentialV6> credentials = []


    def PersonCredentialsV6(String guid) {
        this.guid = guid
    }

}

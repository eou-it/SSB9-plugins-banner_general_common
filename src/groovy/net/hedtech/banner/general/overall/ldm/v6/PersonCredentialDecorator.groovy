/*******************************************************************************
 Copyright 2016-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm.v6

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includeFields = true)
class PersonCredentialDecorator {
    String type
    String value

    PersonCredentialDecorator(String type, String value) {
        this.type = type
        this.value = value
    }

}

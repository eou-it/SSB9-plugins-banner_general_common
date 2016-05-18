/** *******************************************************************************
 Copyright 2016-2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall.ldm.v6

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includeFields = true)
class NonPersonDecorator {

    String bannerId
    String title
    String guid

    NonPersonDecorator(String bannerId, String title, String guid) {
        this.bannerId = bannerId
        this.title = title
        this.guid = guid
    }

    def getRoles() {
        [["role": "affiliate"]]
    }

    def getCredentials() {
        [["type": "bannerId", "value": bannerId]]
    }

}

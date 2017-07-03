/** *******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.system.ldm.v4

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.system.CitizenType

/**
 * Decorator for citizenship-statuses HEDM API JSON v4 schema
 *
 */
@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includeFields = true)
class CitizenshipStatus {

    @Delegate
    private final CitizenType citizenType
    String guid
    String category


    CitizenshipStatus(CitizenType citizenType, String guid, String category) {
        this.citizenType = citizenType
        this.guid = guid
        this.category = category
    }

}

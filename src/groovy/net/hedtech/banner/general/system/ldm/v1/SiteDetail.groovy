/** *******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.system.ldm.v1

import net.hedtech.banner.general.system.Building
import net.hedtech.banner.general.system.Campus

/**
 * Decorator for Site LDM (/base/domain/site/v1/site.json-schema)
 *
 */
class SiteDetail {

    @Delegate
    private final Campus campus

    String guid
    def buildings = []

    def SiteDetail(String guid, def campus, def buildings) {
        this.guid = guid
        this.campus = campus
        this.buildings = buildings
    }

    /**
     * Equals method to compare the two SiteDetail
     * Objects
     * @param o
     * @return
     */
    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        SiteDetail that = (SiteDetail) o

        if (guid != that.guid) return false
        if (campus != that.campus) return false

        return true
    }

    /**
     *  returns hash code
     * @return int
     */
    int hashCode() {
        int result
        result = (campus != null ? campus.hashCode() : 0)
        result = 31 * result + (guid != null ? guid.hashCode() : 0)
        return result
    }



}

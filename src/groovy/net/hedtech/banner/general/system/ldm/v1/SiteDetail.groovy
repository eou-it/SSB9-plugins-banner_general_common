/** *******************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.system.ldm.v1

import net.hedtech.banner.general.system.Campus

/**
 * Decorator for Site LDM (/base/domain/site/v1/site.json-schema)
 *
 */
class SiteDetail {

    @Delegate
    private final Campus campus
    Metadata metadata


    String guid
    def buildings = []
    String type

    def SiteDetail(String guid) {
        this.campus = new Campus()
        this.guid = guid
    }

    def SiteDetail(String guid, def campus, def buildings, Metadata metadata) {
        this.guid = guid
        this.campus = campus
        this.buildings = buildings
        this.metadata = metadata
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
        if (metadata != that.metadata) return false
        if (guid != that.guid) return false
        if (campus != that.campus) return false
        if (buildings != that.buildings) return false
        return true
    }


    public String toString() {
        """SiteDetail[
                    campus=$campus,
                    guid=$guid,
                    buildings=$buildings,
                    metadata=$metadata]"""
    }
}

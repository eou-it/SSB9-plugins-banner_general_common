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
    protected final Campus campus
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
        if (!(o instanceof SiteDetail)) return false

        SiteDetail that = (SiteDetail) o

        if (buildings != that.buildings) return false
        if (campus != that.campus) return false
        if (guid != that.guid) return false
        if (metadata != that.metadata) return false
        if (type != that.type) return false

        return true
    }

    int hashCode() {
        int result
        result = (campus != null ? campus.hashCode() : 0)
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0)
        result = 31 * result + (guid != null ? guid.hashCode() : 0)
        result = 31 * result + (buildings != null ? buildings.hashCode() : 0)
        result = 31 * result + (type != null ? type.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return "SiteDetail{" +
                "campus=" + campus +
                ", metadata=" + metadata +
                ", guid='" + guid + '\'' +
                ", buildings=" + buildings +
                ", type='" + type + '\'' +
                '}';
    }
}

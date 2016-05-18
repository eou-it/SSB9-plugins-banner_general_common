/** *******************************************************************************
 Copyright 2014-2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.system.ldm.v1

import groovy.transform.EqualsAndHashCode
import net.hedtech.banner.general.system.Campus

/**
 * Decorator for Site LDM (/base/domain/site/v1/site.json-schema)
 *
 */
@EqualsAndHashCode(includeFields = true)
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

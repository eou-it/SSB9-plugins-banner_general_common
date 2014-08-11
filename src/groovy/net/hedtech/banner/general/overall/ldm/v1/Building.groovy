/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm.v1

import net.hedtech.banner.general.overall.HousingLocationBuildingDescription
import net.hedtech.banner.general.system.ldm.v1.SiteDetail

/**
 * LDM decorator for building resource (/base/domain/buildings/v1/buildings.json-schema)
 */
class Building {

    @Delegate
    private final HousingLocationBuildingDescription housingLocationBuildingDescription
    String guid
    private SiteDetail siteDetail

    Building ( HousingLocationBuildingDescription housingLocationBuildingDescription, SiteDetail siteDetail, String guid ) {
        this.housingLocationBuildingDescription = housingLocationBuildingDescription
        this.siteDetail = siteDetail
        this.guid = guid
    }


    boolean equals( o ) {
        if (this.is( o )) return true
        if (getClass() != o.class) return false
        Building that = (Building) o
        if (housingLocationBuildingDescription != that.housingLocationBuildingDescription) return false
        if (guid != that.guid) return false
        return true
    }


    int hashCode() {
        int result
        result = (housingLocationBuildingDescription != null ? housingLocationBuildingDescription.hashCode() : 0)
        result = 31 * result + (guid != null ? guid.hashCode() : 0)
        return result
    }


    String getTitle(){
        housingLocationBuildingDescription?.building?.description
    }


    String getAbbreviation(){
        housingLocationBuildingDescription?.building?.code
    }


    SiteDetail getSite(){
        siteDetail
    }


    public String toString() {
        """Building[
                       housingLocationBuildingDescription=$housingLocationBuildingDescription,
                       guid=$guid]"""
    }

}

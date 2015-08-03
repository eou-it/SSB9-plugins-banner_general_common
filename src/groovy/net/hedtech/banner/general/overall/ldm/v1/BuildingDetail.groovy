/*********************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm.v1

import net.hedtech.banner.general.overall.HousingLocationBuildingDescription
import net.hedtech.banner.general.system.ldm.v1.Metadata
import net.hedtech.banner.general.system.ldm.v1.SiteDetail

/**
 * LDM decorator for building resource (/base/domain/buildings/v1/buildings.json-schema)
 */
class BuildingDetail {

    @Delegate
    private final HousingLocationBuildingDescription housingLocationBuildingDescription
    String guid
    private SiteDetail siteDetail
    Metadata metadata
    List rooms = []
    String code
    String description

    BuildingDetail ( guid ) {
        this.guid = guid
        this.housingLocationBuildingDescription = new HousingLocationBuildingDescription()
    }
            BuildingDetail ( HousingLocationBuildingDescription housingLocationBuildingDescription, SiteDetail siteDetail, String guid, def rooms, Metadata metadata ) {
        this.housingLocationBuildingDescription = housingLocationBuildingDescription
        this.siteDetail = siteDetail
        this.guid = guid
        this.rooms = rooms
        this.metadata = metadata
        this.code = housingLocationBuildingDescription.building.code
        this.description = housingLocationBuildingDescription.building.description
    }


    boolean equals( o ) {
        if (this.is( o )) return true
        if (getClass() != o.class) return false
        BuildingDetail that = (BuildingDetail) o
        if (housingLocationBuildingDescription != that.housingLocationBuildingDescription) return false
        if (guid != that.guid) return false
        if (metadata != that.metadata) return false
        if (rooms != that.rooms) return false
        return true
    }


    String getTitle(){
        housingLocationBuildingDescription?.building?.description
    }


    String getAbbreviation(){
        housingLocationBuildingDescription?.building?.code
    }


    SiteDetail getSiteDetail(){
        siteDetail
    }


    public String toString() {
        """BuildingDetail[
                       housingLocationBuildingDescription=$housingLocationBuildingDescription,
                       metadata=$metadata,
                       rooms=$rooms,
                       guid=$guid]"""
    }

}

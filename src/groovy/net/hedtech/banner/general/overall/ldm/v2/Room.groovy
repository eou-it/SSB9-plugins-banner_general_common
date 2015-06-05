/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm.v2

import net.hedtech.banner.general.overall.HousingRoomDescriptionReadOnly
import net.hedtech.banner.general.overall.ldm.v1.AvailableRoom
import net.hedtech.banner.general.overall.ldm.v1.BuildingDetail
import net.hedtech.banner.general.system.ldm.v1.Metadata
import net.hedtech.banner.general.system.ldm.v1.SiteDetail

/**
 * Decorator for HeDM "rooms" (version 2)
 *
 */
class Room extends AvailableRoom {

    SiteDetail siteDetail


    Room( HousingRoomDescriptionReadOnly housingRoomDescription, BuildingDetail buildingDetail, SiteDetail siteDetail, List occupancies, String guid, Metadata metadata ) {
        super( housingRoomDescription, buildingDetail, occupancies, guid, metadata )
        this.siteDetail = siteDetail
    }

}

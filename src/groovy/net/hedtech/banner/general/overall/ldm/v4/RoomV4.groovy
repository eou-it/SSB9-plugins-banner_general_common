/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm.v4

import net.hedtech.banner.general.overall.HousingRoomDescriptionReadOnly
import net.hedtech.banner.general.overall.ldm.v1.BuildingDetail
import net.hedtech.banner.general.overall.ldm.v2.Room
import net.hedtech.banner.general.system.ldm.v1.Metadata
import net.hedtech.banner.general.system.ldm.v1.SiteDetail

/**
 * Decorator for HeDM "rooms" (version 4)
 *
 */
class RoomV4 extends Room {

    def roomTypes
    def roomDetails

    RoomV4( HousingRoomDescriptionReadOnly housingRoomDescription, BuildingDetail buildingDetail, SiteDetail siteDetail, List occupancies, String guid,RoomType roomType) {
        super( housingRoomDescription, buildingDetail, siteDetail,occupancies, guid, null )
        this.roomDetails=[:]
        this.roomTypes=[]
        roomDetails.type = roomType.getType()
        roomDetails.detail= ["id": roomType.id]
        this.roomTypes << roomDetails
    }


    @Override
    public String toString() {
        return "RoomV4{" +
                "roomTypes=" + roomTypes +
                ", roomDetails=" + roomDetails +
                '}';
    }
}

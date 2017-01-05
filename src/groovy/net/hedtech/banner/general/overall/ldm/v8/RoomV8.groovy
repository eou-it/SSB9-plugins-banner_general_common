/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm.v8

import net.hedtech.banner.general.overall.HousingRoomDescriptionReadOnly
import net.hedtech.banner.general.overall.ldm.v1.BuildingDetail
import net.hedtech.banner.general.overall.ldm.v2.Room
import net.hedtech.banner.general.overall.ldm.v4.RoomType
import net.hedtech.banner.general.system.ldm.v1.SiteDetail
import net.hedtech.banner.general.overall.ldm.v4.RoomType

/**
 * Decorator for HeDM "rooms" (version 8)
 *
 */
class RoomV8  {

    protected final HousingRoomDescriptionReadOnly availableRoomDescription
    BuildingDetail buildingDetail
    List occupancies
    String guid
    SiteDetail siteDetail

    final String type = "room"

    def roomTypes
    def roomDetails

    def roomCharacterstics = new String[3]

    RoomV8(HousingRoomDescriptionReadOnly housingRoomDescription, BuildingDetail buildingDetail, SiteDetail siteDetail, List occupancies, String guid, RoomType roomType) {

        this.availableRoomDescription = housingRoomDescription
        this.buildingDetail = buildingDetail
        this.occupancies = occupancies
        this.guid = guid
        this.siteDetail = siteDetail
        this.roomDetails=[:]
        this.roomTypes=[]
        roomDetails.type = roomType.getType()
        roomDetails.detail= ["id": roomType.id]
        this.roomTypes << roomDetails
        roomCharacterstics  = []
    }


    @Override
    public String toString() {
        return "RoomV8{" +
                "roomTypes=" + roomTypes +
                ", roomDetails=" + roomDetails +
                '}';
    }


}


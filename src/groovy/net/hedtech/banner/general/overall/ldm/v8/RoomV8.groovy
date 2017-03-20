/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm.v8

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
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
@ToString(includeFields = true, includeNames = true)
@EqualsAndHashCode
public class RoomV8  {

    protected final HousingRoomDescriptionReadOnly availableRoomDescription
    BuildingDetail buildingDetail
    List occupancies
    String guid
    SiteDetail siteDetail
    String roomNumber
    String description

    final String type = "room"

    def roomTypes
    def roomDetails

    def roomCharacteristics

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

        this.roomNumber = housingRoomDescription.roomNumber
        this.description = housingRoomDescription.description
    }


    @Override
    public String toString() {
        return "RoomV8{" +
                "roomTypes=" + roomTypes +
                ", roomDetails=" + roomDetails +
                '}';
    }


}


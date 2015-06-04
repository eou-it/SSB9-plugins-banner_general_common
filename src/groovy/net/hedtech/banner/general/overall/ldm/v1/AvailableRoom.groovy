/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm.v1

import net.hedtech.banner.general.overall.HousingRoomDescriptionReadOnly
import net.hedtech.banner.general.system.ldm.v1.Metadata
import net.hedtech.banner.general.system.ldm.v1.SiteDetail

/**
 * LDM decorator for AvailableRoom resource
 */
class AvailableRoom {

    public static final String LDM_NAME = 'rooms'

    @Delegate
    private final HousingRoomDescriptionReadOnly availableRoomDescription
    BuildingDetail buildingDetail
    SiteDetail siteDetail
    List occupancies
    String guid
    Metadata metadata
    final String type = "room"

    AvailableRoom(String guid) {
        this.guid = guid
        this.availableRoomDescription = new HousingRoomDescriptionReadOnly()
    }

    AvailableRoom(HousingRoomDescriptionReadOnly housingRoomDescription, BuildingDetail buildingDetail, List occupancies, String guid, Metadata metadata) {
        this.availableRoomDescription = housingRoomDescription
        this.buildingDetail = buildingDetail
        this.occupancies = occupancies
        this.guid = guid
        this.metadata = metadata
    }

    AvailableRoom(HousingRoomDescriptionReadOnly housingRoomDescription, BuildingDetail buildingDetail, SiteDetail siteDetail, List occupancies, String guid, Metadata metadata) {
        this.availableRoomDescription = housingRoomDescription
        this.buildingDetail = buildingDetail
        this.siteDetail = siteDetail
        this.occupancies = occupancies
        this.guid = guid
        this.metadata = metadata
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        AvailableRoom room = (AvailableRoom) o
        if (guid != room.guid) return false
        if (availableRoomDescription != room.availableRoomDescription) return false
        if (buildingDetail != room.buildingDetail) return false
        if (siteDetail != room.siteDetail) return false
        if (occupancies != room.occupancies) return false
        if (metadata != room.metadata) return false
        return true
    }


    int hashCode() {
        int result
        result = (availableRoomDescription != null ? availableRoomDescription.hashCode() : 0)
        result = 31 * result + (buildingDetail != null ? buildingDetail.hashCode() : 0)
        result = 31 * result + (siteDetail != null ? siteDetail.hashCode() : 0)
        result = 31 * result + (occupancies != null ? occupancies.hashCode() : 0)
        result = 31 * result + (guid != null ? guid.hashCode() : 0)
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0)
        return result
    }


    public String toString() {
        """Room[
                    availableRoomDescription=$availableRoomDescription,
                    building=$buildingDetail,
                    siteDetail=$siteDetail,
                    occupancies=$occupancies,
                    metadata=$metadata,
                    guid=$guid]"""
    }
}

/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm.v1

import net.hedtech.banner.general.overall.HousingRoomDescription
import net.hedtech.banner.general.system.ldm.v1.Metadata

/**
 * LDM decorator for Room resource(/base/domain/room/room.json-schema)
 */
class Room {

    public static final String LDM_NAME = 'rooms'

    @Delegate
    private final HousingRoomDescription housingRoomDescription
    BuildingDetail buildingDetail
    List occupancies
    String guid
    Metadata metadata

    Room(HousingRoomDescription housingRoomDescription, BuildingDetail buildingDetail, List occupancies, String guid, Metadata metadata) {
        this.housingRoomDescription = housingRoomDescription
        this.buildingDetail = buildingDetail
        this.occupancies = occupancies
        this.guid = guid
        this.metadata = metadata
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        Room room = (Room) o
        if (guid != room.guid) return false
        if (housingRoomDescription != room.housingRoomDescription) return false
        if (buildingDetail != room.buildingDetail) return false
        if (occupancies != room.occupancies) return false
        if (metadata != room.metadata) return false
        return true
    }


    int hashCode() {
        int result
        result = (housingRoomDescription != null ? housingRoomDescription.hashCode() : 0)
        result = 31 * result + (buildingDetail != null ? buildingDetail.hashCode() : 0)
        result = 31 * result + (occupancies != null ? occupancies.hashCode() : 0)
        result = 31 * result + (guid != null ? guid.hashCode() : 0)
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0)
        return result
    }


    public String toString() {
        """Room[
                    housingRoomDescription=$housingRoomDescription,
                    buildingDetail=$buildingDetail,
                    occupancies=$occupancies,
                    metadata=$metadata,
                    guid=$guid]"""
    }
}

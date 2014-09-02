/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm.v1

import net.hedtech.banner.general.overall.AvailableRoomDescription
import net.hedtech.banner.general.system.ldm.v1.Metadata

/**
 * LDM decorator for AvailableRoom resource
 */
class AvailableRoom {

    public static final String LDM_NAME = 'rooms'

    @Delegate
    private final AvailableRoomDescription availableRoomDescription

    @Delegate
    BuildingDetail building
    List occupancies
    String guid
    Metadata metadata


    AvailableRoom(AvailableRoomDescription housingRoomDescription, BuildingDetail building, List occupancies, String guid, Metadata metadata) {
        this.availableRoomDescription = housingRoomDescription
        this.building = building
        this.occupancies = occupancies
        this.guid = guid
        this.metadata = metadata
    }


    BuildingDetail getBuilding() {
        this.building
    }


    void setBuilding(BuildingDetail building) {
        this.building = building
    }







    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        AvailableRoom room = (AvailableRoom) o
        if (guid != room.guid) return false
        if (availableRoomDescription != room.availableRoomDescription) return false
        if (building != room.building) return false
        if (occupancies != room.occupancies) return false
        if (metadata != room.metadata) return false
        return true
    }


    int hashCode() {
        int result
        result = (availableRoomDescription != null ? availableRoomDescription.hashCode() : 0)
        result = 31 * result + (building != null ? building.hashCode() : 0)
        result = 31 * result + (occupancies != null ? occupancies.hashCode() : 0)
        result = 31 * result + (guid != null ? guid.hashCode() : 0)
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0)
        return result
    }


    public String toString() {
        """Room[
                    availableRoomDescription=$availableRoomDescription,
                    building=$building,
                    occupancies=$occupancies,
                    metadata=$metadata,
                    guid=$guid]"""
    }
}

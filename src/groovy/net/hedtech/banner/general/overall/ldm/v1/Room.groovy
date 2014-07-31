/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm.v1

import net.hedtech.banner.general.overall.HousingRoomDescription

class Room {

    @Delegate
    private final HousingRoomDescription housingRoomDescription
    List occupancies
    String guid

    Room(HousingRoomDescription housingRoomDescription, List occupancies, String guid){
        this.housingRoomDescription = housingRoomDescription
        this.occupancies = occupancies
        this.guid = guid
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        Room room = (Room) o
        if (guid != room.guid) return false
        if (housingRoomDescription != room.housingRoomDescription) return false
        if (occupancies != room.occupancies) return false
        return true
    }


    int hashCode() {
        int result
        result = (housingRoomDescription != null ? housingRoomDescription.hashCode() : 0)
        result = 31 * result + (occupancies != null ? occupancies.hashCode() : 0)
        result = 31 * result + (guid != null ? guid.hashCode() : 0)
        return result
    }


    public String toString() {
        """Room[
                    housingRoomDescription=$housingRoomDescription,
                    occupancies=$occupancies,
                    guid=$guid]"""
    }
}

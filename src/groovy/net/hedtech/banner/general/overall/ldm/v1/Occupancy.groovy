/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm.v1

/**
 * POGO to represent each element of "occupancies" array inside Room LDM (/base/domain/room/v1/room.json-schema)
 */
class Occupancy {

    String roomLayoutType
    Integer maxOccupancy


    Occupancy(String roomLayoutType, Integer maxOccupancy) {
        this.roomLayoutType = roomLayoutType
        this.maxOccupancy = maxOccupancy
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        Occupancy occupancy = (Occupancy) o
        if (maxOccupancy != occupancy.maxOccupancy) return false
        if (roomLayoutType != occupancy.roomLayoutType) return false
        return true
    }


    int hashCode() {
        int result
        result = (roomLayoutType != null ? roomLayoutType.hashCode() : 0)
        result = 31 * result + (maxOccupancy != null ? maxOccupancy.hashCode() : 0)
        return result
    }


    public String toString() {
        """Occupancy[
                    roomLayoutType=$roomLayoutType,
                    maxOccupancy=$maxOccupancy]"""
    }
}

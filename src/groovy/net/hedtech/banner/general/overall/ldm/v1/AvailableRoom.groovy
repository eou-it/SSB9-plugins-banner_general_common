/*******************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm.v1

import net.hedtech.banner.general.overall.HousingRoomDescriptionReadOnly
import net.hedtech.banner.general.system.ldm.v1.Metadata

/**
 * LDM decorator for AvailableRoom resource
 */
class AvailableRoom {

    public static final String LDM_NAME = 'rooms'

    @Delegate
    protected final HousingRoomDescriptionReadOnly availableRoomDescription
    BuildingDetail buildingDetail
    List occupancies
    String guid
    Metadata metadata
    final String type = "room"


    AvailableRoom( String guid ) {
        this.guid = guid
        this.availableRoomDescription = new HousingRoomDescriptionReadOnly()
    }


    AvailableRoom( HousingRoomDescriptionReadOnly housingRoomDescription, BuildingDetail buildingDetail, List occupancies, String guid, Metadata metadata ) {
        this.availableRoomDescription = housingRoomDescription
        this.buildingDetail = buildingDetail
        this.occupancies = occupancies
        this.guid = guid
        this.metadata = metadata
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof AvailableRoom)) return false

        AvailableRoom that = (AvailableRoom) o

        if (availableRoomDescription != that.availableRoomDescription) return false
        if (buildingDetail != that.buildingDetail) return false
        if (guid != that.guid) return false
        if (metadata != that.metadata) return false
        if (occupancies != that.occupancies) return false
        if (type != that.type) return false

        return true
    }

    int hashCode() {
        int result
        result = (availableRoomDescription != null ? availableRoomDescription.hashCode() : 0)
        result = 31 * result + (buildingDetail != null ? buildingDetail.hashCode() : 0)
        result = 31 * result + (occupancies != null ? occupancies.hashCode() : 0)
        result = 31 * result + (guid != null ? guid.hashCode() : 0)
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0)
        result = 31 * result + (type != null ? type.hashCode() : 0)
        return result
    }

    @Override
    public String toString() {
        return "AvailableRoom{" +
                "availableRoomDescription=" + availableRoomDescription +
                ", buildingDetail=" + buildingDetail +
                ", occupancies=" + occupancies +
                ", guid='" + guid + '\'' +
                ", metadata=" + metadata +
                ", type='" + type + '\'' +
                '}';
    }
}

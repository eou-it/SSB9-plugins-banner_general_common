/*******************************************************************************
 Copyright 2014-2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm.v1

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.overall.HousingRoomDescriptionReadOnly
import net.hedtech.banner.general.system.ldm.v1.Metadata

/**
 * LDM decorator for AvailableRoom resource
 */
@ToString(includeNames = true, includeFields = true)
@EqualsAndHashCode(includeFields = true)
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
}

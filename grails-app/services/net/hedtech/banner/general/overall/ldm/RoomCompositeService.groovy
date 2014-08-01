/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.HousingRoomDescription
import net.hedtech.banner.general.overall.ldm.v1.Occupancy
import net.hedtech.banner.general.overall.ldm.v1.Room
import net.hedtech.banner.query.operators.Operators
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional

@Transactional
class RoomCompositeService {

    private static final String ROOM_LDM_NAME = 'rooms'
    private static final String ROOM_LAYOUT_TYPE_CLASSROOM = 'Classroom'


    List<Room> list(Map map) {
        List rooms = []
        RestfulApiValidationUtility.correctMaxAndOffset(map, 10, 30)
        List<HousingRoomDescription> housingRoomDescriptions = HousingRoomDescription.fetchAllActiveClassrooms([:], map)
        housingRoomDescriptions.each { housingRoomDescription ->
            List occupancies = [new Occupancy(ROOM_LAYOUT_TYPE_CLASSROOM, housingRoomDescription.capacity)]
            rooms << new Room(housingRoomDescription, occupancies, GlobalUniqueIdentifier.findByLdmNameAndDomainId(ROOM_LDM_NAME, housingRoomDescription.id).guid)
        }
        return rooms
    }


    Long count() {
        return HousingRoomDescription.count()
    }


    @Transactional(readOnly = true)
    Room get(String guid) {
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndGuid(ROOM_LDM_NAME, guid)
        if (!globalUniqueIdentifier) {
            throw new ApplicationException(GlobalUniqueIdentifierService.API, new NotFoundException(id: Room.class.simpleName))
        }

        def filterData = [params: [id: globalUniqueIdentifier.domainId], criteria: [[key: 'id', binding: 'id', operator: Operators.EQUALS]]]
        HousingRoomDescription housingRoomDescription = HousingRoomDescription.fetchAllActiveClassrooms(filterData, [:])[0]
        if (!housingRoomDescription) {
            throw new ApplicationException(GlobalUniqueIdentifierService.API, new NotFoundException(id: Room.class.simpleName))
        }

        List occupancies = [new Occupancy(ROOM_LAYOUT_TYPE_CLASSROOM, housingRoomDescription.capacity)]
        return new Room(housingRoomDescription, occupancies, globalUniqueIdentifier.guid)
    }
}

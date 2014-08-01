/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.HousingRoomDescription
import net.hedtech.banner.general.overall.ldm.v1.Room
import net.hedtech.banner.general.system.Building
import net.hedtech.banner.testing.BaseIntegrationTestCase

class RoomCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    HousingRoomDescription i_success_housingRoomDescription
    def roomCompositeService


    void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initiializeDataReferences()
    }


    private void initiializeDataReferences() {
        Building building = Building.findByCode('CIS')
        i_success_housingRoomDescription = HousingRoomDescription.findByBuildingAndRoomNumber(building, '100')
    }


    void testListWithoutPaginationParams() {
        List rooms = roomCompositeService.list([:])
        assertNotNull rooms
        assertFalse rooms.isEmpty()
        assertEquals 10, rooms.size()
    }


    void testList() {
        def paginationParams = [max: '20', offset: '0']
        List rooms = roomCompositeService.list(paginationParams)
        assertNotNull rooms
        assertFalse rooms.isEmpty()
        assertTrue rooms.size() <= 20
    }


    void testCount() {
        assertNotNull i_success_housingRoomDescription
        assertTrue roomCompositeService.count() > 0
    }


    void testGetInvalidGuid() {
        try {
            roomCompositeService.get('Invalid-guid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, "NotFoundException"
        }
    }


    void testGetInvalidNonExistentHousingRoomDescription() {
        HousingRoomDescription housingRoomDescription = i_success_housingRoomDescription
        assertNotNull housingRoomDescription.id
        def id = i_success_housingRoomDescription.id
        String guid = GlobalUniqueIdentifier.findByLdmNameAndDomainId('rooms', i_success_housingRoomDescription.id).guid
        assertNotNull guid

        housingRoomDescription.delete(flush: true)
        assertNull HousingRoomDescription.get(id)

        try {
            roomCompositeService.get(guid)
        } catch (ApplicationException ae) {
            assertApplicationException ae, "NotFoundException"
        }
    }


    void testGet() {
        def paginationParams = [max: '1', offset: '0']
        List rooms = roomCompositeService.list(paginationParams)
        assertNotNull rooms
        assertFalse rooms.isEmpty()

        assertNotNull rooms[0].guid
        Room room = roomCompositeService.get(rooms[0].guid)
        assertNotNull room
        assertEquals rooms[0], room
    }
}

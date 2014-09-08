/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.HousingRoomDescription
import net.hedtech.banner.general.overall.ldm.v1.AvailableRoom
import net.hedtech.banner.general.overall.ldm.v1.Room
import net.hedtech.banner.general.system.Building
import net.hedtech.banner.testing.BaseIntegrationTestCase

class RoomCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    HousingRoomDescription i_success_housingRoomDescription
    def roomCompositeService


    protected void setUp() {
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


    void testListWithPagination() {
        def paginationParams = [max: '20', offset: '0']
        List rooms = roomCompositeService.list(paginationParams)
        assertNotNull rooms
        assertFalse rooms.isEmpty()
        assertTrue rooms.size() <= 20
    }


    void testListWithFilter() {
        def params = ['filter[filter[0][value]': 'Classroom', 'filter[0][field]': 'roomLayoutType', 'filter[0][operator]': 'equals']
        List rooms = roomCompositeService.list(params)
        assertNotNull rooms
        assertFalse rooms.isEmpty()
        assertTrue rooms.size() <= 20
        assertNull rooms.find { it.occupancies[0].roomLayoutType != 'Classroom' }
    }


    void testCount() {
        assertNotNull i_success_housingRoomDescription
        assertTrue roomCompositeService.count() > 0
    }


    void testGetInvalidGuid() {
        try {
            roomCompositeService.get('Invalid-guid')
            fail('This should have failed as the guid is invalid')
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
            fail('This should have failed as the room for the guid does not exist')
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
        assertEquals rooms[0].metadata.dataOrigin, room.metadata.dataOrigin
        assertEquals rooms[0].building, room.building
        assertEquals rooms[0].occupancies, room.occupancies
        assertEquals rooms[0].guid, room.guid
    }


    void testListForMissingStartDate() {
        Map params = getParamsForRoomQuery()
        params.remove('startDate')
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the startDate is missing')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'missing.startDate'
        }
    }


    void testListForMissingEndDate() {
        Map params = getParamsForRoomQuery()
        params.remove('endDate')
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the endDate is missing')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'missing.endDate'
        }
    }


    void testListForMissingStartTime() {
        Map params = getParamsForRoomQuery()
        params.remove('startTime')
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the startTime is missing')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'missing.startTime'
        }
    }


    void testListForMissingEndTime() {
        Map params = getParamsForRoomQuery()
        params.remove('endTime')
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the endTime is missing')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'missing.endTime'
        }
    }


    void testListForStartDateLaterThanEndDate() {
        Map params = getParamsForRoomQuery()
        params.startDate = '2014-09-10'
        params.endDate = '2014-09-09'
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the startDate is later than endDate')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'startDate.laterThanEndDate'
        }
    }


    void testListForInvalidStartTime() {
        Map params = getParamsForRoomQuery()
        params.startTime = '00:11'
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the startTime is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'invalid.startTime'
        }
        params.startTime = '15:60:60'
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the startTime is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'invalid.startTime'
        }
    }


    void testListForInvalidEndTime() {
        Map params = getParamsForRoomQuery()
        params.endTime = '23:59'
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the endTime is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'invalid.endTime'
        }
        params.endTime = '24:60:60'
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the endTime is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'invalid.endTime'
        }
    }


    void testListForStartTimeLaterThanEndTime() {
        Map params = getParamsForRoomQuery()
        params.startTime = '03:15:00'
        params.endTime = '02:15:00'
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the startTime is later than endTime')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'startTime.laterThanEndTime'
        }
    }


    void testListForMissingRecurrence() {
        Map params = getParamsForRoomQuery()
        params.remove('recurrence')
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the recurrence is missing')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'missing.recurrence'
        }
    }


    void testListForMissingByDays() {
        Map params = getParamsForRoomQuery()
        params.recurrence.byDay = []
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the recurrence byDay is empty')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'missing.recurrence.byDay'
        }
    }


    void testListForInvalidByDaysMoreThanSevenDays() {
        Map params = getParamsForRoomQuery()
        params.recurrence.byDay = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday', 'Monday']
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the recurrence byDay has more than seven items')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'invalid.recurrence.byDay'
        }
    }


    void testListForInvalidByDaysForMismatchWithDate() {
        Map params = getParamsForRoomQuery()
        params.startDate = '2014-09-08'
        params.endDate = '2014-09-08'
        params.recurrence.byDay = ['Tuesday']
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the recurrence byDay does not match the dates as the startDate/endDate is a Monday')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'invalid.recurrence.byDay'
        }
    }


    void testListForInvalidByDaysForMismatchWithDates() {
        Map params = getParamsForRoomQuery()
        params.startDate = '2014-09-08'
        params.endDate = '2014-09-10'
        params.recurrence.byDay = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday']
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the recurrence byDay does not match the dates')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'invalid.recurrence.byDay'
        }
    }


    void testListForInvalidByDaysForInvalidDay() {
        Map params = getParamsForRoomQuery()
        params.startDate = '2014-09-08'
        params.endDate = '2014-09-18'
        params.recurrence.byDay = ['Monday', 'XXXXXXXXX', 'Wednesday', 'Friday']
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the recurrence byDay as it contains an invalid day')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'invalid.recurrence.byDay'
        }
    }


    void testListForMissingOccupancies() {
        Map params = getParamsForRoomQuery()
        params.remove('occupancies')
        try {
            roomCompositeService.list(params)
            fail("This should have failed as the occupancies are missing")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "missing.occupancies"
        }
    }


    void testListForMissingRoomLayoutType() {
        Map params = getParamsForRoomQuery()
        params.occupancies[0]?.remove('roomLayoutType')
        try {
            roomCompositeService.list(params)
            fail("This should have failed as the occupancy roomLayoutType is missing")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "missing.roomLayoutType"
        }
    }


    void testListForMissingMaxOccupancy() {
        Map params = getParamsForRoomQuery()
        params.occupancies[0].remove('maxOccupancy')
        try {
            roomCompositeService.list(params)
            fail("This should have failed as the occupancy maxOccupancy is missing")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "missing.maxOccupancy"
        }
    }


    void testListForInvalidMaxOccupancy() {
        Map params = getParamsForRoomQuery()
        params.occupancies[0].maxOccupancy = 'abc'
        try {
            roomCompositeService.list(params)
            fail("This should have failed as the occupancy maxOccupancy must be an integer")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "invalid.maxOccupancy"
        }
    }


    void testListForValidParams() {
        Map params = getParamsForRoomQuery()
        List<AvailableRoom> availableRooms = roomCompositeService.list(params)
        assertNotNull availableRooms
        assertFalse availableRooms.isEmpty()
        assertNull availableRooms.find { it.capacity < params.occupancies[0]?.maxOccupancy }
    }


    private Map getParamsForRoomQuery() {
        return [
                action     : [POST: "list"],
                occupancies: [[
                                      maxOccupancy  : 200,
                                      roomLayoutType: "Classroom"
                              ]],
                startDate  : "2014-01-01",
                endDate    : "2014-12-31",
                startTime  : "00:00:00",
                endTime    : "23:59:59",
                recurrence : [
                        byDay: ["Monday", "Wednesday", "Friday"]
                ]
        ]
    }

}

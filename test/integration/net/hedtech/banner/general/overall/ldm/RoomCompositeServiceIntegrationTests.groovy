/*********************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.HousingRoomDescription
import net.hedtech.banner.general.overall.HousingRoomDescriptionReadOnly
import net.hedtech.banner.general.overall.ldm.v1.AvailableRoom
import net.hedtech.banner.general.system.Building
import net.hedtech.banner.general.system.Campus
import net.hedtech.banner.general.system.ldm.SiteDetailCompositeService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.After
import org.junit.Before
import org.junit.Test

class RoomCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    private static
    final String CONTENT_TYPE_ROOM_AVAILABILITY_V2 = "application/vnd.hedtech.integration.room-availability.v2+json"

    HousingRoomDescription i_success_housingRoomDescription
    def roomCompositeService
    Campus icampus
    Building ibuilding


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initiializeDataReferences()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    private void initiializeDataReferences() {
        Building building = Building.findByCode('CIS')
        i_success_housingRoomDescription = HousingRoomDescription.findByBuildingAndRoomNumber(building, '100')
        icampus = Campus.findByCode("M")
        ibuilding = Building.findByCode("GENERL")
    }


    @Test
    void testListWithoutPaginationParams() {
        List rooms = roomCompositeService.list([:])
        assertNotNull rooms
        assertFalse rooms.isEmpty()
    }


    @Test
    void testListWithPagination() {
        def paginationParams = [max: '20', offset: '0']
        List rooms = roomCompositeService.list(paginationParams)
        assertNotNull rooms
        assertFalse rooms.isEmpty()
        assertTrue rooms.size() <= 20
        assertTrue roomCompositeService.count() > 0
    }


    @Test
    void testListWithFilter() {
        //we will forcefully set the accept header so that the tests go through all possible code flows
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")
        def params = ['filter[0][value]': 'Classroom', 'filter[0][field]': 'roomLayoutType', 'filter[0][operator]': 'equals']
        List rooms = roomCompositeService.list(params)
        assertNotNull rooms
        assertFalse rooms.isEmpty()
        assertNull rooms.find { it.occupancies[0].roomLayoutType != 'Classroom' }
    }


    @Test
    void testListWithFilterForInvalidRoomLayoutType() {
        def params = ['filter[0][value]': 'XXXX', 'filter[0][field]': 'roomLayoutType', 'filter[0][operator]': 'equals']
        try {
            roomCompositeService.list(params)
            fail("This should have failed as the filter is invalid")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "missing.roomLayoutType"
        }
    }


    @Test
    void testGetInvalidGuid() {
        try {
            roomCompositeService.get('Invalid-guid')
            fail('This should have failed as the guid is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, "NotFoundException"
        }
    }


    @Test
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


    @Test
    void testGet() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")
        request.addHeader("Content-Type", CONTENT_TYPE_ROOM_AVAILABILITY_V2)

        assertNotNull i_success_housingRoomDescription
        AvailableRoom existingAvailRoom = roomCompositeService.fetchByRoomBuildingAndTerm(i_success_housingRoomDescription.roomNumber, i_success_housingRoomDescription.building, i_success_housingRoomDescription.termEffective)
        assertNotNull existingAvailRoom.guid

        AvailableRoom room = roomCompositeService.get(existingAvailRoom.guid)
        assertNotNull room.toString()
        assertEquals existingAvailRoom, room
        assertEquals existingAvailRoom.metadata.dataOrigin, room.metadata.dataOrigin
        assertEquals existingAvailRoom.buildingDetail, room.buildingDetail
        assertEquals existingAvailRoom.occupancies, room.occupancies
        assertEquals existingAvailRoom.guid, room.guid
    }


    @Test
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


    @Test
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


    @Test
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


    @Test
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


    @Test
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


    @Test
    void testListForInvalidStartTimeLength() {
        Map params = getParamsForRoomQuery()
        params.startTime = '00:11'
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the startTime is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'invalid.timeFormat'
        }
    }


    @Test
    void testListForInvalidStartTime() {
        Map params = getParamsForRoomQuery()
        params.startTime = '24:60:60'
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the startTime is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'invalid.timeFormat'
        }
    }


    @Test
    void testListForInvalidEndTimeLength() {
        Map params = getParamsForRoomQuery()
        params.endTime = '23:59'
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the endTime is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'invalid.timeFormat'
        }
    }


    @Test
    void testListForInvalidEndTime() {
        Map params = getParamsForRoomQuery()
        params.endTime = '24:60:60'
        try {
            roomCompositeService.list(params)
            fail('This should have failed as the endTime is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'invalid.timeFormat'
        }
    }


    @Test
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


    @Test
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


    @Test
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


    @Test
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


    @Test
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


    @Test
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


    @Test
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


    @Test
    void testListForMissingOccupanciesV2Header() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")
        request.addHeader("Content-Type", CONTENT_TYPE_ROOM_AVAILABILITY_V2)
        Map params = getParamsForRoomQuery()
        params.remove('occupancies')
        try {
            roomCompositeService.list(params)
            fail("This should have failed as the occupancies are missing")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "missing.occupancies"
        }
    }

    @Test
    void testListForMissingOccupancies() {
        Map params = getParamsForRoomQueryHeaderV4()
        params.remove('occupancies')
        try {
            roomCompositeService.list(params)
            fail("This should have failed as the occupancies are missing")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "missing.occupancies"
        }
    }


    @Test
    void testListForMissingRoomLayoutTypeV2Header() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")
        request.addHeader("Content-Type", CONTENT_TYPE_ROOM_AVAILABILITY_V2)
        Map params = getParamsForRoomQuery()
        params.occupancies[0]?.remove('roomLayoutType')
        try {
            roomCompositeService.list(params)
            fail("This should have failed as the occupancy roomLayoutType is missing")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "missing.roomLayoutType"
        }
    }

    @Test
    void testListForMissingRoomLayoutType() {
        Map params = getParamsForRoomQueryHeaderV4()
        params.roomTypes[0]?.remove('type')
        try {
            roomCompositeService.list(params)
            fail("This should have failed as the occupancy roomLayoutType is missing")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "missing.roomLayoutType"
        }
    }


    @Test
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


    @Test
    void testListForInvalidMaxOccupancyHeaderV2() {
        Map params = getParamsForRoomQuery()
        params.occupancies[0].maxOccupancy = 'abc'
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")
        request.addHeader("Content-Type", CONTENT_TYPE_ROOM_AVAILABILITY_V2)
        try {
            roomCompositeService.list(params)
            fail("This should have failed as the occupancy maxOccupancy must be an integer")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "invalid.maxOccupancy"
        }
    }

    @Test
    void testListForInvalidMaxOccupancyHeader() {
        Map params = getParamsForRoomQueryHeaderV4()
        params.occupancies[0].maxOccupancy = 'abc'
        try {
            roomCompositeService.list(params)
            fail("This should have failed as the occupancy maxOccupancy must be an integer")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "invalid.maxOccupancy"
        }
    }

    @Test
    void testQApiRoomAvailabilityV1ForValidParams() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v1+json")
        request.addHeader("Content-Type", "application/vnd.hedtech.integration.room-availability.v1+json")
        Map params = getParamsForRoomQuery()
        List<AvailableRoom> availableRooms = roomCompositeService.list(params)
        assertNotNull availableRooms
        assertFalse availableRooms.isEmpty()
        assertNull availableRooms.find { it.capacity < params.occupancies[0]?.maxOccupancy }
    }


    @Test
    void testListForBuildingAndSite() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")
        request.addHeader("Content-Type", CONTENT_TYPE_ROOM_AVAILABILITY_V2)

        Map params = getParamsForRoomQueryWithBuildingAndSite()

        List<AvailableRoom> availableRooms = roomCompositeService.list(params)
        assertNotNull availableRooms
        assertFalse availableRooms.isEmpty()
        assertTrue availableRooms.size() > 1
    }


    @Test
    void testQApiRoomAvailability_GenericMediaTypes_HeaderV2() {
        //we will forcefully set the accept header so that the tests go through all possible code flows
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")
        request.addHeader("Content-Type", CONTENT_TYPE_ROOM_AVAILABILITY_V2)
        Map params = getParamsForRoomQueryWithBuildingAndSite()
        List<AvailableRoom> availableRooms = roomCompositeService.list(params)
        assertNotNull availableRooms
        assertFalse availableRooms.isEmpty()
        assertTrue availableRooms.size() > 1
    }

    @Test
    void testQApiRoomAvailability_GenericMediaTypes() {
        //we will forcefully set the accept header so that the tests go through all possible code flows
        Map params = getParamsForRoomQueryWithBuildingAndSiteV4()
        List<AvailableRoom> availableRooms = roomCompositeService.list(params)
        assertNotNull availableRooms
        assertFalse availableRooms.isEmpty()
        assertTrue availableRooms.size() > 1
        assertNotNull availableRooms.find {it.occupancies[0].roomLayoutType = 'seminar' }
        assertNotNull availableRooms.find {it.roomDetails["type"] = 'classroom' }
    }


    @Test
    void testListForBuildingAndSiteNULL_HeaderV2() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")
        request.addHeader("Content-Type", CONTENT_TYPE_ROOM_AVAILABILITY_V2)

        Map params = getParamsForRoomQueryWithBuildingAndSite()

        params.put('building', null)
        params.put('site', null)

        List<AvailableRoom> availableRooms = roomCompositeService.list(params)
        assertNotNull availableRooms
        assertTrue availableRooms.isEmpty()
    }

    @Test
    void testListForBuildingAndSiteNULL() {
        Map params = getParamsForRoomQueryWithBuildingAndSiteV4()
        params.put('building', null)
        params.put('site', null)

        List<AvailableRoom> availableRooms = roomCompositeService.list(params)
        assertNotNull availableRooms
        assertTrue availableRooms.isEmpty()
    }


    @Test
    void testListForBuildingNotFoundHeaderV2() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")
        request.addHeader("Content-Type", CONTENT_TYPE_ROOM_AVAILABILITY_V2)

        Map params = getParamsForRoomQueryWithBuildingAndSite()
        params.put('building', 'xyz')

        try {
            roomCompositeService.list(params)
            fail('This should have failed as Building GUID is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'not.found.message'
        }
    }

    @Test
    void testListForBuildingNotFound() {
        Map params = getParamsForRoomQueryWithBuildingAndSiteV4()
        params.building = ["id": "X"]

        try {
            roomCompositeService.list(params)
            fail('This should have failed as Building GUID is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'not.found.message'
        }
    }


    @Test
    void testListForSiteNotFoundHeaderV2() {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v2+json")
        request.addHeader("Content-Type", CONTENT_TYPE_ROOM_AVAILABILITY_V2)

        Map params = getParamsForRoomQueryWithBuildingAndSite()
        params.put('site', 'xyz')

        try {
            roomCompositeService.list(params)
            fail('This should have failed as Site GUID is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'not.found.message'
        }
    }

    @Test
    void testListForSiteNotFound() {
        Map params = getParamsForRoomQueryWithBuildingAndSiteV4()
        params.site = ["id": "X"]

        try {
            roomCompositeService.list(params)
            fail('This should have failed as Site GUID is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'not.found.message'
        }
    }

    @Test
    void testBuildingataLink(){
        String buildingId = HousingRoomDescriptionReadOnly.findByRoomType('C')?.buildingGUID
        params.put('building.id',buildingId)
        List<AvailableRoom> availableRooms= roomCompositeService.list(params)
        availableRooms.each{
            availableRoom->
            assertEquals availableRoom?.buildingGUID,buildingId
        }
        params.put('building.id',buildingId.substring(4))
        shouldFail(ApplicationException) {
            roomCompositeService.list(params)
        }
    }


    private Map getParamsForRoomQuery() {
        return [
                max        : "20",
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
                ],
        ]
    }

    private Map getParamsForRoomQueryHeaderV4() {
        return [
                max        : "20",
                action     : [POST: "list"],
                occupancies: [[
                                      "maxOccupancy": 25,
                                      "type": "seminar"
                              ]],
                roomTypes : [[
                               type : "classroom"
                             ]],
                recurrence :[
                        repeatRule : [
                                    daysOfWeek: ["sunday","monday","tuesday"]
                                    ],
                        timePeriod : [
                                              startOn : "2015-09-14T13:30:00+00:00",
                                              endOn   : "2015-11-09T15:30:00+00:00"

                                     ]
                ]




        ]
    }


    private Map getParamsForRoomQueryWithBuildingAndSite() {
        Map params = getParamsForRoomQuery()
        params.put('building', GlobalUniqueIdentifier.fetchByLdmNameAndDomainKeys(BuildingCompositeService.LDM_NAME, ibuilding.code)[0].guid)
        params.put('site', GlobalUniqueIdentifier.fetchByLdmNameAndDomainKeys(SiteDetailCompositeService.LDM_NAME, icampus.code)[0].guid)
        return params
    }

    private Map getParamsForRoomQueryWithBuildingAndSiteV4() {
        Map params = getParamsForRoomQueryHeaderV4()
        params.building = ["id": GlobalUniqueIdentifier.fetchByLdmNameAndDomainKeys(BuildingCompositeService.LDM_NAME, ibuilding.code)[0].guid]
        params.site = ["id": GlobalUniqueIdentifier.fetchByLdmNameAndDomainKeys(SiteDetailCompositeService.LDM_NAME, icampus.code)[0].guid]
        return params
    }

    @Test
    void testListWithFilterV4Header() {
        //we will forcefully set the accept header so that the tests go through all possible code flows
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v4+json")
        def params = ['filter[0][value]': 'classroom', 'filter[0][field]': 'type', 'filter[0][operator]': 'equals']
        List rooms = roomCompositeService.list(params)
        assertNotNull rooms
        assertFalse rooms.isEmpty()
        assertNotNull rooms.find {it.occupancies[0].roomLayoutType = 'seminar' }
        assertNotNull rooms.find {it.roomDetails["type"] = 'classroom' }
    }

}

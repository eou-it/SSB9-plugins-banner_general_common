/*********************************************************************************
 Copyright 2010-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.general.overall


import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.general.system.*
import groovy.sql.Sql

class SectionMeetingTimeServiceIntegrationTests extends BaseIntegrationTestCase {

    def sectionMeetingTimeService


    @Before
    public void setUp() {
        formContext = ['GUAGMNU', 'GEIFUNC', 'GEAFUNC', 'SLAEVNT', 'SSAMATX', 'SFQSECT', 'SSASECT']// Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    /**
     * The SectionMeetingTime may be used for scheduling sections with a traditional term and also
     * for an event which is not associated with a term.  Integration tests are written for both
     * types of meeting times. 
     */

    @Test
    void testSectionMeetingTimeCreateTraditionalSection() {

        def sectionMeetingTime = newSectionMeetingTime()

        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]

        sectionMeetingTime = sectionMeetingTimeService.create(map)
        assertNotNull "Section Meeting Time ID is null in Section Meeting Time Service Tests", sectionMeetingTime.id
        assertNotNull "Section Meeting Time Course Reference Number is null in Section Meeting Time Service Tests", sectionMeetingTime.courseReferenceNumber
        assertNotNull "Section Meeting Time Term is null in Section Meeting Time Service Tests", sectionMeetingTime.term
        assertNotNull sectionMeetingTime.dataOrigin
        assertNotNull sectionMeetingTime.lastModifiedBy
        assertNotNull sectionMeetingTime.lastModified
        assertNotNull "SectionMeetingTime scheduleType is null in SectionMeetingTime Service Tests", sectionMeetingTime.scheduleType
        assertNotNull "SectionMeetingTime meetingType is null in SectionMeetingTime Service Tests", sectionMeetingTime.meetingType
    }


    @Test
    void testSectionMeetingTimeCreateOLRSection() {

        def sectionMeetingTime = newSectionMeetingTimeOLR()

        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]

        sectionMeetingTime = sectionMeetingTimeService.create(map)
        assertNotNull "Section Meeting Time ID is null in Section Meeting Time Service Tests", sectionMeetingTime.id
        assertNotNull "Section Meeting Time Course Reference Number is null in Section Meeting Time Service Tests", sectionMeetingTime.courseReferenceNumber
        assertNotNull "Section Meeting Time Term is null in Section Meeting Time Service Tests", sectionMeetingTime.term
        assertNotNull sectionMeetingTime.dataOrigin
        assertNotNull sectionMeetingTime.lastModifiedBy
        assertNotNull sectionMeetingTime.lastModified
        assertNotNull "SectionMeetingTime scheduleType is null in SectionMeetingTime Service Tests", sectionMeetingTime.scheduleType
        assertNotNull "SectionMeetingTime meetingType is null in SectionMeetingTime Service Tests", sectionMeetingTime.meetingType
    }



    @Test
    void testEventMeetingTimeCreate() {

        def sectionMeetingTime = newEventMeetingTime()
        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]
        sectionMeetingTime = sectionMeetingTimeService.create(map)
        assertNotNull "Section Meeting Time ID is null in Section Meeting Time Service Tests", sectionMeetingTime.id
        assertNotNull "Section Meeting Time Course Reference Number is null in Section Meeting Time Service Tests", sectionMeetingTime.courseReferenceNumber
        assertNotNull "Section Meeting Time Term is null in Section Meeting Time Service Tests", sectionMeetingTime.term
        assertNotNull sectionMeetingTime.dataOrigin
        assertNotNull sectionMeetingTime.lastModifiedBy
        assertNotNull sectionMeetingTime.lastModified
        assertNotNull "SectionMeetingTime function is null in SectionMeetingTime Service Tests", sectionMeetingTime.function
        assertNotNull "SectionMeetingTime committee is null in SectionMeetingTime Service Tests", sectionMeetingTime.committee
    }

    /**
     * The API should default the MeetingType of CLAS when a class meeting type is entered.
     */

    @Test
    void testSectionMeetingTimeValidCreateDefaultMeetingType() {

        def sectionMeetingTime = newSectionMeetingTime()
        sectionMeetingTime.meetingType = null

        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]

        sectionMeetingTime = sectionMeetingTimeService.create(map)
        assertNotNull "Section Meeting Time ID is null in Section Meeting Time Service Tests", sectionMeetingTime.id
        assertNotNull "Section Meeting Time Course Reference Number is null in Section Meeting Time Service Tests", sectionMeetingTime.courseReferenceNumber
        assertNotNull "Section Meeting Time Term is null in Section Meeting Time Service Tests", sectionMeetingTime.term
        assertNotNull "SectionMeetingTime scheduleType is null in SectionMeetingTime Service Tests", sectionMeetingTime.scheduleType

        //fetch the record to make sure that meeting type has been defaulted
        def sectionMeetingTimeList = SectionMeetingTime.fetchByTermAndCourseReferenceNumber("201410", "20431")
        assertTrue sectionMeetingTimeList.size() > 0

    }


    @Test
    void testSectionMeetingTimeInvalidCreateTerm() {

        def sectionMeetingTime = newSectionMeetingTime()
        //set the term to something invalid
        sectionMeetingTime.term = 'ENDTIME'
        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]
        try {
            sectionMeetingTimeService.create(map)
            fail "Should have failed with @@r1:invalid_term@@"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalid_term"
        }
    }


    @Test
    void testEventMeetingTimeInvalidCreateTerm() {

        def sectionMeetingTime = newEventMeetingTime()
        //set the term to something invalid
        sectionMeetingTime.term = "MEET"
        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]
        try {
            sectionMeetingTimeService.create(map)
            fail "Should have failed with @@r1:invalid_term@@"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalid_term"
        }
    }



    @Test
    void testEventMeetingTimeInvalidBeginTime() {

        def sectionMeetingTime = newEventMeetingTime()
        //set the begin time to something invalid
        sectionMeetingTime.beginTime = "0060"
        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]
        try {
            sectionMeetingTimeService.create(map)
            fail "Should have failed with @@r1:begin_time@@"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "begin_time"
        }
    }


    @Test
    void testEventMeetingTimeInvalidEndTime() {

        def sectionMeetingTime = newEventMeetingTime()
        //set the begin time to something invalid
        sectionMeetingTime.endTime = "2400"
        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]
        try {
            sectionMeetingTimeService.create(map)
            fail "Should have failed with @@r1:end_time@@"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "end_time"
        }
    }


    @Test
    void testEventMeetingTimeInvalidMissingBeginTime() {

        def sectionMeetingTime = newEventMeetingTime()
        //set the begin time to something invalid
        sectionMeetingTime.beginTime = ""
        sectionMeetingTime.endTime = "1100"
        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]
        try {
            sectionMeetingTimeService.create(map)
            fail "Should have failed with @@r1:begin_end_time@@"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "begin_end_time"
        }
    }


    @Test
    void testEventMeetingTimeInvalidMissingEndTime() {

        def sectionMeetingTime = newEventMeetingTime()
        //set the begin time to something invalid
        sectionMeetingTime.beginTime = "0900"
        sectionMeetingTime.endTime = ""
        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]
        try {
            sectionMeetingTimeService.create(map)
            fail "Should have failed with @@r1:begin_end_time@@"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "begin_end_time"
        }
    }


    @Test
    void testEventMeetingTimeInvalidBeginTimeGreaterThanEndTime() {

        def sectionMeetingTime = newEventMeetingTime()
        //set the begin time to something invalid
        sectionMeetingTime.endTime = "0001"
        sectionMeetingTime.beginTime = "0100"
        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]
        try {
            sectionMeetingTimeService.create(map)
            fail "Should have failed with @@r1:begin_time_greater_than_end_time@@"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "begin_time_greater_than_end_time"
        }
    }


    @Test
    void testSectionMeetingTimeMissingHoursWeek() {

        def sectionMeetingTime = newSectionMeetingTime()
        //remove the hoursWeek
        sectionMeetingTime.hoursWeek = null
        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]
        try {
            sectionMeetingTimeService.create(map)
            fail "Should have failed with @@r1:missing_hours_week@@"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "missing_hours_week"
        }
    }


    @Test
    void testSectionMeetingTimeUpdate() {

        def sectionMeetingTime = newSectionMeetingTime()
        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]
        sectionMeetingTime = sectionMeetingTimeService.create(map)
        assertNotNull "Class Meeting Time ID should not be null", sectionMeetingTime.id
        SectionMeetingTime sectionMeetingTimeUpdate = SectionMeetingTime.get(
                sectionMeetingTime.id)
        sectionMeetingTimeUpdate.monday = null
        sectionMeetingTimeUpdate.override = "O"
        sectionMeetingTimeUpdate.creditHourSession = new BigDecimal(1)
        def updateMap = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTimeUpdate]
        sectionMeetingTimeUpdate = sectionMeetingTimeService.update(updateMap)
        assertNull sectionMeetingTimeUpdate.monday
        assertEquals new BigDecimal(1), sectionMeetingTimeUpdate.creditHourSession, 0.001
        assertEquals "O", sectionMeetingTimeUpdate.override
    }


    @Test
    void testSectionMeetingTimeInvalidUpdateTraditionalSection() {

        def sectionMeetingTime = newSectionMeetingTime()
        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]
        sectionMeetingTime = sectionMeetingTimeService.create(map)

        assertNotNull "Class Meeting Time ID should not be null", sectionMeetingTime.id
        SectionMeetingTime sectionMeetingTimeUpdate = SectionMeetingTime.get(
                sectionMeetingTime.id)
        //update a field so the record becomes invalid
        sectionMeetingTimeUpdate.room = null
        sectionMeetingTimeUpdate.monday = "X"
        sectionMeetingTimeUpdate.override = "X"
        sectionMeetingTimeUpdate.creditHourSession = new BigDecimal(1)
        def updateMap = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTimeUpdate]
        shouldFail(ApplicationException) {
            sectionMeetingTimeUpdate = sectionMeetingTimeService.update(updateMap)
        }
    }


    @Test
    void testEventMeetingTimeUpdate() {

        def sectionMeetingTime = newEventMeetingTime()
        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]
        sectionMeetingTime = sectionMeetingTimeService.create(map)
        assertNotNull "Class Meeting Time ID should not be null", sectionMeetingTime.id
        SectionMeetingTime sectionMeetingTimeUpdate = SectionMeetingTime.get(
                sectionMeetingTime.id)
        sectionMeetingTimeUpdate.monday = null
        sectionMeetingTimeUpdate.tuesday = null
        sectionMeetingTimeUpdate.override = "O"
        def updateMap = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTimeUpdate]
        sectionMeetingTimeUpdate = sectionMeetingTimeService.update(updateMap)
        assertNull sectionMeetingTimeUpdate.monday
        assertNull sectionMeetingTimeUpdate.tuesday
        assertEquals "O", sectionMeetingTimeUpdate.override
    }


    @Test
    void testEventMeetingTimeInvalidUpdate() {

        def sectionMeetingTime = newEventMeetingTime()
        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]
        sectionMeetingTime = sectionMeetingTimeService.create(map)
        assertNotNull "Class Meeting Time ID should not be null", sectionMeetingTime.id
        SectionMeetingTime sectionMeetingTimeUpdate = SectionMeetingTime.get(
                sectionMeetingTime.id)
        //update to make it invalid
        sectionMeetingTimeUpdate.monday = "X"
        sectionMeetingTimeUpdate.tuesday = "X"
        sectionMeetingTimeUpdate.override = "X"
        def updateMap = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTimeUpdate]
        shouldFail(ApplicationException) {
            sectionMeetingTimeUpdate = sectionMeetingTimeService.update(updateMap)
        }
    }


    @Test
    void testReadOnlyCrn() {
        def sectionMeetingTime = SectionMeetingTime.findByTermAndCourseReferenceNumber("201410", "20001")
        assertNotNull sectionMeetingTime

        sectionMeetingTime.courseReferenceNumber = "20002"

        try {
            sectionMeetingTimeService.update([domainModel: sectionMeetingTime])
            fail "Should have failed with @@r1:readonlyFieldsCannotBeModified@@"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    @Test
    void testReadOnlyTerm() {
        def sectionMeetingTime = SectionMeetingTime.findByTermAndCourseReferenceNumber("201410", "20001")
        assertNotNull sectionMeetingTime

        sectionMeetingTime.term = "201420"

        try {
            sectionMeetingTimeService.update([domainModel: sectionMeetingTime])
            fail "Should have failed with @@r1:readonlyFieldsCannotBeModified@@"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    @Test
    void testSectionMeetingTimeDeleteTraditionalSection() {
        def sectionMeetingTime = newSectionMeetingTime()

        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]

        sectionMeetingTime = sectionMeetingTimeService.create(map)

        SectionMeetingTime sectionMeetingTimeUpdate = SectionMeetingTime.get(
                sectionMeetingTime.id)
        sectionMeetingTimeService.delete([domainModel: sectionMeetingTimeUpdate])

        assertNull "Section Meeting Time should have been deleted", SectionMeetingTime.get(sectionMeetingTimeUpdate.id)
    }



    @Test
    void testEventMeetingTimeDelete() {
        def sectionMeetingTime = newEventMeetingTime()

        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]

        sectionMeetingTime = sectionMeetingTimeService.create(map)

        SectionMeetingTime sectionMeetingTimeUpdate = SectionMeetingTime.get(
                sectionMeetingTime.id)
        sectionMeetingTimeService.delete(sectionMeetingTimeUpdate.id)

        assertNull "Event Meeting Time should have been deleted", SectionMeetingTime.get(sectionMeetingTimeUpdate.id)
    }


    @Test
    void testIsBuildingRoomAndNoTime() {
        def sectionMeetingTime = newSectionMeetingTime()
        sectionMeetingTime.building = Building.findByCode("ADAMS")
        sectionMeetingTime.room = "101"
        sectionMeetingTime.beginTime = null
        sectionMeetingTime.endTime = null

        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]

        sectionMeetingTime = sectionMeetingTimeService.create(map)

        assertTrue sectionMeetingTimeService.isBuildingRoomAndNoTimeDays(sectionMeetingTime)
    }


    @Test
    void testIsBuildingRoomAndNoDays() {
        def sectionMeetingTime = newSectionMeetingTime()
        sectionMeetingTime.building = Building.findByCode("ADAMS")
        sectionMeetingTime.room = "101"
        sectionMeetingTime.monday = null
        sectionMeetingTime.tuesday = null
        sectionMeetingTime.wednesday = null
        sectionMeetingTime.thursday = null
        sectionMeetingTime.friday = null
        sectionMeetingTime.saturday = null
        sectionMeetingTime.sunday = null

        def keyBlockMap = [term: sectionMeetingTime.term,
                courseReferenceNumber: sectionMeetingTime.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionMeetingTime]

        sectionMeetingTime = sectionMeetingTimeService.create(map)

        assertTrue sectionMeetingTimeService.isBuildingRoomAndNoTimeDays(sectionMeetingTime)
    }


    private def newSectionMeetingTime() {
        def myFormat = 'MM/dd/yyyy'
        def istartDate = Date.parse(myFormat, '01/01/2013')
        def iendDate = Date.parse(myFormat, '06/30/2023')
        def idayOfWeek = DayOfWeek.findByCode("T")
        def ischeduleType = "L"  //Unable to access ScheduleType
        def imeetingType = MeetingType.findByCode("CLAS")

        return new SectionMeetingTime(term: "201410",
                courseReferenceNumber: "20431",
                dayOfWeek: idayOfWeek,
                dayNumber: 1,
                beginTime: "0100",
                endTime: "0200",
                startDate: istartDate,
                endDate: iendDate,
                category: "03",
                sunday: null,
                monday: "M",
                tuesday: "T",
                wednesday: "W",
                thursday: "R",
                friday: "F",
                saturday: null,
                override: null,
                creditHourSession: 1,
                meetNumber: 1,
                hoursWeek: 1,
                scheduleType: ischeduleType,
                meetingType: imeetingType)
    }


    private def newSectionMeetingTimeOLR() {
        def iterm = Term.findByCode("201410")
        def idayOfWeek = DayOfWeek.findByCode("T")
        def ischeduleType = "L"  //Unable to access ScheduleType
        def imeetingType = MeetingType.findByCode("CLAS")

        return new SectionMeetingTime(term: iterm.code,
                courseReferenceNumber: "20349",
                dayOfWeek: idayOfWeek,
                dayNumber: 1,
                beginTime: "0100",
                endTime: "0200",
                startDate: iterm.startDate,
                endDate: iterm.endDate,
                category: "03",
                sunday: null,
                monday: "M",
                tuesday: "T",
                wednesday: "W",
                thursday: "R",
                friday: "F",
                saturday: null,
                override: null,
                creditHourSession: 1,
                meetNumber: 1,
                hoursWeek: 1,
                scheduleType: ischeduleType,
                meetingType: imeetingType)
    }


    private def newEventMeetingTime() {
        def iterm = "EVENT"
        def idayOfWeek = DayOfWeek.findByCode("T")
        def ischeduleType = "L"  //Unable to access ScheduleType
        def ifunction = new Function(code: "TTTTT",
                description: "TTTTT",
                etypCode: "EBRK")
        save ifunction
        def icommittee = CommitteeAndServiceType.findByCode("GRAD")
        def myFormat = 'MM/dd/yyyy'
        def istartDate = Date.parse(myFormat, '02/03/2009')
        def iendDate = Date.parse(myFormat, '06/03/2009')

        def isql = """insert into SLBEVNT ( slbevnt_crn,
                                            slbevnt_etyp_code,
                                            slbevnt_desc,
                                            slbevnt_comm_ind,
                                            slbevnt_activity_date )
                              values ( 'TTTTT',
                                       'TTTT',
                                       'Event Description',
                                       'Y', sysdate ) """
        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate(isql)
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }


        return new SectionMeetingTime(term: iterm,
                courseReferenceNumber: "TTTTT",
                dayOfWeek: idayOfWeek,
                dayNumber: 1,
                beginTime: "0100",
                endTime: "0200",
                startDate: istartDate,
                endDate: iendDate,
                sunday: null,
                monday: "M",
                tuesday: "T",
                wednesday: "W",
                thursday: "R",
                friday: "F",
                saturday: null,
                override: null,
                meetNumber: 1,
                committee: icommittee,
                function: ifunction,
                scheduleType: ischeduleType,
                category: 1)
    }


    @Test
    void testSectionMeetingTimeForSessionExists() {
        //test an existing section
        def sectionMeetingTimeForSession = SectionMeetingTimeService.isMeetingTimesForSession("201410", "20001", "01")
        assertTrue sectionMeetingTimeForSession
    }


    @Test
    void testSectionMeetingTimeForSessionDoesNotExist() {
        //test a section without meeting times for this category
        def sectionMeetingTimeForSession = SectionMeetingTimeService.isMeetingTimesForSession("201410", "20431", "02")
        assertFalse sectionMeetingTimeForSession
    }

}

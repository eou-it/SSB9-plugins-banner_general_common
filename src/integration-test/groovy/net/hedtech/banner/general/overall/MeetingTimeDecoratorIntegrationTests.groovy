/** *****************************************************************************
 Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.general.system.MeetingType

@Integration
@Rollback
class MeetingTimeDecoratorIntegrationTests extends BaseIntegrationTestCase {


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testNullNewSection() {
        def newSection = new MeetingTimeDecorator()
        assertNull newSection.term
        assertNull newSection.courseReferenceNumber
    }


    @Test
    void testNewSection() {
        // section has two meetings
        //        SSRMEET_TERM_CODE SSRMEET_CRN SSRMEET_DAYS_CODE SSRMEET_DAY_NUMBER     SSRMEET_BEGIN_TIME SSRMEET_END_TIME SSRMEET_BLDG_CODE SSRMEET_ROOM_CODE SSRMEET_ACTIVITY_DATE     SSRMEET_START_DATE        SSRMEET_END_DATE          SSRMEET_CATAGORY SSRMEET_SUN_DAY SSRMEET_MON_DAY SSRMEET_TUE_DAY SSRMEET_WED_DAY SSRMEET_THU_DAY SSRMEET_FRI_DAY SSRMEET_SAT_DAY SSRMEET_SCHD_CODE SSRMEET_OVER_RIDE SSRMEET_CREDIT_HR_SESS SSRMEET_MEET_NO        SSRMEET_HRS_WEEK       SSRMEET_FUNC_CODE SSRMEET_COMT_CODE SSRMEET_SCHS_CODE SSRMEET_MTYP_CODE SSRMEET_DATA_ORIGIN            SSRMEET_USER_ID                SSRMEET_SURROGATE_ID   SSRMEET_VERSION        SSRMEET_VPDI_CODE
        //        ----------------- ----------- ----------------- ---------------------- ------------------ ---------------- ----------------- ----------------- ------------------------- ------------------------- ------------------------- ---------------- --------------- --------------- --------------- --------------- --------------- --------------- --------------- ----------------- ----------------- ---------------------- ---------------------- ---------------------- ----------------- ----------------- ----------------- ----------------- ------------------------------ ------------------------------ ---------------------- ---------------------- -----------------
        //        201410            20199                                                0900               1000                                                 17-JAN-12                 10-JAN-10                 20-MAY-10                 03                                               T                               R                                               L                                                          38                     2                                                                            CLAS              GRAILS                         GRAILS                         53441                  0
        //        201410            20199                                                1200               1300                                                 17-JAN-12                 10-JAN-10                 20-MAY-10                 04                                               T                               R                               S               L                                                          56                     2                                                                            CLAS              GRAILS                         GRAILS                         53442                  0
        //
        def term = "201410"
        def crn = "20199"

        def courseLists = MeetingTimeSearch.findAllByTermAndCourseReferenceNumber(term, crn)
        assertEquals courseLists.size(), 2
        assertTrue courseLists[0] instanceof MeetingTimeSearch

        def newMeeting = new MeetingTimeDecorator(courseLists[0])
        assertEquals newMeeting.term, courseLists[0].term
        assertEquals newMeeting.courseReferenceNumber, courseLists[0].courseReferenceNumber
        assertEquals newMeeting.category, courseLists[0].category
        assertEquals newMeeting.beginTime, courseLists[0].beginTime
        assertEquals newMeeting.endTime, courseLists[0].endTime
        assertEquals newMeeting.room, courseLists[0].room
        assertEquals newMeeting.startDate, courseLists[0].startDate
        assertEquals newMeeting.endDate, courseLists[0].endDate
        assertEquals newMeeting.sunday, courseLists[0].sunday
        assertEquals newMeeting.monday, courseLists[0].monday
        assertEquals newMeeting.tuesday, courseLists[0].tuesday
        assertEquals newMeeting.wednesday, courseLists[0].wednesday
        assertEquals newMeeting.thursday, courseLists[0].thursday
        assertEquals newMeeting.friday, courseLists[0].friday
        assertEquals newMeeting.saturday, courseLists[0].saturday
        assertEquals newMeeting.creditHourSession, courseLists[0].creditHourSession
        assertEquals newMeeting.hoursWeek, courseLists[0].hoursWeek, 0
        assertEquals newMeeting.building, courseLists[0].building
        assertEquals newMeeting.meetingScheduleType, courseLists[0].scheduleType
    }


    @Test
    void testToString() {
        def term = "201410"
        def crn = "20199"

        def courseLists = MeetingTimeSearch.findAllByTermAndCourseReferenceNumber(term, crn)
        assertEquals courseLists.size(), 2
        def newMeeting = new MeetingTimeDecorator(courseLists[0])

        def printSection = newMeeting.toString()
        if (!printSection.contains("MeetingTimeDecorator[")) {
            fail "Meeting Decorator toString error: ${printSection}"
        }
    }

    @Test
    void testMeetingTypeAndMeetingTypeDescription() {
        def existingMeetings = MeetingTimeSearch.findAllByCourseReferenceNumberAndTerm("20036", "201410")
        assertNotNull existingMeetings

        existingMeetings.each {meetingTime ->
            assertNotNull meetingTime
            assertNotNull meetingTime.meetingType

            def meetingTimeDecorator = new MeetingTimeDecorator(meetingTime)
            assertNotNull meetingTimeDecorator.meetingType
            assertNotNull meetingTimeDecorator.meetingTypeDescription
            def meetingTypeDescription = MeetingType.findByCode(meetingTime.meetingType)?.description
            assertEquals meetingTypeDescription, meetingTimeDecorator.meetingTypeDescription
        }
    }

    @Test
    void testMeetingTypeIsNull() {
        def existingMeetings = MeetingTimeSearch.findAllByCourseReferenceNumberAndTerm("20036", "201410")
        assertNotNull existingMeetings

        existingMeetings.each {meetingTime ->
            assertNotNull meetingTime
            assertNotNull meetingTime.meetingType

            //Explicitly set meetingType to null to make sure description is not returned.
            meetingTime.meetingType = null
            def meetingTimeDecorator = new MeetingTimeDecorator(meetingTime)
            assertNull meetingTimeDecorator.meetingType
            assertNull meetingTimeDecorator.meetingTypeDescription
        }
    }

}

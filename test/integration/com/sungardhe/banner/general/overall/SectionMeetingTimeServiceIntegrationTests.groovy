/** *****************************************************************************

 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

package com.sungardhe.banner.general.overall

import com.sungardhe.banner.exceptions.ApplicationException
import com.sungardhe.banner.testing.BaseIntegrationTestCase
import com.sungardhe.banner.general.system.*
import groovy.sql.Sql

class SectionMeetingTimeServiceIntegrationTests extends BaseIntegrationTestCase {

    def sectionMeetingTimeService


    protected void setUp() {
        formContext = ['GEIFUNC', 'GEAFUNC', 'SLAEVNT', 'SSAMATX', 'SFQSECT', 'SSASECT']// Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }

    /**
     * The SectionMeetingTime may be used for scheduling sections with a traditional term and also
     * for an event which is not associated with a term.  Integration tests are written for both
     * types of meeting times. 
     */

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
        assertEquals new BigDecimal(1), sectionMeetingTimeUpdate.creditHourSession
        assertEquals "O", sectionMeetingTimeUpdate.override
    }


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
        /**
         * Please use the appropriate finder methods to load the references here
         * This area is being protected to preserve the customization on regeneration
         */
        /*PROTECTED REGION ID(sectionmeetingtime_service_integration_tests_data_fetch_for_references) ENABLED START*/
        def iterm = Term.findByCode("201410")
        def idayOfWeek = DayOfWeek.findByCode("T")
        def ischeduleType = "L"  //Unable to access ScheduleType
        def imeetingType = MeetingType.findByCode("CLAS")
        /*PROTECTED REGION END*/

        return new SectionMeetingTime(term: iterm.code,
                courseReferenceNumber: "20431",
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
                lastModified: new Date(),
                lastModifiedBy: "test",
                dataOrigin: "Banner",
                scheduleType: ischeduleType,
                meetingType: imeetingType)

    }


    private def newSectionMeetingTimeOLR() {
        /**
         * Please use the appropriate finder methods to load the references here
         * This area is being protected to preserve the customization on regeneration
         */
        /*PROTECTED REGION ID(sectionmeetingtime_service_integration_tests_data_fetch_for_references) ENABLED START*/
        def iterm = Term.findByCode("201410")
        def idayOfWeek = DayOfWeek.findByCode("T")
        def ischeduleType = "L"  //Unable to access ScheduleType
        def imeetingType = MeetingType.findByCode("CLAS")
        /*PROTECTED REGION END*/

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
                lastModified: new Date(),
                lastModifiedBy: "test",
                dataOrigin: "Banner",
                scheduleType: ischeduleType,
                meetingType: imeetingType)

    }


    private def newEventMeetingTime() {
        /**
         * Please use the appropriate finder methods to load the references here
         * This area is being protected to preserve the customization on regeneration
         */
        /*PROTECTED REGION ID(sectionmeetingtime_service_integration_tests_data_fetch_for_references) ENABLED START*/
        def iterm = "EVENT"
        def idayOfWeek = DayOfWeek.findByCode("T")
        def ischeduleType = "L"  //Unable to access ScheduleType
        def ifunction = new Function(code: "TTTTT",
                description: "TTTTT",
                etypCode: "TTTT",
                lastModified: new Date(),
                lastModifiedBy: "test", dataOrigin: "Banner")
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

        /*PROTECTED REGION END*/


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

/**
 * Please put all the custom service tests in this protected section to protect the code
 * from being overwritten on re-generation
 */
/*PROTECTED REGION ID(sectionmeetingtime_custom_service_integration_test_methods) ENABLED START*/


    void testSectionMeetingTimeForSessionExists() {
        //test an existing section
        def sectionMeetingTimeForSession = SectionMeetingTimeService.isMeetingTimesForSession("201410", "20001", "01")
        assertTrue sectionMeetingTimeForSession
    }


    void testSectionMeetingTimeForSessionDoesNotExist() {
        //test a section without meeting times for this category
        def sectionMeetingTimeForSession = SectionMeetingTimeService.isMeetingTimesForSession("201410", "20431", "02")
        assertFalse sectionMeetingTimeForSession
    }

/*PROTECTED REGION END*/

}
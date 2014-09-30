
/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.general.overall

import groovy.sql.Sql
import net.hedtech.banner.general.system.*
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Ignore
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

class SectionMeetingTimeIntegrationTests extends BaseIntegrationTestCase {
    //Test data for creating new domain instance
    //Valid test data (For success tests)
    def i_success_term
    def i_success_dayOfWeek
    def i_success_building
    def i_success_scheduleType
    def i_success_function
    def i_success_committeeAndServiceType
    def i_success_scheduleToolStatus
    def i_success_meetingType
    def i_success_startDate
    def i_success_endDate

    def i_success_courseReferenceNumber = "20349"
    def i_success_dayNumber = 1
    def i_success_beginTime = "0900"
    def i_success_endTime = "0950"
    def i_success_room = "101"
    def i_success_category = "03"
    def i_success_sunday = null
    def i_success_monday = "M"
    def i_success_tuesday = null
    def i_success_wednesday = "W"
    def i_success_thursday = null
    def i_success_friday = "F"
    def i_success_saturday = null
    def i_success_override = "O"
    def i_success_creditHourSession = 1
    def i_success_meetNumber = 1
    def i_success_hoursWeek = 1

    //Invalid test data (For failure tests)
    def i_failure_term
    def i_failure_dayOfWeek
    def i_failure_building
    def i_failure_scheduleType
    def i_failure_function
    def i_failure_committeeAndServiceType
    def i_failure_scheduleToolStatus
    def i_failure_meetingType
    def i_failure_startDate
    def i_failure_endDate

    def i_failure_courseReferenceNumber = "20349"
    def i_failure_dayNumber = 1
    def i_failure_beginTime = null
    def i_failure_endTime = null
    def i_failure_room = "102"
    def i_failure_category = "TT"
    def i_failure_sunday = null
    def i_failure_monday = "X"
    def i_failure_tuesday = null
    def i_failure_wednesday = null
    def i_failure_thursday = "R"
    def i_failure_friday = null
    def i_failure_saturday = "S"
    def i_failure_override = null
    def i_failure_creditHourSession = 1
    def i_failure_meetNumber = 1
    def i_failure_hoursWeek = 1

    //Test data for creating updating domain instance
    //Valid test data (For success tests)
    def u_success_term
    def u_success_dayOfWeek
    def u_success_building
    def u_success_scheduleType
    def u_success_function
    def u_success_committeeAndServiceType
    def u_success_scheduleToolStatus
    def u_success_meetingType
    def u_success_startDate
    def u_success_endDate

    def u_success_courseReferenceNumber = "20349"
    def u_success_dayNumber = 1
    def u_success_beginTime = "1100"
    def u_success_endTime = "1215"
    def u_success_room = "103"
    def u_success_category = "TT"
    def u_success_sunday = null
    def u_success_monday = null
    def u_success_tuesday = "T"
    def u_success_wednesday = null
    def u_success_thursday = "R"
    def u_success_friday = null
    def u_success_saturday = null
    def u_success_override = null
    def u_success_creditHourSession = 1
    def u_success_meetNumber = 1
    def u_success_hoursWeek = 3
    //Valid test data (For failure tests)
    def u_failure_term
    def u_failure_dayOfWeek
    def u_failure_building
    def u_failure_scheduleType
    def u_failure_function
    def u_failure_committeeAndServiceType
    def u_failure_scheduleToolStatus
    def u_failure_meetingType
    def u_failure_startDate
    def u_failure_endDate

    def u_failure_courseReferenceNumber = "20349"
    def u_failure_dayNumber = 1
    def u_failure_beginTime = "1500"
    def u_failure_endTime = "1700"
    def u_failure_room = "201"
    def u_failure_category = "TT"
    def u_failure_sunday = null
    def u_failure_monday = null
    def u_failure_tuesday = "X"
    def u_failure_wednesday = null
    def u_failure_thursday = null
    def u_failure_friday = null
    def u_failure_saturday = null
    def u_failure_override = null
    def u_failure_creditHourSession = 1
    def u_failure_meetNumber = 1
    def u_failure_hoursWeek = 1

    //Test data for updating existing records
    //Valid test data (For success tests)
    def u_success_existing_term
    def u_success_existing_courseReferenceNumber = "20001"


    void setUp() {
        formContext = ['GEIFUNC', 'GEAFUNC', 'SLAEVNT', 'SSAMATX', 'SFQSECT', 'SSASECT'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
        initializeTestDataForReferences()
    }

    //This method is used to initialize test data for references.
    //A method is required to execute database calls as it requires a active transaction


    void initializeTestDataForReferences() {

        /**
         * NOTE:  This domain supports two types of meetings:
         *   1.  Traditional class sections for term
         *   2.  Events which are not term based
         * Because of this, the term is a simple String to support the value of 'EVENT'.
         * Tests exist in the service integration tests for class sections and meeting events.
         */
        //Valid test data (For success tests)
        i_success_term = Term.findByCode("201410").code
        i_success_dayOfWeek = DayOfWeek.findByCode("T")
        i_success_building = Building.findByCode("BIOL")
        i_success_scheduleType = "L"
        i_success_function = null
        i_success_committeeAndServiceType = CommitteeAndServiceType.findByCode("GRAD")
        i_success_scheduleToolStatus = null
        i_success_meetingType = MeetingType.findByCode("CLAS")
        i_success_startDate = Term.findByCode(i_success_term).startDate
        i_success_endDate = Term.findByCode(i_success_term).endDate

        //Invalid test data (For failure tests)
        i_failure_term = Term.findByCode("201410").code
        i_failure_dayOfWeek = DayOfWeek.findByCode("T")
        i_failure_building = Building.findByCode("BIOL")
        i_failure_scheduleType = "LAB"
        i_failure_function = null
        i_failure_committeeAndServiceType = CommitteeAndServiceType.findByCode("FAC")
        i_failure_scheduleToolStatus = ScheduleToolStatus.findByCode("ASM")
        i_failure_meetingType = MeetingType.findByCode("DARK")
        i_failure_startDate = Term.findByCode(i_failure_term).startDate
        i_failure_endDate = null

        //Valid test data (For success tests)
        u_success_term = Term.findByCode("201410").code
        u_success_dayOfWeek = DayOfWeek.findByCode("T")
        u_success_building = Building.findByCode("BIOL")
        u_success_scheduleType = "A"
        u_success_function = null
        u_success_committeeAndServiceType = CommitteeAndServiceType.findByCode("DISS")
        u_success_scheduleToolStatus = ScheduleToolStatus.findByCode("NSM")
        u_success_meetingType = MeetingType.findByCode("LAB")
        u_success_startDate = Term.findByCode(u_success_term).startDate
        u_success_endDate = Term.findByCode(u_success_term).endDate

        //Valid test data (For failure tests)
        u_failure_term = Term.findByCode("201410").code
        u_failure_dayOfWeek = DayOfWeek.findByCode("T")
        u_failure_building = Building.findByCode("BROWN")
        u_failure_scheduleType = "IN"
        u_failure_function = null
        u_failure_committeeAndServiceType = CommitteeAndServiceType.findByCode("MBA")
        u_failure_scheduleToolStatus = ScheduleToolStatus.findByCode("HSM")
        u_failure_meetingType = MeetingType.findByCode("RECI")
        u_failure_startDate = Term.findByCode(u_failure_term).startDate
        u_failure_endDate = Term.findByCode(u_failure_term).endDate

        //Valid test data (For success tests)
        u_success_existing_term = Term.findByCode("201410").code
    }


    void tearDown() {
        super.tearDown()
    }

    //Test creation of a valid record


    void testCreateValidSectionMeetingTime() {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        sectionMeetingTime.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sectionMeetingTime.id
        assertNotNull sectionMeetingTime.dataOrigin
        assertNotNull sectionMeetingTime.lastModified
        assertNotNull sectionMeetingTime.lastModifiedBy
    }

    //Test creation of an invalid record


    void testCreateInvalidSectionMeetingTime() {
        def sectionMeetingTime = newInvalidForCreateSectionMeetingTime()
        shouldFail {
            sectionMeetingTime.save(failOnError: true, flush: true)
        }
    }

    //Test valid update of a new record


    void testUpdateValidSectionMeetingTime() {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        sectionMeetingTime.save(failOnError: true, flush: true)
        assertNotNull sectionMeetingTime.id
        assertEquals 0L, sectionMeetingTime.version
        assertEquals i_success_courseReferenceNumber, sectionMeetingTime.courseReferenceNumber
        assertEquals i_success_dayNumber, sectionMeetingTime.dayNumber
        assertEquals i_success_beginTime, sectionMeetingTime.beginTime
        assertEquals i_success_endTime, sectionMeetingTime.endTime
        assertEquals i_success_room, sectionMeetingTime.room
        assertEquals i_success_startDate, sectionMeetingTime.startDate
        assertEquals i_success_endDate, sectionMeetingTime.endDate
        assertEquals i_success_category, sectionMeetingTime.category
        assertEquals i_success_sunday, sectionMeetingTime.sunday
        assertEquals i_success_monday, sectionMeetingTime.monday
        assertEquals i_success_tuesday, sectionMeetingTime.tuesday
        assertEquals i_success_wednesday, sectionMeetingTime.wednesday
        assertEquals i_success_thursday, sectionMeetingTime.thursday
        assertEquals i_success_friday, sectionMeetingTime.friday
        assertEquals i_success_saturday, sectionMeetingTime.saturday
        assertEquals i_success_override, sectionMeetingTime.override
        assertEquals i_success_creditHourSession, sectionMeetingTime.creditHourSession
        assertEquals i_success_meetNumber, sectionMeetingTime.meetNumber
        assertEquals i_success_hoursWeek, sectionMeetingTime.hoursWeek

        //Update the entity
        sectionMeetingTime.courseReferenceNumber = u_success_courseReferenceNumber
        sectionMeetingTime.dayNumber = u_success_dayNumber
        sectionMeetingTime.beginTime = u_success_beginTime
        sectionMeetingTime.endTime = u_success_endTime
        sectionMeetingTime.room = u_success_room
        sectionMeetingTime.startDate = u_success_startDate
        sectionMeetingTime.endDate = u_success_endDate
        sectionMeetingTime.category = u_success_category
        sectionMeetingTime.sunday = u_success_sunday
        sectionMeetingTime.monday = u_success_monday
        sectionMeetingTime.tuesday = u_success_tuesday
        sectionMeetingTime.wednesday = u_success_wednesday
        sectionMeetingTime.thursday = u_success_thursday
        sectionMeetingTime.friday = u_success_friday
        sectionMeetingTime.saturday = u_success_saturday
        sectionMeetingTime.override = u_success_override
        sectionMeetingTime.creditHourSession = u_success_creditHourSession
        sectionMeetingTime.meetNumber = u_success_meetNumber
        sectionMeetingTime.hoursWeek = u_success_hoursWeek

        sectionMeetingTime.term = u_success_term
        sectionMeetingTime.dayOfWeek = u_success_dayOfWeek
        sectionMeetingTime.building = u_success_building
        sectionMeetingTime.function = u_success_function
        sectionMeetingTime.committee = u_success_committeeAndServiceType
        sectionMeetingTime.scheduleToolStatus = u_success_scheduleToolStatus
        sectionMeetingTime.meetingType = u_success_meetingType
        sectionMeetingTime.save(failOnError: true, flush: true)

        //Assert for successful update
        sectionMeetingTime = SectionMeetingTime.get(sectionMeetingTime.id)
        assertEquals 1L, sectionMeetingTime?.version
        assertEquals u_success_courseReferenceNumber, sectionMeetingTime.courseReferenceNumber
        assertEquals u_success_dayNumber, sectionMeetingTime.dayNumber
        assertEquals u_success_beginTime, sectionMeetingTime.beginTime
        assertEquals u_success_endTime, sectionMeetingTime.endTime
        assertEquals u_success_room, sectionMeetingTime.room
        assertEquals u_success_startDate, sectionMeetingTime.startDate
        assertEquals u_success_endDate, sectionMeetingTime.endDate
        assertEquals u_success_category, sectionMeetingTime.category
        assertEquals u_success_sunday, sectionMeetingTime.sunday
        assertEquals u_success_monday, sectionMeetingTime.monday
        assertEquals u_success_tuesday, sectionMeetingTime.tuesday
        assertEquals u_success_wednesday, sectionMeetingTime.wednesday
        assertEquals u_success_thursday, sectionMeetingTime.thursday
        assertEquals u_success_friday, sectionMeetingTime.friday
        assertEquals u_success_saturday, sectionMeetingTime.saturday
        assertEquals u_success_override, sectionMeetingTime.override
        assertEquals u_success_creditHourSession, sectionMeetingTime.creditHourSession
        assertEquals u_success_meetNumber, sectionMeetingTime.meetNumber
        assertEquals u_success_hoursWeek, sectionMeetingTime.hoursWeek

        sectionMeetingTime.term = u_success_term
        sectionMeetingTime.dayOfWeek = u_success_dayOfWeek
        sectionMeetingTime.building = u_success_building
        sectionMeetingTime.scheduleType = u_success_scheduleType
        sectionMeetingTime.function = u_success_function
        sectionMeetingTime.committee = u_success_committeeAndServiceType
        sectionMeetingTime.scheduleToolStatus = u_success_scheduleToolStatus
        sectionMeetingTime.meetingType = u_success_meetingType
    }

    //Test invalid update of a new record


    void testUpdateInvalidSectionMeetingTime() {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        sectionMeetingTime.save(failOnError: true, flush: true)
        assertNotNull sectionMeetingTime.id
        assertEquals 0L, sectionMeetingTime.version
        assertEquals i_success_courseReferenceNumber, sectionMeetingTime.courseReferenceNumber
        assertEquals i_success_dayNumber, sectionMeetingTime.dayNumber
        assertEquals i_success_beginTime, sectionMeetingTime.beginTime
        assertEquals i_success_endTime, sectionMeetingTime.endTime
        assertEquals i_success_room, sectionMeetingTime.room
        assertEquals i_success_startDate, sectionMeetingTime.startDate
        assertEquals i_success_endDate, sectionMeetingTime.endDate
        assertEquals i_success_category, sectionMeetingTime.category
        assertEquals i_success_sunday, sectionMeetingTime.sunday
        assertEquals i_success_monday, sectionMeetingTime.monday
        assertEquals i_success_tuesday, sectionMeetingTime.tuesday
        assertEquals i_success_wednesday, sectionMeetingTime.wednesday
        assertEquals i_success_thursday, sectionMeetingTime.thursday
        assertEquals i_success_friday, sectionMeetingTime.friday
        assertEquals i_success_saturday, sectionMeetingTime.saturday
        assertEquals i_success_override, sectionMeetingTime.override
        assertEquals i_success_creditHourSession, sectionMeetingTime.creditHourSession
        assertEquals i_success_meetNumber, sectionMeetingTime.meetNumber
        assertEquals i_success_hoursWeek, sectionMeetingTime.hoursWeek

        //Update the entity with invalid values
        sectionMeetingTime.courseReferenceNumber = u_failure_courseReferenceNumber
        sectionMeetingTime.dayNumber = u_failure_dayNumber
        sectionMeetingTime.beginTime = u_failure_beginTime
        sectionMeetingTime.endTime = u_failure_endTime
        sectionMeetingTime.room = u_failure_room
        sectionMeetingTime.startDate = u_failure_startDate
        sectionMeetingTime.endDate = u_failure_endDate
        sectionMeetingTime.category = u_failure_category
        sectionMeetingTime.sunday = u_failure_sunday
        sectionMeetingTime.monday = u_failure_monday
        sectionMeetingTime.tuesday = u_failure_tuesday
        sectionMeetingTime.wednesday = u_failure_wednesday
        sectionMeetingTime.thursday = u_failure_thursday
        sectionMeetingTime.friday = u_failure_friday
        sectionMeetingTime.saturday = u_failure_saturday
        sectionMeetingTime.override = u_failure_override
        sectionMeetingTime.creditHourSession = u_failure_creditHourSession
        sectionMeetingTime.meetNumber = u_failure_meetNumber
        sectionMeetingTime.hoursWeek = u_failure_hoursWeek

        sectionMeetingTime.term = u_failure_term
        sectionMeetingTime.dayOfWeek = u_failure_dayOfWeek
        sectionMeetingTime.building = u_failure_building
        sectionMeetingTime.scheduleType = u_failure_scheduleType
        sectionMeetingTime.function = u_failure_function
        sectionMeetingTime.committee = u_failure_committeeAndServiceType
        sectionMeetingTime.scheduleToolStatus = u_failure_scheduleToolStatus
        sectionMeetingTime.meetingType = u_failure_meetingType
        shouldFail {
            sectionMeetingTime.save(failOnError: true, flush: true)
        }
    }

    //Test optimistic lock with creation and update of new record


    void testOptimisticLock() {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        sectionMeetingTime.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update SV_SSRMEET set SSRMEET_VERSION = 999 where SSRMEET_SURROGATE_ID = ?", [sectionMeetingTime.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        sectionMeetingTime.courseReferenceNumber = u_success_courseReferenceNumber
        sectionMeetingTime.dayNumber = u_success_dayNumber
        sectionMeetingTime.beginTime = u_success_beginTime
        sectionMeetingTime.endTime = u_success_endTime
        sectionMeetingTime.room = u_success_room
        sectionMeetingTime.startDate = u_success_startDate
        sectionMeetingTime.endDate = u_success_endDate
        sectionMeetingTime.category = u_success_category
        sectionMeetingTime.sunday = u_success_sunday
        sectionMeetingTime.monday = u_success_monday
        sectionMeetingTime.tuesday = u_success_tuesday
        sectionMeetingTime.wednesday = u_success_wednesday
        sectionMeetingTime.thursday = u_success_thursday
        sectionMeetingTime.friday = u_success_friday
        sectionMeetingTime.saturday = u_success_saturday
        sectionMeetingTime.override = u_success_override
        sectionMeetingTime.creditHourSession = u_success_creditHourSession
        sectionMeetingTime.meetNumber = u_success_meetNumber
        sectionMeetingTime.hoursWeek = u_success_hoursWeek
        shouldFail(HibernateOptimisticLockingFailureException) {
            sectionMeetingTime.save(failOnError: true, flush: true)
        }
    }

    //Test delete of a new record
    void testDeleteSectionMeetingTime() {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        sectionMeetingTime.save(failOnError: true, flush: true)
        def id = sectionMeetingTime.id
        assertNotNull id
        sectionMeetingTime.delete()
        assertNull SectionMeetingTime.get(id)
    }

    //Test that a valid record is validated
    void testValidation() {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        assertTrue "SectionMeetingTime could not be validated as expected due to ${sectionMeetingTime.errors}", sectionMeetingTime.validate()
    }

    //Test that not null fields fail validation
    void testNullValidationFailure() {
        def sectionMeetingTime = new SectionMeetingTime()
        assertFalse "SectionMeetingTime should have failed validation", sectionMeetingTime.validate()
        assertErrorsFor sectionMeetingTime, 'nullable',
                [
                        'courseReferenceNumber',
                        'startDate',
                        'endDate',
                        'term'
                ]
        assertNoErrorsFor sectionMeetingTime,
                [
                        'dayNumber',
                        'beginTime',
                        'endTime',
                        'room',
                        'category',
                        'sunday',
                        'monday',
                        'tuesday',
                        'wednesday',
                        'thursday',
                        'friday',
                        'saturday',
                        'override',
                        'creditHourSession',
                        'meetNumber',
                        'hoursWeek',
                        'dayOfWeek',
                        'building',
                        'scheduleType',
                        'function',
                        'committee',
                        'scheduleToolStatus',
                        'meetingType'
                ]
    }

    //Test that exceeding maximum size of fields causes failure
    void testMaxSizeValidationFailures() {
        def sectionMeetingTime = new SectionMeetingTime(
                term: "XXXXXXX",
                courseReferenceNumber: "XXXXXX",
                beginTime: "XXXXXX",
                endTime: "XXXXXX",
                room: "XXXXXXXXXXXX",
                category: "XXXX",
                sunday: "XXX",
                monday: "XXX",
                tuesday: "XXX",
                wednesday: "XXX",
                thursday: "XXX",
                friday: "XXX",
                saturday: "XXX",
                override: "XXX",
                scheduleType: "XXXX")
        assertFalse "SectionMeetingTime should have failed validation", sectionMeetingTime.validate()
        assertErrorsFor sectionMeetingTime, 'maxSize', ['term', 'courseReferenceNumber', 'beginTime', 'endTime', 'room', 'category', 'sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'override', 'scheduleType']
    }

    //Create a valid section meeting time record
    private def newValidForCreateSectionMeetingTime() {
        def sectionMeetingTime = new SectionMeetingTime(
                courseReferenceNumber: i_success_courseReferenceNumber,
                dayNumber: i_success_dayNumber,
                beginTime: i_success_beginTime,
                endTime: i_success_endTime,
                room: i_success_room,
                startDate: i_success_startDate,
                endDate: i_success_endDate,
                category: i_success_category,
                sunday: i_success_sunday,
                monday: i_success_monday,
                tuesday: i_success_tuesday,
                wednesday: i_success_wednesday,
                thursday: i_success_thursday,
                friday: i_success_friday,
                saturday: i_success_saturday,
                override: i_success_override,
                creditHourSession: i_success_creditHourSession,
                meetNumber: i_success_meetNumber,
                hoursWeek: i_success_hoursWeek,
                term: i_success_term,
                dayOfWeek: i_success_dayOfWeek,
                building: i_success_building,
                scheduleType: i_success_scheduleType,
                function: i_success_function,
                committee: i_success_committeeAndServiceType,
                scheduleToolStatus: i_success_scheduleToolStatus,
                meetingType: i_success_meetingType
        )
        return sectionMeetingTime
    }

    //Create an invalid section meeting time record
    private def newInvalidForCreateSectionMeetingTime() {
        def sectionMeetingTime = new SectionMeetingTime(
                courseReferenceNumber: i_failure_courseReferenceNumber,
                dayNumber: i_failure_dayNumber,
                beginTime: i_failure_beginTime,
                endTime: i_failure_endTime,
                room: i_failure_room,
                startDate: i_failure_startDate,
                endDate: i_failure_endDate,
                category: i_failure_category,
                sunday: i_failure_sunday,
                monday: i_failure_monday,
                tuesday: i_failure_tuesday,
                wednesday: i_failure_wednesday,
                thursday: i_failure_thursday,
                friday: i_failure_friday,
                saturday: i_failure_saturday,
                override: i_failure_override,
                creditHourSession: i_failure_creditHourSession,
                meetNumber: i_failure_meetNumber,
                hoursWeek: i_failure_hoursWeek,
                term: i_failure_term,
                dayOfWeek: i_failure_dayOfWeek,
                building: i_failure_building,
                scheduleType: i_failure_scheduleType,
                function: i_failure_function,
                committee: i_failure_committeeAndServiceType,
                scheduleToolStatus: i_failure_scheduleToolStatus,
                meetingType: i_failure_meetingType
        )
        return sectionMeetingTime
    }

    // Confirm that only the values in the inList are accepted.
    void testInListValidation() {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        //set the inList fields to invalid values
        sectionMeetingTime.sunday = 'X'
        sectionMeetingTime.monday = 'X'
        sectionMeetingTime.tuesday = 'X'
        sectionMeetingTime.wednesday = 'X'
        sectionMeetingTime.thursday = 'X'
        sectionMeetingTime.friday = 'X'
        sectionMeetingTime.saturday = 'X'
        sectionMeetingTime.override = 'X'
        assertFalse "SectionMeetingTime should have failed validation", sectionMeetingTime.validate()
        assertErrorsFor(sectionMeetingTime, 'inList',
                ['sunday',
                        'monday',
                        'tuesday',
                        'wednesday',
                        'thursday',
                        'friday',
                        'saturday',
                        'override'])
    }

    // Test the BigDecimal fields fail when exceed the maximum size.
    void testScaleMaxSizeValidation() {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        sectionMeetingTime.creditHourSession = 9999.9999
        sectionMeetingTime.hoursWeek = 999.999
        assertFalse "SectionMeetingTime should have failed validation", sectionMeetingTime.validate()
        assertErrorsFor(sectionMeetingTime, 'max',
                ['creditHourSession', 'hoursWeek'])
    }

    // Test the BigDecimal fields fail when it is smaller than minimum size.
    void testScaleMinSizeValidation() {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        sectionMeetingTime.creditHourSession = -1
        sectionMeetingTime.hoursWeek = -1
        assertFalse "SectionMeetingTime should have failed validation", sectionMeetingTime.validate()
        assertErrorsFor(sectionMeetingTime, 'min',
                ['creditHourSession', 'hoursWeek'])
    }

    // Test the Integer fields fail validation when maximum size is exceeded.
    void testIntegerMaxSizeValidation() {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        sectionMeetingTime.dayNumber = 99
        sectionMeetingTime.meetNumber = 99999

        assertFalse "SectionMeetingTime should have failed validation", sectionMeetingTime.validate()
        assertErrorsFor(sectionMeetingTime, 'max',
                ['dayNumber', 'meetNumber'])
    }

    // Test the Integer fields fail validation when maximum size is exceeded.
    void testIntegerMinSizeValidation() {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        sectionMeetingTime.dayNumber = -1
        sectionMeetingTime.meetNumber = -1

        assertFalse "SectionMeetingTime should have failed validation", sectionMeetingTime.validate()
        assertErrorsFor(sectionMeetingTime, 'min',
                ['dayNumber', 'meetNumber'])
    }

    // Local method for testing the beginTime validator.
    private testBeginTime(String begin) {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        sectionMeetingTime.beginTime = begin

        assertFalse "SectionMeetingTime should have failed validation due to beginTime", sectionMeetingTime.validate()
        assertErrorsFor(sectionMeetingTime, 'validator', ['beginTime'])
    }

    // Local method for testing the endTime validator.
    private testEndTime(String end) {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        sectionMeetingTime.endTime = end

        assertFalse "SectionMeetingTime should have failed validation due to endTime", sectionMeetingTime.validate()
        assertErrorsFor(sectionMeetingTime, 'validator', ['endTime'])
    }

    // Local method for testing the beginTime and endTime combinations with failures on beginTime.
    private testInvalidCombinationFailOnBeginTime(String begin, String end) {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        if (begin == 'null')
            begin = null
        if (end == 'null')
            end = null
        sectionMeetingTime.beginTime = begin
        sectionMeetingTime.endTime = end

        assertFalse "SectionMeetingTime should have failed validation due to beginTime and endTime combination failing on beginTime", sectionMeetingTime.validate()
        assertErrorsFor(sectionMeetingTime, 'validator', ['beginTime'])
    }

    // Local method for testing the beginTime and endTime combinations with failures on endTime.
    private testInvalidCombinationFailOnEndTime(String begin, String end) {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        if (begin == 'null')
            begin = null
        if (end == 'null')
            end = null
        sectionMeetingTime.beginTime = begin
        sectionMeetingTime.endTime = end

        assertFalse "SectionMeetingTime should have failed validation due to beginTime and endTime combination failing on endTime", sectionMeetingTime.validate()
        assertErrorsFor(sectionMeetingTime, 'validator', ['endTime'])
    }

    // Execute a test for the begin and end times to make sure they correspond to valid times in range of '0000'..'2359'
    void testBeginAndEndTimes() {

        //test begin times being > "2359"
        testBeginTime("2400")

        //test end times being > "2359"
        testEndTime("2400")

        //test begin times having an invalid value for minutes
        testBeginTime("0860")

        //test end times having an invalid value for minutes
        testEndTime("1060")

        //test invalid combination of begin and end times
        //endTime is missing
        testInvalidCombinationFailOnBeginTime('0800', 'null')

        //beginTime is missing
        testInvalidCombinationFailOnEndTime('null', '1050')

        //beginTime is equal to the endTime
        testInvalidCombinationFailOnBeginTime('0800', '0800')

        //beginTime is greater than the endTime
        testInvalidCombinationFailOnBeginTime("0805", '"0800')
    }

    // Execute a test for start date
    void testStartDate() {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        //set the start date after the end date
        sectionMeetingTime.startDate = sectionMeetingTime.endDate + 1

        assertFalse "SectionMeetingTime should have failed validation",
                sectionMeetingTime.validate()
        assertErrorsFor(sectionMeetingTime, 'validator', ['startDate'])
    }

    // Execute a test for end date
    void testEndDate() {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        //set the end date before the start date
        sectionMeetingTime.startDate = Term.findByCode(sectionMeetingTime.term).startDate
        sectionMeetingTime.endDate = sectionMeetingTime.startDate - 1

        assertFalse "SectionMeetingTime should have failed validation",
                sectionMeetingTime.validate()
        assertErrorsFor(sectionMeetingTime, 'validator', ['endDate'])
    }

    // Test the ScheduleToolStatus and requirement for building and room.
    void testScheduleToolStatusRequireBuildingRoom() {
        //test a code which requires the building and room
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        sectionMeetingTime.scheduleToolStatus = ScheduleToolStatus.findByCode('ASM')
        sectionMeetingTime.building = null
        sectionMeetingTime.room = null

        assertFalse "SectionMeetingTime should have failed validation", sectionMeetingTime.validate()
        assertErrorsFor(sectionMeetingTime, 'validator', ['scheduleToolStatus'])
    }

    // Test the ScheduleToolStatus when it does not require building and room.
    void testScheduleToolStatusNotRequireBuildingRoom() {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        sectionMeetingTime.scheduleToolStatus = ScheduleToolStatus.findByCode('NSM')
        sectionMeetingTime.building = null
        sectionMeetingTime.room = null

        assertTrue sectionMeetingTime.validate()
    }

    // This test will execute the fetchBy in the domain to retrieve a list of class meeting times from the NamedQuery after creating a new reocrd
    void testFetchByTermAndCourseReferenceNumberNewRecord() {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        sectionMeetingTime.save(failOnError: true, flush: true)
        def result = SectionMeetingTime.fetchByTermAndCourseReferenceNumber(
                sectionMeetingTime.term, sectionMeetingTime.courseReferenceNumber)

        assertEquals 1, result.size()
        assertEquals i_success_monday, result[0].monday
        assertEquals i_success_wednesday, result[0].wednesday
        assertEquals i_success_friday, result[0].friday
        assertEquals i_success_category, result[0].category
        assertEquals i_success_beginTime, result[0].beginTime
        assertEquals i_success_endTime, result[0].endTime
    }

    // This test will execute the fetchBy in the domain to retrieve a list of class meeting times from the NamedQuery after creating a new reocrd
    void testFetchByTermAndCourseReferenceNumberExistingRecord() {
        def result = SectionMeetingTime.fetchByTermAndCourseReferenceNumber(
                u_success_existing_term, u_success_existing_courseReferenceNumber)

        assertEquals 1, result.size()
        assertEquals "M", result[0].monday
        assertEquals "W", result[0].wednesday
        assertEquals "F", result[0].friday
        assertEquals "01", result[0].category
        assertEquals "0900", result[0].beginTime
        assertEquals "1000", result[0].endTime
    }


    void testFetchCountOfSchedulesByDateTimeAndLocation() {
        def sectionMeetingTime = newValidForCreateSectionMeetingTime()
        sectionMeetingTime.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sectionMeetingTime.id
        //Exact date as in previous schedule
        int count = SectionMeetingTime.fetchCountOfSchedulesByDateTimeAndLocation("0900", "0950", Term.findByCode(i_success_term).startDate, Term.findByCode(i_success_term).endDate,
                "BIOL", "101", "M", null, "W", null, null, null, null)
        assertTrue "Expected atleast one schedule exist matching the schedule", count > 0
        Date startDate = Term.findByCode(i_success_term).startDate
        Calendar startCal = Calendar.instance
        startCal.setTime(startDate)
        startCal.add(Calendar.DAY_OF_MONTH, 4)
        Date endDate = Term.findByCode(i_success_term).endDate
        Calendar endCal = Calendar.instance
        endCal.setTime(endDate)
        //start date less than the previous schedule
        count = SectionMeetingTime.fetchCountOfSchedulesByDateTimeAndLocation("0900", "0950", startCal.getTime(), endCal.getTime(),
                "BIOL", "101", "M", null, "W", null, null, null, null)
        assertTrue "Expected atleast one schedule exist matching the schedule", count > 0
        //startDate same as previous schedule but end date less than previous schedule
        count = SectionMeetingTime.fetchCountOfSchedulesByDateTimeAndLocation("0900", "0950", Term.findByCode(i_success_term).startDate, startCal.getTime(),
                "BIOL", "101", "M", null, "W", null, null, null, null)
        assertTrue "Expected atleast one schedule exist matching the schedule", count > 0
        //starttime and endtime overlapping
        count = SectionMeetingTime.fetchCountOfSchedulesByDateTimeAndLocation("0850", "1000", Term.findByCode(i_success_term).startDate, startCal.getTime(),
                "BIOL", "101", "M", null, "W", null, null, null, null)
        assertTrue "Expected atleast one schedule exist matching the schedule", count > 0
        //starttime and endtime overlapping
        count = SectionMeetingTime.fetchCountOfSchedulesByDateTimeAndLocation("0930", "1030", Term.findByCode(i_success_term).startDate, startCal.getTime(),
                "BIOL", "101", "M", null, "W", null, null, null, null)
        assertTrue "Expected atleast one schedule exist matching the schedule", count > 0
        //starttime and endtime overlapping
        count = SectionMeetingTime.fetchCountOfSchedulesByDateTimeAndLocation("0900", "0930", Term.findByCode(i_success_term).startDate, startCal.getTime(),
                "BIOL", "101", "M", null, "W", null, null, null, null)
        assertTrue "Expected atleast one schedule exist matching the schedule", count > 0
        //starttime and endtime overlapping
        count = SectionMeetingTime.fetchCountOfSchedulesByDateTimeAndLocation("1100", "1200", Term.findByCode(i_success_term).startDate, startCal.getTime(),
                "BIOL", "101", "M", null, "W", null, null, null, null)
        assertFalse "Schedule does not exist with this time ", count > 0
    }

    // This test will execute the fetchBy in the domain to retrieve a list of class meeting times from the NamedQuery after creating a new reocrd
    void testFetchByTermCRNAndFunction() {
        def sectionMeetingTime = newValidForCreateSectionMeetingTimeForEvent()
        sectionMeetingTime.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sectionMeetingTime.id
        def results = SectionMeetingTime.fetchByTermCRNAndFunction(
                "EVENT", sectionMeetingTime.courseReferenceNumber, sectionMeetingTime.function.code)

        assertTrue results?.size() >= 1
        results?.each { result ->
            assertEquals "EVENT", result.term
            assertEquals sectionMeetingTime.courseReferenceNumber, result.courseReferenceNumber
            assertEquals sectionMeetingTime.function.code, result.function.code
        }

        /* Test for null values */
        def result = SectionMeetingTime.fetchByTermCRNAndFunction(
                null, null, null)

        assertFalse "SectionMeetingTime does not exist for the Event and Function ", result?.size() > 0
    }

    //Create a valid section meeting time record for an Event and Function
    private def newValidForCreateSectionMeetingTimeForEvent() {
        def sectionMeetingTime = new SectionMeetingTime(
                courseReferenceNumber: "E0001",
                dayNumber: i_success_dayNumber,
                beginTime: i_success_beginTime,
                endTime: i_success_endTime,
                room: i_success_room,
                startDate: i_success_startDate,
                endDate: i_success_endDate,
                category: i_success_category,
                sunday: i_success_sunday,
                monday: i_success_monday,
                tuesday: i_success_tuesday,
                wednesday: i_success_wednesday,
                thursday: i_success_thursday,
                friday: i_success_friday,
                saturday: i_success_saturday,
                override: i_success_override,
                creditHourSession: i_success_creditHourSession,
                meetNumber: i_success_meetNumber,
                hoursWeek: i_success_hoursWeek,
                term: "EVENT",
                dayOfWeek: i_success_dayOfWeek,
                building: i_success_building,
                scheduleType: i_success_scheduleType,
                function: Function.findByCode("BREAK"),
                committee: i_success_committeeAndServiceType,
                scheduleToolStatus: i_success_scheduleToolStatus,
                meetingType: i_success_meetingType
        )
        return sectionMeetingTime
    }
}

/*********************************************************************************
  Copyright 2010-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException
import net.hedtech.banner.general.system.Building
import static groovy.test.GroovyAssert.*

@Integration
@Rollback
class HousingRoomUsageRestrictionIntegrationTests extends BaseIntegrationTestCase {

    //Test data for creating new domain instance
    //Valid test data (For success tests)
    def i_success_building

    def i_success_roomNumber = "TTTTT"
    def i_success_startDate = new Date()
    def i_success_endDate = new Date()
    def i_success_beginTime = "TTTT"
    def i_success_endTime = "TTTT"
    def i_success_sunday = "#"
    def i_success_monday = "#"
    def i_success_tuesday = "#"
    def i_success_wednesday = "#"
    def i_success_thursday = "#"
    def i_success_friday = "#"
    def i_success_saturday = "#"
    //Invalid test data (For failure tests)
    def i_failure_building

    def i_failure_roomNumber = null
    def i_failure_startDate = new Date()
    def i_failure_endDate = new Date()
    def i_failure_beginTime = "TTTT"
    def i_failure_endTime = "TTTT"
    def i_failure_sunday = "#"
    def i_failure_monday = "#"
    def i_failure_tuesday = "#"
    def i_failure_wednesday = "#"
    def i_failure_thursday = "#"
    def i_failure_friday = "#"
    def i_failure_saturday = "#"

    //Test data for creating updating domain instance
    //Valid test data (For success tests)
    def u_success_building

    def u_success_roomNumber = "YYYYY"
    def u_success_startDate = new Date()
    def u_success_endDate = new Date()
    def u_success_beginTime = "YYYY"
    def u_success_endTime = "YYYY"
    def u_success_sunday = "#"
    def u_success_monday = "#"
    def u_success_tuesday = "#"
    def u_success_wednesday = "#"
    def u_success_thursday = "#"
    def u_success_friday = "#"
    def u_success_saturday = "#"
    //Valid test data (For failure tests)
    def u_failure_building

    def u_failure_roomNumber = "TTTTT"
    def u_failure_startDate = new Date()
    def u_failure_endDate = new Date()
    def u_failure_beginTime = "TTTT"
    def u_failure_endTime = "TTTT"
    def u_failure_sunday = "#"
    def u_failure_monday = "#"
    def u_failure_tuesday = "#"
    def u_failure_wednesday = "#"
    def u_failure_thursday = "#"
    def u_failure_friday = "#"
    def u_failure_saturday = "#"


    @Before
    public void setUp() {
        formContext = ['GUAGMNU','SSASECT'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
        initializeTestDataForReferences()
    }

    //This method is used to initialize test data for references.
    //A method is required to execute database calls as it requires a active transaction
    void initializeTestDataForReferences() {
        //Valid test data (For success tests)
        i_success_building = Building.findWhere(code: "NORTH")

        //Invalid test data (For failure tests)
        i_failure_building = Building.findWhere(code: "SOUTH")

        //Valid test data (For success tests)
        u_success_building = Building.findWhere(code: "EAST")

        //Valid test data (For failure tests)
        u_failure_building = Building.findWhere(code: "MENDAL")

        //Test data for references for custom tests
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateValidHousingRoomUsageRestriction() {
        def housingRoomUsageRestriction = newValidForCreateHousingRoomUsageRestriction()
        housingRoomUsageRestriction.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull housingRoomUsageRestriction.id
        assertNotNull housingRoomUsageRestriction.lastModified
        assertNotNull housingRoomUsageRestriction.lastModifiedBy
        assertNotNull housingRoomUsageRestriction.dataOrigin
    }


    @Test
    void testUpdateValidHousingRoomUsageRestriction() {
        def housingRoomUsageRestriction = newValidForCreateHousingRoomUsageRestriction()
        housingRoomUsageRestriction.save(failOnError: true, flush: true)
        assertNotNull housingRoomUsageRestriction.id
        assertEquals 0L, housingRoomUsageRestriction.version
        assertEquals i_success_roomNumber, housingRoomUsageRestriction.roomNumber
        assertEquals i_success_startDate, housingRoomUsageRestriction.startDate
        assertEquals i_success_endDate, housingRoomUsageRestriction.endDate
        assertEquals i_success_beginTime, housingRoomUsageRestriction.beginTime
        assertEquals i_success_endTime, housingRoomUsageRestriction.endTime
        assertEquals i_success_sunday, housingRoomUsageRestriction.sunday
        assertEquals i_success_monday, housingRoomUsageRestriction.monday
        assertEquals i_success_tuesday, housingRoomUsageRestriction.tuesday
        assertEquals i_success_wednesday, housingRoomUsageRestriction.wednesday
        assertEquals i_success_thursday, housingRoomUsageRestriction.thursday
        assertEquals i_success_friday, housingRoomUsageRestriction.friday
        assertEquals i_success_saturday, housingRoomUsageRestriction.saturday

        //Update the entity
        housingRoomUsageRestriction.roomNumber = u_success_roomNumber
        housingRoomUsageRestriction.startDate = u_success_startDate
        housingRoomUsageRestriction.endDate = u_success_endDate
        housingRoomUsageRestriction.beginTime = u_success_beginTime
        housingRoomUsageRestriction.endTime = u_success_endTime
        housingRoomUsageRestriction.sunday = u_success_sunday
        housingRoomUsageRestriction.monday = u_success_monday
        housingRoomUsageRestriction.tuesday = u_success_tuesday
        housingRoomUsageRestriction.wednesday = u_success_wednesday
        housingRoomUsageRestriction.thursday = u_success_thursday
        housingRoomUsageRestriction.friday = u_success_friday
        housingRoomUsageRestriction.saturday = u_success_saturday


        housingRoomUsageRestriction.building = u_success_building
        housingRoomUsageRestriction.save(failOnError: true, flush: true)
        //Asset for sucessful update
        housingRoomUsageRestriction = HousingRoomUsageRestriction.get(housingRoomUsageRestriction.id)
        assertEquals 1L, housingRoomUsageRestriction?.version
        assertEquals u_success_roomNumber, housingRoomUsageRestriction.roomNumber
        assertEquals u_success_startDate, housingRoomUsageRestriction.startDate
        assertEquals u_success_endDate, housingRoomUsageRestriction.endDate
        assertEquals u_success_beginTime, housingRoomUsageRestriction.beginTime
        assertEquals u_success_endTime, housingRoomUsageRestriction.endTime
        assertEquals u_success_sunday, housingRoomUsageRestriction.sunday
        assertEquals u_success_monday, housingRoomUsageRestriction.monday
        assertEquals u_success_tuesday, housingRoomUsageRestriction.tuesday
        assertEquals u_success_wednesday, housingRoomUsageRestriction.wednesday
        assertEquals u_success_thursday, housingRoomUsageRestriction.thursday
        assertEquals u_success_friday, housingRoomUsageRestriction.friday
        assertEquals u_success_saturday, housingRoomUsageRestriction.saturday

        housingRoomUsageRestriction.building = u_success_building
    }


    @Test
    void testOptimisticLock() {
        def housingRoomUsageRestriction = newValidForCreateHousingRoomUsageRestriction()
        housingRoomUsageRestriction.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update SLRRUSE set SLRRUSE_VERSION = 999 where SLRRUSE_SURROGATE_ID = ?", [housingRoomUsageRestriction.id])
        } finally {
//            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        housingRoomUsageRestriction.roomNumber = u_success_roomNumber
        housingRoomUsageRestriction.startDate = u_success_startDate
        housingRoomUsageRestriction.endDate = u_success_endDate
        housingRoomUsageRestriction.beginTime = u_success_beginTime
        housingRoomUsageRestriction.endTime = u_success_endTime
        housingRoomUsageRestriction.sunday = u_success_sunday
        housingRoomUsageRestriction.monday = u_success_monday
        housingRoomUsageRestriction.tuesday = u_success_tuesday
        housingRoomUsageRestriction.wednesday = u_success_wednesday
        housingRoomUsageRestriction.thursday = u_success_thursday
        housingRoomUsageRestriction.friday = u_success_friday
        housingRoomUsageRestriction.saturday = u_success_saturday
        shouldFail(HibernateOptimisticLockingFailureException) {
            housingRoomUsageRestriction.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteHousingRoomUsageRestriction() {
        def housingRoomUsageRestriction = newValidForCreateHousingRoomUsageRestriction()
        housingRoomUsageRestriction.save(failOnError: true, flush: true)
        def id = housingRoomUsageRestriction.id
        assertNotNull id
        housingRoomUsageRestriction.delete()
        assertNull HousingRoomUsageRestriction.get(id)
    }


    @Test
    void testNullValidationFailure() {
        def housingRoomUsageRestriction = new HousingRoomUsageRestriction()
        assertFalse "HousingRoomUsageRestriction should have failed validation", housingRoomUsageRestriction.validate()
        assertErrorsFor housingRoomUsageRestriction, 'nullable',
                        [
                        'roomNumber',
                        'startDate',
                        'building'
                        ]
        assertNoErrorsFor housingRoomUsageRestriction,
                          [
                          'endDate',
                          'beginTime',
                          'endTime',
                          'sunday',
                          'monday',
                          'tuesday',
                          'wednesday',
                          'thursday',
                          'friday',
                          'saturday'
                          ]
    }


    @Test
    void testMaxSizeValidationFailures() {
        def housingRoomUsageRestriction = new HousingRoomUsageRestriction(
                roomNumber: '01234567891',
                beginTime: 'XXXXXX',
                endTime: 'XXXXXX',
                sunday: 'XXX',
                monday: 'XXX',
                tuesday: 'XXX',
                wednesday: 'XXX',
                thursday: 'XXX',
                friday: 'XXX',
                saturday: 'XXX')
        assertFalse "HousingRoomUsageRestriction should have failed validation", housingRoomUsageRestriction.validate()
        assertErrorsFor housingRoomUsageRestriction, 'maxSize', ['roomNumber', 'beginTime', 'endTime', 'sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday']
    }


    @Test
    void testFetchCountOfUsageRestrictionsByDateAndLocation() {
        def housingRoomUsageRestriction = newValidForCreateHousingRoomUsageRestriction()
        Calendar startDatecal = Calendar.instance
        startDatecal.set(1998, 8, 21)
        Calendar endDateCal = Calendar.instance
        endDateCal.set(1998, 9, 21)
        housingRoomUsageRestriction.startDate = startDatecal.getTime()
        housingRoomUsageRestriction.endDate = endDateCal.getTime()

        housingRoomUsageRestriction.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull housingRoomUsageRestriction.id

        int count = HousingRoomUsageRestriction.fetchCountOfUsageRestrictionsByDateAndLocation(startDatecal.getTime(), endDateCal.getTime(), "TTTTT", "NORTH")
        assertTrue "Expected is atleast one record found", count > 0

        startDatecal.set(1998, 8, 24)
        count = HousingRoomUsageRestriction.fetchCountOfUsageRestrictionsByDateAndLocation(startDatecal.getTime(), endDateCal.getTime(), "TTTTT", "NORTH")
        assertTrue "Expected is atleast one record found", count > 0

        endDateCal.set(1998, 9, 14)
        count = HousingRoomUsageRestriction.fetchCountOfUsageRestrictionsByDateAndLocation(startDatecal.getTime(), endDateCal.getTime(), "TTTTT", "NORTH")
        assertTrue "Expected is atleast one record found", count > 0

        startDatecal.set(1998, 9, 25)
        endDateCal.set(1998, 9, 28)
        count = HousingRoomUsageRestriction.fetchCountOfUsageRestrictionsByDateAndLocation(startDatecal.getTime(), endDateCal.getTime(), "TTTTT", "NORTH")
        assertFalse "Expected no usage restriction to be found for the given date", count > 0
    }


    private def newValidForCreateHousingRoomUsageRestriction() {
        def housingRoomUsageRestriction = new HousingRoomUsageRestriction(
                roomNumber: i_success_roomNumber,
                startDate: i_success_startDate,
                endDate: i_success_endDate,
                beginTime: i_success_beginTime,
                endTime: i_success_endTime,
                sunday: i_success_sunday,
                monday: i_success_monday,
                tuesday: i_success_tuesday,
                wednesday: i_success_wednesday,
                thursday: i_success_thursday,
                friday: i_success_friday,
                saturday: i_success_saturday,
                building: i_success_building
        )
        return housingRoomUsageRestriction
    }


}

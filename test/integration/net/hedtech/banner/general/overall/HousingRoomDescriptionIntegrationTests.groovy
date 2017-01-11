/*********************************************************************************
 Copyright 2010-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import org.junit.Before
import org.junit.Test
import org.junit.After

import groovy.sql.Sql
import net.hedtech.banner.general.system.*
import net.hedtech.banner.query.operators.Operators
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

class HousingRoomDescriptionIntegrationTests extends BaseIntegrationTestCase {

    //Valid test data (For success tests)
    def i_success_department
    def i_success_partition
    def i_success_building
    def i_success_roomStatus
    def i_success_roomRate
    def i_success_phoneRate
    def i_success_college
    def i_success_roomNumber = "101"
    def i_success_termEffective = "201410"
    def i_success_description = "TTTTT"
    def i_success_capacity = 1
    def i_success_maximumCapacity = 1
    def i_success_utilityRate = 1
    def i_success_utilityRatePeriod = null
    def i_success_phoneArea = "TTTTT"
    def i_success_phoneNumber = "TTTTT"
    def i_success_phoneExtension = "TTTTT"
    def i_success_benefitCategory = null
    def i_success_sex = "M"
    def i_success_roomType = "D"
    def i_success_priority = null
    def i_success_keyNumber = null
    def i_success_width = 1
    def i_success_length = 1
    def i_success_area = 1
    def i_success_countryPhone = null

    //Invalid test data (For failure tests)
    def i_failure_department
    def i_failure_partition
    def i_failure_building
    def i_failure_roomStatus
    def i_failure_roomRate
    def i_failure_phoneRate
    def i_failure_college
    def i_failure_roomNumber = "TTTTT"
    def i_failure_termEffective = "TTTTT"
    def i_failure_description = "TTTTT"
    def i_failure_capacity = 1
    def i_failure_maximumCapacity = 1
    def i_failure_utilityRate = 1
    def i_failure_utilityRatePeriod = "TT"
    def i_failure_phoneArea = "TTTTT"
    def i_failure_phoneNumber = "TTTTT"
    def i_failure_phoneExtension = "TTTTT"
    def i_failure_benefitCategory = null
    def i_failure_sex = "#"
    def i_failure_roomType = "D"
    def i_failure_priority = "TTTTT"
    def i_failure_keyNumber = "TTTTT"
    def i_failure_width = 1
    def i_failure_length = 1
    def i_failure_area = 1
    def i_failure_countryPhone = "TTTT"

    //Valid test data (For success tests)
    def u_success_department
    def u_success_partition
    def u_success_building
    def u_success_roomStatus
    def u_success_roomRate
    def u_success_phoneRate
    def u_success_college
    def u_success_roomNumber = "101"
    def u_success_termEffective = "201410"
    def u_success_description = "TTTTT"
    def u_success_capacity = 1
    def u_success_maximumCapacity = 1
    def u_success_utilityRate = 1
    def u_success_utilityRatePeriod = "TT"
    def u_success_phoneArea = "TTTTT"
    def u_success_phoneNumber = "TTTTT"
    def u_success_phoneExtension = "TTTTT"
    def u_success_benefitCategory = null
    def u_success_sex = "F"
    def u_success_roomType = "C"
    def u_success_priority = "TTTTT"
    def u_success_keyNumber = "TTTTT"
    def u_success_width = 1
    def u_success_length = 1
    def u_success_area = 1
    def u_success_countryPhone = "TTTT"

    //Valid test data (For failure tests)
    def u_failure_department
    def u_failure_partition
    def u_failure_building
    def u_failure_roomStatus
    def u_failure_roomRate
    def u_failure_phoneRate
    def u_failure_college
    def u_failure_roomNumber = "103"
    def u_failure_termEffective = "201420"
    def u_failure_description = "TTTTT"
    def u_failure_capacity = 1
    def u_failure_maximumCapacity = 1
    def u_failure_utilityRate = 1
    def u_failure_utilityRatePeriod = "TT"
    def u_failure_phoneArea = "TTTTT"
    def u_failure_phoneNumber = "TTTTT"
    def u_failure_phoneExtension = "TTTTT"
    def u_failure_benefitCategory = null
    def u_failure_sex = "#"
    def u_failure_roomType = "#"
    def u_failure_priority = "TTTTT"
    def u_failure_keyNumber = "TTTTT"
    def u_failure_width = 1
    def u_failure_length = 1
    def u_failure_area = 1
    def u_failure_countryPhone = "TTTT"


    @Before
    public void setUp() {
        formContext = ["GUAGMNU","SSASECT"] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
        initializeTestDataForReferences()
    }

    //This method is used to initialize test data for references.
    //A method is required to execute database calls as it requires a active transaction
    void initializeTestDataForReferences() {
        //Valid test data (For success tests)
        i_success_department = Department.findByCode("HIST")
        i_success_partition = null
        i_success_building = Building.findByCode("FLOYD")
        i_success_roomStatus = RoomStatus.findByCode("AC")
        i_success_roomRate = RoomRate.findByCode("DBLD")
        i_success_phoneRate = PhoneRate.findByCode("PRVM")
        i_success_college = College.findByCode("AS")

        //Invalid test data (For failure tests)
        i_failure_department = Department.findByCode("HIST")
        i_failure_partition = null
        i_failure_building = Building.findByCode("FLOYD")
        i_failure_roomStatus = RoomStatus.findByCode("AC")
        i_failure_roomRate = RoomRate.findByCode("DBLD")
        i_failure_phoneRate = PhoneRate.findByCode("PRVM")
        i_failure_college = College.findByCode("AS")

        //Valid test data (For success tests)
        u_success_department = Department.findByCode("ENGL")
        u_success_partition = null
        u_success_building = Building.findByCode("FLOYD")
        u_success_roomStatus = RoomStatus.findByCode("AC")
        u_success_roomRate = RoomRate.findByCode("DBLM")
        u_success_phoneRate = PhoneRate.findByCode("PRVT")
        u_success_college = null

        //Valid test data (For failure tests)
        u_failure_department = Department.findByCode("HIST")
        u_failure_partition = null
        u_failure_building = Building.findByCode("FLOYD")
        u_failure_roomStatus = RoomStatus.findByCode("AC")
        u_failure_roomRate = RoomRate.findByCode("DBLD")
        u_failure_phoneRate = PhoneRate.findByCode("PRVM")
        u_failure_college = College.findByCode("AS")

        //Test data for references for custom tests
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateValidHousingRoomDescription() {
        def housingRoomDescription = newValidForCreateHousingRoomDescription()
        housingRoomDescription.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull housingRoomDescription.id
        assertNotNull housingRoomDescription.lastModified
        assertNotNull housingRoomDescription.lastModifiedBy
        assertNotNull housingRoomDescription.dataOrigin
    }


    @Test
    void testCreateInvalidHousingRoomDescription() {
        def housingRoomDescription = newInvalidForCreateHousingRoomDescription()
        shouldFail {
            housingRoomDescription.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidHousingRoomDescription() {
        def housingRoomDescription = newValidForCreateHousingRoomDescription()
        housingRoomDescription.save(failOnError: true, flush: true)
        assertNotNull housingRoomDescription.id
        assertEquals 0L, housingRoomDescription.version
        assertEquals i_success_roomNumber, housingRoomDescription.roomNumber
        assertEquals i_success_termEffective, housingRoomDescription.termEffective
        assertEquals i_success_description, housingRoomDescription.description
        assertEquals i_success_capacity, housingRoomDescription.capacity
        assertEquals i_success_maximumCapacity, housingRoomDescription.maximumCapacity
        assertEquals i_success_utilityRate, housingRoomDescription.utilityRate, 0
        assertEquals i_success_utilityRatePeriod, housingRoomDescription.utilityRatePeriod
        assertEquals i_success_phoneArea, housingRoomDescription.phoneArea
        assertEquals i_success_phoneNumber, housingRoomDescription.phoneNumber
        assertEquals i_success_phoneExtension, housingRoomDescription.phoneExtension
        assertEquals i_success_benefitCategory, housingRoomDescription.benefitCategory
        assertEquals i_success_sex, housingRoomDescription.sex
        assertEquals i_success_roomType, housingRoomDescription.roomType
        assertEquals i_success_priority, housingRoomDescription.priority
        assertEquals i_success_keyNumber, housingRoomDescription.keyNumber
        assertEquals i_success_width, housingRoomDescription.width, 0
        assertEquals i_success_length, housingRoomDescription.length, 0
        assertEquals i_success_area, housingRoomDescription.area, 0
        assertEquals i_success_countryPhone, housingRoomDescription.countryPhone

        //Update the entity
        housingRoomDescription.description = u_success_description
        housingRoomDescription.capacity = u_success_capacity
        housingRoomDescription.maximumCapacity = u_success_maximumCapacity
        housingRoomDescription.utilityRate = u_success_utilityRate
        housingRoomDescription.utilityRatePeriod = u_success_utilityRatePeriod
        housingRoomDescription.phoneArea = u_success_phoneArea
        housingRoomDescription.phoneNumber = u_success_phoneNumber
        housingRoomDescription.phoneExtension = u_success_phoneExtension
        housingRoomDescription.benefitCategory = u_success_benefitCategory
        housingRoomDescription.sex = u_success_sex
        housingRoomDescription.roomType = u_success_roomType
        housingRoomDescription.priority = u_success_priority
        housingRoomDescription.keyNumber = u_success_keyNumber
        housingRoomDescription.width = u_success_width
        housingRoomDescription.length = u_success_length
        housingRoomDescription.area = u_success_area
        housingRoomDescription.countryPhone = u_success_countryPhone
        housingRoomDescription.department = u_success_department
        housingRoomDescription.partition = u_success_partition
        housingRoomDescription.roomStatus = u_success_roomStatus
        housingRoomDescription.roomRate = u_success_roomRate
        housingRoomDescription.phoneRate = u_success_phoneRate
        housingRoomDescription.college = u_success_college

        housingRoomDescription.save(failOnError: true, flush: true)
        //Assert for successful update
        housingRoomDescription = HousingRoomDescription.get(housingRoomDescription.id)
        assertEquals 1L, housingRoomDescription?.version
        assertEquals u_success_description, housingRoomDescription.description
        assertEquals u_success_capacity, housingRoomDescription.capacity
        assertEquals u_success_maximumCapacity, housingRoomDescription.maximumCapacity
        assertEquals u_success_utilityRate, housingRoomDescription.utilityRate, 0
        assertEquals u_success_utilityRatePeriod, housingRoomDescription.utilityRatePeriod
        assertEquals u_success_phoneArea, housingRoomDescription.phoneArea
        assertEquals u_success_phoneNumber, housingRoomDescription.phoneNumber
        assertEquals u_success_phoneExtension, housingRoomDescription.phoneExtension
        assertEquals u_success_benefitCategory, housingRoomDescription.benefitCategory
        assertEquals u_success_sex, housingRoomDescription.sex
        assertEquals u_success_roomType, housingRoomDescription.roomType
        assertEquals u_success_priority, housingRoomDescription.priority
        assertEquals u_success_keyNumber, housingRoomDescription.keyNumber
        assertEquals u_success_width, housingRoomDescription.width, 0
        assertEquals u_success_length, housingRoomDescription.length, 0
        assertEquals u_success_area, housingRoomDescription.area, 0
        assertEquals u_success_countryPhone, housingRoomDescription.countryPhone
    }


    @Test
    void testUpdateInvalidHousingRoomDescription() {
        def housingRoomDescription = newValidForCreateHousingRoomDescription()
        housingRoomDescription.save(failOnError: true, flush: true)
        assertNotNull housingRoomDescription.id
        assertEquals 0L, housingRoomDescription.version
        assertEquals i_success_roomNumber, housingRoomDescription.roomNumber
        assertEquals i_success_termEffective, housingRoomDescription.termEffective
        assertEquals i_success_description, housingRoomDescription.description
        assertEquals i_success_capacity, housingRoomDescription.capacity
        assertEquals i_success_maximumCapacity, housingRoomDescription.maximumCapacity
        assertEquals i_success_utilityRate, housingRoomDescription.utilityRate, 0
        assertEquals i_success_utilityRatePeriod, housingRoomDescription.utilityRatePeriod
        assertEquals i_success_phoneArea, housingRoomDescription.phoneArea
        assertEquals i_success_phoneNumber, housingRoomDescription.phoneNumber
        assertEquals i_success_phoneExtension, housingRoomDescription.phoneExtension
        assertEquals i_success_benefitCategory, housingRoomDescription.benefitCategory
        assertEquals i_success_sex, housingRoomDescription.sex
        assertEquals i_success_roomType, housingRoomDescription.roomType
        assertEquals i_success_priority, housingRoomDescription.priority
        assertEquals i_success_keyNumber, housingRoomDescription.keyNumber
        assertEquals i_success_width, housingRoomDescription.width, 0
        assertEquals i_success_length, housingRoomDescription.length, 0
        assertEquals i_success_area, housingRoomDescription.area, 0
        assertEquals i_success_countryPhone, housingRoomDescription.countryPhone

        //Update the entity with invalid values
        housingRoomDescription.description = u_failure_description
        housingRoomDescription.capacity = u_failure_capacity
        housingRoomDescription.maximumCapacity = u_failure_maximumCapacity
        housingRoomDescription.utilityRate = u_failure_utilityRate
        housingRoomDescription.utilityRatePeriod = u_failure_utilityRatePeriod
        housingRoomDescription.phoneArea = u_failure_phoneArea
        housingRoomDescription.phoneNumber = u_failure_phoneNumber
        housingRoomDescription.phoneExtension = u_failure_phoneExtension
        housingRoomDescription.benefitCategory = u_failure_benefitCategory
        housingRoomDescription.sex = u_failure_sex
        housingRoomDescription.roomType = u_failure_roomType
        housingRoomDescription.priority = u_failure_priority
        housingRoomDescription.keyNumber = u_failure_keyNumber
        housingRoomDescription.width = u_failure_width
        housingRoomDescription.length = u_failure_length
        housingRoomDescription.area = u_failure_area
        housingRoomDescription.countryPhone = u_failure_countryPhone
        housingRoomDescription.department = u_failure_department
        housingRoomDescription.partition = u_failure_partition
        housingRoomDescription.roomStatus = u_failure_roomStatus
        housingRoomDescription.roomRate = u_failure_roomRate
        housingRoomDescription.phoneRate = u_failure_phoneRate
        housingRoomDescription.college = u_failure_college
        shouldFail {
            housingRoomDescription.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testOptimisticLock() {
        def housingRoomDescription = newValidForCreateHousingRoomDescription()
        housingRoomDescription.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update SV_SLBRDEF set SLBRDEF_VERSION = 999 where SLBRDEF_SURROGATE_ID = ?", [housingRoomDescription.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }

        //Try to update the entity
        housingRoomDescription.description = u_success_description
        housingRoomDescription.capacity = u_success_capacity
        housingRoomDescription.maximumCapacity = u_success_maximumCapacity
        housingRoomDescription.utilityRate = u_success_utilityRate
        housingRoomDescription.utilityRatePeriod = u_success_utilityRatePeriod
        housingRoomDescription.phoneArea = u_success_phoneArea
        housingRoomDescription.phoneNumber = u_success_phoneNumber
        housingRoomDescription.phoneExtension = u_success_phoneExtension
        housingRoomDescription.benefitCategory = u_success_benefitCategory
        housingRoomDescription.sex = u_success_sex
        housingRoomDescription.roomType = u_success_roomType
        housingRoomDescription.priority = u_success_priority
        housingRoomDescription.keyNumber = u_success_keyNumber
        housingRoomDescription.width = u_success_width
        housingRoomDescription.length = u_success_length
        housingRoomDescription.area = u_success_area
        housingRoomDescription.countryPhone = u_success_countryPhone
        shouldFail(HibernateOptimisticLockingFailureException) {
            housingRoomDescription.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteHousingRoomDescription() {
        def housingRoomDescription = newValidForCreateHousingRoomDescription()
        housingRoomDescription.save(failOnError: true, flush: true)
        def id = housingRoomDescription.id
        assertNotNull id
        housingRoomDescription.delete()
        assertNull HousingRoomDescription.get(id)
    }


    @Test
    void testValidation() {
        def housingRoomDescription = newValidForCreateHousingRoomDescription()
        assertTrue "HousingRoomDescription could not be validated as expected due to ${ housingRoomDescription.errors }", housingRoomDescription.validate()
    }


    @Test
    void testNullValidationFailure() {
        def housingRoomDescription = new HousingRoomDescription()
        assertFalse "HousingRoomDescription should have failed validation", housingRoomDescription.validate()
        assertErrorsFor housingRoomDescription, 'nullable',
                [
                        'roomNumber',
                        'termEffective',
                        'capacity',
                        'roomType',
                        'building'
                ]
        assertNoErrorsFor housingRoomDescription,
                [
                        'description',
                        'maximumCapacity',
                        'utilityRate',
                        'utilityRatePeriod',
                        'phoneArea',
                        'phoneNumber',
                        'phoneExtension',
                        'benefitCategory',
                        'sex',
                        'priority',
                        'keyNumber',
                        'width',
                        'length',
                        'area',
                        'countryPhone',
                        'department',
                        'partition',
                        'roomStatus',
                        'roomRate',
                        'phoneRate',
                        'college'
                ]
    }


    @Test
    void testMaxSizeValidationFailures() {
        def housingRoomDescription = new HousingRoomDescription(
                description: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                roomNumber: '01234567891',
                termEffective: '1234567',
                utilityRatePeriod: 'XXXX',
                phoneArea: 'XXXXXXXX',
                phoneNumber: 'XXXXXXXXXXXXXX',
                phoneExtension: 'XXXXXXXXXXXX',
                benefitCategory: 'XXXXXX',
                sex: 'XXX',
                priority: 'XXXXXXXXXX',
                keyNumber: 'XXXXXXX',
                countryPhone: 'XXXXXX')
        assertFalse "HousingRoomDescription should have failed validation", housingRoomDescription.validate()
        assertErrorsFor housingRoomDescription, 'maxSize', ['description', 'roomNumber', 'termEffective', 'utilityRatePeriod', 'phoneArea', 'phoneNumber', 'phoneExtension', 'benefitCategory', 'sex', 'priority', 'keyNumber', 'countryPhone']
    }


    @Test
    void testMaxValueValidationFailure() {
        def housingRoomDescription = newValidForCreateHousingRoomDescription()
        housingRoomDescription.capacity = 100000
        housingRoomDescription.maximumCapacity = 100000
        housingRoomDescription.utilityRate = 100000.999
        housingRoomDescription.width = 19999.99
        housingRoomDescription.length = 10000.99
        housingRoomDescription.area = 100000000.99

        assertFalse "HousingLocationBuildingDescription should have failed validation", housingRoomDescription.validate()
        assertErrorsFor housingRoomDescription, 'max',
                [
                        'capacity',
                        'maximumCapacity',
                        'utilityRate',
                        'width',
                        'length',
                        'area'
                ]
    }


    @Test
    void testMinValueValidationFailure() {
        def housingRoomDescription = newValidForCreateHousingRoomDescription()
        housingRoomDescription.capacity = -100000
        housingRoomDescription.maximumCapacity = -100000
        housingRoomDescription.utilityRate = -100000.999
        housingRoomDescription.width = -10000.99
        housingRoomDescription.length = -10000.99
        housingRoomDescription.area = -100000000.99

        assertFalse "HousingLocationBuildingDescription should have failed validation", housingRoomDescription.validate()
        assertErrorsFor housingRoomDescription, 'min',
                [
                        'capacity',
                        'maximumCapacity',
                        'utilityRate',
                        'width',
                        'length',
                        'area'
                ]
    }


    @Test
    void testFetchTermTo() {
        //Seed data
        // BuildingCode B00A	RoomNumber R00A	    TermCode200010
        //BuildingCode B00A	RoomNumber R00A	    TermCode201070
        //So when fetchTermTo will call be called for B00A,R00A,200010 expected result is 201070
        String termFetched = HousingRoomDescription.fetchTermToOfRoom("ADAMS", "100", "200010")
        assertEquals "200110", termFetched
    }


    @Test
    void testFetchBySomeHousingRoomDescriptionRoom() {
        def housings = HousingRoomDescription.fetchBySomeHousingRoomDescriptionRoom()
        assertNotNull housings
        assertTrue housings.list.size() > 3
    }


    @Test
    void testFetchBySomeHousingRoomDescriptionRoomBuilding() {
        def map = [building: Building.findByCode('ADAMS'), termEffective: '200110']
        def housings = HousingRoomDescription.fetchBySomeHousingRoomDescriptionRoom(map)
        assertEquals 'ADAMS', housings.list[0].building.code
    }


    @Test
    void testFetchBySomeHousingRoomDescriptionRoomFilter() {
        def map = [building: Building.findByCode('ADAMS'), termEffective: '200110']
        def housings = HousingRoomDescription.fetchBySomeHousingRoomDescriptionRoom('1', map)
        assertTrue housings.list.size() > 0
    }

    //Test by passing room number or description as filters
    @Test
     void testFetchBySomeHousingRoomDescription() {
         //Test by passing room description as filter
         def map = [termEffective: '200110']
         def housings = HousingRoomDescription.fetchBySomeHousingRoomDescriptionRoom('lecture hall', map)
         assertNotNull housings
         assertTrue housings.list.size() > 0
         def descList = housings.list*.description
         descList.each { it ->
              assertTrue(it.contains('Lecture Hall'))
         }
         //Test by passing room number as filter
         def map2 = [termEffective: '200110']
         def housings2 = HousingRoomDescription.fetchBySomeHousingRoomDescriptionRoom('100', map2)
         assertNotNull housings2
         assertTrue housings2.list.size() > 0
         def roomNumberList = housings2.list*.roomNumber
           roomNumberList.each { it ->
              assertTrue(it.contains('100'))
         }
    }


    @Test
    void testFetchValidRoomAndBuilding() {
        def map = [building: Building.findByCode('ADAMS'), termEffective: '200110']
        def housings = HousingRoomDescription.fetchValidRoomAndBuilding("100", map)
        assertNotNull housings
        assertEquals 'ADAMS', housings.building.code
    }


    @Test
    void testFetchValidSomeRoomAndBuilding() {
        def map = [building: Building.findByCode('ADAMS'), termEffective: '200110']
        def housings = HousingRoomDescription.fetchValidSomeRoomAndBuilding("100", map)
        assertNotNull housings
        assertEquals 'ADAMS', housings.building.code
    }


    @Test
    void testFetchValidSomeRoomAndBuildingWithoutBuildingParam() {
        def map = [termEffective: '200110']
        def housings = HousingRoomDescription.fetchValidSomeRoomAndBuilding("100", map)
        assertNotNull housings
        assertEquals '100', housings.roomNumber
    }



    private def newValidForCreateHousingRoomDescription() {
        def housingRoomDescription = new HousingRoomDescription(
                roomNumber: i_success_roomNumber,
                termEffective: i_success_termEffective,
                description: i_success_description,
                capacity: i_success_capacity,
                maximumCapacity: i_success_maximumCapacity,
                utilityRate: i_success_utilityRate,
                utilityRatePeriod: i_success_utilityRatePeriod,
                phoneArea: i_success_phoneArea,
                phoneNumber: i_success_phoneNumber,
                phoneExtension: i_success_phoneExtension,
                benefitCategory: i_success_benefitCategory,
                sex: i_success_sex,
                roomType: i_success_roomType,
                priority: i_success_priority,
                keyNumber: i_success_keyNumber,
                width: i_success_width,
                length: i_success_length,
                area: i_success_area,
                countryPhone: i_success_countryPhone,
                department: i_success_department,
                partition: i_success_partition,
                building: i_success_building,
                roomStatus: i_success_roomStatus,
                roomRate: i_success_roomRate,
                phoneRate: i_success_phoneRate,
                college: i_success_college
        )
        return housingRoomDescription
    }


    private def newInvalidForCreateHousingRoomDescription() {
        def housingRoomDescription = new HousingRoomDescription(
                roomNumber: i_failure_roomNumber,
                termEffective: i_failure_termEffective,
                description: i_failure_description,
                capacity: i_failure_capacity,
                maximumCapacity: i_failure_maximumCapacity,
                utilityRate: i_failure_utilityRate,
                utilityRatePeriod: i_failure_utilityRatePeriod,
                phoneArea: i_failure_phoneArea,
                phoneNumber: i_failure_phoneNumber,
                phoneExtension: i_failure_phoneExtension,
                benefitCategory: i_failure_benefitCategory,
                sex: i_failure_sex,
                roomType: i_failure_roomType,
                priority: i_failure_priority,
                keyNumber: i_failure_keyNumber,
                width: i_failure_width,
                length: i_failure_length,
                area: i_failure_area,
                countryPhone: i_failure_countryPhone,
                department: i_failure_department,
                partition: i_failure_partition,
                building: i_failure_building,
                roomStatus: i_failure_roomStatus,
                roomRate: i_failure_roomRate,
                phoneRate: i_failure_phoneRate,
                college: i_failure_college
        )
        return housingRoomDescription
    }


}

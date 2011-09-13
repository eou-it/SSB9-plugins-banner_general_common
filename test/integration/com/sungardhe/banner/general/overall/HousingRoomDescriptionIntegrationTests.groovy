/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard, Banner and Luminis are either 
 registered trademarks or trademarks of SunGard Higher Education in the U.S.A. 
 and/or other regions and/or countries.
 **********************************************************************************/
/**
 Banner Automator Version: 0.1.1
 Generated: Fri Feb 11 16:39:35 EST 2011
 */
package com.sungardhe.banner.general.overall

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import com.sungardhe.banner.general.system.Department
import com.sungardhe.banner.general.system.Building
import com.sungardhe.banner.general.system.RoomStatus
import com.sungardhe.banner.general.system.RoomRate
import com.sungardhe.banner.general.system.PhoneRate
import com.sungardhe.banner.general.system.College
import org.junit.Ignore


class HousingRoomDescriptionIntegrationTests extends BaseIntegrationTestCase {

    /*PROTECTED REGION ID(housingroomdescription_domain_integration_test_data) ENABLED START*/
    //Test data for creating new domain instance
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

    //Test data for creating updating domain instance
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
    /*PROTECTED REGION END*/


    protected void setUp() {
        formContext = ["SSASECT"] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
        initializeTestDataForReferences()
    }

    //This method is used to initialize test data for references.
    //A method is required to execute database calls as it requires a active transaction


    void initializeTestDataForReferences() {
        /*PROTECTED REGION ID(housingroomdescription_domain_integration_test_data_initialization) ENABLED START*/
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
        /*PROTECTED REGION END*/
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateValidHousingRoomDescription() {
        def housingRoomDescription = newValidForCreateHousingRoomDescription()
        housingRoomDescription.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull housingRoomDescription.id
    }


    void testCreateInvalidHousingRoomDescription() {
        def housingRoomDescription = newInvalidForCreateHousingRoomDescription()
        shouldFail {
            housingRoomDescription.save(failOnError: true, flush: true)
        }
    }


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
        assertEquals i_success_utilityRate, housingRoomDescription.utilityRate
        assertEquals i_success_utilityRatePeriod, housingRoomDescription.utilityRatePeriod
        assertEquals i_success_phoneArea, housingRoomDescription.phoneArea
        assertEquals i_success_phoneNumber, housingRoomDescription.phoneNumber
        assertEquals i_success_phoneExtension, housingRoomDescription.phoneExtension
        assertEquals i_success_benefitCategory, housingRoomDescription.benefitCategory
        assertEquals i_success_sex, housingRoomDescription.sex
        assertEquals i_success_roomType, housingRoomDescription.roomType
        assertEquals i_success_priority, housingRoomDescription.priority
        assertEquals i_success_keyNumber, housingRoomDescription.keyNumber
        assertEquals i_success_width, housingRoomDescription.width
        assertEquals i_success_length, housingRoomDescription.length
        assertEquals i_success_area, housingRoomDescription.area
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
        //Asset for sucessful update
        housingRoomDescription = HousingRoomDescription.get(housingRoomDescription.id)
        assertEquals 1L, housingRoomDescription?.version
        assertEquals u_success_description, housingRoomDescription.description
        assertEquals u_success_capacity, housingRoomDescription.capacity
        assertEquals u_success_maximumCapacity, housingRoomDescription.maximumCapacity
        assertEquals u_success_utilityRate, housingRoomDescription.utilityRate
        assertEquals u_success_utilityRatePeriod, housingRoomDescription.utilityRatePeriod
        assertEquals u_success_phoneArea, housingRoomDescription.phoneArea
        assertEquals u_success_phoneNumber, housingRoomDescription.phoneNumber
        assertEquals u_success_phoneExtension, housingRoomDescription.phoneExtension
        assertEquals u_success_benefitCategory, housingRoomDescription.benefitCategory
        assertEquals u_success_sex, housingRoomDescription.sex
        assertEquals u_success_roomType, housingRoomDescription.roomType
        assertEquals u_success_priority, housingRoomDescription.priority
        assertEquals u_success_keyNumber, housingRoomDescription.keyNumber
        assertEquals u_success_width, housingRoomDescription.width
        assertEquals u_success_length, housingRoomDescription.length
        assertEquals u_success_area, housingRoomDescription.area
        assertEquals u_success_countryPhone, housingRoomDescription.countryPhone


        housingRoomDescription.department = u_success_department

        housingRoomDescription.partition = u_success_partition

        housingRoomDescription.roomStatus = u_success_roomStatus

        housingRoomDescription.roomRate = u_success_roomRate

        housingRoomDescription.phoneRate = u_success_phoneRate

        housingRoomDescription.college = u_success_college
    }


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
        assertEquals i_success_utilityRate, housingRoomDescription.utilityRate
        assertEquals i_success_utilityRatePeriod, housingRoomDescription.utilityRatePeriod
        assertEquals i_success_phoneArea, housingRoomDescription.phoneArea
        assertEquals i_success_phoneNumber, housingRoomDescription.phoneNumber
        assertEquals i_success_phoneExtension, housingRoomDescription.phoneExtension
        assertEquals i_success_benefitCategory, housingRoomDescription.benefitCategory
        assertEquals i_success_sex, housingRoomDescription.sex
        assertEquals i_success_roomType, housingRoomDescription.roomType
        assertEquals i_success_priority, housingRoomDescription.priority
        assertEquals i_success_keyNumber, housingRoomDescription.keyNumber
        assertEquals i_success_width, housingRoomDescription.width
        assertEquals i_success_length, housingRoomDescription.length
        assertEquals i_success_area, housingRoomDescription.area
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
        shouldFail(HibernateOptimisticLockingFailureException) {
            housingRoomDescription.save(failOnError: true, flush: true)
        }
    }


    void testDeleteHousingRoomDescription() {
        def housingRoomDescription = newValidForCreateHousingRoomDescription()
        housingRoomDescription.save(failOnError: true, flush: true)
        def id = housingRoomDescription.id
        assertNotNull id
        housingRoomDescription.delete()
        assertNull HousingRoomDescription.get(id)
    }


    void testValidation() {
        def housingRoomDescription = newValidForCreateHousingRoomDescription()
        assertTrue "HousingRoomDescription could not be validated as expected due to ${housingRoomDescription.errors}", housingRoomDescription.validate()
    }

    void testFetchBySomeHousingRoomDescriptionRoom()
    {
        def map = [building:u_success_building]
        def housings = HousingRoomDescription.fetchBySomeHousingRoomDescriptionRoom("102",map)
        assertNotNull housings
    }

    void testFetchBySomeHousingRoomDescriptionRoomBuilding()
    {
        def buildings = [building:u_success_building]
        def housings = HousingRoomDescription.fetchBySomeHousingRoomDescriptionRoom(buildings)
        assertNotNull housings
    }


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


    void testMaxSizeValidationFailures() {
        def housingRoomDescription = new HousingRoomDescription(
                description: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
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
        assertErrorsFor housingRoomDescription, 'maxSize', ['description', 'utilityRatePeriod', 'phoneArea', 'phoneNumber', 'phoneExtension', 'benefitCategory', 'sex', 'priority', 'keyNumber', 'countryPhone']
    }


    @Ignore
    void testFetchTermTo() {
        //Seed data
        // BuildingCode B00A	RoomNumber R00A	    TermCode200010
        //BuildingCode B00A	RoomNumber R00A	    TermCode201070
        //So when fetchTermTo will call be called for B00A,R00A,200010 expected result is 201070
        String termFetched = HousingRoomDescription.fetchTermToOfRoom("B00A","R00A","200010")
        assertEquals "201070", termFetched

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
                college: i_success_college,
                lastModified: new Date(),
                lastModifiedBy: "test",
                dataOrigin: "Banner"
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
                college: i_failure_college,
                lastModified: new Date(),
                lastModifiedBy: "test",
                dataOrigin: "Banner"
        )
        return housingRoomDescription
    }

    /**
     * Please put all the custom tests in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(housingroomdescription_custom_integration_test_methods) ENABLED START*/
    /*PROTECTED REGION END*/
}

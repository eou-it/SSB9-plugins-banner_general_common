/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import net.hedtech.banner.general.system.*
import org.junit.Test
import org.apache.commons.lang.StringUtils

class HousingLocationBuildingDescriptionIntegrationTests extends BaseIntegrationTestCase {

    //Test data for creating new domain instance
    //Valid test data (For success tests)
    def i_success_building
    def i_success_campus
    def i_success_roomRate
    def i_success_phoneRate
    def i_success_site
    def i_success_state
    def i_success_county
    def i_success_college
    def i_success_department
    def i_success_partition

    def i_success_capacity = 1
    def i_success_maximumCapacity = 1
    def i_success_streetLine1 = "TTTTT"
    def i_success_streetLine2 = "TTTTT"
    def i_success_streetLine3 = "TTTTT"
    def i_success_city = "TTTTT"
    def i_success_zip = "TTTTT"
    def i_success_phoneArea = "TTTTT"
    def i_success_phoneNumber = "TTTTT"
    def i_success_phoneExtension = "TTTTT"
    def i_success_sex = "M"
    def i_success_keyNumber = "TTTTT"
    def i_success_countryPhone = "TTTT"
    def i_success_houseNumber = "TTTTT"
    def i_success_streetLine4 = "TTTTT"
    //Invalid test data (For failure tests)
    def i_failure_building
    def i_failure_campus
    def i_failure_roomRate
    def i_failure_phoneRate
    def i_failure_site
    def i_failure_state
    def i_failure_county
    def i_failure_college
    def i_failure_department
    def i_failure_partition

    def i_failure_capacity = 1
    def i_failure_maximumCapacity = 1
    def i_failure_streetLine1 = "TTTTT"
    def i_failure_streetLine2 = "TTTTT"
    def i_failure_streetLine3 = "TTTTT"
    def i_failure_city = "TTTTT"
    def i_failure_zip = "TTTTT"
    def i_failure_phoneArea = "TTTTT"
    def i_failure_phoneNumber = "TTTTT"
    def i_failure_phoneExtension = "TTTTT"
    def i_failure_sex = "M"
    def i_failure_keyNumber = "TTTTT"
    def i_failure_countryPhone = "TTTT"
    def i_failure_houseNumber = "TTTTT"
    def i_failure_streetLine4 = "TTTTT"

    //Test data for creating updating domain instance
    //Valid test data (For success tests)
    def u_success_building
    def u_success_campus
    def u_success_roomRate
    def u_success_phoneRate
    def u_success_site
    def u_success_state
    def u_success_county
    def u_success_college
    def u_success_department
    def u_success_partition

    def u_success_capacity = 1
    def u_success_maximumCapacity = 1
    def u_success_streetLine1 = "TTTTT"
    def u_success_streetLine2 = "TTTTT"
    def u_success_streetLine3 = "TTTTT"
    def u_success_city = "TTTTT"
    def u_success_zip = "TTTTT"
    def u_success_phoneArea = "TTTTT"
    def u_success_phoneNumber = "TTTTT"
    def u_success_phoneExtension = "TTTTT"
    def u_success_sex = "F"
    def u_success_keyNumber = "TTTTT"
    def u_success_countryPhone = "TTTT"
    def u_success_houseNumber = "TTTTT"
    def u_success_streetLine4 = "TTTTT"
    //Valid test data (For failure tests)
    def u_failure_building
    def u_failure_campus
    def u_failure_roomRate
    def u_failure_phoneRate
    def u_failure_site
    def u_failure_state
    def u_failure_county
    def u_failure_college
    def u_failure_department
    def u_failure_partition

    def u_failure_capacity = 1
    def u_failure_maximumCapacity = 1
    def u_failure_streetLine1 = "TTTTT"
    def u_failure_streetLine2 = "TTTTT"
    def u_failure_streetLine3 = "TTTTT"
    def u_failure_city = "TTTTT"
    def u_failure_zip = "TTTTT"
    def u_failure_phoneArea = "TTTTT"
    def u_failure_phoneNumber = "TTTTT"
    def u_failure_phoneExtension = "TTTTT"
    def u_failure_sex = "X"
    def u_failure_keyNumber = "TTTTT"
    def u_failure_countryPhone = "TTTT"
    def u_failure_houseNumber = "TTTTT"
    def u_failure_streetLine4 = "TTTTT"


	@Before
	public void setUp() {
        formContext = ["SSASECT"] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
        initializeTestDataForReferences()
    }

    //This method is used to initialize test data for references.
    //A method is required to execute database calls as it requires a active transaction


    void initializeTestDataForReferences() {
        //Valid test data (For success tests)
        i_success_building = Building.findByCode("GRANT")
        i_success_campus = Campus.findByCode("M")
        i_success_roomRate = RoomRate.findByCode("STND")
        i_success_phoneRate = PhoneRate.findByCode("PRVD")
        i_success_site = Site.findByCode("001")
        i_success_state = State.findByCode("NY")
        i_success_county = County.findByCode("180")
        i_success_college = College.findByCode("AS")
        i_success_department = Department.findByCode("ENGL")
        i_success_partition = null

        //Invalid test data (For failure tests)
        i_failure_building = Building.findByCode("AHO")
        i_failure_campus = Campus.findByCode("M")
        i_failure_roomRate = RoomRate.findByCode("STND")
        i_failure_phoneRate = PhoneRate.findByCode("PRVD")
        i_failure_site = Site.findByCode("001")
        i_failure_state = State.findByCode("NY")
        i_failure_county = County.findByCode("180")
        i_failure_college = College.findByCode("AS")
        i_failure_department = Department.findByCode("ENGL")
        i_failure_partition = null

        //Valid test data (For success tests)
        u_success_building = Building.findByCode("GRANT")
        u_success_campus = Campus.findByCode("M")
        u_success_roomRate = RoomRate.findByCode("STND")
        u_success_phoneRate = PhoneRate.findByCode("PRVD")
        u_success_site = Site.findByCode("001")
        u_success_state = State.findByCode("NY")
        u_success_county = County.findByCode("180")
        u_success_college = College.findByCode("AS")
        u_success_department = Department.findByCode("ENGL")
        u_success_partition = null

        //Valid test data (For failure tests)
        u_failure_building = Building.findByCode("AHO")
        u_failure_campus = Campus.findByCode("M")
        u_failure_roomRate = RoomRate.findByCode("STND")
        u_failure_phoneRate = PhoneRate.findByCode("PRVD")
        u_failure_site = Site.findByCode("001")
        u_failure_state = State.findByCode("NY")
        u_failure_county = County.findByCode("180")
        u_failure_college = College.findByCode("AS")
        u_failure_department = Department.findByCode("ENGL")
        u_failure_partition = null
    }


	@After
	public void tearDown() {
        super.tearDown()
    }


	@Test
    void testCreateValidHousingLocationBuildingDescription() {
        def housingLocationBuildingDescription = newValidForCreateHousingLocationBuildingDescription()
        housingLocationBuildingDescription.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull housingLocationBuildingDescription.id
        assertNotNull housingLocationBuildingDescription.lastModified
        assertNotNull housingLocationBuildingDescription.lastModifiedBy
        assertNotNull housingLocationBuildingDescription.dataOrigin
    }


	@Test
    void testCreateInvalidHousingLocationBuildingDescription() {
        def housingLocationBuildingDescription = newInvalidForCreateHousingLocationBuildingDescription()
        shouldFail {
            housingLocationBuildingDescription.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidHousingLocationBuildingDescription() {
        def housingLocationBuildingDescription = newValidForCreateHousingLocationBuildingDescription()
        housingLocationBuildingDescription.save(failOnError: true, flush: true)
        assertNotNull housingLocationBuildingDescription.id
        assertEquals 0L, housingLocationBuildingDescription.version
        assertEquals i_success_capacity, housingLocationBuildingDescription.capacity
        assertEquals i_success_maximumCapacity, housingLocationBuildingDescription.maximumCapacity
        assertEquals i_success_streetLine1, housingLocationBuildingDescription.streetLine1
        assertEquals i_success_streetLine2, housingLocationBuildingDescription.streetLine2
        assertEquals i_success_streetLine3, housingLocationBuildingDescription.streetLine3
        assertEquals i_success_city, housingLocationBuildingDescription.city
        assertEquals i_success_zip, housingLocationBuildingDescription.zip
        assertEquals i_success_phoneArea, housingLocationBuildingDescription.phoneArea
        assertEquals i_success_phoneNumber, housingLocationBuildingDescription.phoneNumber
        assertEquals i_success_phoneExtension, housingLocationBuildingDescription.phoneExtension
        assertEquals i_success_sex, housingLocationBuildingDescription.sex
        assertEquals i_success_keyNumber, housingLocationBuildingDescription.keyNumber
        assertEquals i_success_countryPhone, housingLocationBuildingDescription.countryPhone
        assertEquals i_success_houseNumber, housingLocationBuildingDescription.houseNumber
        assertEquals i_success_streetLine4, housingLocationBuildingDescription.streetLine4

        //Update the entity
        housingLocationBuildingDescription.capacity = u_success_capacity
        housingLocationBuildingDescription.maximumCapacity = u_success_maximumCapacity
        housingLocationBuildingDescription.streetLine1 = u_success_streetLine1
        housingLocationBuildingDescription.streetLine2 = u_success_streetLine2
        housingLocationBuildingDescription.streetLine3 = u_success_streetLine3
        housingLocationBuildingDescription.city = u_success_city
        housingLocationBuildingDescription.zip = u_success_zip
        housingLocationBuildingDescription.phoneArea = u_success_phoneArea
        housingLocationBuildingDescription.phoneNumber = u_success_phoneNumber
        housingLocationBuildingDescription.phoneExtension = u_success_phoneExtension
        housingLocationBuildingDescription.sex = u_success_sex
        housingLocationBuildingDescription.keyNumber = u_success_keyNumber
        housingLocationBuildingDescription.countryPhone = u_success_countryPhone
        housingLocationBuildingDescription.houseNumber = u_success_houseNumber
        housingLocationBuildingDescription.streetLine4 = u_success_streetLine4


        housingLocationBuildingDescription.campus = u_success_campus

        housingLocationBuildingDescription.roomRate = u_success_roomRate

        housingLocationBuildingDescription.phoneRate = u_success_phoneRate

        housingLocationBuildingDescription.site = u_success_site

        housingLocationBuildingDescription.state = u_success_state

        housingLocationBuildingDescription.county = u_success_county

        housingLocationBuildingDescription.college = u_success_college

        housingLocationBuildingDescription.department = u_success_department

        housingLocationBuildingDescription.partition = u_success_partition
        housingLocationBuildingDescription.save(failOnError: true, flush: true)
        //Asset for sucessful update
        housingLocationBuildingDescription = HousingLocationBuildingDescription.get(housingLocationBuildingDescription.id)
        assertEquals 1L, housingLocationBuildingDescription?.version
        assertEquals u_success_capacity, housingLocationBuildingDescription.capacity
        assertEquals u_success_maximumCapacity, housingLocationBuildingDescription.maximumCapacity
        assertEquals u_success_streetLine1, housingLocationBuildingDescription.streetLine1
        assertEquals u_success_streetLine2, housingLocationBuildingDescription.streetLine2
        assertEquals u_success_streetLine3, housingLocationBuildingDescription.streetLine3
        assertEquals u_success_city, housingLocationBuildingDescription.city
        assertEquals u_success_zip, housingLocationBuildingDescription.zip
        assertEquals u_success_phoneArea, housingLocationBuildingDescription.phoneArea
        assertEquals u_success_phoneNumber, housingLocationBuildingDescription.phoneNumber
        assertEquals u_success_phoneExtension, housingLocationBuildingDescription.phoneExtension
        assertEquals u_success_sex, housingLocationBuildingDescription.sex
        assertEquals u_success_keyNumber, housingLocationBuildingDescription.keyNumber
        assertEquals u_success_countryPhone, housingLocationBuildingDescription.countryPhone
        assertEquals u_success_houseNumber, housingLocationBuildingDescription.houseNumber
        assertEquals u_success_streetLine4, housingLocationBuildingDescription.streetLine4


        housingLocationBuildingDescription.campus = u_success_campus

        housingLocationBuildingDescription.roomRate = u_success_roomRate

        housingLocationBuildingDescription.phoneRate = u_success_phoneRate

        housingLocationBuildingDescription.site = u_success_site

        housingLocationBuildingDescription.state = u_success_state

        housingLocationBuildingDescription.county = u_success_county

        housingLocationBuildingDescription.college = u_success_college

        housingLocationBuildingDescription.department = u_success_department

        housingLocationBuildingDescription.partition = u_success_partition
    }


    @Test
    void testUpdateInvalidHousingLocationBuildingDescription() {
        def housingLocationBuildingDescription = newValidForCreateHousingLocationBuildingDescription()
        housingLocationBuildingDescription.save(failOnError: true, flush: true)
        assertNotNull housingLocationBuildingDescription.id
        assertEquals 0L, housingLocationBuildingDescription.version
        assertEquals i_success_capacity, housingLocationBuildingDescription.capacity
        assertEquals i_success_maximumCapacity, housingLocationBuildingDescription.maximumCapacity
        assertEquals i_success_streetLine1, housingLocationBuildingDescription.streetLine1
        assertEquals i_success_streetLine2, housingLocationBuildingDescription.streetLine2
        assertEquals i_success_streetLine3, housingLocationBuildingDescription.streetLine3
        assertEquals i_success_city, housingLocationBuildingDescription.city
        assertEquals i_success_zip, housingLocationBuildingDescription.zip
        assertEquals i_success_phoneArea, housingLocationBuildingDescription.phoneArea
        assertEquals i_success_phoneNumber, housingLocationBuildingDescription.phoneNumber
        assertEquals i_success_phoneExtension, housingLocationBuildingDescription.phoneExtension
        assertEquals i_success_sex, housingLocationBuildingDescription.sex
        assertEquals i_success_keyNumber, housingLocationBuildingDescription.keyNumber
        assertEquals i_success_countryPhone, housingLocationBuildingDescription.countryPhone
        assertEquals i_success_houseNumber, housingLocationBuildingDescription.houseNumber
        assertEquals i_success_streetLine4, housingLocationBuildingDescription.streetLine4

        //Update the entity with invalid values
        housingLocationBuildingDescription.capacity = u_failure_capacity
        housingLocationBuildingDescription.maximumCapacity = u_failure_maximumCapacity
        housingLocationBuildingDescription.streetLine1 = u_failure_streetLine1
        housingLocationBuildingDescription.streetLine2 = u_failure_streetLine2
        housingLocationBuildingDescription.streetLine3 = u_failure_streetLine3
        housingLocationBuildingDescription.city = u_failure_city
        housingLocationBuildingDescription.zip = u_failure_zip
        housingLocationBuildingDescription.phoneArea = u_failure_phoneArea
        housingLocationBuildingDescription.phoneNumber = u_failure_phoneNumber
        housingLocationBuildingDescription.phoneExtension = u_failure_phoneExtension
        housingLocationBuildingDescription.sex = u_failure_sex
        housingLocationBuildingDescription.keyNumber = u_failure_keyNumber
        housingLocationBuildingDescription.countryPhone = u_failure_countryPhone
        housingLocationBuildingDescription.houseNumber = u_failure_houseNumber
        housingLocationBuildingDescription.streetLine4 = u_failure_streetLine4


        housingLocationBuildingDescription.campus = u_failure_campus

        housingLocationBuildingDescription.roomRate = u_failure_roomRate

        housingLocationBuildingDescription.phoneRate = u_failure_phoneRate

        housingLocationBuildingDescription.site = u_failure_site

        housingLocationBuildingDescription.state = u_failure_state

        housingLocationBuildingDescription.county = u_failure_county

        housingLocationBuildingDescription.college = u_failure_college

        housingLocationBuildingDescription.department = u_failure_department

        housingLocationBuildingDescription.partition = u_failure_partition
        shouldFail {
            housingLocationBuildingDescription.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testOptimisticLock() {
        def housingLocationBuildingDescription = newValidForCreateHousingLocationBuildingDescription()
        housingLocationBuildingDescription.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update SV_SLBBLDG set SLBBLDG_VERSION = 999 where SLBBLDG_SURROGATE_ID = ?", [housingLocationBuildingDescription.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        housingLocationBuildingDescription.capacity = u_success_capacity
        housingLocationBuildingDescription.maximumCapacity = u_success_maximumCapacity
        housingLocationBuildingDescription.streetLine1 = u_success_streetLine1
        housingLocationBuildingDescription.streetLine2 = u_success_streetLine2
        housingLocationBuildingDescription.streetLine3 = u_success_streetLine3
        housingLocationBuildingDescription.city = u_success_city
        housingLocationBuildingDescription.zip = u_success_zip
        housingLocationBuildingDescription.phoneArea = u_success_phoneArea
        housingLocationBuildingDescription.phoneNumber = u_success_phoneNumber
        housingLocationBuildingDescription.phoneExtension = u_success_phoneExtension
        housingLocationBuildingDescription.sex = u_success_sex
        housingLocationBuildingDescription.keyNumber = u_success_keyNumber
        housingLocationBuildingDescription.countryPhone = u_success_countryPhone
        housingLocationBuildingDescription.houseNumber = u_success_houseNumber
        housingLocationBuildingDescription.streetLine4 = u_success_streetLine4
        shouldFail(HibernateOptimisticLockingFailureException) {
            housingLocationBuildingDescription.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteHousingLocationBuildingDescription() {
        def housingLocationBuildingDescription = newValidForCreateHousingLocationBuildingDescription()
        housingLocationBuildingDescription.save(failOnError: true, flush: true)
        def id = housingLocationBuildingDescription.id
        assertNotNull id
        housingLocationBuildingDescription.delete()
        assertNull HousingLocationBuildingDescription.get(id)
    }


    @Test
    void testValidation() {
        def housingLocationBuildingDescription = newInvalidForCreateHousingLocationBuildingDescription()
        assertTrue "HousingLocationBuildingDescription could not be validated as expected due to ${housingLocationBuildingDescription.errors}", housingLocationBuildingDescription.validate()
    }


    @Test
    void testNullValidationFailure() {
        def housingLocationBuildingDescription = new HousingLocationBuildingDescription()
        assertFalse "HousingLocationBuildingDescription should have failed validation", housingLocationBuildingDescription.validate()
        assertErrorsFor housingLocationBuildingDescription, 'nullable',
                        [
                                'capacity',
                                'building',
                                'campus'
                        ]
        assertNoErrorsFor housingLocationBuildingDescription,
                          [
                                  'maximumCapacity',
                                  'streetLine1',
                                  'streetLine2',
                                  'streetLine3',
                                  'city',
                                  'zip',
                                  'phoneArea',
                                  'phoneNumber',
                                  'phoneExtension',
                                  'sex',
                                  'keyNumber',
                                  'countryPhone',
                                  'houseNumber',
                                  'streetLine4',
                                  'roomRate',
                                  'phoneRate',
                                  'site',
                                  'state',
                                  'county',
                                  'college',
                                  'department',
                                  'partition'
                          ]
    }


    @Test
    void testMaxValueValidationFailure() {
        def housingLocationBuildingDescription = newValidForCreateHousingLocationBuildingDescription()
        housingLocationBuildingDescription.capacity = 100000
        housingLocationBuildingDescription.maximumCapacity = 100000
        assertFalse "HousingLocationBuildingDescription should have failed validation", housingLocationBuildingDescription.validate()
        assertErrorsFor housingLocationBuildingDescription, 'max',
                        [
                                'capacity',
                                'maximumCapacity'
                        ]
    }


    @Test
    void testMinValueValidationFailure() {
        def housingLocationBuildingDescription = newValidForCreateHousingLocationBuildingDescription()
        housingLocationBuildingDescription.capacity = -100000
        housingLocationBuildingDescription.maximumCapacity = -100000
        assertFalse "HousingLocationBuildingDescription should have failed validation", housingLocationBuildingDescription.validate()
        assertErrorsFor housingLocationBuildingDescription, 'min',
                        [
                                'capacity',
                                'maximumCapacity'
                        ]
    }


    @Test
    void testMaxSizeValidationFailures() {
        def housingLocationBuildingDescription = new HousingLocationBuildingDescription(
                streetLine1: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                streetLine2: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                streetLine3: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                city: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                zip: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
                phoneArea: 'XXXXXXXX',
                phoneNumber: 'XXXXXXXXXXXXXX',
                phoneExtension: 'XXXXXXXXXXXX',
                sex: 'XXX',
                keyNumber: 'XXXXXXX',
                countryPhone: 'XXXXXX',
                houseNumber: 'XXXXXXXXXXXX',
                streetLine4: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX')
        assertFalse "HousingLocationBuildingDescription should have failed validation", housingLocationBuildingDescription.validate()
        assertErrorsFor housingLocationBuildingDescription, 'maxSize', ['streetLine1', 'streetLine2', 'streetLine3', 'city', 'zip', 'phoneArea', 'phoneNumber', 'phoneExtension', 'sex', 'keyNumber', 'countryPhone', 'houseNumber', 'streetLine4']
    }


    private def newValidForCreateHousingLocationBuildingDescription() {
        def housingLocationBuildingDescription = new HousingLocationBuildingDescription(
                capacity: i_success_capacity,
                maximumCapacity: i_success_maximumCapacity,
                streetLine1: i_success_streetLine1,
                streetLine2: i_success_streetLine2,
                streetLine3: i_success_streetLine3,
                city: i_success_city,
                zip: i_success_zip,
                phoneArea: i_success_phoneArea,
                phoneNumber: i_success_phoneNumber,
                phoneExtension: i_success_phoneExtension,
                sex: i_success_sex,
                keyNumber: i_success_keyNumber,
                countryPhone: i_success_countryPhone,
                houseNumber: i_success_houseNumber,
                streetLine4: i_success_streetLine4,
                building: i_success_building,
                campus: i_success_campus,
                roomRate: i_success_roomRate,
                phoneRate: i_success_phoneRate,
                site: i_success_site,
                state: i_success_state,
                county: i_success_county,
                college: i_success_college,
                department: i_success_department,
                partition: i_success_partition
        )
        return housingLocationBuildingDescription
    }


    private def newInvalidForCreateHousingLocationBuildingDescription() {
        def housingLocationBuildingDescription = new HousingLocationBuildingDescription(
                capacity: i_failure_capacity,
                maximumCapacity: i_failure_maximumCapacity,
                streetLine1: i_failure_streetLine1,
                streetLine2: i_failure_streetLine2,
                streetLine3: i_failure_streetLine3,
                city: i_failure_city,
                zip: i_failure_zip,
                phoneArea: i_failure_phoneArea,
                phoneNumber: i_failure_phoneNumber,
                phoneExtension: i_failure_phoneExtension,
                sex: i_failure_sex,
                keyNumber: i_failure_keyNumber,
                countryPhone: i_failure_countryPhone,
                houseNumber: i_failure_houseNumber,
                streetLine4: i_failure_streetLine4,
                building: i_failure_building,
                campus: i_failure_campus,
                roomRate: i_failure_roomRate,
                phoneRate: i_failure_phoneRate,
                site: i_failure_site,
                state: i_failure_state,
                county: i_failure_county,
                college: i_failure_college,
                department: i_failure_department,
                partition: i_failure_partition
        )
        return housingLocationBuildingDescription
    }


    @Test
    void testFetchByBuilding() {
        def housingLocationBuildingDescriptionList = HousingLocationBuildingDescription.fetchBySomeHousingLocationBuildingDescriptionBuilding()
        assertTrue housingLocationBuildingDescriptionList.list.size() > 20

        //expects a parameter map with Building object
        housingLocationBuildingDescriptionList = HousingLocationBuildingDescription.fetchBySomeHousingLocationBuildingDescriptionBuilding([building: Building.findByCode("AA")])
        assertEquals housingLocationBuildingDescriptionList.list.size(), 1
        assertEquals "AA", housingLocationBuildingDescriptionList.list[0].building.code

        //expects a String
        housingLocationBuildingDescriptionList = HousingLocationBuildingDescription.fetchBySomeHousingLocationBuildingDescriptionBuilding("AA")
        assertEquals housingLocationBuildingDescriptionList.list.size(), 1
        assertEquals "AA", housingLocationBuildingDescriptionList.list[0].building.code
    }


    @Test
    void testFetchValidBuilding() {
        //expects the Building object
        def housingLocationBuildingDescriptionRec = HousingLocationBuildingDescription.fetchValidBuilding(Building.findByCode("AA"))
        assertEquals "AA", housingLocationBuildingDescriptionRec.building.code

        //expects a String
        housingLocationBuildingDescriptionRec = HousingLocationBuildingDescription.fetchValidBuilding("AA")
        assertEquals "AA", housingLocationBuildingDescriptionRec.building.code
    }


    @Test
    void testFetchAllByBuilding() {
        String filter = "API"
        def buildings = HousingLocationBuildingDescription.fetchAllByBuilding(formatWildCard(filter))
        assertNotNull buildings
        buildings.each { building ->
            assertTrue building[0].contains(filter)
        }
    }

    /**
     * add wildcard and change to upper case for searching
     * @param filter
     * @return filter
     */

    static String formatWildCard(filter) {
        def wildCard = "%"
        if (StringUtils.isBlank(filter)) {
            filter = wildCard
        } else if (!(filter =~ /%/)) {
            filter = wildCard + filter.toUpperCase() + wildCard
        } else filter = filter.toUpperCase()
        return filter
    }
}

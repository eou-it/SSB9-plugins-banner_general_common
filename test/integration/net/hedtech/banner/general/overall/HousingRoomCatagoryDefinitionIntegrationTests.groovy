
/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import grails.validation.ValidationException
import groovy.sql.Sql
import java.text.SimpleDateFormat
import net.hedtech.banner.general.system.Building
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

class HousingRoomCatagoryDefinitionIntegrationTests extends BaseIntegrationTestCase {

	//Test data for creating new domain instance
	//Valid test data (For success tests)
	def i_success_building

	def i_success_code = "TTTT"
	def i_success_description = "insert_success_description"
	//Invalid test data (For failure tests)
	def i_failure_building

	def i_failure_code = "TTTT"
	def i_failure_description = "insert failure description field with a string of more than 30 characters"

	//Test data for creating updating domain instance
	//Valid test data (For success tests)
	def u_success_building

	def u_success_code = "TTTT"
	def u_success_description = "updated success description"
	//Valid test data (For failure tests)
	def u_failure_building

	def u_failure_code = "TTTT"
	def u_failure_description = "update failure description field with a string of more than 30 characters"


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
    	i_success_building = new Building(code : "AAAA", description: "Building A description")
        i_success_building.save(failOnError: true, flush: true)

		//Invalid test data (For failure tests)
	    i_failure_building = new Building(code : "BBBB", description: "Building B description")
        i_failure_building.save(failOnError: true, flush: true)

		//Valid test data (For success tests)
	    u_success_building = new Building(code : "CCCC", description: "Building C description")
        u_success_building.save(failOnError: true, flush: true)

		//Valid test data (For failure tests)
    	u_failure_building = new Building(code : "DDDD", description: "Building D description")
        u_failure_building.save(failOnError: true, flush: true)

	}

    @After
    public void tearDown() {
		super.tearDown()
	}


    @Test
	void testCreateValidHousingRoomCatagoryDefinition() {
		def housingRoomCatagoryDefinition = newValidForCreateHousingRoomCatagoryDefinition()
		housingRoomCatagoryDefinition.save( failOnError: true, flush: true )
		//Test if the generated entity now has an id assigned
        assertNotNull housingRoomCatagoryDefinition.id
	}

    @Test
	void testCreateInvalidHousingRoomCatagoryDefinition() {
		def housingRoomCatagoryDefinition = newInvalidForCreateHousingRoomCatagoryDefinition()
		shouldFail(ValidationException) {
            housingRoomCatagoryDefinition.save( failOnError: true, flush: true )
		}
	}


    @Test
	void testUpdateValidHousingRoomCatagoryDefinition() {
		def housingRoomCatagoryDefinition = newValidForCreateHousingRoomCatagoryDefinition()
		housingRoomCatagoryDefinition.save( failOnError: true, flush: true )
        assertNotNull housingRoomCatagoryDefinition.id
        assertEquals 0L, housingRoomCatagoryDefinition.version
        assertEquals i_success_code, housingRoomCatagoryDefinition.code
        assertEquals i_success_description, housingRoomCatagoryDefinition.description

		//Update the entity
		housingRoomCatagoryDefinition.description = u_success_description

		housingRoomCatagoryDefinition.save( failOnError: true, flush: true )
		//Assert for sucessful update
        housingRoomCatagoryDefinition = HousingRoomCatagoryDefinition.get( housingRoomCatagoryDefinition.id )
        assertEquals 1L, housingRoomCatagoryDefinition?.version
        assertEquals u_success_description, housingRoomCatagoryDefinition.description

	}


    @Test
	void testUpdateInvalidHousingRoomCatagoryDefinition() {
		def housingRoomCatagoryDefinition = newValidForCreateHousingRoomCatagoryDefinition()
		housingRoomCatagoryDefinition.save( failOnError: true, flush: true )
        assertNotNull housingRoomCatagoryDefinition.id
        assertEquals 0L, housingRoomCatagoryDefinition.version
        assertEquals i_success_code, housingRoomCatagoryDefinition.code
        assertEquals i_success_description, housingRoomCatagoryDefinition.description

		//Update the entity with invalid values
		housingRoomCatagoryDefinition.description = u_failure_description
        housingRoomCatagoryDefinition.building = u_failure_building

		shouldFail(ValidationException) {
            housingRoomCatagoryDefinition.save( failOnError: true, flush: true )
		}
	}


    @Test
    void testDates() {
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

    	def housingRoomCatagoryDefinition = newValidForCreateHousingRoomCatagoryDefinition()



    	housingRoomCatagoryDefinition.save(flush: true, failOnError: true)
    	housingRoomCatagoryDefinition.refresh()
    	assertNotNull "HousingRoomCatagoryDefinition should have been saved", housingRoomCatagoryDefinition.id

    	// test date values -
    	assertEquals date.format(today), date.format(housingRoomCatagoryDefinition.lastModified)
    	assertEquals hour.format(today), hour.format(housingRoomCatagoryDefinition.lastModified)
    }


    @Test
    void testOptimisticLock() {
		def housingRoomCatagoryDefinition = newValidForCreateHousingRoomCatagoryDefinition()
		housingRoomCatagoryDefinition.save( failOnError: true, flush: true )

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update SLRBCAT set SLRBCAT_VERSION = 999 where SLRBCAT_SURROGATE_ID = ?", [ housingRoomCatagoryDefinition.id ] )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
		//Try to update the entity
		//Update the entity
		housingRoomCatagoryDefinition.description = u_success_description
        shouldFail( HibernateOptimisticLockingFailureException ) {
            housingRoomCatagoryDefinition.save( failOnError: true, flush: true )
        }
    }


    @Test
	void testDeleteHousingRoomCatagoryDefinition() {
		def housingRoomCatagoryDefinition = newValidForCreateHousingRoomCatagoryDefinition()
		housingRoomCatagoryDefinition.save( failOnError: true, flush: true )
		def id = housingRoomCatagoryDefinition.id
		assertNotNull id
		housingRoomCatagoryDefinition.delete()
		assertNull HousingRoomCatagoryDefinition.get( id )
	}


    @Test
    void testValidation() {
       def housingRoomCatagoryDefinition = newInvalidForCreateHousingRoomCatagoryDefinition()
       assertFalse "HousingRoomCatagoryDefinition could not be validated as expected due to ${housingRoomCatagoryDefinition.errors}", housingRoomCatagoryDefinition.validate()
    }


    @Test
    void testNullValidationFailure() {
        def housingRoomCatagoryDefinition = new HousingRoomCatagoryDefinition()
        assertFalse "HousingRoomCatagoryDefinition should have failed validation", housingRoomCatagoryDefinition.validate()
        assertErrorsFor housingRoomCatagoryDefinition, 'nullable',
                                               [
                                                 'code',
                                                 'description',
                                                 'building'
                                               ]
    }


	private def newValidForCreateHousingRoomCatagoryDefinition() {
		def housingRoomCatagoryDefinition = new HousingRoomCatagoryDefinition(
			code: i_success_code,
			description: i_success_description,
			building: i_success_building,
		)
		return housingRoomCatagoryDefinition
	}

	private def newInvalidForCreateHousingRoomCatagoryDefinition() {
		def housingRoomCatagoryDefinition = new HousingRoomCatagoryDefinition(
			code: i_failure_code,
			description: i_failure_description,
			building: i_failure_building,
		)
		return housingRoomCatagoryDefinition
	}


    @Test
   void testFetchByCodeOrDescription() {
       //test to verify all records are returned when filter is not provided
       def result = HousingRoomCatagoryDefinition.fetchByCodeOrDescription()
       assertTrue result.get("list").size() > 0

       def housingRoomCatagoryDefinition = HousingRoomCatagoryDefinition.findByCode("CONF")
       //test to verify only the record specified in the filter is returned
       result = HousingRoomCatagoryDefinition.fetchByCodeOrDescription("CONF")
       assertTrue result.get("list").size() > 0
       assertTrue result.get("list").contains(housingRoomCatagoryDefinition)

       //test to verify no records are returned when invalid code is given
       result = HousingRoomCatagoryDefinition.fetchByCodeOrDescription("PQRS")
       assertFalse result.get("list").size() > 0
   }

}


/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of
 SunGard Higher Education and its subsidiaries. Any use of this software is limited
 solely to SunGard Higher Education licensees, and is further subject to the terms
 and conditions of one or more written license agreements between SunGard Higher
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
/**
 Banner Automator Version: 1.29
 Generated: Tue Nov 22 15:43:35 IST 2011
 */
package com.sungardhe.banner.general.overall

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import com.sungardhe.banner.exceptions.ApplicationException
import groovy.sql.Sql
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import com.sungardhe.banner.general.system.Building
import grails.validation.ValidationException
import java.text.SimpleDateFormat
import org.junit.Ignore


class HousingRoomCatagoryDefinitionIntegrationTests extends BaseIntegrationTestCase {

	/*PROTECTED REGION ID(housingroomcatagorydefinition_domain_integration_test_data) ENABLED START*/
	//Test data for creating new domain instance
	//Valid test data (For success tests)
	def i_success_building

	def i_success_code = "TTTT"
	def i_success_description = "insert_success_description"
	//Invalid test data (For failure tests)
	def i_failure_building

	def i_failure_code = "TTTT"
	def i_failure_description = "insert_failure_description"

	//Test data for creating updating domain instance
	//Valid test data (For success tests)
	def u_success_building

	def u_success_code = "TTTT"
	def u_success_description = "updated success description"
	//Valid test data (For failure tests)
	def u_failure_building

	def u_failure_code = "TTTT"
	def u_failure_description = "updated failure description"
	/*PROTECTED REGION END*/

	protected void setUp() {
		formContext = ['SLABLDG', 'SLQBCAT'] // Since we are not testing a controller, we need to explicitly set this
		super.setUp()
		initializeTestDataForReferences()
	}

	//This method is used to initialize test data for references.
	//A method is required to execute database calls as it requires a active transaction
	void initializeTestDataForReferences() {
		/*PROTECTED REGION ID(housingroomcatagorydefinition_domain_integration_test_data_initialization) ENABLED START*/
		//Valid test data (For success tests)
    	i_success_building = Building.findWhere(code : "B00A") //TODO: fill in the query condition

		//Invalid test data (For failure tests)
	    i_failure_building = Building.findWhere(code : "ABCD") //TODO: fill in the query condition

		//Valid test data (For success tests)
	    u_success_building = Building.findWhere(code : "B00G") //TODO: fill in the query condition

		//Valid test data (For failure tests)
    	u_failure_building = Building.findWhere(code : "PQRS") //TODO: fill in the query condition

		//Test data for references for custom tests
		/*PROTECTED REGION END*/
	}

	protected void tearDown() {
		super.tearDown()
	}


    @Ignore
	void testCreateValidHousingRoomCatagoryDefinition() {
		def housingRoomCatagoryDefinition = newValidForCreateHousingRoomCatagoryDefinition()
		housingRoomCatagoryDefinition.save( failOnError: true, flush: true )
		//Test if the generated entity now has an id assigned
        assertNotNull housingRoomCatagoryDefinition.id
	}

	void testCreateInvalidHousingRoomCatagoryDefinition() {
		def housingRoomCatagoryDefinition = newInvalidForCreateHousingRoomCatagoryDefinition()
		shouldFail(ValidationException) {
            housingRoomCatagoryDefinition.save( failOnError: true, flush: true )
		}
	}


    @Ignore
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


    @Ignore
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


    @Ignore
    void testDates() {
        def time = new SimpleDateFormat('HHmmss')
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

    @Ignore
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

    @Ignore
	void testDeleteHousingRoomCatagoryDefinition() {
		def housingRoomCatagoryDefinition = newValidForCreateHousingRoomCatagoryDefinition()
		housingRoomCatagoryDefinition.save( failOnError: true, flush: true )
		def id = housingRoomCatagoryDefinition.id
		assertNotNull id
		housingRoomCatagoryDefinition.delete()
		assertNull HousingRoomCatagoryDefinition.get( id )
	}

    void testValidation() {
       def housingRoomCatagoryDefinition = newInvalidForCreateHousingRoomCatagoryDefinition()
       assertFalse "HousingRoomCatagoryDefinition could not be validated as expected due to ${housingRoomCatagoryDefinition.errors}", housingRoomCatagoryDefinition.validate()
    }

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

   /**
     * Please put all the custom tests in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(housingroomcatagorydefinition_custom_integration_test_methods) ENABLED START*/

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

    /*PROTECTED REGION END*/
}

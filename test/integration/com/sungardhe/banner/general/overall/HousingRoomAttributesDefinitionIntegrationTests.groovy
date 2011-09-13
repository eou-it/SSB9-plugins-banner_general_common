
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
 Banner Automator Version: 1.21
 Generated: Fri Jul 01 19:13:33 IST 2011
 */
package com.sungardhe.banner.general.overall

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import com.sungardhe.banner.exceptions.ApplicationException
import groovy.sql.Sql
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import com.sungardhe.banner.general.system.Building
import com.sungardhe.banner.general.system.BuildingAndRoomAttribute
import org.junit.Ignore


class HousingRoomAttributesDefinitionIntegrationTests extends BaseIntegrationTestCase {

	/*PROTECTED REGION ID(housingroomattributesdefinition_domain_integration_test_data) ENABLED START*/
	//Test data for creating new domain instance
	//Valid test data (For success tests)
	def i_success_building
	def i_success_buildingAndRoomAttribute

	def i_success_roomNumber = "101"
	def i_success_termEffective = "200410"
	def i_success_mustMatch = "Y"
	//Invalid test data (For failure tests)
	def i_failure_building
	def i_failure_buildingAndRoomAttribute

	def i_failure_roomNumber = "TTTTT"
	def i_failure_termEffective = "TTTTT"
	def i_failure_mustMatch = "N"

	//Test data for creating updating domain instance
	//Valid test data (For success tests)
	def u_success_building
	def u_success_buildingAndRoomAttribute

	def u_success_roomNumber = "201"
	def u_success_termEffective = "YYYYY"
	def u_success_mustMatch = "N"
	//Valid test data (For failure tests)
	def u_failure_building
	def u_failure_buildingAndRoomAttribute

	def u_failure_roomNumber = "TTTTT"
	def u_failure_termEffective = "TTTTT"
	def u_failure_mustMatch = null
	/*PROTECTED REGION END*/

	protected void setUp() {
		formContext = ['SLARDEF'] // Since we are not testing a controller, we need to explicitly set this
		super.setUp()
		initializeTestDataForReferences()
	}

	//This method is used to initialize test data for references.
	//A method is required to execute database calls as it requires a active transaction
	void initializeTestDataForReferences() {
		/*PROTECTED REGION ID(housingroomattributesdefinition_domain_integration_test_data_initialization) ENABLED START*/
		//Valid test data (For success tests)
    	i_success_building = Building.findWhere(code:"HUM")
    	i_success_buildingAndRoomAttribute = BuildingAndRoomAttribute.findWhere(code:"AUD")

		//Invalid test data (For failure tests)
	    i_failure_building = Building.findWhere(code:"SOUTH")
	    i_failure_buildingAndRoomAttribute = BuildingAndRoomAttribute.findWhere(code:"LAB")

		//Valid test data (For success tests)
	    u_success_building = Building.findWhere(code:"BIOL")
	    u_success_buildingAndRoomAttribute = BuildingAndRoomAttribute.findWhere(code:"GYM")

		//Valid test data (For failure tests)
    	u_failure_building = Building.findWhere(code:"MENDAL")
    	u_failure_buildingAndRoomAttribute = BuildingAndRoomAttribute.findWhere(code:"GCL")

		//Test data for references for custom tests
		/*PROTECTED REGION END*/
	}

	protected void tearDown() {
		super.tearDown()
	}

	void testCreateValidHousingRoomAttributesDefinition() {
		def housingRoomAttributesDefinition = newValidForCreateHousingRoomAttributesDefinition()
		save housingRoomAttributesDefinition
		//Test if the generated entity now has an id assigned
        assertNotNull housingRoomAttributesDefinition.id
	}



    @Ignore
	void testUpdateValidHousingRoomAttributesDefinition() {
		def housingRoomAttributesDefinition = newValidForCreateHousingRoomAttributesDefinition()
		housingRoomAttributesDefinition.save(flush:true, failOnError:true)
//        housingRoomAttributesDefinition.refresh()

        assertNotNull housingRoomAttributesDefinition.id
        assertEquals 0L, housingRoomAttributesDefinition.version
        assertEquals i_success_roomNumber, housingRoomAttributesDefinition.roomNumber
        assertEquals i_success_termEffective, housingRoomAttributesDefinition.termEffective
        assertEquals i_success_mustMatch, housingRoomAttributesDefinition.mustMatch

		//Update the entity
		housingRoomAttributesDefinition.mustMatch = u_success_mustMatch
        housingRoomAttributesDefinition.save(flush:true, failOnError:true)

		//Asset for sucessful update
        housingRoomAttributesDefinition = HousingRoomAttributesDefinition.get( housingRoomAttributesDefinition.id )
        assertEquals 1L, housingRoomAttributesDefinition?.version
        assertEquals u_success_mustMatch, housingRoomAttributesDefinition.mustMatch

	}


    void testOptimisticLock() {
		def housingRoomAttributesDefinition = newValidForCreateHousingRoomAttributesDefinition()
		save housingRoomAttributesDefinition

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update SLRRDEF set SLRRDEF_VERSION = 999 where SLRRDEF_SURROGATE_ID = ?", [ housingRoomAttributesDefinition.id ] )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
		//Try to update the entity
		//Update the entity
		housingRoomAttributesDefinition.mustMatch = u_success_mustMatch
        shouldFail( HibernateOptimisticLockingFailureException ) {
            housingRoomAttributesDefinition.save( failOnError: true, flush: true )
        }
    }


	void testDeleteHousingRoomAttributesDefinition() {
		def housingRoomAttributesDefinition = newValidForCreateHousingRoomAttributesDefinition()
		save housingRoomAttributesDefinition
		def id = housingRoomAttributesDefinition.id
		assertNotNull id
		housingRoomAttributesDefinition.delete()
		assertNull HousingRoomAttributesDefinition.get( id )
	}


    void testNullValidationFailure() {
        def housingRoomAttributesDefinition = new HousingRoomAttributesDefinition()
        assertFalse "HousingRoomAttributesDefinition should have failed validation", housingRoomAttributesDefinition.validate()
        assertErrorsFor housingRoomAttributesDefinition, 'nullable',
                                               [
                                                 'roomNumber',
                                                 'termEffective',
                                                 'building',
                                                 'buildingAndRoomAttribute'
                                               ]
        assertNoErrorsFor housingRoomAttributesDefinition,
        									   [
             									 'mustMatch'
											   ]
    }

    void testMaxSizeValidationFailures() {
        def housingRoomAttributesDefinition = new HousingRoomAttributesDefinition(
        mustMatch:'XXX' )
		assertFalse "HousingRoomAttributesDefinition should have failed validation", housingRoomAttributesDefinition.validate()
		assertErrorsFor housingRoomAttributesDefinition, 'maxSize', [ 'mustMatch' ]
    }




    void testFetchByBuildingRoomNumberAndTermEffective() {
        def housingRoomAttributesDefinition = newValidForCreateHousingRoomAttributesDefinition()
		save housingRoomAttributesDefinition
        def lst = HousingRoomAttributesDefinition.fetchByBuildingRoomNumberAndTermEffective(i_success_building.code,i_success_roomNumber,i_success_termEffective)
        assertNotNull lst
        assertTrue "List is not empty", !lst.isEmpty()
        def resultAttributes = []
        for (HousingRoomAttributesDefinition attr:lst)  {
            resultAttributes.add(attr.buildingAndRoomAttribute)
        }
        BuildingAndRoomAttribute audBuilding = BuildingAndRoomAttribute.findByCode("AUD")
        assertTrue resultAttributes.contains(audBuilding)
    }

	private def newValidForCreateHousingRoomAttributesDefinition() {
		def housingRoomAttributesDefinition = new HousingRoomAttributesDefinition(
			roomNumber: i_success_roomNumber,
			termEffective: i_success_termEffective,
			mustMatch: i_success_mustMatch,
			building: i_success_building,
			buildingAndRoomAttribute: i_success_buildingAndRoomAttribute,
        	lastModified: new Date(),
			lastModifiedBy: "test",
			dataOrigin: "Banner"
	    )
		return housingRoomAttributesDefinition
	}

	private def newInvalidForCreateHousingRoomAttributesDefinition() {
		def housingRoomAttributesDefinition = new HousingRoomAttributesDefinition(
			roomNumber: i_failure_roomNumber,
			termEffective: i_failure_termEffective,
			mustMatch: i_failure_mustMatch,
			building: i_failure_building,
			buildingAndRoomAttribute: i_failure_buildingAndRoomAttribute,
        	lastModified: new Date(),
			lastModifiedBy: "test",
			dataOrigin: "Banner"
		)
		return housingRoomAttributesDefinition
	}

   /**
     * Please put all the custom tests in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(housingroomattributesdefinition_custom_integration_test_methods) ENABLED START*/
    /*PROTECTED REGION END*/
}

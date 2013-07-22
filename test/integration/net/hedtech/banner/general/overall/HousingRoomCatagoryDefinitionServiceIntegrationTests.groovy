
/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/**
 Banner Automator Version: 1.29
 Generated: Tue Nov 22 15:43:57 IST 2011
 */
package net.hedtech.banner.general.overall

import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.Building


class HousingRoomCatagoryDefinitionServiceIntegrationTests extends BaseIntegrationTestCase {

  def housingRoomCatagoryDefinitionService

	/*PROTECTED REGION ID(housingroomcatagorydefinition_service_integration_test_data) ENABLED START*/
	//Test data for creating new domain instance
	//Valid test data (For success tests)
    def i_success_building
	def i_success_code = "TTTT"
	def i_success_description = "insert success description"


	//Invalid test data (For failure tests)
    def i_failure_building
	def i_failure_code = "TTTT"
	def i_failure_description = "insert failure description field with a string of more than 30 characters"


	//Test data for creating updating domain instance
	//Valid test data (For success tests)
    def u_success_building
	def u_success_code = "TTTT"
	def u_success_description = "update success description"


	//Valid test data (For failure tests)
    def u_failure_building
	def u_failure_code = "TTTT"
	def u_failure_description = "update failure description field with a string of more than 30 characters"

	//TODO: Create keyblock map for insert (For success tests)
	def i_success_keyBlockMap = [:]

	//TODO: Create keyblock map for insert (For failure tests)
	def i_failure_keyBlockMap = [:]

	//TODO: Create keyblock map for update (If success required)
	def u_success_keyBlockMap = [:]

	//TODO: Create keyblock map for update (If failure required)
	def u_failure_keyBlockMap = [:]

	/*PROTECTED REGION END*/

	protected void setUp() {
		formContext = ['SSASECT']
		super.setUp()
        initializeTestDataForReferences()
	}

	//This method is used to initialize test data for references.
	//A method is required to execute database calls as it requires a active transaction
	void initializeTestDataForReferences() {
		/*PROTECTED REGION ID(housingroomcatagorydefinition_domain_service_integration_test_data_initialization) ENABLED START*/
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
    	u_failure_building = Building.findWhere(code : "PQRS")

		//Test data for references for custom tests
		/*PROTECTED REGION END*/
	}

	protected void tearDown() {
		super.tearDown()
	}


	void testHousingRoomCatagoryDefinitionValidCreate() {
		def housingRoomCatagoryDefinition = newValidForCreateHousingRoomCatagoryDefinition()
		def map = [keyBlock: i_success_keyBlockMap,
			domainModel: housingRoomCatagoryDefinition]
		housingRoomCatagoryDefinition = housingRoomCatagoryDefinitionService.create(map)
		assertNotNull "HousingRoomCatagoryDefinition ID is null in HousingRoomCatagoryDefinition Service Tests Create", housingRoomCatagoryDefinition.id
		assertNotNull "HousingRoomCatagoryDefinition building is null in HousingRoomCatagoryDefinition Service Tests", housingRoomCatagoryDefinition.building
	    assertNotNull housingRoomCatagoryDefinition.version
	    assertNotNull housingRoomCatagoryDefinition.dataOrigin
		assertNotNull housingRoomCatagoryDefinition.lastModifiedBy
	    assertNotNull housingRoomCatagoryDefinition.lastModified
    }

	void testHousingRoomCatagoryDefinitionInvalidCreate() {
		def housingRoomCatagoryDefinition = newInvalidForCreateHousingRoomCatagoryDefinition()
		def map = [keyBlock: i_failure_keyBlockMap,
			domainModel: housingRoomCatagoryDefinition]
		shouldFail(ApplicationException) {
			housingRoomCatagoryDefinitionService.create(map)
		}
    }



	void testHousingRoomCatagoryDefinitionValidUpdate() {
		def housingRoomCatagoryDefinition = newValidForCreateHousingRoomCatagoryDefinition()
		def map = [keyBlock: i_success_keyBlockMap,
			domainModel: housingRoomCatagoryDefinition]
		housingRoomCatagoryDefinition = housingRoomCatagoryDefinitionService.create(map)
		assertNotNull "HousingRoomCatagoryDefinition ID is null in HousingRoomCatagoryDefinition Service Tests Create", housingRoomCatagoryDefinition.id
		assertNotNull "HousingRoomCatagoryDefinition building is null in HousingRoomCatagoryDefinition Service Tests", housingRoomCatagoryDefinition.building
	    assertNotNull housingRoomCatagoryDefinition.version
	    assertNotNull housingRoomCatagoryDefinition.dataOrigin
		assertNotNull housingRoomCatagoryDefinition.lastModifiedBy
	    assertNotNull housingRoomCatagoryDefinition.lastModified
		//Update the entity with new values
		housingRoomCatagoryDefinition.description = u_success_description

		map.keyBlock = u_success_keyBlockMap
		map.domainModel = housingRoomCatagoryDefinition
		housingRoomCatagoryDefinition = housingRoomCatagoryDefinitionService.update(map)
		// test the values
		assertEquals u_success_description, housingRoomCatagoryDefinition.description
	}


	void testHousingRoomCatagoryDefinitionInvalidUpdate() {
		def housingRoomCatagoryDefinition = newValidForCreateHousingRoomCatagoryDefinition()
		def map = [keyBlock: i_success_keyBlockMap,
			domainModel: housingRoomCatagoryDefinition]
		housingRoomCatagoryDefinition = housingRoomCatagoryDefinitionService.create(map)
		assertNotNull "HousingRoomCatagoryDefinition ID is null in HousingRoomCatagoryDefinition Service Tests Create", housingRoomCatagoryDefinition.id
		assertNotNull "HousingRoomCatagoryDefinition building is null in HousingRoomCatagoryDefinition Service Tests", housingRoomCatagoryDefinition.building
	    assertNotNull housingRoomCatagoryDefinition.version
	    assertNotNull housingRoomCatagoryDefinition.dataOrigin
		assertNotNull housingRoomCatagoryDefinition.lastModifiedBy
	    assertNotNull housingRoomCatagoryDefinition.lastModified
		//Update the entity with new invalid values
		housingRoomCatagoryDefinition.description = u_failure_description

		map.keyBlock = u_failure_keyBlockMap
		map.domainModel = housingRoomCatagoryDefinition
		shouldFail(ApplicationException) {
			housingRoomCatagoryDefinition = housingRoomCatagoryDefinitionService.update(map)
		}
	}



	void testHousingRoomCatagoryDefinitionDelete() {
		def housingRoomCatagoryDefinition = newValidForCreateHousingRoomCatagoryDefinition()
		def map = [keyBlock: i_success_keyBlockMap,
			domainModel: housingRoomCatagoryDefinition]
		housingRoomCatagoryDefinition = housingRoomCatagoryDefinitionService.create(map)
		assertNotNull "HousingRoomCatagoryDefinition ID is null in HousingRoomCatagoryDefinition Service Tests Create", housingRoomCatagoryDefinition.id
		def id = housingRoomCatagoryDefinition.id
        map = [keyBlock: i_success_keyBlockMap,
                        domainModel: housingRoomCatagoryDefinition]
		housingRoomCatagoryDefinitionService.delete( map )
		assertNull "HousingRoomCatagoryDefinition should have been deleted", housingRoomCatagoryDefinition.get(id)
  	}



	void testReadOnly() {
		def housingRoomCatagoryDefinition = newValidForCreateHousingRoomCatagoryDefinition()
		def map = [keyBlock: i_success_keyBlockMap,
			domainModel: housingRoomCatagoryDefinition]
		housingRoomCatagoryDefinition = housingRoomCatagoryDefinitionService.create(map)
		assertNotNull "HousingRoomCatagoryDefinition ID is null in HousingRoomCatagoryDefinition Service Tests Create", housingRoomCatagoryDefinition.id
		housingRoomCatagoryDefinition.building = u_success_building

		housingRoomCatagoryDefinition.code = u_success_code
		try {
        	housingRoomCatagoryDefinitionService.update([domainModel: housingRoomCatagoryDefinition])
        	fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
		}
    	catch (ApplicationException ae) {
			 assertApplicationException ae, "readonlyFieldsCannotBeModified"
    	}
	}

	private def newValidForCreateHousingRoomCatagoryDefinition() {
		def housingRoomCatagoryDefinition = new HousingRoomCatagoryDefinition(
			code: i_success_code,
			description: i_success_description,

			building: i_success_building
	    )
		return housingRoomCatagoryDefinition
	}

	private def newInvalidForCreateHousingRoomCatagoryDefinition() {
		def housingRoomCatagoryDefinition = new HousingRoomCatagoryDefinition(
			code: i_failure_code,
			description: i_failure_description,

			building: i_failure_building
		)
		return housingRoomCatagoryDefinition
	}

	/**
	 * Please put all the custom service tests in this protected section to protect the code
     * from being overwritten on re-generation
	*/
	/*PROTECTED REGION ID(housingroomcatagorydefinition_custom_service_integration_test_methods) ENABLED START*/

	/*PROTECTED REGION END*/
}


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
 Banner Automator Version: 1.24
 Generated: Tue Aug 09 14:09:56 IST 2011
 */
package com.sungardhe.banner.general.overall
import com.sungardhe.banner.testing.BaseIntegrationTestCase
import com.sungardhe.banner.exceptions.ApplicationException
import org.junit.Test



class SequenceNumberBaseServiceIntegrationTests extends BaseIntegrationTestCase {

     def sequenceNumberBaseService

	/*PROTECTED REGION ID(sequencenumberbase_service_integration_test_data) ENABLED START*/
	//Test data for creating new domain instance
	//Valid test data (For success tests)
	def i_success_function = "TTTTT"
	def i_success_sequenceNumberPrefix = "#"
	def i_success_maximumSequenceNumber =  1


	//Invalid test data (For failure tests)
	def i_failure_function = "TTTTT"
	def i_failure_sequenceNumberPrefix = "##"
	def i_failure_maximumSequenceNumber =  1


	//Test data for creating updating domain instance
	//Valid test data (For success tests)
	def u_success_function = "UUUUU"
	def u_success_sequenceNumberPrefix = "A"
	def u_success_maximumSequenceNumber =  2


	//Valid test data (For failure tests)
	def u_failure_function = "TTTTT"
	def u_failure_sequenceNumberPrefix = "##"
	def u_failure_maximumSequenceNumber =  1

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
		formContext = ['GEAFUNC']
		super.setUp()
	}

	//This method is used to initialize test data for references.
	//A method is required to execute database calls as it requires a active transaction
	void initializeTestDataForReferences() {
		/*PROTECTED REGION ID(sequencenumberbase_domain_service_integration_test_data_initialization) ENABLED START*/
		//Valid test data (For success tests)

		//Invalid test data (For failure tests)

		//Valid test data (For success tests)

		//Valid test data (For failure tests)

		//Test data for references for custom tests
		/*PROTECTED REGION END*/
	}

	protected void tearDown() {
		super.tearDown()
	}

	void testSequenceNumberBaseValidCreate() {
		def sequenceNumberBase = newValidForCreateSequenceNumberBase()
		def map = [keyBlock: i_success_keyBlockMap,
			domainModel: sequenceNumberBase]
		sequenceNumberBase = sequenceNumberBaseService.create(map)
		assertNotNull "SequenceNumberBase ID is null in SequenceNumberBase Service Tests Create", sequenceNumberBase.id
	    assertNotNull sequenceNumberBase.version
	    assertNotNull sequenceNumberBase.dataOrigin
		assertNotNull sequenceNumberBase.lastModifiedBy
	    assertNotNull sequenceNumberBase.lastModified
    }

	void testSequenceNumberBaseInvalidCreate() {
		def sequenceNumberBase = newInvalidForCreateSequenceNumberBase()
		def map = [keyBlock: i_failure_keyBlockMap,
			domainModel: sequenceNumberBase]
		shouldFail(ApplicationException) {
			sequenceNumberBaseService.create(map)
		}
    }

	void testSequenceNumberBaseValidUpdate() {
		def sequenceNumberBase = newValidForCreateSequenceNumberBase()
		def map = [keyBlock: i_success_keyBlockMap,
			domainModel: sequenceNumberBase]
		sequenceNumberBase = sequenceNumberBaseService.create(map)
		assertNotNull "SequenceNumberBase ID is null in SequenceNumberBase Service Tests Create", sequenceNumberBase.id
	    assertNotNull sequenceNumberBase.version
	    assertNotNull sequenceNumberBase.dataOrigin
		assertNotNull sequenceNumberBase.lastModifiedBy
	    assertNotNull sequenceNumberBase.lastModified
		//Update the entity with new values
		sequenceNumberBase.sequenceNumberPrefix = u_success_sequenceNumberPrefix
		sequenceNumberBase.maximumSequenceNumber = u_success_maximumSequenceNumber

		map.keyBlock = u_success_keyBlockMap
		map.domainModel = sequenceNumberBase
		sequenceNumberBase = sequenceNumberBaseService.update(map)
		// test the values
		assertEquals u_success_sequenceNumberPrefix, sequenceNumberBase.sequenceNumberPrefix
		assertEquals u_success_maximumSequenceNumber, sequenceNumberBase.maximumSequenceNumber
	}

	void testSequenceNumberBaseInvalidUpdate() {
		def sequenceNumberBase = newValidForCreateSequenceNumberBase()
		def map = [keyBlock: i_success_keyBlockMap,
			domainModel: sequenceNumberBase]
		sequenceNumberBase = sequenceNumberBaseService.create(map)
		assertNotNull "SequenceNumberBase ID is null in SequenceNumberBase Service Tests Create", sequenceNumberBase.id
	    assertNotNull sequenceNumberBase.version
	    assertNotNull sequenceNumberBase.dataOrigin
		assertNotNull sequenceNumberBase.lastModifiedBy
	    assertNotNull sequenceNumberBase.lastModified
		//Update the entity with new invalid values
		sequenceNumberBase.sequenceNumberPrefix = u_failure_sequenceNumberPrefix
		sequenceNumberBase.maximumSequenceNumber = u_failure_maximumSequenceNumber

		map.keyBlock = u_failure_keyBlockMap
		map.domainModel = sequenceNumberBase
		shouldFail(ApplicationException) {
			sequenceNumberBase = sequenceNumberBaseService.update(map)
		}
	}

	void testSequenceNumberBaseDelete() {
		def sequenceNumberBase = newValidForCreateSequenceNumberBase()
		def map = [keyBlock: i_success_keyBlockMap,
			domainModel: sequenceNumberBase]
		sequenceNumberBase = sequenceNumberBaseService.create(map)
		assertNotNull "SequenceNumberBase ID is null in SequenceNumberBase Service Tests Create", sequenceNumberBase.id
		def id = sequenceNumberBase.id
		map = [keyBlock: i_success_keyBlockMap,
			domainModel: sequenceNumberBase]
		sequenceNumberBaseService.delete( map )
		assertNull "SequenceNumberBase should have been deleted", sequenceNumberBase.get(id)
  	}


	void testReadOnly() {
		def sequenceNumberBase = newValidForCreateSequenceNumberBase()
		def map = [keyBlock: i_success_keyBlockMap,
			domainModel: sequenceNumberBase]
		sequenceNumberBase = sequenceNumberBaseService.create(map)
		assertNotNull "SequenceNumberBase ID is null in SequenceNumberBase Service Tests Create", sequenceNumberBase.id
		map = [keyBlock: i_success_keyBlockMap,
			domainModel: sequenceNumberBase]
        map.domainModel.function = u_success_function
		try {
        	sequenceNumberBaseService.update([domainModel: sequenceNumberBase])
        	fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
		}
    	catch (ApplicationException ae) {
			 assertApplicationException ae, "readonlyFieldsCannotBeModified"
    	}
	}

	private def newValidForCreateSequenceNumberBase() {
		def sequenceNumberBase = new SequenceNumberBase(
			function: i_success_function,
			sequenceNumberPrefix: i_success_sequenceNumberPrefix,
			maximumSequenceNumber: i_success_maximumSequenceNumber,
        	lastModified: new Date(),
			lastModifiedBy: "test",
			dataOrigin: "Banner"
	    )
		return sequenceNumberBase
	}

	private def newInvalidForCreateSequenceNumberBase() {
		def sequenceNumberBase = new SequenceNumberBase(
			function: i_failure_function,
			sequenceNumberPrefix: i_failure_sequenceNumberPrefix,
			maximumSequenceNumber: i_failure_maximumSequenceNumber,
        	lastModified: new Date(),
			lastModifiedBy: "test",
			dataOrigin: "Banner"
		)
		return sequenceNumberBase
	}

	/**
	 * Please put all the custom service tests in this protected section to protect the code
     * from being overwritten on re-generation
	*/
	/*PROTECTED REGION ID(sequencenumberbase_custom_service_integration_test_methods) ENABLED START*/
    @Test
    void testGetNextSequenceNumberBase(){
        def sequenceNumberBase = new SequenceNumberBase(function: "TEST",sequenceNumberPrefix: "A",maximumSequenceNumber: 25)
        save sequenceNumberBase

        String function = "TEST"
        Integer maximumSequence = 9999
        String expectedSequence = "A0026"
        assertEquals  expectedSequence,sequenceNumberBaseService.getNextSequenceNumberBase(function,maximumSequence)
        maximumSequence = 26
        expectedSequence = "B01"
        assertEquals  expectedSequence,sequenceNumberBaseService.getNextSequenceNumberBase(function,maximumSequence)
    }

	/*PROTECTED REGION END*/
}

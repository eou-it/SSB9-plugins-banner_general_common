
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
 Generated: Tue Aug 09 14:09:48 IST 2011
 */
package com.sungardhe.banner.general.overall

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import grails.validation.ValidationException


class SequenceNumberBaseIntegrationTests extends BaseIntegrationTestCase {

	/*PROTECTED REGION ID(sequencenumberbase_domain_integration_test_data) ENABLED START*/
	//Test data for creating new domain instance
	//Valid test data (For success tests)

	def i_success_function = "TTTTT"
	def i_success_sequenceNumberPrefix = "#"
	def i_success_maximumSequenceNumber = 1
	//Invalid test data (For failure tests)

	def i_failure_function = "TTTTT"
	def i_failure_sequenceNumberPrefix = "##"
	def i_failure_maximumSequenceNumber = 1

	//Test data for creating updating domain instance
	//Valid test data (For success tests)

	def u_success_function = "UUUUU"
	def u_success_sequenceNumberPrefix = "A"
	def u_success_maximumSequenceNumber = 1
	//Valid test data (For failure tests)

	def u_failure_function = "TTTTT"
	def u_failure_sequenceNumberPrefix = "##"
	def u_failure_maximumSequenceNumber = 1
	/*PROTECTED REGION END*/

	protected void setUp() {
		formContext = ['SLQEVNT'] // Since we are not testing a controller, we need to explicitly set this
		super.setUp()
		initializeTestDataForReferences()
	}

	//This method is used to initialize test data for references.
	//A method is required to execute database calls as it requires a active transaction
	void initializeTestDataForReferences() {
		/*PROTECTED REGION ID(sequencenumberbase_domain_integration_test_data_initialization) ENABLED START*/
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

	void testCreateValidSequenceNumberBase() {
		def sequenceNumberBase = newValidForCreateSequenceNumberBase()
		sequenceNumberBase.save( failOnError: true, flush: true )
		//Test if the generated entity now has an id assigned
        assertNotNull sequenceNumberBase.id
        assertNotNull sequenceNumberBase.lastModified
        assertNotNull sequenceNumberBase.lastModifiedBy
        assertNotNull sequenceNumberBase.dataOrigin
	}

	void testCreateInvalidSequenceNumberBase() {
		def sequenceNumberBase = newInvalidForCreateSequenceNumberBase()
		shouldFail(ValidationException) {
            sequenceNumberBase.save( failOnError: true, flush: true )
		}
	}

	void testUpdateValidSequenceNumberBase() {
		def sequenceNumberBase = newValidForCreateSequenceNumberBase()
		sequenceNumberBase.save( failOnError: true, flush: true )
        assertNotNull sequenceNumberBase.id
        assertEquals 0L, sequenceNumberBase.version
        assertEquals i_success_function, sequenceNumberBase.function
        assertEquals i_success_sequenceNumberPrefix, sequenceNumberBase.sequenceNumberPrefix
        assertEquals i_success_maximumSequenceNumber, sequenceNumberBase.maximumSequenceNumber

		//Update the entity
		sequenceNumberBase.sequenceNumberPrefix = u_success_sequenceNumberPrefix
		sequenceNumberBase.maximumSequenceNumber = u_success_maximumSequenceNumber
		sequenceNumberBase.save( failOnError: true, flush: true )
		//Assert for sucessful update
        sequenceNumberBase = SequenceNumberBase.get( sequenceNumberBase.id )
        assertEquals 1L, sequenceNumberBase?.version
        assertEquals u_success_sequenceNumberPrefix, sequenceNumberBase.sequenceNumberPrefix
        assertEquals u_success_maximumSequenceNumber, sequenceNumberBase.maximumSequenceNumber
	}

	void testUpdateInvalidSequenceNumberBase() {
		def sequenceNumberBase = newValidForCreateSequenceNumberBase()
		sequenceNumberBase.save( failOnError: true, flush: true )
        assertNotNull sequenceNumberBase.id
        assertEquals 0L, sequenceNumberBase.version
        assertEquals i_success_function, sequenceNumberBase.function
        assertEquals i_success_sequenceNumberPrefix, sequenceNumberBase.sequenceNumberPrefix
        assertEquals i_success_maximumSequenceNumber, sequenceNumberBase.maximumSequenceNumber

		//Update the entity with invalid values
		sequenceNumberBase.sequenceNumberPrefix = u_failure_sequenceNumberPrefix
		sequenceNumberBase.maximumSequenceNumber = u_failure_maximumSequenceNumber
		shouldFail(ValidationException) {
            sequenceNumberBase.save( failOnError: true, flush: true )
		}
	}

    void testOptimisticLock() {
		def sequenceNumberBase = newValidForCreateSequenceNumberBase()
		sequenceNumberBase.save( failOnError: true, flush: true )

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update SOBSEQN set SOBSEQN_VERSION = 999 where SOBSEQN_SURROGATE_ID = ?", [ sequenceNumberBase.id ] )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
		//Try to update the entity
		//Update the entity
		sequenceNumberBase.sequenceNumberPrefix = u_success_sequenceNumberPrefix
		sequenceNumberBase.maximumSequenceNumber = u_success_maximumSequenceNumber
        shouldFail( HibernateOptimisticLockingFailureException ) {
            sequenceNumberBase.save( failOnError: true, flush: true )
        }
    }

	void testDeleteSequenceNumberBase() {
		def sequenceNumberBase = newValidForCreateSequenceNumberBase()
		sequenceNumberBase.save( failOnError: true, flush: true )
		def id = sequenceNumberBase.id
		assertNotNull id
		sequenceNumberBase.delete()
		assertNull SequenceNumberBase.get( id )
	}

    void testValidation() {
       def sequenceNumberBase = newInvalidForCreateSequenceNumberBase()
       assertFalse "SequenceNumberBase could not be validated as expected due to ${sequenceNumberBase.errors}", sequenceNumberBase.validate()
    }

    void testNullValidationFailure() {
        def sequenceNumberBase = new SequenceNumberBase()
        assertFalse "SequenceNumberBase should have failed validation", sequenceNumberBase.validate()
        assertErrorsFor sequenceNumberBase, 'nullable',
                                               [
                                                 'function',
                                                 'maximumSequenceNumber'
                                               ]
        assertNoErrorsFor sequenceNumberBase,
        									   [
             									 'sequenceNumberPrefix'
											   ]
    }

    void testMaxSizeValidationFailures() {
        def sequenceNumberBase = new SequenceNumberBase(
        sequenceNumberPrefix:'XXX' )
		assertFalse "SequenceNumberBase should have failed validation", sequenceNumberBase.validate()
		assertErrorsFor sequenceNumberBase, 'maxSize', [ 'sequenceNumberPrefix' ]
    }



	private def newValidForCreateSequenceNumberBase() {
		def sequenceNumberBase = new SequenceNumberBase(
			function: i_success_function,
			sequenceNumberPrefix: i_success_sequenceNumberPrefix,
			maximumSequenceNumber: i_success_maximumSequenceNumber,
		)
		return sequenceNumberBase
	}

	private def newInvalidForCreateSequenceNumberBase() {
		def sequenceNumberBase = new SequenceNumberBase(
			function: i_failure_function,
			sequenceNumberPrefix: i_failure_sequenceNumberPrefix,
			maximumSequenceNumber: i_failure_maximumSequenceNumber,
		)
		return sequenceNumberBase
	}

   /**
     * Please put all the custom tests in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(sequencenumberbase_custom_integration_test_methods) ENABLED START*/
    /*PROTECTED REGION END*/
}

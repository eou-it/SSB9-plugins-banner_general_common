
/*******************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.

 This copyrighted software contains confidential and proprietary information of
 SunGard Higher Education and its subsidiaries. Any use of this software is limited
 solely to SunGard Higher Education licensees, and is further subject to the terms
 and conditions of one or more written license agreements between SunGard Higher
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher
 Education in the U.S.A. and/or other regions and/or countries.
 *******************************************************************************/
/**
 Banner Automator Version: 1.29
 Generated: Thu Aug 01 15:12:58 IST 2013
 */
package net.hedtech.banner.general.overall

import grails.validation.ValidationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException
import groovy.sql.Sql
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

import java.text.SimpleDateFormat


class PinQuestionIntegrationTests extends BaseIntegrationTestCase {

     //Test data for creating new domain instance
	//Valid test data (For success tests)

	def i_success_pinQuestionId = "TTT12"
	def i_success_description = "Who is your first teacher at school?"
	def i_success_displayIndicator = true
	//Invalid test data (For failure tests)

	def i_failure_pinQuestionId = ""
	def i_failure_description = null
	def i_failure_displayIndicator = true

	//Test data for creating updating domain instance
	//Valid test data (For success tests)

	def u_success_pinQuestionId = "TTT12"
	def u_success_description = "TTTTTTTTTTTTT"
	def u_success_displayIndicator = false
	//Valid test data (For failure tests)

	def u_failure_pinQuestionId = "TTTTT"
	def u_failure_description = null
	def u_failure_displayIndicator = true
	/*PROTECTED REGION END*/

	protected void setUp() {
        formContext = ['GUAGMNU']// Since we are not testing a controller, we need to explicitly set this
		super.setUp()
		//initializeTestDataForReferences()
	}

	//This method is used to initialize test data for references.
	//A method is required to execute database calls as it requires a active transaction
	void initializeTestDataForReferences() {

	}

	protected void tearDown() {
		super.tearDown()
	}

	void testCreateValidPinQuestion() {
		def pinQuestion = newValidForCreatePinQuestion()
		pinQuestion.save( failOnError: true, flush: true )
		//Test if the generated entity now has an id assigned
        assertNotNull pinQuestion.id
	}

	void testCreateInvalidPinQuestion() {
		def pinQuestion = newInvalidForCreatePinQuestion()
		shouldFail(ValidationException) {
            pinQuestion.save( failOnError: true, flush: true )
		}
	}

	void testUpdateValidPinQuestion() {
		def pinQuestion = newValidForCreatePinQuestion()
		pinQuestion.save( failOnError: true, flush: true )
        assertNotNull pinQuestion.id
        assertEquals 0L, pinQuestion.version
        assertEquals i_success_pinQuestionId, pinQuestion.pinQuestionId
        assertEquals i_success_description, pinQuestion.description
        assertEquals i_success_displayIndicator, pinQuestion.displayIndicator

		//Update the entity
		pinQuestion.description = u_success_description
		pinQuestion.displayIndicator = u_success_displayIndicator
		pinQuestion.save( failOnError: true, flush: true )
		//Assert for sucessful update
        pinQuestion = PinQuestion.get( pinQuestion.id )
        assertEquals 1L, pinQuestion?.version
        assertEquals u_success_description, pinQuestion.description
        assertEquals u_success_displayIndicator, pinQuestion.displayIndicator
	}

	void testUpdateInvalidPinQuestion() {
		def pinQuestion = newValidForCreatePinQuestion()
		pinQuestion.save( failOnError: true, flush: true )
        assertNotNull pinQuestion.id
        assertEquals 0L, pinQuestion.version
        assertEquals i_success_pinQuestionId, pinQuestion.pinQuestionId
        assertEquals i_success_description, pinQuestion.description
        assertEquals i_success_displayIndicator, pinQuestion.displayIndicator

		//Update the entity with invalid values
		pinQuestion.description = u_failure_description
		pinQuestion.displayIndicator = u_failure_displayIndicator
		shouldFail(ValidationException) {
            pinQuestion.save( failOnError: true, flush: true )
		}
	}

    void testDates() {
        def time = new SimpleDateFormat('HHmmss')
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

    	def pinQuestion = newValidForCreatePinQuestion()



    	pinQuestion.save(flush: true, failOnError: true)
    	pinQuestion.refresh()
    	assertNotNull "PinQuestion should have been saved", pinQuestion.id

    	// test date values -
    	assertEquals date.format(today), date.format(pinQuestion.lastModified)
    	assertEquals hour.format(today), hour.format(pinQuestion.lastModified)


    }

    void testOptimisticLock() {
		def pinQuestion = newValidForCreatePinQuestion()
		pinQuestion.save( failOnError: true, flush: true )

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update GV_GOBQSTN set GOBQSTN_VERSION = 999 where GOBQSTN_SURROGATE_ID = ?", [ pinQuestion.id ] )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
		//Try to update the entity
		//Update the entity
		pinQuestion.description = u_success_description
		pinQuestion.displayIndicator = u_success_displayIndicator
        shouldFail( HibernateOptimisticLockingFailureException ) {
            pinQuestion.save( failOnError: true, flush: true )
        }
    }

	void testDeletePinQuestion() {
		def pinQuestion = newValidForCreatePinQuestion()
		pinQuestion.save( failOnError: true, flush: true )
		def id = pinQuestion.id
		assertNotNull id
		pinQuestion.delete()
		assertNull PinQuestion.get( id )
	}

    void testValidation() {
       def pinQuestion = newInvalidForCreatePinQuestion()
       assertFalse "PinQuestion could not be validated as expected due to ${pinQuestion.errors}", pinQuestion.validate()
    }

    void testNullValidationFailure() {
        def pinQuestion = new PinQuestion()
        assertFalse "PinQuestion should have failed validation", pinQuestion.validate()
        assertErrorsFor pinQuestion, 'nullable',
                                               [
                                                 'pinQuestionId',
                                                 'description',
                                                 'displayIndicator'
                                               ]
    }

	private def newValidForCreatePinQuestion() {
		def pinQuestion = new PinQuestion(
			pinQuestionId: i_success_pinQuestionId,
			description: i_success_description,
			displayIndicator: i_success_displayIndicator,
		)
		return pinQuestion
	}

	private def newInvalidForCreatePinQuestion() {
		def pinQuestion = new PinQuestion(
			pinQuestionId: i_failure_pinQuestionId,
			description: i_failure_description,
			displayIndicator: i_failure_displayIndicator,
		)
		return pinQuestion
	}

    void testFetchQuestions() {
        def pinQuestion = newValidForCreatePinQuestion()
        pinQuestion.save( failOnError: true, flush: true )
        assertNotNull pinQuestion.pinQuestionId
        List fetchedPinQuestions = PinQuestion.fetchQuestions()
        assertTrue fetchedPinQuestions.size()>0
    }

    void testFetchByQuestionId (){
        def pinQuestion = newValidForCreatePinQuestion()
        pinQuestion.save( failOnError: true, flush: true )
        assertNotNull pinQuestion.pinQuestionId
        String pinQuestionId = pinQuestion.pinQuestionId
        PinQuestion fetchedPinQuestion = PinQuestion.fetchQuestionOnId(pinQuestionId)
        assertNotNull fetchedPinQuestion.pinQuestionId
    }


}

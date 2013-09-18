
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
 Generated: Thu Aug 01 15:13:07 IST 2013
 */
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.PinQuestion

class GeneralForStoringResponsesAndPinQuestionServiceIntegrationTests extends BaseIntegrationTestCase {

  def generalForStoringResponsesAndPinQuestionService

	//Test data for creating new domain instance
	//Valid test data (For success tests)
    def i_success_pinQuestion
	def i_success_pidm
	def i_success_number =  1
	def i_success_questionDescription = "TTTTT"
	def i_success_answerDescription = "TTTTT"
	def i_success_answerSalt = "TTTTT"


	//Invalid test data (For failure tests)
    def i_failure_pinQuestion
	def i_failure_pidm =  1
	def i_failure_number =  1
	def i_failure_questionDescription = null
	def i_failure_answerDescription = null
	def i_failure_answerSalt = "TTTTT"


	//Test data for creating updating domain instance
	//Valid test data (For success tests)
    def u_success_pinQuestion
	def u_success_pidm
	def u_success_number =  2
	def u_success_questionDescription = "Which is ur favourite hang out place?"
	def u_success_answerDescription = "TTTTT"
	def u_success_answerSalt = "DUMMY"


	//Valid test data (For failure tests)
    def u_failure_pinQuestion =  null
	def u_failure_pidm =  1
	def u_failure_number =  1
	def u_failure_questionDescription = null
	def u_failure_answerDescription = null
	def u_failure_answerSalt = "DUMMY"

	protected void setUp() {
        formContext = ['GUAGMNU']
		super.setUp()
        initializeTestDataForReferences()
	}

	//This method is used to initialize test data for references.
	//A method is required to execute database alls as it requires a active transaction
	void initializeTestDataForReferences() {
        i_success_pidm = PersonUtility.getPerson("HOSWEB017").pidm
        u_success_pidm = i_success_pidm
	}

	protected void tearDown() {
		super.tearDown()
	}

	void testGeneralForStoringResponsesAndPinQuestionValidCreate() {
		def generalForStoringResponsesAndPinQuestion1 = newValidForCreateGeneralForStoringResponsesAndPinQuestion()
		def map = [domainModel: generalForStoringResponsesAndPinQuestion1]
        assertNotNull generalForStoringResponsesAndPinQuestion1.pinQuestion
		def generalForStoringResponsesAndPinQuestion = generalForStoringResponsesAndPinQuestionService.create(map)
		assertNotNull "GeneralForStoringResponsesAndPinQuestion ID is null in GeneralForStoringResponsesAndPinQuestion Service Tests Create", generalForStoringResponsesAndPinQuestion.id
		assertNotNull "GeneralForStoringResponsesAndPinQuestion pinQuestion is null in GeneralForStoringResponsesAndPinQuestion Service Tests", generalForStoringResponsesAndPinQuestion.pinQuestion
	    assertNotNull generalForStoringResponsesAndPinQuestion.version
	    assertNotNull generalForStoringResponsesAndPinQuestion.dataOrigin
		assertNotNull generalForStoringResponsesAndPinQuestion.lastModifiedBy
	    assertNotNull generalForStoringResponsesAndPinQuestion.lastModified
    }

	void testGeneralForStoringResponsesAndPinQuestionInvalidCreate() {
		def generalForStoringResponsesAndPinQuestion = newInvalidForCreateGeneralForStoringResponsesAndPinQuestion()
		def map = [domainModel: generalForStoringResponsesAndPinQuestion]
		shouldFail(ApplicationException) {
			generalForStoringResponsesAndPinQuestionService.create(map)
		}
    }

	void testGeneralForStoringResponsesAndPinQuestionValidUpdate() {
		def generalForStoringResponsesAndPinQuestion = newValidForCreateGeneralForStoringResponsesAndPinQuestion()
		def map = [domainModel: generalForStoringResponsesAndPinQuestion]
        generalForStoringResponsesAndPinQuestion = generalForStoringResponsesAndPinQuestionService.create(map)
		assertNotNull "GeneralForStoringResponsesAndPinQuestion ID is null in GeneralForStoringResponsesAndPinQuestion Service Tests Create", generalForStoringResponsesAndPinQuestion.id
		assertNotNull "GeneralForStoringResponsesAndPinQuestion pinQuestion is null in GeneralForStoringResponsesAndPinQuestion Service Tests", generalForStoringResponsesAndPinQuestion.pinQuestion
	    assertNotNull generalForStoringResponsesAndPinQuestion.version
	    assertNotNull generalForStoringResponsesAndPinQuestion.dataOrigin
		assertNotNull generalForStoringResponsesAndPinQuestion.lastModifiedBy
	    assertNotNull generalForStoringResponsesAndPinQuestion.lastModified
		//Update the entity with new values
        def u_success_pinQuestion = newValidForCreatePinQuestion("TT1234")
        u_success_pinQuestion.save( failOnError: true, flush: true )
		generalForStoringResponsesAndPinQuestion.pinQuestion = u_success_pinQuestion
		generalForStoringResponsesAndPinQuestion.questionDescription = u_success_questionDescription
		generalForStoringResponsesAndPinQuestion.answerDescription = u_success_answerDescription
		generalForStoringResponsesAndPinQuestion.answerSalt = u_success_answerSalt

		map.domainModel = generalForStoringResponsesAndPinQuestion
		generalForStoringResponsesAndPinQuestion = generalForStoringResponsesAndPinQuestionService.update(map)
		// test the values
		groovy.util.GroovyTestCase.assertEquals u_success_questionDescription, generalForStoringResponsesAndPinQuestion.questionDescription
        groovy.util.GroovyTestCase.assertEquals u_success_answerDescription, generalForStoringResponsesAndPinQuestion.answerDescription
        groovy.util.GroovyTestCase.assertEquals u_success_answerSalt, generalForStoringResponsesAndPinQuestion.answerSalt
		assertEquals u_success_pinQuestion, generalForStoringResponsesAndPinQuestion.pinQuestion
	}

	void testGeneralForStoringResponsesAndPinQuestionInvalidUpdate() {
        def generalForStoringResponsesAndPinQuestion = newValidForCreateGeneralForStoringResponsesAndPinQuestion()
        def map = [domainModel: generalForStoringResponsesAndPinQuestion]
        generalForStoringResponsesAndPinQuestion = generalForStoringResponsesAndPinQuestionService.create(map)
      	assertNotNull "GeneralForStoringResponsesAndPinQuestion ID is null in GeneralForStoringResponsesAndPinQuestion Service Tests Create", generalForStoringResponsesAndPinQuestion.id
		assertNotNull "GeneralForStoringResponsesAndPinQuestion pinQuestion is null in GeneralForStoringResponsesAndPinQuestion Service Tests", generalForStoringResponsesAndPinQuestion.pinQuestion
	    assertNotNull generalForStoringResponsesAndPinQuestion.version
	    assertNotNull generalForStoringResponsesAndPinQuestion.dataOrigin
		assertNotNull generalForStoringResponsesAndPinQuestion.lastModifiedBy
	    assertNotNull generalForStoringResponsesAndPinQuestion.lastModified
		//Update the entity with new invalid values
		generalForStoringResponsesAndPinQuestion.pinQuestion = u_failure_pinQuestion
		generalForStoringResponsesAndPinQuestion.questionDescription = u_failure_questionDescription
		generalForStoringResponsesAndPinQuestion.answerDescription = u_failure_answerDescription
		generalForStoringResponsesAndPinQuestion.answerSalt = u_failure_answerSalt
		map.domainModel = generalForStoringResponsesAndPinQuestion
		shouldFail(ApplicationException) {
			generalForStoringResponsesAndPinQuestion = generalForStoringResponsesAndPinQuestionService.update(map)
		}
	}

	void testGeneralForStoringResponsesAndPinQuestionDelete() {
		def generalForStoringResponsesAndPinQuestion = newValidForCreateGeneralForStoringResponsesAndPinQuestion()
		def map = [domainModel: generalForStoringResponsesAndPinQuestion]
        generalForStoringResponsesAndPinQuestion = generalForStoringResponsesAndPinQuestionService.create(map)
		assertNotNull "GeneralForStoringResponsesAndPinQuestion ID is null in GeneralForStoringResponsesAndPinQuestion Service Tests Create", generalForStoringResponsesAndPinQuestion.id
		def id = generalForStoringResponsesAndPinQuestion.id
        map.domainModel = generalForStoringResponsesAndPinQuestion
		generalForStoringResponsesAndPinQuestionService.delete( map )
		assertNull "GeneralForStoringResponsesAndPinQuestion should have been deleted", generalForStoringResponsesAndPinQuestion.get(id)
  	}


	void testReadOnly() {
		def generalForStoringResponsesAndPinQuestion = newValidForCreateGeneralForStoringResponsesAndPinQuestion()
		def map = [domainModel: generalForStoringResponsesAndPinQuestion]
        generalForStoringResponsesAndPinQuestion = generalForStoringResponsesAndPinQuestionService.create(map)
		assertNotNull "GeneralForStoringResponsesAndPinQuestion ID is null in GeneralForStoringResponsesAndPinQuestion Service Tests Create", generalForStoringResponsesAndPinQuestion.id
        generalForStoringResponsesAndPinQuestion.pidm = u_success_pidm
        generalForStoringResponsesAndPinQuestion.number = u_success_number
		try {
        	generalForStoringResponsesAndPinQuestionService.update([domainModel: generalForStoringResponsesAndPinQuestion])
        	fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
		}
    	catch (ApplicationException ae) {
			 assertApplicationException ae, "readonlyFieldsCannotBeModified"
    	}
	}

	private def newValidForCreateGeneralForStoringResponsesAndPinQuestion() {
        def pinQuestion = newValidForCreatePinQuestion("TT123")
      	pinQuestion.save( failOnError: true, flush: true )
		def generalForStoringResponsesAndPinQuestion = new GeneralForStoringResponsesAndPinQuestion(
			pidm: i_success_pidm,
			number: i_success_number,
			questionDescription: i_success_questionDescription,
			answerDescription: i_success_answerDescription,
			answerSalt: i_success_answerSalt,
			pinQuestion: pinQuestion
	    )
		return generalForStoringResponsesAndPinQuestion
	}

	private def newInvalidForCreateGeneralForStoringResponsesAndPinQuestion() {
		def generalForStoringResponsesAndPinQuestion = new GeneralForStoringResponsesAndPinQuestion(
			pidm: i_failure_pidm,
			number: i_failure_number,
			questionDescription: i_failure_questionDescription,
			answerDescription: i_failure_answerDescription,
			answerSalt: i_failure_answerSalt,
			pinQuestion: i_failure_pinQuestion
		)
		return generalForStoringResponsesAndPinQuestion
	}

    private def newValidForCreatePinQuestion(String pinQuestionId) {
   		def pinQuestion = new PinQuestion(
   			pinQuestionId: pinQuestionId,
   			description: "Favorite drink",
   			displayIndicator: true,
   		)
   		return pinQuestion
   	}
}
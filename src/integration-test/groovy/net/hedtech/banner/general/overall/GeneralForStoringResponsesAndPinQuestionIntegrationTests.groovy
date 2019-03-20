
/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
/**
 Banner Automator Version: 1.29
 Generated: Thu Aug 01 15:12:58 IST 2013
 */
package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.GeneralCommonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

import java.text.SimpleDateFormat

class GeneralForStoringResponsesAndPinQuestionIntegrationTests extends BaseIntegrationTestCase {

    //Test data for creating new domain instance
    //Valid test data (For success tests)
    def i_success_pinQuestion

    def i_success_pidm = 1
    def i_success_number = 1
    def i_success_number1 = 2
    def i_success_questionDescription = null
    def i_success_user_entered_questionDescription = "My First School1"
    def i_success_answerDescription = "Cluny"
    def i_success_answerSalt = "DUMMY"
    //Invalid test data (For failure tests)
    def i_failure_pinQuestion

    def i_failure_pidm
    def i_failure_number = -1
    def i_failure_answerDescription = null
    def i_failure_answerSalt = ""

    //Test data for creating updating domain instance
    //Valid test data (For success tests)
    def u_success_pinQuestion = null

    def u_success_pidm
    def u_success_questionDescription = "Favourite place"
    def u_success_answerDescription = "Swiss"
    def u_success_answerSalt = "DUMMY"
    //Valid test data (For failure tests)
    def u_failure_pinQuestion

    def u_failure_pidm = 1
    def u_failure_number = 1
    def u_failure_questionDescription = "TTTTT"
    def u_failure_answerDescription = null
    def u_failure_answerSalt = null
    /*PROTECTED REGION END*/

    @Before
    public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
        initializeTestDataForReferences()
    }

    //This method is used to initialize test data for references.
    //A method is required to execute database calls as it requires a active transaction
    void initializeTestDataForReferences() {
        i_success_pidm = PersonUtility.getPerson("HOSWEB017").pidm
        u_success_pidm = i_success_pidm
        i_failure_pinQuestion = null
        u_failure_pinQuestion = null
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testCreateValidUserEnteredPinQuestion() {
        def generalForStoringResponsesAndPinQuestion = newValidUserResponsesWithOutPinQuestion()
        generalForStoringResponsesAndPinQuestion.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull generalForStoringResponsesAndPinQuestion.id
        assertEquals i_success_user_entered_questionDescription, generalForStoringResponsesAndPinQuestion.questionDescription
    }

    @Test
    void testCreateValidGeneralForStoringResponsesAndPinQuestion() {
        def generalForStoringResponsesAndPinQuestion = newValidForCreateGeneralForStoringResponsesAndPinQuestion()
        generalForStoringResponsesAndPinQuestion.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull generalForStoringResponsesAndPinQuestion.id
        assertEquals i_success_questionDescription, generalForStoringResponsesAndPinQuestion.questionDescription
    }

    @Test
    void testCreateInvalidGeneralForStoringResponsesAndPinQuestion() {
        def generalForStoringResponsesAndPinQuestion = newInvalidForCreateGeneralForStoringResponsesAndPinQuestion()
        shouldFail(ValidationException) {
            generalForStoringResponsesAndPinQuestion.save( failOnError: true, flush: true )
        }
    }

    @Test
    void testUpdateValidGeneralForStoringResponsesAndPinQuestion() {
        def generalForStoringResponsesAndPinQuestion = newValidForCreateGeneralForStoringResponsesAndPinQuestion()
        generalForStoringResponsesAndPinQuestion.save( failOnError: true, flush: true )
        assertNotNull generalForStoringResponsesAndPinQuestion.id
        assertEquals 0L, generalForStoringResponsesAndPinQuestion.version
        assertEquals i_success_pidm, generalForStoringResponsesAndPinQuestion.pidm
        assertEquals i_success_number, generalForStoringResponsesAndPinQuestion.number
        assertEquals i_success_questionDescription, generalForStoringResponsesAndPinQuestion.questionDescription
        assertEquals i_success_answerDescription, generalForStoringResponsesAndPinQuestion.answerDescription
        assertEquals i_success_answerSalt, generalForStoringResponsesAndPinQuestion.answerSalt

        //Update the entity
        generalForStoringResponsesAndPinQuestion.questionDescription = u_success_questionDescription
        generalForStoringResponsesAndPinQuestion.answerDescription = u_success_answerDescription
        generalForStoringResponsesAndPinQuestion.answerSalt = u_success_answerSalt


        generalForStoringResponsesAndPinQuestion.pinQuestion = u_success_pinQuestion
        generalForStoringResponsesAndPinQuestion.save( failOnError: true, flush: true )
        //Assert for sucessful update
        generalForStoringResponsesAndPinQuestion = GeneralForStoringResponsesAndPinQuestion.get( generalForStoringResponsesAndPinQuestion.id )
        assertEquals 1L, generalForStoringResponsesAndPinQuestion?.version
        assertEquals u_success_questionDescription, generalForStoringResponsesAndPinQuestion.questionDescription
        assertEquals u_success_answerDescription, generalForStoringResponsesAndPinQuestion.answerDescription
        assertEquals u_success_pinQuestion, generalForStoringResponsesAndPinQuestion.pinQuestion
        generalForStoringResponsesAndPinQuestion.pinQuestion = u_success_pinQuestion
    }

    @Test
    void testUpdateInvalidGeneralForStoringResponsesAndPinQuestion() {
        def generalForStoringResponsesAndPinQuestion = newValidForCreateGeneralForStoringResponsesAndPinQuestion()
        generalForStoringResponsesAndPinQuestion.save( failOnError: true, flush: true )
        assertNotNull generalForStoringResponsesAndPinQuestion.id
        assertEquals 0L, generalForStoringResponsesAndPinQuestion.version
        assertEquals i_success_pidm, generalForStoringResponsesAndPinQuestion.pidm
        assertEquals i_success_number, generalForStoringResponsesAndPinQuestion.number
        assertEquals i_success_questionDescription, generalForStoringResponsesAndPinQuestion.questionDescription
        assertEquals i_success_answerDescription, generalForStoringResponsesAndPinQuestion.answerDescription
        assertEquals i_success_answerSalt, generalForStoringResponsesAndPinQuestion.answerSalt

        //Update the entity with invalid values
        generalForStoringResponsesAndPinQuestion.questionDescription = u_failure_questionDescription
        generalForStoringResponsesAndPinQuestion.answerDescription = u_failure_answerDescription
        generalForStoringResponsesAndPinQuestion.answerSalt = u_failure_answerSalt


        generalForStoringResponsesAndPinQuestion.pinQuestion = u_failure_pinQuestion
        shouldFail(ValidationException) {
            generalForStoringResponsesAndPinQuestion.save( failOnError: true, flush: true )
        }
    }

    @Test
    void testDates() {
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = GeneralCommonUtility.getSystemDate()
        def generalForStoringResponsesAndPinQuestion = newValidForCreateGeneralForStoringResponsesAndPinQuestion()



        generalForStoringResponsesAndPinQuestion.save(flush: true, failOnError: true)
        generalForStoringResponsesAndPinQuestion.refresh()
        assertNotNull "GeneralForStoringResponsesAndPinQuestion should have been saved", generalForStoringResponsesAndPinQuestion.id

        // test date values -
        assertEquals date.format(today), date.format(generalForStoringResponsesAndPinQuestion.lastModified)
        assertEquals hour.format(today), hour.format(generalForStoringResponsesAndPinQuestion.lastModified)


    }

    @Test
    void testOptimisticLock() {
        def generalForStoringResponsesAndPinQuestion = newValidForCreateGeneralForStoringResponsesAndPinQuestion()
        generalForStoringResponsesAndPinQuestion.save( failOnError: true, flush: true )

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update GV_GOBANSR set GOBANSR_VERSION = 999 where GOBANSR_SURROGATE_ID = ?", [ generalForStoringResponsesAndPinQuestion.id ] )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        generalForStoringResponsesAndPinQuestion.questionDescription = u_success_questionDescription
        generalForStoringResponsesAndPinQuestion.answerDescription = u_success_answerDescription
        generalForStoringResponsesAndPinQuestion.answerSalt = u_success_answerSalt
        shouldFail( HibernateOptimisticLockingFailureException ) {
            generalForStoringResponsesAndPinQuestion.save( failOnError: true, flush: true )
        }
    }

    @Test
    void testDeleteGeneralForStoringResponsesAndPinQuestion() {
        def generalForStoringResponsesAndPinQuestion = newValidForCreateGeneralForStoringResponsesAndPinQuestion()
        generalForStoringResponsesAndPinQuestion.save( failOnError: true, flush: true )
        def id = generalForStoringResponsesAndPinQuestion.id
        assertNotNull id
        generalForStoringResponsesAndPinQuestion.delete()
        assertNull GeneralForStoringResponsesAndPinQuestion.get( id )
    }

    @Test
    void testValidation() {
        def generalForStoringResponsesAndPinQuestion = newInvalidForCreateGeneralForStoringResponsesAndPinQuestion()
        assertFalse "GeneralForStoringResponsesAndPinQuestion could not be validated as expected due to ${generalForStoringResponsesAndPinQuestion.errors}", generalForStoringResponsesAndPinQuestion.validate()
    }

    @Test
    void testNullValidationFailure() {
        def generalForStoringResponsesAndPinQuestion = new GeneralForStoringResponsesAndPinQuestion()
        assertFalse "GeneralForStoringResponsesAndPinQuestion should have failed validation", generalForStoringResponsesAndPinQuestion.validate()
        assertErrorsFor generalForStoringResponsesAndPinQuestion, 'nullable',
                [
                        'pidm',
                        'number',
                        'answerDescription',
                        'answerSalt'
                ]
        assertNoErrorsFor generalForStoringResponsesAndPinQuestion,
                [
                        'questionDescription',
                        'pinQuestion'
                ]
    }

    @Test
    void testMaxSizeValidationFailures() {
        def generalForStoringResponsesAndPinQuestion = new GeneralForStoringResponsesAndPinQuestion(
                questionDescription:'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX' )
        assertFalse "GeneralForStoringResponsesAndPinQuestion should have failed validation", generalForStoringResponsesAndPinQuestion.validate()
        assertErrorsFor generalForStoringResponsesAndPinQuestion, 'maxSize', [ 'questionDescription' ]
    }

    @Test
    void testAnswerCountForPidm() {
        def generalForStoringResponsesAndPinQuestion = newValidForCreateGeneralForStoringResponsesAndPinQuestion()
        generalForStoringResponsesAndPinQuestion.save( failOnError: true, flush: true )
        assertNotNull generalForStoringResponsesAndPinQuestion.id
        testCreateValidUserEnteredPinQuestion()
        int ansrCount = GeneralForStoringResponsesAndPinQuestion.fetchCountOfAnswersForPidm(generalForStoringResponsesAndPinQuestion.pidm)
        assertTrue ansrCount>1
    }

    @Test
    void testFetchCountOfSameQuestionForPidm() {
        def generalForStoringResponsesAndPinQuestion = newValidUserResponsesWithOutPinQuestion()
        generalForStoringResponsesAndPinQuestion.save( failOnError: true, flush: true )
        assertNotNull generalForStoringResponsesAndPinQuestion.id
        int quesCount = GeneralForStoringResponsesAndPinQuestion.fetchCountOfSameQuestionForPidm([pidm: generalForStoringResponsesAndPinQuestion.pidm,questionDescription: generalForStoringResponsesAndPinQuestion.questionDescription])
        assertTrue quesCount>0
    }

    private def newValidForCreateGeneralForStoringResponsesAndPinQuestion() {
        def pinQuestion = newValidForCreatePinQuestion()
        pinQuestion.save( failOnError: true, flush: true )
        def generalForStoringResponsesAndPinQuestion = new GeneralForStoringResponsesAndPinQuestion(
                pidm: i_success_pidm,
                number: i_success_number,
                questionDescription: i_success_questionDescription,
                answerDescription: i_success_answerDescription,
                answerSalt: i_success_answerSalt,
                pinQuestion: pinQuestion,
        )
        return generalForStoringResponsesAndPinQuestion
    }

    private def newValidUserResponsesWithOutPinQuestion() {
        def generalForStoringResponsesAndPinQuestion = new GeneralForStoringResponsesAndPinQuestion(
                pidm: i_success_pidm,
                number: i_success_number1,
                questionDescription: i_success_user_entered_questionDescription,
                answerDescription: i_success_answerDescription,
                answerSalt: i_success_answerSalt,
                pinQuestion: null,
        )
        return generalForStoringResponsesAndPinQuestion
    }

    private def newValidForCreatePinQuestion() {
        def pinQuestion = new PinQuestion(
                pinQuestionId: "TTT12",
                description: "Favorite drink",
                displayIndicator: true,
        )
        return pinQuestion
    }

    private def newInvalidForCreateGeneralForStoringResponsesAndPinQuestion() {
        def generalForStoringResponsesAndPinQuestion = new GeneralForStoringResponsesAndPinQuestion(
                pidm: i_failure_pidm,
                number: i_failure_number,
                questionDescription: i_success_questionDescription,
                answerDescription: i_failure_answerDescription,
                answerSalt: null,
                pinQuestion: null,
        )
        return generalForStoringResponsesAndPinQuestion
    }

}


/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
/**
 Banner Automator Version: 1.29
 Generated: Thu Aug 01 15:12:58 IST 2013
 */
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Before
import org.junit.Test
import org.junit.After

import grails.validation.ValidationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.general.GeneralCommonUtility
import groovy.sql.Sql
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException
import static groovy.test.GroovyAssert.*
import java.text.SimpleDateFormat


@Integration
@Rollback
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

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']// Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }

    //This method is used to initialize test data for references.
    //A method is required to execute database calls as it requires a active transaction
    void initializeTestDataForReferences() {

    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testCreateValidPinQuestion() {
        def pinQuestion = newValidForCreatePinQuestion()
        pinQuestion.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull pinQuestion.id
    }

    @Test
    void testCreateInvalidPinQuestion() {
        def pinQuestion = newInvalidForCreatePinQuestion()
        shouldFail(ValidationException) {
            pinQuestion.save( failOnError: true, flush: true )
        }
    }

    @Test
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

    @Test
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

    @Test
    void testDates() {
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = GeneralCommonUtility.getSystemDate()

        def pinQuestion = newValidForCreatePinQuestion()



        pinQuestion.save(flush: true, failOnError: true)
        pinQuestion.refresh()
        assertNotNull "PinQuestion should have been saved", pinQuestion.id

        // test date values -
        assertEquals date.format(today), date.format(pinQuestion.lastModified)
        assertEquals hour.format(today), hour.format(pinQuestion.lastModified)


    }

    @Test
    void testOptimisticLock() {
        def pinQuestion = newValidForCreatePinQuestion()
        pinQuestion.save( failOnError: true, flush: true )

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update GV_GOBQSTN set GOBQSTN_VERSION = 999 where GOBQSTN_SURROGATE_ID = ?", [ pinQuestion.id ] )
        } finally {
//            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        pinQuestion.description = u_success_description
        pinQuestion.displayIndicator = u_success_displayIndicator
        shouldFail( HibernateOptimisticLockingFailureException ) {
            pinQuestion.save( failOnError: true, flush: true )
        }
    }

    @Test
    void testDeletePinQuestion() {
        def pinQuestion = newValidForCreatePinQuestion()
        pinQuestion.save( failOnError: true, flush: true )
        def id = pinQuestion.id
        assertNotNull id
        pinQuestion.delete()
        assertNull PinQuestion.get( id )
    }

    @Test
    void testValidation() {
        def pinQuestion = newInvalidForCreatePinQuestion()
        assertFalse "PinQuestion could not be validated as expected due to ${pinQuestion.errors}", pinQuestion.validate()
    }

    @Test
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

    @Test
    void testFetchQuestions() {
        def pinQuestion = newValidForCreatePinQuestion()
        pinQuestion.save( failOnError: true, flush: true )
        assertNotNull pinQuestion.pinQuestionId
        List fetchedPinQuestions = PinQuestion.fetchQuestions()
        assertTrue fetchedPinQuestions.size()>0
    }

    @Test
    void testFetchByQuestionId (){
        def pinQuestion = newValidForCreatePinQuestion()
        pinQuestion.save( failOnError: true, flush: true )
        assertNotNull pinQuestion.pinQuestionId
        String pinQuestionId = pinQuestion.pinQuestionId
        PinQuestion fetchedPinQuestion = PinQuestion.fetchQuestionOnId(pinQuestionId)
        assertNotNull fetchedPinQuestion.pinQuestionId
    }


}

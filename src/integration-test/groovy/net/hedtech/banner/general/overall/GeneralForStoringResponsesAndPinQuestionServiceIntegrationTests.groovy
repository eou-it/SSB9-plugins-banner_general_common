
/*******************************************************************************
 Copyright 2013-2017 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
/**
 Banner Automator Version: 1.29
 Generated: Thu Aug 01 15:13:07 IST 2013
 */
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException

@Integration
@Rollback
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
    def i_success_user_def_question = "TTTTT"


    //Invalid test data (For failure tests)
    def i_failure_pinQuestion
    def i_failure_pidm =  1
    def i_failure_number =  1
    def i_failure_questionDescription = null
    def i_failure_answerDescription = null
    def i_failure_answerSalt = "TTTTT"


    //Test data for creating updating domain instance
    //Valid test data (For success tests)
    def u_success_pidm
    def u_success_number =  2
    def u_success_questionDescription = "Which is ur favourite hang out place?"
    def u_success_answerDescription = "TTTTT"
    def u_success_answerSalt = "DUMMY"


    //Valid test data (For failure tests)
    def u_failure_pinQuestion =  null
    def u_failure_pidm =  1
    def u_failure_questionDescription = null
    def u_failure_answerDescription = null
    def u_failure_answerSalt = "DUMMY"

    @Before
    public void setUp() {
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

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
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

    @Test
    void testGeneralForStoringResponsesAndPinQuestionInvalidCreate() {
        def generalForStoringResponsesAndPinQuestion = newInvalidForCreateGeneralForStoringResponsesAndPinQuestion()
        def map = [domainModel: generalForStoringResponsesAndPinQuestion]
        shouldFail(ApplicationException) {
            generalForStoringResponsesAndPinQuestionService.create(map)
        }
    }

    @Test
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

    @Test
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

    @Test
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


    @Test
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

    @Test
    void testFetchQuestionForPidm() {
        def generalForStoringResponsesAndPinQuestion = newValidForCreateGeneralForStoringResponsesAndPinQuestion()
        generalForStoringResponsesAndPinQuestion.save()
        def result = generalForStoringResponsesAndPinQuestionService.fetchQuestionForPidm(generalForStoringResponsesAndPinQuestion.pidm)[0]

        groovy.util.GroovyTestCase.assertEquals i_success_questionDescription, result.questionDescription
        groovy.util.GroovyTestCase.assertEquals i_success_answerDescription, result.answerDescription
        groovy.util.GroovyTestCase.assertEquals i_success_answerSalt, result.answerSalt
    }

    @Test
    void testFetchCountOfSameQuestionForPidmByIdNone() {
        def qstn = newValidUserResponsesWithOutPinQuestion()
        qstn.save()
        def result = generalForStoringResponsesAndPinQuestionService.fetchCountOfSameQuestionForPidmById(qstn.pidm, qstn.questionDescription, qstn.id as int)

        groovy.util.GroovyTestCase.assertEquals 0, result
    }

    @Test
    void testFetchCountOfSameQuestionForPidmByIdOne() {
        def qstn = newValidUserResponsesWithOutPinQuestion()
        qstn.save()
        def result = generalForStoringResponsesAndPinQuestionService.fetchCountOfSameQuestionForPidmById(qstn.pidm, qstn.questionDescription, qstn.id+1 as int)

        groovy.util.GroovyTestCase.assertEquals 1, result
    }

    private def newValidUserResponsesWithOutPinQuestion() {
        def generalForStoringResponsesAndPinQuestion = new GeneralForStoringResponsesAndPinQuestion(
                pidm: i_success_pidm,
                number: 1,
                questionDescription: i_success_user_def_question,
                answerDescription: i_success_answerDescription,
                answerSalt: "DUMMY",
                pinQuestion: null,
        )
        return generalForStoringResponsesAndPinQuestion
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

/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.personalinformation

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class AnswerSurveyCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def answerSurveyCompositeService

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    @Test
    void testFetchSurveysSuccess() {
        loginSSB('HOSS001', '111111')
        def result = answerSurveyCompositeService.fetchSurveys()
        assertNotNull result
        assertTrue result?.success
        assertEquals('Age Survey', result?.surveys[2].surveyTitle)
        assertEquals('AGE', result?.surveys[2].surveyName)
        assertEquals('1', result?.surveys[2].nextDisp)
        assertEquals('Please respond to the following series of questions.', result?.surveys[2].surveyInfo)
        assertEquals(5, result?.surveys.size())
    }

    @Test
    void testFetchSurveysWithAnyRole() {
        loginSSB('JABS-0011', '111111')
        def result = answerSurveyCompositeService.fetchSurveys()
        assertNotNull result
        assertTrue result?.success
        assertEquals('SITE SURVEY', result?.surveys[0].surveyTitle)
        assertEquals('WEB SURVEY 3', result?.surveys[0].surveyName)
        assertEquals('1', result?.surveys[0].nextDisp)
        assertEquals('', result?.surveys[0].surveyInfo)
    }

    @Test
    void testFetchSurveysNoSurveys() {
        loginSSB('JABS-0001', '111111')
        def result = answerSurveyCompositeService.fetchSurveys()
        assertNotNull result
        assertFalse result?.success
        assertEquals('No surveys are available at this time for your responses.', result?.error)
    }

    @Test
    void testFetchQuestionAnswers() {
        loginSSB('HOSS001', '111111')
        def surveyParams = [
                surveyName: 'REUNION',
                nextDisp  : '1'
        ]
        def resultMap = answerSurveyCompositeService.fetchQuestionAnswers(surveyParams)
        assertNotNull resultMap
        def result = resultMap.questionDetails
        assertEquals('1', result[0].questionNo)
        assertEquals('Do you plan to attend?', result[0].questionText)
        assertEquals('qust1', result[0].questionCode)
        assertEquals('N', result[0].multiResponseInd)
        assertEquals(3, result[0].responseList.size())
        assertNull result[0].radioValue
        assertEquals('rsp11', result[0].responseList[0].name)
        assertEquals('1', result[0].responseList[0].value)
        assertEquals('Yes', result[0].responseList[0].responseText)
        assertEquals('rsp11', result[0].responseList[1].name)
        assertEquals('2', result[0].responseList[1].value)
        assertEquals('No', result[0].responseList[1].responseText)
        assertEquals('N', result[0].allowComments)
    }

    @Test
    void testFetchQuestionAnswersWithComments() {
        loginSSB('HOSS001', '111111')
        def surveyParams = [
                surveyName: 'AGE',
                nextDisp  : '1'
        ]
        def resultMap = answerSurveyCompositeService.fetchQuestionAnswers(surveyParams)
        assertNotNull resultMap
        def result = resultMap.questionDetails
        assertEquals('1', result[0].questionNo)
        assertEquals('What age are you at your earliest memories', result[0].questionText)
        assertEquals('qust1', result[0].questionCode)
        assertEquals('Y', result[0].multiResponseInd)
        assertEquals(4, result[0].responseList.size())
        assertEquals(false, result[0].responseList[0].checked)
        assertEquals('rsp11', result[0].responseList[0].name)
        assertEquals('Y', result[0].responseList[0].value)
        assertEquals('I cant remember my childhood at all', result[0].responseList[0].responseText)
        assertEquals(false, result[0].responseList[1].checked)
        assertEquals('rsp12', result[0].responseList[1].name)
        assertEquals('Y', result[0].responseList[1].value)
        assertEquals('I remember everything from birth on', result[0].responseList[1].responseText)
        assertEquals('Y', result[0].allowComments)
    }

    @Test
    void testFetchQuestionAnswersWithSavedResponse() {
        loginSSB('HOSS001', '111111')
        def surveyParams = [
                surveyName: 'REUNION',
                nextDisp  : '3'
        ]
        def resultMap = answerSurveyCompositeService.fetchQuestionAnswers(surveyParams)
        assertNotNull resultMap
        def result = resultMap.questionDetails
        assertEquals('3', result[0].questionNo)
        assertEquals('What mailing lists do wish to be on?', result[0].questionText)
        assertEquals('qust1', result[0].questionCode)
        assertEquals('Y', result[0].multiResponseInd)
        assertEquals(5, result[0].responseList.size())
        assertEquals(true, result[0].responseList[0].checked)
        assertEquals('rsp11', result[0].responseList[0].name)
        assertEquals('Y', result[0].responseList[0].value)
        assertEquals('Friends of Music and Dance', result[0].responseList[0].responseText)
        assertEquals(true, result[0].responseList[4].checked)
        assertEquals('rsp15', result[0].responseList[4].name)
        assertEquals('Y', result[0].responseList[4].value)
        assertEquals('Friends of Art', result[0].responseList[4].responseText)
        assertEquals('Y', result[0].allowComments)
        assertEquals('cmnt1', result[0].commentName)
        assertEquals('Sample comment for Reunion Survey', result[0].comment)
    }

    @Test
    void testSaveResponsePreviousAction() {
        loginSSB('HOSS001', '111111')

        def surveyParams = """{
                    "surveyName":"REUNION",
                    "questionNo":"3","nextDisp":2,
                    "responses":[{"checked":false,"name":"rsp11","value":"Y","responseText":"Friends of Music and Dance"},
                    {"checked":false,"name":"rsp12","value":"Y","responseText":"Friends of Athletics"},
                    {"checked":false,"name":"rsp13","value":"Y","responseText":"Scott Arboretum"},
                    {"checked":false,"name":"rsp14","value":"Y","responseText":"McCabe Library"},
                    {"checked":true,"name":"rsp15","value":"Y","responseText":"Friends of Art"}],
                    "comment":"Sample comment for Reunion Survey",
                    "submitAction":"Previous"}"""
        def resultMap = answerSurveyCompositeService.saveResponse(surveyParams)
        assertNotNull resultMap
        def result = resultMap.questionDetails
        assertEquals('2', result[0].questionNo)
        assertEquals('Is Swarthmore in your will?', result[0].questionText)
        assertEquals('qust1', result[0].questionCode)
        assertEquals('N', result[0].multiResponseInd)
        assertEquals(2, result[0].responseList.size())
        assertNull result[0].radioValue
        assertEquals('rsp11', result[0].responseList[0].name)
        assertEquals('1', result[0].responseList[0].value)
        assertEquals('Yes', result[0].responseList[0].responseText)
        assertEquals('rsp11', result[0].responseList[1].name)
        assertEquals('2', result[0].responseList[1].value)
        assertEquals('No', result[0].responseList[1].responseText)
        assertEquals('N', result[0].allowComments)
        assertNull result[0].commentName
        assertNull result[0].comment

        surveyParams = [
                surveyName: 'REUNION',
                nextDisp  : '3'
        ]
        resultMap = answerSurveyCompositeService.fetchQuestionAnswers(surveyParams)
        assertNotNull resultMap
        result = resultMap.questionDetails
        assertEquals('3', result[0].questionNo)
        assertEquals('What mailing lists do wish to be on?', result[0].questionText)
        assertEquals('qust1', result[0].questionCode)
        assertEquals('Y', result[0].multiResponseInd)
        assertEquals(5, result[0].responseList.size())
        assertEquals(false, result[0].responseList[0].checked)
        assertEquals('rsp11', result[0].responseList[0].name)
        assertEquals('Y', result[0].responseList[0].value)
        assertEquals('Friends of Music and Dance', result[0].responseList[0].responseText)
    }

    @Test
    void testSaveResponseNextAction() {
        loginSSB('HOSS001', '111111')

        def surveyParams = """{
                    "surveyName":"REUNION",
                    "questionNo":"2","nextDisp":3,
                    "responses":[{"name":"rsp11","value":"1","responseText":"Yes"},
                    {"name":"rsp11","value":"2","responseText":"No"}],
                    "radioValue":"1",
                    "submitAction":"Next"}"""
        def resultMap = answerSurveyCompositeService.saveResponse(surveyParams)
        assertNotNull resultMap
        def result = resultMap.questionDetails
        assertEquals('3', result[0].questionNo)
        assertEquals('What mailing lists do wish to be on?', result[0].questionText)
        assertEquals('qust1', result[0].questionCode)
        assertEquals('Y', result[0].multiResponseInd)
        assertEquals(5, result[0].responseList.size())
        assertEquals(true, result[0].responseList[0].checked)
        assertEquals('rsp11', result[0].responseList[0].name)
        assertEquals('Y', result[0].responseList[0].value)
        assertEquals('Friends of Music and Dance', result[0].responseList[0].responseText)

        surveyParams = [
                surveyName: 'REUNION',
                nextDisp  : '2'
        ]
        resultMap = answerSurveyCompositeService.fetchQuestionAnswers(surveyParams)
        assertNotNull resultMap
        result = resultMap.questionDetails
        assertEquals('2', result[0].questionNo)
        assertEquals('Is Swarthmore in your will?', result[0].questionText)
        assertEquals('qust1', result[0].questionCode)
        assertEquals('N', result[0].multiResponseInd)
        assertEquals(2, result[0].responseList.size())
        assertEquals("1", result[0].radioValue)
        assertEquals('rsp11', result[0].responseList[0].name)
        assertEquals("1", result[0].responseList[0].value)
        assertEquals('Yes', result[0].responseList[0].responseText)
    }

    @Test
    void testSaveResponseReturnToBeginningAction() {
        loginSSB('HOSS001', '111111')

        def surveyParams = """{
                    "surveyName":"REUNION",
                    "questionNo":"2","nextDisp":0,
                    "responses":[{"name":"rsp11","value":"1","responseText":"Yes"},
                    {"name":"rsp11","value":"2","responseText":"No"}],
                    "radioValue":"1",
                    "submitAction":"Return to Beginning of Survey"}"""
        def resultMap = answerSurveyCompositeService.saveResponse(surveyParams)
        assertNotNull resultMap
        def result = resultMap.questionDetails
        assertEquals('1', result[0].questionNo)
        assertEquals('Do you plan to attend?', result[0].questionText)
        assertEquals('qust1', result[0].questionCode)
        assertEquals('N', result[0].multiResponseInd)
        assertEquals(3, result[0].responseList.size())
        assertEquals('rsp11', result[0].responseList[0].name)
        assertEquals('1', result[0].responseList[0].value)
        assertEquals('Yes', result[0].responseList[0].responseText)
        assertEquals('rsp11', result[0].responseList[1].name)
        assertEquals('2', result[0].responseList[1].value)
        assertEquals('No', result[0].responseList[1].responseText)
        assertEquals('N', result[0].allowComments)

        surveyParams = [
                surveyName: 'REUNION',
                nextDisp  : '2'
        ]
        resultMap = answerSurveyCompositeService.fetchQuestionAnswers(surveyParams)
        assertNotNull resultMap
        result = resultMap.questionDetails
        assertEquals('2', result[0].questionNo)
        assertEquals('Is Swarthmore in your will?', result[0].questionText)
        assertEquals('qust1', result[0].questionCode)
        assertEquals('N', result[0].multiResponseInd)
        assertEquals(2, result[0].responseList.size())
        assertEquals('1', result[0].radioValue)
        assertEquals('rsp11', result[0].responseList[0].name)
        assertEquals('1', result[0].responseList[0].value)
        assertEquals('Yes', result[0].responseList[0].responseText)
    }

    @Test
    void testSaveResponseFinishLaterAction() {
        loginSSB('HOSS001', '111111')

        def surveyParams = """{
                    "surveyName":"REUNION",
                    "questionNo":"2","nextDisp":3,
                    "responses":[{"name":"rsp11","value":"1","responseText":"Yes"},
                    {"name":"rsp11","value":"2","responseText":"No"}],
                    "radioValue":"1",
                    "submitAction":"Finish Later"}"""
        def resultMap = answerSurveyCompositeService.saveResponse(surveyParams)
        assertNotNull resultMap
        String message = resultMap.message
        assertEquals('Thank you for beginning the survey, please remember to return and finish the survey.', message)

        surveyParams = [
                surveyName: 'REUNION',
                nextDisp  : '2'
        ]
        resultMap = answerSurveyCompositeService.fetchQuestionAnswers(surveyParams)
        assertNotNull resultMap
        def result = resultMap.questionDetails
        assertEquals('2', result[0].questionNo)
        assertEquals('Is Swarthmore in your will?', result[0].questionText)
        assertEquals('qust1', result[0].questionCode)
        assertEquals('N', result[0].multiResponseInd)
        assertEquals(2, result[0].responseList.size())
        assertEquals('1', result[0].radioValue)
        assertEquals('rsp11', result[0].responseList[0].name)
        assertEquals('1', result[0].responseList[0].value)
        assertEquals('Yes', result[0].responseList[0].responseText)
    }

    @Test
    void testSaveResponseSurveyCompleteAction() {
        loginSSB('HOSS001', '111111')

        def surveyParams = """{
                    "surveyName":"REUNION",
                    "questionNo":"2","nextDisp":3,
                    "responses":[{"name":"rsp11","value":"1","responseText":"Yes"},
                    {"name":"rsp11","value":"2","responseText":"No"}],
                    "radioValue":"1",
                    "submitAction":"Survey Complete"}"""
        def resultMap = answerSurveyCompositeService.saveResponse(surveyParams)
        assertNotNull resultMap
        String message = resultMap.message
        assertEquals('Thank you for completing the survey.', message)

        def result = answerSurveyCompositeService.fetchSurveys()
        assertNotNull result
        assertTrue result?.success
        assertEquals(4, result?.surveys.size())
    }

    @Test
    void testSaveResponseRemoveSurveyAction() {
        loginSSB('HOSS001', '111111')

        def surveyParams = """{
                    "surveyName":"REUNION",
                    "questionNo":"2","nextDisp":3,
                    "responses":[{"checked":true,"name":"rsp11","value":"1","responseText":"Yes"},
                    {"checked":false,"name":"rsp11","value":"2","responseText":"No"}],
                    "submitAction":"Remove Survey from List"}"""
        def resultMap = answerSurveyCompositeService.saveResponse(surveyParams)
        assertNotNull resultMap
        String message = resultMap.message
        assertEquals('The survey has been removed from your list.', message)

        def result = answerSurveyCompositeService.fetchSurveys()
        assertNotNull result
        assertTrue result?.success
        assertEquals(4, result?.surveys.size())
    }
}

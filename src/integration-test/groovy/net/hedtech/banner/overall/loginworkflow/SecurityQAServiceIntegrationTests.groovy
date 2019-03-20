/*******************************************************************************
 Copyright 2013-2014 Ellucian Company L.P. and its affiliates.
 ******************************************************************************/
package net.hedtech.banner.overall.loginworkflow
import org.junit.Before
import org.junit.Test
import org.junit.After

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.GeneralForStoringResponsesAndPinQuestion
import net.hedtech.banner.general.overall.PinQuestion
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase

class SecurityQAServiceIntegrationTests extends BaseIntegrationTestCase{
    def securityQAService
    def i_success_pidm
    def i_success_question_desc1 ="My Success Question"
    def i_user_def_question1 = "My Success User Question"
    def i_user_def_question2 = "My Success User Question2"
    def i_success_answer1 ="My Success Answer"
    def i_failure_answer1 ="My Answer<>"
    def i_failure_question_desc1 ="My test Question<>"
    def u_success_answer1 = "The successful answer"
    def pidm

    @Before
    public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
        initializeTestDataForReferences()
    }

    //This method is used to initialize test data for references.
    //A method is required to execute database calls as it requires a active transaction
    void initializeTestDataForReferences() {
        pidm = PersonUtility.getPerson("HOSWEB017").pidm
        i_success_pidm = pidm.toString()
    }

    @Test
    void testSaveQAResponseWithInvalidPassword(){
        List quesAnsList = [[pidm: i_success_pidm,question: i_success_question_desc1,userDefinedQuestion:i_user_def_question1,answer:i_success_answer1,questionNo:""] ]
        String invalidPassword = "abcdef"
        try{
            securityQAService.saveSecurityQAResponse(i_success_pidm, quesAnsList, invalidPassword)
        }catch(ApplicationException ae){
            assertApplicationException ae, "securityQA.invaild.pin"
        }

    }

    @Test
    void testSaveQAResponseWithInvalidNoOfQuestions(){
        List quesAnsList = [[pidm: i_success_pidm,question: i_success_question_desc1,userDefinedQuestion:i_user_def_question1,answer:i_success_answer1,questionNo:""] ]
        String validPassword = "111111"
        try{
            securityQAService.saveSecurityQAResponse(i_success_pidm, quesAnsList, validPassword)
        }catch(ApplicationException ae){
            assertApplicationException ae, "securityQA.invalid.number.question"
        }
    }

    @Test
    void testSaveQAResponseWithNullQuestions(){
        List quesAnsList = [[pidm: i_success_pidm,question:null,userDefinedQuestion:null,answer:i_success_answer1,questionNo:null] ]
        String validPassword = "111111"
        try{
            securityQAService.saveSecurityQAResponse(i_success_pidm, quesAnsList, validPassword)
        }catch(ApplicationException ae){
            assertApplicationException ae, "securityQA.error"
        }
    }

    @Test
    void testSaveQAResponseWithNullAnswer(){
        List quesAnsList = [[pidm: i_success_pidm,question: i_success_question_desc1,userDefinedQuestion:null,answer:null,questionNo:null] ]
        String validPassword = "111111"
        try{
            securityQAService.saveSecurityQAResponse(i_success_pidm, quesAnsList, validPassword)
        }catch(ApplicationException ae){
            assertApplicationException ae, "securityQA.error"
        }
    }
    @Test
    void testSaveQAResponseWithEmptyQuestions(){
        List quesAnsList = [[pidm: i_success_pidm,question:"",userDefinedQuestion:"",answer:i_success_answer1,questionNo:""] ]
        String validPassword = "111111"
        try{
            securityQAService.saveSecurityQAResponse(i_success_pidm, quesAnsList, validPassword)
        }catch(ApplicationException ae){
            assertApplicationException ae, "securityQA.error"
        }
    }

    @Test
    void testSaveQAResponseWithEmptyAnswer(){
        List quesAnsList = [[pidm: i_success_pidm,question: i_success_question_desc1,userDefinedQuestion:"",answer:"",questionNo:""] ]
        String validPassword = "111111"
        try{
            securityQAService.saveSecurityQAResponse(i_success_pidm, quesAnsList, validPassword)
        }catch(ApplicationException ae){
            assertApplicationException ae, "securityQA.error"
        }
    }

    @Test
    void testSaveQAResponseWithInvalidQuestion(){
        List quesAnsList = [[pidm: i_success_pidm,question: "",userDefinedQuestion:i_failure_question_desc1,answer:"asdasd",questionNo:""] ]
        String validPassword = "111111"
        try{
            securityQAService.saveSecurityQAResponse(i_success_pidm,quesAnsList,validPassword)
        }catch(ApplicationException ae){
            assertApplicationException ae, "securityQA.invalid.question"
        }
    }

    @Test
    void testSaveQAResponseWithInvalidAnswer(){
        List quesAnsList = [[pidm: i_success_pidm,question: "",userDefinedQuestion:i_success_question_desc1,answer:i_failure_answer1,questionNo:""] ]
        String validPassword = "111111"
        try{
            securityQAService.saveSecurityQAResponse(i_success_pidm,quesAnsList,validPassword)
        }catch(ApplicationException ae){
            assertApplicationException ae, "securityQA.invalid.answer"
        }
    }

    @Test
    void testQuestionMinimumLength(){
        def questionMinimumLength = securityQAService.getUserDefinedPreference()?.GUBPPRF_QSTN_MIN_LENGTH
        if(questionMinimumLength>0){
            String generatedQuesString = generateString(questionMinimumLength)
            List quesAnsList = [[pidm: i_success_pidm,question: "",userDefinedQuestion:generatedQuesString,answer:i_success_answer1,questionNo:""]]
            String validPassword = "111111"
            try{
                securityQAService.saveSecurityQAResponse(i_success_pidm,quesAnsList,validPassword)
            }catch(ApplicationException ae){
                assertApplicationException ae, "securityQA.invalid.length.question"
            }
        }
    }

    @Test
    void testAnswerMinimumLength(){
        def length = securityQAService.getUserDefinedPreference()?.GUBPPRF_ANSR_MIN_LENGTH
        if(length>0){
            String generatedAnsString = generateString(length)
            List quesAnsList = [[pidm: i_success_pidm,question:"",userDefinedQuestion:i_user_def_question1,answer:generatedAnsString,questionNo:""] ]
            String validPassword = "111111"
            try{
                securityQAService.saveSecurityQAResponse(i_success_pidm,quesAnsList,validPassword)
            }catch(ApplicationException ae){
                assertApplicationException ae, "securityQA.invalid.length.answer"
            }
        }
    }

    @Test
    void testUniqueQuestion(){
        def generalForStoringResponsesAndPinQuestion = newValidUserResponsesWithOutPinQuestion()
        generalForStoringResponsesAndPinQuestion.save( failOnError: true, flush: true )
        assertNotNull generalForStoringResponsesAndPinQuestion.id
        int quesCount = GeneralForStoringResponsesAndPinQuestion.fetchCountOfSameQuestionForPidm([pidm: generalForStoringResponsesAndPinQuestion.pidm,questionDescription: generalForStoringResponsesAndPinQuestion.questionDescription])
        assertTrue quesCount>0
        List quesAnsList = [[pidm: i_success_pidm,question:"" ,userDefinedQuestion:i_user_def_question2,answer:i_success_answer1,questionNo:""] ]
        String validPassword = "111111"
        try{
            securityQAService.saveSecurityQAResponse(i_success_pidm,quesAnsList,validPassword)
        }catch(ApplicationException ae){
            assertApplicationException ae, "securityQA.unique.question"
        }

    }

    @Test
    void testVaildSaveSingleQAResponse()
    {
        def pinQuestion  = newValidForCreatePinQuestion("TT12")
        pinQuestion.save( failOnError: true, flush: true )
        assertNotNull pinQuestion.id
        String validPassword = "111111"
        setNumberOfQuestion(1)
        assertEquals 1, securityQAService.getUserDefinedPreference()?.GUBPPRF_NO_OF_QSTNS.toInteger()
        List quesAnsList = [[pidm: i_success_pidm,question: i_success_question_desc1,userDefinedQuestion:"",answer:i_success_answer1,questionNo:pinQuestion.id]]
        securityQAService.saveSecurityQAResponse(i_success_pidm,quesAnsList,validPassword)
        int ansrCount = GeneralForStoringResponsesAndPinQuestion.fetchCountOfAnswersForPidm(pidm)
        assertTrue ansrCount==1

    }

    @Test
    void testVaildSaveSingleUserDefinedQAResponse()
    {
        String validPassword = "111111"
        setNumberOfQuestion(1)
        assertEquals 1, securityQAService.getUserDefinedPreference()?.GUBPPRF_NO_OF_QSTNS.toInteger()
        List quesAnsList = [[pidm: i_success_pidm,question:"",userDefinedQuestion:i_user_def_question1,answer:i_success_answer1,questionNo:""]]
        securityQAService.saveSecurityQAResponse(i_success_pidm,quesAnsList,validPassword)
        int ansrCount = GeneralForStoringResponsesAndPinQuestion.fetchCountOfAnswersForPidm(pidm)
        assertTrue ansrCount==1

    }

    @Test
    void testValidSaveSingleQAResponseWithUserDefinedAsNull()
    {
        def pinQuestion  = newValidForCreatePinQuestion("TT12")
        pinQuestion.save( failOnError: true, flush: true )
        assertNotNull pinQuestion.id
        String validPassword = "111111"
        setNumberOfQuestion(1)
        assertEquals 1, securityQAService.getUserDefinedPreference()?.GUBPPRF_NO_OF_QSTNS.toInteger()
        List quesAnsList = [[pidm: i_success_pidm,question: i_success_question_desc1,userDefinedQuestion:null,answer:i_success_answer1,questionNo:pinQuestion.id]]
        securityQAService.saveSecurityQAResponse(i_success_pidm,quesAnsList,validPassword)
        int ansrCount = GeneralForStoringResponsesAndPinQuestion.fetchCountOfAnswersForPidm(pidm)
        assertTrue ansrCount==1

    }

    @Test
    void testVaildSaveSecurityQAResponse()
    {
        def pinQuestion  = newValidForCreatePinQuestion("TT12")
        pinQuestion.save( failOnError: true, flush: true )
        assertNotNull pinQuestion.id
        String validPassword = "111111"
        setNumberOfQuestion(3)
        assertEquals 3, securityQAService.getUserDefinedPreference()?.GUBPPRF_NO_OF_QSTNS.toInteger()
        List quesAnsList = [[pidm: i_success_pidm,question: i_success_question_desc1,userDefinedQuestion:"",answer:i_success_answer1,questionNo:pinQuestion.id],
                            [pidm: i_success_pidm,question: "",userDefinedQuestion:i_user_def_question2,answer:i_success_answer1,questionNo:""],
                            [pidm: i_success_pidm,question: "",userDefinedQuestion:i_user_def_question1,answer:i_success_answer1,questionNo:""] ]
        securityQAService.saveSecurityQAResponse(i_success_pidm,quesAnsList,validPassword)
        int ansrCount = GeneralForStoringResponsesAndPinQuestion.fetchCountOfAnswersForPidm(pidm)
        assertTrue ansrCount==3

    }

    @Test
    void testValidSaveUpdateSecurityQAResponseSave()
    {
        def pinQuestion  = newValidForCreatePinQuestion("TT12")
        pinQuestion.save( failOnError: true, flush: true )
        assertNotNull pinQuestion.id
        String validPassword = "111111"
        setNumberOfQuestion(3)
        assertEquals 3, securityQAService.getUserDefinedPreference()?.GUBPPRF_NO_OF_QSTNS.toInteger()
        List quesAnsList = [[pidm: i_success_pidm,question: i_success_question_desc1,userDefinedQuestion:"",answer:i_success_answer1,questionNo:pinQuestion.id],
                            [pidm: i_success_pidm,question: "",userDefinedQuestion:i_user_def_question2,answer:i_success_answer1,questionNo:""],
                            [pidm: i_success_pidm,question: "",userDefinedQuestion:i_user_def_question1,answer:i_success_answer1,questionNo:""] ]
        securityQAService.saveOrUpdateSecurityQAResponse(i_success_pidm,quesAnsList,validPassword)
        int ansrCount = GeneralForStoringResponsesAndPinQuestion.fetchCountOfAnswersForPidm(pidm)
        assertTrue ansrCount==3
    }

    @Test
    void testValidSaveUpdateSecurityQAResponseUpdate()
    {
        def pinQuestion  = newValidForCreatePinQuestion("TT12")
        pinQuestion.save( failOnError: true, flush: true )
        assertNotNull pinQuestion.id

        def generalForStoringResponsesAndPinQuestion = newValidUserResponsesWithOutPinQuestion()
        generalForStoringResponsesAndPinQuestion.save( failOnError: true, flush: true )
        assertNotNull generalForStoringResponsesAndPinQuestion.id
        assertNotNull generalForStoringResponsesAndPinQuestion.version
        assertEquals i_success_answer1, generalForStoringResponsesAndPinQuestion.answerDescription
        def u_success_id = generalForStoringResponsesAndPinQuestion.id
        def u_success_version = generalForStoringResponsesAndPinQuestion.version

        String validPassword = "111111"
        setNumberOfQuestion(3)
        assertEquals 3, securityQAService.getUserDefinedPreference()?.GUBPPRF_NO_OF_QSTNS.toInteger()
        List quesAnsList = [[pidm: i_success_pidm,question: "",userDefinedQuestion:i_user_def_question2,answer:u_success_answer1,questionNo:"",id:u_success_id,version:u_success_version],
                            [pidm: i_success_pidm,question: i_success_question_desc1,userDefinedQuestion:"",answer:i_success_answer1,questionNo:pinQuestion.id],
                            [pidm: i_success_pidm,question: "",userDefinedQuestion:i_user_def_question1,answer:i_success_answer1,questionNo:""]]
        securityQAService.saveOrUpdateSecurityQAResponse(i_success_pidm,quesAnsList,validPassword)
        int ansrCount = GeneralForStoringResponsesAndPinQuestion.fetchCountOfAnswersForPidm(pidm)
        assertTrue ansrCount==3
    }

    @Test
    void testInvalidSaveUpdateSecurityQAResponseUpdate()
    {
        def pinQuestion  = newValidForCreatePinQuestion("TT12")
        pinQuestion.save( failOnError: true, flush: true )
        assertNotNull pinQuestion.id

        def generalForStoringResponsesAndPinQuestion = newValidUserResponsesWithOutPinQuestion()
        generalForStoringResponsesAndPinQuestion.save( failOnError: true, flush: true )
        assertNotNull generalForStoringResponsesAndPinQuestion.id
        assertNotNull generalForStoringResponsesAndPinQuestion.version
        assertEquals i_success_answer1, generalForStoringResponsesAndPinQuestion.answerDescription
        def u_success_id = generalForStoringResponsesAndPinQuestion.id
        def u_success_version = generalForStoringResponsesAndPinQuestion.version

        String validPassword = "111111"
        setNumberOfQuestion(3)
        assertEquals 3, securityQAService.getUserDefinedPreference()?.GUBPPRF_NO_OF_QSTNS.toInteger()
        List quesAnsList = [[pidm: i_success_pidm, question: "", userDefinedQuestion:i_user_def_question2, answer:u_success_answer1, questionNo:"", id:u_success_id,version:u_success_version],
                            [pidm: i_success_pidm, question: "", userDefinedQuestion:i_user_def_question2, answer:i_success_answer1, questionNo:""],
                            [pidm: i_success_pidm, question: i_success_question_desc1, userDefinedQuestion:"", answer:i_success_answer1, questionNo:pinQuestion.id]]

        def exceptionMessage = shouldFail(ApplicationException) {
            securityQAService.saveOrUpdateSecurityQAResponse(i_success_pidm, quesAnsList, validPassword)
        }
        assertEquals 'securityQA.unique.question', exceptionMessage
        int ansrCount = GeneralForStoringResponsesAndPinQuestion.fetchCountOfAnswersForPidm(pidm)
        assertTrue ansrCount==1
    }

    @Test
    void testGetNumberOfQuestions()
    {
        def numberOfQues =  securityQAService.getUserDefinedPreference()?.GUBPPRF_NO_OF_QSTNS
        assertNotNull numberOfQues
    }

    @Test
    void testGetUserDefinedQuestionFlag()
    {
        def userDefinedFlag = securityQAService.getUserDefinedPreference()?.GUBPPRF_EDITQSTN_IND
        assertNotNull userDefinedFlag
    }

    @Test
    void testGetNumberOfQuestionsAnswered()
    {
        def generalForStoringResponsesAndPinQuestion = newValidUserResponsesWithOutPinQuestion()
        generalForStoringResponsesAndPinQuestion.save( failOnError: true, flush: true )
        assertNotNull generalForStoringResponsesAndPinQuestion.id
        def count =  securityQAService.getNumberOfQuestionsAnswered(pidm)
        assertTrue count>0
    }

    private def generateString(def length)
    {
        String generatedString = ""
        while ( length-- > 1 ) {
            generatedString = generatedString + "T"
        }
        return generatedString
    }

    private def newValidForCreatePinQuestion(String pinQuestionId) {
        def pinQuestion = new PinQuestion(
                pinQuestionId: pinQuestionId,
                description: "What is your Favorite drink1?",
                displayIndicator: true,
        )
        return pinQuestion
    }

    private def newValidUserResponsesWithOutPinQuestion() {
        def generalForStoringResponsesAndPinQuestion = new GeneralForStoringResponsesAndPinQuestion(
                pidm: i_success_pidm,
                number: 1,
                questionDescription: i_user_def_question2,
                answerDescription: i_success_answer1,
                answerSalt: "DUMMY",
                pinQuestion: null,
        )
        return generalForStoringResponsesAndPinQuestion
    }

    private void setNumberOfQuestion(int noOfQuestions){
        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GUBPPRF set GUBPPRF_NO_OF_QSTNS = ?",[noOfQuestions])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
    }


}

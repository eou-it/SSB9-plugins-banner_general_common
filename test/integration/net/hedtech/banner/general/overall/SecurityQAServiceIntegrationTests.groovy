/** *****************************************************************************
 © 2013 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase

/**
 * SecurityQAServiceIntegrationTests.
 *
 * Date: 9/6/13
 * Time: 12:45 PM
 */
class SecurityQAServiceIntegrationTests extends BaseIntegrationTestCase{
    def securityQAService
    def i_success_pidm
    def i_success_question_desc1 ="My Success Question"
    def i_user_def_question1 = "My Success User Question"
    def i_user_def_question2 = "My Success User Question2"
    def i_success_answer1 ="My Success Answer"
    def i_failure_answer1 ="My Answer<>"
    def i_failure_question_desc1 ="My test Question<>"
    def pidm

    protected void setUp() {
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

    void testSaveQAResponseWithInvalidPassword(){
        List quesAnsList = [[pidm: i_success_pidm,question: i_success_question_desc1,userDefinedQuestion:i_user_def_question1,answer:i_success_answer1,questionNo:""] ]
        String invalidPassword = "abcdef"
        try{
            securityQAService.saveSecurityQAResponse(i_success_pidm, quesAnsList, invalidPassword)
        }catch(ApplicationException ae){
            assertApplicationException ae, "securityQA.invaild.pin"
        }

    }

    void testSaveQAResponseWithInvalidNoOfQuestions(){
        List quesAnsList = [[pidm: i_success_pidm,question: i_success_question_desc1,userDefinedQuestion:i_user_def_question1,answer:i_success_answer1,questionNo:""] ]
        String validPassword = "111111"
        try{
            securityQAService.saveSecurityQAResponse(i_success_pidm, quesAnsList, validPassword)
        }catch(ApplicationException ae){
            assertApplicationException ae, "securityQA.invalid.number.question"
        }
    }

    void testSaveQAResponseWithEmptyQuestions(){
        List quesAnsList = [[pidm: i_success_pidm,question:"",userDefinedQuestion:"",answer:i_success_answer1,questionNo:""] ]
        String validPassword = "111111"
        try{
            securityQAService.saveSecurityQAResponse(i_success_pidm, quesAnsList, validPassword)
        }catch(ApplicationException ae){
            assertApplicationException ae, "securityQA.error"
        }
    }

    void testSaveQAResponseWithEmptyAnswer(){
        List quesAnsList = [[pidm: i_success_pidm,question: i_success_question_desc1,userDefinedQuestion:"",answer:"",questionNo:""] ]
        String validPassword = "111111"
        try{
            securityQAService.saveSecurityQAResponse(i_success_pidm, quesAnsList, validPassword)
        }catch(ApplicationException ae){
            assertApplicationException ae, "securityQA.error"
        }
    }

    void testSaveQAResponseWithInvalidQuestion(){
        List quesAnsList = [[pidm: i_success_pidm,question: "",userDefinedQuestion:i_failure_question_desc1,answer:"asdasd",questionNo:""] ]
        String validPassword = "111111"
        try{
            securityQAService.saveSecurityQAResponse(i_success_pidm,quesAnsList,validPassword)
        }catch(ApplicationException ae){
            assertApplicationException ae, "securityQA.invalid.question"
        }
    }

    void testSaveQAResponseWithInvalidAnswer(){
        List quesAnsList = [[pidm: i_success_pidm,question: "",userDefinedQuestion:i_success_question_desc1,answer:i_failure_answer1,questionNo:""] ]
        String validPassword = "111111"
        try{
            securityQAService.saveSecurityQAResponse(i_success_pidm,quesAnsList,validPassword)
        }catch(ApplicationException ae){
            assertApplicationException ae, "securityQA.invalid.answer"
        }
    }

    void testQuestionMinimumLength(){
        def questionMinimumLength = securityQAService.getQuestionMinimumLength()
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

    void testAnswerMinimumLength(){
        def length = securityQAService.getAnswerMinimumLength()
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

    void testVaildSaveSingleQAResponse()
    {
        def pinQuestion  = newValidForCreatePinQuestion("TT12")
        pinQuestion.save( failOnError: true, flush: true )
        assertNotNull pinQuestion.id
        String validPassword = "111111"
        setNumberOfQuestion(1)
        assertEquals 1, securityQAService.getNumberOfQuestions()
        List quesAnsList = [[pidm: i_success_pidm,question: i_success_question_desc1,userDefinedQuestion:"",answer:i_success_answer1,questionNo:pinQuestion.id]]
        securityQAService.saveSecurityQAResponse(i_success_pidm,quesAnsList,validPassword)
        int ansrCount = GeneralForStoringResponsesAndPinQuestion.fetchCountOfAnswersForPidm(pidm)
        assertTrue ansrCount==1

    }

    void testVaildSaveSingleUserDefinedQAResponse()
    {
        String validPassword = "111111"
        setNumberOfQuestion(1)
        assertEquals 1, securityQAService.getNumberOfQuestions()
        List quesAnsList = [[pidm: i_success_pidm,question:"",userDefinedQuestion:i_user_def_question1,answer:i_success_answer1,questionNo:""]]
        securityQAService.saveSecurityQAResponse(i_success_pidm,quesAnsList,validPassword)
        int ansrCount = GeneralForStoringResponsesAndPinQuestion.fetchCountOfAnswersForPidm(pidm)
        assertTrue ansrCount==1

    }

    void testVaildSaveSecurityQAResponse()
    {
        def pinQuestion  = newValidForCreatePinQuestion("TT12")
        pinQuestion.save( failOnError: true, flush: true )
        assertNotNull pinQuestion.id
        String validPassword = "111111"
        setNumberOfQuestion(3)
        assertEquals 3, securityQAService.getNumberOfQuestions()
        List quesAnsList = [[pidm: i_success_pidm,question: i_success_question_desc1,userDefinedQuestion:"",answer:i_success_answer1,questionNo:pinQuestion.id],
                                 [pidm: i_success_pidm,question: "",userDefinedQuestion:i_user_def_question2,answer:i_success_answer1,questionNo:""],
                                 [pidm: i_success_pidm,question: "",userDefinedQuestion:i_user_def_question1,answer:i_success_answer1,questionNo:""] ]
        securityQAService.saveSecurityQAResponse(i_success_pidm,quesAnsList,validPassword)
        int ansrCount = GeneralForStoringResponsesAndPinQuestion.fetchCountOfAnswersForPidm(pidm)
        assertTrue ansrCount==3

    }

    void testGetNumberOfQuestions()
    {
        def numberOfQues =  securityQAService.getNumberOfQuestions()
        assertNotNull numberOfQues
    }

    void testGetUserDefinedQuestionFlag()
    {
        def userDefinedFlag = securityQAService.getUserDefinedQuestionFlag()
        assertNotNull userDefinedFlag
    }

    void testGetNumberOfQuestionsAnswered()
    {
        def generalForStoringResponsesAndPinQuestion = newValidUserResponsesWithOutPinQuestion()
        generalForStoringResponsesAndPinQuestion.save( failOnError: true, flush: true )
        assertNotNull generalForStoringResponsesAndPinQuestion.id
        def count =  securityQAService.getNumberOfQuestionsAnswered(i_success_pidm)
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

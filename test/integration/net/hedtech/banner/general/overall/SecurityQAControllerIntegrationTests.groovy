package net.hedtech.banner.general.overall

import groovy.sql.Sql
import net.hedtech.banner.loginworkflow.SecurityQAController
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken as UPAT
import org.springframework.security.core.context.SecurityContextHolder
/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
class SecurityQAControllerIntegrationTests extends BaseIntegrationTestCase {

    def selfServiceBannerAuthenticationProvider
    def securityQAService
    def noOfQuestions
    def questions = [:]
    List dataToView = []
    def questionMinimumLength
    def answerMinimumLength
    def userDefinedQuesFlag
    def questionList = []
    def selectedQues = []
    def i_user_question_id ="question0"
    def i_user_question = "My First school"
    def i_question1 = "Fav destination?"
    def i_question2 = "Fav food?"
    def pidm = '400720'
    protected void setUp() {

        // For testing RESTful APIs, we don't want the default 'controller support' added by our base class.
        // Most importantly, we don't want to redefine the controller's params to be a map within this test,
        // as we need Grails to automatically populate the params from the request.
        //
        // So, we'll set the formContext and then call super(), just as if this were not a controller test.
        // That is, we'll set the controller after we call super() so the base class won't manipulate it.
        if (!isSsbEnabled()) return
        formContext = ['GUAGMNU']

        controller = new SecurityQAController()

        super.setUp()
        ServletContextHolder.servletContext.removeAttribute("gtvsdax")
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UPAT('HOF00720', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
    }


    protected void tearDown() {
        if (!isSsbEnabled()) return
        super.tearDown()
        logout()
    }

    void testRetrieveDate() {
        if (!isSsbEnabled()) return
        controller.index()
        assertEquals controller.response.status, 200
        def securityQAData = controller?.response?.contentAsString
        assertNotNull securityQAData
        def ques = PinQuestion.fetchQuestions()
        userDefinedQuesFlag = securityQAService.getUserDefinedQuestionFlag()
        ques.each {
            questions.put(it.pinQuestionId, it.description)
        }
        def questionList = questions.values().collect()
        noOfQuestions = securityQAService.getNumberOfQuestions()
        questionMinimumLength = securityQAService.getQuestionMinimumLength()
        answerMinimumLength = securityQAService.getAnswerMinimumLength()
        assertTrue !controller?.response?.contentAsString?.equals("[]")
        def fields = renderMap.model
        //assertEquals fields.noOfQuestions, noOfQuestions
        assertEquals fields.questionMinimumLength, questionMinimumLength
        assertEquals fields.answerMinimumLength, answerMinimumLength
        assertEquals fields.questions.size(), ques.size()
    }

    void testSave(){
        if (!isSsbEnabled()) return
        def pinQuestion1  = newValidForCreatePinQuestion("TT11" ,i_question1 )
        pinQuestion1.save( failOnError: true, flush: true )
        assertNotNull pinQuestion1.id

        def pinQuestion2  = newValidForCreatePinQuestion("TT12",i_question2)
        pinQuestion2.save( failOnError: true, flush: true )
        assertNotNull pinQuestion2.id

        List questionList = []
        def ques = PinQuestion.fetchQuestions()
        Map questions = [:]
        ques.each {
            questions.put(it.pinQuestionId, it.description)
        }
        questionList = questions.values().collect()
        setNumberOfQuestion(3)
        assertEquals 3, securityQAService.getNumberOfQuestions()

        int question1Index = questionList.indexOf(pinQuestion1.getDescription())+1
        int question2Index = questionList.indexOf(pinQuestion2.getDescription()) +1
        String question1 = "question"+question1Index
        String question2 = "question"+question2Index

        controller.params.question = [question1,question2,"question0"]
        controller.params.userDefinedQuestion = ['','',i_user_question]
        controller.params.answer=["answer1","answer2","answer3"]
        controller.params.pin='111111'

        controller.save()
        assertEquals controller.response.status, 200
        def securityQAData = controller?.response?.contentAsString
        int ansrCount = securityQAService.getNumberOfQuestionsAnswered(pidm)
        assertEquals 3, ansrCount
        assertNotNull securityQAData

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

    private def newValidForCreatePinQuestion(String pinQuestionId,String desc) {
   		def pinQuestion = new PinQuestion(
   			pinQuestionId: pinQuestionId,
   			description: desc,
   			displayIndicator: true,
   		)
   		return pinQuestion
   	}

    private def isSsbEnabled() {
        ConfigurationHolder.config.ssbEnabled instanceof Boolean ? ConfigurationHolder.config.ssbEnabled : false
    }


}

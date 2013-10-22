/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.loginworkflow

import net.hedtech.banner.configuration.HttpRequestUtils
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.PinQuestion
import net.hedtech.banner.security.BannerGrantedAuthorityService


class SecurityQAController {

    static defaultAction = "index"
    def securityQAService

    int noOfQuestions
    Map questions = [:]
    int questionMinimumLength
    int answerMinimumLength
    String userDefinedQuesFlag
    String securityQAInfo
    List questionList = []
    private static final QUESTION_LABEL = "question"
    public static final SECURITY_QA_ACTION = "securityqadone"
    public static final ACTION_DONE = "true"
    private static final INVALID_ANSWER_LENGTH_ERROR_KEY = "securityQA.invalid.length.answer"
    private static final INVALID_QUESTION_LENGTH_ERROR_KEY = "securityQA.invalid.length.question"
    private static final USER_DEFINED_QUESTION_DISABLED = "N"

    def index() {
        setGlobalVariables()
        render view: "securityQA", model: [questions: questionList, userDefinedQuesFlag: userDefinedQuesFlag, noOfquestions: noOfQuestions, questionMinimumLength: questionMinimumLength, answerMinimumLength: answerMinimumLength, selectedQues: "", selectedAns: [], selectedUserDefinedQues: [],securityQAInfo: securityQAInfo]
    }

    private void setGlobalVariables() {

        questionList = loadQuestionList()
        Map result = securityQAService.getUserDefinedPreference()
        securityQAInfo = securityQAService.getDefaultQuestionInfoText()
        if (result != null) {
            noOfQuestions = result.GUBPPRF_NO_OF_QSTNS?.intValue()
            questionMinimumLength = result.GUBPPRF_QSTN_MIN_LENGTH?.intValue()
            answerMinimumLength = result.GUBPPRF_ANSR_MIN_LENGTH?.intValue()
            userDefinedQuesFlag = result.GUBPPRF_EDITQSTN_IND
        }

    }

    private List loadQuestionList() {
        List ques = PinQuestion.fetchQuestions()
        ques.each {
            questions.put(it.description, it.pinQuestionId)
        }
        questions.keySet().collect()
    }

    def save() {

        log.info("save")

        setGlobalVariables()

        String pidm = BannerGrantedAuthorityService.getPidm()
        List selectedQA = loadSelectedQuestionAnswerFromParams()
        String messages = null

        try {
            securityQAService.saveSecurityQAResponse(pidm, selectedQA, params.pin)
        } catch (ApplicationException ae) {

            messages = getErrorMessage(ae.wrappedException.message)

            def selectedDropdown = []
            selectedQA.each {
                selectedDropdown.add(QUESTION_LABEL + (questionList.indexOf(it.question) + 1))
            }
            Map model = [:]

            model.put("questions", questionList)
            model.put("userDefinedQuesFlag", userDefinedQuesFlag)
            model.put("noOfquestions", noOfQuestions)
            model.put("questionMinimumLength", questionMinimumLength)
            model.put("answerMinimumLength", answerMinimumLength)
            model.put("dataToView", selectedQA)
            model.put("notification", messages)
            model.put("selectedQues", selectedDropdown)
            model.put("selectedAns", selectedQA.answer)
            model.put("selectedUserDefinedQues", selectedQA.userDefinedQuestion)
            model.put("securityQAInfo",securityQAInfo)

            render view: "securityQA", model: model
        }
        if (messages == null) {
            done()
        }

    }

    private List loadSelectedQuestionAnswerFromParams() {
        List selectedQA = []
        def question = params.question
        def userDefinedQstn = params.userDefinedQuestion
        def answer = params.answer

        def questionsAnswered
        if (noOfQuestions == 1) {
            questionsAnswered = getAnsweredQuestions(question, userDefinedQstn, answer)
            selectedQA.add(questionsAnswered)
        }
        else {
            for (int index = 0; index < noOfQuestions; index++) {

                def userDefQsn
                if (USER_DEFINED_QUESTION_DISABLED.equals(userDefinedQuesFlag) ) {
                    userDefQsn = null
                }
                else {
                    userDefQsn = userDefinedQstn[index]
                }
                questionsAnswered = getAnsweredQuestions(question[index], userDefQsn, answer[index])
                selectedQA.add(questionsAnswered)
            }
        }
        return selectedQA
    }

    private Map getAnsweredQuestions(questionlabel, userDefinedQstn, answer) {
        def question = null
        def questionNo = null
        int questionId = questionlabel.split(QUESTION_LABEL)[1].toInteger()

        if (questionId != 0) {
            question = questionList.get(questionId - 1)
            questionNo = questions.find {it.key == question}?.value
        }
        return [question: question, questionNo: questionNo, userDefinedQuestion: userDefinedQstn, answer: answer]
    }



    private def getErrorMessage(msg) {
        String message = message(code: msg)

        if (message.contains("{0}")) {
            if (msg.equals(INVALID_QUESTION_LENGTH_ERROR_KEY)) {
                message = message.replace("{0}", questionMinimumLength.toString())
            } else if (msg.equals(INVALID_ANSWER_LENGTH_ERROR_KEY)) {
                message = message.replace("{0}", answerMinimumLength.toString())
            }
        }

        return message
    }


    def done() {
        request.getSession().setAttribute(SECURITY_QA_ACTION, ACTION_DONE)
        String path = request.getSession().getAttribute(PostLoginWorkflow.URI_ACCESSED)
        if (path == null) {
            path = "/"
        } else {
            path = checkPath(path)
        }
        request.getSession().setAttribute(PostLoginWorkflow.URI_REDIRECTED, path)
        redirect uri: path
    }

    protected String checkPath(String path) {
        String controllerName = HttpRequestUtils.getControllerNameFromPath(path)
        List<PostLoginWorkflow> listOfFlows = PostLoginWorkflow.getListOfFlows()
        for (PostLoginWorkflow flow : listOfFlows) {
            if (flow.getControllerName().equals(controllerName)) {
                path = "/"
                return path
            }
        }
        return path
    }
}

/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.loginworkflow

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.PinQuestion

class SecurityQAController {

    static defaultAction = "index"
    def securityQAService

    int noOfQuestions
    Map questions = [:]
    int questionMinimumLength
    int answerMinimumLength
    String userDefinedQuesFlag
    List questionList = []

    def index() {
        setGlobalVariables()
        render view: "securityQA", model: [questions: questionList, userDefinedQuesFlag: userDefinedQuesFlag, noOfquestions: noOfQuestions, questionMinimumLength: questionMinimumLength, answerMinimumLength: answerMinimumLength,selectedQues: "", selectedAns: [], selectedUserDefinedQues: []]
    }

    private void setGlobalVariables() {

        questionList = loadQuestionList()
        Map result = securityQAService.getUserDefinedPreference()
        if(result != null) {
            noOfQuestions = result.GUBPPRF_NO_OF_QSTNS?.intValue()
            questionMinimumLength = result.GUBPPRF_QSTN_MIN_LENGTH?.intValue()
            answerMinimumLength = result.GUBPPRF_ANSR_MIN_LENGTH?.intValue()
            userDefinedQuesFlag = result.GUBPPRF_EDITQSTN_IND
        }

    }

    private List loadQuestionList() {
        List ques = PinQuestion.fetchQuestions()
        ques.each {
            questions.put(it.pinQuestionId, it.description)
        }
        questions.values().collect()
    }

    def save() {

        log.info("save")

        setGlobalVariables()

        String pidm = securityQAService.getPidm()
        List selectedQA = loadSelectedQuestionAnswerFromParams()
        String messages = null

        try {
            securityQAService.saveSecurityQAResponse(pidm, selectedQA, params.pin)
        } catch (ApplicationException ae) {

            messages = getErrorMessage(ae.wrappedException.message)

            def selectedDropdown = []
            selectedQA.each {
                selectedDropdown.add("question" + (questionList.indexOf(it.question) + 1))
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


            render view: "securityQA", model: model
        }
        if (messages == null) {
            completed()
        }

    }

    private List loadSelectedQuestionAnswerFromParams() {
        List selectedQA = []

        for (int index = 0; index < noOfQuestions; index++) {
            int questionId
            if(params.question instanceof String) {
                questionId = params.question.split("question")[1].toInteger()
            } else {
                questionId = params.question[index].split("question")[1].toInteger()
            }

            String question
            String questionNo
            if (questionId != 0) {
                question = questionList.get(questionId - 1)
                questionNo = questions.find {it.value == question}?.key
            }
            else {
                question = null
                questionNo = null
            }
            String userDefinedQstn
            if(!userDefinedQuesFlag.equals("N")) {
                if(params.userDefinedQuestion instanceof String) {
                    userDefinedQstn = params.userDefinedQuestion
                } else {
                    userDefinedQstn = params.userDefinedQuestion[index]
                }
            } else {
                userDefinedQstn = null
            }

            String answer
            if(params.answer instanceof String) {
                answer = params.answer
            } else {
                answer = params.answer[index]
            }
            def questionsAnswered = [question: question, questionNo: questionNo, userDefinedQuestion: userDefinedQstn, answer: answer]
            selectedQA.add(questionsAnswered)
        }
        return selectedQA
    }

    private def getErrorMessage(msg) {
        String message = message(code: msg)

        if (message.contains("{0}")) {
            if (msg.equals("securityQA.invalid.length.question")) {
                message = message.replace("{0}", questionMinimumLength.toString())
            } else if (msg.equals("securityQA.invalid.length.answer")) {
                message = message.replace("{0}", answerMinimumLength.toString())
            }
        }

        return message
    }


    def completed() {
        request.getSession().setAttribute("securityqadone", "true")
        done()
    }

    def done() {
        String path = request.getSession().getAttribute(PostLoginWorkflow.URI_ACCESSED)
        if (path == null) {
            path = "/"
        }
        redirect uri: path
    }
}

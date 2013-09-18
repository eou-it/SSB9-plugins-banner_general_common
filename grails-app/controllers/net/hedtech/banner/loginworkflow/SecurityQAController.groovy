package net.hedtech.banner.loginworkflow

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.PinQuestion

class SecurityQAController {

    static defaultAction = "index"
    def securityQAService

    def noOfQuestions
    def questions = [:]
    def questionMinimumLength
    def answerMinimumLength
    def userDefinedQuesFlag
    def questionList = []

    def index() {
        setGlobalVariables()
        render view: "securityQA", model: [questions: questionList, userDefinedQuesFlag: userDefinedQuesFlag, noOfquestions: noOfQuestions, questionMinimumLength: questionMinimumLength, answerMinimumLength: answerMinimumLength,selectedQues: "", selectedAns: [], selectedUserDefinedQues: []]
    }

    private void setGlobalVariables() {

        questionList = loadQuestionList()
        noOfQuestions = securityQAService.getNumberOfQuestions()
        questionMinimumLength = securityQAService.getQuestionMinimumLength()
        answerMinimumLength = securityQAService.getAnswerMinimumLength()
        userDefinedQuesFlag = securityQAService.getUserDefinedQuestionFlag()
    }

    private List loadQuestionList() {
        def ques = PinQuestion.fetchQuestions()
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
            def model = [:]

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
            def questionId
            if(params.question instanceof String) {
                questionId = params.question.split("question")[1].toInteger()
            } else {
                questionId = params.question[index].split("question")[1].toInteger()
            }

            def question
            def questionNo
            if (questionId != 0) {
                question = questionList.get(questionId - 1)
                questionNo = questions.find {it.value == question}?.key
            }
            else {
                question = null
                questionNo = null
            }
            def userDefinedQstn
            if(!userDefinedQuesFlag.equals("N")) {
                if(params.userDefinedQuestion instanceof String) {
                    userDefinedQstn = params.userDefinedQuestion
                } else {
                    userDefinedQstn = params.userDefinedQuestion[index]
                }
            } else {
                userDefinedQstn = null
            }

            def ansr
            if(params.answer instanceof String) {
                ansr = params.answer
            } else {
                ansr = params.answer[index]
            }
            def questionsAnswered = [question: question, questionNo: questionNo, userDefinedQuestion: userDefinedQstn, answer: ansr]
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

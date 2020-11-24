/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.personalinformation

import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import net.hedtech.banner.general.GeneralCommonUtility
import net.hedtech.banner.i18n.MessageHelper

@Transactional
class AnswerSurveyCompositeService {

    def generalSqlJsonService
    public static final String NO_SURVEYS_MSG_KEY = 'BWGKSRV1_SS9-0001'
    public static final String TRUE = 'TRUE'
    public static final int TOTAL_NO_OF_RESPONSES = 5
    public static final String SURVEY_COMPLETE_ACTION = 'Survey Complete'
    public static final String REMOVE_SURVEY_ACTION = 'Remove Survey from List'
    public static final String FINISH_LATER_ACTION = 'Finish Later'

    /**
     * Fetch all the valid surveys that have valid questions, that matches user role and that which is not completed or declined
     * @return list of survey details
     */
    def fetchSurveys() {
        def surveyListJson = generalSqlJsonService.executeProcedure('baninst1_ss9.bwgksrvy.P_ShowSurveys')
        def result
        if (surveyListJson?.messages?.error) {
            String errorMsg = surveyListJson?.messages?.error[0]
            result = getSurveyResultMap(false, errorMsg, null)
        } else {
            def surveyInfoList = getSurveyList(surveyListJson)
            if (surveyInfoList.size()) {
                result = getSurveyResultMap(true, null, surveyInfoList)
            } else {
                String errorMsg = MessageHelper.message(NO_SURVEYS_MSG_KEY)
                result = getSurveyResultMap(false, errorMsg, null)
            }
        }
        result
    }

    private getSurveyResultMap(success, error, surveys) {
        return [
                success: success,
                error  : error,
                surveys: surveys
        ]
    }

    private getSurveyList(parentJson) {
        def surveyInfoList = []
        def surveysJson = parentJson?.begin_13?.begin_7
        if (surveysJson?.surveys_available == 'Y') {
            def surveysListJson = surveysJson?.loop_8
            surveysListJson?.each { survey ->
                def rolesList = survey.loop_9
                boolean hasRole = rolesList ? checkRole(rolesList.gursvrl_rec_gursvrl_role) : true
                if (hasRole && survey.list_survey == 'Y') {
                    def surveyInfo = [
                            surveyTitle: survey.msg_text,
                            surveyName : survey.gubsrvy_rec_gubsrvy_name,
                            nextDisp   : survey.next_disp,
                            surveyInfo : survey.gubsrvy_rec_gubsrvy_info_is_not_null == TRUE ? survey.gubsrvy_rec_gubsrvy_info : ''
                    ]
                    surveyInfoList.push(surveyInfo)
                }
            }
        }
        surveyInfoList
    }

    private checkRole(rolesList) {
        boolean hasRole
        rolesList?.each { role ->
            if (GeneralCommonUtility.checkUserRole(role)) {
                hasRole = true
                return true
            }
        }
        hasRole
    }

    /**
     * Fetches the question and responses information based on the input question number
     * @param surveyParams consists of survey name and next question number to be displayed
     * @return question details for requested question.
     */
    def fetchQuestionAnswers(surveyParams) {
        def inputParamsList = []
        inputParamsList.push(getParameterMap(surveyParams.surveyName, 'string', 'srvy_name'))
        inputParamsList.push(getParameterMap(surveyParams.nextDisp, 'string', 'next_disp'))
        def questionsJson = generalSqlJsonService.executeProcedure('baninst1_ss9.bwgksrvy.P_ShowQuestions', inputParamsList)
        def questionDetails = getQuestions(questionsJson)
        questionDetails
    }

    private def getQuestions(questionsJson) {
        def questionsListJson = questionsJson?.begin_14?.begin_3?.loop_4
        int questionsCount = Integer.parseInt(questionsJson?.begin_14?.begin_3?.questions_count)
        def questionsFormattedList = []
        questionsListJson?.each { questionDetail ->
            if (questionDetail.displayed == '1') {
                def responseDetails = getResponseList(questionDetail)
                def questionDetailMap = [
                        questionNo      : questionDetail.gursrvq_rec_gursrvq_question_no,
                        questionText    : questionDetail.gursrvq_rec_gursrvq_text,
                        questionCode    : questionDetail.CodeSeq,
                        multiResponseInd: questionDetail.gursrvq_rec_gursrvq_mult_resp_ind,
                        responseList    : responseDetails.responseList,
                        radioValue      : responseDetails.radioValue,
                        allowComments   : questionDetail.gursrvq_rec_gursrvq_allow_comments_ind,
                        commentsLabel   : questionDetail.gursrvq_rec_gursrvq_comments_text,
                        commentName     : questionDetail.CmntSeq,
                        comment         : questionDetail.gorsrvr_rec_gorsrvr_comments
                ]
                questionsFormattedList.push(questionDetailMap)
            }
        }
        [
                questionDetails: questionsFormattedList,
                questionsCount : questionsCount
        ]
    }

    private def getResponseList(questionDetailsObj) {
        def responseList = []
        String radioValue
        for (int i = 1; i <= TOTAL_NO_OF_RESPONSES; i++) {
            def responseMap
            if (questionDetailsObj['gursrvq_rec_gursrvq_response_' + i + '_text_is_not_null'] == TRUE) {
                String responseText = questionDetailsObj['gursrvq_rec_gursrvq_response_' + i + '_text']
                boolean checked = (questionDetailsObj['gorsrvr_rec_gorsrvr_response_' + i] == 'Y') ? true : false
                if (questionDetailsObj.gursrvq_rec_gursrvq_mult_resp_ind == 'Y') {
                    responseMap = [
                            checked     : checked,
                            name        : 'rsp1' + i,
                            value       : 'Y',
                            responseText: responseText
                    ]
                } else {
                    responseMap = [
                            name        : 'rsp1' + '1',
                            value       : i as String,
                            responseText: responseText
                    ]
                    if(checked){
                        radioValue = i as String
                    }
                }
                responseList.push(responseMap)
            }
        }
        [
                responseList: responseList,
                radioValue  : radioValue
        ]
    }

    /**
     * Saves user response for a survey question.
     * Displays next question in case submit action is Previous, Next or Return to beginning.
     * Exits the survey with appropriate message in case submit action is Survey complete, Finish later or Remove survey.
     * @param inputParams consists of current question number, next question number, user responses, comment and submit action.
     * @return next question details or appropriate message.
     */
    def saveResponse(inputParams) {
        def result
        JsonSlurper json_params = new JsonSlurper()
        def params = json_params.parseText(inputParams)
        def inputParamsList = getSaveParamsList(params)
        def questionsJson = generalSqlJsonService.executeProcedure('baninst1_ss9.bwgksrvy.P_SaveResponses', inputParamsList)
        if (params.submitAction == SURVEY_COMPLETE_ACTION ||
                params.submitAction == REMOVE_SURVEY_ACTION ||
                params.submitAction == FINISH_LATER_ACTION) {
            result = [
                    message: MessageHelper.message(questionsJson?.begin_12?.success_msg)
            ]
        } else {
            result = getQuestions(questionsJson?.begin_12?.P_ShowQuestions_1)
        }
        result
    }

    private getSaveParamsList(inputParams) {
        def inputParamsList = []
        inputParamsList.push(getParameterMap(inputParams.surveyName, 'string', 'srvy_name'))
        inputParamsList.push(getParameterMap(inputParams.nextDisp, 'number', 'next_disp'))
        inputParamsList.push(getParameterMap(inputParams.questionNo, 'number', 'qust1'))
        inputParamsList.push(getParameterMap(inputParams.submitAction, 'string', 'submit_btn'))
        if (inputParams.comment) {
            inputParamsList.push(getParameterMap(inputParams.comment, 'string', 'cmnt1'))
        }
        def responses = inputParams.responses
        if (responses && responses.size()) {
            responses.each { response ->
                if (response.checked || inputParams.radioValue == response.value) {
                    inputParamsList.push(getParameterMap(response.value, 'string', response.name))
                }
            }
        }
        inputParamsList
    }

    private def getParameterMap(def paramValue, String paramType, String paramName) {
        return [
                paramValue: paramValue,
                paramType : paramType,
                paramName : paramName
        ]
    }

}

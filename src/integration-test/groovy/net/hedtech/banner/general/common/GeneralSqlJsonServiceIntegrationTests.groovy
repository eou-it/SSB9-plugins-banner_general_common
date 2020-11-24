/*********************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.common

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.json.JsonSlurper
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class GeneralSqlJsonServiceIntegrationTests extends BaseIntegrationTestCase {
    def generalSqlJsonService

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
    void testExecuteProcedureWithNoParams() {
        loginSSB('JABS-0011', '111111')
        def json = generalSqlJsonService.executeProcedure('baninst1_ss9.bwvkkmen.P_SelectAidYear')
        assertNotNull(json)
    }

    @Test
    void testExecuteProcedureWithParams() {
        loginSSB('JABS-0011', '111111')
        List inputParamsList = []
        def inputParam = getParameterMap('LG20', 'string', 'p_aidy_code')
        inputParamsList.push(inputParam)
        def json = generalSqlJsonService.executeProcedure('baninst1_ss9.bwvkkmen.P_SelectAidYear', inputParamsList)
        assertNotNull(json)
    }

    @Test
    void testExecuteProcedureWithNoAuth() {
        def inputParamsList= []
        inputParamsList.push(getParameterMap('LG20', 'string', 'p_aidy_code'))
        inputParamsList.push(getParameterMap('LGFALL19', 'string', 'p_aidp_code'))
        inputParamsList.push(getParameterMap('OB_MOD_AW', 'string', 'p_objt_code'))
        inputParamsList.push(getParameterMap('LG-BURSARIES', 'string', 'p_objt_inst_id'))
        inputParamsList.push(getParameterMap('OB_AP_FORM', 'string', 'p_par_objt_code'))
        inputParamsList.push(getParameterMap('LG-STUD-AID', 'string', 'p_par_objt_inst_id'))
        inputParamsList.push(getParameterMap(1001, 'number', 'p_disp_seq_no'))
        def json_data = generalSqlJsonService.executeProcedure('baninst1_ss9.bwvkkapf.P_PreviewModulePage', inputParamsList, false)
        assertNotNull(json_data)
    }

    @Test
    void testExecuteProcedureWithArrParams() {
        loginSSB('A00040170', '111111')
        String inputParams = "{\"studentsData\":[{\"comments\":\"\",\"pidm\":105392,\"grade\":\"A\",\"recomm\":[\"1=12\"],\"row\":1,\"issues\":[\"1=10\",\"1=32\"]},{\"comments\":\"Hi\",\"pidm\":105393,\"recomm\":[\"2=31\"],\"row\":2,\"issues\":[\"2=2\"]}],\"p_student_count\":2,\"p_crn_in\":\"13016\",\"p_session_control_id_in\":50,\"p_target_rec\":\"1\",\"p_class_size\":2}"
        def inputParamsList = reformatInputJSON(inputParams)
        def out_json = generalSqlJsonService.executeProcedure('baninst1_ss9.bwlkfdbk.p_facultyfeedbackpost', inputParamsList)
        assertNotNull( out_json )
    }

    private def reformatInputJSON(inputParams) {
        JsonSlurper json_params = new JsonSlurper()
        def params = json_params.parseText(inputParams)
        def pidmList = []
        def gradesList = []
        def issuesList = ['Placeholder']
        def recommList = ['Placeholder']
        def commentsList = []
        params.studentsData.each { data ->
            pidmList.push(String.valueOf(data.pidm))
            gradesList.push(data.grade ? (data.grade.equals('None') ? '' : data.grade) : '')
            commentsList.push(data.comments ?: '')
            data.recomm.each { rec ->
                recommList.push(rec)
            }
            data.issues.each { issue ->
                issuesList.push(issue)
            }
        }
        def inputParamsList = []
        inputParamsList.push(getParameterMap(pidmList, 'ident_arr', 'p_pidm_tab'))
        inputParamsList.push(getParameterMap(gradesList, 'ident_arr', 'p_egrde_tab'))
        inputParamsList.push(getParameterMap(issuesList, 'ident_arr', 'p_pcheck_tab'))
        inputParamsList.push(getParameterMap(recommList, 'ident_arr', 'p_rcheck_tab'))
        inputParamsList.push(getParameterMap(commentsList, 'vc_arr', 'p_comments_tab'))
        inputParamsList.push(getParameterMap(params.p_student_count, 'int', 'p_student_count'))
        inputParamsList.push(getParameterMap(params.p_target_rec, 'int', 'p_target_rec'))
        inputParamsList.push(getParameterMap(params.p_class_size, 'int', 'p_class_size'))
        inputParamsList.push(getParameterMap(params.p_crn_in, 'string', 'p_crn_in'))
        inputParamsList.push(getParameterMap(params.p_session_control_id_in, 'int', 'p_session_control_id_in'))
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
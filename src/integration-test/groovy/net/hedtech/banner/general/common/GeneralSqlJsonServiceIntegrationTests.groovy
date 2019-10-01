/*********************************************************************************
 Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.common

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import grails.web.servlet.context.GrailsWebApplicationContext
import net.hedtech.banner.general.communication.email.CommunicationEmailItem
import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

@Integration
@Rollback
class GeneralSqlJsonServiceIntegrationTests extends BaseIntegrationTestCase {
    def generalSqlJsonService
    def facultyFeedbackCompositeService

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('A00040170', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
    }

    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    @Test
    void testExecuteProcedureWithNoParams() {
        def json = generalSqlJsonService.executeProcedure('silkfdbk.f_facultyfeedback')
        assertNotNull(json)
    }

    @Test
    void testExecuteProcedureWithPrimitiveParams() {
        def queryObject = [
                p_crn               : '13002',
                p_term              : '201913',
                p_target_rec        : '0',
                p_session_control_id: '50']

        def inputParamsList = facultyFeedbackCompositeService.reformatInputforFetchRoster(queryObject)
        def json_data = generalSqlJsonService.executeProcedure('silkfdbk.f_facultyfeedback', inputParamsList)
        assertNotNull(json_data)
    }

    @Test
    void testExecuteProcedureWithArrayParams() {
        String inputParams = """{
                "studentsData":[
                {
                    "comments":"",
                    "pidm":105392,
                    "grade":"A",
                    "recomm":["1=12"],
                    "row":1,
                    "issues":["1=10","1=32"]
                },
                {
                    "comments":"Hi",
                    "pidm":105393,
                    "recomm":["2=31"],
                    "row":2,
                    "issues":["2=2"]
                }],
                "p_student_count":2,
                "p_crn_in":"13016",
                "p_session_control_id_in":50,
                "p_target_rec":"1",
                "p_class_size":2
                }"""

        def inputParamsList = facultyFeedbackCompositeService.reformatInputforFetchRoster(inputParams)
        def json_data = generalSqlJsonService.executeProcedure('silkfdbk.f_facultyfeedbackpost', inputParamsList)
        assertNotNull(json_data)
    }
}

/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.overall.ldm.v7.CampusOrganizationsV7
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.Before
import org.junit.Test


class CampusOrganizationsCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    CampusOrganizationsCompositeService campusOrganizationsCompositeService
    final String i_success_code_1 = '180'
    final String i_success_code_2 = 'MBA'
    GlobalUniqueIdentifier o_success_guid_1
    GlobalUniqueIdentifier o_success_guid_2
    

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initializeDataReferences()
    }


    private void initializeDataReferences() {
        o_success_guid_1 = GlobalUniqueIdentifier.fetchByDomainKeyAndLdmName(i_success_code_1, GeneralCommonConstants.STUDENT_ACTIVITY_LDM_NAME)
        assertNotNull o_success_guid_1
        o_success_guid_2 = GlobalUniqueIdentifier.fetchByDomainKeyAndLdmName(i_success_code_2, GeneralCommonConstants.COMMITTEE_TYPE_LDM_NAME)
        assertNotNull o_success_guid_2
    }


    @Test
    void testValidList_v7() {
        setAcceptHeader("application/vnd.hedtech.integration.v7+json")
        Map params = [:]
        List<CampusOrganizationsV7> campusOrganizationsV7List = campusOrganizationsCompositeService.list(params)
        assertNotNull campusOrganizationsV7List
    }


    @Test
    void testValidList_RequiredProperties_v7() {

        //Required Properties are id and name
        Map params = [:]
        setAcceptHeader("application/vnd.hedtech.integration.v7+json")
        List<CampusOrganizationsV7> campusOrganizationsV7List = campusOrganizationsCompositeService.list(params)
        assertNotNull campusOrganizationsV7List

        campusOrganizationsV7List.each { campusOrganizationsV7 ->
            assertNotNull campusOrganizationsV7.guid
            assertNotNull campusOrganizationsV7.name
        }
    }


    @Test
    void testValidGet_v7() {
        setAcceptHeader("application/vnd.hedtech.integration.v7+json")
        CampusOrganizationsV7 campusOrganizationsV7_stuAct = campusOrganizationsCompositeService.get(o_success_guid_1.guid)
        assertNotNull campusOrganizationsV7_stuAct
        assertNotNull campusOrganizationsV7_stuAct.guid
        assertNotNull campusOrganizationsV7_stuAct.name

        CampusOrganizationsV7 campusOrganizationsV7_comType = campusOrganizationsCompositeService.get(o_success_guid_2.guid)
        assertNotNull campusOrganizationsV7_comType
        assertNotNull campusOrganizationsV7_comType.guid
        assertNotNull campusOrganizationsV7_comType.name
    }


    @Test
    void testGet_NotFound() {
        setAcceptHeader("application/vnd.hedtech.integration.v7+json")
        try {
            campusOrganizationsCompositeService.get("invalid-guid")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "NotFoundException"
        }
    }


    private void setAcceptHeader(String mediaType) {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", mediaType)
    }
}

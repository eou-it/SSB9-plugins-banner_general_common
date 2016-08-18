/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before
import org.junit.Test


class CampusOrganizationViewServiceIntegrationTests extends BaseIntegrationTestCase {

    CampusOrganizationViewService campusOrganizationViewService
    final String i_success_code_1 = '180'
    GlobalUniqueIdentifier o_success_guid_1

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initializeDataReferences()
    }


    private void initializeDataReferences() {
        o_success_guid_1 = GlobalUniqueIdentifier.fetchByDomainKeyAndLdmName(i_success_code_1, GeneralCommonConstants.STUDENT_ACTIVITY_LDM_NAME)
        assertNotNull o_success_guid_1
    }


    @Test
    void testCreateNotAllowed() {
        shouldFail( ApplicationException ) {
            campusOrganizationViewService.create( [campusOrganizationsView: new CampusOrganizationView()] )
        }
    }


    @Test
    void testUpdateNotAllowed() {
        shouldFail( ApplicationException ) {
            campusOrganizationViewService.update( [campusOrganizationsView: new CampusOrganizationView()] )
        }
    }


    @Test
    void testDeleteNotAllowed() {
        shouldFail( ApplicationException ) {
            campusOrganizationViewService.delete( [campusOrganizationsView: new CampusOrganizationView()] )
        }
    }


    @Test
    void testReadAllowed() {
        CampusOrganizationView campusOrganizationsView = CampusOrganizationView.findAll()[0]
        CampusOrganizationView objReadUsingService = campusOrganizationViewService.read( campusOrganizationsView.id ) as CampusOrganizationView
        assertNotNull objReadUsingService
        assertEquals( campusOrganizationsView.id, objReadUsingService.id )
        assertEquals( campusOrganizationsView.campusOrgDesc, objReadUsingService.campusOrgDesc )
    }


    @Test
    void testFetchAll() {
        List<CampusOrganizationView> campusOrganizationsViewList = campusOrganizationViewService.fetchAll(500, 0)
        assertNotNull campusOrganizationsViewList
        assertTrue campusOrganizationsViewList.size() > 0

        campusOrganizationsViewList.each { campusOrganizationsView ->
            assertNotNull campusOrganizationsView.id
            assertNotNull campusOrganizationsView.campusOrgDesc
        }
    }


    @Test
    void testFetchByCode() {
        CampusOrganizationView campusOrganizationsView = campusOrganizationViewService.fetchByCode(o_success_guid_1.domainKey)
        assertNotNull campusOrganizationsView
        assertNotNull campusOrganizationsView.id
        assertNotNull campusOrganizationsView.campusOrgDesc
    }


    @Test
    void testFetchByGuid() {
        CampusOrganizationView campusOrganizationsView = campusOrganizationViewService.fetchByGuid(o_success_guid_1.guid)
        assertNotNull campusOrganizationsView
        assertNotNull campusOrganizationsView.id
        assertNotNull campusOrganizationsView.campusOrgDesc
    }

}

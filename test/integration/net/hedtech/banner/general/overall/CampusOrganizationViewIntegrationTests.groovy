/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.dao.InvalidDataAccessResourceUsageException


class CampusOrganizationViewIntegrationTests extends BaseIntegrationTestCase {

    final String i_success_code_1 = '180'
    GlobalUniqueIdentifier o_success_guid_1

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initializeDataReferences()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    private void initializeDataReferences() {
        o_success_guid_1 = GlobalUniqueIdentifier.fetchByDomainKeyAndLdmName(i_success_code_1, GeneralCommonConstants.STUDENT_ACTIVITY_LDM_NAME)
        assertNotNull o_success_guid_1
    }

    /**
     * Tests that view does not allow create,update,delete  operations and is readonly
     */

    @Test
    void testCreateExceptionResults() {
        CampusOrganizationView existingList = CampusOrganizationView.findAll()[0]
        assertNotNull existingList.toString()
        CampusOrganizationView newRecord = new CampusOrganizationView(existingList.properties)
        newRecord.campusOrgCode = "99999"
        newRecord.id = 'random-guid'
        shouldFail(InvalidDataAccessResourceUsageException) {
            newRecord.save(flush: true, onError: true)
        }

    }


    @Test
    void testUpdateExceptionResults() {
        CampusOrganizationView existingList = CampusOrganizationView.findAll()[0]
        assertNotNull existingList.toString()
        existingList.campusOrgDesc = "This is a test update"
        shouldFail(InvalidDataAccessResourceUsageException) {
            existingList.save(flush: true, onError: true)
        }
    }


    @Test
    void testDeleteExceptionResults() {
        CampusOrganizationView existingList = CampusOrganizationView.findAll()[0]
        assertNotNull existingList.toString()
        //Changed from org.springframework.orm.hibernate3.HibernateJdbcException due to spring 4.1.5
        shouldFail() {
            existingList.delete(flush: true, onError: true)
        }
    }

}

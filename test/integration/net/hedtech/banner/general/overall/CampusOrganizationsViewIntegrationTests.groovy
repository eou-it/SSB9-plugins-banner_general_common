/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.dao.InvalidDataAccessResourceUsageException


class CampusOrganizationsViewIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    /**
     * Tests that view does not allow create,update,delete  operations and is readonly
     */

    @Test
    void testCreateExceptionResults() {
        CampusOrganizationsView existingList = CampusOrganizationsView.findAll()[0]
        assertNotNull existingList.toString()
        CampusOrganizationsView newRecord = new CampusOrganizationsView(existingList.properties)
        newRecord.campusOrgCode = "99999"
        newRecord.id = 'random-guid'
        shouldFail(InvalidDataAccessResourceUsageException) {
            newRecord.save(flush: true, onError: true)
        }

    }


    @Test
    void testUpdateExceptionResults() {
        CampusOrganizationsView existingList = CampusOrganizationsView.findAll()[0]
        assertNotNull existingList.toString()
        existingList.campusOrgDesc = "This is a test update"
        shouldFail(InvalidDataAccessResourceUsageException) {
            existingList.save(flush: true, onError: true)
        }
    }


    @Test
    void testDeleteExceptionResults() {
        CampusOrganizationsView existingList = CampusOrganizationsView.findAll()[0]
        assertNotNull existingList.toString()
        //Changed from org.springframework.orm.hibernate3.HibernateJdbcException due to spring 4.1.5
        shouldFail() {
            existingList.delete(flush: true, onError: true)
        }
    }

}

/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.system

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.dao.InvalidDataAccessResourceUsageException

class EducationalInstitutionViewIntegrationTests extends BaseIntegrationTestCase {
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
        EducationalInstitutionView existingInstitution = EducationalInstitutionView.findAll()[0]
        assertNotNull existingInstitution.toString()
        EducationalInstitutionView newInstitution = new EducationalInstitutionView(existingInstitution.properties)
        newInstitution.title="random-test-title"
        newInstitution.id='99999'
        shouldFail(InvalidDataAccessResourceUsageException) {
            newInstitution.save(flush: true, onError: true)
        }

    }


    @Test
    void testUpdateExceptionResults() {
        EducationalInstitutionView existingInstitution = EducationalInstitutionView.findAll()[0]
        assertNotNull existingInstitution.toString()
        existingInstitution.sourceTable = "This is a test update"
        shouldFail(InvalidDataAccessResourceUsageException) {
            existingInstitution.save(flush: true, onError: true)
        }
    }


    @Test
    void testDeleteExceptionResults() {
        EducationalInstitutionView existingInstitution = EducationalInstitutionView.findAll()[0]
        assertNotNull existingInstitution.toString()
        //Changed from org.springframework.orm.hibernate3.HibernateJdbcException due to spring 4.1.5
        shouldFail() {
            existingInstitution.delete(flush: true, onError: true)
        }
    }
}

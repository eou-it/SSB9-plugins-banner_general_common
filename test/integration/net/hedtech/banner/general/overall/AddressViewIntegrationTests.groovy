/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.dao.InvalidDataAccessResourceUsageException

class AddressViewIntegrationTests extends BaseIntegrationTestCase {
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
        AddressView existingAddress = AddressView.findAll()[0]
        assertNotNull existingAddress.toString()
        AddressView newAddress = new AddressView(existingAddress.properties)
        newAddress.addressLine1="random-test-address"
        newAddress.id='99999'
        shouldFail(InvalidDataAccessResourceUsageException) {
            newAddress.save(flush: true, onError: true)
        }

    }


    @Test
    void testUpdateExceptionResults() {
        AddressView existingAddress = AddressView.findAll()[0]
        assertNotNull existingAddress.toString()
        existingAddress.sourceTable = "This is a test update"
        shouldFail(InvalidDataAccessResourceUsageException) {
            existingAddress.save(flush: true, onError: true)
        }
    }


    @Test
    void testDeleteExceptionResults() {
        AddressView existingAddress = AddressView.findAll()[0]
        assertNotNull existingAddress.toString()
        //Changed from org.springframework.orm.hibernate3.HibernateJdbcException due to spring 4.1.5
        shouldFail() {
            existingAddress.delete(flush: true, onError: true)
        }
    }
}

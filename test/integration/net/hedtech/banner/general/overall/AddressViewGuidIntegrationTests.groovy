/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.dao.InvalidDataAccessResourceUsageException

/**
 * Tests that view does not allow create,update,delete operations and is readonly
 */
class AddressViewGuidIntegrationTests extends BaseIntegrationTestCase {


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateExceptionResults() {
        AddressViewGuid existingAddress = AddressViewGuid.findAll([max: 10])[0]
        assertNotNull existingAddress.toString()
        AddressViewGuid newAddress = new AddressViewGuid(existingAddress.properties)
        newAddress.id='99999'
        shouldFail(InvalidDataAccessResourceUsageException) {
            newAddress.save(flush: true, onError: true)
        }

    }


    @Test
    void testDeleteExceptionResults() {
        AddressViewGuid existingAddress = AddressViewGuid.findAll([max: 10])[0]
        assertNotNull existingAddress.toString()
        //Changed from org.springframework.orm.hibernate3.HibernateJdbcException due to spring 4.1.5
        shouldFail() {
            existingAddress.delete(flush: true, onError: true)
        }
    }
}

/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.personalinformation

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class PersonalInformationCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def personalInformationCompositeService

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
    void testGetAddressValidationObjects() {
        def roles = ['EMPLOYEE']
        def address = [:]
        address.addressType = [code: 'PR', description: 'Permanent']
        address.nation = [code: '155', description: 'United Arab Emirates']

        def result = personalInformationCompositeService.getAddressValidationObjects(roles, address)

        assertNotNull result.addressType.id
        assertNotNull result.nation.id
    }

    @Test
    void testGetAddressValidationObjectsNoAtyp() {
        def roles = ['EMPLOYEE']
        def address = [:]
        address.nation = [code: '155', description: 'United Arab Emirates']

        try {
            def result = personalInformationCompositeService.getAddressValidationObjects(roles, address)
            fail("I should have received an error but it passed; @@r1:NullPointerException@@ ")
        }
        catch (Exception e) {
            assertEquals java.lang.NullPointerException, e.getClass()
        }
    }

    @Test
    void testGetAddressValidationObjectsBadAtyp() {
        def roles = ['EMPLOYEE']
        def address = [:]
        address.addressType = [code: 'W2', description: 'W2 Address']
        address.nation = [code: '155', description: 'United Arab Emirates']

        try{
            def result = personalInformationCompositeService.getAddressValidationObjects(roles, address)
            fail("I should have received an error but it passed; @@r1:invalidAddressType@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidAddressType"
        }
    }

    @Test
    void testGetAddressValidationObjectsBadCounty() {
        def roles = ['EMPLOYEE']
        def address = [:]
        address.addressType = [code: 'PR', description: 'Permanent']
        address.county = [code: 'Z989', description: 'fail']

        try{
            def result = personalInformationCompositeService.getAddressValidationObjects(roles, address)
            fail("I should have received an error but it passed; @@r1:invalidCounty@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidCounty"
        }
    }

    @Test
    void testGetAddressValidationObjectsBadState() {
        def roles = ['EMPLOYEE']
        def address = [:]
        address.addressType = [code: 'PR', description: 'Permanent']
        address.state = [code: 'XJ', description: 'fail']
        address.county = [code: '251', description: 'Los Angeles County']

        try{
            def result = personalInformationCompositeService.getAddressValidationObjects(roles, address)
            fail("I should have received an error but it passed; @@r1:invalidState@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidState"
        }
    }

    @Test
    void testGetAddressValidationObjectsBadNation() {
        def roles = ['EMPLOYEE']
        def address = [:]
        address.addressType = [code: 'PR', description: 'Permanent']
        address.nation = [code: '8080', description: 'fail']
        address.state = [code: 'TX', description: 'Texas']
        address.county = [code: '251', description: 'Los Angeles County']

        try{
            def result = personalInformationCompositeService.getAddressValidationObjects(roles, address)
            fail("I should have received an error but it passed; @@r1:invalidNation@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidNation"
        }
    }

}
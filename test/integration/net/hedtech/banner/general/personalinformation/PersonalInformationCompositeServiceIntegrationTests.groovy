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
    void testGetPersonValidationObjects() {
        def roles = ['EMPLOYEE']
        def person = [:]
        person.addressType = [code: 'PR', description: 'Permanent']
        person.nation = [code: '155', description: 'United Arab Emirates']
        person.relationship = [code: 'P', description: 'Spouse']

        def result = personalInformationCompositeService.getPersonValidationObjects(roles, person)

        assertNotNull result.addressType.id
        assertNotNull result.nation.id
        assertNotNull result.relationship.id
    }

    @Test
    void testGetPersonValidationObjectsBadAtyp() {
        def roles = ['EMPLOYEE']
        def address = [:]
        address.addressType = [code: 'W2', description: 'W2 Address']
        address.nation = [code: '155', description: 'United Arab Emirates']

        try{
            def result = personalInformationCompositeService.getPersonValidationObjects(roles, address)
            fail("I should have received an error but it passed; @@r1:invalidAddressType@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidAddressType"
        }
    }

    @Test
    void testGetPersonValidationObjectsBadCounty() {
        def roles = ['EMPLOYEE']
        def address = [:]
        address.addressType = [code: 'PR', description: 'Permanent']
        address.county = [code: 'Z989', description: 'fail']

        try{
            def result = personalInformationCompositeService.getPersonValidationObjects(roles, address)
            fail("I should have received an error but it passed; @@r1:invalidCounty@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidCounty"
        }
    }

    @Test
    void testGetPersonValidationObjectsBadState() {
        def roles = ['EMPLOYEE']
        def address = [:]
        address.addressType = [code: 'PR', description: 'Permanent']
        address.state = [code: 'XJ', description: 'fail']
        address.county = [code: '251', description: 'Los Angeles County']

        try{
            def result = personalInformationCompositeService.getPersonValidationObjects(roles, address)
            fail("I should have received an error but it passed; @@r1:invalidState@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidState"
        }
    }

    @Test
    void testGetPersonValidationObjectsBadNation() {
        def roles = ['EMPLOYEE']
        def address = [:]
        address.addressType = [code: 'PR', description: 'Permanent']
        address.nation = [code: '8080', description: 'fail']
        address.state = [code: 'TX', description: 'Texas']
        address.county = [code: '251', description: 'Los Angeles County']

        try{
            def result = personalInformationCompositeService.getPersonValidationObjects(roles, address)
            fail("I should have received an error but it passed; @@r1:invalidNation@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidNation"
        }
    }

    @Test
    void testGetPersonValidationObjectsBadRelationship() {
        def roles = ['EMPLOYEE']
        def person = [:]
        person.addressType = [code: 'PR', description: 'Permanent']
        person.state = [code: 'TX', description: 'Texas']
        person.county = [code: '251', description: 'Los Angeles County']
        person.relationship = [code: 'Z', description: 'fail']

        try{
            def result = personalInformationCompositeService.getPersonValidationObjects(roles, person)
            fail("I should have received an error but it passed; @@r1:invalidRelationship@@ ")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalidRelationship"
        }
    }

}
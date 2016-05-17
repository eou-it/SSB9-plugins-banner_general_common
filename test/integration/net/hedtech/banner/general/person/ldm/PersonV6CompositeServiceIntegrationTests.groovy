/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.After
import org.junit.Before
import org.junit.Test

class PersonV6CompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    PersonV6CompositeService personV6CompositeService

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
    void testListPersonV6InvalidRoleRequired() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        try {
            personV6CompositeService.list(params)
            fail('Role is Required')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'role.required'
        }
    }


    @Test
    void testListPersonV6InvalidForRoleFaculty() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        Map params = [role: "faculty"]

        try {
            personV6CompositeService.list(params)
            fail('Invalid role for Person V6')
        } catch (ApplicationException ae) {
            assertApplicationException ae, 'role.supported.v6'
        }
    }


    @Test
    void testListPersonValidV6ForRoleInstructor() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        Map params = [role: "instructor"]
        def o_success_persons = personV6CompositeService.list(params)

        assertNotNull o_success_persons
        assertFalse o_success_persons.isEmpty()
        assertNotNull o_success_persons[0].guid
    }


    @Test
    void testListPersonValidV6ForRoleStudent() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        Map params = [role: "student"]
        def o_success_persons = personV6CompositeService.list(params)

        assertNotNull o_success_persons
        assertFalse o_success_persons.isEmpty()
        assertNotNull o_success_persons[0].guid
    }


    private void setAcceptHeader(String mediaType) {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", mediaType)
    }
}

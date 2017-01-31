/*********************************************************************************
 Copyright 2016-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.After
import org.junit.Before
import org.junit.Test

class PersonCredentialFacadeServiceIntegrationTests extends BaseIntegrationTestCase {

    PersonCredentialFacadeService personCredentialFacadeService

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
    void testListV6() {
        setContentTypeAndAcceptHeaders("application/vnd.hedtech.integration.v6+json")

        List personCredentials = personCredentialFacadeService.list([:])
        assertNotNull personCredentials
        assertFalse personCredentials.isEmpty()
        assertTrue personCredentials.size() <= RestfulApiValidationUtility.MAX_DEFAULT
        assertTrue personCredentials[0] instanceof net.hedtech.banner.general.overall.ldm.v6.PersonCredentialsDecorator
    }

    @Test
    void testListV8() {
        setContentTypeAndAcceptHeaders("application/vnd.hedtech.integration.v8+json")

        List personCredentials = personCredentialFacadeService.list([:])
        assertNotNull personCredentials
        assertFalse personCredentials.isEmpty()
        assertTrue personCredentials.size() <= RestfulApiValidationUtility.MAX_DEFAULT
        assertTrue personCredentials[0] instanceof net.hedtech.banner.general.overall.ldm.v8.PersonCredentialsDecorator
    }

    @Test
    void testCount() {
        def actualCount = personCredentialFacadeService.count().toInteger()
        assertNotNull actualCount
        assertTrue actualCount > 0
    }


    @Test
    void testGet_v6() {
        setContentTypeAndAcceptHeaders("application/vnd.hedtech.integration.v6+json")
        def personCredentials = personCredentialFacadeService.list([max: '1', offset: '0'])
        assertNotNull personCredentials
        assertFalse personCredentials.isEmpty()
        String guid = personCredentials[0].guid

        def personCredentialsSingle = personCredentialFacadeService.get(guid)
        assertTrue personCredentialsSingle instanceof net.hedtech.banner.general.overall.ldm.v6.PersonCredentialsDecorator

    }

    @Test
    void testGet_v8() {
        setContentTypeAndAcceptHeaders("application/vnd.hedtech.integration.v8+json")
        def personCredentials = personCredentialFacadeService.list([max: '1', offset: '0'])
        assertNotNull personCredentials
        assertFalse personCredentials.isEmpty()
        String guid = personCredentials[0].guid

        def personCredentialsSingle = personCredentialFacadeService.get(guid)
        assertTrue personCredentialsSingle instanceof net.hedtech.banner.general.overall.ldm.v8.PersonCredentialsDecorator
    }


    @Test
    void testGetForInvalidGuid_v6() {
        setContentTypeAndAcceptHeaders("application/vnd.hedtech.integration.v6+json")
        try {
            personCredentialFacadeService.get('Invalid-guid')
            fail('This should have failed as the guid is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, "NotFoundException"
        }
    }

    @Test
    void testGetForInvalidGuid_v8() {
        setContentTypeAndAcceptHeaders("application/vnd.hedtech.integration.v8+json")
        try {
            personCredentialFacadeService.get('Invalid-guid')
            fail('This should have failed as the guid is invalid')
        } catch (ApplicationException ae) {
            assertApplicationException ae, "NotFoundException"
        }
    }


    private void setContentTypeAndAcceptHeaders(String mediaType) {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", mediaType)
        request.addHeader("Content-Type", mediaType)
    }


}

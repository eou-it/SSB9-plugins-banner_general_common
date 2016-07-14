/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.Before
import org.junit.Test

class AddressCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    AddressCompositeService addressCompositeService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @Test
    void testValidList(){
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        def paginationParams = [:]
        def addresses = addressCompositeService.list(paginationParams)
        assertNotNull addresses
    }

    @Test
    void testGet(){
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        def paginationParams = [:]
        def addresses = addressCompositeService.list(paginationParams)
        assertNotNull addresses
        String guid = addresses.get(0).guid
        def address = addressCompositeService.get(guid)
        assertNotNull address
        assertEquals address.guid, guid
    }


    private void setAcceptHeader(String mediaType) {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", mediaType)
    }


    private void setContentTypeHeader(String mediaType) {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Content-Type", mediaType)
    }
}

/*********************************************************************************
 Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.letter


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import grails.web.databinding.DataBinder
import grails.web.servlet.context.GrailsWebApplicationContext
import groovy.util.slurpersupport.GPathResult
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Tests crud methods provided by communication template service.
 */
@Integration
@Rollback
class CommunicationLetterTemplateServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationTemplateService
    def communicationLetterTemplateService
    def selfServiceBannerAuthenticationProvider

    CommunicationFolder defaultFolder


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        webAppCtx = new GrailsWebApplicationContext()
        mockRequest()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
    }

    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    public GrailsWebRequest mockRequest() {
        GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();
        GrailsWebMockUtil.bindMockWebRequest(webAppCtx, mockRequest, mockResponse)
    }

    void setUpData() {
        defaultFolder = new CommunicationFolder(
                name: "testFolder"
        )
        defaultFolder.save(failOnError: true, flush: true)
        assertNotNull defaultFolder.id
    }

    @Test
    void testCreate() {
        setUpData()
        Date today = new Date()
        CommunicationLetterTemplate template = new CommunicationLetterTemplate(
            name: "testCreate",
            validFrom: today,
            folder: defaultFolder
        )

        template = communicationLetterTemplateService.create(template)
        assertNotNull template.createDate
        assertNotNull template.createdBy
        assertNotNull template.lastModified
        assertNotNull template.lastModifiedBy
        assertEquals today, template.validFrom
        assertNull template.validTo
        assertFalse template.systemIndicator

        def templateList = communicationTemplateService.findAll()
        assertEquals( 1, templateList.size() )
    }


    @Test
    void testUpdateTemplate() {
        setUpData()
        CommunicationLetterTemplate template = new CommunicationLetterTemplate(
            name: "testUpdateTemplate",
            validFrom: new Date(),
            validTo: null,
            folder: defaultFolder
        )

        template = communicationLetterTemplateService.create(template)
        assertNotNull template.id

        template.description = "Updated description"
        template = communicationLetterTemplateService.update(template)
        assertEquals( "Updated description", template.description )
    }

    @Test
    void testDelete() {
        setUpData()
        Date today = new Date()
        CommunicationLetterTemplate template = new CommunicationLetterTemplate(
                name: "testDelete",
                validFrom: today,
                folder: defaultFolder
        )

        template = communicationLetterTemplateService.create( template )
        assertNotNull template.id

        assertNotNull( CommunicationLetterTemplate.get( template.id ) )

        communicationTemplateService.delete( template )

        assertNull( CommunicationLetterTemplate.get( template.id ) )
    }

    @Test
    void testNoToAddressPublish() {
        setUpData()
        CommunicationLetterTemplate template = new CommunicationLetterTemplate(
                name: "testUpdateTemplate",
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder,
                content: "testToAddressCannotBeNull"
        )

        template = (CommunicationLetterTemplate) communicationLetterTemplateService.create(template)

        try {
            template = (CommunicationLetterTemplate) communicationLetterTemplateService.publish( template )
            fail "Expected template to fail publish because of no toAddress"
        } catch (ApplicationException e ) {
            assertEquals( "@@r1:toAddressRequiredToPublish@@", e.wrappedException.message )
        }

        template.toAddress = "Los Angeles"
        template = (CommunicationLetterTemplate) communicationLetterTemplateService.publish(template)
        assertTrue template.published
    }

    @Test
    void testNoContentPublish() {
        setUpData()
        CommunicationLetterTemplate template = new CommunicationLetterTemplate(
                name: "testUpdateTemplate",
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder,
                toAddress: "attn: santa claus, 1 north pole way, north pole"
        )

        template = (CommunicationLetterTemplate) communicationLetterTemplateService.create(template)

        try {
            template = (CommunicationLetterTemplate) communicationLetterTemplateService.publish(template)
            fail "Expected template to fail publish because of no content"
        } catch (ApplicationException e) {
            assertEquals("@@r1:contentRequiredToPublish@@", e.wrappedException.message)
        }

        template.content = "A deployment of BCM"
        template = (CommunicationLetterTemplate) communicationLetterTemplateService.publish(template)
        assertTrue template.published
    }

}

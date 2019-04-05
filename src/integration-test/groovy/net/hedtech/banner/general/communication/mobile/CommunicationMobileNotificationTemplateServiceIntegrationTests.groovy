/*********************************************************************************
 Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.mobile

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import grails.web.servlet.context.GrailsWebApplicationContext
import groovy.time.DatumDependentDuration
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.template.CommunicationDurationUnit
import net.hedtech.banner.general.communication.template.CommunicationTemplate
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
class CommunicationMobileNotificationTemplateServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationTemplateService
    def communicationMobileNotificationTemplateService
    def selfServiceBannerAuthenticationProvider

    def CommunicationFolder defaultFolder


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
        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
            name: "testCreate",
            validFrom: today,
            folder: defaultFolder
        )

        template = communicationMobileNotificationTemplateService.create(template)
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
        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
            name: "testUpdateTemplate",
            validFrom: new Date(),
            validTo: null,
            folder: defaultFolder
        )
        template = communicationMobileNotificationTemplateService.create(template)
        assertNotNull template.id

        template.description = "Updated description"
        template = communicationMobileNotificationTemplateService.update(template)
        assertEquals( "Updated description", template.description )
    }

    @Test
    void testDurationCannotBeNull() {
        setUpData()
        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
                name: "testUpdateTemplate",
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder,
                expirationPolicy: CommunicationMobileNotificationExpirationPolicy.DURATION,
                durationUnit: CommunicationDurationUnit.MINUTE
        )

        template.duration = -1
        try {
            template = (CommunicationMobileNotificationTemplate) communicationMobileNotificationTemplateService.create(template)
            fail( "Expected application exception from validate exception on creation." )
        } catch (ApplicationException ae ) {
            assertEquals( "ValidationException", ae.wrappedException.getClass().simpleName )
        }

        template.duration = 1
        template = (CommunicationMobileNotificationTemplate) communicationMobileNotificationTemplateService.create(template)
        assertNotNull template.id

        template.duration = -1
        try {
            template = (CommunicationMobileNotificationTemplate) communicationMobileNotificationTemplateService.update(template)
            fail( "Expected application exception from validate exception on update." )
        } catch (ApplicationException ae) {
            assertEquals( "ValidationException", ae.wrappedException.getClass().simpleName )
        }

        template.duration = 2
        communicationMobileNotificationTemplateService.update(template)
    }

    @Test
    void testNoExpirationPublish() {
        setUpData()
        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
            name: "testNoExpirationPublish",
            validFrom: new Date(),
            validTo: null,
            folder: defaultFolder
        )
        template = communicationMobileNotificationTemplateService.create( template )
        assertNotNull template.id
        assertFalse template.published

        assertCannotPublish( template, "mobileHeadlineFieldRequiredToPublish" )
        template.mobileHeadline = "a mobile headline"
        template = communicationMobileNotificationTemplateService.publish( template )
        assertTrue template.published
    }


    @Test
    void testDateTimeExpirationPublish() {
        setUpData()
        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
            name: "testDateTimeExpirationPublish",
            validFrom: new Date(),
            validTo: null,
            folder: defaultFolder,
            expirationPolicy: CommunicationMobileNotificationExpirationPolicy.DATE_TIME
        )
        template = communicationMobileNotificationTemplateService.create( template )
        assertNotNull template.id
        assertFalse template.published

        assertCannotPublish( template, "mobileHeadlineFieldRequiredToPublish" )
        template.mobileHeadline = "a mobile headline"
        assertCannotPublish( template, "expirationDateRequiredToPublish" )

        Date today = new Date()
        DatumDependentDuration period = new DatumDependentDuration(0, 0, 2, 0, 0, 0, 0)
        Date expirationDate = period + today
        template.expirationDateTime = expirationDate
        template = communicationMobileNotificationTemplateService.publish( template )
        assertTrue template.published
    }

    @Test
    void testDurationExpirationPublish() {
        setUpData()
        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
                name: "testDurationExpirationPublish",
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder,
                expirationPolicy: CommunicationMobileNotificationExpirationPolicy.DURATION
        )
        template = communicationMobileNotificationTemplateService.create( template )
        assertNotNull template.id
        assertFalse template.published

        assertCannotPublish( template, "mobileHeadlineFieldRequiredToPublish" )
        template.mobileHeadline = "a mobile headline"
        assertCannotPublish( template, "durationRequiredToPublish" )

        template.duration = 5
        template = communicationMobileNotificationTemplateService.publish( template )
        assertTrue template.published
    }


    @Test
    void testStickyPublish() {
        setUpData()
        CommunicationMobileNotificationTemplate template = new CommunicationMobileNotificationTemplate(
                name: "testNoExpirationPublish",
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder
        )
        template = communicationMobileNotificationTemplateService.create( template )
        assertNotNull template.id
        assertFalse template.published

        assertCannotPublish( template, "mobileHeadlineFieldRequiredToPublish" )
        template.mobileHeadline = "a mobile headline"
        template.sticky = true
        assertCannotPublish( template, "expirationRequiredForSticky" )

        template.expirationPolicy = CommunicationMobileNotificationExpirationPolicy.DURATION
        template.duration = 1
        template = communicationMobileNotificationTemplateService.publish( template )
        assertTrue template.published
    }


    private void assertCannotPublish( CommunicationTemplate template, String reason ) {
        setUpData()
        Boolean originalPublished = template.published
        try {
            communicationMobileNotificationTemplateService.publish( template )
            fail( reason )
        } catch (ApplicationException ae ) {
            assertApplicationException ae, reason
        } finally {
            template.published = originalPublished
        }
    }


}

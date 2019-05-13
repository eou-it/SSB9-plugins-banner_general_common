/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.textmessage

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import grails.web.servlet.context.GrailsWebApplicationContext
import groovy.time.DatumDependentDuration
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationExpirationPolicy
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
class CommunicationTextMessageTemplateServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationTemplateService
    def communicationTextMessageTemplateService
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
        defaultFolder = CommunicationManagementTestingSupport.newValidForCreateFolderWithSave("default folder")
    }

    @Test
    void testCreate() {
        setUpData()
        Date today = new Date()
        CommunicationTextMessageTemplate template = new CommunicationTextMessageTemplate(
                name: "testCreate",
                validFrom: today,
                folder: defaultFolder
        )

        template = communicationTextMessageTemplateService.create([domainModel: template])
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
        CommunicationTextMessageTemplate template = new CommunicationTextMessageTemplate(
                name: "testUpdateTemplate",
                validFrom: new Date(),
                validTo: null,
                folder: defaultFolder,
                message: "test message"
        )
        template = communicationTextMessageTemplateService.create([domainModel: template])
        assertNotNull template.id

        template.description = "Updated description"
        template = communicationTextMessageTemplateService.update([domainModel: template])
        assertEquals( "Updated description", template.description )
    }

}

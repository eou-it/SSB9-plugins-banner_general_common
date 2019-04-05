/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.mobile

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import grails.web.servlet.context.GrailsWebApplicationContext
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.folder.CommunicationFolderService
import net.hedtech.banner.general.communication.item.CommunicationChannel
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
 * Tests crud methods provided by communication mobile notification item service.
 */
@Integration
@Rollback
class CommunicationMobileNotificationItemServiceIntegrationTests extends BaseIntegrationTestCase {

    CommunicationMobileNotificationItemService communicationMobileNotificationItemService
    CommunicationMobileNotificationTemplateService communicationMobileNotificationTemplateService
    def selfServiceBannerAuthenticationProvider
    CommunicationFolderService communicationFolderService

    CommunicationFolder folder
    CommunicationMobileNotificationTemplate mobileNotificationTemplate

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
        folder = new CommunicationFolder()
        folder.name = "test folder"
        folder = (CommunicationFolder) communicationFolderService.create( folder )

        mobileNotificationTemplate = new CommunicationMobileNotificationTemplate()
        mobileNotificationTemplate.name = "mobile notification template"
        mobileNotificationTemplate.folder = folder
        mobileNotificationTemplate.headline = "headline"
        mobileNotificationTemplate.mobileHeadline = "mobileHeadline"
        mobileNotificationTemplate.messageDescription = "messageDescription"
        mobileNotificationTemplate = (CommunicationMobileNotificationTemplate) communicationMobileNotificationTemplateService.create( mobileNotificationTemplate )
        mobileNotificationTemplate = (CommunicationMobileNotificationTemplate) communicationMobileNotificationTemplateService.publish( mobileNotificationTemplate )
    }

    @Test
    void testCreate() {

        setUpData()
        Long recipientPidm = 49152
        String createdBy = 'MBRZYCKI'
        String referenceId = UUID.randomUUID().toString()

        CommunicationMobileNotificationItem mobileNotificationItem = new CommunicationMobileNotificationItem()
        mobileNotificationItem.serverResponse = "test Server Response"
        mobileNotificationItem.templateId = mobileNotificationTemplate.id
        mobileNotificationItem.referenceId = referenceId
        mobileNotificationItem.createdBy = createdBy
        mobileNotificationItem.recipientPidm = recipientPidm
        mobileNotificationItem = (CommunicationMobileNotificationItem) communicationMobileNotificationItemService.create( mobileNotificationItem )

        assertNotNull( mobileNotificationItem.id )
        assertEquals( CommunicationChannel.MOBILE_NOTIFICATION, mobileNotificationItem.communicationChannel )
        assertEquals( "test Server Response", mobileNotificationItem.serverResponse )
        assertEquals( mobileNotificationTemplate.id, mobileNotificationItem.templateId )
        assertEquals( referenceId, mobileNotificationItem.referenceId )
        assertEquals( createdBy, mobileNotificationItem.createdBy )
        assertEquals( recipientPidm, mobileNotificationItem.recipientPidm )

        assertNotNull( communicationMobileNotificationItemService.get( mobileNotificationItem.id ) )
    }

    @Test
    void testDelete() {

        setUpData()
        CommunicationMobileNotificationItem mobileNotificationItem = new CommunicationMobileNotificationItem()
        mobileNotificationItem.serverResponse = "test Server Response"
        mobileNotificationItem.templateId = mobileNotificationTemplate.id
        mobileNotificationItem.referenceId = UUID.randomUUID().toString()
        mobileNotificationItem.createdBy = 'MBRZYCKI'
        mobileNotificationItem.recipientPidm = 49152
        mobileNotificationItem = (CommunicationMobileNotificationItem) communicationMobileNotificationItemService.create( mobileNotificationItem )

        assertNotNull( communicationMobileNotificationItemService.get( mobileNotificationItem.id ) )

        communicationMobileNotificationItemService.delete( mobileNotificationItem )

        try {
            assertNull( communicationMobileNotificationItemService.get( mobileNotificationItem.id ) )
            fail "Expected NotFoundException"
        } catch (ApplicationException e) {
            assertEquals( new NotFoundException([id:mobileNotificationItem.id, entityClassName:mobileNotificationItem.getClass().getSimpleName()]).message, e.message )
        }
    }

}

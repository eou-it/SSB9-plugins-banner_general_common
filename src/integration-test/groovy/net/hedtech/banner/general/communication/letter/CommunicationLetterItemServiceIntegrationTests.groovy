/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.letter

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
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import static groovy.test.GroovyAssert.*

/**
 * Tests crud methods provided by communication letter item service.
 */
@Integration
@Rollback
class CommunicationLetterItemServiceIntegrationTests extends BaseIntegrationTestCase {

    CommunicationLetterItemService communicationLetterItemService
    CommunicationLetterTemplateService communicationLetterTemplateService
    def selfServiceBannerAuthenticationProvider
    CommunicationFolderService communicationFolderService

    CommunicationFolder folder
    CommunicationLetterTemplate letterTemplate

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

        letterTemplate = new CommunicationLetterTemplate()
        letterTemplate.name = "letterTemplate"
        letterTemplate.folder = folder
        letterTemplate.toAddress = "test to address"
        letterTemplate.content = "test content"
        letterTemplate = (CommunicationLetterTemplate) communicationLetterTemplateService.create( letterTemplate )
        letterTemplate = (CommunicationLetterTemplate) communicationLetterTemplateService.publish( letterTemplate )
    }

    @Test
    void testCreate() {
        setUpData()
        Long recipientPidm = 49152
        String createdBy = 'MBRZYCKI'
        String referenceId = UUID.randomUUID().toString()

        CommunicationLetterItem letterItem = new CommunicationLetterItem()
        letterItem.toAddress = "result to address"
        letterItem.content = "result content"
        letterItem.templateId = letterTemplate.id
        letterItem.referenceId = referenceId
        letterItem.createdBy = createdBy
        letterItem.recipientPidm = recipientPidm

        CommunicationLetterPageSettings extractStatement = new CommunicationLetterPageSettings()

        String json = extractStatement.toJson()
        letterItem.style = json
        letterItem = (CommunicationLetterItem) communicationLetterItemService.create( letterItem )

        assertNotNull( letterItem.id )
        assertEquals( CommunicationChannel.LETTER, letterItem.communicationChannel )
        assertEquals( "result to address", letterItem.toAddress )
        assertEquals( "result content", letterItem.content )
        assertEquals( letterTemplate.id, letterItem.templateId )
        assertEquals( referenceId, letterItem.referenceId )
        assertEquals( createdBy, letterItem.createdBy )
        assertEquals( recipientPidm, letterItem.recipientPidm )
        assertEquals(json,letterItem.style)

        assertNotNull( communicationLetterItemService.get( letterItem.id ) )
    }

    @Test
    void testDelete() {
        setUpData()
        CommunicationLetterItem letterItem = new CommunicationLetterItem()
        letterItem.toAddress = "result to address"
        letterItem.content = "result content"
        letterItem.templateId = letterTemplate.id
        letterItem.referenceId = UUID.randomUUID().toString()
        letterItem.createdBy = 'MBRZYCKI'
        letterItem.recipientPidm = 49152
        letterItem = (CommunicationLetterItem) communicationLetterItemService.create( letterItem )

        assertNotNull( communicationLetterItemService.get( letterItem.id ) )

        communicationLetterItemService.delete( letterItem )

        try {
            assertNull( communicationLetterItemService.get( letterItem.id ) )
            fail "Expected NotFoundException"
        } catch (ApplicationException e) {
            assertEquals( new NotFoundException([id:letterItem.id, entityClassName:letterItem.getClass().getSimpleName()]).message, e.message )
        }
    }

}

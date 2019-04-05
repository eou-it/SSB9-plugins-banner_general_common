/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.communication.event

import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerProperties
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerPropertiesType
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import grails.util.GrailsWebMockUtil
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import grails.web.servlet.context.GrailsWebApplicationContext

/**
 * Tests crud methods provided by event mapping service.
 */
@Integration
@Rollback
class CommunicationEventMappingServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationEventMappingService
    def communicationEmailTemplateService
    def communicationOrganizationService
    def communicationFolderService
    def communicationEmailServerPropertiesService
    def selfServiceBannerAuthenticationProvider


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

    @Test
    void testList() {
        long originalListCount = communicationEventMappingService.list().size()

        CommunicationFolder folder = new CommunicationFolder()
        folder.name = "test-integration"
        folder.description = "description"
        CommunicationFolder createdFolder = communicationFolderService.create(folder)
        assertNotNull(createdFolder)

        CommunicationOrganization organization = new CommunicationOrganization()
        organization.name = "testOrg"
        organization.description = "testOrg-description"
        organization.isAvailable = true

        def receiveProperties = new CommunicationEmailServerProperties(
                // Required fields
                host: "TTTTTTTTTT",
                port: 1234,
                type: CommunicationEmailServerPropertiesType.Receive
        )
        receiveProperties = communicationEmailServerPropertiesService.create(receiveProperties)

        def sendProperties = new CommunicationEmailServerProperties(
                // Required fields
                host: "TTTTTTTTTT",
                port: 1234,
                type: CommunicationEmailServerPropertiesType.Send
        )
        sendProperties = communicationEmailServerPropertiesService.create(sendProperties)

        organization.receiveEmailServerProperties = receiveProperties
        organization.sendEmailServerProperties = sendProperties
        CommunicationOrganization createdOrganization = communicationOrganizationService.create(organization)

        assertNotNull createdOrganization
        assertNotNull createdOrganization.id
        assertNotNull createdOrganization.receiveEmailServerProperties
        assertNotNull createdOrganization.sendEmailServerProperties
        assertTrue(createdOrganization.isAvailable)

        CommunicationEmailTemplate emailTemplate = new CommunicationEmailTemplate(
                name: "testEmailTemplate",
                folder: folder,
                personal: false,
                oneOff: false,
                subject: "test subject",
                toList: "test@test.edu",
                content: "This is a test email"
        )
        CommunicationEmailTemplate createdEmailTemplate = communicationEmailTemplateService.create(emailTemplate)
        assertNotNull createdEmailTemplate
        assertNotNull createdEmailTemplate?.id

        CommunicationEventMapping eventMapping = new CommunicationEventMapping()
        eventMapping.eventName = "test-integration"
        eventMapping.organization = createdOrganization
        eventMapping.template = createdEmailTemplate
        CommunicationEventMapping createdEventMapping = communicationEventMappingService.create(eventMapping)
        assertNotNull(createdEventMapping)
        assertNotNull createdEventMapping?.id

        long addedListCount = communicationEventMappingService.list().size()
        assertEquals(originalListCount + 1, addedListCount)
    }


    @Test
    void testCreate() {
        CommunicationFolder folder = new CommunicationFolder()
        folder.name = "test-integration"
        folder.description = "description"
        CommunicationFolder createdFolder = communicationFolderService.create(folder)
        assertNotNull(createdFolder)

        CommunicationOrganization organization = new CommunicationOrganization()
        organization.name = "testOrg"
        organization.description = "testOrg-description"
        organization.isAvailable = true

        def receiveProperties = new CommunicationEmailServerProperties(
                // Required fields
                host: "TTTTTTTTTT",
                port: 1234,
                type: CommunicationEmailServerPropertiesType.Receive
        )
        receiveProperties = communicationEmailServerPropertiesService.create(receiveProperties)

        def sendProperties = new CommunicationEmailServerProperties(
                // Required fields
                host: "TTTTTTTTTT",
                port: 1234,
                type: CommunicationEmailServerPropertiesType.Send
        )
        sendProperties = communicationEmailServerPropertiesService.create(sendProperties)

        organization.receiveEmailServerProperties = receiveProperties
        organization.sendEmailServerProperties = sendProperties
        CommunicationOrganization createdOrganization = communicationOrganizationService.create(organization)

        assertNotNull createdOrganization
        assertNotNull createdOrganization.id
        assertNotNull createdOrganization.receiveEmailServerProperties
        assertNotNull createdOrganization.sendEmailServerProperties
        assertTrue(createdOrganization.isAvailable)

        CommunicationEmailTemplate emailTemplate = new CommunicationEmailTemplate(
                name: "testEmailTemplate",
                folder: folder,
                personal: false,
                oneOff: false,
                subject: "test subject",
                toList: "test@test.edu",
                content: "This is a test email"
        )
        CommunicationEmailTemplate createdEmailTemplate = communicationEmailTemplateService.create(emailTemplate)
        assertNotNull createdEmailTemplate
        assertNotNull createdEmailTemplate?.id

        CommunicationEventMapping eventMapping = new CommunicationEventMapping()
        eventMapping.eventName = "test-integration"
        eventMapping.organization = createdOrganization
        eventMapping.template = createdEmailTemplate
        CommunicationEventMapping createdEventMapping = communicationEventMappingService.create(eventMapping)
        assertNotNull(createdEventMapping)
        assertNotNull createdEventMapping?.id

        CommunicationEventMapping foundEventMapping = CommunicationEventMapping.fetchByName("test-integration")
        assertEquals(createdEventMapping, foundEventMapping)

        CommunicationEventMapping sameEventMapping = new CommunicationEventMapping()
        sameEventMapping.eventName = "test-integration"
        try {
            communicationEventMappingService.create(sameEventMapping)
            Assert.fail "Expected sameNameFolder to fail because of name unique constraint."
        } catch (ApplicationException e) {
            assertTrue(e.getMessage().toString().contains("eventMappingExists"))

        }
    }

    @Test
    void testUpdate() {
        CommunicationFolder folder = new CommunicationFolder()
        folder.name = "test-integration"
        folder.description = "description"
        CommunicationFolder createdFolder = communicationFolderService.create(folder)
        assertNotNull(createdFolder)

        CommunicationOrganization organization = new CommunicationOrganization()
        organization.name = "testOrg"
        organization.description = "testOrg-description"
        organization.isAvailable = true

        def receiveProperties = new CommunicationEmailServerProperties(
                // Required fields
                host: "TTTTTTTTTT",
                port: 1234,
                type: CommunicationEmailServerPropertiesType.Receive
        )
        receiveProperties = communicationEmailServerPropertiesService.create(receiveProperties)

        def sendProperties = new CommunicationEmailServerProperties(
                // Required fields
                host: "TTTTTTTTTT",
                port: 1234,
                type: CommunicationEmailServerPropertiesType.Send
        )
        sendProperties = communicationEmailServerPropertiesService.create(sendProperties)

        organization.receiveEmailServerProperties = receiveProperties
        organization.sendEmailServerProperties = sendProperties
        CommunicationOrganization createdOrganization = communicationOrganizationService.create(organization)

        assertNotNull createdOrganization
        assertNotNull createdOrganization.id
        assertNotNull createdOrganization.receiveEmailServerProperties
        assertNotNull createdOrganization.sendEmailServerProperties
        assertTrue(createdOrganization.isAvailable)

        CommunicationEmailTemplate emailTemplate = new CommunicationEmailTemplate(
                name: "testEmailTemplate",
                folder: folder,
                personal: false,
                oneOff: false,
                subject: "test subject",
                toList: "test@test.edu",
                content: "This is a test email"
        )
        CommunicationEmailTemplate createdEmailTemplate = communicationEmailTemplateService.create(emailTemplate)
        assertNotNull createdEmailTemplate
        assertNotNull createdEmailTemplate?.id

        CommunicationEventMapping eventMapping = new CommunicationEventMapping()
        eventMapping.eventName = "test-integration"
        eventMapping.organization = createdOrganization
        eventMapping.template = createdEmailTemplate
        CommunicationEventMapping createdEventMapping = communicationEventMappingService.create(eventMapping)
        assertNotNull(createdEventMapping)
        assertNotNull createdEventMapping?.id

        createdEventMapping = CommunicationEventMapping.get(createdEventMapping.getId())
        assertNotNull createdEventMapping.id
        //Create a second template
        CommunicationEmailTemplate emailTemplate2 = new CommunicationEmailTemplate(
                name: "testEmailTemplate2",
                folder: folder,
                personal: false,
                oneOff: false,
                subject: "test subject 2",
                toList: "test@test.edu",
                content: "This is the second test email"
        )

        CommunicationEmailTemplate createdEmailTemplate2 = communicationEmailTemplateService.create(emailTemplate2)
        assertNotNull createdEmailTemplate2?.id

        createdEventMapping.template = createdEmailTemplate2
        CommunicationEventMapping updatedEventMapping = communicationEventMappingService.update(createdEventMapping)
        assertNotNull updatedEventMapping.id
        assertEquals(updatedEventMapping.template.id, createdEmailTemplate2.id)



        CommunicationEventMapping eventMapping2 = new CommunicationEventMapping()
        eventMapping2.eventName = "test-integration-new"
        eventMapping2.organization = createdOrganization
        eventMapping2.template = createdEmailTemplate
        CommunicationEventMapping createdEventMapping2 = communicationEventMappingService.create(eventMapping2)
        assertNotNull createdEventMapping2.id

        createdEventMapping2.eventName = createdEventMapping.eventName
        try {
            communicationEventMappingService.update(createdEventMapping2)
            Assert.fail "Expected sameNameFolder to fail because of name unique constraint."
        } catch (ApplicationException e) {
            assertEquals("@@r1:eventMappingExists:" + createdEventMapping.eventName  + "@@", e.message)
        }
    }

    @Test
    void testDelete() {
        CommunicationFolder folder = new CommunicationFolder()
        folder.name = "test-integration"
        folder.description = "description"
        CommunicationFolder createdFolder = communicationFolderService.create(folder)
        assertNotNull(createdFolder)

        CommunicationOrganization organization = new CommunicationOrganization()
        organization.name = "testOrg"
        organization.description = "testOrg-description"
        organization.isAvailable = true

        def receiveProperties = new CommunicationEmailServerProperties(
                // Required fields
                host: "TTTTTTTTTT",
                port: 1234,
                type: CommunicationEmailServerPropertiesType.Receive
        )
        receiveProperties = communicationEmailServerPropertiesService.create(receiveProperties)

        def sendProperties = new CommunicationEmailServerProperties(
                // Required fields
                host: "TTTTTTTTTT",
                port: 1234,
                type: CommunicationEmailServerPropertiesType.Send
        )
        sendProperties = communicationEmailServerPropertiesService.create(sendProperties)

        organization.receiveEmailServerProperties = receiveProperties
        organization.sendEmailServerProperties = sendProperties
        CommunicationOrganization createdOrganization = communicationOrganizationService.create(organization)

        assertNotNull createdOrganization
        assertNotNull createdOrganization.id
        assertNotNull createdOrganization.receiveEmailServerProperties
        assertNotNull createdOrganization.sendEmailServerProperties
        assertTrue(createdOrganization.isAvailable)

        CommunicationEmailTemplate emailTemplate = new CommunicationEmailTemplate(
                name: "testEmailTemplate",
                folder: folder,
                personal: false,
                oneOff: false,
                subject: "test subject",
                toList: "test@test.edu",
                content: "This is a test email"
        )
        CommunicationEmailTemplate createdEmailTemplate = communicationEmailTemplateService.create(emailTemplate)
        assertNotNull createdEmailTemplate
        assertNotNull createdEmailTemplate?.id

        CommunicationEventMapping eventMapping = new CommunicationEventMapping()
        eventMapping.eventName = "test-integration"
        eventMapping.organization = createdOrganization
        eventMapping.template = createdEmailTemplate
        CommunicationEventMapping createdEventMapping = communicationEventMappingService.create(eventMapping)
        assertNotNull(createdEventMapping)
        assertNotNull createdEventMapping?.id

        createdEventMapping = CommunicationEventMapping.get(createdEventMapping.getId())
        assertNotNull createdEventMapping.id

        Long id = createdEventMapping.getId()

        long count = communicationEventMappingService.list().size()

        communicationEventMappingService.delete(createdEventMapping)

        assertEquals(count - 1, communicationEventMappingService.list().size())

        try {
            assertNull(communicationEventMappingService.get(id))
            Assert.fail "Expected get by id to fail because does not exist."
        } catch (ApplicationException e) {
            assertEquals("NotFoundException", e.getType())
        }
    }
}


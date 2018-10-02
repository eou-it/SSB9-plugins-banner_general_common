/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.recurrence

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerProperties
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerPropertiesType
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.population.CommunicationPopulation
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Tests crud methods provided by communication recurrent message service.
 */
class CommunicationRecurrentMessageServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationRecurrentMessageService
    def communicationPopulationService
    def communicationEmailTemplateService
    def communicationOrganizationService
    def communicationFolderService
    def communicationEmailServerPropertiesService
    def selfServiceBannerAuthenticationProvider


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testList() {
        long originalListCount = communicationRecurrentMessageService.list().size()

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

        def population = newPopulation("TEST" )
        population.folder = createdFolder
        population = communicationPopulationService.create( [domainModel: population] )

        assertNotNull population?.id
        assertEquals createdFolder.name, population.folder.name
        assertEquals "BCMADMIN", population.createdBy
        assertEquals "Population Description", population.description
        assertEquals "TEST", population.name
        assertFalse population.systemIndicator

        def currentTime = Calendar.getInstance().getTime()
        CommunicationRecurrentMessage recurrentMessage = new CommunicationRecurrentMessage()
        recurrentMessage.name = "Send Recurrence"
        recurrentMessage.description = "Recurrence Message test case"
        recurrentMessage.startDate = currentTime
        recurrentMessage.cronExpression = "* * * * * ? *"
        recurrentMessage.cronTimezone = Calendar.getInstance().getTimeZone().getID()
        recurrentMessage.createdBy = "BCMADMIN"
        recurrentMessage.creationDateTime = currentTime
        recurrentMessage.recalculateOnSend = false
        recurrentMessage.totalCount =0
        recurrentMessage.successCount =0
        recurrentMessage.failureCount =0
        recurrentMessage.organizationId = createdOrganization.id
        recurrentMessage.templateId = createdEmailTemplate.id
        recurrentMessage.populationId = population.id

        CommunicationRecurrentMessage createdRecurrentMessage = communicationRecurrentMessageService.create(recurrentMessage)
        assertNotNull(createdRecurrentMessage)
        assertNotNull createdRecurrentMessage?.id

        long addedListCount = communicationRecurrentMessageService.list().size()
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

        def population = newPopulation("TEST" )
        population.folder = createdFolder
        population = communicationPopulationService.create( [domainModel: population] )

        assertNotNull population?.id
        assertEquals createdFolder.name, population.folder.name
        assertEquals "BCMADMIN", population.createdBy
        assertEquals "Population Description", population.description
        assertEquals "TEST", population.name
        assertFalse population.systemIndicator

        def currentTime = Calendar.getInstance().getTime()
        CommunicationRecurrentMessage recurrentMessage = new CommunicationRecurrentMessage()
        recurrentMessage.name = "Send Recurrence"
        recurrentMessage.description = "Recurrence Message test case"
        recurrentMessage.startDate = currentTime
        recurrentMessage.cronExpression = "* * * * * ? *"
        recurrentMessage.cronTimezone = Calendar.getInstance().getTimeZone().getID()
        recurrentMessage.createdBy = "BCMADMIN"
        recurrentMessage.creationDateTime = currentTime
        recurrentMessage.recalculateOnSend = false
        recurrentMessage.totalCount =0
        recurrentMessage.successCount =0
        recurrentMessage.failureCount =0
        recurrentMessage.organizationId = createdOrganization.id
        recurrentMessage.templateId = createdEmailTemplate.id
        recurrentMessage.populationId = population.id

        CommunicationRecurrentMessage createdRecurrentMessage = communicationRecurrentMessageService.create(recurrentMessage)
        assertNotNull(createdRecurrentMessage)
        assertNotNull createdRecurrentMessage?.id
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

        def population = newPopulation("TEST" )
        population.folder = createdFolder
        population = communicationPopulationService.create( [domainModel: population] )

        assertNotNull population?.id
        assertEquals createdFolder.name, population.folder.name
        assertEquals "BCMADMIN", population.createdBy
        assertEquals "Population Description", population.description
        assertEquals "TEST", population.name
        assertFalse population.systemIndicator

        def currentTime = Calendar.getInstance().getTime()
        CommunicationRecurrentMessage recurrentMessage = new CommunicationRecurrentMessage()
        recurrentMessage.name = "Send Recurrence"
        recurrentMessage.description = "Recurrence Message test case"
        recurrentMessage.startDate = currentTime
        recurrentMessage.cronExpression = "* * * * * ? *"
        recurrentMessage.cronTimezone = Calendar.getInstance().getTimeZone().getID()
        recurrentMessage.createdBy = "BCMADMIN"
        recurrentMessage.creationDateTime = currentTime
        recurrentMessage.recalculateOnSend = false
        recurrentMessage.totalCount =0
        recurrentMessage.successCount =0
        recurrentMessage.failureCount =0
        recurrentMessage.organizationId = createdOrganization.id
        recurrentMessage.templateId = createdEmailTemplate.id
        recurrentMessage.populationId = population.id

        CommunicationRecurrentMessage createdRecurrentMessage = communicationRecurrentMessageService.create(recurrentMessage)
        assertNotNull(createdRecurrentMessage)
        assertNotNull createdRecurrentMessage?.id

        createdRecurrentMessage = CommunicationRecurrentMessage.get(createdRecurrentMessage.getId())
        assertNotNull createdRecurrentMessage.id
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

        createdRecurrentMessage.templateId = createdEmailTemplate2.id
        CommunicationRecurrentMessage updatedRecurrentMessage = communicationRecurrentMessageService.update(createdRecurrentMessage)
        assertNotNull updatedRecurrentMessage.id
        assertEquals(updatedRecurrentMessage.templateId, createdEmailTemplate2.id)

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

        def population = newPopulation("TEST" )
        population.folder = createdFolder
        population = communicationPopulationService.create( [domainModel: population] )

        assertNotNull population?.id
        assertEquals createdFolder.name, population.folder.name
        assertEquals "BCMADMIN", population.createdBy
        assertEquals "Population Description", population.description
        assertEquals "TEST", population.name
        assertFalse population.systemIndicator

        def currentTime = Calendar.getInstance().getTime()
        CommunicationRecurrentMessage recurrentMessage = new CommunicationRecurrentMessage()
        recurrentMessage.name = "Send Recurrence"
        recurrentMessage.description = "Recurrence Message test case"
        recurrentMessage.startDate = currentTime
        recurrentMessage.cronExpression = "* * * * * ? *"
        recurrentMessage.cronTimezone = Calendar.getInstance().getTimeZone().getID()
        recurrentMessage.createdBy = "BCMADMIN"
        recurrentMessage.creationDateTime = currentTime
        recurrentMessage.recalculateOnSend = false
        recurrentMessage.totalCount =0
        recurrentMessage.successCount =0
        recurrentMessage.failureCount =0
        recurrentMessage.organizationId = createdOrganization.id
        recurrentMessage.templateId = createdEmailTemplate.id
        recurrentMessage.populationId = population.id

        CommunicationRecurrentMessage createdRecurrentMessage = communicationRecurrentMessageService.create(recurrentMessage)
        assertNotNull(createdRecurrentMessage)
        assertNotNull createdRecurrentMessage?.id

        createdRecurrentMessage = CommunicationRecurrentMessage.get(createdRecurrentMessage.getId())
        assertNotNull createdRecurrentMessage.id

        Long id = createdRecurrentMessage.getId()

        long count = communicationRecurrentMessageService.list().size()

        communicationRecurrentMessageService.delete(createdRecurrentMessage)

        assertEquals(count - 1, communicationRecurrentMessageService.list().size())

        try {
            assertNull(communicationRecurrentMessageService.get(id))
            Assert.fail "Expected get by id to fail because does not exist."
        } catch (ApplicationException e) {
            assertEquals("NotFoundException", e.getType())
        }
    }

    private def newPopulation(String populationName) {
        def population = new CommunicationPopulation(
                // Required fields
                name: populationName,
                systemIndicator: false,
                // Nullable fields
                description: "Population Description",
        )
        return population
    }
}

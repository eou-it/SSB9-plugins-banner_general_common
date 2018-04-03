/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.communication.event

import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerProperties
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerPropertiesType
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Event Mapping Tests
 */
class CommunicationEventMappingIntegrationTests  extends BaseIntegrationTestCase {

    def i_valid_folder_name = "My Folder"
    def i_valid_folder_description = "My Folder"
    def i_valid_folder_internal = true

    def i_valid_org_name = "My Organization"
    def i_valid_org_description = "My Organization"

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
    void testCreateValidEventMapping() {
        //Create a folder
        def folder = newValidForCreateFolder()
        folder.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull folder.id
        assertFalse folder.systemIndicator

        //Create an organization
        def organization = newValidForCreateOrganization()
        organization.isAvailable = true
        def receiveProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Receive )
        def sendProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Send )
        organization.receiveEmailServerProperties = receiveProperties
        organization.sendEmailServerProperties = sendProperties
        organization.save(failOnError: true, flush: true)
        assertNotNull organization.receiveEmailServerProperties
        assertNotNull organization.sendEmailServerProperties
        assertNull(organization.parent)
        assertTrue(organization.isAvailable)

        //Create a template
        CommunicationEmailTemplate emailTemplate = new CommunicationEmailTemplate(
                name: "testEmailTemplate",
                folder: folder,
                personal: false,
                oneOff: false,
                subject: "test subject",
                toList: "test@test.edu",
                content: "This is a test email"
        )

        emailTemplate = emailTemplate.save(failOnError: true, flush: true)
        assertNotNull emailTemplate?.id

        //Create an event mapping
        CommunicationEventMapping eventMapping = new CommunicationEventMapping(
                eventName: "TEST_EVENT",
                organization: organization,
                template: emailTemplate
        )
        eventMapping.save(failOnError: true, flush: true)
        assertNotNull eventMapping?.id
    }

    @Test
    void testUpdateValidEventMapping() {
        //Create a folder
        def folder = newValidForCreateFolder()
        folder.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull folder.id
        assertFalse folder.systemIndicator

        //Create an organization
        def organization = newValidForCreateOrganization()
        organization.isAvailable = true
        def receiveProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Receive )
        def sendProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Send )
        organization.receiveEmailServerProperties = receiveProperties
        organization.sendEmailServerProperties = sendProperties
        organization.save(failOnError: true, flush: true)
        assertNotNull organization.receiveEmailServerProperties
        assertNotNull organization.sendEmailServerProperties
        assertNull(organization.parent)
        assertTrue(organization.isAvailable)

        //Create a template
        CommunicationEmailTemplate emailTemplate = new CommunicationEmailTemplate(
                name: "testEmailTemplate1",
                folder: folder,
                personal: false,
                oneOff: false,
                subject: "test subject 1",
                toList: "test@test.edu",
                content: "This is the first test email"
        )

        emailTemplate = emailTemplate.save(failOnError: true, flush: true)
        assertNotNull emailTemplate?.id

        //Create an event mapping
        CommunicationEventMapping eventMapping = new CommunicationEventMapping(
                eventName: "TEST_EVENT",
                organization: organization,
                template: emailTemplate
        )
        eventMapping.save(failOnError: true, flush: true)
        assertNotNull eventMapping?.id

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

        emailTemplate2 = emailTemplate2.save(failOnError: true, flush: true)
        assertNotNull emailTemplate2?.id

        eventMapping.template = emailTemplate2
        eventMapping.save()
        def updatedEventMapping = eventMapping.get(eventMapping.id)
        assertEquals (updatedEventMapping.template.id,emailTemplate2.id)
    }

    @Test
    void testExistsAnotherSameNameEvent() {
//Create a folder
        def folder = newValidForCreateFolder()
        folder.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull folder.id
        assertFalse folder.systemIndicator

        //Create an organization
        def organization = newValidForCreateOrganization()
        organization.isAvailable = true
        def receiveProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Receive )
        def sendProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Send )
        organization.receiveEmailServerProperties = receiveProperties
        organization.sendEmailServerProperties = sendProperties
        organization.save(failOnError: true, flush: true)
        assertNotNull organization.receiveEmailServerProperties
        assertNotNull organization.sendEmailServerProperties
        assertNull(organization.parent)
        assertTrue(organization.isAvailable)

        //Create a template
        CommunicationEmailTemplate emailTemplate = new CommunicationEmailTemplate(
                name: "testEmailTemplate1",
                folder: folder,
                personal: false,
                oneOff: false,
                subject: "test subject 1",
                toList: "test@test.edu",
                content: "This is the first test email"
        )

        emailTemplate = emailTemplate.save(failOnError: true, flush: true)
        assertNotNull emailTemplate?.id

        //Create an event mapping
        CommunicationEventMapping eventMapping = new CommunicationEventMapping(
                eventName: "TEST_EVENT",
                organization: organization,
                template: emailTemplate
        )
        eventMapping.save(failOnError: true, flush: true)
        assertNotNull eventMapping?.id

        Boolean falseResult = CommunicationEventMapping.existsAnotherSameNameEvent(eventMapping.id, eventMapping.eventName)
        assertFalse(falseResult)


        //Create an event mapping
        CommunicationEventMapping eventMapping2 = new CommunicationEventMapping(
                eventName: "TEST_EVENT2",
                organization: organization,
                template: emailTemplate
        )
        eventMapping2.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull eventMapping2?.id

        Boolean trueResult = CommunicationEventMapping.existsAnotherSameNameEvent(eventMapping.id, eventMapping2.eventName)
        assertTrue(trueResult)
    }

    @Test
    void testFetchByName() {
        //Create a folder
        def folder = newValidForCreateFolder()
        folder.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull folder.id
        assertFalse folder.systemIndicator

        //Create an organization
        def organization = newValidForCreateOrganization()
        organization.isAvailable = true
        def receiveProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Receive )
        def sendProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Send )
        organization.receiveEmailServerProperties = receiveProperties
        organization.sendEmailServerProperties = sendProperties
        organization.save(failOnError: true, flush: true)
        assertNotNull organization.receiveEmailServerProperties
        assertNotNull organization.sendEmailServerProperties
        assertNull(organization.parent)
        assertTrue(organization.isAvailable)

        //Create a template
        CommunicationEmailTemplate emailTemplate = new CommunicationEmailTemplate(
                name: "testEmailTemplate",
                folder: folder,
                personal: false,
                oneOff: false,
                subject: "test subject",
                toList: "test@test.edu",
                content: "This is a test email"
        )

        emailTemplate = emailTemplate.save(failOnError: true, flush: true)
        assertNotNull emailTemplate?.id

        //Create an event mapping
        CommunicationEventMapping eventMapping = new CommunicationEventMapping(
                eventName: "TEST_EVENT",
                organization: organization,
                template: emailTemplate
        )
        eventMapping.save(failOnError: true, flush: true)
        assertNotNull eventMapping?.id

        CommunicationEventMapping eventMapping2 = CommunicationEventMapping.fetchByName("TEST_EVENT")
        assertEquals(eventMapping.id, eventMapping2.id)
    }

    private def newValidForCreateFolder() {
        def folder = new CommunicationFolder(
                description: i_valid_folder_description,
                internal: i_valid_folder_internal,
                name: i_valid_folder_name
        )
        return folder
    }

    private def newValidForCreateOrganization() {
        def organization = new CommunicationOrganization(
                description: i_valid_org_description,
                name: i_valid_org_name
        )
        return organization
    }

    private def newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType serverType) {
        def communicationEmailServerProperties = new CommunicationEmailServerProperties(
                // Required fields
                host: "TTTTTTTTTT",
                port: 1234,
                type: serverType
        )
        communicationEmailServerProperties.save()
        return communicationEmailServerProperties
    }
}

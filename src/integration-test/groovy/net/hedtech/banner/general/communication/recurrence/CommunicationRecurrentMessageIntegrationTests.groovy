/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.recurrence

import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerProperties
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerPropertiesType
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.population.CommunicationPopulation
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests basic CRUD operations on an CommunicationRecurrentMessage entity object
 * and any field level validation.
 */
class CommunicationRecurrentMessageIntegrationTests extends BaseIntegrationTestCase {

    def i_valid_folder_name = "My Folder"
    def i_valid_folder_description = "My Folder"
    def i_valid_folder_internal = true

    def i_valid_org_name = "My Organization"
    def i_valid_org_description = "My Organization"

    // template
    def i_valid_emailTemplate_name = """Valid Name"""
    def i_valid_emailTemplate_description = """Valid Template Description"""
    def i_valid_emailTemplate_personal = false
    def i_valid_emailTemplate_bccList = """Valid Emailtemplate Bcclist"""
    def i_valid_emailTemplate_ccList = """Valid Emailtemplate Cclist"""
    def i_valid_emailTemplate_content = """Valid Emailtemplate Content"""
    def i_valid_emailTemplate_fromList = """Valid Emailtemplate Fromlist"""
    def i_valid_emailTemplate_subject = """Valid Emailtemplate Subject"""
    def i_valid_emailTemplate_toList = """Valid Emailtemplate Tolist"""
    def i_valid_emailTemplate_dataOrigin = """Valid Emailtemplate Dataorigin"""
    def i_valid_emailTemplate_oneOff = true
    def i_valid_emailTemplate_published = true
    def i_valid_emailTemplate_validFrom = new Date()
    def i_valid_emailTemplate_validTo = new Date()
    def i_valid_emailTemplate_createdBy = """Valid EmailTemplate createdBy"""
    def i_valid_emailTemplate_createDate = new Date()

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
    void testCreateRecurrentMessage() {

        //Create a folder
        def folder = newValidForCreateFolder("TEST")
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

        def emailTemplate = newValidForCreateEmailTemplate( folder )
        emailTemplate.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull emailTemplate.id

        def population = newPopulation("Recurrent Test")
        population.folder = folder
        population.save(failOnError: true, flush: true)
        assertNotNull population.id

        def recurrentMessage = newValidForCreateRecurrentMessage()
        recurrentMessage.organizationId = organization.id
        recurrentMessage.templateId = emailTemplate.id
        recurrentMessage.populationId = population.id
        recurrentMessage.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull recurrentMessage.id
    }


    @Test
    void testDeleteRecurrentMessage() {

        //Create a folder
        def folder = newValidForCreateFolder("TEST")
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

        def emailTemplate = newValidForCreateEmailTemplate( folder )
        emailTemplate.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull emailTemplate.id

        def population = newPopulation("Recurrent Test")
        population.folder = folder
        population.save(failOnError: true, flush: true)
        assertNotNull population.id

        def recurrentMessage = newValidForCreateRecurrentMessage()
        recurrentMessage.organizationId = organization.id
        recurrentMessage.templateId = emailTemplate.id
        recurrentMessage.populationId = population.id
        recurrentMessage.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull recurrentMessage.id

        recurrentMessage.delete()
        def id = recurrentMessage.id
        assertNull recurrentMessage.get(id)
    }

    private def newValidForCreateFolder( String folderName ) {
        def folder = new CommunicationFolder(
                description: i_valid_folder_description,
                internal: i_valid_folder_internal,
                name: folderName,
                lastModifiedBy: "BCMADMIN",
                lastModified: new Date()
        )
        return folder
    }

    private def newValidForCreateEmailTemplate( CommunicationFolder folder ) {
        def emailTemplate = new CommunicationEmailTemplate(
                description: i_valid_emailTemplate_description,
                personal: i_valid_emailTemplate_personal,
                name: i_valid_emailTemplate_name,
                oneOff: i_valid_emailTemplate_oneOff,
                published: i_valid_emailTemplate_published,
                createdBy: i_valid_emailTemplate_createdBy,
                createDate: i_valid_emailTemplate_createDate,
                validFrom: i_valid_emailTemplate_validFrom,
                validTo: i_valid_emailTemplate_validTo,
                folder: folder,
                bccList: i_valid_emailTemplate_bccList,
                ccList: i_valid_emailTemplate_ccList,
                content: i_valid_emailTemplate_content,
                fromList: i_valid_emailTemplate_fromList,
                subject: i_valid_emailTemplate_subject,
                toList: i_valid_emailTemplate_toList,
                dataOrigin: i_valid_emailTemplate_dataOrigin,
        )
    }

    private def newPopulation(String populationName) {
        def population = new CommunicationPopulation(
                // Required fields
                name: populationName,
                createDate: new Date(),
                createdBy: "BCMADMIN",
                systemIndicator: false,
                // Nullable fields
                description: "Population Description",
        )
        return population
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

    private def newValidForCreateRecurrentMessage() {

        def currentTime = Calendar.getInstance().getTime()
        def recurrentMessage = new CommunicationRecurrentMessage(
                name: "Send Recurrence",
                description: "Recurrence Message test case",
                startDate: currentTime,
                cronExpression: "* * * * * ? *",
                cronTimezone: Calendar.getInstance().getTimeZone().getID(),
                createdBy: "BCMADMIN",
                creationDateTime: currentTime,
                recalculateOnSend: false,
                totalCount: 0,
                successCount: 0,
                failureCount: 0
        )
        return recurrentMessage
    }
}

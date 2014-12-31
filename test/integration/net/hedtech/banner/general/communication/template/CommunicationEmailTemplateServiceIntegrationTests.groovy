/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.template

import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Tests crud methods provided by communication template service.
 */
class CommunicationEmailTemplateServiceIntegrationTests extends BaseIntegrationTestCase {
    def i_valid_emailTemplate_active = true

    def i_valid_emailTemplate_bccList = """Valid Emailtemplate Bcclist"""
    def i_invalid_emailTemplate_bccList = "foo@bar.com".padLeft(1021)

    def i_valid_emailTemplate_ccList = """Valid Emailtemplate Cclist"""
    def i_invalid_emailTemplate_ccList = "foo@bar.com".padLeft(1021)

    def i_valid_emailTemplate_content = """Valid Emailtemplate Content"""

    def i_valid_emailTemplate_createDate = new Date()

    def i_valid_emailTemplate_createdBy = """Valid EmailTemplate createdBy"""
    def i_invalid_emailTemplate_createdBy = """Valid EmailTemplate createdBy""".padLeft(31)

    def i_valid_emailTemplate_dataOrigin = """Valid Emailtemplate Dataorigin"""
    def i_invalid_emailTemplate_dataOrigin = "XE Communication Manager".padLeft(31)

    def i_valid_emailTemplate_description = """Valid Template Description"""
    def i_invalid_emailTemplate_description = """Valid Template Description""".padLeft(4001)

    def i_valid_emailTemplate_fromList = """Valid Emailtemplate Fromlist"""
    def i_invalid_emailTemplate_fromList = "foo@bar.com".padLeft(1021)

    def i_valid_emailTemplate_lastModified = new Date()

    def i_valid_emailTemplate_lastModifiedBy = """Valid Emailtemplate Lastmodifiedby"""
    def i_invalid_emailTemplate_lastModifiedBy = "BCMUSER".padLeft(31)

    def i_valid_emailTemplate_name = """Valid Name"""
    def i_invalid_emailTemplate_name = """Valid Name""".padLeft(2049)

    def i_valid_emailTemplate_oneOff = true
    def i_valid_emailTemplate_personal = false
    def i_valid_emailTemplate_published = true

    def i_valid_emailTemplate_subject = """Valid Emailtemplate Subject"""
    def i_invalid_emailTemplate_subject = """You're a winner!""".padLeft(1021)

    def i_valid_emailTemplate_toList = """Valid Emailtemplate Tolist"""
    def i_invalid_emailTemplate_toList = "foo@bar.com".padLeft(1021)

    def i_valid_emailTemplate_validFrom = new Date()
    def i_valid_emailTemplate_validTo = new Date() + 200

    def i_valid_folder_description = "Valid older description"
    def i_valid_folder_internal = true
    def i_valid_folder_name1 = "Valid Folder1 Name"
    def i_valid_folder_name2 = "Valid Folder2 Name"


    def communicationEmailTemplateService
    def selfServiceBannerAuthenticationProvider

    def CommunicationFolder folder1
    def CommunicationFolder folder2


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()

        folder1 = newValidForCreateFolder(i_valid_folder_name1)
        folder1.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull folder1.id
        folder2 = newValidForCreateFolder(i_valid_folder_name2)
        folder2.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull folder2.id

    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateTemplate() {
        def originalListCount = communicationEmailTemplateService.list().size()
        def template = newValidForCreateEmailTemplate(folder1)
        def newTemplate = communicationEmailTemplateService.create([domainModel: template])
        //Test if the service set the created date, and the infrastructure set the modifiedby and date
        assertNotNull newTemplate.createDate
        assertNotNull(newTemplate.lastModified)
        assertNotNull(newTemplate.lastModifiedBy)

        // Now test findall
        def foundEmailTemplates = communicationEmailTemplateService.findAll()
        assertEquals(1, foundEmailTemplates.size())
    }

    @Test
    void testPublishTemplate() {
        def originalListCount = communicationEmailTemplateService.list().size()
        def template = newValidForCreateEmailTemplate(folder1)
        def newTemplate = communicationEmailTemplateService.create([domainModel: template])
        //Test if the service set the created date, and the infrastructure set the modifiedby and date
        assertNotNull newTemplate.createDate
        assertNotNull(newTemplate.lastModified)
        assertNotNull(newTemplate.lastModifiedBy)
        assertFalse(newTemplate.published)

        newTemplate.toList = null
        def template1 = communicationEmailTemplateService.update([domainModel:newTemplate])
        assertNull(template1.toList)
        shouldFail {
            def notPublished = communicationEmailTemplateService.publishTemplate([id:template1.id])
        }

        template1.toList = "TOLIST"
        def template2 = communicationEmailTemplateService.update([domainModel:newTemplate])

        def pubtemp = communicationEmailTemplateService.publishTemplate(["id":newTemplate.id])
        assertTrue(pubtemp.published)

        // Now test findall
        def foundEmailTemplates = communicationEmailTemplateService.findAll()
        assertEquals(1, foundEmailTemplates.size())
    }


    @Test
    void testUpdateTemplate() {
        def template = newValidForCreateEmailTemplate(folder1)
        def newTemplate = communicationEmailTemplateService.create([domainModel: template])
        //Test if the generated entity now has an id assigned
        assertNotNull newTemplate.id
        newTemplate.description = "Updated description"
        def updatedTemplate = communicationEmailTemplateService.update([domainModel: newTemplate])
        assertEquals("Updated description", updatedTemplate.description)
    }


    @Test
    void testUpdateTemplateFolder() {
        def template = newValidForCreateEmailTemplate(folder1)
        def newTemplate = communicationEmailTemplateService.create([domainModel: template])
        //Test if the generated entity now has an id assigned
        assertNotNull newTemplate.id
        newTemplate.folder = folder2
        def updatedTemplate = communicationEmailTemplateService.update([domainModel: newTemplate])
        assertEquals(i_valid_folder_name2, updatedTemplate.folder.name)
    }


    @Test
    void testDeleteTemplate() {
        def originalListCount = communicationEmailTemplateService.list().size()
        def template = newValidForCreateEmailTemplate(folder1)
        def createdTemplate = communicationEmailTemplateService.create([domainModel: template])
        //Test if the generated entity now has an id assigned
        assertNotNull createdTemplate.id

        //delete the newly created template and check if count is back to original
        communicationEmailTemplateService.delete(createdTemplate)
        def createdId = createdTemplate.id
        shouldFail
                {
                    (communicationEmailTemplateService.get(createdId))
                }
    }


    @Test
    void testCreateInValidCommunicationTemplate() {
        def communicationTemplate = newValidForCreateEmailTemplate(folder1)

        communicationTemplate.name = i_invalid_emailTemplate_name
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }

        communicationTemplate = newValidForCreateEmailTemplate(folder1)
        communicationTemplate.description = i_invalid_emailTemplate_description
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }

        communicationTemplate = newValidForCreateEmailTemplate(folder1)
        communicationTemplate.bccList = i_invalid_emailTemplate_bccList
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }

        communicationTemplate = newValidForCreateEmailTemplate(folder1)
        communicationTemplate.ccList = i_invalid_emailTemplate_ccList
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }

        communicationTemplate = newValidForCreateEmailTemplate(folder1)
        communicationTemplate.createdBy = i_invalid_emailTemplate_createdBy
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }

        communicationTemplate = newValidForCreateEmailTemplate(folder1)
        communicationTemplate.dataOrigin = i_invalid_emailTemplate_dataOrigin
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }

        communicationTemplate = newValidForCreateEmailTemplate(folder1)
        communicationTemplate.fromList = i_invalid_emailTemplate_fromList
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }

        communicationTemplate = newValidForCreateEmailTemplate(folder1)
        communicationTemplate.subject = i_invalid_emailTemplate_subject
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }

        communicationTemplate = newValidForCreateEmailTemplate(folder1)
        communicationTemplate.toList = i_invalid_emailTemplate_toList
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }


    }

    @Test
    void testFetchPublishedActivePublicByFolderName() {

        def emailTemplate = newValidForCreateEmailTemplate( folder1 )
        emailTemplate.save( failOnError: true, flush: true )
        assertNotNull( emailTemplate.folder.name )
        def emailTemplates = CommunicationEmailTemplate.fetchPublishedActivePublicByFolderName( folder1.name )
        assertEquals( 1, emailTemplates.size() )
    }

    private def newValidForCreateFolder(String folderName) {
        def folder = new CommunicationFolder(
                description: i_valid_folder_description,
                internal: i_valid_folder_internal,
                name: folderName
        )
        return folder
    }


    private def newValidForCreateEmailTemplate(CommunicationFolder folder) {
        def communicationTemplate = new CommunicationEmailTemplate(
                description: i_valid_emailTemplate_description,
                personal: i_valid_emailTemplate_personal,
                name: i_valid_emailTemplate_name,
                active: i_valid_emailTemplate_active,
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

        return communicationTemplate
    }

}

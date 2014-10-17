/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.item

import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CommunicationEmailItemIntegrationTests extends BaseIntegrationTestCase {
    //folder
    def i_valid_folder_name = "Valid Folder Nname"
    def i_valid_folder_description = "Valid older description"
    def i_valid_folder_internal = true

    // template
    def i_valid_emailTemplate_name = """Valid Name"""
    def i_valid_emailTemplate_description = """Valid Template Description"""
    def i_valid_emailTemplate_personal = true
    def i_valid_emailTemplate_bccList = """Valid Emailtemplate Bcclist"""
    def i_valid_emailTemplate_ccList = """Valid Emailtemplate Cclist"""
    def i_valid_emailTemplate_content = """Valid Emailtemplate Content"""
    def i_valid_emailTemplate_fromList = """Valid Emailtemplate Fromlist"""
    def i_valid_emailTemplate_subject = """Valid Emailtemplate Subject"""
    def i_valid_emailTemplate_toList = """Valid Emailtemplate Tolist"""
    def i_valid_emailTemplate_lastModifiedBy = """Valid Emailtemplate Lastmodifiedby"""
    def i_valid_emailTemplate_lastModified = new Date()
    def i_valid_emailTemplate_dataOrigin = """Valid Emailtemplate Dataorigin"""
    def i_valid_emailTemplate_active = true
    def i_valid_emailTemplate_oneOff = true
    def i_valid_emailTemplate_published = true
    def i_valid_emailTemplate_validFrom = new Date()
    def i_valid_emailTemplate_validTo = new Date()
    def i_valid_emailTemplate_createdBy = """Valid EmailTemplate createdBy"""
    def i_valid_emailTemplate_createDate = new Date()

    def CommunicationFolder folder
    def CommunicationEmailItem emailTemplate


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        folder = newValidForCreateFolder()
        folder.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull folder.id

    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateEmailItem() {

        def originalList = CommunicationEmailTemplate.findAll()

        def emailTemplate = newValidForCreateEmailTemplate(folder)
        emailTemplate.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull emailTemplate.id
        assertEquals i_valid_emailTemplate_name, emailTemplate.name
        assertEquals i_valid_emailTemplate_description, emailTemplate.description
        assertEquals i_valid_emailTemplate_personal, emailTemplate.personal
        assertEquals i_valid_emailTemplate_bccList, emailTemplate.bccList
        assertEquals i_valid_emailTemplate_ccList, emailTemplate.ccList
        assertEquals i_valid_emailTemplate_content, emailTemplate.content
        assertEquals i_valid_emailTemplate_fromList, emailTemplate.fromList
        assertEquals i_valid_emailTemplate_subject, emailTemplate.subject
        assertEquals i_valid_emailTemplate_toList, emailTemplate.toList
        assertEquals "grails_user", emailTemplate.lastModifiedBy
        /* you can't predict what lastModified will get set to, so just check not null */
        assertNotNull emailTemplate.lastModified
        /* gets set to Banner by the framework */
        assertEquals "Banner", emailTemplate.dataOrigin
        assertEquals i_valid_emailTemplate_active, emailTemplate.active
        assertEquals i_valid_emailTemplate_oneOff, emailTemplate.oneOff
        assertEquals i_valid_emailTemplate_published, emailTemplate.published
        assertEquals i_valid_emailTemplate_validFrom, emailTemplate.validFrom
        assertEquals i_valid_emailTemplate_validTo, emailTemplate.validTo
        assertEquals i_valid_emailTemplate_createdBy, emailTemplate.createdBy
        assertEquals i_valid_emailTemplate_createDate, emailTemplate.createDate

        // Now test findall
        def foundEmailTemplates = CommunicationEmailTemplate.findAll()
        assertEquals(originalList.size() + 1, foundEmailTemplates.size())

        def emailItem = new CommunicationEmailItem(
                communicationChannel: "EMAIL",
                createdBy: i_valid_emailTemplate_createdBy,
                createDate: i_valid_emailTemplate_createDate,
                recipientPidm: 999999999,
                bccList: i_valid_emailTemplate_bccList,
                ccList: i_valid_emailTemplate_ccList,
                content: i_valid_emailTemplate_content,
                fromList: i_valid_emailTemplate_fromList,
                subject: i_valid_emailTemplate_subject,
                toList: i_valid_emailTemplate_toList);

        emailItem.save(failOnError: true, flush: true)
        assertNotNull emailItem.id
    }


    private def newValidForCreateFolder() {
        def folder = new CommunicationFolder(
                description: i_valid_folder_description,
                internal: i_valid_folder_internal,
                name: i_valid_folder_name
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
                toList: i_valid_emailTemplate_toList)
        return communicationTemplate
    }
}

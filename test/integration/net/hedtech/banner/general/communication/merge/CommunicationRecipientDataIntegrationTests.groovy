/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.merge

import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.organization.CommunicationOrganizationService
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for CommunicationRecipientData entity
 */
class CommunicationRecipientDataIntegrationTests extends BaseIntegrationTestCase {

    def CommunicationFolder folder
    def CommunicationEmailTemplate emailTemplate
    def CommunicationFieldValue fieldValue1
    def CommunicationOrganizationService communicationOrganizationService

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

    private CommunicationOrganization organization


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
        folder = newValidForCreateFolder()
        folder.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull folder.id

        emailTemplate = newValidForCreateEmailTemplate(folder)
        emailTemplate.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull emailTemplate.id

        fieldValue1 = newFieldValue("FieldValue1")

        organization = new CommunicationOrganization(name: "Test Org", isRoot: true)
        organization = communicationOrganizationService.create(organization) as CommunicationOrganization
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateCommunicationRecipientData() {
        def communicationRecipientData = newCommunicationRecipientData()
        communicationRecipientData.setOrganization( organization )
        communicationRecipientData.save(failOnError: true, flush: true)

        assertNotNull(communicationRecipientData.id)
        assertTrue(communicationRecipientData.pidm.equals(new Long(99990001)))
        assertNotNull(communicationRecipientData.version)

        def getvalue = CommunicationFieldValue.findAll()
        assertNotNull(getvalue)
    }


    private def newCommunicationRecipientData() {
        def communicationRecipientData = new CommunicationRecipientData(
                // Required fields
                pidm: 99990001,
                templateId: emailTemplate.id,
                referenceId: 1,
                ownerId: "OWNER",
                fieldValues: ["name": fieldValue1]
        )
        return communicationRecipientData
    }


    private def newFieldValue(String insertvalue) {
        def CommunicationFieldValue = new CommunicationFieldValue(
                value: insertvalue,
                renderAsHtml: false
        )

        return CommunicationFieldValue
    }


    private def newValidForCreateFolder() {
        def folder = new CommunicationFolder(
                description: "Test Folder",
                internal: false,
                name: "Folder Name"
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
                toList: i_valid_emailTemplate_toList,)
        return communicationTemplate
    }
}

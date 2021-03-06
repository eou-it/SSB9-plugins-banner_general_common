/*********************************************************************************
 Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.email

import grails.util.GrailsWebMockUtil
import grails.web.servlet.context.GrailsWebApplicationContext
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import static groovy.test.GroovyAssert.*

/**
 * Tests crud methods provided by communication template service.
 */
@Integration
@Rollback
class CommunicationEmailTemplateServiceIntegrationTests extends BaseIntegrationTestCase {

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

    def i_valid_emailTemplate_name = """Valid Name"""
    def i_invalid_emailTemplate_name = """Valid Name""".padLeft(2049)

    def i_valid_emailTemplate_oneOff = true
    def i_valid_emailTemplate_personal = false
    def i_valid_emailTemplate_published = false

    def i_valid_emailTemplate_subject = """Valid Emailtemplate Subject"""
    def i_invalid_emailTemplate_subject = """You're a winner!""".padLeft(1021)

    def i_valid_emailTemplate_toList = """Valid Emailtemplate Tolist"""
    def i_invalid_emailTemplate_toList = "foo@bar.com".padLeft(1021)

    def i_valid_emailTemplate_validFrom = new Date()
    def i_valid_emailTemplate_validTo = new Date() + 200

    def communicationTemplateService
    def communicationEmailTemplateService
    def selfServiceBannerAuthenticationProvider

    def CommunicationFolder folder1
    def CommunicationFolder folder2


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

    void setUpFolderData() {
        if(!folder1) {
            folder1 = CommunicationManagementTestingSupport.newValidForCreateFolderWithSave()
        }
        if(!folder2) {
            folder2 = CommunicationManagementTestingSupport.newValidForCreateFolderWithSave()
        }
    }

    void setUpData() {
        // Force the validTo into the future
        Calendar c = Calendar.getInstance()
        c.setTime( new Date() )
        c.add( Calendar.DATE, 150 )
        i_valid_emailTemplate_validTo = c.getTime()
        c.setTime( new Date() )
        c.add( Calendar.DATE, -300 )
        i_valid_emailTemplate_validFrom = c.getTime()
    }

    public GrailsWebRequest mockRequest() {
        GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();
        GrailsWebMockUtil.bindMockWebRequest(webAppCtx, mockRequest, mockResponse)
    }

    @Test
    void testCreateTemplate() {
        def originalListCount = communicationTemplateService.list().size()
        def template = newValidForCreateEmailTemplate()
        def newTemplate = communicationEmailTemplateService.create([domainModel: template])
        //Test if the service set the created date, and the infrastructure set the modifiedby and date
        assertNotNull newTemplate.createDate
        assertNotNull(newTemplate.lastModified)
        assertNotNull(newTemplate.lastModifiedBy)
        assertFalse newTemplate.systemIndicator

        // Now test findall
        def foundEmailTemplates = communicationTemplateService.findAll()
        assertEquals(1, foundEmailTemplates.size())
    }

    //TODO: test that publish fails if template contains invalid data fields or if any of the following are null: name ,toList,content,subject
    //
    //TODO - Grails 3 migration - Commenting out this test as the publishTemplate throws OptimisticLockException when it should not.
    // This is because we need a fix in ServiceBase extractParameters method, when a domainObject is passed to fetch the version attribute from parent.
    //This method is not used from the UI controller anymore, it has been updated to use the update method using the map for the 9.6 release
    /*@Test
    void testPublishTemplate() {
        def originalListCount = communicationTemplateService.list().size()
        def template = newValidForCreateEmailTemplate()
        def newTemplate = communicationEmailTemplateService.create([domainModel: template])
        //Test if the service set the created date, and the infrastructure set the modifiedby and date
        assertNotNull newTemplate.createDate
        assertNotNull(newTemplate.lastModified)
        assertNotNull(newTemplate.lastModifiedBy)
        assertFalse("Should not be published", newTemplate.published)

        newTemplate.toList = null
        def template1 = communicationEmailTemplateService.update([domainModel:newTemplate])
        assertNull(template1.toList)
        shouldFail {
            def notPublished = communicationEmailTemplateService.publishTemplate([id:template1.id])
        }

        template1.refresh()
        template1.toList = "TOLIST"
        def template2 = communicationEmailTemplateService.update([domainModel:template1])

        def pubtemp = communicationEmailTemplateService.publishTemplate(["id":template1.id])
        assertTrue("Should be published",pubtemp.published)

        // Now test findall
        def foundEmailTemplates = communicationTemplateService.findAll()
        assertEquals(1, foundEmailTemplates.size())
    }*/


    @Test
    void testUpdateTemplate() {
        def template = newValidForCreateEmailTemplate()
        def newTemplate = communicationEmailTemplateService.create([domainModel: template])
        //Test if the generated entity now has an id assigned
        assertNotNull newTemplate.id
        newTemplate.description = "Updated description"
        def updatedTemplate = communicationEmailTemplateService.update([domainModel: newTemplate])
        assertEquals("Updated description", updatedTemplate.description)

        def template2 = newValidForCreateEmailTemplateWithFolder2()

        def newTemplate2 = communicationEmailTemplateService.create([domainModel: template2])
        //Test if the generated entity now has an id assigned
        assertNotNull newTemplate2.id
        newTemplate2.folder = folder1
        try {
            communicationEmailTemplateService.update([domainModel: newTemplate2])
            Assert.fail "Expected sameNameTemplate to fail because of name unique constraint."
        } catch (ApplicationException e) {
            assertEquals("@@r1:templateExists:" + newTemplate2.name + "@@", e.message)
        }
    }


    @Test
    void testUpdateTemplateFolder() {
        def template = newValidForCreateEmailTemplate()
        def newTemplate = communicationEmailTemplateService.create([domainModel: template])
        //Test if the generated entity now has an id assigned
        assertNotNull newTemplate.id
        newTemplate.folder = folder2
        def updatedTemplate = communicationEmailTemplateService.update([domainModel: newTemplate])
        assertEquals(folder2.name, updatedTemplate.folder.name)
    }


    @Test
    void testDeleteTemplate() {
        def originalListCount = communicationTemplateService.list().size()
        def template = newValidForCreateEmailTemplate()
        def createdTemplate = communicationEmailTemplateService.create([domainModel: template])
        //Test if the generated entity now has an id assigned
        assertNotNull createdTemplate.id

        //delete the newly created template and check if count is back to original
        communicationTemplateService.delete(createdTemplate)
        def createdId = createdTemplate.id
        shouldFail
                {
                    (communicationTemplateService.get(createdId))
                }
    }


    @Test
    void testCreateInValidCommunicationTemplate() {
        def communicationTemplate = newValidForCreateEmailTemplate()

        communicationTemplate.name = i_invalid_emailTemplate_name
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }

        communicationTemplate = newValidForCreateEmailTemplate()
        communicationTemplate.description = i_invalid_emailTemplate_description
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }

        communicationTemplate = newValidForCreateEmailTemplate()
        communicationTemplate.bccList = i_invalid_emailTemplate_bccList
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }

        communicationTemplate = newValidForCreateEmailTemplate()
        communicationTemplate.ccList = i_invalid_emailTemplate_ccList
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }

        communicationTemplate = newValidForCreateEmailTemplate()
        communicationTemplate.createdBy = i_invalid_emailTemplate_createdBy
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }

        communicationTemplate = newValidForCreateEmailTemplate()
        communicationTemplate.dataOrigin = i_invalid_emailTemplate_dataOrigin
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }

        communicationTemplate = newValidForCreateEmailTemplate()
        communicationTemplate.fromList = i_invalid_emailTemplate_fromList
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }

        communicationTemplate = newValidForCreateEmailTemplate()
        communicationTemplate.subject = i_invalid_emailTemplate_subject
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }

        communicationTemplate = newValidForCreateEmailTemplate()
        communicationTemplate.toList = i_invalid_emailTemplate_toList
        shouldFail { communicationTemplate.save(failOnError: true, flush: true) }


    }

    private def newValidForCreateEmailTemplate() {
        setUpFolderData()
        setUpData()
        def communicationTemplate = new CommunicationEmailTemplate(
                description: i_valid_emailTemplate_description,
                personal: i_valid_emailTemplate_personal,
                name: i_valid_emailTemplate_name,
                oneOff: i_valid_emailTemplate_oneOff,
                published: i_valid_emailTemplate_published,
                createdBy: i_valid_emailTemplate_createdBy,
                createDate: i_valid_emailTemplate_createDate,
                validFrom: i_valid_emailTemplate_validFrom,
                validTo: i_valid_emailTemplate_validTo,
                folder: folder1,
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

    private def newValidForCreateEmailTemplateWithFolder2() {
        setUpFolderData()
        setUpData()
        def communicationTemplate = new CommunicationEmailTemplate(
                description: i_valid_emailTemplate_description,
                personal: i_valid_emailTemplate_personal,
                name: i_valid_emailTemplate_name,
                oneOff: i_valid_emailTemplate_oneOff,
                published: i_valid_emailTemplate_published,
                createdBy: i_valid_emailTemplate_createdBy,
                createDate: i_valid_emailTemplate_createDate,
                validFrom: i_valid_emailTemplate_validFrom,
                validTo: i_valid_emailTemplate_validTo,
                folder: folder2,
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

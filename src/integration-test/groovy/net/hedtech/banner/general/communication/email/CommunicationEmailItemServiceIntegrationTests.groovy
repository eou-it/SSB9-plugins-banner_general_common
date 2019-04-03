/*********************************************************************************
 Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.email

import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import grails.util.GrailsWebMockUtil
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import grails.web.servlet.context.GrailsWebApplicationContext

/**
 * Tests crud methods provided by communication item service.
 */
@Integration
@Rollback
class CommunicationEmailItemServiceIntegrationTests extends BaseIntegrationTestCase {
    def i_valid_emailTemplate_active = true

    def i_valid_emailTemplate_bccList = """Valid Emailtemplate Bcclist"""
    def i_invalid_emailTemplate_bccList = "foo@bar.com".padLeft( 1021 )

    def i_valid_emailTemplate_ccList = """Valid Emailtemplate Cclist"""
    def i_invalid_emailTemplate_ccList = "foo@bar.com".padLeft( 1021 )

    def i_valid_emailTemplate_content = """Valid Emailtemplate Content"""

    def i_valid_emailTemplate_createDate = new Date()

    def i_valid_emailTemplate_createdBy = """Valid EmailTemplate createdBy"""
    def i_invalid_emailTemplate_createdBy = """Valid EmailTemplate createdBy""".padLeft( 31 )

    def i_valid_emailTemplate_dataOrigin = """Valid Emailtemplate Dataorigin"""
    def i_invalid_emailTemplate_dataOrigin = "XE Communication Manager".padLeft( 31 )

    def i_valid_emailTemplate_description = """Valid Template Description"""
    def i_invalid_emailTemplate_description = """Valid Template Description""".padLeft( 4001 )

    def i_valid_emailTemplate_fromList = """Valid Emailtemplate Fromlist"""
    def i_invalid_emailTemplate_fromList = "foo@bar.com".padLeft( 1021 )

    def i_valid_emailTemplate_lastModified = new Date()

    def i_valid_emailTemplate_lastModifiedBy = """Valid Emailtemplate Lastmodifiedby"""
    def i_invalid_emailTemplate_lastModifiedBy = "BCMUSER".padLeft( 31 )

    def i_valid_emailTemplate_name = """Valid Name"""
    def i_invalid_emailTemplate_name = """Valid Name""".padLeft( 2049 )

    def i_valid_emailTemplate_oneOff = true
    def i_valid_emailTemplate_personal = true
    def i_valid_emailTemplate_published = true

    def i_valid_emailTemplate_subject = """Valid Emailtemplate Subject"""
    def i_invalid_emailTemplate_subject = """You're a winner!""".padLeft( 1021 )

    def i_valid_emailTemplate_toList = """Valid Emailtemplate Tolist"""
    def i_invalid_emailTemplate_toList = "foo@bar.com".padLeft( 1021 )

    def i_valid_emailTemplate_validFrom = new Date()-200
    def i_valid_emailTemplate_validTo = new Date()+200

    def i_valid_folder_description = "Valid older description"
    def i_valid_folder_internal = true
    def i_valid_folder_name1 = "Valid Folder1 Name"
    def i_valid_folder_name2 = "Valid Folder2 Name"

    def communicationEmailItemService

    def CommunicationFolder folder1
//    def CommunicationFolder folder2

    def selfServiceBannerAuthenticationProvider
    def communicationTemplateService
    def communicationEmailTemplateService


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
        folder1 = newValidForCreateFolder( i_valid_folder_name1 )
        folder1.save( failOnError: true, flush: true )
        //Test if the generated entity now has an id assigned
        assertNotNull folder1.id
//        folder2 = newValidForCreateFolder( i_valid_folder_name2 )
//        folder2.save( failOnError: true, flush: true )
//        //Test if the generated entity now has an id assigned
//        assertNotNull folder2.id
    }

    @Test
    void testCreateEmailItem() {
        def originalListCount = communicationTemplateService.list().size()
        def template = newValidForCreateEmailTemplate()
        def newTemplate = communicationEmailTemplateService.create( [domainModel: template] )
        //Test if the service set the created date, and the infrastructure set the modifiedby and date
        assertNotNull newTemplate.createDate
        assertNotNull( newTemplate.lastModified )
        assertNotNull( newTemplate.lastModifiedBy )

        // Now test findall
        def foundEmailTemplates = communicationTemplateService.findAll()
        assertEquals( 1, foundEmailTemplates.size() )

        def newEmailItem = communicationEmailItemService.create( [domainModel: newValidForCreateEmailItem()] )
        assertNotNull( newEmailItem.id )
        assertEquals(CommunicationChannel.EMAIL, newEmailItem.communicationChannel)

    }


@Test
   void testDeleteEmailItem() {
    def originalListCount = communicationTemplateService.list().size()
       def template = newValidForCreateEmailTemplate()
       def newTemplate = communicationEmailTemplateService.create( [domainModel: template] )
       //Test if the service set the created date, and the infrastructure set the modifiedby and date
       assertNotNull newTemplate.createDate
       assertNotNull( newTemplate.lastModified )
       assertNotNull( newTemplate.lastModifiedBy )

       // Now test findall
       def foundEmailTemplates = communicationTemplateService.findAll()
       assertEquals( 1, foundEmailTemplates.size() )

       def newEmailItem = communicationEmailItemService.create( [domainModel: newValidForCreateEmailItem()] )
       assertNotNull( newEmailItem.id )
       CommunicationEmailItem.findAll().each { item ->
           communicationEmailItemService.delete( item )
       }

       def results = CommunicationEmailItem.findAll()
       assertEquals(0, results.size())
   }



    private def newValidForCreateFolder( String folderName ) {
        def folder = new CommunicationFolder(
                description: i_valid_folder_description,
                internal: i_valid_folder_internal,
                name: folderName
        )
        return folder
    }


    private def newValidForCreateEmailTemplate() {
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


    private def newValidForCreateEmailItem() {
        def emailItem = new CommunicationEmailItem(
                communicationChannel: CommunicationChannel.EMAIL,
                createdBy: i_valid_emailTemplate_createdBy,
                createDate: i_valid_emailTemplate_createDate,
                recipientPidm: 999999999,
                bccList: i_valid_emailTemplate_bccList,
                ccList: i_valid_emailTemplate_ccList,
                content: i_valid_emailTemplate_content,
                fromList: i_valid_emailTemplate_fromList,
                subject: i_valid_emailTemplate_subject,
                toList: i_valid_emailTemplate_toList,
                referenceId: UUID.randomUUID().toString() );

        return emailItem;
    }
}

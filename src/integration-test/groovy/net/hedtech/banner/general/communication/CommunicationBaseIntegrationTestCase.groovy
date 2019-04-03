/********************************************************************************
  Copyright 2017-2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.communication

import groovy.sql.Sql
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerConnectionSecurity
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerProperties
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerPropertiesType
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccount
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccountService
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccountType
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.junit.After
import org.junit.Before
import com.icegreen.greenmail.util.*
import grails.util.GrailsWebMockUtil
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import grails.web.servlet.context.GrailsWebApplicationContext
/**
 * A BaseIntegrationTestCase with added test support for communication artifacts.
 */
class CommunicationBaseIntegrationTestCase extends BaseIntegrationTestCase {

/*    def communicationGroupSendMonitor
    def communicationGroupSendCompositeService
    CommunicationMailboxAccountService communicationMailboxAccountService
    def communicationFolderService
    def communicationTemplateService
    def communicationEmailTemplateService
    def communicationOrganizationCompositeService
    def communicationGroupSendItemProcessingEngine
    def communicationJobProcessingEngine
    def communicationFieldService

    protected CommunicationOrganization defaultOrganization
    protected CommunicationFolder defaultFolder
    protected CommunicationEmailTemplate defaultEmailTemplate
    protected GreenMail mailServer
    protected static final int smtp_port = 4025*/

    def userName = 'BCMADMIN'
    def password = '111111'

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    public void setUp(bannerId, password) {
        this.setUp()

    }

    protected void login(bannerId, password) {
        webAppCtx = new GrailsWebApplicationContext()
        mockRequest()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken( userName, password ))
        SecurityContextHolder.getContext().setAuthentication(auth)
    }

    @After
    public void tearDown() {
//        if (mailServer) mailServer.stop()

//        deleteAll()
        logout()
        super.tearDown()
    }

    public GrailsWebRequest mockRequest() {
        GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();
        GrailsWebMockUtil.bindMockWebRequest(webAppCtx, mockRequest, mockResponse)
    }

//    public void dataAndMailServerSetUp() {
//        deleteAll()
//        setUpDefaultOrganization()
//        setUpDefaultFolder()
//        setUpDefaultEmailTemplate()
//
//        ServerSetup smtpServerSetup = new ServerSetup( smtp_port, "127.0.0.1", ServerSetup.PROTOCOL_SMTP);
//        mailServer = new GreenMail( smtpServerSetup)
//
//        CommunicationEmailServerProperties sendEmailServerProperties = defaultOrganization.sendEmailServerProperties
//        String userPassword = communicationMailboxAccountService.decryptPassword( defaultOrganization.senderMailboxAccount.encryptedPassword )
//    }

/*    protected void deleteAll() {
        def sql
        try {
            sessionFactory.currentSession.with { session ->
                sql = new Sql(session.connection())
                def tx = session.beginTransaction()
                sql.executeUpdate("Delete from GCRLETM")
                sql.executeUpdate("Delete from GCRMITM")
                sql.executeUpdate("Delete from GCREITM")
                sql.executeUpdate("Delete from GCRTITM")
                sql.executeUpdate("Delete from GCRCITM")
                sql.executeUpdate("Delete from GCRMINT")
                sql.executeUpdate("Delete from GCBCJOB")
                sql.executeUpdate("Delete from GCRFVAL")
                sql.executeUpdate("Delete from GCBRDAT")
                sql.executeUpdate("Delete from GCRGSIM")
                sql.executeUpdate("Delete from GCBGSND")
                sql.executeUpdate("Delete from GCBCREC")
                sql.executeUpdate("Delete from GCBEVMP where gcbevmp_system_req_ind = 'N'")
                sql.executeUpdate("Delete from GCRTPFL")
                sql.executeUpdate("Delete from GCBEMTL WHERE EXISTS (SELECT gcbtmpl_surrogate_id FROM gcbtmpl WHERE gcbtmpl_surrogate_id = gcbemtl_surrogate_id AND gcbtmpl_system_req_ind = 'N')")
                sql.executeUpdate("Delete from GCBMNTL")
                sql.executeUpdate("Delete from GCBLTPL")
                sql.executeUpdate("Delete from GCBTMTL")
                sql.executeUpdate("Delete from GCBTMPL  WHERE gcbtmpl_system_req_ind = 'N'")
                sql.executeUpdate("Delete from GCRFLPM a WHERE a.gcrflpm_field_id IN (SELECT gcrcfld_surrogate_id FROM gcrcfld WHERE gcrcfld_system_ind = 'N')")
                sql.executeUpdate("Delete from GCRCFLD WHERE gcrcfld_system_ind = 'N'")
                sql.executeUpdate("Delete from GCRPARM WHERE gcrparm_system_ind = 'N'")
                sql.executeUpdate("Delete from GCRLENT")
                sql.executeUpdate("Delete from GCRPQID")
                sql.executeUpdate("Delete from GCRPVID")
                sql.executeUpdate("Delete from GCRPOPC")
                sql.executeUpdate("Delete from GCRPOPV")
                sql.executeUpdate("Delete from GCRSLIS")
                sql.executeUpdate("Delete from GCBPOPL")
                sql.executeUpdate("Delete from GCRQRYV")
                sql.executeUpdate("Delete from GCBQURY")
                sql.executeUpdate("Delete from GCRITPE")
                sql.executeUpdate("DELETE FROM gcrfldr WHERE NOT EXISTS (SELECT a.GCBACTM_GCRFLDR_ID FROM gcbactm a WHERE a.GCBACTM_GCRFLDR_ID = gcrfldr_surrogate_id) AND \
                                                             NOT EXISTS (SELECT b.GCBAGRP_GCRFLDR_ID FROM gcbagrp b WHERE b.GCBAGRP_GCRFLDR_ID = gcrfldr_surrogate_id) AND \
                                                             gcrfldr_system_ind = 'N'")
                sql.executeUpdate("Delete from GCRORAN WHERE NOT EXISTS (SELECT E.GCBEVMP_ORGANIZATION_ID FROM GCBEVMP E WHERE E.GCBEVMP_ORGANIZATION_ID = GCRORAN_SURROGATE_ID) AND GCRORAN_PARENT_ID IS NOT NULL")
                sql.executeUpdate("Delete from GCBSPRP WHERE NOT EXISTS (SELECT S.GCRORAN_SEND_EMAILPROP_ID FROM GCRORAN S WHERE S.GCRORAN_SEND_EMAILPROP_ID = GCBSPRP_SURROGATE_ID)")
                sql.executeUpdate("Delete from GCRMBAC WHERE NOT EXISTS (SELECT S.GCRORAN_SEND_MAILBOX_ID FROM GCRORAN S WHERE S.GCRORAN_SEND_MAILBOX_ID = GCRMBAC_SURROGATE_ID) AND \
                                                             NOT EXISTS (SELECT R.GCRORAN_REPLY_MAILBOX_ID FROM GCRORAN R WHERE R.GCRORAN_REPLY_MAILBOX_ID = GCRMBAC_SURROGATE_ID)")
                tx.commit()
            }
        } finally {
//            sql?.close()
        }
    }

*//*


     *//*


    protected void setUpDefaultOrganization() {
        List organizations = communicationOrganizationCompositeService.listOrganizations()
        if (organizations.size() == 0) {
            defaultOrganization = new CommunicationOrganization(name: "Test Org")

            def cma = new CommunicationMailboxAccount(
                emailAddress: 'rasul.shishehbor@ellucian.com',
                encryptedPassword: communicationMailboxAccountService.encryptPassword( "changeit" ),
                userName: 'rshishehbor',
                organization: defaultOrganization,
                type: CommunicationMailboxAccountType.Sender
            )
            defaultOrganization.senderMailboxAccount = cma

            def cesp = new CommunicationEmailServerProperties(
                securityProtocol: CommunicationEmailServerConnectionSecurity.None,
                host: "127.0.0.1",
                port: smtp_port,
                organization: defaultOrganization,
                type: CommunicationEmailServerPropertiesType.Send
            )
            defaultOrganization.sendEmailServerProperties = cesp
            defaultOrganization = communicationOrganizationCompositeService.createOrganization(defaultOrganization) as CommunicationOrganization
        } else {
            defaultOrganization = organizations.get(0) as CommunicationOrganization
        }
    }

    protected void setUpDefaultFolder() {
        defaultFolder = CommunicationFolder.findByName( "CommunicationGroupSendCompositeServiceTests" )
        if (!defaultFolder) {
            defaultFolder = new CommunicationFolder( name: "CommunicationGroupSendCompositeServiceTests", description: "integration test" )
            defaultFolder = communicationFolderService.create( defaultFolder )
        }
    }

    protected void setUpDefaultEmailTemplate() {
        defaultEmailTemplate = CommunicationEmailTemplate.findByName( "CommunicationGroupSendCompositeServiceTests_template" )
        if (!defaultEmailTemplate) {
            defaultEmailTemplate = new CommunicationEmailTemplate (
                    name: "CommunicationGroupSendCompositeServiceTests_template",
                    personal: false,
                    oneOff: false,
                    folder: defaultFolder,
                    toList: "test@test.edu",
                    subject: "test subject",
                    content: "test content",
            )
            defaultEmailTemplate = communicationEmailTemplateService.create( defaultEmailTemplate )
            defaultEmailTemplate = communicationEmailTemplateService.publishTemplate( ["id": defaultEmailTemplate.id] )
        }
    }

    protected void deleteAll( service ) {
        service.findAll().each {
            service.delete( it )
        }
    }*/

}

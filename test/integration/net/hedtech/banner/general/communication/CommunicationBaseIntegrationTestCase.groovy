package net.hedtech.banner.general.communication

import groovy.sql.Sql
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerConnectionSecurity
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerProperties
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerPropertiesService
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerPropertiesType
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccount
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccountService
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccountType
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import com.icegreen.greenmail.util.*

/**
 * A BaseIntegrationTestCase with added test support for communication artifacts.
 */
class CommunicationBaseIntegrationTestCase extends BaseIntegrationTestCase {

    def communicationGroupSendMonitor
    def communicationGroupSendCompositeService
    CommunicationMailboxAccountService communicationMailboxAccountService
    CommunicationEmailServerPropertiesService communicationEmailServerPropertiesService
    def communicationGroupSendService
    def communicationGroupSendItemService
    def communicationPopulationQueryService
    def communicationPopulationQueryExecutionService
    def communicationPopulationSelectionListService
    def communicationFolderService
    def communicationTemplateService
    def communicationEmailTemplateService
    def communicationOrganizationCompositeService
    def communicationGroupSendItemProcessingEngine
    def communicationJobProcessingEngine
    def communicationRecipientDataService
    def communicationJobService
    def communicationItemService
    def communicationEmailItemService
    def communicationFieldService

    protected CommunicationOrganization defaultOrganization
    protected CommunicationFolder defaultFolder
    protected CommunicationEmailTemplate defaultEmailTemplate
    protected GreenMail mailServer
    protected static final int smtp_port = 4025

    @Before
    public void setUp() {
        super.setUp()

        deleteAll()
        setUpDefaultOrganization()
        setUpDefaultFolder()
        setUpDefaultEmailTemplate()

        ServerSetup smtpServerSetup = new ServerSetup( smtp_port, "127.0.0.1", ServerSetup.PROTOCOL_SMTP);
        mailServer = new GreenMail( smtpServerSetup)

        CommunicationEmailServerProperties sendEmailServerProperties = defaultOrganization.sendEmailServerProperties
        String userPassword = communicationMailboxAccountService.decryptPassword( defaultOrganization.senderMailboxAccount.encryptedPassword )
    }

    @After
    public void tearDown() {
        if (mailServer) mailServer.stop()

        deleteAll()
        super.tearDown()
    }

    protected void deleteAll() {
        def sql
        try {
            sessionFactory.currentSession.with { session ->
                sql = new Sql(session.connection())
                def tx = session.beginTransaction()
                sql.executeUpdate("Delete from GCRMITM")
                sql.executeUpdate("Delete from GCREITM")
                sql.executeUpdate("Delete from GCRCITM")
                sql.executeUpdate("Delete from GCBCJOB")
                sql.executeUpdate("Delete from GCRFVAL")
                sql.executeUpdate("Delete from GCBRDAT")
                sql.executeUpdate("Delete from GCRGSIM")
                sql.executeUpdate("Delete from GCBGSND")
                sql.executeUpdate("Delete from GCBEMTL")
                sql.executeUpdate("Delete from GCBMNTL")
                sql.executeUpdate("Delete from GCBTMPL")
                sql.executeUpdate("Delete from GCRCFLD")
                sql.executeUpdate("Delete from GCRLENT")
                sql.executeUpdate("Delete from GCRPVID")
                sql.executeUpdate("Delete from GCRSLIS")
                sql.executeUpdate("Delete from GCRPQID")
                sql.executeUpdate("Delete from GCRPOPV")
                sql.executeUpdate("Delete from GCBPOPL")
                sql.executeUpdate("Delete from GCRQRYV")
                sql.executeUpdate("Delete from GCBQURY")
                sql.executeUpdate("Delete from GCRFLDR")
                sql.executeUpdate("Delete from GCRORAN")
                sql.executeUpdate("Delete from GCBSPRP")
                sql.executeUpdate("Delete from GCRMBAC")
                tx.commit()
            }
        } finally {
            sql?.close()
        }
    }

/*


     */


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
    }

}

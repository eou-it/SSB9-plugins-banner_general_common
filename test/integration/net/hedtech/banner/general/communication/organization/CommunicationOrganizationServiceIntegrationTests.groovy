/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.organization

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Tests crud methods provided by organization service.
 */
class CommunicationOrganizationServiceIntegrationTests extends BaseIntegrationTestCase {
    def communicationOrganizationService
    def selfServiceBannerAuthenticationProvider


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( 'BCMADMIN', '111111' ) )
        SecurityContextHolder.getContext().setAuthentication( auth )

    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testList() {
        long originalListCount = communicationOrganizationService.list().size()

        CommunicationOrganization organization = new CommunicationOrganization();
        organization.name = "test"
        organization.description = "description"
        CommunicationOrganization createdOrganization = communicationOrganizationService.create( organization )
        assertNotNull( createdOrganization )

        long addedListCount = communicationOrganizationService.list().size()
        assertEquals( originalListCount + 1, addedListCount )
    }


    @Test
    void testCreate() {
        CommunicationOrganization organization = new CommunicationOrganization()
        organization.name = "test"
        organization.description = "description"
        CommunicationOrganization createdOrganization = communicationOrganizationService.create( organization )
        assertNotNull( createdOrganization )
        assertEquals( "test", createdOrganization.name )
        assertEquals( "description", createdOrganization.description )

        CommunicationOrganization foundOrganization = CommunicationOrganization.findByName( "test" )
        assertEquals( createdOrganization, foundOrganization )

        CommunicationOrganization sameNameOrganization = new CommunicationOrganization()
        sameNameOrganization.name = "test"
        sameNameOrganization.description = "another organization with same name"
        try {
            communicationOrganizationService.create( sameNameOrganization )
            Assert.fail "Expected sameNameOrganization to fail because of name unique constraint."
        } catch (ApplicationException e) {
            assertTrue e.getSqlException().toString().contains( "ORA-00001: unique constraint (GENERAL.GCRORAN_KEY_INDEX) violated" )
        }

    }


    @Test
    void testUpdate() {
        CommunicationOrganization organization1 = new CommunicationOrganization()
        organization1.name = "organization1"
        organization1 = communicationOrganizationService.create( organization1 )

        organization1 = CommunicationOrganization.get( organization1.getId() )
        organization1.setName( "organization1 changed" )
        organization1.setDescription( "description changed" )
        organization1 = communicationOrganizationService.update( organization1 )

        assertEquals( "organization1 changed", organization1.getName() )
        assertEquals( "description changed", organization1.getDescription() )

        CommunicationOrganization organization2 = new CommunicationOrganization()
        organization2.name = "organization2"
        organization2 = communicationOrganizationService.create( organization2 )

        organization1.name = organization2.name
        try {
            communicationOrganizationService.update( organization1 )
            Assert.fail "Expected sameNameOrganization to fail because of name unique constraint."
        } catch (ApplicationException e) {
            assertTrue e.getSqlException().toString().contains( "ORA-00001: unique constraint (GENERAL.GCRORAN_KEY_INDEX) violated" )
        }
    }


    @Test
    void testDelete() {
        CommunicationOrganization organization1 = new CommunicationOrganization();
        organization1.name = "test"
        organization1.description = "description"
        CommunicationOrganization createdOrganization = communicationOrganizationService.create( organization1 )
        assertNotNull( createdOrganization )
        Long id = createdOrganization.getId()

        long count = communicationOrganizationService.list().size()

        //set the flush mode to auto to prevent the write mode error during test
        //ssessionFactory.currentSession.flushMode = FlushMode.AUTO
        // Delete the domain
        communicationOrganizationService.delete( createdOrganization )

        assertEquals( count - 1, communicationOrganizationService.list().size() )

        try {
            assertNull( communicationOrganizationService.get( id ) )
            Assert.fail "Expected get by id to fail because does not exist."
        } catch (ApplicationException e) {
            assertEquals( "NotFoundException", e.getType() )
        }
    }


    @Test
    void testCreateWithServerSettings() {
        CommunicationOrganization organization = new CommunicationOrganization()
        organization.name = "test"
        organization.description = "description"
        def receiveProperties = newCommunicationEmailServerProperties( CommunicationEmailServerPropertiesType.Receive, organization )
        def sendProperties = newCommunicationEmailServerProperties( CommunicationEmailServerPropertiesType.Send, organization )
        def senderMailboxAccountSettings = newCommunicationMailBoxProperties( CommunicationMailboxAccountType.Sender, organization )
        def replyToMailboxAccountSettings = newCommunicationMailBoxProperties( CommunicationMailboxAccountType.ReplyTo, organization )
        organization.receiveEmailServerProperties = receiveProperties
        organization.sendEmailServerProperties = sendProperties
        organization.senderMailboxAccountSettings = senderMailboxAccountSettings
        organization.replyToMailboxAccountSettings = replyToMailboxAccountSettings
        CommunicationOrganization createdOrganization = communicationOrganizationService.create( organization )
        assertNotNull( createdOrganization )
        assertEquals( "test", createdOrganization.name )
        assertEquals( "description", createdOrganization.description )
        assertEquals( "D359A3537A74FC42F284450BCCDDA734", createdOrganization.senderMailboxAccountSettings.encryptedPassword )
        assertEquals( "D359A3537A74FC42F284450BCCDDA734", createdOrganization.replyToMailboxAccountSettings.encryptedPassword )

    }


    @Test
    void testPasswordEncryptAndDecrypt() {

        def thePassword = "someSecretThisIs"
        def encryptedPassword = communicationOrganizationService.encryptMailBoxAccountPassword( thePassword )
        def decryptedPassword = communicationOrganizationService.decryptMailBoxAccountPassword( encryptedPassword )
        assertEquals( thePassword, decryptedPassword )
    }


    private def newCommunicationEmailServerProperties( CommunicationEmailServerPropertiesType serverType, organization ) {
        def communicationEmailServerProperties = new CommunicationEmailServerProperties(
                // Required fields
                securityProtocol: "TTTTTTTTTT",
                smtpHost: "TTTTTTTTTT",
                smtpPort: 1234,
                organization: organization,
                type: serverType
        )
        return communicationEmailServerProperties
    }


    private def newCommunicationMailBoxProperties( CommunicationMailboxAccountType communicationMailboxAccountType, organization ) {
        def communicationMailboxAccount = new CommunicationMailboxAccount(
                encryptedPassword: "D359A3537A74FC42F284450BCCDDA734",
                organization: organization,
                type: communicationMailboxAccountType,
                emailAddress: "Registrar@BannerUniversity.edu",
                userName: "bannerRegUser" + communicationMailboxAccountType,
                emailDisplayName: "The Office of The Registrar"
        )
        return communicationMailboxAccount
    }


}

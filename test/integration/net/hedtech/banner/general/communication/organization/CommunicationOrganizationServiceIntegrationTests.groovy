/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.organization

import grails.util.Holders
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
    def encryptionKey = Holders.config.communication?.security?.password?.encKey


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
        if (originalListCount == 0) {
            CommunicationOrganization organization = new CommunicationOrganization();
            organization.name = "test"
            organization.description = "description"
            CommunicationOrganization createdOrganization = communicationOrganizationService.create( organization )
            assertNotNull( createdOrganization )

            long addedListCount = communicationOrganizationService.list().size()
            assertEquals( originalListCount + 1, addedListCount )
        } else {
            fail( "Cannot test, an organization already exists" )
        }
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
            assertTrue e.getMessage().contains( "onlyOneOrgCanExist" )
            //assertTrue e.getSqlException().toString().contains( "ORA-00001: unique constraint (GENERAL.GCRORAN_KEY_INDEX) violated" )
        }

    }


    @Test
    void testCreateMultiple() {
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
        sameNameOrganization.name = "testAnother"
        sameNameOrganization.description = "another organization that shouldnt be created"
        try {
            communicationOrganizationService.create( sameNameOrganization )
            Assert.fail "Expected sameNameOrganization to fail because only one org can exist."
        } catch (ApplicationException e) {
            assertTrue ("Failed to get expected exception onlyOneOrgCanExist",e.getMessage().contains( "onlyOneOrgCanExist" ))
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
        organization.receiveEmailServerProperties = [receiveProperties]
        organization.sendEmailServerProperties = [sendProperties]
        organization.senderMailboxAccountSettings = [senderMailboxAccountSettings]
        organization.replyToMailboxAccountSettings = [replyToMailboxAccountSettings]
        CommunicationOrganization createdOrganization = communicationOrganizationService.create( organization )
        assertNotNull( createdOrganization )
        assertEquals( "test", createdOrganization.name )
        assertEquals( "description", createdOrganization.description )
        assertEquals( "D359A3537A74FC42F284450BCCDDA734", createdOrganization.senderMailboxAccountSettings[0].encryptedPassword )
        assertEquals( "D359A3537A74FC42F284450BCCDDA734", createdOrganization.replyToMailboxAccountSettings[0].encryptedPassword )

    }


    @Test
    void testSetAndResetPassword() {
        def organization = new CommunicationOrganization()
        organization.name = "test"
        organization.description = "description"
        def senderMailboxAccountSettings = newCommunicationMailBoxProperties( CommunicationMailboxAccountType.Sender, organization )
        def replyToMailboxAccountSettings = newCommunicationMailBoxProperties( CommunicationMailboxAccountType.ReplyTo, organization )
        organization.senderMailboxAccountSettings = [senderMailboxAccountSettings]
        organization.replyToMailboxAccountSettings = [replyToMailboxAccountSettings]
        def createdOrganization = communicationOrganizationService.create( organization )
        assertNotNull( createdOrganization )
        assertEquals( "test", createdOrganization.name )
        assertEquals( "description", createdOrganization.description )
        assertEquals( "D359A3537A74FC42F284450BCCDDA734", createdOrganization.senderMailboxAccountSettings[0].encryptedPassword )
        assertEquals( "D359A3537A74FC42F284450BCCDDA734", createdOrganization.replyToMailboxAccountSettings[0].encryptedPassword )
        assertNull( "senderMailboxAccount cleartext password is not null", createdOrganization.senderMailboxAccountSettings[0].clearTextPassword )
        assertNull( "replyToMailboxAccount cleartext password is not null", createdOrganization.replyToMailboxAccountSettings[0].clearTextPassword )

        /* Do an update, with clearTextPassword null, the encrypted password should remain the same */
        createdOrganization.senderMailboxAccountSettings[0].userName = 'AstorPiazolla'
        def updatedOrganization = communicationOrganizationService.update( createdOrganization )
        assertNotNull( updatedOrganization.id )
        assertEquals( "D359A3537A74FC42F284450BCCDDA734", createdOrganization.senderMailboxAccountSettings[0].encryptedPassword )
        /* Do an update and set the clearTextPassword to something new, the encrypted password should change */
        createdOrganization.senderMailboxAccountSettings[0].clearTextPassword = "Unobtanium"
        updatedOrganization = communicationOrganizationService.update( createdOrganization )
        assertNotNull( updatedOrganization.id )
        assertTrue( "New encrypted password was not generated", "D359A3537A74FC42F284450BCCDDA734" != createdOrganization.senderMailboxAccountSettings[0].encryptedPassword )

    }


    @Test
    void testFetchByOrganizationIdAndType() {


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
                host: "TTTTTTTTTT",
                port: 1234,
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

/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.organization


import groovy.sql.Sql
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
    def clearTextPassword = "SuperSecretPassword"


    public void cleanUp() {
        def sql
        try {
            sessionFactory.currentSession.with { session ->
                sql = new Sql(session.connection())
                sql.executeUpdate("Delete from GCRORAN")
            }
        } finally {
            sql?.close()
        }
    }


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        cleanUp()
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
            CommunicationOrganization createdOrganization = communicationOrganizationService.create(organization)
            assertNotNull(createdOrganization)

            long addedListCount = communicationOrganizationService.list().size()
            assertEquals(originalListCount + 1, addedListCount)
            assertNull(organization.parent)
            assertFalse(organization.isAvailable)
        }
    }


    @Test
    void testCreate() {
        CommunicationOrganization organization = new CommunicationOrganization()
        organization.name = "test"
        organization.description = "description"
        CommunicationOrganization createdOrganization = communicationOrganizationService.create(organization)
        assertNotNull(createdOrganization)
        assertEquals("test", createdOrganization.name)
        assertEquals("description", createdOrganization.description)
        assertNull(createdOrganization.parent)
        assertFalse(createdOrganization.isAvailable)

        CommunicationOrganization foundOrganization = CommunicationOrganization.findByName("test")
        assertEquals(createdOrganization, foundOrganization)

        CommunicationOrganization rootorg = CommunicationOrganization.fetchRoot()
        assertEquals(rootorg.id, createdOrganization.id)

        CommunicationOrganization sameNameOrganization = new CommunicationOrganization()
        sameNameOrganization.name = "test"
        sameNameOrganization.description = "another organization with same name"
        try {
            communicationOrganizationService.create(sameNameOrganization)
            Assert.fail "Expected sameNameOrganization to fail because of unique name."
        } catch (ApplicationException e) {
            assertTrue("Failed to get expected exception key index constraint violation exception", e.getMessage().contains("GCRORAN_KEY_INDEX"))
        }

    }


    @Test
    void testCreateMultiple() {
        CommunicationOrganization organization = new CommunicationOrganization()
        organization.name = "test"
        organization.description = "description"
        CommunicationOrganization createdOrganization = communicationOrganizationService.create(organization)
        assertNotNull(createdOrganization)
        assertEquals("test", createdOrganization.name)
        assertEquals("description", createdOrganization.description)
        assertNull(createdOrganization.parent)

        CommunicationOrganization foundOrganization = CommunicationOrganization.findByName("test")
        assertEquals(createdOrganization, foundOrganization)

        CommunicationOrganization sameNameOrganization = new CommunicationOrganization()
        sameNameOrganization.name = "testAnother"
        sameNameOrganization.description = "another organization that shouldnt be created"
        sameNameOrganization = communicationOrganizationService.create(sameNameOrganization)
        assertEquals(foundOrganization.id,sameNameOrganization.parent );


        CommunicationOrganization rootorg = CommunicationOrganization.fetchRoot()
        assertEquals(rootorg.id, createdOrganization.id)

        CommunicationOrganization anotherOrganization = new CommunicationOrganization()
        anotherOrganization.name = "testAnotherOne"
        anotherOrganization.description = "another org description"
        anotherOrganization.setParent(rootorg.id)
        def anotherCreatedOrg = communicationOrganizationService.create(anotherOrganization)
        assertEquals("testAnotherOne", anotherCreatedOrg.name)
        assertNotNull(anotherCreatedOrg.parent)

        def orgCount = communicationOrganizationService.list()
        assertEquals(3, orgCount.size())


    }


    @Test
    void testUpdate() {
        CommunicationOrganization organization1 = new CommunicationOrganization()
        organization1.name = "organization1"
        organization1 = communicationOrganizationService.create(organization1)

        organization1 = CommunicationOrganization.get(organization1.getId())
        organization1.setName("organization1 changed")
        organization1.setDescription("description changed")
        organization1 = communicationOrganizationService.update(organization1)

        assertEquals("organization1 changed", organization1.getName())
        assertEquals("description changed", organization1.getDescription())

    }


    @Test
    void testDelete() {
        CommunicationOrganization organization1 = new CommunicationOrganization();
        organization1.name = "test"
        organization1.description = "description"
        CommunicationOrganization createdOrganization = communicationOrganizationService.create(organization1)
        assertNotNull(createdOrganization)
        Long id = createdOrganization.getId()

        long count = communicationOrganizationService.list().size()

        //set the flush mode to auto to prevent the write mode error during test
        //ssessionFactory.currentSession.flushMode = FlushMode.AUTO
        // Delete the domain
        communicationOrganizationService.delete(createdOrganization)

        assertEquals(count - 1, communicationOrganizationService.list().size())

        try {
            assertNull(communicationOrganizationService.get(id))
            Assert.fail "Expected get by id to fail because does not exist."
        } catch (ApplicationException e) {
            assertEquals("NotFoundException", e.getType())
        }
    }


    @Test
    void testCreateWithServerSettings() {
        def encryptedPassword = communicationOrganizationService.encryptMailBoxAccountPassword(clearTextPassword)
        CommunicationOrganization organization = new CommunicationOrganization()
        organization.name = "test"
        organization.description = "description"
        def receiveProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Receive, organization)
        def sendProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Send, organization)
        def senderMailboxAccountSettings = newCommunicationMailBoxProperties(CommunicationMailboxAccountType.Sender, organization)
        def replyToMailboxAccountSettings = newCommunicationMailBoxProperties(CommunicationMailboxAccountType.ReplyTo, organization)
        organization.receiveEmailServerProperties = [receiveProperties]
        organization.sendEmailServerProperties = [sendProperties]
        organization.senderMailboxAccountSettings = [senderMailboxAccountSettings]
        organization.replyToMailboxAccountSettings = [replyToMailboxAccountSettings]
        CommunicationOrganization createdOrganization = communicationOrganizationService.create(organization)
        assertNotNull(createdOrganization)
        assertEquals("test", createdOrganization.name)
        assertEquals("description", createdOrganization.description)
        assertEquals(encryptedPassword, createdOrganization.senderMailboxAccountSettings[0].encryptedPassword)
        assertEquals(encryptedPassword, createdOrganization.replyToMailboxAccountSettings[0].encryptedPassword)

    }


    @Test
    void testSetAndResetPassword() {
        def organization = new CommunicationOrganization()
        def encryptedPassword = communicationOrganizationService.encryptMailBoxAccountPassword(clearTextPassword)
        organization.name = "test"
        organization.description = "description"
        def senderMailboxAccountSettings = newCommunicationMailBoxProperties(CommunicationMailboxAccountType.Sender, organization)
        def replyToMailboxAccountSettings = newCommunicationMailBoxProperties(CommunicationMailboxAccountType.ReplyTo, organization)
        organization.senderMailboxAccountSettings = [senderMailboxAccountSettings]
        organization.replyToMailboxAccountSettings = [replyToMailboxAccountSettings]
        def savedOrganization = communicationOrganizationService.create(organization)
        /* try to force a fetch */
        def createdOrganization = CommunicationOrganization.findById(savedOrganization.id)
        assertNotNull(createdOrganization)
        assertEquals("test", createdOrganization.name)
        assertEquals("description", createdOrganization.description)
        assertEquals("senderMailboxAccount encryptedPassword password is not correct", encryptedPassword, createdOrganization.senderMailboxAccountSettings[0].encryptedPassword)
        assertEquals("replyToMailboxAccount encryptedPassword password is not correct", encryptedPassword, createdOrganization.replyToMailboxAccountSettings[0].encryptedPassword)

        /* Do an update of senderMailboxAccount, with clearTextPassword null, the encrypted password should remain the same */
        createdOrganization.senderMailboxAccountSettings[0].userName = 'AstorPiazolla'
        def updatedOrganization = communicationOrganizationService.update(createdOrganization)
        assertNotNull(updatedOrganization.id)
        assertEquals(encryptedPassword, createdOrganization.senderMailboxAccountSettings[0].encryptedPassword)
        /* Do an update and set the clearTextPassword to something new, the encrypted password should change */
        createdOrganization.senderMailboxAccountSettings[0].clearTextPassword = "Unobtanium"
        updatedOrganization = communicationOrganizationService.update(createdOrganization)
        assertNotNull(updatedOrganization.id)
        assertTrue("New encrypted password was not generated", encryptedPassword != createdOrganization.senderMailboxAccountSettings[0].encryptedPassword)

        /* Do an update of replyToMailboxAccount, with clearTextPassword null, the encrypted password should remain the same */
        createdOrganization.replyToMailboxAccountSettings[0].userName = 'DjangoRienhart'
        updatedOrganization = communicationOrganizationService.update(createdOrganization)
        assertNotNull(updatedOrganization.id)
        assertEquals(encryptedPassword, createdOrganization.replyToMailboxAccountSettings[0].encryptedPassword)
        /* Do an update and set the clearTextPassword to something new, the encrypted password should change */
        createdOrganization.replyToMailboxAccountSettings[0].clearTextPassword = "Unobtanium"
        updatedOrganization = communicationOrganizationService.update(createdOrganization)
        assertNotNull(updatedOrganization.id)
        assertTrue("New encrypted password was not generated", encryptedPassword != createdOrganization.replyToMailboxAccountSettings[0].encryptedPassword)

    }


    @Test
    void testPasswordEncryptAndDecrypt() {

        def encryptedPassword = communicationOrganizationService.encryptMailBoxAccountPassword(clearTextPassword)
        def decryptedPassword = communicationOrganizationService.decryptMailBoxAccountPassword(encryptedPassword)
        assertEquals(clearTextPassword, decryptedPassword)
    }


    private def newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType serverType, organization) {
        def communicationEmailServerProperties = new CommunicationEmailServerProperties(
                // Required fields
                host: "TTTTTTTTTT",
                port: 1234,
                organization: organization,
                type: serverType
        )
        return communicationEmailServerProperties
    }


    private
    def newCommunicationMailBoxProperties(CommunicationMailboxAccountType communicationMailboxAccountType, organization) {
        def communicationMailboxAccount = new CommunicationMailboxAccount(
                clearTextPassword: clearTextPassword,
                organization: organization,
                type: communicationMailboxAccountType,
                emailAddress: "Registrar@BannerUniversity.edu",
                userName: "bannerRegUser" + communicationMailboxAccountType,
                emailDisplayName: "The Office of The Registrar"
        )
        return communicationMailboxAccount
    }


}

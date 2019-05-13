/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.organization

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
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
@Integration
@Rollback
class CommunicationOrganizationServiceIntegrationTests extends BaseIntegrationTestCase {
    def communicationOrganizationService
    def communicationMailboxAccountService
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
            //sql?.close()
        }
    }


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testList() {
//        cleanUp()
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
//        cleanUp()
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
    void testMobileSettings() {
//        cleanUp()
        CommunicationOrganization organization = new CommunicationOrganization()
        organization.name = "Root"
        organization.description = "description"
        CommunicationOrganization createdOrganization = communicationOrganizationService.create(organization)
        assertNotNull(createdOrganization)
        assertEquals("Root", createdOrganization.name)
        assertEquals("description", createdOrganization.description)
        assertNull(createdOrganization.parent)
        assertFalse(createdOrganization.isAvailable)

        CommunicationOrganization rootorg = CommunicationOrganization.fetchRoot()
        assertEquals(rootorg.id, createdOrganization.id)

        rootorg.setMobileApplicationName("BCM_GO_MOBILE");
        rootorg.setMobileEndPointUrl("BCM_GO.com")
        rootorg.setClearMobileApplicationKey("password")
        CommunicationOrganization updatedRoot = communicationOrganizationService.update(rootorg)

        assertEquals("BCM_GO_MOBILE", updatedRoot.getMobileApplicationName())
        assertEquals("BCM_GO.com", updatedRoot.getMobileEndPointUrl())
        assertNotNull(updatedRoot.encryptedMobileApplicationKey)

        updatedRoot.setMobileApplicationName(null)
        updatedRoot.setClearMobileApplicationKey(null)
        CommunicationOrganization updatedRoot1 = communicationOrganizationService.update(updatedRoot)
        assertNull(updatedRoot1.mobileApplicationName)
        assertNull(updatedRoot1.encryptedMobileApplicationKey)
    }


    @Test
    void testLargeInvalidPassword() {
//        cleanUp()
        CommunicationOrganization organization = new CommunicationOrganization()
        organization.name = "Root"
        organization.description = "description"
        CommunicationOrganization createdOrganization = communicationOrganizationService.create(organization)
        assertNotNull(createdOrganization)
        assertEquals("Root", createdOrganization.name)
        assertEquals("description", createdOrganization.description)
        assertNull(createdOrganization.parent)
        assertFalse(createdOrganization.isAvailable)

        CommunicationOrganization rootorg = CommunicationOrganization.fetchRoot()
        assertEquals(rootorg.id, createdOrganization.id)

        rootorg.setMobileApplicationName("BCM_GO_MOBILE");
        rootorg.setMobileEndPointUrl("BCM_GO.com")
        //first set password to a large value and test if it errors out
        def largePassword = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012"
        rootorg.setClearMobileApplicationKey(largePassword)

        try {
            CommunicationOrganization updatedRoot = communicationOrganizationService.update(rootorg)
            fail "Should have failed with large password error"
        } catch (Exception e) {
            assertTrue e.toString().contains("12899")
        }
    }

    @Test
    void testLargeValidPassword() {
//        cleanUp()
        CommunicationOrganization organization = new CommunicationOrganization()
        organization.name = "Root"
        organization.description = "description"
        CommunicationOrganization createdOrganization = communicationOrganizationService.create(organization)
        assertNotNull(createdOrganization)
        assertEquals("Root", createdOrganization.name)
        assertEquals("description", createdOrganization.description)
        assertNull(createdOrganization.parent)
        assertFalse(createdOrganization.isAvailable)

        CommunicationOrganization rootorg = CommunicationOrganization.fetchRoot()
        assertEquals(rootorg.id, createdOrganization.id)

        rootorg.setMobileApplicationName("BCM_GO_MOBILE");
        rootorg.setMobileEndPointUrl("BCM_GO.com")
        //first set password to a large value and test if it errors out
        def largePassword = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901"
        rootorg.setClearMobileApplicationKey(largePassword)
        CommunicationOrganization updatedRoot = communicationOrganizationService.update(rootorg)

        assertEquals("BCM_GO_MOBILE", updatedRoot.getMobileApplicationName())
        assertEquals("BCM_GO.com", updatedRoot.getMobileEndPointUrl())
        assertNotNull(updatedRoot.encryptedMobileApplicationKey)
    }

    @Test
    void testCreateMultiple() {
//        cleanUp()
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
        assertEquals(foundOrganization.id, sameNameOrganization.parent);


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
//        cleanUp()
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
//        cleanUp()
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
    void testDeleteWithChildren() {
//        cleanUp()
        CommunicationOrganization organization = new CommunicationOrganization()
        organization.name = "test"
        organization.description = "description"
        def receiveProperties = newCommunicationEmailServer(CommunicationEmailServerPropertiesType.Receive)
        def sendProperties = newCommunicationEmailServer(CommunicationEmailServerPropertiesType.Send)
        def senderMailboxAccount = newCommunicationMailBox(CommunicationMailboxAccountType.Sender)
        def replyToMailboxAccount = newCommunicationMailBox(CommunicationMailboxAccountType.ReplyTo)
        organization.receiveEmailServerProperties = receiveProperties
        organization.sendEmailServerProperties = sendProperties
        organization.senderMailboxAccount = senderMailboxAccount
        organization.replyToMailboxAccount = replyToMailboxAccount
        CommunicationOrganization createdOrganization = communicationOrganizationService.create(organization)
        assertNotNull(createdOrganization)
        assertEquals("test", createdOrganization.name)
        assertEquals("description", createdOrganization.description)

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
//        cleanUp()
        def encryptedPassword = communicationOrganizationService.encryptPassword(clearTextPassword)
        CommunicationOrganization organization = new CommunicationOrganization()
        organization.name = "test"
        organization.description = "description"
        CommunicationOrganization createdOrganization = communicationOrganizationService.create(organization)

        createdOrganization.receiveEmailServerProperties = newCommunicationEmailServer(CommunicationEmailServerPropertiesType.Receive)
        createdOrganization.sendEmailServerProperties = newCommunicationEmailServer(CommunicationEmailServerPropertiesType.Send)
        createdOrganization.senderMailboxAccount = newCommunicationMailBox(CommunicationMailboxAccountType.Sender)
        createdOrganization.replyToMailboxAccount = newCommunicationMailBox(CommunicationMailboxAccountType.ReplyTo)
        createdOrganization = communicationOrganizationService.update(createdOrganization)

        assertNotNull(createdOrganization)
        assertEquals("test", createdOrganization.name)
        assertEquals("description", createdOrganization.description)
        assertEquals(encryptedPassword, createdOrganization.senderMailboxAccount.encryptedPassword)
        assertEquals(encryptedPassword, createdOrganization.replyToMailboxAccount.encryptedPassword)

    }


    @Test
    void testSetAndResetPassword() {
//        cleanUp()
        CommunicationOrganization organization = new CommunicationOrganization()
        def encryptedPassword = communicationMailboxAccountService.encryptPassword(clearTextPassword)
        organization.name = "test"
        organization.description = "description"
        organization = communicationOrganizationService.create(organization)
        CommunicationMailboxAccount senderMailboxAccount = newCommunicationMailBox(CommunicationMailboxAccountType.Sender)
        CommunicationMailboxAccount replyToMailboxAccount = newCommunicationMailBox(CommunicationMailboxAccountType.ReplyTo)
        organization.senderMailboxAccount = senderMailboxAccount
        organization.replyToMailboxAccount = replyToMailboxAccount
        CommunicationOrganization savedOrganization = communicationOrganizationService.update(organization)
        /* try to force a fetch */
        CommunicationOrganization createdOrganization = CommunicationOrganization.findById(savedOrganization.id)
        assertNotNull(createdOrganization)
        assertEquals("test", createdOrganization.name)
        assertEquals("description", createdOrganization.description)
        assertEquals("senderMailboxAccount encryptedPassword password is not correct", encryptedPassword, createdOrganization.senderMailboxAccount.encryptedPassword)
        assertEquals("replyToMailboxAccount encryptedPassword password is not correct", encryptedPassword, createdOrganization.replyToMailboxAccount.encryptedPassword)

        /* Do an update of senderMailboxAccount, with clearTextPassword null, the encrypted password should remain the same */
        createdOrganization.senderMailboxAccount.userName = 'AstorPiazolla'
        def updatedOrganization = communicationOrganizationService.update(createdOrganization)
        assertNotNull(updatedOrganization.id)
        assertEquals(encryptedPassword, createdOrganization.senderMailboxAccount.encryptedPassword)
        /* Do an update and set the clearTextPassword to something new, the encrypted password should change */
        createdOrganization.senderMailboxAccount.clearTextPassword = "Unobtanium"
        updatedOrganization = communicationOrganizationService.update(createdOrganization)
        assertNotNull(updatedOrganization.id)
        assertEquals( encryptedPassword, createdOrganization.senderMailboxAccount.encryptedPassword )

        /* Do an update of replyToMailboxAccount, with clearTextPassword null, the encrypted password should remain the same */
        createdOrganization.replyToMailboxAccount.userName = 'DjangoRienhart'
        updatedOrganization = communicationOrganizationService.update(createdOrganization)
        assertNotNull(updatedOrganization.id)
        assertEquals(encryptedPassword, createdOrganization.replyToMailboxAccount.encryptedPassword)
        /* Do an update and set the clearTextPassword to something new, the encrypted password should change */
        createdOrganization.replyToMailboxAccount.clearTextPassword = "Unobtanium"
        updatedOrganization = communicationOrganizationService.update(createdOrganization)
        assertNotNull(updatedOrganization.id)
        assertEquals(encryptedPassword, createdOrganization.replyToMailboxAccount.encryptedPassword)

    }


    @Test
    void testPasswordEncryptAndDecrypt() {
//        cleanUp()
        def encryptedPassword = communicationOrganizationService.encryptPassword(clearTextPassword)
        def decryptedPassword = communicationOrganizationService.decryptPassword(encryptedPassword)
        assertEquals(clearTextPassword, decryptedPassword)
    }


    @Test
    void testMaxPasswordEncryptAndDecrypt() {
//        cleanUp()
        def largepassword = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901"
        def encryptedPassword = communicationOrganizationService.encryptPassword(largepassword)
        def decryptedPassword = communicationOrganizationService.decryptPassword(encryptedPassword)
        assertEquals(largepassword, decryptedPassword)
    }

    private def newCommunicationEmailServer(CommunicationEmailServerPropertiesType serverType) {
        CommunicationEmailServerProperties communicationEmailServerProperties = new CommunicationEmailServerProperties(
                // Required fields
                host: "TTTTTTTTTT",
                port: 1234,
                type: serverType
        )
        communicationEmailServerProperties.save( flush: true )
        return communicationEmailServerProperties
    }


    private def newCommunicationMailBox(CommunicationMailboxAccountType communicationMailboxAccountType) {
        CommunicationMailboxAccount communicationMailboxAccount = new CommunicationMailboxAccount(
                clearTextPassword: clearTextPassword,
                encryptedPassword: communicationMailboxAccountService.encryptPassword( clearTextPassword ),
                type: communicationMailboxAccountType,
                emailAddress: "Registrar@BannerUniversity.edu",
                userName: "bannerRegUser" + communicationMailboxAccountType,
                emailDisplayName: "The Office of The Registrar"
        )
        communicationMailboxAccount.save( flush: true )
        return communicationMailboxAccount
    }


}

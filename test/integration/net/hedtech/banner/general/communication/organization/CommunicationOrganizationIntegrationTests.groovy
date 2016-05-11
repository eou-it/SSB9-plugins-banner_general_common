/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.organization

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * OrganizationTest.
 */
class CommunicationOrganizationIntegrationTests extends BaseIntegrationTestCase {

    def i_valid_name = "My Organization"
    def i_valid_description = "My Organization"


    def u_valid_name = "My Organization1"
    def u_valid_description = "My Organization1"


    def i_invalid_name = "My Organization".padLeft(1021)
    def i_invalid_description = "My Organization".padLeft(4001)


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
        formContext = ['GUAGMNU']
        super.setUp()
        cleanUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateValidOrganization() {
        def organization = newValidForCreateOrganization()
        organization.isAvailable = true
        def receiveProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Receive )
        def sendProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Send )
        organization.receiveEmailServerProperties = receiveProperties
        organization.sendEmailServerProperties = sendProperties
        organization.save(failOnError: true, flush: true)
        assertNotNull organization.receiveEmailServerProperties
        assertNotNull organization.sendEmailServerProperties
        assertNull(organization.parent)
        assertTrue(organization.isAvailable)


    }


    @Test
    void testDelete() {
        def organization = newValidForCreateOrganization()
        organization.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull organization.id
        organization.delete()
        def id = organization.id
        assertNull organization.get(id)
    }


    @Test
    void testUpdate() {
        def organization = newValidForCreateOrganization()
        organization.save()
        //Test if the generated entity now has an id assigned
        assertNotNull organization.id
        assertNull(organization.parent)
        assertFalse(organization.isAvailable)
        organization.description = u_valid_description
        organization.isAvailable = true
        organization.name = u_valid_name

        organization.save()
        def id = organization.id
        def updatedOrganization = organization.get(id)
        assertEquals("Updated description", u_valid_description, updatedOrganization.description)

        assertEquals("Updated name", u_valid_name, updatedOrganization.name)
        assertTrue(updatedOrganization.isAvailable)
        // Test update of dependent objects
        def receiveProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Receive )
        organization.receiveEmailServerProperties = receiveProperties
        organization.save()
        assertEquals 1234L, organization.receiveEmailServerProperties.port

    }


    @Test
    void testPassword() {
        def organization = newValidForCreateOrganization()
        def receiveProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Receive )
        def sendProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Send )
        def senderMailboxAccount = newCommunicationMailBox(CommunicationMailboxAccountType.Sender )
        def replyToMailboxAccount = newCommunicationMailBox(CommunicationMailboxAccountType.ReplyTo )
        organization.receiveEmailServerProperties = receiveProperties
        organization.sendEmailServerProperties = sendProperties
        organization.senderMailboxAccount = senderMailboxAccount
        organization.replyToMailboxAccount = replyToMailboxAccount
        organization.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull organization.id
        assertNotNull organization.receiveEmailServerProperties.id
        assertNotNull organization.sendEmailServerProperties.id
        assertNotNull organization.senderMailboxAccount.encryptedPassword
        assertNotNull organization.replyToMailboxAccount.encryptedPassword
    }


    @Test
    void testCreateInValidOrganization() {
        def organization = newValidForCreateOrganization()

        organization = newValidForCreateOrganization()
        organization.description = i_invalid_description
        shouldFail { organization.save(failOnError: true, flush: true) }


        organization = newValidForCreateOrganization()
        organization.name = i_invalid_name
        shouldFail { organization.save(failOnError: true, flush: true) }
    }


    @Test
    void testFetchMailBoxAccountByOrganizationId() {
        def organization = newValidForCreateOrganization()
        organization.senderMailboxAccount = newCommunicationMailBox(CommunicationMailboxAccountType.Sender )
        organization.replyToMailboxAccount = newCommunicationMailBox(CommunicationMailboxAccountType.ReplyTo )
        organization.save(failOnError: true, flush: true)

        def mailboxAccountList = CommunicationMailboxAccount.findAll()
        assertEquals(2, mailboxAccountList.size())
    }


    @Test
    void testList() {
        def organization = newValidForCreateOrganization()
        organization.senderMailboxAccount = newCommunicationMailBox( CommunicationMailboxAccountType.Sender )
        organization.save(failOnError: true, flush: true)
        def organizationList = CommunicationOrganization.list()
        assertNotNull( organizationList[0].senderMailboxAccount )
        assertNull( organizationList[0].replyToMailboxAccount )

        /* Now add another */
        organization.replyToMailboxAccount = newCommunicationMailBox( CommunicationMailboxAccountType.ReplyTo )
        organization.save(failOnError: true, flush: true)

        organizationList = CommunicationOrganization.list()
        assertNotNull(organization)
        assertNotNull(organizationList[0]?.replyToMailboxAccount)
        assertNotNull(organizationList[0]?.senderMailboxAccount)
    }


    private def newValidForCreateOrganization() {
        def organization = new CommunicationOrganization(
                description: i_valid_description,
                name: i_valid_name
        )
        return organization
    }


    private def newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType serverType) {
        def communicationEmailServerProperties = new CommunicationEmailServerProperties(
                // Required fields
                host: "TTTTTTTTTT",
                port: 1234,
                type: serverType
        )
        communicationEmailServerProperties.save()
        return communicationEmailServerProperties
    }


    private
    def newCommunicationMailBox(CommunicationMailboxAccountType communicationMailboxAccountType) {


        def communicationMailboxAccount = new CommunicationMailboxAccount(
                encryptedPassword: "supersecretpassword",
                type: communicationMailboxAccountType,
                emailAddress: "Registrar@BannerUniversity.edu",
                userName: "bannerRegUser" + communicationMailboxAccountType,
                emailDisplayName: "The Office of The Registrar"
        )
        communicationMailboxAccount.save()
        return communicationMailboxAccount
    }

}

/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.organization

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException
import static groovy.test.GroovyAssert.*
/**
 * Integration tests for communicationEmailServerProperties entity
 */
@Integration
@Rollback
class CommunicationEmailServerPropertiesIntegrationTests extends BaseIntegrationTestCase {
    def organization


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
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateCommunicationEmailServerProperties() {
//        cleanUp()
        def receiveProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Receive)
        def sendProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Send)
        receiveProperties.save(failOnError: true, flush: true)
        sendProperties.save(failOnError: true, flush: true)

        // Assert domain values
        assertNotNull receiveProperties?.id
        assertEquals CommunicationEmailServerConnectionSecurity.None, receiveProperties.securityProtocol
        assertEquals "TTTTTTTTTT", receiveProperties.host
        assertEquals 1234, receiveProperties.port
        assertEquals CommunicationEmailServerPropertiesType.Receive, receiveProperties.type

        // Assert domain values
        assertNotNull receiveProperties?.id
        assertEquals CommunicationEmailServerConnectionSecurity.None, sendProperties.securityProtocol
        assertEquals "TTTTTTTTTT", sendProperties.host
        assertEquals 1234, sendProperties.port
        assertEquals CommunicationEmailServerPropertiesType.Send, sendProperties.type
        assertEquals '' , sendProperties.smtpProperties
    }


    @Test
    void testUpdateCommunicationEmailServerProperties() {
//        cleanUp()
        def communicationEmailServerProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Send)
        communicationEmailServerProperties.save(failOnError: true, flush: true)

        // Assert domain values
        assertNotNull communicationEmailServerProperties?.id
        def id = communicationEmailServerProperties.id

        // Find the domain
        communicationEmailServerProperties = communicationEmailServerProperties.get(id)
        assertNotNull communicationEmailServerProperties

        // Update domain values
        communicationEmailServerProperties.securityProtocol = CommunicationEmailServerConnectionSecurity.SSL

        communicationEmailServerProperties.save(failOnError: true, flush: true)

        // Find the updated domain
        communicationEmailServerProperties = communicationEmailServerProperties.get(id)

        // Assert updated domain values
        assertNotNull communicationEmailServerProperties?.id
        assertEquals(CommunicationEmailServerConnectionSecurity.SSL, communicationEmailServerProperties.securityProtocol)
        assertEquals '' , communicationEmailServerProperties.smtpProperties
    }


    @Test
    void testDeleteCommunicationEmailServerProperties() {
//        cleanUp()
        def communicationEmailServerProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Receive)
        communicationEmailServerProperties.save(failOnError: true, flush: true)

        // Assert domain values
        assertNotNull communicationEmailServerProperties?.id
        def id = communicationEmailServerProperties.id

        // Find the domain
        communicationEmailServerProperties = communicationEmailServerProperties.get(id)
        assertNotNull communicationEmailServerProperties

        // Delete the domain
        communicationEmailServerProperties.delete()

        // Attempt to find the deleted domain
        communicationEmailServerProperties = communicationEmailServerProperties.get(id)
        assertNull communicationEmailServerProperties
    }


    @Test
    void testNullValidationFailure() {
//        cleanUp()
        // Instantiate an empty domain
        def communicationEmailServerProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Receive)
        // TODO: implement these assertions
        // Assert for domain validation
        // assertFalse "communicationEmailServerProperties should have failed null value validation", communicationEmailServerProperties.validate()

        // Assert for specific field validation
        /* assertErrorsFor communicationEmailServerProperties, 'nullable',
                 ['securityProtocol',
                  'smtpHost',
                  'smtpPort',
                  'organization'
                 ]
         assertNoErrorsFor communicationEmailServerProperties,
                 [

                 ]*/
    }


    @Test
    void testMaxSizeValidationFailure() {
//        cleanUp()
        def communicationEmailServerProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Receive)

        // Set domain values to exceed maximum allowed length
        communicationEmailServerProperties.host = """To Long""".padLeft(2001)

        // Assert for domain
        assertFalse "communicationEmailServerProperties should have failed max size validation", communicationEmailServerProperties.validate()

        // Assert for specific fields
        assertErrorsFor communicationEmailServerProperties, 'maxSize',
                [
                        'host',
                ]
    }


    @Test
    void testOptimisticLock() {
//        cleanUp()
        def communicationEmailServerProperties = newCommunicationEmailServerProperties(CommunicationEmailServerPropertiesType.Receive)
        communicationEmailServerProperties.save(failOnError: true, flush: true)
        assertNotNull communicationEmailServerProperties?.id

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("UPDATE gcbsprp SET gcbsprp_version = 999 WHERE gcbsprp_surrogate_id = ?", [communicationEmailServerProperties.id])
        } finally {
//            sql?.close()
        }

        // Update the entity
        communicationEmailServerProperties.dataOrigin = "OPT_TEST"
        shouldFail(HibernateOptimisticLockingFailureException) {
            communicationEmailServerProperties.save(failOnError: true, flush: true)
        }
    }


    private def newCommunicationEmailServerProperties(serverType) {
        def communicationEmailServerProperties = new CommunicationEmailServerProperties(
                // Required fields
                securityProtocol: CommunicationEmailServerConnectionSecurity.None,
                host: "TTTTTTTTTT",
                port: 1234,
                type: serverType,
                smtpProperties: ''
        )
        return communicationEmailServerProperties
    }

}

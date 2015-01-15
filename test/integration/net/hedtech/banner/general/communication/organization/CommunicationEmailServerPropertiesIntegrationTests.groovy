/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.organization

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

/**
 * Integration tests for communicationEmailServerProperties entity
 */
class CommunicationEmailServerPropertiesIntegrationTests extends BaseIntegrationTestCase {

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
        def communicationEmailServerProperties = newCommunicationEmailServerProperties()
        communicationEmailServerProperties.save( failOnError: true, flush: true )

        // Assert domain values
        assertNotNull communicationEmailServerProperties?.id
        assertEquals "TTTTTTTTTT", communicationEmailServerProperties.securityProtocol
        assertEquals "TTTTTTTTTT", communicationEmailServerProperties.smtpHost
        assertEquals 1234, communicationEmailServerProperties.smtpPort
    }


    @Test
    void testUpdateCommunicationEmailServerProperties() {
        def communicationEmailServerProperties = newCommunicationEmailServerProperties()
        communicationEmailServerProperties.save( failOnError: true, flush: true )

        // Assert domain values
        assertNotNull communicationEmailServerProperties?.id
        def id = communicationEmailServerProperties.id

        // Find the domain
        communicationEmailServerProperties = communicationEmailServerProperties.get( id )
        assertNotNull communicationEmailServerProperties

        // Update domain values
        communicationEmailServerProperties.securityProtocol = "###"

        communicationEmailServerProperties.save( failOnError: true, flush: true )

        // Find the updated domain
        communicationEmailServerProperties = communicationEmailServerProperties.get( id )

        // Assert updated domain values
        assertNotNull communicationEmailServerProperties?.id
        assertEquals( "###", communicationEmailServerProperties.securityProtocol )

    }


    @Test
    void testDeleteCommunicationEmailServerProperties() {
        def communicationEmailServerProperties = newCommunicationEmailServerProperties()
        communicationEmailServerProperties.save( failOnError: true, flush: true )

        // Assert domain values
        assertNotNull communicationEmailServerProperties?.id
        def id = communicationEmailServerProperties.id

        // Find the domain
        communicationEmailServerProperties = communicationEmailServerProperties.get( id )
        assertNotNull communicationEmailServerProperties

        // Delete the domain
        communicationEmailServerProperties.delete()

        // Attempt to find the deleted domain
        communicationEmailServerProperties = communicationEmailServerProperties.get( id )
        assertNull communicationEmailServerProperties
    }


    @Test
    void testNullValidationFailure() {
        // Instantiate an empty domain
        def communicationEmailServerProperties = new CommunicationEmailServerProperties()

        // Assert for domain validation
        assertFalse "communicationEmailServerProperties should have failed null value validation", communicationEmailServerProperties.validate()

        // Assert for specific field validation
        assertErrorsFor communicationEmailServerProperties, 'nullable',
                ['securityProtocol',
                 'smtpHost',
                // TODO fix nullability test for 'smtpPort',
                ]
        assertNoErrorsFor communicationEmailServerProperties,
                [

                ]
    }


    @Test
    void testMaxSizeValidationFailure() {
        def communicationEmailServerProperties = newCommunicationEmailServerProperties()

        // Set domain values to exceed maximum allowed length
        communicationEmailServerProperties.securityProtocol = """To Long""".padLeft( 2001 )
        communicationEmailServerProperties.smtpHost = """To Long""".padLeft( 2001 )

        // Assert for domain
        assertFalse "communicationEmailServerProperties should have failed max size validation", communicationEmailServerProperties.validate()

        // Assert for specific fields
        assertErrorsFor communicationEmailServerProperties, 'maxSize',
                [
                        'securityProtocol',
                        'smtpHost',
                ]
    }


    @Test
    void testOptimisticLock() {
        def communicationEmailServerProperties = newCommunicationEmailServerProperties()
        communicationEmailServerProperties.save( failOnError: true, flush: true )
        assertNotNull communicationEmailServerProperties?.id

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "UPDATE gcbsprp SET gcbsprp_version = 999 WHERE gcbsprp_surrogate_id = ?", [communicationEmailServerProperties.id] )
        } finally {
            sql?.close()
        }

        // Update the entity
        communicationEmailServerProperties.dataOrigin = "OPT_TEST"
        shouldFail( HibernateOptimisticLockingFailureException ) { communicationEmailServerProperties.save( failOnError: true, flush: true ) }
    }


    private def newCommunicationEmailServerProperties() {
        def communicationEmailServerProperties = new CommunicationEmailServerProperties(
                // Required fields
                securityProtocol: "TTTTTTTTTT",
                smtpHost: "TTTTTTTTTT",
                smtpPort: 1234,
                // Nullable fields

        )

        return communicationEmailServerProperties
    }

}

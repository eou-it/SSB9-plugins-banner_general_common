/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.log

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

/**
 * Integration tests for CommunicationLog entity
 */
class CommunicationLogIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU', 'SELFSERVICE']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateCommunicationLog() {
        def communicationLog = newCommunicationLog()
        communicationLog.save( failOnError: true, flush: true )

        // Assert domain values
        assertNotNull communicationLog?.id
        assertEquals "TTTTTTTTTT", communicationLog.commChannel
        assertEquals "TTTTTTTTTT", communicationLog.creatorId
        assertEquals "TTTTTTTT", communicationLog.errorText
        assertEquals "TTTTTTTTTT", communicationLog.organizationName
        assertEquals 50216, communicationLog.pidm
        assertEquals "TTTTTTTTTT", communicationLog.sendItemReferenceId
        assertEquals "TTTTTTTTTT", communicationLog.status
        assertEquals "TTTTTTTTTT", communicationLog.templateName
    }


    @Test
    void testUpdateCommunicationLog() {
        def communicationLog = newCommunicationLog()
        communicationLog.save( failOnError: true, flush: true )

        // Assert domain values
        assertNotNull communicationLog?.id
        def id = communicationLog.id

        // Find the domain
        communicationLog = CommunicationLog.get( id )
        assertNotNull communicationLog

        // Update domain values
        communicationLog.status = "ERROR"
        communicationLog.save( failOnError: true, flush: true )

        // Find the updated domain
        communicationLog = CommunicationLog.get( id )

        // Assert updated domain values
        assertNotNull communicationLog?.id
        assertEquals( "ERROR", communicationLog.status )
    }


    @Test
    void testDeleteCommunicationLog() {
        def communicationLog = newCommunicationLog()
        communicationLog.save( failOnError: true, flush: true )

        // Assert domain values
        assertNotNull communicationLog?.id
        def id = communicationLog.id

        // Find the domain
        communicationLog = CommunicationLog.get( id )
        assertNotNull communicationLog

        // Delete the domain
        communicationLog.delete()

        // Attempt to find the deleted domain
        communicationLog = CommunicationLog.get( id )
        assertNull communicationLog
    }


    @Test
    void testNullValidationFailure() {
        // Instantiate an empty domain
        def communicationLog = new CommunicationLog()

        // Assert for domain validation
        assertFalse "CommunicationLog should have failed null value validation", communicationLog.validate()

        // Assert for specific field validation
        assertErrorsFor communicationLog, 'nullable',
                [
                        'commChannel',
                        'creatorId',
                        'pidm',
                        'status',
                ]
        assertNoErrorsFor communicationLog,
                [
                        'communicationItemId',
                        'errorText',
                        'organizationName',
                        'sendItemReferenceId',
                        'templateName',
                ]
    }


    @Test
    void testMaxSizeValidationFailure() {
        def communicationLog = newCommunicationLog()

        // Set domain values to exceed maximum allowed length
        communicationLog.commChannel = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"
        communicationLog.creatorId = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"
        communicationLog.sendItemReferenceId = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"
        communicationLog.organizationName = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT".padLeft(1021)
        communicationLog.templateName = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT".padLeft(2049)
        communicationLog.status = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT".padLeft(31)


        // Assert for domain
        assertFalse "CommunicationLog should have failed max size validation", communicationLog.validate()

        // Assert for specific fields
        assertErrorsFor communicationLog, 'maxSize',
                [
                        'commChannel',
                        'creatorId',
                        'organizationName',
                        'sendItemReferenceId',
                        'status',
                        'templateName',
                ]
    }


    @Test
    void testOptimisticLock() {
        def communicationLog = newCommunicationLog()
        communicationLog.save( failOnError: true, flush: true )
        assertNotNull communicationLog?.id

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "UPDATE gcbclog SET gcbclog_version = 999 WHERE gcbclog_surrogate_id = ?", [communicationLog.id] )
        } finally {
            sql?.close()
        }

        // Update the entity
        communicationLog.dataOrigin = "OPT_TEST"
        shouldFail( HibernateOptimisticLockingFailureException ) { communicationLog.save( failOnError: true, flush: true ) }
    }


    private CommunicationLog newCommunicationLog() {
        CommunicationLog communicationLog = new CommunicationLog(
                // Required fields
                commChannel: "TTTTTTTTTT",
                creatorId: "TTTTTTTTTT",
                pidm: 50216,
                status: "TTTTTTTTTT",

                // Nullable fields
                communicationItemId: 9999999999999999999,
                errorText: "TTTTTTTT",
                organizationName: "TTTTTTTTTT",
                sendItemReferenceId: "TTTTTTTTTT",
                templateName: "TTTTTTTTTT",
        )

        return communicationLog
    }

}

package net.hedtech.banner.general.communication.log
/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for CommunicationLogService service
 */
class CommunicationLogServiceIntegrationTests extends BaseIntegrationTestCase {

    def communicationLogService


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateCommunicationLog() {
        CommunicationLog communicationLog = newCommunicationLog()
        communicationLog = communicationLog.save(failOnError: true, flush: true)

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
        communicationLog = communicationLog.save(failOnError: true, flush: true)
        assertNotNull communicationLog
        def id = communicationLog.id

        // Find the domain
        communicationLog = communicationLog.get( id )
        assertNotNull communicationLog?.id

        // Update domain values
        communicationLog.status = "ERROR"
        communicationLog = communicationLog.save(failOnError: true, flush: true)

        // Find the updated domain
        communicationLog = communicationLog.get( id )

        // Assert updated domain values
        assertNotNull communicationLog
        assertEquals "ERROR", communicationLog.status
    }


    @Test
    void testDeleteCommunicationLog() {
        def communicationLog = newCommunicationLog()
        communicationLog = communicationLog.save(failOnError: true, flush: true)
        assertNotNull communicationLog
        def id = communicationLog.id

        // Find the domain
        communicationLog = communicationLog.get( id )
        assertNotNull communicationLog

        // Delete the domain
        communicationLog.delete(failOnError: true, flush: true)

        // Attempt to find the deleted domain
        communicationLog = communicationLog.get( id )
        assertNull communicationLog
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

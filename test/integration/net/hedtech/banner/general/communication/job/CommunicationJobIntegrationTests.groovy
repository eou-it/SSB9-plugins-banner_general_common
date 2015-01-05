/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.job

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

/**
 * Integration tests for CommunicationJob entity
 */
class CommunicationJobIntegrationTests extends BaseIntegrationTestCase {

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
    void testCreateCommunicationJob() {
        def CommunicationJob = newCommunicationJob()
        CommunicationJob.save( failOnError: true, flush: true )

        // Assert domain values
        assertNotNull CommunicationJob?.id
        assertEquals "TTTTTTTTTT", CommunicationJob.referenceId
        assertEquals "TTTTTTTTTT", CommunicationJob.status
    }


    @Test
    void testUpdateCommunicationJob() {
        def CommunicationJob = newCommunicationJob()
        CommunicationJob.save( failOnError: true, flush: true )

        // Assert domain values
        assertNotNull CommunicationJob?.id
        def id = CommunicationJob.id

        // Find the domain
        CommunicationJob = CommunicationJob.get( id )
        assertNotNull CommunicationJob

        // Update domain values
        CommunicationJob.status = "ERROR"
        CommunicationJob.save( failOnError: true, flush: true )

        // Find the updated domain
        CommunicationJob = CommunicationJob.get( id )

        // Assert updated domain values
        assertNotNull CommunicationJob?.id
        assertEquals( "ERROR", CommunicationJob.status )
    }


    @Test
    void testDeleteCommunicationJob() {
        def CommunicationJob = newCommunicationJob()
        CommunicationJob.save( failOnError: true, flush: true )

        // Assert domain values
        assertNotNull CommunicationJob?.id
        def id = CommunicationJob.id

        // Find the domain
        CommunicationJob = CommunicationJob.get( id )
        assertNotNull CommunicationJob

        // Delete the domain
        CommunicationJob.delete()

        // Attempt to find the deleted domain
        CommunicationJob = CommunicationJob.get( id )
        assertNull CommunicationJob
    }


    @Test
    void testNullValidationFailure() {
        // Instantiate an empty domain
        def CommunicationJob = new CommunicationJob()

        // Assert for domain validation
        assertFalse "CommunicationJob should have failed null value validation", CommunicationJob.validate()

        // Assert for specific field validation
        assertErrorsFor CommunicationJob, 'nullable',
                [
                        'referenceId',
                        'status',
                ]

    }


    @Test
    void testMaxSizeValidationFailure() {
        def CommunicationJob = newCommunicationJob()

        // Set domain values to exceed maximum allowed length
        CommunicationJob.referenceId = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"
        CommunicationJob.status = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT".padLeft( 31 )

        // Assert for domain
        assertFalse "CommunicationJob should have failed max size validation", CommunicationJob.validate()

        // Assert for specific fields
        assertErrorsFor CommunicationJob, 'maxSize',
                [
                        'referenceId',
                        'status',

                ]
    }


    @Test
    void testOptimisticLock() {
        def CommunicationJob = newCommunicationJob()
        CommunicationJob.save( failOnError: true, flush: true )
        assertNotNull CommunicationJob?.id

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "UPDATE gcbcjob SET gcbcjob_version = 999 WHERE gcbcjob_surrogate_id = ?", [CommunicationJob.id] )
        } finally {
            sql?.close()
        }

        // Update the entity
        CommunicationJob.dataOrigin = "OPT_TEST"
        shouldFail( HibernateOptimisticLockingFailureException ) { CommunicationJob.save( failOnError: true, flush: true ) }
    }


    private CommunicationJob newCommunicationJob() {
        CommunicationJob CommunicationJob = new CommunicationJob(
                // Required fields
                referenceId: "TTTTTTTTTT",
                status: "TTTTTTTTTT",
        )

        return CommunicationJob
    }

}

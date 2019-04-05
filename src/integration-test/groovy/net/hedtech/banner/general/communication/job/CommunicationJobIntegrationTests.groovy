/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.job

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

import static groovy.test.GroovyAssert.shouldFail

/**
 * Integration tests for CommunicationJob entity
 */
@Integration
@Rollback
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
        assertEquals CommunicationJobStatus.PENDING, CommunicationJob.status
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
        CommunicationJob.status = CommunicationJobStatus.FAILED
        CommunicationJob.save( failOnError: true, flush: true )

        // Find the updated domain
        CommunicationJob = CommunicationJob.get( id )

        // Assert updated domain values
        assertNotNull CommunicationJob?.id
        assertEquals( CommunicationJobStatus.FAILED, CommunicationJob.status )
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
                        'referenceId'
                ]

    }


    @Test
    void testMaxSizeValidationFailure() {
        def CommunicationJob = newCommunicationJob()

        // Set domain values to exceed maximum allowed length
        CommunicationJob.referenceId = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"

        // Assert for domain
        assertFalse "CommunicationJob should have failed max size validation", CommunicationJob.validate()

        // Assert for specific fields
        assertErrorsFor CommunicationJob, 'maxSize',
                [
                        'referenceId'
                ]
    }


    @Test
    void testOptimisticLock() {
        def communicationJob = newCommunicationJob()
        communicationJob.save( failOnError: true, flush: true )
        assertNotNull communicationJob?.id

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "UPDATE gcbcjob SET gcbcjob_version = 999 WHERE gcbcjob_surrogate_id = ?", [communicationJob.id] )
        } finally {
            //sql?.close()
        }

        // Update the entity
        communicationJob.dataOrigin = "OPT_TEST"
        shouldFail( HibernateOptimisticLockingFailureException ) {
            communicationJob.save( failOnError: true, flush: true )
        }
    }


    private CommunicationJob newCommunicationJob() {
        CommunicationJob CommunicationJob = new CommunicationJob(
                // Required fields
                referenceId: "TTTTTTTTTT",
                creationDateTime: new Date()
        )

        return CommunicationJob
    }

}

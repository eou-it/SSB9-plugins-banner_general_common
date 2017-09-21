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
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Integration tests for CommunicationJob entity
 */
class CommunicationJobIntegrationTests extends BaseIntegrationTestCase {

    /**
     * Convenience method to login a user if not already logged in. You may pass in a username and password,
     * or omit and accept the default 'grails_user' and 'u_pick_it' for admin and 'HOSWEB002' and '111111' for ssb
     **/
    @Override
    protected void loginIfNecessary(String username,password) {
        println "In the loginIfNecessary of the Communication JOb INtegration tests"
        println "The username is "+username
        println "the password is "+password
        if (!SecurityContextHolder.getContext().getAuthentication()) {
            println "There is no authentication"
            if(username != null && username.empty || (password != null && password.Empty())){
                println "Everything about the username is empty"
                username = "grails_user"
                password = "u_pick_it"
            } else {
                println "The username test has failed "+username.empty+"***"
                println "The password test is "+password.Empty()+"***"
            }
            println "Going to log in with the username password"
            println "The username after is "+username
            println "the password after is "+password
            login username, password
        } else {
            println "There is authentication " + SecurityContextHolder.getContext().getAuthentication()
        }
    }
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
                creationDateTime: new Date()
        )

        return CommunicationJob
    }

}

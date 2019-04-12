/*********************************************************************************
 Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Before
import org.junit.Test
import org.junit.After

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException
import static groovy.test.GroovyAssert.*

@Integration
@Rollback
class FacultyTenureStatusIntegrationTests extends BaseIntegrationTestCase {

    //Valid test data (For success tests)

    def i_success_code = "TT"
    def i_success_description = "TTTTT"
    def i_success_dateIndicator = true
    def i_success_reviewDateIndicator = true
    def i_success_eeoTenureIndicator = "I"
    //Invalid test data (For failure tests)

    def i_failure_code = "XXXXX"
    def i_failure_description = "0123456789012345678901234567890123456789"
    def i_failure_dateIndicator = null
    def i_failure_reviewDateIndicator = null
    def i_failure_eeoTenureIndicator = "X"

    //Test data for creating updating domain instance
    //Valid test data (For success tests)

    def u_success_code = "TT"
    def u_success_description = "012345678901234567890123456789"
    def u_success_dateIndicator = true
    def u_success_reviewDateIndicator = true
    def u_success_eeoTenureIndicator = "T"
    //Valid test data (For failure tests)

    def u_failure_code = "TT"
    def u_failure_description = "0123456789012345678901234567890123456789"
    def u_failure_dateIndicator = null
    def u_failure_reviewDateIndicator = null
    def u_failure_eeoTenureIndicator = "X"

    @Before
    public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateValidFacultyTenureStatus() {
        def facultyTenureStatus = newValidForCreateFacultyTenureStatus()
        facultyTenureStatus.save(flush: true, failOnError: true)
        //Test if the generated entity now has an id assigned
        assertNotNull facultyTenureStatus.id
    }


    @Test
    void testCreateInvalidFacultyTenureStatus() {
        def facultyTenureStatus = newInvalidForCreateFacultyTenureStatus()
        shouldFail(ValidationException) {
            facultyTenureStatus.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidFacultyTenureStatus() {
        def facultyTenureStatus = newValidForCreateFacultyTenureStatus()
        facultyTenureStatus.save(flush: true, failOnError: true)
        assertNotNull facultyTenureStatus.id
        assertEquals 0L, facultyTenureStatus.version
        assertEquals i_success_code, facultyTenureStatus.code
        assertEquals i_success_description, facultyTenureStatus.description
        assertEquals i_success_dateIndicator, facultyTenureStatus.dateIndicator
        assertEquals i_success_reviewDateIndicator, facultyTenureStatus.reviewDateIndicator
        assertEquals i_success_eeoTenureIndicator, facultyTenureStatus.eeoTenureIndicator

        //Update the entity
        facultyTenureStatus.description = u_success_description
        facultyTenureStatus.dateIndicator = u_success_dateIndicator
        facultyTenureStatus.reviewDateIndicator = u_success_reviewDateIndicator
        facultyTenureStatus.eeoTenureIndicator = u_success_eeoTenureIndicator
        facultyTenureStatus.save(flush: true, failOnError: true)
        //Asset for sucessful update
        facultyTenureStatus = FacultyTenureStatus.get(facultyTenureStatus.id)
        assertEquals 1L, facultyTenureStatus?.version
        assertEquals u_success_description, facultyTenureStatus.description
        assertEquals u_success_dateIndicator, facultyTenureStatus.dateIndicator
        assertEquals u_success_reviewDateIndicator, facultyTenureStatus.reviewDateIndicator
        assertEquals u_success_eeoTenureIndicator, facultyTenureStatus.eeoTenureIndicator
    }


    @Test
    void testUpdateInvalidFacultyTenureStatus() {
        def facultyTenureStatus = newValidForCreateFacultyTenureStatus()
        facultyTenureStatus.save(flush: true, failOnError: true)
        assertNotNull facultyTenureStatus.id
        assertEquals 0L, facultyTenureStatus.version
        assertEquals i_success_code, facultyTenureStatus.code
        assertEquals i_success_description, facultyTenureStatus.description
        assertEquals i_success_dateIndicator, facultyTenureStatus.dateIndicator
        assertEquals i_success_reviewDateIndicator, facultyTenureStatus.reviewDateIndicator
        assertEquals i_success_eeoTenureIndicator, facultyTenureStatus.eeoTenureIndicator

        //Update the entity with invalid values
        facultyTenureStatus.description = u_failure_description
        facultyTenureStatus.dateIndicator = u_failure_dateIndicator
        facultyTenureStatus.reviewDateIndicator = u_failure_reviewDateIndicator
        facultyTenureStatus.eeoTenureIndicator = u_failure_eeoTenureIndicator
        shouldFail(ValidationException) {
            facultyTenureStatus.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testOptimisticLock() {
        def facultyTenureStatus = newValidForCreateFacultyTenureStatus()
        facultyTenureStatus.save(flush: true, failOnError: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update PTRTENR set PTRTENR_VERSION = 999 where PTRTENR_SURROGATE_ID = ?", [facultyTenureStatus.id])
        } finally {
//            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        facultyTenureStatus.description = u_success_description
        facultyTenureStatus.dateIndicator = u_success_dateIndicator
        facultyTenureStatus.reviewDateIndicator = u_success_reviewDateIndicator
        facultyTenureStatus.eeoTenureIndicator = u_success_eeoTenureIndicator
        shouldFail(HibernateOptimisticLockingFailureException) {
            facultyTenureStatus.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteFacultyTenureStatus() {
        def facultyTenureStatus = newValidForCreateFacultyTenureStatus()
        facultyTenureStatus.save(flush: true, failOnError: true)
        def id = facultyTenureStatus.id
        assertNotNull id
        facultyTenureStatus.delete()
        assertNull FacultyTenureStatus.get(id)
    }


    @Test
    void testValidation() {
        def facultyTenureStatus = newValidForCreateFacultyTenureStatus()
        assertTrue "FacultyTenureStatus could not be validated as expected due to ${facultyTenureStatus.errors}", facultyTenureStatus.validate()
    }


    @Test
    void testNullValidationFailure() {
        def facultyTenureStatus = new FacultyTenureStatus()
        assertFalse "FacultyTenureStatus should have failed validation", facultyTenureStatus.validate()
        assertErrorsFor facultyTenureStatus, 'nullable',
                [
                        'code',
                        'description',
                        'dateIndicator',
                        'reviewDateIndicator',
                        'eeoTenureIndicator'
                ]
    }


    @Test
    void testInListValidationFailure() {
        def facultyTenureStatus = newValidForCreateFacultyTenureStatus()
        facultyTenureStatus.eeoTenureIndicator = i_failure_eeoTenureIndicator
        assertFalse "FacultyTenureStatus should have failed validation", facultyTenureStatus.validate()
        assertErrorsFor facultyTenureStatus, 'inList', ['eeoTenureIndicator']
    }


    @Test
    void testMaxSizeValidationFailures() {
        def facultyTenureStatus = newValidForCreateFacultyTenureStatus()
        facultyTenureStatus.code = i_failure_code
        facultyTenureStatus.description = i_failure_description
        assertFalse "FacultyTenureStatus should have failed validation", facultyTenureStatus.validate()
        assertErrorsFor facultyTenureStatus, 'maxSize', ['code', 'description']
    }


    private def newValidForCreateFacultyTenureStatus() {
        def facultyTenureStatus = new FacultyTenureStatus(
                code: i_success_code,
                description: i_success_description,
                dateIndicator: i_success_dateIndicator,
                reviewDateIndicator: i_success_reviewDateIndicator,
                eeoTenureIndicator: i_success_eeoTenureIndicator
        )
        return facultyTenureStatus
    }


    private def newInvalidForCreateFacultyTenureStatus() {
        def facultyTenureStatus = new FacultyTenureStatus(
                code: i_failure_code,
                description: i_failure_description,
                dateIndicator: i_failure_dateIndicator,
                reviewDateIndicator: i_failure_reviewDateIndicator,
                eeoTenureIndicator: i_failure_eeoTenureIndicator
        )
        return facultyTenureStatus
    }

}

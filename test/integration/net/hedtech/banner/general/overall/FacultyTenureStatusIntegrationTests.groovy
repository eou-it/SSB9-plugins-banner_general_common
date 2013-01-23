/** *******************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 ********************************************************************************* */
/**
 Banner Automator Version: 1.21
 Generated: Wed Jul 20 14:02:47 EDT 2011 
 */
package net.hedtech.banner.general.overall

import net.hedtech.banner.testing.BaseIntegrationTestCase
import grails.validation.ValidationException
import groovy.sql.Sql
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

class FacultyTenureStatusIntegrationTests extends BaseIntegrationTestCase {

    /*PROTECTED REGION ID(facultytenurestatus_domain_integration_test_data) ENABLED START*/
    //Test data for creating new domain instance
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
    /*PROTECTED REGION END*/


    protected void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
        initializeTestDataForReferences()
    }

    //This method is used to initialize test data for references.
    //A method is required to execute database calls as it requires a active transaction
    void initializeTestDataForReferences() {
        /*PROTECTED REGION ID(facultytenurestatus_domain_integration_test_data_initialization) ENABLED START*/
        //Valid test data (For success tests)

        //Invalid test data (For failure tests)

        //Valid test data (For success tests)

        //Valid test data (For failure tests)

        //Test data for references for custom tests
        /*PROTECTED REGION END*/
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateValidFacultyTenureStatus() {
        def facultyTenureStatus = newValidForCreateFacultyTenureStatus()
        facultyTenureStatus.save(flush: true, failOnError: true)
        //Test if the generated entity now has an id assigned
        assertNotNull facultyTenureStatus.id
    }


    void testCreateInvalidFacultyTenureStatus() {
        def facultyTenureStatus = newInvalidForCreateFacultyTenureStatus()
        shouldFail(ValidationException) {
            facultyTenureStatus.save(failOnError: true, flush: true)
        }
    }


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


    void testOptimisticLock() {
        def facultyTenureStatus = newValidForCreateFacultyTenureStatus()
        facultyTenureStatus.save(flush: true, failOnError: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update PTRTENR set PTRTENR_VERSION = 999 where PTRTENR_SURROGATE_ID = ?", [facultyTenureStatus.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
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


    void testDeleteFacultyTenureStatus() {
        def facultyTenureStatus = newValidForCreateFacultyTenureStatus()
        facultyTenureStatus.save(flush: true, failOnError: true)
        def id = facultyTenureStatus.id
        assertNotNull id
        facultyTenureStatus.delete()
        assertNull FacultyTenureStatus.get(id)
    }


    void testValidation() {
        def facultyTenureStatus = newValidForCreateFacultyTenureStatus()
        assertTrue "FacultyTenureStatus could not be validated as expected due to ${facultyTenureStatus.errors}", facultyTenureStatus.validate()
    }


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


    void testInListValidationFailure() {
        def facultyTenureStatus = newValidForCreateFacultyTenureStatus()
        facultyTenureStatus.eeoTenureIndicator = i_failure_eeoTenureIndicator
        assertFalse "FacultyTenureStatus should have failed validation", facultyTenureStatus.validate()
        assertErrorsFor facultyTenureStatus, 'inList', ['eeoTenureIndicator']
    }


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

    /**
     * Please put all the custom tests in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(facultytenurestatus_custom_integration_test_methods) ENABLED START*/
    /*PROTECTED REGION END*/
}

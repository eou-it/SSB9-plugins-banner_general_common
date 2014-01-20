/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import net.hedtech.banner.general.person.PersonUtility
import java.text.SimpleDateFormat
import grails.validation.ValidationException

class PidmAndUDCIdMappingIntegrationTests extends BaseIntegrationTestCase {

    //Test data for creating new domain instance
    //Valid test data (For success tests)

    def i_success_udcId = "TTTTT"
    def i_success_pidm = 1
    def i_success_createDate = new Date()
    //Invalid test data (For failure tests)

    def i_failure_udcId = "TTTTT"
    def i_failure_pidm = 1
    def i_failure_createDate = new Date()

    //Test data for creating updating domain instance
    //Valid test data (For success tests)

    def u_success_udcId = "TTTTT"
    def u_success_pidm = 1
    def u_success_createDate = new Date()
    //Valid test data (For failure tests)

    def u_failure_udcId = "TTTTT"
    def u_failure_pidm = 1
    def u_failure_createDate = new Date()


    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initializeTestDataForReferences()
    }

    //This method is used to initialize test data for references.
    //A method is required to execute database calls as it requires a active transaction
    void initializeTestDataForReferences() {
        i_success_pidm = PersonUtility.getPerson("HOS00001").pidm

    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateValidPidmAndUDCIdMapping() {
        def pidmAndUDCIdMapping = newValidForCreatePidmAndUDCIdMapping()
        pidmAndUDCIdMapping.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull pidmAndUDCIdMapping.id
    }


    void testUpdateValidPidmAndUDCIdMapping() {
        def pidmAndUDCIdMapping = newValidForCreatePidmAndUDCIdMapping()
        pidmAndUDCIdMapping.save(failOnError: true, flush: true)
        assertNotNull pidmAndUDCIdMapping.id
        assertEquals 0L, pidmAndUDCIdMapping.version
        assertEquals i_success_udcId, pidmAndUDCIdMapping.udcId
        assertEquals i_success_pidm, pidmAndUDCIdMapping.pidm
        assertEquals i_success_createDate, pidmAndUDCIdMapping.createDate

        //Update the entity
        pidmAndUDCIdMapping.pidm = u_success_pidm
        pidmAndUDCIdMapping.createDate = u_success_createDate
        pidmAndUDCIdMapping.save(failOnError: true, flush: true)
        //Assert for sucessful update
        pidmAndUDCIdMapping = PidmAndUDCIdMapping.get(pidmAndUDCIdMapping.id)
        assertEquals 1L, pidmAndUDCIdMapping?.version
        assertEquals u_success_pidm, pidmAndUDCIdMapping.pidm
        assertEquals u_success_createDate, pidmAndUDCIdMapping.createDate
    }


    void testUpdateInvalidPidmAndUDCIdMapping() {
        def pidmAndUDCIdMapping = newValidForCreatePidmAndUDCIdMapping()
        pidmAndUDCIdMapping.save(failOnError: true, flush: true)
        assertNotNull pidmAndUDCIdMapping.id
        assertEquals 0L, pidmAndUDCIdMapping.version
        assertEquals i_success_udcId, pidmAndUDCIdMapping.udcId
        assertEquals i_success_pidm, pidmAndUDCIdMapping.pidm
        assertEquals i_success_createDate, pidmAndUDCIdMapping.createDate

        //Update the entity with invalid values
        pidmAndUDCIdMapping.pidm = null
        pidmAndUDCIdMapping.createDate = null
        shouldFail(ValidationException) {
            pidmAndUDCIdMapping.save(failOnError: true, flush: true)
        }
    }


    void testDates() {
        def time = new SimpleDateFormat('HHmmss')
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def pidmAndUDCIdMapping = newValidForCreatePidmAndUDCIdMapping()
        pidmAndUDCIdMapping.createDate = new Date()

        pidmAndUDCIdMapping.save(flush: true, failOnError: true)
        pidmAndUDCIdMapping.refresh()
        assertNotNull "PidmAndUDCIdMapping should have been saved", pidmAndUDCIdMapping.id

        // test date values -
        assertEquals date.format(today), date.format(pidmAndUDCIdMapping.lastModified)
        assertEquals hour.format(today), hour.format(pidmAndUDCIdMapping.lastModified)

        assertEquals time.format(pidmAndUDCIdMapping.createDate), "000000"

    }


    void testOptimisticLock() {
        def pidmAndUDCIdMapping = newValidForCreatePidmAndUDCIdMapping()
        pidmAndUDCIdMapping.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GV_GOBUMAP set GOBUMAP_VERSION = 999 where GOBUMAP_SURROGATE_ID = ?", [pidmAndUDCIdMapping.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        pidmAndUDCIdMapping.pidm = u_success_pidm
        pidmAndUDCIdMapping.createDate = u_success_createDate
        shouldFail(HibernateOptimisticLockingFailureException) {
            pidmAndUDCIdMapping.save(failOnError: true, flush: true)
        }
    }


    void testDeletePidmAndUDCIdMapping() {
        def pidmAndUDCIdMapping = newValidForCreatePidmAndUDCIdMapping()
        pidmAndUDCIdMapping.save(failOnError: true, flush: true)
        def id = pidmAndUDCIdMapping.id
        def deleteMe = PidmAndUDCIdMapping.get(id)
        assertNotNull deleteMe
        deleteMe.delete()
        assertNull PidmAndUDCIdMapping.get(id)
    }


    void testValidation() {
        def pidmAndUDCIdMapping = new PidmAndUDCIdMapping()
        assertFalse "PidmAndUDCIdMapping could not be validated as expected due to ${pidmAndUDCIdMapping.errors}", pidmAndUDCIdMapping.validate()
    }


    void testNullValidationFailure() {
        def pidmAndUDCIdMapping = new PidmAndUDCIdMapping()
        assertFalse "PidmAndUDCIdMapping should have failed validation", pidmAndUDCIdMapping.validate()
        assertErrorsFor pidmAndUDCIdMapping, 'nullable',
                        [
                                'udcId',
                                'pidm',
                                'createDate'
                        ]
    }


    void testFetchByUdcId() {
        def pidmAndUDCIdMapping = newValidForCreatePidmAndUDCIdMapping()
        pidmAndUDCIdMapping.save(failOnError: true, flush: true)
        def id = pidmAndUDCIdMapping.id

        def udcId = PidmAndUDCIdMapping.fetchByUdcId(i_success_udcId)
        assertNotNull udcId
    }


    private def newValidForCreatePidmAndUDCIdMapping() {
        def pidmAndUDCIdMapping = new PidmAndUDCIdMapping(
                udcId: i_success_udcId,
                pidm: i_success_pidm,
                createDate: i_success_createDate,
                )
        return pidmAndUDCIdMapping
    }


    private def newInvalidForCreatePidmAndUDCIdMapping() {
        def pidmAndUDCIdMapping = new PidmAndUDCIdMapping(
                udcId: i_failure_udcId,
                pidm: i_failure_pidm,
                createDate: i_failure_createDate,
                )
        return pidmAndUDCIdMapping
    }

}

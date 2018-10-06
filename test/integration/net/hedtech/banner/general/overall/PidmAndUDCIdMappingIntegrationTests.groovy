/*******************************************************************************
 Copyright 2013 - 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.GeneralCommonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

import java.text.SimpleDateFormat

class PidmAndUDCIdMappingIntegrationTests extends BaseIntegrationTestCase {

    //Test data for creating new domain instance
    //Valid test data (For success tests)

    def i_success_udcId = "TTTTT"
    def i_success_pidm
    def i_success_createDate = new Date()
    def i_success_udcId_2 = "ZZZZZ"
    def i_success_pidm_2
    def banner_ids = ["HOS00001","HOS00002"]

    //Test data for creating updating domain instance
    //Valid test data (For success tests)

    def u_success_udcId = "TTTTT"
    def u_success_pidm
    def u_success_createDate = new Date()


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initializeTestDataForReferences()
    }

    //This method is used to initialize test data for references.
    //A method is required to execute database calls as it requires a active transaction
    void initializeTestDataForReferences() {
        i_success_pidm = PersonUtility.getPerson("HOS00001").pidm
        i_success_pidm_2 = PersonUtility.getPerson("HOS00002").pidm
        u_success_pidm = PersonUtility.getPerson("HOS00002").pidm
		[i_success_pidm, i_success_pidm_2].each { pidm ->
			def deleteMe = PidmAndUDCIdMapping.findByPidm(pidm)
			if (deleteMe) {
				deleteMe = deleteMe.get(deleteMe.id).refresh()
				deleteMe.delete(failOnError: true, flush: true)
			}
		}
		def enterpriseIds = PidmAndUDCIdMapping.fetchEnterpriseIdsByBannerIdList(banner_ids)
		enterpriseIds.each { enterpriseId ->
			def deleteMe = PidmAndUDCIdMapping.findByUdcId(enterpriseId)
			if (deleteMe) {
				deleteMe = deleteMe.get(deleteMe.id).refresh()
				deleteMe.delete(failOnError: true, flush: true)
			}
		}
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateValidPidmAndUDCIdMapping() {
        def pidmAndUDCIdMapping = newValidForCreatePidmAndUDCIdMapping()
        pidmAndUDCIdMapping.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull pidmAndUDCIdMapping.id
    }


    @Test
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


    @Test
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


    @Test
    void testDates() {
        def time = new SimpleDateFormat('HHmmss')
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = GeneralCommonUtility.getSystemDate()

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


    @Test
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


    @Test
    void testDeletePidmAndUDCIdMapping() {
        def pidmAndUDCIdMapping = newValidForCreatePidmAndUDCIdMapping()
        pidmAndUDCIdMapping.save(failOnError: true, flush: true)
        def id = pidmAndUDCIdMapping.id
        def deleteMe = PidmAndUDCIdMapping.get(id)
        assertNotNull deleteMe
        deleteMe.delete(failOnError: true, flush: true)
        assertNull PidmAndUDCIdMapping.get(id)
    }


    @Test
    void testValidation() {
        def pidmAndUDCIdMapping = new PidmAndUDCIdMapping()
        assertFalse "PidmAndUDCIdMapping could not be validated as expected due to ${pidmAndUDCIdMapping.errors}", pidmAndUDCIdMapping.validate()
    }


    @Test
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


    @Test
    void testFetchByUdcId() {
        def pidmAndUDCIdMapping = newValidForCreatePidmAndUDCIdMapping()
        pidmAndUDCIdMapping.save(failOnError: true, flush: true)
        def id = pidmAndUDCIdMapping.id

        def result = PidmAndUDCIdMapping.fetchByUdcId(i_success_udcId)
        assertNotNull result
		assertEquals i_success_udcId, result.udcId
		assertEquals i_success_pidm, result.pidm
    }


    @Test
    void testFetchByUdcIdList() {
        def pidmAndUDCIdMappings = newMultipleValidForCreatePidmAndUDCIdMapping()
        pidmAndUDCIdMappings.each { it.save(failOnError: true, flush: true)}
        def results = PidmAndUDCIdMapping.fetchByUdcList(pidmAndUDCIdMappings.udcId)
        assertEquals 2, results.size()
        assertTrue results[0] instanceof PidmAndUDCIdMapping
    }


    @Test
	void testGenerateAndFetchEnterpriseIdsByBannerIdList() {
		def results = PidmAndUDCIdMapping.fetchEnterpriseIdsByBannerIdList(banner_ids)
		assertEquals 0, results.size()
		PidmAndUDCIdMapping.generateByBannerIdList(banner_ids)
		results = PidmAndUDCIdMapping.fetchEnterpriseIdsByBannerIdList(banner_ids)
		assertEquals 2, results.size()
		assertTrue results[0] instanceof String
	}
	

    private def newValidForCreatePidmAndUDCIdMapping() {
        def pidmAndUDCIdMapping = new PidmAndUDCIdMapping(
            udcId: i_success_udcId,
            pidm: i_success_pidm,
            createDate: i_success_createDate,
        )
        return pidmAndUDCIdMapping
    }


    private def newMultipleValidForCreatePidmAndUDCIdMapping() {
        def pidmAndUDCIdMappings = []
        pidmAndUDCIdMappings.add(newValidForCreatePidmAndUDCIdMapping())
        pidmAndUDCIdMappings.add(new PidmAndUDCIdMapping(
            udcId: i_success_udcId_2,
            pidm: i_success_pidm_2,
            createDate: i_success_createDate)
        )
        return pidmAndUDCIdMappings
    }

}

/*******************************************************************************
 Copyright 2013-2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.PortOfEntry
import net.hedtech.banner.general.system.VisaIssuingAuthority
import net.hedtech.banner.general.system.VisaType
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

import java.text.SimpleDateFormat

class VisaInformationIntegrationTests extends BaseIntegrationTestCase {

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
    void testCreateValidVisaInformation() {
        def visaInformation = newValidForCreateVisaInformation()
        visaInformation.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull visaInformation.id
    }


    @Test
    void testCreateInvalidVisaInformation() {
        def visaInformation = newInvalidForCreateVisaInformation()
        shouldFail(ValidationException) {
            visaInformation.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidVisaInformation() {
        def visaInformation = newValidForCreateVisaInformation()
        visaInformation.save(failOnError: true, flush: true)
        assertNotNull visaInformation.id
        assertEquals 0L, visaInformation.version
        assertEquals PersonUtility.getPerson("HOR000008").pidm, visaInformation.pidm
        assertEquals 1, visaInformation.sequenceNumber
        assertEquals "123456789012345678", visaInformation.visaNumber
        assertEquals "1", visaInformation.nationIssue
        assertEquals true, visaInformation.entryIndicator
        assertEquals "M", visaInformation.numberEntries
        assertEquals "F1", visaInformation.visaType.code
        assertEquals "PARIS", visaInformation.visaIssuingAuthority.code
        assertEquals "NYC", visaInformation.portOfEntry.code

        //Update the entity
        visaInformation.visaNumber = "123456789012UPDATE"
        visaInformation.nationIssue = "2"
        visaInformation.entryIndicator = false
        visaInformation.numberEntries = ""
        visaInformation.visaIssuingAuthority = VisaIssuingAuthority.findByCode("CHINAE")
        visaInformation.portOfEntry = PortOfEntry.findByCode("CHI")
        visaInformation.save(failOnError: true, flush: true)

        //Assert for sucessful update
        visaInformation = VisaInformation.get(visaInformation.id)
        assertEquals 1L, visaInformation?.version
        assertEquals "123456789012UPDATE", visaInformation.visaNumber
        assertEquals "2", visaInformation.nationIssue
        assertEquals false, visaInformation.entryIndicator
        assertEquals "", visaInformation.numberEntries
        assertEquals "CHINAE", visaInformation.visaIssuingAuthority.code
        assertEquals "CHI", visaInformation.portOfEntry.code
    }


    @Test
    void testUpdateInvalidVisaInformation() {
        def visaInformation = newValidForCreateVisaInformation()
        visaInformation.save(failOnError: true, flush: true)
        assertNotNull visaInformation.id
        assertEquals 0L, visaInformation.version
        assertEquals PersonUtility.getPerson("HOR000008").pidm, visaInformation.pidm
        assertEquals 1, visaInformation.sequenceNumber
        assertEquals "123456789012345678", visaInformation.visaNumber
        assertEquals "1", visaInformation.nationIssue
        assertEquals true, visaInformation.entryIndicator
        assertEquals "M", visaInformation.numberEntries
        assertEquals "F1", visaInformation.visaType.code
        assertEquals "PARIS", visaInformation.visaIssuingAuthority.code
        assertEquals "NYC", visaInformation.portOfEntry.code

        //Update the entity with invalid values
        visaInformation.visaNumber = "123456789012345678Z"
        visaInformation.nationIssue = "12345Z"
        shouldFail(ValidationException) {
            visaInformation.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDates() {
        def time = new SimpleDateFormat('HHmmss')
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = getSystemDate()

        def visaInformation = newValidForCreateVisaInformation()

        visaInformation.visaStartDate = new Date()
        visaInformation.visaExpireDate = new Date()
        visaInformation.visaRequiredDate = new Date()
        visaInformation.visaIssueDate = new Date()

        visaInformation.save(flush: true, failOnError: true)
        visaInformation.refresh()
        assertNotNull "VisaInformation should have been saved", visaInformation.id

        // test date values -
        assertEquals date.format(today), date.format(visaInformation.lastModified)
        assertEquals hour.format(today), hour.format(visaInformation.lastModified)

        assertEquals time.format(visaInformation.visaStartDate), "000000"
        assertEquals time.format(visaInformation.visaExpireDate), "000000"
        assertEquals time.format(visaInformation.visaRequiredDate), "000000"
        assertEquals time.format(visaInformation.visaIssueDate), "000000"
    }


    @Test
    void testOptimisticLock() {
        def visaInformation = newValidForCreateVisaInformation()
        visaInformation.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GV_GORVISA set GORVISA_VERSION = 999 where GORVISA_SURROGATE_ID = ?", [visaInformation.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        visaInformation.nationIssue = "2"
        shouldFail(HibernateOptimisticLockingFailureException) {
            visaInformation.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteVisaInformation() {
        def visaInformation = newValidForCreateVisaInformation()
        visaInformation.save(failOnError: true, flush: true)
        def id = visaInformation.id
        assertNotNull id
        visaInformation.delete()
        assertNull VisaInformation.get(id)
    }


    @Test
    void testValidation() {
        def visaInformation = newInvalidForCreateVisaInformation()
        assertFalse "VisaInformation could not be validated as expected due to ${visaInformation.errors}", visaInformation.validate()
    }


    @Test
    void testNullValidationFailure() {
        def visaInformation = new VisaInformation()
        assertFalse "VisaInformation should have failed validation", visaInformation.validate()
        assertErrorsFor visaInformation, 'nullable',
                [
                        'pidm',
                        'sequenceNumber',
                        'entryIndicator',
                        'visaType'
                ]
        assertNoErrorsFor visaInformation,
                [
                        'visaNumber',
                        'nationIssue',
                        'visaStartDate',
                        'visaExpireDate',
                        'visaRequiredDate',
                        'visaIssueDate',
                        'numberEntries',
                        'visaIssuingAuthority',
                        'portOfEntry'
                ]
    }


    @Test
    void testMaxSizeValidationFailures() {
        def visaInformation = new VisaInformation(
                visaNumber: 'XXXXXXXXXXXXXXXXXXXX',
                nationIssue: 'XXXXXXX',
                numberEntries: 'XXXX')
        assertFalse "VisaInformation should have failed validation", visaInformation.validate()
        assertErrorsFor visaInformation, 'maxSize', ['visaNumber', 'nationIssue', 'numberEntries']
    }


    @Test
    void testCreateOverlappingExpireDateExists() {
        def visaInformation = newValidForCreateVisaInformation()
        visaInformation.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull visaInformation.id

        // Date does not overlap
        assertFalse VisaInformation.overlappingExpireDateExists(PersonUtility.getPerson("HOR000008").pidm, 2, new Date()+1, new Date()+1)

        // Date that do overrlap
        assertTrue VisaInformation.overlappingExpireDateExists(PersonUtility.getPerson("HOR000008").pidm, 2, new Date(), new Date()+1)
        assertTrue VisaInformation.overlappingExpireDateExists(PersonUtility.getPerson("HOR000008").pidm, 2, new Date(), new Date())
    }


    @Test
	void testFetchByPidmListAndDateCompare() {
		def currentDate = new Date()
		def startDate = currentDate - 7
		def expireDate = currentDate + 7
		def visaInformation = newValidForCreateVisaInformation()
        visaInformation.visaStartDate = startDate
        visaInformation.visaExpireDate = expireDate
        visaInformation.visaRequiredDate = visaInformation.visaStartDate
        visaInformation.visaIssueDate = visaInformation.visaStartDate
		visaInformation.save(failOnError: true, flush: true)
		// Test if the generated entity now has an id assigned
		assertNotNull visaInformation.id

		// Test that the entity is returned for the current date and start/expire dates
		assertEquals 1, VisaInformation.fetchByPidmListAndDateCompare([PersonUtility.getPerson("HOR000008").pidm], currentDate).size()
		assertEquals 1, VisaInformation.fetchByPidmListAndDateCompare([PersonUtility.getPerson("HOR000008").pidm], startDate).size()
		assertEquals 1, VisaInformation.fetchByPidmListAndDateCompare([PersonUtility.getPerson("HOR000008").pidm], expireDate).size()

		// Test dates out of range
		assertEquals 0, VisaInformation.fetchByPidmListAndDateCompare([PersonUtility.getPerson("HOR000008").pidm], startDate - 1).size()
		assertEquals 0, VisaInformation.fetchByPidmListAndDateCompare([PersonUtility.getPerson("HOR000008").pidm], expireDate + 1).size()

        // Test null expiration date
        visaInformation.visaExpireDate = null
        visaInformation.save(failOnError: true, flush: true)
        assertEquals 1, VisaInformation.fetchByPidmListAndDateCompare([PersonUtility.getPerson("HOR000008").pidm], currentDate).size()

        // Test null start date
        visaInformation.visaStartDate = null
        visaInformation.save(failOnError: true, flush: true)
        assertEquals 0, VisaInformation.fetchByPidmListAndDateCompare([PersonUtility.getPerson("HOR000008").pidm], currentDate).size()
	}


    private def newValidForCreateVisaInformation() {
        def visaInformation = new VisaInformation(
                pidm: PersonUtility.getPerson("HOR000008").pidm,
                sequenceNumber: 1,
                visaNumber: "123456789012345678",
                nationIssue: "1",
                visaStartDate: new Date(),
                visaExpireDate: new Date(),
                entryIndicator: true,
                visaRequiredDate: new Date(),
                visaIssueDate: new Date(),
                numberEntries: "M",
                visaType: VisaType.findByCode("F1"),
                visaIssuingAuthority: VisaIssuingAuthority.findByCode("PARIS"),
                portOfEntry: PortOfEntry.findByCode("NYC"),
        )
        return visaInformation
    }


    private def newInvalidForCreateVisaInformation() {
        def visaInformation = new VisaInformation()
        return visaInformation
    }
}

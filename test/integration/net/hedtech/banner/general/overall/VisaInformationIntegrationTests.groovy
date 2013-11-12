/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

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

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateValidVisaInformation() {
        def visaInformation = newValidForCreateVisaInformation()
        visaInformation.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull visaInformation.id
    }


    void testCreateInvalidVisaInformation() {
        def visaInformation = newInvalidForCreateVisaInformation()
        shouldFail(ValidationException) {
            visaInformation.save(failOnError: true, flush: true)
        }
    }


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


    void testDates() {
        def time = new SimpleDateFormat('HHmmss')
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

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


    void testDeleteVisaInformation() {
        def visaInformation = newValidForCreateVisaInformation()
        visaInformation.save(failOnError: true, flush: true)
        def id = visaInformation.id
        assertNotNull id
        visaInformation.delete()
        assertNull VisaInformation.get(id)
    }


    void testValidation() {
        def visaInformation = newInvalidForCreateVisaInformation()
        assertFalse "VisaInformation could not be validated as expected due to ${visaInformation.errors}", visaInformation.validate()
    }


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


    void testMaxSizeValidationFailures() {
        def visaInformation = new VisaInformation(
                visaNumber: 'XXXXXXXXXXXXXXXXXXXX',
                nationIssue: 'XXXXXXX',
                numberEntries: 'XXXX')
        assertFalse "VisaInformation should have failed validation", visaInformation.validate()
        assertErrorsFor visaInformation, 'maxSize', ['visaNumber', 'nationIssue', 'numberEntries']
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

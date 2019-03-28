/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.*
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException

import java.text.SimpleDateFormat

class VisaInternationalInformationIntegrationTests extends BaseIntegrationTestCase {

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
    void testCreateValidVisaInternationalInformation() {
        def visaInternationalInformation = newValidForCreateVisaInternationalInformation()
        visaInternationalInformation.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull visaInternationalInformation.id
    }


    @Test
    void testCreateInvalidVisaInternationalInformation() {
        def visaInternationalInformation = newInvalidForCreateVisaInternationalInformation()
        shouldFail(ValidationException) {
            visaInternationalInformation.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidVisaInternationalInformation() {
        def visaInternationalInformation = newValidForCreateVisaInternationalInformation()
        visaInternationalInformation.save(failOnError: true, flush: true)
        assertNotNull visaInternationalInformation.id
        assertEquals 0L, visaInternationalInformation.version
        assertEquals PersonUtility.getPerson("HOR000008").pidm, visaInternationalInformation.pidm
        assertEquals "Y", visaInternationalInformation.spouseIndicator
        assertEquals "N", visaInternationalInformation.signatureIndicator
        assertEquals "1234567890123456789012345", visaInternationalInformation.passportId
        assertEquals "1", visaInternationalInformation.nationIssue
        assertEquals "123", visaInternationalInformation.i94Status
        assertEquals "12345678901", visaInternationalInformation.registrationNumber
        assertEquals "Y", visaInternationalInformation.duration
        assertEquals "1234567890X", visaInternationalInformation.certificateNumber
        assertEquals "2", visaInternationalInformation.nationBirth
        assertEquals "3", visaInternationalInformation.nationLegal
        assertEquals "123456789012345", visaInternationalInformation.foreignSsn
        assertEquals 99, visaInternationalInformation.childNumber
        assertEquals "I-20", visaInternationalInformation.certificationOfEligibility.code
        assertEquals "VISA", visaInternationalInformation.admissionRequest.code
        assertEquals "SPN", visaInternationalInformation.language.code
        assertEquals "SUN", visaInternationalInformation.internationalSponsor.code
        assertEquals "STU", visaInternationalInformation.employmentType.code

        //Update the entity
        visaInternationalInformation.spouseIndicator = "N"
        visaInternationalInformation.signatureIndicator = "T"
        visaInternationalInformation.passportId = "1234567890123456789UPDATE"
        visaInternationalInformation.nationIssue = "5"
        visaInternationalInformation.i94Status = "UPD"
        visaInternationalInformation.registrationNumber = "12345678UPD"
        visaInternationalInformation.duration = "N"
        visaInternationalInformation.certificateNumber = "12345UPDATE"
        visaInternationalInformation.nationBirth = "6"
        visaInternationalInformation.nationLegal = "7"
        visaInternationalInformation.foreignSsn = "123456789012UPD"
        visaInternationalInformation.childNumber = 88
        visaInternationalInformation.certificationOfEligibility = CertificationOfEligibility.findByCode("I-94")
        visaInternationalInformation.admissionRequest = AdmissionRequest.findByCode("TUTD")
        visaInternationalInformation.language = Language.findByCode("JPN")
        visaInternationalInformation.internationalSponsor = InternationalSponsor.findByCode("SOI")
        visaInternationalInformation.employmentType = EmploymentType.findByCode("EMP")
        visaInternationalInformation.save(failOnError: true, flush: true)

        //Assert for sucessful update
        visaInternationalInformation = VisaInternationalInformation.get(visaInternationalInformation.id)
        assertEquals 1L, visaInternationalInformation?.version
        assertEquals "N", visaInternationalInformation.spouseIndicator
        assertEquals "T", visaInternationalInformation.signatureIndicator
        assertEquals "1234567890123456789UPDATE", visaInternationalInformation.passportId
        assertEquals "5", visaInternationalInformation.nationIssue
        assertEquals "UPD", visaInternationalInformation.i94Status
        assertEquals "12345678UPD", visaInternationalInformation.registrationNumber
        assertEquals "N", visaInternationalInformation.duration
        assertEquals "12345UPDATE", visaInternationalInformation.certificateNumber
        assertEquals "6", visaInternationalInformation.nationBirth
        assertEquals "7", visaInternationalInformation.nationLegal
        assertEquals "123456789012UPD", visaInternationalInformation.foreignSsn
        assertEquals 88, visaInternationalInformation.childNumber
        assertEquals "I-94", visaInternationalInformation.certificationOfEligibility.code
        assertEquals "TUTD", visaInternationalInformation.admissionRequest.code
        assertEquals "JPN", visaInternationalInformation.language.code
        assertEquals "SOI", visaInternationalInformation.internationalSponsor.code
        assertEquals "EMP", visaInternationalInformation.employmentType.code
    }


    @Test
    void testUpdateInvalidVisaInternationalInformation() {
        def visaInternationalInformation = newValidForCreateVisaInternationalInformation()
        visaInternationalInformation.save(failOnError: true, flush: true)
        assertNotNull visaInternationalInformation.id
        assertEquals 0L, visaInternationalInformation.version
        assertEquals PersonUtility.getPerson("HOR000008").pidm, visaInternationalInformation.pidm
        assertEquals "Y", visaInternationalInformation.spouseIndicator
        assertEquals "N", visaInternationalInformation.signatureIndicator
        assertEquals "1234567890123456789012345", visaInternationalInformation.passportId
        assertEquals "1", visaInternationalInformation.nationIssue
        assertEquals "123", visaInternationalInformation.i94Status
        assertEquals "12345678901", visaInternationalInformation.registrationNumber
        assertEquals "Y", visaInternationalInformation.duration
        assertEquals "1234567890X", visaInternationalInformation.certificateNumber
        assertEquals "2", visaInternationalInformation.nationBirth
        assertEquals "3", visaInternationalInformation.nationLegal
        assertEquals "123456789012345", visaInternationalInformation.foreignSsn
        assertEquals 99, visaInternationalInformation.childNumber
        assertEquals "I-20", visaInternationalInformation.certificationOfEligibility.code
        assertEquals "VISA", visaInternationalInformation.admissionRequest.code
        assertEquals "SPN", visaInternationalInformation.language.code
        assertEquals "SUN", visaInternationalInformation.internationalSponsor.code
        assertEquals "STU", visaInternationalInformation.employmentType.code

        //Update the entity with invalid values
        visaInternationalInformation.spouseIndicator = "Z"
        shouldFail(ValidationException) {
            visaInternationalInformation.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDates() {
        def time = new SimpleDateFormat('HHmmss')
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def visaInternationalInformation = newValidForCreateVisaInternationalInformation()

        visaInternationalInformation.passportExpenditureDate = new Date()
        visaInternationalInformation.i94Date = new Date()
        visaInternationalInformation.certificateDateIssue = new Date()
        visaInternationalInformation.certificateDateReceipt = new Date()

        visaInternationalInformation.save(flush: true, failOnError: true)
        visaInternationalInformation.refresh()
        assertNotNull "VisaInternationalInformation should have been saved", visaInternationalInformation.id

        // test date values -
        assertEquals date.format(today), date.format(visaInternationalInformation.lastModified)
        assertEquals hour.format(today), hour.format(visaInternationalInformation.lastModified)

        assertEquals time.format(visaInternationalInformation.passportExpenditureDate), "000000"
        assertEquals time.format(visaInternationalInformation.i94Date), "000000"
        assertEquals time.format(visaInternationalInformation.certificateDateIssue), "000000"
        assertEquals time.format(visaInternationalInformation.certificateDateReceipt), "000000"
    }


    @Test
    void testOptimisticLock() {
        def visaInternationalInformation = newValidForCreateVisaInternationalInformation()
        visaInternationalInformation.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GOBINTL set GOBINTL_VERSION = 999 where GOBINTL_SURROGATE_ID = ?", [visaInternationalInformation.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        visaInternationalInformation.spouseIndicator = "T"
        shouldFail(HibernateOptimisticLockingFailureException) {
            visaInternationalInformation.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteVisaInternationalInformation() {
        def visaInternationalInformation = newValidForCreateVisaInternationalInformation()
        visaInternationalInformation.save(failOnError: true, flush: true)
        def id = visaInternationalInformation.id
        assertNotNull id
        visaInternationalInformation.delete()
        assertNull VisaInternationalInformation.get(id)
    }


    @Test
    void testValidation() {
        def visaInternationalInformation = newInvalidForCreateVisaInternationalInformation()
        assertFalse "VisaInternationalInformation could not be validated as expected due to ${visaInternationalInformation.errors}", visaInternationalInformation.validate()
    }


    @Test
    void testNullValidationFailure() {
        def visaInternationalInformation = new VisaInternationalInformation()
        assertFalse "VisaInternationalInformation should have failed validation", visaInternationalInformation.validate()
        assertErrorsFor visaInternationalInformation, 'nullable',
                [
                        'pidm',
                        'spouseIndicator',
                        'signatureIndicator'
                ]
        assertNoErrorsFor visaInternationalInformation,
                [
                        'passportId',
                        'nationIssue',
                        'passportExpenditureDate',
                        'i94Status',
                        'i94Date',
                        'registrationNumber',
                        'duration',
                        'certificateNumber',
                        'certificateDateIssue',
                        'certificateDateReceipt',
                        'nationBirth',
                        'nationLegal',
                        'foreignSsn',
                        'childNumber',
                        'certificationOfEligibility',
                        'admissionRequest',
                        'language',
                        'internationalSponsor',
                        'employmentType'
                ]
    }


    @Test
    void testMaxSizeValidationFailures() {
        def visaInternationalInformation = new VisaInternationalInformation(
                passportId: 'XXXXXXXXXXXXXXXXXXXXXXXXXXX',
                nationIssue: 'XXXXXXX',
                i94Status: 'XXXXX',
                registrationNumber: 'XXXXXXXXXXXXX',
                duration: 'XXX',
                certificateNumber: 'XXXXXXXXXXXXX',
                nationBirth: 'XXXXXXX',
                nationLegal: 'XXXXXXX',
                foreignSsn: 'XXXXXXXXXXXXXXXXX')
        assertFalse "VisaInternationalInformation should have failed validation", visaInternationalInformation.validate()
        assertErrorsFor visaInternationalInformation, 'maxSize', ['passportId', 'nationIssue', 'i94Status', 'registrationNumber', 'duration', 'certificateNumber', 'nationBirth', 'nationLegal', 'foreignSsn']
    }


    private def newValidForCreateVisaInternationalInformation() {
        def visaInternationalInformation = new VisaInternationalInformation(
                pidm: PersonUtility.getPerson("HOR000008").pidm,
                spouseIndicator: "Y",
                signatureIndicator: "N",
                passportId: "1234567890123456789012345",
                nationIssue: "1",
                passportExpenditureDate: new Date(),
                i94Status: "123",
                i94Date: new Date(),
                registrationNumber: "12345678901",
                duration: "Y",
                certificateNumber: "1234567890X",
                certificateDateIssue: new Date(),
                certificateDateReceipt: new Date(),
                nationBirth: "2",
                nationLegal: "3",
                foreignSsn: "123456789012345",
                childNumber: 99,
                certificationOfEligibility: CertificationOfEligibility.findByCode("I-20"),
                admissionRequest: AdmissionRequest.findByCode("VISA"),
                language: Language.findByCode("SPN"),
                internationalSponsor: InternationalSponsor.findByCode("SUN"),
                employmentType: EmploymentType.findByCode("STU"),
        )
        return visaInternationalInformation
    }


    private def newInvalidForCreateVisaInternationalInformation() {
        def visaInternationalInformation = new VisaInternationalInformation()
        return visaInternationalInformation
    }
}

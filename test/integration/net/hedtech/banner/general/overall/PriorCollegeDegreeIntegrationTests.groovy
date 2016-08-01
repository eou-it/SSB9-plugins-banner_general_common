/*********************************************************************************
 Copyright 2010-2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import org.junit.Before
import org.junit.Test
import org.junit.After

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.*
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

import java.text.SimpleDateFormat

class PriorCollegeDegreeIntegrationTests extends BaseIntegrationTestCase {

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
    void testCreateValidPriorCollegeDegree() {
        def priorCollegeDegree = newValidForCreatePriorCollegeDegree()
        priorCollegeDegree.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull priorCollegeDegree.id
    }


    @Test
    void testCreateInvalidPriorCollegeDegree() {
        def priorCollegeDegree = newInvalidForCreatePriorCollegeDegree()
        shouldFail(ValidationException) {
            priorCollegeDegree.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidPriorCollegeDegree() {
        def priorCollegeDegree = newValidForCreatePriorCollegeDegree()
        priorCollegeDegree.save(failOnError: true, flush: true)
        assertNotNull priorCollegeDegree.id
        assertEquals 0L, priorCollegeDegree.version
        assertEquals PersonUtility.getPerson("HOR000001").pidm, priorCollegeDegree.pidm
        assertEquals 1, priorCollegeDegree.degreeSequenceNumber
        assertEquals 200.00, priorCollegeDegree.hoursTransferred, 0
        assertEquals 1000.00, priorCollegeDegree.gpaTransferred, 0
        assertEquals "2014", priorCollegeDegree.degreeYear
        assertEquals "Y", priorCollegeDegree.termDegree
        assertEquals "Y", priorCollegeDegree.primaryIndicator
        assertEquals "999999", priorCollegeDegree.sourceAndBackgroundInstitution.code
        assertEquals "PHD", priorCollegeDegree.degree.code
        assertEquals "AH", priorCollegeDegree.college.code
        assertEquals "TTTTTT", priorCollegeDegree.institutionalHonor.code
        assertEquals "MA", priorCollegeDegree.educationGoal

        //Update the entity
        def institutionalHonorNew = new InstitutionalHonor(
                code: "TTTTT2",
                description: "123456789012345678901234567890",
                transcPrintIndicator: "Y",
                commencePrintIndicator: "Y",
                electronicDataInterchangeEquivalent: "TTT",
        )
        institutionalHonorNew.save(failOnError: true, flush: true)

        priorCollegeDegree.degreeSequenceNumber = 2
        priorCollegeDegree.hoursTransferred = 201.00
        priorCollegeDegree.gpaTransferred = 1001.00
        priorCollegeDegree.degreeYear = "2013"
        priorCollegeDegree.termDegree = "N"
        priorCollegeDegree.primaryIndicator = "N"
        priorCollegeDegree.degree = Degree.findByCode("MA")
        priorCollegeDegree.college = College.findByCode("BU")
        priorCollegeDegree.institutionalHonor = institutionalHonorNew
        priorCollegeDegree.educationGoal = "PH"
        priorCollegeDegree.save(failOnError: true, flush: true)

        //Assert for sucessful update
        priorCollegeDegree = PriorCollegeDegree.get(priorCollegeDegree.id)
        assertEquals 1L, priorCollegeDegree?.version
        assertEquals 2, priorCollegeDegree.degreeSequenceNumber
        assertEquals 201.00, priorCollegeDegree.hoursTransferred, 0
        assertEquals 1001.00, priorCollegeDegree.gpaTransferred, 0
        assertEquals "2013", priorCollegeDegree.degreeYear
        assertEquals "N", priorCollegeDegree.termDegree
        assertEquals "N", priorCollegeDegree.primaryIndicator
        assertEquals "MA", priorCollegeDegree.degree.code
        assertEquals "BU", priorCollegeDegree.college.code
        assertEquals "TTTTT2", priorCollegeDegree.institutionalHonor.code
        assertEquals "PH", priorCollegeDegree.educationGoal
    }


    @Test
    void testUpdateInvalidPriorCollegeDegree() {
        def priorCollegeDegree = newValidForCreatePriorCollegeDegree()
        priorCollegeDegree.save(failOnError: true, flush: true)
        assertNotNull priorCollegeDegree.id
        assertEquals 0L, priorCollegeDegree.version
        assertEquals PersonUtility.getPerson("HOR000001").pidm, priorCollegeDegree.pidm
        assertEquals 1, priorCollegeDegree.degreeSequenceNumber
        assertEquals 200.00, priorCollegeDegree.hoursTransferred, 0
        assertEquals 1000.00, priorCollegeDegree.gpaTransferred, 0
        assertEquals "2014", priorCollegeDegree.degreeYear
        assertEquals "Y", priorCollegeDegree.termDegree
        assertEquals "Y", priorCollegeDegree.primaryIndicator
        assertEquals "999999", priorCollegeDegree.sourceAndBackgroundInstitution.code
        assertEquals "PHD", priorCollegeDegree.degree.code
        assertEquals "AH", priorCollegeDegree.college.code
        assertEquals "TTTTTT", priorCollegeDegree.institutionalHonor.code
        assertEquals "MA", priorCollegeDegree.educationGoal

        //Update the entity with invalid values
        priorCollegeDegree.degreeSequenceNumber = 100
        shouldFail(ValidationException) {
            priorCollegeDegree.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDates() {
        def time = new SimpleDateFormat('HHmmss')
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def priorCollegeDegree = newValidForCreatePriorCollegeDegree()

        priorCollegeDegree.attendenceFrom = new Date()
        priorCollegeDegree.attendenceTo = new Date()
        priorCollegeDegree.degreeDate = new Date()

        priorCollegeDegree.save(flush: true, failOnError: true)
        priorCollegeDegree.refresh()
        assertNotNull "PriorCollegeDegree should have been saved", priorCollegeDegree.id

        // test date values -
        assertEquals date.format(today), date.format(priorCollegeDegree.lastModified)
        assertEquals hour.format(today), hour.format(priorCollegeDegree.lastModified)

        assertEquals time.format(priorCollegeDegree.attendenceFrom), "000000"
        assertEquals time.format(priorCollegeDegree.attendenceTo), "000000"
        assertEquals time.format(priorCollegeDegree.degreeDate), "000000"
    }


    @Test
    void testOptimisticLock() {
        def priorCollegeDegree = newValidForCreatePriorCollegeDegree()
        priorCollegeDegree.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update SV_SORDEGR set SORDEGR_VERSION = 999 where SORDEGR_SURROGATE_ID = ?", [priorCollegeDegree.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        priorCollegeDegree.degreeSequenceNumber = 2
        shouldFail(HibernateOptimisticLockingFailureException) {
            priorCollegeDegree.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeletePriorCollegeDegree() {
        def priorCollegeDegree = newValidForCreatePriorCollegeDegree()
        priorCollegeDegree.save(failOnError: true, flush: true)
        def id = priorCollegeDegree.id
        assertNotNull id
        priorCollegeDegree.delete()
        assertNull PriorCollegeDegree.get(id)
    }


    @Test
    void testValidation() {
        def priorCollegeDegree = newInvalidForCreatePriorCollegeDegree()
        assertFalse "PriorCollegeDegree could not be validated as expected due to ${priorCollegeDegree.errors}", priorCollegeDegree.validate()
    }


    @Test
    void testNullValidationFailure() {
        def priorCollegeDegree = new PriorCollegeDegree()
        assertFalse "PriorCollegeDegree should have failed validation", priorCollegeDegree.validate()
        assertErrorsFor priorCollegeDegree, 'nullable',
                [
                        'pidm',
                        'degreeSequenceNumber',
                        'sourceAndBackgroundInstitution',
                ]
        assertNoErrorsFor priorCollegeDegree,
                [
                        'attendenceFrom',
                        'attendenceTo',
                        'hoursTransferred',
                        'gpaTransferred',
                        'degreeDate',
                        'degreeYear',
                        'termDegree',
                        'primaryIndicator',
                        'degree',
                        'college',
                        'institutionalHonor',
                        'educationGoal'
                ]
    }


    @Test
    void testMaxSizeValidationFailures() {
        def priorCollegeDegree = new PriorCollegeDegree(
                degreeYear: 'XXXXXX',
                termDegree: 'XXX',
                primaryIndicator: 'XXX')
        assertFalse "PriorCollegeDegree should have failed validation", priorCollegeDegree.validate()
        assertErrorsFor priorCollegeDegree, 'maxSize', ['degreeYear', 'termDegree', 'primaryIndicator']
    }


    @Test
    void testFetchByPidm() {
        def priorCollegeDegree = newValidForCreatePriorCollegeDegree()
        priorCollegeDegree.save(failOnError: true, flush: true)

        def priorCollegeDegreeList = PriorCollegeDegree.fetchByPidm(priorCollegeDegree.pidm)

        assertNotNull priorCollegeDegreeList
        assertFalse priorCollegeDegreeList.isEmpty()
        assertTrue priorCollegeDegreeList.contains(priorCollegeDegree)
    }


    void testFetchByPidmList() {
        def priorCollegeDegree = newValidForCreatePriorCollegeDegree()
        priorCollegeDegree.save(failOnError: true, flush: true)

        def priorCollegeDegreeList = PriorCollegeDegree.fetchByPidmList([priorCollegeDegree.pidm])

        assertNotNull priorCollegeDegreeList
        assertFalse priorCollegeDegreeList.isEmpty()
        assertTrue priorCollegeDegreeList.contains(priorCollegeDegree)
    }


    private def newValidForCreatePriorCollegeDegree() {
        def priorCollege = newValidForCreatePriorCollege()
        priorCollege.save(failOnError: true, flush: true)

        def institutionalHonor = newValidForCreateInstitutionalHonor()
        institutionalHonor.save(failOnError: true, flush: true)

        def priorCollegeDegree = new PriorCollegeDegree(
                pidm: PersonUtility.getPerson("HOR000001").pidm,
                degreeSequenceNumber: 1,
                attendenceFrom: new Date(),
                attendenceTo: new Date(),
                hoursTransferred: 200.00,
                gpaTransferred: 1000.00,
                degreeDate: new Date(),
                degreeYear: "2014",
                termDegree: "Y",
                primaryIndicator: "Y",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
                degree: Degree.findByCode("PHD"),
                college: College.findByCode("AH"),
                institutionalHonor: institutionalHonor,
                educationGoal: "MA",
        )
        return priorCollegeDegree
    }


    private def newInvalidForCreatePriorCollegeDegree() {
        def priorCollegeDegree = new PriorCollegeDegree(
                pidm: null,
                degreeSequenceNumber: 100,
                sourceAndBackgroundInstitution: null,
        )
        return priorCollegeDegree
    }


    private def newValidForCreatePriorCollege() {
        def priorCollege = new PriorCollege(
                pidm: PersonUtility.getPerson("HOR000001").pidm,
                transactionRecvDate: new Date(),
                transactionRevDate: new Date(),
                officialTransaction: "Y",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
                admissionRequest: AdmissionRequest.findByCode("VISA"),
        )
        return priorCollege
    }


    private def newValidForCreateInstitutionalHonor() {
        def institutionalHonor = new InstitutionalHonor(
                code: "TTTTTT",
                description: "123456789012345678901234567890",
                transcPrintIndicator: "Y",
                commencePrintIndicator: "Y",
                electronicDataInterchangeEquivalent: "TTT",
        )
        return institutionalHonor
    }
}

/*********************************************************************************
  Copyright 2010-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import org.junit.Before
import org.junit.Test
import org.junit.After
import grails.validation.ValidationException
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.*
import net.hedtech.banner.testing.BaseIntegrationTestCase
import java.text.SimpleDateFormat

class PriorCollegeMajorIntegrationTests extends BaseIntegrationTestCase {

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
    void testCreateValidPriorCollegeMajor() {
        def priorCollegeMajor = newValidForCreatePriorCollegeMajor()
        priorCollegeMajor.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull priorCollegeMajor.id
    }


    @Test
    void testCreateInvalidPriorCollegeMajor() {
        def priorCollegeMajor = newInvalidForCreatePriorCollegeMajor()
        shouldFail(ValidationException) {
            priorCollegeMajor.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidPriorCollegeMajor() {
        def priorCollegeMajor = newValidForCreatePriorCollegeMajor()
        priorCollegeMajor.save(failOnError: true, flush: true)
        assertNotNull priorCollegeMajor.id
        assertEquals 0L, priorCollegeMajor.version
        assertEquals PersonUtility.getPerson("HOR000001").pidm, priorCollegeMajor.pidm
        assertEquals 1, priorCollegeMajor.degreeSequenceNumber
        assertEquals "999999", priorCollegeMajor.sourceAndBackgroundInstitution.code
        assertEquals "PHD", priorCollegeMajor.degree.code
        assertEquals "EDUC", priorCollegeMajor.majorMinorConcentrationMajor.code

        priorCollegeMajor.majorMinorConcentrationMajor = MajorMinorConcentration.findByCode("ECNO")
        //Changed from org.springframework.orm.hibernate3.HibernateJdbcException due to spring 4.1.5
        shouldFail(org.springframework.dao.QueryTimeoutException) {
            priorCollegeMajor.save(flush: true, failOnError: true)
            fail("this should have failed, update not allowed")
        }
    }


    @Test
    void testDates() {
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = getSystemDate()

        def priorCollegeMajor = newValidForCreatePriorCollegeMajor()

        priorCollegeMajor.save(flush: true, failOnError: true)
        priorCollegeMajor.refresh()
        assertNotNull "PriorCollegeMajor should have been saved", priorCollegeMajor.id

        // test date values -
        assertEquals date.format(today), date.format(priorCollegeMajor.lastModified)
        assertEquals hour.format(today), hour.format(priorCollegeMajor.lastModified)
    }


    @Test
    void testDeletePriorCollegeMajor() {
        def priorCollegeMajor = newValidForCreatePriorCollegeMajor()
        priorCollegeMajor.save(failOnError: true, flush: true)
        def id = priorCollegeMajor.id
        assertNotNull id
        priorCollegeMajor.delete()
        assertNull PriorCollegeMajor.get(id)
    }


    @Test
    void testValidation() {
        def priorCollegeMajor = newInvalidForCreatePriorCollegeMajor()
        assertFalse "PriorCollegeMajor could not be validated as expected due to ${priorCollegeMajor.errors}", priorCollegeMajor.validate()
    }


    @Test
    void testNullValidationFailure() {
        def priorCollegeMajor = new PriorCollegeMajor()
        assertFalse "PriorCollegeMajor should have failed validation", priorCollegeMajor.validate()
        assertErrorsFor priorCollegeMajor, 'nullable',
                [
                        'pidm',
                        'degreeSequenceNumber',
                        'sourceAndBackgroundInstitution',
                        'majorMinorConcentrationMajor'
                ]
        assertNoErrorsFor priorCollegeMajor,
                [
                        'degree'
                ]
    }


    private def newValidForCreatePriorCollegeMajor() {
        def priorCollegeDegree = newValidForCreatePriorCollegeDegree()
        priorCollegeDegree.save(failOnError: true, flush: true)

        def priorCollegeMajor = new PriorCollegeMajor(
                pidm: priorCollegeDegree.pidm,
                degreeSequenceNumber: priorCollegeDegree.degreeSequenceNumber,
                sourceAndBackgroundInstitution: priorCollegeDegree.sourceAndBackgroundInstitution,
                degree: priorCollegeDegree.degree,
                majorMinorConcentrationMajor: MajorMinorConcentration.findByCode("EDUC"),
        )
        return priorCollegeMajor
    }


    private def newInvalidForCreatePriorCollegeMajor() {
        def priorCollegeMajor = new PriorCollegeMajor(
                pidm: null,
                degreeSequenceNumber: 100,
                sourceAndBackgroundInstitution: null,
                degree: null,
                majorMinorConcentrationMajor: null,
        )
        return priorCollegeMajor
    }


    private def newValidForCreatePriorCollegeDegree() {
        def priorCollege = newValidForCreatePriorCollege()
        priorCollege.save(failOnError: true, flush: true)

        def priorCollegeDegree = new PriorCollegeDegree(
                pidm: priorCollege.pidm,
                degreeSequenceNumber: 1,
                attendenceFrom: new Date(),
                attendenceTo: new Date(),
                hoursTransferred: 200.00,
                gpaTransferred: 1000.00,
                degreeDate: new Date(),
                degreeYear: 2014,
                termDegree: "Y",
                primaryIndicator: "Y",
                sourceAndBackgroundInstitution: priorCollege.sourceAndBackgroundInstitution,
                degree: Degree.findByCode("PHD"),
                college: College.findByCode("AH"),
                institutionalHonor: null,
                educationGoal: "MA",
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
}

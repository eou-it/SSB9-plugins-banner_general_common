/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
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

class PriorCollegeMinorIntegrationTests extends BaseIntegrationTestCase {

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
    void testCreateValidPriorCollegeMinor() {
        def priorCollegeMinor = newValidForCreatePriorCollegeMinor()
        priorCollegeMinor.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull priorCollegeMinor.id
    }


    @Test
    void testCreateInvalidPriorCollegeMinor() {
        def priorCollegeMinor = newInvalidForCreatePriorCollegeMinor()
        shouldFail(ValidationException) {
            priorCollegeMinor.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidPriorCollegeMinor() {
        def priorCollegeMinor = newValidForCreatePriorCollegeMinor()
        priorCollegeMinor.save(failOnError: true, flush: true)
        assertNotNull priorCollegeMinor.id
        assertEquals 0L, priorCollegeMinor.version
        assertEquals PersonUtility.getPerson("HOR000001").pidm, priorCollegeMinor.pidm
        assertEquals 1, priorCollegeMinor.degreeSequenceNumber
        assertEquals "999999", priorCollegeMinor.sourceAndBackgroundInstitution.code
        assertEquals "PHD", priorCollegeMinor.degree.code
        assertEquals "EDUC", priorCollegeMinor.majorMinorConcentrationMinor.code

        priorCollegeMinor.majorMinorConcentrationMinor = MajorMinorConcentration.findByCode("ECNO")
        try {
            priorCollegeMinor.save(flush: true, failOnError: true)
            fail("this should have failed, update not allowed")
        }
        catch (org.springframework.orm.hibernate3.HibernateJdbcException ae) {
//            assertApplicationException ae, "unsupported.operation"
        }
    }


    @Test
    void testDates() {
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def priorCollegeMinor = newValidForCreatePriorCollegeMinor()

        priorCollegeMinor.save(flush: true, failOnError: true)
        priorCollegeMinor.refresh()
        assertNotNull "PriorCollegeMinor should have been saved", priorCollegeMinor.id

        // test date values -
        assertEquals date.format(today), date.format(priorCollegeMinor.lastModified)
        assertEquals hour.format(today), hour.format(priorCollegeMinor.lastModified)
    }


    @Test
    void testDeletePriorCollegeMinor() {
        def priorCollegeMinor = newValidForCreatePriorCollegeMinor()
        priorCollegeMinor.save(failOnError: true, flush: true)
        def id = priorCollegeMinor.id
        assertNotNull id
        priorCollegeMinor.delete()
        assertNull PriorCollegeMinor.get(id)
    }


    @Test
    void testValidation() {
        def priorCollegeMinor = newInvalidForCreatePriorCollegeMinor()
        assertFalse "PriorCollegeMinor could not be validated as expected due to ${priorCollegeMinor.errors}", priorCollegeMinor.validate()
    }


    @Test
    void testNullValidationFailure() {
        def priorCollegeMinor = new PriorCollegeMinor()
        assertFalse "PriorCollegeMinor should have failed validation", priorCollegeMinor.validate()
        assertErrorsFor priorCollegeMinor, 'nullable',
                [
                        'pidm',
                        'degreeSequenceNumber',
                        'sourceAndBackgroundInstitution',
                        'majorMinorConcentrationMinor'
                ]
        assertNoErrorsFor priorCollegeMinor,
                [
                        'degree'
                ]
    }


    private def newValidForCreatePriorCollegeMinor() {
        def priorCollegeDegree = newValidForCreatePriorCollegeDegree()
        priorCollegeDegree.save(failOnError: true, flush: true)

        def priorCollegeMinor = new PriorCollegeMinor(
                pidm: priorCollegeDegree.pidm,
                degreeSequenceNumber: priorCollegeDegree.degreeSequenceNumber,
                sourceAndBackgroundInstitution: priorCollegeDegree.sourceAndBackgroundInstitution,
                degree: priorCollegeDegree.degree,
                majorMinorConcentrationMinor: MajorMinorConcentration.findByCode("EDUC"),
        )
        return priorCollegeMinor
    }


    private def newInvalidForCreatePriorCollegeMinor() {
        def priorCollegeMinor = new PriorCollegeMinor(
                pidm: null,
                degreeSequenceNumber: 100,
                sourceAndBackgroundInstitution: null,
                degree: null,
                majorMinorConcentrationMinor: null,
        )
        return priorCollegeMinor
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

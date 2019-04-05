/*********************************************************************************
  Copyright 2010-2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.*
import net.hedtech.banner.testing.BaseIntegrationTestCase

@Integration
@Rollback
class PriorCollegeDegreeServiceIntegrationTests extends BaseIntegrationTestCase {

    def priorCollegeDegreeService


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
    void testPriorCollegeDegreeValidCreate() {
        def priorCollegeDegree = newValidForCreatePriorCollegeDegree()
        def map = [domainModel: priorCollegeDegree]
        priorCollegeDegree = priorCollegeDegreeService.create(map)
        assertNotNull "PriorCollegeDegree ID is null in PriorCollegeDegree Service Tests Create", priorCollegeDegree.id
        assertNotNull "PriorCollegeDegree sourceAndBackgroundInstitution is null in PriorCollegeDegree Service Tests", priorCollegeDegree.sourceAndBackgroundInstitution
        assertNotNull "PriorCollegeDegree degree is null in PriorCollegeDegree Service Tests", priorCollegeDegree.degree
        assertNotNull "PriorCollegeDegree college is null in PriorCollegeDegree Service Tests", priorCollegeDegree.college
        assertNotNull "PriorCollegeDegree institutionalHonor is null in PriorCollegeDegree Service Tests", priorCollegeDegree.institutionalHonor
        assertNotNull priorCollegeDegree.pidm
        assertNotNull priorCollegeDegree.degreeSequenceNumber
        assertNotNull priorCollegeDegree.hoursTransferred
        assertNotNull priorCollegeDegree.gpaTransferred
        assertNotNull priorCollegeDegree.degreeYear
        assertNotNull priorCollegeDegree.termDegree
        assertNotNull priorCollegeDegree.primaryIndicator
        assertNotNull priorCollegeDegree.educationGoal
        assertNotNull priorCollegeDegree.version
        assertNotNull priorCollegeDegree.dataOrigin
        assertNotNull priorCollegeDegree.lastModifiedBy
        assertNotNull priorCollegeDegree.lastModified
    }


    @Test
    void testPriorCollegeDegreeInvalidCreate() {
        def priorCollegeDegree = newInvalidForCreatePriorCollegeDegree()
        def map = [domainModel: priorCollegeDegree]
        shouldFail(ApplicationException) {
            priorCollegeDegreeService.create(map)
        }
    }


    @Test
    void testPriorCollegeDegreeValidUpdate() {
        def priorCollegeDegree = newValidForCreatePriorCollegeDegree()
        def map = [domainModel: priorCollegeDegree]
        priorCollegeDegree = priorCollegeDegreeService.create(map)
        assertNotNull "PriorCollegeDegree ID is null in PriorCollegeDegree Service Tests Create", priorCollegeDegree.id
        assertNotNull "PriorCollegeDegree sourceAndBackgroundInstitution is null in PriorCollegeDegree Service Tests", priorCollegeDegree.sourceAndBackgroundInstitution
        assertNotNull "PriorCollegeDegree degree is null in PriorCollegeDegree Service Tests", priorCollegeDegree.degree
        assertNotNull "PriorCollegeDegree college is null in PriorCollegeDegree Service Tests", priorCollegeDegree.college
        assertNotNull "PriorCollegeDegree institutionalHonor is null in PriorCollegeDegree Service Tests", priorCollegeDegree.institutionalHonor
        assertNotNull priorCollegeDegree.pidm
        assertNotNull priorCollegeDegree.degreeSequenceNumber
        assertNotNull priorCollegeDegree.hoursTransferred
        assertNotNull priorCollegeDegree.gpaTransferred
        assertNotNull priorCollegeDegree.degreeYear
        assertNotNull priorCollegeDegree.termDegree
        assertNotNull priorCollegeDegree.primaryIndicator
        assertNotNull priorCollegeDegree.educationGoal
        assertNotNull priorCollegeDegree.version
        assertNotNull priorCollegeDegree.dataOrigin
        assertNotNull priorCollegeDegree.lastModifiedBy
        assertNotNull priorCollegeDegree.lastModified

        //Update the entity with new values
        def institutionalHonorNew = new InstitutionalHonor(
                code: "TTTTT2",
                description: "123456789012345678901234567890",
                transcPrintIndicator: "Y",
                commencePrintIndicator: "Y",
                electronicDataInterchangeEquivalent: "TTT",
        )
        institutionalHonorNew.save(failOnError: true, flush: true)

        priorCollegeDegree.degree = Degree.findByCode("MA")
        priorCollegeDegree.college = College.findByCode("BU")
        priorCollegeDegree.institutionalHonor = institutionalHonorNew
        priorCollegeDegree.educationGoal = "PH"
        priorCollegeDegree.degreeSequenceNumber = 2
        priorCollegeDegree.hoursTransferred = 201.00
        priorCollegeDegree.gpaTransferred = 1001.00
        priorCollegeDegree.degreeYear = "2013"
        priorCollegeDegree.termDegree = "N"
        priorCollegeDegree.primaryIndicator = "N"

        map.domainModel = priorCollegeDegree
        priorCollegeDegree = priorCollegeDegreeService.update(map)

        // test the values
        priorCollegeDegree = PriorCollegeDegree.get(priorCollegeDegree.id)
        assertEquals 1L, priorCollegeDegree?.version
        assertEquals 2, priorCollegeDegree.degreeSequenceNumber
        assertEquals 201.00, priorCollegeDegree.hoursTransferred, 0.001
        assertEquals 1001.00, priorCollegeDegree.gpaTransferred, 0.001
        assertEquals "2013", priorCollegeDegree.degreeYear
        assertEquals "N", priorCollegeDegree.termDegree
        assertEquals "N", priorCollegeDegree.primaryIndicator
        assertEquals "MA", priorCollegeDegree.degree.code
        assertEquals "BU", priorCollegeDegree.college.code
        assertEquals "TTTTT2", priorCollegeDegree.institutionalHonor.code
        assertEquals "PH", priorCollegeDegree.educationGoal
    }


    @Test
    void testPriorCollegeDegreeInvalidUpdate() {
        def priorCollegeDegree = newValidForCreatePriorCollegeDegree()
        def map = [domainModel: priorCollegeDegree]
        priorCollegeDegree = priorCollegeDegreeService.create(map)
        assertNotNull "PriorCollegeDegree ID is null in PriorCollegeDegree Service Tests Create", priorCollegeDegree.id
        assertNotNull "PriorCollegeDegree sourceAndBackgroundInstitution is null in PriorCollegeDegree Service Tests", priorCollegeDegree.sourceAndBackgroundInstitution
        assertNotNull "PriorCollegeDegree degree is null in PriorCollegeDegree Service Tests", priorCollegeDegree.degree
        assertNotNull "PriorCollegeDegree college is null in PriorCollegeDegree Service Tests", priorCollegeDegree.college
        assertNotNull "PriorCollegeDegree institutionalHonor is null in PriorCollegeDegree Service Tests", priorCollegeDegree.institutionalHonor
        assertNotNull priorCollegeDegree.pidm
        assertNotNull priorCollegeDegree.degreeSequenceNumber
        assertNotNull priorCollegeDegree.hoursTransferred
        assertNotNull priorCollegeDegree.gpaTransferred
        assertNotNull priorCollegeDegree.degreeYear
        assertNotNull priorCollegeDegree.termDegree
        assertNotNull priorCollegeDegree.primaryIndicator
        assertNotNull priorCollegeDegree.educationGoal
        assertNotNull priorCollegeDegree.version
        assertNotNull priorCollegeDegree.dataOrigin
        assertNotNull priorCollegeDegree.lastModifiedBy
        assertNotNull priorCollegeDegree.lastModified

        //Update the entity with new invalid values
        priorCollegeDegree.sourceAndBackgroundInstitution = null
        map.domainModel = priorCollegeDegree
        shouldFail(ApplicationException) {
            priorCollegeDegree = priorCollegeDegreeService.update(map)
        }
    }


    @Test
    void testPriorCollegeDegreeDelete() {
        def priorCollegeDegree = newValidForCreatePriorCollegeDegree()
        def map = [domainModel: priorCollegeDegree]
        priorCollegeDegree = priorCollegeDegreeService.create(map)
        assertNotNull "PriorCollegeDegree ID is null in PriorCollegeDegree Service Tests Create", priorCollegeDegree.id

        def id = priorCollegeDegree.id
        priorCollegeDegreeService.delete([domainModel: priorCollegeDegree])
        assertNull "PriorCollegeDegree should have been deleted", priorCollegeDegree.get(id)
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

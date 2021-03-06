 /*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
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
import static groovy.test.GroovyAssert.*

@Integration
@Rollback
class PriorCollegeConcentrationAreaServiceIntegrationTests extends BaseIntegrationTestCase {

    def priorCollegeConcentrationAreaService


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testPriorCollegeConcentrationAreaValidCreate() {
        def priorCollegeConcentrationArea = newValidForCreatePriorCollegeConcentrationArea()
        def map = [domainModel: priorCollegeConcentrationArea]
        priorCollegeConcentrationArea = priorCollegeConcentrationAreaService.create(map)
        assertNotNull "PriorCollegeConcentrationArea ID is null in PriorCollegeConcentrationArea Service Tests Create", priorCollegeConcentrationArea.id
        assertNotNull "PriorCollegeConcentrationArea sourceAndBackgroundInstitution is null in PriorCollegeConcentrationArea Service Tests", priorCollegeConcentrationArea.sourceAndBackgroundInstitution
        assertNotNull "PriorCollegeConcentrationArea degree is null in PriorCollegeConcentrationArea Service Tests", priorCollegeConcentrationArea.degree
        assertNotNull "PriorCollegeConcentrationArea concentration is null in PriorCollegeConcentrationArea Service Tests", priorCollegeConcentrationArea.concentration
        assertNotNull priorCollegeConcentrationArea.pidm
        assertNotNull priorCollegeConcentrationArea.degreeSequenceNumber
        assertNotNull priorCollegeConcentrationArea.version
        assertNotNull priorCollegeConcentrationArea.dataOrigin
        assertNotNull priorCollegeConcentrationArea.lastModifiedBy
        assertNotNull priorCollegeConcentrationArea.lastModified
    }


    @Test
    void testPriorCollegeConcentrationAreaInvalidCreate() {
        def priorCollegeConcentrationArea = newInvalidForCreatePriorCollegeConcentrationArea()
        def map = [domainModel: priorCollegeConcentrationArea]
        shouldFail(ApplicationException) {
            priorCollegeConcentrationAreaService.create(map)
        }
    }


    @Test
    void testPriorCollegeConcentrationAreaValidUpdate() {
        def priorCollegeConcentrationArea = newValidForCreatePriorCollegeConcentrationArea()
        def map = [domainModel: priorCollegeConcentrationArea]
        priorCollegeConcentrationArea = priorCollegeConcentrationAreaService.create(map)
        assertNotNull "PriorCollegeConcentrationArea ID is null in PriorCollegeConcentrationArea Service Tests Create", priorCollegeConcentrationArea.id
        assertNotNull "PriorCollegeConcentrationArea sourceAndBackgroundInstitution is null in PriorCollegeConcentrationArea Service Tests", priorCollegeConcentrationArea.sourceAndBackgroundInstitution
        assertNotNull "PriorCollegeConcentrationArea degree is null in PriorCollegeConcentrationArea Service Tests", priorCollegeConcentrationArea.degree
        assertNotNull "PriorCollegeConcentrationArea concentration is null in PriorCollegeConcentrationArea Service Tests", priorCollegeConcentrationArea.concentration
        assertNotNull priorCollegeConcentrationArea.pidm
        assertNotNull priorCollegeConcentrationArea.degreeSequenceNumber
        assertNotNull priorCollegeConcentrationArea.version
        assertNotNull priorCollegeConcentrationArea.dataOrigin
        assertNotNull priorCollegeConcentrationArea.lastModifiedBy
        assertNotNull priorCollegeConcentrationArea.lastModified

        //Update the entity with new values
        priorCollegeConcentrationArea.degree = Degree.findByCode("MA")
        map.domainModel = priorCollegeConcentrationArea
        try {
            priorCollegeConcentrationAreaService.update(map)
            fail "Cannot update curriculum"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "unsupported.operation"
        }
    }


    @Test
    void testPriorCollegeConcentrationAreaDelete() {
        def priorCollegeConcentrationArea = newValidForCreatePriorCollegeConcentrationArea()
        def map = [domainModel: priorCollegeConcentrationArea]
        priorCollegeConcentrationArea = priorCollegeConcentrationAreaService.create(map)
        assertNotNull "PriorCollegeConcentrationArea ID is null in PriorCollegeConcentrationArea Service Tests Create", priorCollegeConcentrationArea.id

        def id = priorCollegeConcentrationArea.id
        priorCollegeConcentrationAreaService.delete([domainModel: priorCollegeConcentrationArea])
        assertNull "PriorCollegeConcentrationArea should have been deleted", priorCollegeConcentrationArea.get(id)
    }


    @Test
    void testReadOnly() {
        def priorCollegeConcentrationArea = newValidForCreatePriorCollegeConcentrationArea()
        def map = [domainModel: priorCollegeConcentrationArea]
        priorCollegeConcentrationArea = priorCollegeConcentrationAreaService.create(map)
        assertNotNull "PriorCollegeConcentrationArea ID is null in PriorCollegeConcentrationArea Service Tests Create", priorCollegeConcentrationArea.id

        priorCollegeConcentrationArea.sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findWhere(code: "000000")
        priorCollegeConcentrationArea.pidm = PersonUtility.getPerson("HOR000002").pidm
        try {
            priorCollegeConcentrationAreaService.update([domainModel: priorCollegeConcentrationArea])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegreeNullPidmList() {
        assertEquals([], priorCollegeConcentrationAreaService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(null, [], [], []))
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegreeEmptyPidmList() {
        assertEquals([], priorCollegeConcentrationAreaService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree([], [], [], []))
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegreeNullPrioirCollegeList() {
        assertEquals([], priorCollegeConcentrationAreaService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(PriorCollegeConcentrationArea.findAll().pidm, null, [], []))
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegreeEmptyPrioirCollegeList() {
        assertEquals([], priorCollegeConcentrationAreaService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(PriorCollegeConcentrationArea.findAll().pidm, [], [], []))
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegreeNullDegSeqNoList() {
        List<PriorCollegeConcentrationArea> priorCollegeConcentrationAreaList = PriorCollegeConcentrationArea.findAll()
        assertEquals([], priorCollegeConcentrationAreaService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(
                priorCollegeConcentrationAreaList.pidm, priorCollegeConcentrationAreaList.sourceAndBackgroundInstitution, null, []))
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegreeEmptyDegSeqNoList() {
        List<PriorCollegeConcentrationArea> priorCollegeConcentrationAreaList = PriorCollegeConcentrationArea.findAll()
        assertEquals([], priorCollegeConcentrationAreaService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(
                priorCollegeConcentrationAreaList.pidm, priorCollegeConcentrationAreaList.sourceAndBackgroundInstitution, [], []))
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegreeNullDegCodeList() {
        List<PriorCollegeConcentrationArea> priorCollegeConcentrationAreaList = PriorCollegeConcentrationArea.findAll()
        assertEquals([], priorCollegeConcentrationAreaService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(
                priorCollegeConcentrationAreaList.pidm, priorCollegeConcentrationAreaList.sourceAndBackgroundInstitution, priorCollegeConcentrationAreaList.degreeSequenceNumber, null))
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegreeEmptyDegCodeList() {
        List<PriorCollegeConcentrationArea> priorCollegeConcentrationAreaList = PriorCollegeConcentrationArea.findAll()
        assertEquals([], priorCollegeConcentrationAreaService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(
                priorCollegeConcentrationAreaList.pidm, priorCollegeConcentrationAreaList.sourceAndBackgroundInstitution, priorCollegeConcentrationAreaList.degreeSequenceNumber, []))
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree() {
        List<PriorCollegeConcentrationArea> priorCollegeConcentrationAreaList = PriorCollegeConcentrationArea.findAll()
        assertEquals([priorCollegeConcentrationAreaList[0]], priorCollegeConcentrationAreaService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(
                [priorCollegeConcentrationAreaList[0].pidm], [priorCollegeConcentrationAreaList[0].sourceAndBackgroundInstitution.code], [priorCollegeConcentrationAreaList[0].degreeSequenceNumber],
                [priorCollegeConcentrationAreaList[0].degree.code]))
    }

    private def newValidForCreatePriorCollegeConcentrationArea() {
        def priorCollegeDegree = newValidForCreatePriorCollegeDegree()
        priorCollegeDegree.save(failOnError: true, flush: true)

        def priorCollegeConcentrationArea = new PriorCollegeConcentrationArea(
                pidm: priorCollegeDegree.pidm,
                degreeSequenceNumber: priorCollegeDegree.degreeSequenceNumber,
                sourceAndBackgroundInstitution: priorCollegeDegree.sourceAndBackgroundInstitution,
                degree: priorCollegeDegree.degree,
                concentration: MajorMinorConcentration.findByCode("EDUC"),
        )
        return priorCollegeConcentrationArea
    }


    private def newInvalidForCreatePriorCollegeConcentrationArea() {
        def priorCollegeConcentrationArea = new PriorCollegeConcentrationArea(
                pidm: null,
                degreeSequenceNumber: 100,
                sourceAndBackgroundInstitution: null,
                degree: null,
                concentration: null,
        )
        return priorCollegeConcentrationArea
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

/*********************************************************************************
 Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.*
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class PriorCollegeMinorServiceIntegrationTests extends BaseIntegrationTestCase {

    def priorCollegeMinorService


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
    void testPriorCollegeMinorValidCreate() {
        def priorCollegeMinor = newValidForCreatePriorCollegeMinor()
        def map = [domainModel: priorCollegeMinor]
        priorCollegeMinor = priorCollegeMinorService.create(map)
        assertNotNull "PriorCollegeMinor ID is null in PriorCollegeMinor Service Tests Create", priorCollegeMinor.id
        assertNotNull "PriorCollegeMinor sourceAndBackgroundInstitution is null in PriorCollegeMinor Service Tests", priorCollegeMinor.sourceAndBackgroundInstitution
        assertNotNull "PriorCollegeMinor degree is null in PriorCollegeMinor Service Tests", priorCollegeMinor.degree
        assertNotNull "PriorCollegeMinor majorMinorConcentrationMinor is null in PriorCollegeMinor Service Tests", priorCollegeMinor.majorMinorConcentrationMinor
        assertNotNull priorCollegeMinor.pidm
        assertNotNull priorCollegeMinor.degreeSequenceNumber
        assertNotNull priorCollegeMinor.version
        assertNotNull priorCollegeMinor.dataOrigin
        assertNotNull priorCollegeMinor.lastModifiedBy
        assertNotNull priorCollegeMinor.lastModified
    }


    @Test
    void testPriorCollegeMinorInvalidCreate() {
        def priorCollegeMinor = newInvalidForCreatePriorCollegeMinor()
        def map = [domainModel: priorCollegeMinor]
        shouldFail(ApplicationException) {
            priorCollegeMinorService.create(map)
        }
    }


    @Test
    void testPriorCollegeMinorValidUpdate() {
        def priorCollegeMinor = newValidForCreatePriorCollegeMinor()
        def map = [domainModel: priorCollegeMinor]
        priorCollegeMinor = priorCollegeMinorService.create(map)
        assertNotNull "PriorCollegeMinor ID is null in PriorCollegeMinor Service Tests Create", priorCollegeMinor.id
        assertNotNull "PriorCollegeMinor sourceAndBackgroundInstitution is null in PriorCollegeMinor Service Tests", priorCollegeMinor.sourceAndBackgroundInstitution
        assertNotNull "PriorCollegeMinor degree is null in PriorCollegeMinor Service Tests", priorCollegeMinor.degree
        assertNotNull "PriorCollegeMinor majorMinorConcentrationMinor is null in PriorCollegeMinor Service Tests", priorCollegeMinor.majorMinorConcentrationMinor
        assertNotNull priorCollegeMinor.pidm
        assertNotNull priorCollegeMinor.degreeSequenceNumber
        assertNotNull priorCollegeMinor.version
        assertNotNull priorCollegeMinor.dataOrigin
        assertNotNull priorCollegeMinor.lastModifiedBy
        assertNotNull priorCollegeMinor.lastModified

        //Update the entity with new values
        priorCollegeMinor.degree = Degree.findByCode("MA")
        map.domainModel = priorCollegeMinor
        try {
            priorCollegeMinorService.update(map)
            fail "Cannot update curriculum"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "unsupported.operation"
        }
    }


    @Test
    void testPriorCollegeMinorDelete() {
        def priorCollegeMinor = newValidForCreatePriorCollegeMinor()
        def map = [domainModel: priorCollegeMinor]
        priorCollegeMinor = priorCollegeMinorService.create(map)
        assertNotNull "PriorCollegeMinor ID is null in PriorCollegeMinor Service Tests Create", priorCollegeMinor.id

        def id = priorCollegeMinor.id
        priorCollegeMinorService.delete([domainModel: priorCollegeMinor])
        assertNull "PriorCollegeMinor should have been deleted", priorCollegeMinor.get(id)
    }


    @Test
    void testReadOnly() {
        def priorCollegeMinor = newValidForCreatePriorCollegeMinor()
        def map = [domainModel: priorCollegeMinor]
        priorCollegeMinor = priorCollegeMinorService.create(map)
        assertNotNull "PriorCollegeMinor ID is null in PriorCollegeMinor Service Tests Create", priorCollegeMinor.id

        priorCollegeMinor.sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findWhere(code: "000000")
        priorCollegeMinor.pidm = PersonUtility.getPerson("HOR000002").pidm
        try {
            priorCollegeMinorService.update([domainModel: priorCollegeMinor])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegreeNullPidmList() {
        assertEquals([], priorCollegeMinorService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(null, [], [], []))
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegreeEmptyPidmList() {
        assertEquals([], priorCollegeMinorService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree([], [], [], []))
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegreeNullPrioirCollegeList() {
        assertEquals([], priorCollegeMinorService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(PriorCollegeMinor.findAll().pidm, null, [], []))
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegreeEmptyPrioirCollegeList() {
        assertEquals([], priorCollegeMinorService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(PriorCollegeMinor.findAll().pidm, [], [], []))
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegreeNullDegSeqNoList() {
        List<PriorCollegeMinor> priorCollegeMinorList = PriorCollegeMinor.findAll()
        assertEquals([], priorCollegeMinorService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(
                priorCollegeMinorList.pidm, priorCollegeMinorList.sourceAndBackgroundInstitution, null, []))
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegreeEmptyDegSeqNoList() {
        List<PriorCollegeMinor> priorCollegeMinorList = PriorCollegeMinor.findAll()
        assertEquals([], priorCollegeMinorService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(
                priorCollegeMinorList.pidm, priorCollegeMinorList.sourceAndBackgroundInstitution, [], []))
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegreeNullDegCodeList() {
        List<PriorCollegeMinor> priorCollegeMinorList = PriorCollegeMinor.findAll()
        assertEquals([], priorCollegeMinorService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(
                priorCollegeMinorList.pidm, priorCollegeMinorList.sourceAndBackgroundInstitution, priorCollegeMinorList.degreeSequenceNumber, null))
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegreeEmptyDegCodeList() {
        List<PriorCollegeMinor> priorCollegeMinorList = PriorCollegeMinor.findAll()
        assertEquals([], priorCollegeMinorService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(
                priorCollegeMinorList.pidm, priorCollegeMinorList.sourceAndBackgroundInstitution, priorCollegeMinorList.degreeSequenceNumber, []))
    }

    @Test
    void testFetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree() {
        List<PriorCollegeMinor> priorCollegeMinorList = PriorCollegeMinor.findAll()
        assertEquals([priorCollegeMinorList[0]], priorCollegeMinorService.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(
                [priorCollegeMinorList[0].pidm], [priorCollegeMinorList[0].sourceAndBackgroundInstitution.code], [priorCollegeMinorList[0].degreeSequenceNumber],
                [priorCollegeMinorList[0].degree.code]))
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

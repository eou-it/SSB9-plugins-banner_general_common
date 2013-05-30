/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.*
import net.hedtech.banner.testing.BaseIntegrationTestCase

class PriorCollegeMajorServiceIntegrationTests extends BaseIntegrationTestCase {

    def priorCollegeMajorService


    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testPriorCollegeMajorValidCreate() {
        def priorCollegeMajor = newValidForCreatePriorCollegeMajor()
        def map = [domainModel: priorCollegeMajor]
        priorCollegeMajor = priorCollegeMajorService.create(map)
        assertNotNull "PriorCollegeMajor ID is null in PriorCollegeMajor Service Tests Create", priorCollegeMajor.id
        assertNotNull "PriorCollegeMajor sourceAndBackgroundInstitution is null in PriorCollegeMajor Service Tests", priorCollegeMajor.sourceAndBackgroundInstitution
        assertNotNull "PriorCollegeMajor degree is null in PriorCollegeMajor Service Tests", priorCollegeMajor.degree
        assertNotNull "PriorCollegeMajor majorMinorConcentrationMajor is null in PriorCollegeMajor Service Tests", priorCollegeMajor.majorMinorConcentrationMajor
        assertNotNull priorCollegeMajor.pidm
        assertNotNull priorCollegeMajor.degreeSequenceNumber
        assertNotNull priorCollegeMajor.version
        assertNotNull priorCollegeMajor.dataOrigin
        assertNotNull priorCollegeMajor.lastModifiedBy
        assertNotNull priorCollegeMajor.lastModified
    }


    void testPriorCollegeMajorInvalidCreate() {
        def priorCollegeMajor = newInvalidForCreatePriorCollegeMajor()
        def map = [domainModel: priorCollegeMajor]
        shouldFail(ApplicationException) {
            priorCollegeMajorService.create(map)
        }
    }


    void testPriorCollegeMajorValidUpdate() {
        def priorCollegeMajor = newValidForCreatePriorCollegeMajor()
        def map = [domainModel: priorCollegeMajor]
        priorCollegeMajor = priorCollegeMajorService.create(map)
        assertNotNull "PriorCollegeMajor ID is null in PriorCollegeMajor Service Tests Create", priorCollegeMajor.id
        assertNotNull "PriorCollegeMajor sourceAndBackgroundInstitution is null in PriorCollegeMajor Service Tests", priorCollegeMajor.sourceAndBackgroundInstitution
        assertNotNull "PriorCollegeMajor degree is null in PriorCollegeMajor Service Tests", priorCollegeMajor.degree
        assertNotNull "PriorCollegeMajor majorMinorConcentrationMajor is null in PriorCollegeMajor Service Tests", priorCollegeMajor.majorMinorConcentrationMajor
        assertNotNull priorCollegeMajor.pidm
        assertNotNull priorCollegeMajor.degreeSequenceNumber
        assertNotNull priorCollegeMajor.version
        assertNotNull priorCollegeMajor.dataOrigin
        assertNotNull priorCollegeMajor.lastModifiedBy
        assertNotNull priorCollegeMajor.lastModified

        //Update the entity with new values
        priorCollegeMajor.degree = Degree.findByCode("MA")
        map.domainModel = priorCollegeMajor
        try {
            priorCollegeMajorService.update(map)
            fail "Cannot update curriculum"
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "unsupported.operation"
        }
    }


    void testPriorCollegeMajorDelete() {
        def priorCollegeMajor = newValidForCreatePriorCollegeMajor()
        def map = [domainModel: priorCollegeMajor]
        priorCollegeMajor = priorCollegeMajorService.create(map)
        assertNotNull "PriorCollegeMajor ID is null in PriorCollegeMajor Service Tests Create", priorCollegeMajor.id

        def id = priorCollegeMajor.id
        priorCollegeMajorService.delete([domainModel: priorCollegeMajor])
        assertNull "PriorCollegeMajor should have been deleted", priorCollegeMajor.get(id)
    }


    void testReadOnly() {
        def priorCollegeMajor = newValidForCreatePriorCollegeMajor()
        def map = [domainModel: priorCollegeMajor]
        priorCollegeMajor = priorCollegeMajorService.create(map)
        assertNotNull "PriorCollegeMajor ID is null in PriorCollegeMajor Service Tests Create", priorCollegeMajor.id

        priorCollegeMajor.sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findWhere(code: "000000")
        priorCollegeMajor.pidm = PersonUtility.getPerson("HOR000002").pidm
        try {
            priorCollegeMajorService.update([domainModel: priorCollegeMajor])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
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

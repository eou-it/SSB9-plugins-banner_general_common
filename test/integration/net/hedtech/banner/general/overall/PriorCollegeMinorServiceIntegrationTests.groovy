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

class PriorCollegeMinorServiceIntegrationTests extends BaseIntegrationTestCase {

    def priorCollegeMinorService


    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


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


    void testPriorCollegeMinorInvalidCreate() {
        def priorCollegeMinor = newInvalidForCreatePriorCollegeMinor()
        def map = [domainModel: priorCollegeMinor]
        shouldFail(ApplicationException) {
            priorCollegeMinorService.create(map)
        }
    }


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


    void testPriorCollegeMinorDelete() {
        def priorCollegeMinor = newValidForCreatePriorCollegeMinor()
        def map = [domainModel: priorCollegeMinor]
        priorCollegeMinor = priorCollegeMinorService.create(map)
        assertNotNull "PriorCollegeMinor ID is null in PriorCollegeMinor Service Tests Create", priorCollegeMinor.id

        def id = priorCollegeMinor.id
        priorCollegeMinorService.delete([domainModel: priorCollegeMinor])
        assertNull "PriorCollegeMinor should have been deleted", priorCollegeMinor.get(id)
    }


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

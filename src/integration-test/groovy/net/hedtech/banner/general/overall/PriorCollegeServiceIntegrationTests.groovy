/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
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
import net.hedtech.banner.general.system.AdmissionRequest
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase

@Integration
@Rollback
class PriorCollegeServiceIntegrationTests extends BaseIntegrationTestCase {

    def priorCollegeService


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
    void testPriorCollegeValidCreate() {
        def priorCollege = newValidForCreatePriorCollege()
        def map = [domainModel: priorCollege]
        priorCollege = priorCollegeService.create(map)
        assertNotNull "PriorCollege ID is null in PriorCollege Service Tests Create", priorCollege.id
        assertNotNull "PriorCollege sourceAndBackgroundInstitution is null in PriorCollege Service Tests", priorCollege.sourceAndBackgroundInstitution
        assertNotNull "PriorCollege admissionRequest is null in PriorCollege Service Tests", priorCollege.admissionRequest
        assertNotNull priorCollege.pidm
        assertNotNull priorCollege.officialTransaction
        assertNotNull priorCollege.version
        assertNotNull priorCollege.dataOrigin
        assertNotNull priorCollege.lastModifiedBy
        assertNotNull priorCollege.lastModified
    }


    @Test
    void testPriorCollegeInvalidCreate() {
        def priorCollege = newInvalidForCreatePriorCollege()
        def map = [domainModel: priorCollege]
        shouldFail(ApplicationException) {
            priorCollegeService.create(map)
        }
    }


    @Test
    void testPriorCollegeValidUpdate() {
        def priorCollege = newValidForCreatePriorCollege()
        def map = [domainModel: priorCollege]
        priorCollege = priorCollegeService.create(map)
        assertNotNull "PriorCollege ID is null in PriorCollege Service Tests Create", priorCollege.id
        assertNotNull "PriorCollege sourceAndBackgroundInstitution is null in PriorCollege Service Tests", priorCollege.sourceAndBackgroundInstitution
        assertNotNull "PriorCollege admissionRequest is null in PriorCollege Service Tests", priorCollege.admissionRequest
        assertNotNull priorCollege.pidm
        assertNotNull priorCollege.officialTransaction
        assertNotNull priorCollege.version
        assertNotNull priorCollege.dataOrigin
        assertNotNull priorCollege.lastModifiedBy
        assertNotNull priorCollege.lastModified

        //Update the entity with new values
        priorCollege.admissionRequest = AdmissionRequest.findByCode("TUTD")
        priorCollege.officialTransaction = null

        map.domainModel = priorCollege
        priorCollege = priorCollegeService.update(map)

        // test the values
        assertNull priorCollege.officialTransaction
        assertEquals "TUTD", priorCollege.admissionRequest.code
    }


    @Test
    void testPriorCollegeInvalidUpdate() {
        def priorCollege = newValidForCreatePriorCollege()
        def map = [domainModel: priorCollege]
        priorCollege = priorCollegeService.create(map)
        assertNotNull "PriorCollege ID is null in PriorCollege Service Tests Create", priorCollege.id
        assertNotNull "PriorCollege sourceAndBackgroundInstitution is null in PriorCollege Service Tests", priorCollege.sourceAndBackgroundInstitution
        assertNotNull "PriorCollege admissionRequest is null in PriorCollege Service Tests", priorCollege.admissionRequest
        assertNotNull priorCollege.pidm
        assertNotNull priorCollege.officialTransaction
        assertNotNull priorCollege.version
        assertNotNull priorCollege.dataOrigin
        assertNotNull priorCollege.lastModifiedBy
        assertNotNull priorCollege.lastModified

        //Update the entity with new invalid values
        priorCollege.officialTransaction = "ZZ"
        map.domainModel = priorCollege
        shouldFail(ApplicationException) {
            priorCollege = priorCollegeService.update(map)
        }
    }


    @Test
    void testPriorCollegeDelete() {
        def priorCollege = newValidForCreatePriorCollege()
        def map = [domainModel: priorCollege]
        priorCollege = priorCollegeService.create(map)
        assertNotNull "PriorCollege ID is null in PriorCollege Service Tests Create", priorCollege.id

        def id = priorCollege.id
        priorCollegeService.delete([domainModel: priorCollege])
        assertNull "PriorCollege should have been deleted", priorCollege.get(id)
    }


    @Test
    void testReadOnly() {
        def priorCollege = newValidForCreatePriorCollege()
        def map = [domainModel: priorCollege]
        priorCollege = priorCollegeService.create(map)
        assertNotNull "PriorCollege ID is null in PriorCollege Service Tests Create", priorCollege.id

        priorCollege.sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findWhere(code: "000000")
        priorCollege.pidm = PersonUtility.getPerson("HOR000002").pidm
        try {
            priorCollegeService.update([domainModel: priorCollege])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
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


    private def newInvalidForCreatePriorCollege() {
        def priorCollege = new PriorCollege(
                pidm: null,
                transactionRecvDate: new Date(),
                transactionRevDate: new Date(),
                officialTransaction: "ZZ",
                sourceAndBackgroundInstitution: null,
                admissionRequest: null,
        )
        return priorCollege
    }
}

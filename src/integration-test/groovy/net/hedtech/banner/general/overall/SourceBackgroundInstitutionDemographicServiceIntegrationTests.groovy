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
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase

@Integration
@Rollback
class SourceBackgroundInstitutionDemographicServiceIntegrationTests extends BaseIntegrationTestCase {

    def sourceBackgroundInstitutionDemographicService


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
    void testSourceBackgroundInstitutionDemographicValidCreate() {
        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
        def map = [domainModel: sourceBackgroundInstitutionDemographic]
        sourceBackgroundInstitutionDemographic = sourceBackgroundInstitutionDemographicService.create(map)
        assertNotNull "SourceBackgroundInstitutionDemographic ID is null in SourceBackgroundInstitutionDemographic Service Tests Create", sourceBackgroundInstitutionDemographic.id
        assertNotNull "SourceBackgroundInstitutionDemographic sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionDemographic Service Tests", sourceBackgroundInstitutionDemographic.sourceAndBackgroundInstitution
        assertNotNull sourceBackgroundInstitutionDemographic.version
        assertNotNull sourceBackgroundInstitutionDemographic.dataOrigin
        assertNotNull sourceBackgroundInstitutionDemographic.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionDemographic.lastModified
    }


    @Test
    void testSourceBackgroundInstitutionDemographicInvalidCreate() {
        def sourceBackgroundInstitutionDemographic = newInvalidForCreateSourceBackgroundInstitutionDemographic()
        def map = [domainModel: sourceBackgroundInstitutionDemographic]
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionDemographicService.create(map)
        }
    }


    @Test
    void testSourceBackgroundInstitutionDemographicValidUpdate() {
        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
        def map = [domainModel: sourceBackgroundInstitutionDemographic]
        sourceBackgroundInstitutionDemographic = sourceBackgroundInstitutionDemographicService.create(map)
        assertNotNull "SourceBackgroundInstitutionDemographic ID is null in SourceBackgroundInstitutionDemographic Service Tests Create", sourceBackgroundInstitutionDemographic.id
        assertNotNull "SourceBackgroundInstitutionDemographic sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionDemographic Service Tests", sourceBackgroundInstitutionDemographic.sourceAndBackgroundInstitution
        assertNotNull sourceBackgroundInstitutionDemographic.version
        assertNotNull sourceBackgroundInstitutionDemographic.dataOrigin
        assertNotNull sourceBackgroundInstitutionDemographic.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionDemographic.lastModified

        //Update the entity with new values
        sourceBackgroundInstitutionDemographic.enrollment = 101
        sourceBackgroundInstitutionDemographic.numberOfSeniors = 51
        sourceBackgroundInstitutionDemographic.meanFamilyIncome = 61
        sourceBackgroundInstitutionDemographic.percentCollegeBound = 81
        map.domainModel = sourceBackgroundInstitutionDemographic
        sourceBackgroundInstitutionDemographic = sourceBackgroundInstitutionDemographicService.update(map)

        // test the values
        assertEquals 101, sourceBackgroundInstitutionDemographic.enrollment
        assertEquals 51, sourceBackgroundInstitutionDemographic.numberOfSeniors
        assertEquals 61, sourceBackgroundInstitutionDemographic.meanFamilyIncome
        assertEquals 81, sourceBackgroundInstitutionDemographic.percentCollegeBound
    }


    @Test
    void testSourceBackgroundInstitutionDemographicInvalidUpdate() {
        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
        def map = [domainModel: sourceBackgroundInstitutionDemographic]
        sourceBackgroundInstitutionDemographic = sourceBackgroundInstitutionDemographicService.create(map)
        assertNotNull "SourceBackgroundInstitutionDemographic ID is null in SourceBackgroundInstitutionDemographic Service Tests Create", sourceBackgroundInstitutionDemographic.id
        assertNotNull "SourceBackgroundInstitutionDemographic sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionDemographic Service Tests", sourceBackgroundInstitutionDemographic.sourceAndBackgroundInstitution
        assertNotNull sourceBackgroundInstitutionDemographic.version
        assertNotNull sourceBackgroundInstitutionDemographic.dataOrigin
        assertNotNull sourceBackgroundInstitutionDemographic.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionDemographic.lastModified

        //Update the entity with new invalid values
        sourceBackgroundInstitutionDemographic.enrollment = 100000
        map.domainModel = sourceBackgroundInstitutionDemographic
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionDemographic = sourceBackgroundInstitutionDemographicService.update(map)
        }
    }


    @Test
    void testSourceBackgroundInstitutionDemographicDelete() {
        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
        def map = [domainModel: sourceBackgroundInstitutionDemographic]
        sourceBackgroundInstitutionDemographic = sourceBackgroundInstitutionDemographicService.create(map)
        assertNotNull "SourceBackgroundInstitutionDemographic ID is null in SourceBackgroundInstitutionDemographic Service Tests Create", sourceBackgroundInstitutionDemographic.id
        def id = sourceBackgroundInstitutionDemographic.id
        sourceBackgroundInstitutionDemographicService.delete([domainModel: sourceBackgroundInstitutionDemographic])
        assertNull "SourceBackgroundInstitutionDemographic should have been deleted", sourceBackgroundInstitutionDemographic.get(id)
    }


    @Test
    void testReadOnly() {
        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
        def map = [domainModel: sourceBackgroundInstitutionDemographic]
        sourceBackgroundInstitutionDemographic = sourceBackgroundInstitutionDemographicService.create(map)
        assertNotNull "SourceBackgroundInstitutionDemographic ID is null in SourceBackgroundInstitutionDemographic Service Tests Create", sourceBackgroundInstitutionDemographic.id

        sourceBackgroundInstitutionDemographic.sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findWhere(code: "5815")
        sourceBackgroundInstitutionDemographic.demographicYear = 2013
        try {
            sourceBackgroundInstitutionDemographicService.update([domainModel: sourceBackgroundInstitutionDemographic])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    private def newValidForCreateSourceBackgroundInstitutionDemographic() {
        def sourceBackgroundInstitutionDemographic = new SourceBackgroundInstitutionDemographic(
                demographicYear: 2014,
                enrollment: 100,
                numberOfSeniors: 50,
                meanFamilyIncome: 60,
                percentCollegeBound: 80,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "443361"),
        )
        return sourceBackgroundInstitutionDemographic
    }


    private def newInvalidForCreateSourceBackgroundInstitutionDemographic() {
        def sourceBackgroundInstitutionDemographic = new SourceBackgroundInstitutionDemographic(
                demographicYear: null,
                sourceAndBackgroundInstitution: null,
        )
        return sourceBackgroundInstitutionDemographic
    }
}

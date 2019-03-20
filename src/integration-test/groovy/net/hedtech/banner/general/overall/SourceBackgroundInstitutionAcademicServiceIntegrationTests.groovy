/*********************************************************************************
  Copyright 2010-2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase

class SourceBackgroundInstitutionAcademicServiceIntegrationTests extends BaseIntegrationTestCase {

    def sourceBackgroundInstitutionAcademicService


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
    void testSourceBackgroundInstitutionAcademicValidCreate() {
        def sourceBackgroundInstitutionAcademic = newValidForCreateSourceBackgroundInstitutionAcademic()
        def map = [domainModel: sourceBackgroundInstitutionAcademic]
        sourceBackgroundInstitutionAcademic = sourceBackgroundInstitutionAcademicService.create(map)
        assertNotNull "SourceBackgroundInstitutionAcademic ID is null in SourceBackgroundInstitutionAcademic Service Tests Create", sourceBackgroundInstitutionAcademic.id
        assertNotNull "SourceBackgroundInstitutionAcademic sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionAcademic Service Tests", sourceBackgroundInstitutionAcademic.sourceAndBackgroundInstitution
        assertNotNull sourceBackgroundInstitutionAcademic.version
        assertNotNull sourceBackgroundInstitutionAcademic.dataOrigin
        assertNotNull sourceBackgroundInstitutionAcademic.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionAcademic.lastModified
    }


    @Test
    void testSourceBackgroundInstitutionAcademicInvalidCreate() {
        def sourceBackgroundInstitutionAcademic = newInvalidForCreateSourceBackgroundInstitutionAcademic()
        def map = [domainModel: sourceBackgroundInstitutionAcademic]
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionAcademicService.create(map)
        }
    }


    @Test
    void testSourceBackgroundInstitutionAcademicValidUpdate() {
        def sourceBackgroundInstitutionAcademic = newValidForCreateSourceBackgroundInstitutionAcademic()
        def map = [domainModel: sourceBackgroundInstitutionAcademic]
        sourceBackgroundInstitutionAcademic = sourceBackgroundInstitutionAcademicService.create(map)
        assertNotNull "SourceBackgroundInstitutionAcademic ID is null in SourceBackgroundInstitutionAcademic Service Tests Create", sourceBackgroundInstitutionAcademic.id
        assertNotNull "SourceBackgroundInstitutionAcademic sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionAcademic Service Tests", sourceBackgroundInstitutionAcademic.sourceAndBackgroundInstitution
        assertNotNull sourceBackgroundInstitutionAcademic.version
        assertNotNull sourceBackgroundInstitutionAcademic.dataOrigin
        assertNotNull sourceBackgroundInstitutionAcademic.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionAcademic.lastModified

        //Update the entity with new values
        sourceBackgroundInstitutionAcademic.stateApprovIndicator = null
        sourceBackgroundInstitutionAcademic.calendarType = "UPDATE"
        sourceBackgroundInstitutionAcademic.accreditationType = "UPDT"
        sourceBackgroundInstitutionAcademic.creditTransactionValue = 5.0
        map.domainModel = sourceBackgroundInstitutionAcademic
        sourceBackgroundInstitutionAcademic = sourceBackgroundInstitutionAcademicService.update(map)

        // test the values
        assertNull sourceBackgroundInstitutionAcademic.stateApprovIndicator
        assertEquals "UPDATE", sourceBackgroundInstitutionAcademic.calendarType
        assertEquals "UPDT", sourceBackgroundInstitutionAcademic.accreditationType
        assertEquals 5.0, sourceBackgroundInstitutionAcademic.creditTransactionValue, 0.001
    }


    @Test
    void testSourceBackgroundInstitutionAcademicInvalidUpdate() {
        def sourceBackgroundInstitutionAcademic = newValidForCreateSourceBackgroundInstitutionAcademic()
        def map = [domainModel: sourceBackgroundInstitutionAcademic]
        sourceBackgroundInstitutionAcademic = sourceBackgroundInstitutionAcademicService.create(map)
        assertNotNull "SourceBackgroundInstitutionAcademic ID is null in SourceBackgroundInstitutionAcademic Service Tests Create", sourceBackgroundInstitutionAcademic.id
        assertNotNull "SourceBackgroundInstitutionAcademic sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionAcademic Service Tests", sourceBackgroundInstitutionAcademic.sourceAndBackgroundInstitution
        assertNotNull sourceBackgroundInstitutionAcademic.version
        assertNotNull sourceBackgroundInstitutionAcademic.dataOrigin
        assertNotNull sourceBackgroundInstitutionAcademic.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionAcademic.lastModified

        //Update the entity with new invalid values
        sourceBackgroundInstitutionAcademic.stateApprovIndicator = "Z"
        map.domainModel = sourceBackgroundInstitutionAcademic
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionAcademic = sourceBackgroundInstitutionAcademicService.update(map)
        }
    }


    @Test
    void testSourceBackgroundInstitutionAcademicDelete() {
        def sourceBackgroundInstitutionAcademic = newValidForCreateSourceBackgroundInstitutionAcademic()
        def map = [domainModel: sourceBackgroundInstitutionAcademic]
        sourceBackgroundInstitutionAcademic = sourceBackgroundInstitutionAcademicService.create(map)
        assertNotNull "SourceBackgroundInstitutionAcademic ID is null in SourceBackgroundInstitutionAcademic Service Tests Create", sourceBackgroundInstitutionAcademic.id
        def id = sourceBackgroundInstitutionAcademic.id
        sourceBackgroundInstitutionAcademicService.delete([domainModel: sourceBackgroundInstitutionAcademic])
        assertNull "SourceBackgroundInstitutionAcademic should have been deleted", sourceBackgroundInstitutionAcademic.get(id)
    }


    @Test
    void testReadOnly() {
        def sourceBackgroundInstitutionAcademic = newValidForCreateSourceBackgroundInstitutionAcademic()
        def map = [domainModel: sourceBackgroundInstitutionAcademic]
        sourceBackgroundInstitutionAcademic = sourceBackgroundInstitutionAcademicService.create(map)
        assertNotNull "SourceBackgroundInstitutionAcademic ID is null in SourceBackgroundInstitutionAcademic Service Tests Create", sourceBackgroundInstitutionAcademic.id

        sourceBackgroundInstitutionAcademic.sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findWhere(code: "5815")
        sourceBackgroundInstitutionAcademic.demographicYear = 2013
        try {
            sourceBackgroundInstitutionAcademicService.update([domainModel: sourceBackgroundInstitutionAcademic])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    private def newValidForCreateSourceBackgroundInstitutionAcademic() {
        def sourceBackgroundInstitutionAcademic = new SourceBackgroundInstitutionAcademic(
                demographicYear: 2014,
                stateApprovIndicator: "Y",
                calendarType: "STANDARD",
                accreditationType: "TTTT",
                creditTransactionValue: 5.5,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "443361"),
        )
        return sourceBackgroundInstitutionAcademic
    }


    private def newInvalidForCreateSourceBackgroundInstitutionAcademic() {
        def sourceBackgroundInstitutionAcademic = new SourceBackgroundInstitutionAcademic(
                demographicYear: null,
                sourceAndBackgroundInstitution: null,
        )
        return sourceBackgroundInstitutionAcademic
    }
}

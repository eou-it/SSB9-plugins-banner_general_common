/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase

class SourceBackgroundInstitutionAcademicServiceIntegrationTests extends BaseIntegrationTestCase {

    def sourceBackgroundInstitutionAcademicService


    void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    void tearDown() {
        super.tearDown()
    }


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


    void testSourceBackgroundInstitutionAcademicInvalidCreate() {
        def sourceBackgroundInstitutionAcademic = newInvalidForCreateSourceBackgroundInstitutionAcademic()
        def map = [domainModel: sourceBackgroundInstitutionAcademic]
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionAcademicService.create(map)
        }
    }


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
        assertEquals 5.0, sourceBackgroundInstitutionAcademic.creditTransactionValue
    }


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


    void testSourceBackgroundInstitutionAcademicDelete() {
        def sourceBackgroundInstitutionAcademic = newValidForCreateSourceBackgroundInstitutionAcademic()
        def map = [domainModel: sourceBackgroundInstitutionAcademic]
        sourceBackgroundInstitutionAcademic = sourceBackgroundInstitutionAcademicService.create(map)
        assertNotNull "SourceBackgroundInstitutionAcademic ID is null in SourceBackgroundInstitutionAcademic Service Tests Create", sourceBackgroundInstitutionAcademic.id
        def id = sourceBackgroundInstitutionAcademic.id
        sourceBackgroundInstitutionAcademicService.delete([domainModel: sourceBackgroundInstitutionAcademic])
        assertNull "SourceBackgroundInstitutionAcademic should have been deleted", sourceBackgroundInstitutionAcademic.get(id)
    }


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

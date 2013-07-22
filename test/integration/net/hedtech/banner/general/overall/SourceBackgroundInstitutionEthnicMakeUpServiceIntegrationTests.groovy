/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.Ethnicity
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase

class SourceBackgroundInstitutionEthnicMakeUpServiceIntegrationTests extends BaseIntegrationTestCase {

    def sourceBackgroundInstitutionEthnicMakeUpService


    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testSourceBackgroundInstitutionEthnicMakeUpValidCreate() {
        def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
        def map = [domainModel: sourceBackgroundInstitutionEthnicMakeUp]
        sourceBackgroundInstitutionEthnicMakeUp = sourceBackgroundInstitutionEthnicMakeUpService.create(map)
        assertNotNull "SourceBackgroundInstitutionEthnicMakeUp ID is null in SourceBackgroundInstitutionEthnicMakeUp Service Tests Create", sourceBackgroundInstitutionEthnicMakeUp.id
        assertNotNull "SourceBackgroundInstitutionEthnicMakeUp sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionEthnicMakeUp Service Tests", sourceBackgroundInstitutionEthnicMakeUp.sourceAndBackgroundInstitution
        assertNotNull "SourceBackgroundInstitutionEthnicMakeUp ethnicity is null in SourceBackgroundInstitutionEthnicMakeUp Service Tests", sourceBackgroundInstitutionEthnicMakeUp.ethnicity
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.version
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.dataOrigin
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.lastModified
    }


    void testSourceBackgroundInstitutionEthnicMakeUpInvalidCreate() {
        def sourceBackgroundInstitutionEthnicMakeUp = newInvalidForCreateSourceBackgroundInstitutionEthnicMakeUp()
        def map = [domainModel: sourceBackgroundInstitutionEthnicMakeUp]
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionEthnicMakeUpService.create(map)
        }
    }


    void testSourceBackgroundInstitutionEthnicMakeUpValidUpdate() {
        def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
        def map = [domainModel: sourceBackgroundInstitutionEthnicMakeUp]
        sourceBackgroundInstitutionEthnicMakeUp = sourceBackgroundInstitutionEthnicMakeUpService.create(map)
        assertNotNull "SourceBackgroundInstitutionEthnicMakeUp ID is null in SourceBackgroundInstitutionEthnicMakeUp Service Tests Create", sourceBackgroundInstitutionEthnicMakeUp.id
        assertNotNull "SourceBackgroundInstitutionEthnicMakeUp sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionEthnicMakeUp Service Tests", sourceBackgroundInstitutionEthnicMakeUp.sourceAndBackgroundInstitution
        assertNotNull "SourceBackgroundInstitutionEthnicMakeUp ethnicity is null in SourceBackgroundInstitutionEthnicMakeUp Service Tests", sourceBackgroundInstitutionEthnicMakeUp.ethnicity
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.version
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.dataOrigin
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.lastModified

        //Update the entity with new values
        sourceBackgroundInstitutionEthnicMakeUp.ethnicPercent = 51
        map.domainModel = sourceBackgroundInstitutionEthnicMakeUp
        sourceBackgroundInstitutionEthnicMakeUp = sourceBackgroundInstitutionEthnicMakeUpService.update(map)

        // test the values
        assertEquals 51, sourceBackgroundInstitutionEthnicMakeUp.ethnicPercent
    }


    void testSourceBackgroundInstitutionEthnicMakeUpInvalidUpdate() {
        def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
        def map = [domainModel: sourceBackgroundInstitutionEthnicMakeUp]
        sourceBackgroundInstitutionEthnicMakeUp = sourceBackgroundInstitutionEthnicMakeUpService.create(map)
        assertNotNull "SourceBackgroundInstitutionEthnicMakeUp ID is null in SourceBackgroundInstitutionEthnicMakeUp Service Tests Create", sourceBackgroundInstitutionEthnicMakeUp.id
        assertNotNull "SourceBackgroundInstitutionEthnicMakeUp sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionEthnicMakeUp Service Tests", sourceBackgroundInstitutionEthnicMakeUp.sourceAndBackgroundInstitution
        assertNotNull "SourceBackgroundInstitutionEthnicMakeUp ethnicity is null in SourceBackgroundInstitutionEthnicMakeUp Service Tests", sourceBackgroundInstitutionEthnicMakeUp.ethnicity
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.version
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.dataOrigin
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.lastModified

        //Update the entity with new invalid values
        sourceBackgroundInstitutionEthnicMakeUp.ethnicPercent = 1000
        map.domainModel = sourceBackgroundInstitutionEthnicMakeUp
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionEthnicMakeUp = sourceBackgroundInstitutionEthnicMakeUpService.update(map)
        }
    }


    void testSourceBackgroundInstitutionEthnicMakeUpDelete() {
        def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
        def map = [domainModel: sourceBackgroundInstitutionEthnicMakeUp]
        sourceBackgroundInstitutionEthnicMakeUp = sourceBackgroundInstitutionEthnicMakeUpService.create(map)
        assertNotNull "SourceBackgroundInstitutionEthnicMakeUp ID is null in SourceBackgroundInstitutionEthnicMakeUp Service Tests Create", sourceBackgroundInstitutionEthnicMakeUp.id
        def id = sourceBackgroundInstitutionEthnicMakeUp.id
        sourceBackgroundInstitutionEthnicMakeUpService.delete([domainModel: sourceBackgroundInstitutionEthnicMakeUp])
        assertNull "SourceBackgroundInstitutionEthnicMakeUp should have been deleted", sourceBackgroundInstitutionEthnicMakeUp.get(id)
    }


    void testReadOnly() {
        def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
        def map = [domainModel: sourceBackgroundInstitutionEthnicMakeUp]
        sourceBackgroundInstitutionEthnicMakeUp = sourceBackgroundInstitutionEthnicMakeUpService.create(map)
        assertNotNull "SourceBackgroundInstitutionEthnicMakeUp ID is null in SourceBackgroundInstitutionEthnicMakeUp Service Tests Create", sourceBackgroundInstitutionEthnicMakeUp.id

        sourceBackgroundInstitutionEthnicMakeUp.sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findWhere(code: "5815")
        sourceBackgroundInstitutionEthnicMakeUp.ethnicity = Ethnicity.findWhere(code: "2")
        sourceBackgroundInstitutionEthnicMakeUp.demographicYear = 2013
        try {
            sourceBackgroundInstitutionEthnicMakeUpService.update([domainModel: sourceBackgroundInstitutionEthnicMakeUp])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    private def newValidForCreateSourceBackgroundInstitutionEthnicMakeUp() {
        def sourceBackgroundInstitutionEthnicMakeUp = new SourceBackgroundInstitutionEthnicMakeUp(
                demographicYear: 2014,
                ethnicPercent: 50,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "443361"),
                ethnicity: Ethnicity.findWhere(code: "1"),
        )
        return sourceBackgroundInstitutionEthnicMakeUp
    }


    private def newInvalidForCreateSourceBackgroundInstitutionEthnicMakeUp() {
        def sourceBackgroundInstitutionEthnicMakeUp = new SourceBackgroundInstitutionEthnicMakeUp(
                demographicYear: null,
                sourceAndBackgroundInstitution: null,
                ethnicity: null,
        )
        return sourceBackgroundInstitutionEthnicMakeUp
    }
}

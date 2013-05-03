/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.Degree
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase

class SourceBackgroundInstitutionDegreesOfferedServiceIntegrationTests extends BaseIntegrationTestCase {

    def sourceBackgroundInstitutionDegreesOfferedService


    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }



    protected void tearDown() {
        super.tearDown()
    }


    void testSourceBackgroundInstitutionDegreesOfferedValidCreate() {
        def sourceBackgroundInstitutionDegreesOffered = newValidForCreateSourceBackgroundInstitutionDegreesOffered()
        def map = [domainModel: sourceBackgroundInstitutionDegreesOffered]
        sourceBackgroundInstitutionDegreesOffered = sourceBackgroundInstitutionDegreesOfferedService.create(map)
        assertNotNull "SourceBackgroundInstitutionDegreesOffered ID is null in SourceBackgroundInstitutionDegreesOffered Service Tests Create", sourceBackgroundInstitutionDegreesOffered.id
        assertNotNull "SourceBackgroundInstitutionDegreesOffered sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionDegreesOffered Service Tests", sourceBackgroundInstitutionDegreesOffered.sourceAndBackgroundInstitution
        assertNotNull "SourceBackgroundInstitutionDegreesOffered degree is null in SourceBackgroundInstitutionDegreesOffered Service Tests", sourceBackgroundInstitutionDegreesOffered.degree
        assertNotNull sourceBackgroundInstitutionDegreesOffered.version
        assertNotNull sourceBackgroundInstitutionDegreesOffered.dataOrigin
        assertNotNull sourceBackgroundInstitutionDegreesOffered.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionDegreesOffered.lastModified
    }


    void testSourceBackgroundInstitutionDegreesOfferedInvalidCreate() {
        def sourceBackgroundInstitutionDegreesOffered = newInvalidForCreateSourceBackgroundInstitutionDegreesOffered()
        def map = [domainModel: sourceBackgroundInstitutionDegreesOffered]
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionDegreesOfferedService.create(map)
        }
    }

    // NOTE: No Updates are allowed

    void testSourceBackgroundInstitutionDegreesOfferedDelete() {
        def sourceBackgroundInstitutionDegreesOffered = newValidForCreateSourceBackgroundInstitutionDegreesOffered()
        def map = [domainModel: sourceBackgroundInstitutionDegreesOffered]
        sourceBackgroundInstitutionDegreesOffered = sourceBackgroundInstitutionDegreesOfferedService.create(map)
        assertNotNull "SourceBackgroundInstitutionDegreesOffered ID is null in SourceBackgroundInstitutionDegreesOffered Service Tests Create", sourceBackgroundInstitutionDegreesOffered.id
        def id = sourceBackgroundInstitutionDegreesOffered.id
        sourceBackgroundInstitutionDegreesOfferedService.delete([domainModel: sourceBackgroundInstitutionDegreesOffered])
        assertNull "SourceBackgroundInstitutionDegreesOffered should have been deleted", sourceBackgroundInstitutionDegreesOffered.get(id)
    }


    void testReadOnly() {
        def sourceBackgroundInstitutionDegreesOffered = newValidForCreateSourceBackgroundInstitutionDegreesOffered()
        def map = [domainModel: sourceBackgroundInstitutionDegreesOffered]
        sourceBackgroundInstitutionDegreesOffered = sourceBackgroundInstitutionDegreesOfferedService.create(map)
        assertNotNull "SourceBackgroundInstitutionDegreesOffered ID is null in SourceBackgroundInstitutionDegreesOffered Service Tests Create", sourceBackgroundInstitutionDegreesOffered.id

        sourceBackgroundInstitutionDegreesOffered.sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findWhere(code: "5815")
        sourceBackgroundInstitutionDegreesOffered.degree = Degree.findWhere(code: "MA")
        sourceBackgroundInstitutionDegreesOffered.demographicYear = 2013
        try {
            sourceBackgroundInstitutionDegreesOfferedService.update([domainModel: sourceBackgroundInstitutionDegreesOffered])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    private def newValidForCreateSourceBackgroundInstitutionDegreesOffered() {
        def sourceBackgroundInstitutionDegreesOffered = new SourceBackgroundInstitutionDegreesOffered(
                demographicYear: 2014,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "443361"),
                degree: Degree.findWhere(code: "PHD"),
        )
        return sourceBackgroundInstitutionDegreesOffered
    }


    private def newInvalidForCreateSourceBackgroundInstitutionDegreesOffered() {
        def sourceBackgroundInstitutionDegreesOffered = new SourceBackgroundInstitutionDegreesOffered(
                demographicYear: null,
                sourceAndBackgroundInstitution: null,
                degree: null,
        )
        return sourceBackgroundInstitutionDegreesOffered
    }
}

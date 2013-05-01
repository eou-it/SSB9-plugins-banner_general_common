/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.DiplomaType
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase

class SourceBackgroundInstitutionDiplomasOfferedServiceIntegrationTests extends BaseIntegrationTestCase {

    def sourceBackgroundInstitutionDiplomasOfferedService


    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testSourceBackgroundInstitutionDiplomasOfferedValidCreate() {
        def sourceBackgroundInstitutionDiplomasOffered = newValidForCreateSourceBackgroundInstitutionDiplomasOffered()
        def map = [domainModel: sourceBackgroundInstitutionDiplomasOffered]
        sourceBackgroundInstitutionDiplomasOffered = sourceBackgroundInstitutionDiplomasOfferedService.create(map)
        assertNotNull "SourceBackgroundInstitutionDiplomasOffered ID is null in SourceBackgroundInstitutionDiplomasOffered Service Tests Create", sourceBackgroundInstitutionDiplomasOffered.id
        assertNotNull "SourceBackgroundInstitutionDiplomasOffered sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionDiplomasOffered Service Tests", sourceBackgroundInstitutionDiplomasOffered.sourceAndBackgroundInstitution
        assertNotNull "SourceBackgroundInstitutionDiplomasOffered diplomaType is null in SourceBackgroundInstitutionDiplomasOffered Service Tests", sourceBackgroundInstitutionDiplomasOffered.diplomaType
        assertNotNull sourceBackgroundInstitutionDiplomasOffered.version
        assertNotNull sourceBackgroundInstitutionDiplomasOffered.dataOrigin
        assertNotNull sourceBackgroundInstitutionDiplomasOffered.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionDiplomasOffered.lastModified
    }

	void testSourceBackgroundInstitutionDiplomasOfferedInvalidCreate() {
		def sourceBackgroundInstitutionDiplomasOffered = newInvalidForCreateSourceBackgroundInstitutionDiplomasOffered()
		def map = [domainModel: sourceBackgroundInstitutionDiplomasOffered]
		shouldFail(ApplicationException) {
			sourceBackgroundInstitutionDiplomasOfferedService.create(map)
		}
    }

    // NOTE: No Updates are allowed

	void testSourceBackgroundInstitutionDiplomasOfferedDelete() {
		def sourceBackgroundInstitutionDiplomasOffered = newValidForCreateSourceBackgroundInstitutionDiplomasOffered()
		def map = [domainModel: sourceBackgroundInstitutionDiplomasOffered]
        sourceBackgroundInstitutionDiplomasOffered = sourceBackgroundInstitutionDiplomasOfferedService.create(map)
		assertNotNull "SourceBackgroundInstitutionDiplomasOffered ID is null in SourceBackgroundInstitutionDiplomasOffered Service Tests Create", sourceBackgroundInstitutionDiplomasOffered.id
		def id = sourceBackgroundInstitutionDiplomasOffered.id
		sourceBackgroundInstitutionDiplomasOfferedService.delete( [domainModel: sourceBackgroundInstitutionDiplomasOffered] )
		assertNull "SourceBackgroundInstitutionDiplomasOffered should have been deleted", sourceBackgroundInstitutionDiplomasOffered.get(id)
  	}


    private def newValidForCreateSourceBackgroundInstitutionDiplomasOffered() {
        def diplomaType = newDiplomaType()
        diplomaType.save(failOnError: true, flush: true)
        def sourceBackgroundInstitutionDiplomasOffered = new SourceBackgroundInstitutionDiplomasOffered(
                demographicYear: 2014,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "443361"),
                diplomaType: diplomaType,
        )
        return sourceBackgroundInstitutionDiplomasOffered
    }


    private def newInvalidForCreateSourceBackgroundInstitutionDiplomasOffered() {
        def sourceBackgroundInstitutionDiplomasOffered = new SourceBackgroundInstitutionDiplomasOffered(
                demographicYear: null,
                sourceAndBackgroundInstitution: null,
                diplomaType: null,
        )
        return sourceBackgroundInstitutionDiplomasOffered
    }


    private def newDiplomaType() {
        def diplomaType = new DiplomaType(
                code: "TT",
                description: "TTTT"
        )
        return diplomaType
    }
}

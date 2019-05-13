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
import static groovy.test.GroovyAssert.*
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.DiplomaType
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase

@Integration
@Rollback
class SourceBackgroundInstitutionDiplomasOfferedServiceIntegrationTests extends BaseIntegrationTestCase {

    def sourceBackgroundInstitutionDiplomasOfferedService


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


    @Test
    void testSourceBackgroundInstitutionDiplomasOfferedInvalidCreate() {
        def sourceBackgroundInstitutionDiplomasOffered = newInvalidForCreateSourceBackgroundInstitutionDiplomasOffered()
        def map = [domainModel: sourceBackgroundInstitutionDiplomasOffered]
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionDiplomasOfferedService.create(map)
        }
    }

    // NOTE: No Updates are allowed

    @Test
    void testSourceBackgroundInstitutionDiplomasOfferedDelete() {
        def sourceBackgroundInstitutionDiplomasOffered = newValidForCreateSourceBackgroundInstitutionDiplomasOffered()
        def map = [domainModel: sourceBackgroundInstitutionDiplomasOffered]
        sourceBackgroundInstitutionDiplomasOffered = sourceBackgroundInstitutionDiplomasOfferedService.create(map)
        assertNotNull "SourceBackgroundInstitutionDiplomasOffered ID is null in SourceBackgroundInstitutionDiplomasOffered Service Tests Create", sourceBackgroundInstitutionDiplomasOffered.id
        def id = sourceBackgroundInstitutionDiplomasOffered.id
        sourceBackgroundInstitutionDiplomasOfferedService.delete([domainModel: sourceBackgroundInstitutionDiplomasOffered])
        assertNull "SourceBackgroundInstitutionDiplomasOffered should have been deleted", sourceBackgroundInstitutionDiplomasOffered.get(id)
    }


    @Test
    void testReadOnly() {
        def sourceBackgroundInstitutionDiplomasOffered = newValidForCreateSourceBackgroundInstitutionDiplomasOffered()
        def map = [domainModel: sourceBackgroundInstitutionDiplomasOffered]
        sourceBackgroundInstitutionDiplomasOffered = sourceBackgroundInstitutionDiplomasOfferedService.create(map)
        assertNotNull "SourceBackgroundInstitutionDiplomasOffered ID is null in SourceBackgroundInstitutionDiplomasOffered Service Tests Create", sourceBackgroundInstitutionDiplomasOffered.id

        sourceBackgroundInstitutionDiplomasOffered.sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findWhere(code: "5815")
        sourceBackgroundInstitutionDiplomasOffered.diplomaType = new DiplomaType(
                code: "ZZ",
                description: "ZZZZ"
        )
        sourceBackgroundInstitutionDiplomasOffered.demographicYear = 2013
        try {
            sourceBackgroundInstitutionDiplomasOfferedService.update([domainModel: sourceBackgroundInstitutionDiplomasOffered])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
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

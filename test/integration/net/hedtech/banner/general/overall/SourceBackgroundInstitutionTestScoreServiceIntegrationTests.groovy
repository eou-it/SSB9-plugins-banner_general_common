/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.general.system.TestScore
import net.hedtech.banner.testing.BaseIntegrationTestCase

class SourceBackgroundInstitutionTestScoreServiceIntegrationTests extends BaseIntegrationTestCase {

    def sourceBackgroundInstitutionTestScoreService


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
    void testSourceBackgroundInstitutionTestScoreValidCreate() {
        def sourceBackgroundInstitutionTestScore = newValidForCreateSourceBackgroundInstitutionTestScore()
        def map = [domainModel: sourceBackgroundInstitutionTestScore]
        sourceBackgroundInstitutionTestScore = sourceBackgroundInstitutionTestScoreService.create(map)
        assertNotNull "SourceBackgroundInstitutionTestScore ID is null in SourceBackgroundInstitutionTestScore Service Tests Create", sourceBackgroundInstitutionTestScore.id
        assertNotNull "SourceBackgroundInstitutionTestScore sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionTestScore Service Tests", sourceBackgroundInstitutionTestScore.sourceAndBackgroundInstitution
        assertNotNull "SourceBackgroundInstitutionTestScore meanTestScore is null in SourceBackgroundInstitutionTestScore Service Tests", sourceBackgroundInstitutionTestScore.meanTestScore
        assertNotNull "SourceBackgroundInstitutionTestScore testScore is null in SourceBackgroundInstitutionTestScore Service Tests", sourceBackgroundInstitutionTestScore.testScore
        assertNotNull sourceBackgroundInstitutionTestScore.version
        assertNotNull sourceBackgroundInstitutionTestScore.dataOrigin
        assertNotNull sourceBackgroundInstitutionTestScore.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionTestScore.lastModified
    }


    @Test
    void testSourceBackgroundInstitutionTestScoreInvalidCreate() {
        def sourceBackgroundInstitutionTestScore = newInvalidForCreateSourceBackgroundInstitutionTestScore()
        def map = [domainModel: sourceBackgroundInstitutionTestScore]
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionTestScoreService.create(map)
        }
    }


    @Test
    void testSourceBackgroundInstitutionTestScoreValidUpdate() {
        def sourceBackgroundInstitutionTestScore = newValidForCreateSourceBackgroundInstitutionTestScore()
        def map = [domainModel: sourceBackgroundInstitutionTestScore]
        sourceBackgroundInstitutionTestScore = sourceBackgroundInstitutionTestScoreService.create(map)
        assertNotNull "SourceBackgroundInstitutionTestScore ID is null in SourceBackgroundInstitutionTestScore Service Tests Create", sourceBackgroundInstitutionTestScore.id
        assertNotNull "SourceBackgroundInstitutionTestScore sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionTestScore Service Tests", sourceBackgroundInstitutionTestScore.sourceAndBackgroundInstitution
        assertNotNull "SourceBackgroundInstitutionTestScore meanTestScore is null in SourceBackgroundInstitutionTestScore Service Tests", sourceBackgroundInstitutionTestScore.meanTestScore
        assertNotNull sourceBackgroundInstitutionTestScore.version
        assertNotNull sourceBackgroundInstitutionTestScore.dataOrigin
        assertNotNull sourceBackgroundInstitutionTestScore.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionTestScore.lastModified

        //Update the entity with new values
        sourceBackgroundInstitutionTestScore.meanTestScore = "B"
        map.domainModel = sourceBackgroundInstitutionTestScore
        sourceBackgroundInstitutionTestScore = sourceBackgroundInstitutionTestScoreService.update(map)

        // test the values
        assertEquals "B", sourceBackgroundInstitutionTestScore.meanTestScore
    }


    @Test
    void testSourceBackgroundInstitutionTestScoreInvalidUpdate() {
        def sourceBackgroundInstitutionTestScore = newValidForCreateSourceBackgroundInstitutionTestScore()
        def map = [domainModel: sourceBackgroundInstitutionTestScore]
        sourceBackgroundInstitutionTestScore = sourceBackgroundInstitutionTestScoreService.create(map)
        assertNotNull "SourceBackgroundInstitutionTestScore ID is null in SourceBackgroundInstitutionTestScore Service Tests Create", sourceBackgroundInstitutionTestScore.id
        assertNotNull "SourceBackgroundInstitutionTestScore sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionTestScore Service Tests", sourceBackgroundInstitutionTestScore.sourceAndBackgroundInstitution
        assertNotNull "SourceBackgroundInstitutionTestScore meanTestScore is null in SourceBackgroundInstitutionTestScore Service Tests", sourceBackgroundInstitutionTestScore.meanTestScore
        assertNotNull sourceBackgroundInstitutionTestScore.version
        assertNotNull sourceBackgroundInstitutionTestScore.dataOrigin
        assertNotNull sourceBackgroundInstitutionTestScore.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionTestScore.lastModified

        //Update the entity with new invalid values
        sourceBackgroundInstitutionTestScore.meanTestScore = "123456"
        map.domainModel = sourceBackgroundInstitutionTestScore
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionTestScore = sourceBackgroundInstitutionTestScoreService.update(map)
        }
    }


    @Test
    void testSourceBackgroundInstitutionTestScoreDelete() {
        def sourceBackgroundInstitutionTestScore = newValidForCreateSourceBackgroundInstitutionTestScore()
        def map = [domainModel: sourceBackgroundInstitutionTestScore]
        sourceBackgroundInstitutionTestScore = sourceBackgroundInstitutionTestScoreService.create(map)
        assertNotNull "SourceBackgroundInstitutionTestScore ID is null in SourceBackgroundInstitutionTestScore Service Tests Create", sourceBackgroundInstitutionTestScore.id
        def id = sourceBackgroundInstitutionTestScore.id
        sourceBackgroundInstitutionTestScore.delete(map)
        assertNull "SourceBackgroundInstitutionTestScore should have been deleted", sourceBackgroundInstitutionTestScore.get(id)
    }


    @Test
    void testReadOnly() {
        def sourceBackgroundInstitutionTestScore = newValidForCreateSourceBackgroundInstitutionTestScore()
        def map = [domainModel: sourceBackgroundInstitutionTestScore]
        sourceBackgroundInstitutionTestScore = sourceBackgroundInstitutionTestScoreService.create(map)
        assertNotNull "SourceBackgroundInstitutionTestScore ID is null in SourceBackgroundInstitutionTestScore Service Tests Create", sourceBackgroundInstitutionTestScore.id

        sourceBackgroundInstitutionTestScore.sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findWhere(code: "5815")
        sourceBackgroundInstitutionTestScore.testScore = TestScore.findWhere(code: "IT")
        sourceBackgroundInstitutionTestScore.demographicYear = 2013
        try {
            sourceBackgroundInstitutionTestScoreService.update([domainModel: sourceBackgroundInstitutionTestScore])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    private def newValidForCreateSourceBackgroundInstitutionTestScore() {
        def sourceBackgroundInstitutionTestScore = new SourceBackgroundInstitutionTestScore(
                demographicYear: 2014,
                meanTestScore: 100,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "443361"),
                testScore: TestScore.findWhere(code: "JL"),
        )
        return sourceBackgroundInstitutionTestScore
    }


    private def newInvalidForCreateSourceBackgroundInstitutionTestScore() {
        def sourceBackgroundInstitutionTestScore = new SourceBackgroundInstitutionTestScore(
                demographicYear: null,
                meanTestScore: null,
                sourceAndBackgroundInstitution: null,
                testScore: null,
        )
        return sourceBackgroundInstitutionTestScore
    }
}

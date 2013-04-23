package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.County
import net.hedtech.banner.general.system.Nation
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.general.system.State
import net.hedtech.banner.testing.BaseIntegrationTestCase

class SourceBackgroundInstitutionBaseCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testSourceBackgroundInstitutionBaseCompositeDelete() {

    }


    private def newValidForCreateSourceBackgroundInstitutionBase() {
        def sourceBackgroundInstitutionBase = new SourceBackgroundInstitutionBase(
                streetLine1: "123456789012345678901234567890123456789012345678901234567890123456789012345",
                streetLine2: "123456789012345678901234567890123456789012345678901234567890123456789012345",
                streetLine3: "123456789012345678901234567890123456789012345678901234567890123456789012345",
                city: "12345678901234567890123456789012345678901234567890",
                zip: "123456789012345678901234567890",
                houseNumber: "1234567890",
                streetLine4: "123456789012345678901234567890123456789012345678901234567890123456789012345",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
                state: State.findWhere(code: "PA"),
                county: County.findWhere(code: "001"),
                nation: Nation.findWhere(code: "1"),
        )
        return sourceBackgroundInstitutionBase
    }


    private def newValidForCreateSourceBackgroundInstitutionComment() {
        def sourceBackgroundInstitutionComment = new SourceBackgroundInstitutionComment(
                sequenceNumber: SourceBackgroundInstitutionComment.fetchNextSequenceNumber(),
                commentData: "1234567890..1234567890",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
        )
        return sourceBackgroundInstitutionComment
    }


    private def newInvalidForCreateSourceBackgroundInstitutionComment() {
        def sourceBackgroundInstitutionComment = new SourceBackgroundInstitutionComment(
                sequenceNumber: SourceBackgroundInstitutionComment.fetchNextSequenceNumber(),
                commentData: "1234567890..1234567890",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "000000"), // Not in SourceBackgroundInstitutionBase
        )
        return sourceBackgroundInstitutionComment
    }
}

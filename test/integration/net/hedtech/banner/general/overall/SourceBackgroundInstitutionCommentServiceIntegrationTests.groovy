/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase

class SourceBackgroundInstitutionCommentServiceIntegrationTests extends BaseIntegrationTestCase {

    def sourceBackgroundInstitutionCommentService


    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testSourceBackgroundInstitutionCommentValidCreate() {
        SourceBackgroundInstitutionComment sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        def map = [domainModel: sourceBackgroundInstitutionComment]
        sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.create(map)

        assertNotNull "SourceBackgroundInstitutionComment ID is null in SourceBackgroundInstitutionComment Service Tests Create",
                sourceBackgroundInstitutionComment.id
        assertNotNull "SourceBackgroundInstitutionComment sourceAndBackgroundInstitution is null in SourceBackgroundInstitutionComment Service Tests",
                sourceBackgroundInstitutionComment.sourceAndBackgroundInstitution
        assertNotNull sourceBackgroundInstitutionComment.version
        assertNotNull sourceBackgroundInstitutionComment.dataOrigin
        assertNotNull sourceBackgroundInstitutionComment.lastModifiedBy
        assertNotNull sourceBackgroundInstitutionComment.lastModified
        assertNotNull sourceBackgroundInstitutionComment.sequenceNumber
    }


    void testSourceBackgroundInstitutionCommentInvalidCreate() {
        SourceBackgroundInstitutionComment sourceBackgroundInstitutionComment = newInvalidForCreateSourceBackgroundInstitutionComment()
        def map = [domainModel: sourceBackgroundInstitutionComment]
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionCommentService.create(map)
        }
    }


    void testSourceBackgroundInstitutionCommentValidUpdate() {
        SourceBackgroundInstitutionComment sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        def map = [domainModel: sourceBackgroundInstitutionComment]
        sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.create(map)

        sourceBackgroundInstitutionComment.commentData = "1234567890..1234567890UPDATE"
        map.domainModel = sourceBackgroundInstitutionComment
        sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.update(map)

        assertEquals "1234567890..1234567890UPDATE", sourceBackgroundInstitutionComment.commentData
    }


    void testSourceBackgroundInstitutionCommentInvalidUpdate() {
        SourceBackgroundInstitutionComment sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        def map = [domainModel: sourceBackgroundInstitutionComment]
        sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.create(map)

        sourceBackgroundInstitutionComment.sequenceNumber = null
        map.domainModel = sourceBackgroundInstitutionComment
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.update(map)
        }
    }


    void testSourceBackgroundInstitutionCommentDelete() {
        SourceBackgroundInstitutionComment sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        def map = [domainModel: sourceBackgroundInstitutionComment]
        sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.create(map)
        assertNotNull "SourceBackgroundInstitutionComment ID is null in SourceBackgroundInstitutionComment Service Tests Create", sourceBackgroundInstitutionComment.id

        def id = sourceBackgroundInstitutionComment.id
        map.domainModel = sourceBackgroundInstitutionComment
        sourceBackgroundInstitutionCommentService.delete(map)
        assertNull "SourceBackgroundInstitutionComment should have been deleted", sourceBackgroundInstitutionComment.get(id)
    }


    private def newValidForCreateSourceBackgroundInstitutionComment() {
        def sourceBackgroundInstitutionComment = new SourceBackgroundInstitutionComment(
                sequenceNumber: null,
                commentData: "1234567890..1234567890",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "443361")
        )
        return sourceBackgroundInstitutionComment
    }


    private def newInvalidForCreateSourceBackgroundInstitutionComment() {
        def sourceBackgroundInstitutionComment = new SourceBackgroundInstitutionComment(
                sequenceNumber: null,
                commentData: "1234567890..1234567890",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "000000") // Not in SourceBackgroundInstitutionBase
        )
        return sourceBackgroundInstitutionComment
    }
}

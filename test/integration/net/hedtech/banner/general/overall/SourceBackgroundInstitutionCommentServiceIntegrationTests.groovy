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
        sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.create([domainModel: sourceBackgroundInstitutionComment])

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
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionCommentService.create([domainModel: sourceBackgroundInstitutionComment])
        }
    }


    void testSourceBackgroundInstitutionCommentValidUpdate() {
        SourceBackgroundInstitutionComment sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.create([domainModel: sourceBackgroundInstitutionComment])

        sourceBackgroundInstitutionComment.commentData = "1234567890..1234567890UPDATE"
        sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.update([domainModel: sourceBackgroundInstitutionComment])

        assertEquals "1234567890..1234567890UPDATE", sourceBackgroundInstitutionComment.commentData
    }


    void testSourceBackgroundInstitutionCommentInvalidUpdate() {
        SourceBackgroundInstitutionComment sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.create([domainModel: sourceBackgroundInstitutionComment])

        sourceBackgroundInstitutionComment.sequenceNumber = null
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.update([domainModel: sourceBackgroundInstitutionComment])
        }
    }


    void testSourceBackgroundInstitutionCommentDelete() {
        SourceBackgroundInstitutionComment sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.create([domainModel: sourceBackgroundInstitutionComment])
        assertNotNull "SourceBackgroundInstitutionComment ID is null in SourceBackgroundInstitutionComment Service Tests Create", sourceBackgroundInstitutionComment.id

        def id = sourceBackgroundInstitutionComment.id
        sourceBackgroundInstitutionCommentService.delete([domainModel: sourceBackgroundInstitutionComment])
        assertNull "SourceBackgroundInstitutionComment should have been deleted", sourceBackgroundInstitutionComment.get(id)
    }


    private def newValidForCreateSourceBackgroundInstitutionComment() {
        def sourceBackgroundInstitutionComment = new SourceBackgroundInstitutionComment(
                sequenceNumber: null,
                commentData: "1234567890..1234567890",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999")
        )
        return sourceBackgroundInstitutionComment
    }


    private def newInvalidForCreateSourceBackgroundInstitutionComment() {
        def sourceBackgroundInstitutionComment = new SourceBackgroundInstitutionComment(
                sequenceNumber: null,
                commentData: "1234567890..1234567890",
                sourceAndBackgroundInstitution: null
        )
        return sourceBackgroundInstitutionComment
    }
}

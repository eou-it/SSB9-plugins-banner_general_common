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

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase

@Integration
@Rollback
class SourceBackgroundInstitutionCommentServiceIntegrationTests extends BaseIntegrationTestCase {

    def sourceBackgroundInstitutionCommentService


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


    @Test
    void testSourceBackgroundInstitutionCommentInvalidCreate() {
        SourceBackgroundInstitutionComment sourceBackgroundInstitutionComment = newInvalidForCreateSourceBackgroundInstitutionComment()
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionCommentService.create([domainModel: sourceBackgroundInstitutionComment])
        }
    }


    @Test
    void testSourceBackgroundInstitutionCommentValidUpdate() {
        SourceBackgroundInstitutionComment sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.create([domainModel: sourceBackgroundInstitutionComment])

        sourceBackgroundInstitutionComment.commentData = "1234567890..1234567890UPDATE"
        sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.update([domainModel: sourceBackgroundInstitutionComment])

        assertEquals "1234567890..1234567890UPDATE", sourceBackgroundInstitutionComment.commentData
    }


    @Test
    void testSourceBackgroundInstitutionCommentInvalidUpdate() {
        SourceBackgroundInstitutionComment sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.create([domainModel: sourceBackgroundInstitutionComment])

        sourceBackgroundInstitutionComment.sequenceNumber = null
        shouldFail(ApplicationException) {
            sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.update([domainModel: sourceBackgroundInstitutionComment])
        }
    }


    @Test
    void testSourceBackgroundInstitutionCommentDelete() {
        SourceBackgroundInstitutionComment sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.create([domainModel: sourceBackgroundInstitutionComment])
        assertNotNull "SourceBackgroundInstitutionComment ID is null in SourceBackgroundInstitutionComment Service Tests Create", sourceBackgroundInstitutionComment.id

        def id = sourceBackgroundInstitutionComment.id
        sourceBackgroundInstitutionCommentService.delete([domainModel: sourceBackgroundInstitutionComment])
        assertNull "SourceBackgroundInstitutionComment should have been deleted", sourceBackgroundInstitutionComment.get(id)
    }


    @Test
    void testReadOnly() {
        def sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        def map = [domainModel: sourceBackgroundInstitutionComment]
        sourceBackgroundInstitutionComment = sourceBackgroundInstitutionCommentService.create(map)
        assertNotNull "SourceBackgroundInstitutionComment ID is null in SourceBackgroundInstitutionComment Service Tests Create", sourceBackgroundInstitutionComment.id

        sourceBackgroundInstitutionComment.sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findWhere(code: "5815")
        sourceBackgroundInstitutionComment.sequenceNumber = 99
        try {
            sourceBackgroundInstitutionCommentService.update([domainModel: sourceBackgroundInstitutionComment])
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
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
                sourceAndBackgroundInstitution: null
        )
        return sourceBackgroundInstitutionComment
    }
}

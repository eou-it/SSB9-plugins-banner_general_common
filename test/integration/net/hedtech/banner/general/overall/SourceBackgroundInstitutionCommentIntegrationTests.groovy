/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

import java.text.SimpleDateFormat

class SourceBackgroundInstitutionCommentIntegrationTests extends BaseIntegrationTestCase {

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateValidSourceBackgroundInstitutionComment() {
        def sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        sourceBackgroundInstitutionComment.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sourceBackgroundInstitutionComment.id
    }


    void testCreateInvalidSourceBackgroundInstitutionComment() {
        def sourceBackgroundInstitutionComment = newInvalidForCreateSourceBackgroundInstitutionComment()
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionComment.save(failOnError: true, flush: true)
        }
    }


    void testUpdateValidSourceBackgroundInstitutionComment() {
        def sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        sourceBackgroundInstitutionComment.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionComment.id
        assertEquals 0L, sourceBackgroundInstitutionComment.version
        assertEquals 1, sourceBackgroundInstitutionComment.sequenceNumber
        assertEquals "1234567890..1234567890", sourceBackgroundInstitutionComment.commentData

        //Update the entity
        sourceBackgroundInstitutionComment.commentData = "1234567890..1234567890UPDATE"
        sourceBackgroundInstitutionComment.save(failOnError: true, flush: true)

        //Assert for sucessful update
        sourceBackgroundInstitutionComment = SourceBackgroundInstitutionComment.get(sourceBackgroundInstitutionComment.id)
        assertEquals 1L, sourceBackgroundInstitutionComment?.version
        assertEquals "1234567890..1234567890UPDATE", sourceBackgroundInstitutionComment.commentData

    }


    void testUpdateInvalidSourceBackgroundInstitutionComment() {
        def sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        sourceBackgroundInstitutionComment.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionComment.id
        assertEquals 0L, sourceBackgroundInstitutionComment.version
        assertEquals 1, sourceBackgroundInstitutionComment.sequenceNumber
        assertEquals "1234567890..1234567890", sourceBackgroundInstitutionComment.commentData

        //Update the entity with invalid values
        sourceBackgroundInstitutionComment.sequenceNumber = null
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionComment.save(failOnError: true, flush: true)
        }
    }


    void testDates() {
        def time = new SimpleDateFormat('HHmmss')
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        sourceBackgroundInstitutionComment.save(flush: true, failOnError: true)
        sourceBackgroundInstitutionComment.refresh()
        assertNotNull "SourceBackgroundInstitutionComment should have been saved", sourceBackgroundInstitutionComment.id

        // test date values -
        assertEquals date.format(today), date.format(sourceBackgroundInstitutionComment.lastModified)
        assertEquals hour.format(today), hour.format(sourceBackgroundInstitutionComment.lastModified)
    }


    void testOptimisticLock() {
        def sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        sourceBackgroundInstitutionComment.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update SORBCMT set SORBCMT_VERSION = 999 where SORBCMT_SURROGATE_ID = ?", [sourceBackgroundInstitutionComment.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        sourceBackgroundInstitutionComment.commentData = "1234567890..1234567890UPDATE"
        shouldFail(HibernateOptimisticLockingFailureException) {
            sourceBackgroundInstitutionComment.save(failOnError: true, flush: true)
        }
    }


    void testDeleteSourceBackgroundInstitutionComment() {
        def sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        sourceBackgroundInstitutionComment.save(failOnError: true, flush: true)
        def id = sourceBackgroundInstitutionComment.id
        assertNotNull id
        sourceBackgroundInstitutionComment.delete()
        assertNull SourceBackgroundInstitutionComment.get(id)
    }


    void testValidation() {
        def sourceBackgroundInstitutionComment = newInvalidForCreateSourceBackgroundInstitutionComment()
        assertFalse "SourceBackgroundInstitutionComment could not be validated as expected due to ${sourceBackgroundInstitutionComment.errors}", sourceBackgroundInstitutionComment.validate()
    }


    void testNullValidationFailure() {
        def sourceBackgroundInstitutionComment = new SourceBackgroundInstitutionComment()
        assertFalse "SourceBackgroundInstitutionComment should have failed validation", sourceBackgroundInstitutionComment.validate()
        assertErrorsFor sourceBackgroundInstitutionComment, 'nullable', ['sequenceNumber', 'sourceAndBackgroundInstitution']
        assertNoErrorsFor sourceBackgroundInstitutionComment,['commentData']
    }


    void testMaxSizeValidationFailures() {
        def sourceBackgroundInstitutionComment = new SourceBackgroundInstitutionComment(
                commentData: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX')
        assertFalse "SourceBackgroundInstitutionComment should have failed validation", sourceBackgroundInstitutionComment.validate()
        assertErrorsFor sourceBackgroundInstitutionComment, 'maxSize', ['commentData']
    }


    private def newValidForCreateSourceBackgroundInstitutionComment() {
        def sourceBackgroundInstitutionComment = new SourceBackgroundInstitutionComment(
                sequenceNumber: SourceBackgroundInstitutionComment.fetchNextSequenceNumber(),
                commentData: "1234567890..1234567890",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "000000"),
        )
        return sourceBackgroundInstitutionComment
    }


    private def newInvalidForCreateSourceBackgroundInstitutionComment() {
        def sourceBackgroundInstitutionComment = new SourceBackgroundInstitutionComment(
                sequenceNumber: null,
                commentData: null,
                sourceAndBackgroundInstitution: null,
        )
        return sourceBackgroundInstitutionComment
    }
}

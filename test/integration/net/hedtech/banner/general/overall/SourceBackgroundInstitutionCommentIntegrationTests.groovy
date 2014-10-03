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

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

import java.text.SimpleDateFormat

class SourceBackgroundInstitutionCommentIntegrationTests extends BaseIntegrationTestCase {

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
    void testCreateValidSourceBackgroundInstitutionComment() {
        def sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        sourceBackgroundInstitutionComment.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sourceBackgroundInstitutionComment.id
    }


	@Test
    void testCreateInvalidSourceBackgroundInstitutionComment() {
        def sourceBackgroundInstitutionComment = newInvalidForCreateSourceBackgroundInstitutionComment()
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionComment.save(failOnError: true, flush: true)
        }
    }


	@Test
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


	@Test
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


	@Test
    void testDates() {
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


	@Test
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


	@Test
    void testDeleteSourceBackgroundInstitutionComment() {
        def sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        sourceBackgroundInstitutionComment.save(failOnError: true, flush: true)
        def id = sourceBackgroundInstitutionComment.id
        assertNotNull id
        sourceBackgroundInstitutionComment.delete()
        assertNull SourceBackgroundInstitutionComment.get(id)
    }


	@Test
    void testValidation() {
        def sourceBackgroundInstitutionComment = newInvalidForCreateSourceBackgroundInstitutionComment()
        assertFalse "SourceBackgroundInstitutionComment could not be validated as expected due to ${sourceBackgroundInstitutionComment.errors}", sourceBackgroundInstitutionComment.validate()
    }


	@Test
    void testNullValidationFailure() {
        def sourceBackgroundInstitutionComment = new SourceBackgroundInstitutionComment()
        assertFalse "SourceBackgroundInstitutionComment should have failed validation", sourceBackgroundInstitutionComment.validate()
        assertErrorsFor sourceBackgroundInstitutionComment, 'nullable', ['sequenceNumber', 'sourceAndBackgroundInstitution']
        assertNoErrorsFor sourceBackgroundInstitutionComment, ['commentData']
    }


	@Test
    void testMaxSizeValidationFailures() {
        def sourceBackgroundInstitutionComment = new SourceBackgroundInstitutionComment(
                commentData: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX')
        assertFalse "SourceBackgroundInstitutionComment should have failed validation", sourceBackgroundInstitutionComment.validate()
        assertErrorsFor sourceBackgroundInstitutionComment, 'maxSize', ['commentData']
    }


    private def doCreateComments() {
        // Create 3 comments
        def sourceBackgroundInstitutionComment = newValidForCreateSourceBackgroundInstitutionComment()
        sourceBackgroundInstitutionComment.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionComment.id
        assertEquals 0L, sourceBackgroundInstitutionComment.version

        def sourceAndBackgroundInstitution = sourceBackgroundInstitutionComment.sourceAndBackgroundInstitution

        sourceBackgroundInstitutionComment = new SourceBackgroundInstitutionComment(
                sequenceNumber: SourceBackgroundInstitutionComment.fetchNextSequenceNumber(sourceAndBackgroundInstitution),
                commentData: "Test comment two",
                sourceAndBackgroundInstitution: sourceAndBackgroundInstitution,
        )
        sourceBackgroundInstitutionComment.save(failOnError: true, flush: true)

        sourceBackgroundInstitutionComment = new SourceBackgroundInstitutionComment(
                sequenceNumber: SourceBackgroundInstitutionComment.fetchNextSequenceNumber(sourceAndBackgroundInstitution),
                commentData: "Test comment three",
                sourceAndBackgroundInstitution: sourceAndBackgroundInstitution,
        )
        sourceBackgroundInstitutionComment.save(failOnError: true, flush: true)

        return sourceAndBackgroundInstitution
    }


	@Test
    void testFetchSearch() {
        def sourceAndBackgroundInstitution = doCreateComments()

        def pagingAndSortParams = [sortColumn: "commentData", sortDirection: "asc", max: 5, offset: 0]
        Map paramsMap = [sourceAndBackgroundInstitutionCode: sourceAndBackgroundInstitution.code, commentData: "%comment%"]
        def criteriaMap = [[key: "commentData", binding: "commentData", operator: "contains"]]
        def filterData = [params: paramsMap, criteria: criteriaMap]

        def records = SourceBackgroundInstitutionComment.fetchSearch(filterData, pagingAndSortParams)
        assertTrue records.size() == 2
        records.each { record ->
            assertTrue record.commentData.indexOf("comment") >= 0 // -1 is a failed search
        }
    }


	@Test
    void testFetchBySourceAndBackgroundInstitution() {
        def sourceAndBackgroundInstitution = doCreateComments()

        def records = SourceBackgroundInstitutionComment.fetchBySourceAndBackgroundInstitution(sourceAndBackgroundInstitution)
        assertTrue records.size() == 3
        records.each { record ->
            assertEquals sourceAndBackgroundInstitution.code,  record.sourceAndBackgroundInstitution.code
        }
    }


    private def newValidForCreateSourceBackgroundInstitutionComment() {
        def sourceAndBackgroundInstitution =  SourceAndBackgroundInstitution.findWhere(code: "999999")
        def sourceBackgroundInstitutionComment = new SourceBackgroundInstitutionComment(
                sequenceNumber: SourceBackgroundInstitutionComment.fetchNextSequenceNumber(sourceAndBackgroundInstitution),
                commentData: "1234567890..1234567890",
                sourceAndBackgroundInstitution: sourceAndBackgroundInstitution,
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

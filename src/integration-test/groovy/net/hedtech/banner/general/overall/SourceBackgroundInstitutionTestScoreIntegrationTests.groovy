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
import net.hedtech.banner.general.system.TestScore
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

import java.text.SimpleDateFormat

class SourceBackgroundInstitutionTestScoreIntegrationTests extends BaseIntegrationTestCase {

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
    void testCreateValidSourceBackgroundInstitutionTestScore() {
        def sourceBackgroundInstitutionTestScore = newValidForCreateSourceBackgroundInstitutionTestScore()
        sourceBackgroundInstitutionTestScore.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sourceBackgroundInstitutionTestScore.id
    }


    @Test
    void testCreateInvalidSourceBackgroundInstitutionTestScore() {
        def sourceBackgroundInstitutionTestScore = newInvalidForCreateSourceBackgroundInstitutionTestScore()
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionTestScore.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidSourceBackgroundInstitutionTestScore() {
        def sourceBackgroundInstitutionTestScore = newValidForCreateSourceBackgroundInstitutionTestScore()
        sourceBackgroundInstitutionTestScore.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionTestScore.id
        assertEquals 0L, sourceBackgroundInstitutionTestScore.version
        assertEquals 2014, sourceBackgroundInstitutionTestScore.demographicYear
        assertEquals "A", sourceBackgroundInstitutionTestScore.meanTestScore
        assertEquals "JL", sourceBackgroundInstitutionTestScore.testScore.code

        //Update the entity
        sourceBackgroundInstitutionTestScore.meanTestScore = "B"
        sourceBackgroundInstitutionTestScore.save(failOnError: true, flush: true)

        //Assert for sucessful update
        sourceBackgroundInstitutionTestScore = SourceBackgroundInstitutionTestScore.get(sourceBackgroundInstitutionTestScore.id)
        assertEquals 1L, sourceBackgroundInstitutionTestScore?.version
        assertEquals "B", sourceBackgroundInstitutionTestScore.meanTestScore
    }


    @Test
    void testUpdateInvalidSourceBackgroundInstitutionTestScore() {
        def sourceBackgroundInstitutionTestScore = newValidForCreateSourceBackgroundInstitutionTestScore()
        sourceBackgroundInstitutionTestScore.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionTestScore.id
        assertEquals 0L, sourceBackgroundInstitutionTestScore.version
        assertEquals 2014, sourceBackgroundInstitutionTestScore.demographicYear
        assertEquals "A", sourceBackgroundInstitutionTestScore.meanTestScore
        assertEquals "JL", sourceBackgroundInstitutionTestScore.testScore.code

        //Update the entity with invalid values
        sourceBackgroundInstitutionTestScore.meanTestScore = "1234567890123456"
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionTestScore.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDates() {
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def sourceBackgroundInstitutionTestScore = newValidForCreateSourceBackgroundInstitutionTestScore()
        sourceBackgroundInstitutionTestScore.save(flush: true, failOnError: true)
        sourceBackgroundInstitutionTestScore.refresh()
        assertNotNull "SourceBackgroundInstitutionTestScore should have been saved", sourceBackgroundInstitutionTestScore.id

        // test date values -
        assertEquals date.format(today), date.format(sourceBackgroundInstitutionTestScore.lastModified)
        assertEquals hour.format(today), hour.format(sourceBackgroundInstitutionTestScore.lastModified)
    }


    @Test
    void testOptimisticLock() {
        def sourceBackgroundInstitutionTestScore = newValidForCreateSourceBackgroundInstitutionTestScore()
        sourceBackgroundInstitutionTestScore.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update SORBTST set SORBTST_VERSION = 999 where SORBTST_SURROGATE_ID = ?", [sourceBackgroundInstitutionTestScore.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        sourceBackgroundInstitutionTestScore.meanTestScore = "B"
        shouldFail(HibernateOptimisticLockingFailureException) {
            sourceBackgroundInstitutionTestScore.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteSourceBackgroundInstitutionTestScore() {
        def sourceBackgroundInstitutionTestScore = newValidForCreateSourceBackgroundInstitutionTestScore()
        sourceBackgroundInstitutionTestScore.save(failOnError: true, flush: true)
        def id = sourceBackgroundInstitutionTestScore.id
        assertNotNull id
        sourceBackgroundInstitutionTestScore.delete()
        assertNull sourceBackgroundInstitutionTestScore.get(id)
    }


    @Test
    void testValidation() {
        def sourceBackgroundInstitutionTestScore = newInvalidForCreateSourceBackgroundInstitutionTestScore()
        assertFalse "SourceBackgroundInstitutionTestScore could not be validated as expected due to ${sourceBackgroundInstitutionTestScore.errors}", sourceBackgroundInstitutionTestScore.validate()
    }


    @Test
    void testNullValidationFailure() {
        def sourceBackgroundInstitutionTestScore = new SourceBackgroundInstitutionTestScore()
        assertFalse "SourceBackgroundInstitutionTestScore should have failed validation", sourceBackgroundInstitutionTestScore.validate()
        assertErrorsFor sourceBackgroundInstitutionTestScore, 'nullable',
                [
                        'demographicYear',
                        'testScore',
                        'sourceAndBackgroundInstitution',
                        'testScore'
                ]
    }


    @Test
    void testFetchSearch() {
        // Create 2 tests
        def sourceBackgroundInstitutionTestScore = newValidForCreateSourceBackgroundInstitutionTestScore()
        sourceBackgroundInstitutionTestScore.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionTestScore.id
        assertEquals 0L, sourceBackgroundInstitutionTestScore.version

        def sourceAndBackgroundInstitution = sourceBackgroundInstitutionTestScore.sourceAndBackgroundInstitution

        sourceBackgroundInstitutionTestScore = new SourceBackgroundInstitutionTestScore(
                demographicYear: 2013,
                meanTestScore: "A",
                sourceAndBackgroundInstitution: sourceAndBackgroundInstitution,
                testScore: TestScore.findWhere(code: "JL"),
        )
        sourceBackgroundInstitutionTestScore.save(failOnError: true, flush: true)

        def pagingAndSortParams = [sortColumn: "demographicYear", sortDirection: "asc", max: 5, offset: 0]
        Map paramsMap = [sourceAndBackgroundInstitutionCode: sourceAndBackgroundInstitution.code, demographicYear: 2013]
        def criteriaMap = [[key: "demographicYear", binding: "demographicYear", operator: "equals"]]
        def filterData = [params: paramsMap, criteria: criteriaMap]

        def records = SourceBackgroundInstitutionTestScore.fetchSearch(filterData, pagingAndSortParams)
        assertTrue records.size() == 1
        records.each { record ->
            assertTrue record.demographicYear == 2013
        }
    }


    private def newValidForCreateSourceBackgroundInstitutionTestScore() {
        def sourceBackgroundInstitutionTestScore = new SourceBackgroundInstitutionTestScore(
                demographicYear: 2014,
                meanTestScore: "A",
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
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

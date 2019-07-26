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
import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.system.Ethnicity
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException

import java.text.SimpleDateFormat

@Integration
@Rollback
class SourceBackgroundInstitutionEthnicMakeUpIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateValidSourceBackgroundInstitutionEthnicMakeUp() {
        def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
        sourceBackgroundInstitutionEthnicMakeUp.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.id
    }


    @Test
    void testCreateInvalidSourceBackgroundInstitutionEthnicMakeUp() {
        def sourceBackgroundInstitutionEthnicMakeUp = newInvalidForCreateSourceBackgroundInstitutionEthnicMakeUp()
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionEthnicMakeUp.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateValidSourceBackgroundInstitutionEthnicMakeUp() {
        def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
        sourceBackgroundInstitutionEthnicMakeUp.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.id
        assertEquals 0L, sourceBackgroundInstitutionEthnicMakeUp.version
        assertEquals 2014, sourceBackgroundInstitutionEthnicMakeUp.demographicYear
        assertEquals 50, sourceBackgroundInstitutionEthnicMakeUp.ethnicPercent

        //Update the entity
        sourceBackgroundInstitutionEthnicMakeUp.ethnicPercent = 51
        sourceBackgroundInstitutionEthnicMakeUp.save(failOnError: true, flush: true)

        //Assert for sucessful update
        sourceBackgroundInstitutionEthnicMakeUp = SourceBackgroundInstitutionEthnicMakeUp.get(sourceBackgroundInstitutionEthnicMakeUp.id)
        assertEquals 1L, sourceBackgroundInstitutionEthnicMakeUp?.version
        assertEquals 51, sourceBackgroundInstitutionEthnicMakeUp.ethnicPercent
    }


    @Test
    void testUpdateInvalidSourceBackgroundInstitutionEthnicMakeUp() {
        def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
        sourceBackgroundInstitutionEthnicMakeUp.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.id
        assertEquals 0L, sourceBackgroundInstitutionEthnicMakeUp.version
        assertEquals 2014, sourceBackgroundInstitutionEthnicMakeUp.demographicYear
        assertEquals 50, sourceBackgroundInstitutionEthnicMakeUp.ethnicPercent

        //Update the entity with invalid values
        sourceBackgroundInstitutionEthnicMakeUp.ethnicPercent = 1000
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionEthnicMakeUp.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDates() {
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
        sourceBackgroundInstitutionEthnicMakeUp.save(flush: true, failOnError: true)
        sourceBackgroundInstitutionEthnicMakeUp.refresh()
        assertNotNull "SourceBackgroundInstitutionEthnicMakeUp should have been saved", sourceBackgroundInstitutionEthnicMakeUp.id

        // test date values -
        assertEquals date.format(today), date.format(sourceBackgroundInstitutionEthnicMakeUp.lastModified)
        assertEquals hour.format(today), hour.format(sourceBackgroundInstitutionEthnicMakeUp.lastModified)
    }


    @Test
    void testOptimisticLock() {
        def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
        sourceBackgroundInstitutionEthnicMakeUp.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update SORBETH set SORBETH_VERSION = 999 where SORBETH_SURROGATE_ID = ?", [sourceBackgroundInstitutionEthnicMakeUp.id])
        } finally {
//            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        sourceBackgroundInstitutionEthnicMakeUp.ethnicPercent = 51
        shouldFail(HibernateOptimisticLockingFailureException) {
            sourceBackgroundInstitutionEthnicMakeUp.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteSourceBackgroundInstitutionEthnicMakeUp() {
        def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
        sourceBackgroundInstitutionEthnicMakeUp.save(failOnError: true, flush: true)
        def id = sourceBackgroundInstitutionEthnicMakeUp.id
        assertNotNull id
        sourceBackgroundInstitutionEthnicMakeUp.delete()
        assertNull SourceBackgroundInstitutionEthnicMakeUp.get(id)
    }


    @Test
    void testValidation() {
        def sourceBackgroundInstitutionEthnicMakeUp = newInvalidForCreateSourceBackgroundInstitutionEthnicMakeUp()
        assertFalse "SourceBackgroundInstitutionEthnicMakeUp could not be validated as expected due to ${sourceBackgroundInstitutionEthnicMakeUp.errors}", sourceBackgroundInstitutionEthnicMakeUp.validate()
    }


    @Test
    void testNullValidationFailure() {
        def sourceBackgroundInstitutionEthnicMakeUp = new SourceBackgroundInstitutionEthnicMakeUp()
        assertFalse "SourceBackgroundInstitutionEthnicMakeUp should have failed validation", sourceBackgroundInstitutionEthnicMakeUp.validate()
        assertErrorsFor sourceBackgroundInstitutionEthnicMakeUp, 'nullable',
                [
                        'demographicYear',
                        'sourceAndBackgroundInstitution',
                        'ethnicity'
                ]
        assertNoErrorsFor sourceBackgroundInstitutionEthnicMakeUp,
                [
                        'ethnicPercent'
                ]
    }


    @Test
    void testFetchSearch() {
        // Create 2 ethnics
        def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
        sourceBackgroundInstitutionEthnicMakeUp.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.id
        assertEquals 0L, sourceBackgroundInstitutionEthnicMakeUp.version

        def sourceAndBackgroundInstitution = sourceBackgroundInstitutionEthnicMakeUp.sourceAndBackgroundInstitution

        sourceBackgroundInstitutionEthnicMakeUp = new SourceBackgroundInstitutionEthnicMakeUp(
                demographicYear: 2013,
                sourceAndBackgroundInstitution: sourceAndBackgroundInstitution,
                ethnicity: Ethnicity.findWhere(code: "1"),
        )
        sourceBackgroundInstitutionEthnicMakeUp.save(failOnError: true, flush: true)

        def pagingAndSortParams = [sortColumn: "demographicYear", sortDirection: "asc", max: 5, offset: 0]
        Map paramsMap = [sourceAndBackgroundInstitutionCode: sourceAndBackgroundInstitution.code, demographicYear: 2013]
        def criteriaMap = [[key: "demographicYear", binding: "demographicYear", operator: "equals"]]
        def filterData = [params: paramsMap, criteria: criteriaMap]

        def records = SourceBackgroundInstitutionEthnicMakeUp.fetchSearch(filterData, pagingAndSortParams)
        assertTrue records.size() == 1
        records.each { record ->
            assertTrue record.demographicYear == 2013
        }
    }


    private def newValidForCreateSourceBackgroundInstitutionEthnicMakeUp() {
        def sourceBackgroundInstitutionEthnicMakeUp = new SourceBackgroundInstitutionEthnicMakeUp(
                demographicYear: 2014,
                ethnicPercent: 50,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
                ethnicity: Ethnicity.findWhere(code: "1"),
        )
        return sourceBackgroundInstitutionEthnicMakeUp
    }


    private def newInvalidForCreateSourceBackgroundInstitutionEthnicMakeUp() {
        def sourceBackgroundInstitutionEthnicMakeUp = new SourceBackgroundInstitutionEthnicMakeUp(
                demographicYear: null,
                sourceAndBackgroundInstitution: null,
                ethnicity: null,
        )
        return sourceBackgroundInstitutionEthnicMakeUp
    }
}

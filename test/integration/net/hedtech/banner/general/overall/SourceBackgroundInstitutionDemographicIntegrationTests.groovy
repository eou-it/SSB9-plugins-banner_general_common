/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
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

class SourceBackgroundInstitutionDemographicIntegrationTests extends BaseIntegrationTestCase {
    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateValidSourceBackgroundInstitutionDemographic() {
        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
        sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sourceBackgroundInstitutionDemographic.id
    }


    void testCreateInvalidSourceBackgroundInstitutionDemographic() {
        def sourceBackgroundInstitutionDemographic = newInvalidForCreateSourceBackgroundInstitutionDemographic()
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)
        }
    }


    void testUpdateValidSourceBackgroundInstitutionDemographic() {
        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
        sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionDemographic.id
        assertEquals 0L, sourceBackgroundInstitutionDemographic.version
        assertEquals 2014, sourceBackgroundInstitutionDemographic.demographicYear
        assertEquals 100, sourceBackgroundInstitutionDemographic.enrollment
        assertEquals 50, sourceBackgroundInstitutionDemographic.numberOfSeniors
        assertEquals 60, sourceBackgroundInstitutionDemographic.meanFamilyIncome
        assertEquals 80, sourceBackgroundInstitutionDemographic.percentCollegeBound

        //Update the entity
        sourceBackgroundInstitutionDemographic.enrollment = 101
        sourceBackgroundInstitutionDemographic.numberOfSeniors = 51
        sourceBackgroundInstitutionDemographic.meanFamilyIncome = 61
        sourceBackgroundInstitutionDemographic.percentCollegeBound = 81
        sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)

        //Assert for sucessful update
        sourceBackgroundInstitutionDemographic = SourceBackgroundInstitutionDemographic.get(sourceBackgroundInstitutionDemographic.id)
        assertEquals 1L, sourceBackgroundInstitutionDemographic?.version
        assertEquals 101, sourceBackgroundInstitutionDemographic.enrollment
        assertEquals 51, sourceBackgroundInstitutionDemographic.numberOfSeniors
        assertEquals 61, sourceBackgroundInstitutionDemographic.meanFamilyIncome
        assertEquals 81, sourceBackgroundInstitutionDemographic.percentCollegeBound
    }


    void testUpdateInvalidSourceBackgroundInstitutionDemographic() {
        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
        sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionDemographic.id
        assertEquals 0L, sourceBackgroundInstitutionDemographic.version
        assertEquals 2014, sourceBackgroundInstitutionDemographic.demographicYear
        assertEquals 100, sourceBackgroundInstitutionDemographic.enrollment
        assertEquals 50, sourceBackgroundInstitutionDemographic.numberOfSeniors
        assertEquals 60, sourceBackgroundInstitutionDemographic.meanFamilyIncome
        assertEquals 80, sourceBackgroundInstitutionDemographic.percentCollegeBound

        //Update the entity with invalid values
        sourceBackgroundInstitutionDemographic.enrollment = 100000
        sourceBackgroundInstitutionDemographic.numberOfSeniors = 100000
        sourceBackgroundInstitutionDemographic.meanFamilyIncome = 100000
        sourceBackgroundInstitutionDemographic.percentCollegeBound = 100000

        shouldFail(ValidationException) {
            sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)
        }
    }


    void testDates() {
        def time = new SimpleDateFormat('HHmmss')
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
        sourceBackgroundInstitutionDemographic.save(flush: true, failOnError: true)
        sourceBackgroundInstitutionDemographic.refresh()
        assertNotNull "SourceBackgroundInstitutionDemographic should have been saved", sourceBackgroundInstitutionDemographic.id

        // test date values -
        assertEquals date.format(today), date.format(sourceBackgroundInstitutionDemographic.lastModified)
        assertEquals hour.format(today), hour.format(sourceBackgroundInstitutionDemographic.lastModified)
    }


    void testOptimisticLock() {
        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
        sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update SORBDMO set SORBDMO_VERSION = 999 where SORBDMO_SURROGATE_ID = ?", [sourceBackgroundInstitutionDemographic.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        sourceBackgroundInstitutionDemographic.enrollment = 101
        shouldFail(HibernateOptimisticLockingFailureException) {
            sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)
        }
    }


    void testDeleteSourceBackgroundInstitutionDemographic() {
        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
        sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)
        def id = sourceBackgroundInstitutionDemographic.id
        assertNotNull id
        sourceBackgroundInstitutionDemographic.delete()
        assertNull SourceBackgroundInstitutionDemographic.get(id)
    }


    void testValidation() {
        def sourceBackgroundInstitutionDemographic = newInvalidForCreateSourceBackgroundInstitutionDemographic()
        assertFalse "SourceBackgroundInstitutionDemographic could not be validated as expected due to ${sourceBackgroundInstitutionDemographic.errors}", sourceBackgroundInstitutionDemographic.validate()
    }


    void testNullValidationFailure() {
        def sourceBackgroundInstitutionDemographic = new SourceBackgroundInstitutionDemographic()
        assertFalse "SourceBackgroundInstitutionDemographic should have failed validation", sourceBackgroundInstitutionDemographic.validate()
        assertErrorsFor sourceBackgroundInstitutionDemographic, 'nullable',
                [
                        'demographicYear',
                        'sourceAndBackgroundInstitution'
                ]
        assertNoErrorsFor sourceBackgroundInstitutionDemographic,
                [
                        'enrollment',
                        'numberOfSeniors',
                        'meanFamilyIncome',
                        'percentCollegeBound'
                ]
    }


    void testFetchSearch() {
        // Create 2 demographics
        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
        sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionDemographic.id
        assertEquals 0L, sourceBackgroundInstitutionDemographic.version

        def sourceAndBackgroundInstitution = sourceBackgroundInstitutionDemographic.sourceAndBackgroundInstitution

        sourceBackgroundInstitutionDemographic = new SourceBackgroundInstitutionDemographic(
                demographicYear: 2013,
                sourceAndBackgroundInstitution: sourceAndBackgroundInstitution,
        )
        sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)

        def pagingAndSortParams = [sortColumn: "demographicYear", sortDirection: "asc", max: 5, offset: 0]
        Map paramsMap = [sourceAndBackgroundInstitutionCode: sourceAndBackgroundInstitution.code, demographicYear: 2013]
        def criteriaMap = [[key: "demographicYear", binding: "demographicYear", operator: "equals"]]
        def filterData = [params: paramsMap, criteria: criteriaMap]

        def records = SourceBackgroundInstitutionDemographic.fetchSearch(filterData, pagingAndSortParams)
        assertTrue records.size() == 1
        records.each { record ->
            assertTrue record.demographicYear == 2013
        }
    }


    private def newValidForCreateSourceBackgroundInstitutionDemographic() {
        def sourceBackgroundInstitutionDemographic = new SourceBackgroundInstitutionDemographic(
                demographicYear: 2014,
                enrollment: 100,
                numberOfSeniors: 50,
                meanFamilyIncome: 60,
                percentCollegeBound: 80,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
        )
        return sourceBackgroundInstitutionDemographic
    }


    private def newInvalidForCreateSourceBackgroundInstitutionDemographic() {
        def sourceBackgroundInstitutionDemographic = new SourceBackgroundInstitutionDemographic(
                demographicYear: null,
                sourceAndBackgroundInstitution: null,
        )
        return sourceBackgroundInstitutionDemographic
    }
}

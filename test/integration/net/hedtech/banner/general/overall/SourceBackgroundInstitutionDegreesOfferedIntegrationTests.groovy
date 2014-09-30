/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import grails.validation.ValidationException
import net.hedtech.banner.general.system.Degree
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase

import java.text.SimpleDateFormat

class SourceBackgroundInstitutionDegreesOfferedIntegrationTests extends BaseIntegrationTestCase {

    void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    void tearDown() {
        super.tearDown()
    }


    void testCreateValidSourceBackgroundInstitutionDegreesOffered() {
        def sourceBackgroundInstitutionDegreesOffered = newValidForCreateSourceBackgroundInstitutionDegreesOffered()
        sourceBackgroundInstitutionDegreesOffered.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sourceBackgroundInstitutionDegreesOffered.id
    }


    void testCreateInvalidSourceBackgroundInstitutionDegreesOffered() {
        def sourceBackgroundInstitutionDegreesOffered = newInvalidForCreateSourceBackgroundInstitutionDegreesOffered()
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionDegreesOffered.save(failOnError: true, flush: true)
        }
    }

    // NOTE: No Updates are allowed

    void testDates() {
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def sourceBackgroundInstitutionDegreesOffered = newValidForCreateSourceBackgroundInstitutionDegreesOffered()
        sourceBackgroundInstitutionDegreesOffered.save(flush: true, failOnError: true)
        sourceBackgroundInstitutionDegreesOffered.refresh()
        assertNotNull "SourceBackgroundInstitutionDegreesOffered should have been saved", sourceBackgroundInstitutionDegreesOffered.id

        // test date values -
        assertEquals date.format(today), date.format(sourceBackgroundInstitutionDegreesOffered.lastModified)
        assertEquals hour.format(today), hour.format(sourceBackgroundInstitutionDegreesOffered.lastModified)
    }


    void testDeleteSourceBackgroundInstitutionDegreesOffered() {
        def sourceBackgroundInstitutionDegreesOffered = newValidForCreateSourceBackgroundInstitutionDegreesOffered()
        sourceBackgroundInstitutionDegreesOffered.save(failOnError: true, flush: true)
        def id = sourceBackgroundInstitutionDegreesOffered.id
        assertNotNull id
        sourceBackgroundInstitutionDegreesOffered.delete()
        assertNull SourceBackgroundInstitutionDegreesOffered.get(id)
    }


    void testValidation() {
        def sourceBackgroundInstitutionDegreesOffered = newInvalidForCreateSourceBackgroundInstitutionDegreesOffered()
        assertFalse "SourceBackgroundInstitutionDegreesOffered could not be validated as expected due to ${sourceBackgroundInstitutionDegreesOffered.errors}", sourceBackgroundInstitutionDegreesOffered.validate()
    }


    void testNullValidationFailure() {
        def sourceBackgroundInstitutionDegreesOffered = new SourceBackgroundInstitutionDegreesOffered()
        assertFalse "SourceBackgroundInstitutionDegreesOffered should have failed validation", sourceBackgroundInstitutionDegreesOffered.validate()
        assertErrorsFor sourceBackgroundInstitutionDegreesOffered, 'nullable',
                [
                        'demographicYear',
                        'sourceAndBackgroundInstitution',
                        'degree'
                ]
    }


    void testFetchSearch() {
        // Create 2 degrees
        def sourceBackgroundInstitutionDegreesOffered = newValidForCreateSourceBackgroundInstitutionDegreesOffered()
        sourceBackgroundInstitutionDegreesOffered.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionDegreesOffered.id
        assertEquals 0L, sourceBackgroundInstitutionDegreesOffered.version

        def sourceAndBackgroundInstitution = sourceBackgroundInstitutionDegreesOffered.sourceAndBackgroundInstitution

        sourceBackgroundInstitutionDegreesOffered = new SourceBackgroundInstitutionDegreesOffered(
                demographicYear: 2013,
                sourceAndBackgroundInstitution: sourceAndBackgroundInstitution,
                degree: Degree.findWhere(code: "PHD"),
        )
        sourceBackgroundInstitutionDegreesOffered.save(failOnError: true, flush: true)

        def pagingAndSortParams = [sortColumn: "demographicYear", sortDirection: "asc", max: 5, offset: 0]
        Map paramsMap = [sourceAndBackgroundInstitutionCode: sourceAndBackgroundInstitution.code, demographicYear: 2013]
        def criteriaMap = [[key: "demographicYear", binding: "demographicYear", operator: "equals"]]
        def filterData = [params: paramsMap, criteria: criteriaMap]

        def records = SourceBackgroundInstitutionDegreesOffered.fetchSearch(filterData, pagingAndSortParams)
        assertTrue records.size() == 1
        records.each { record ->
            assertTrue record.demographicYear == 2013
        }
    }


    private def newValidForCreateSourceBackgroundInstitutionDegreesOffered() {
        def sourceBackgroundInstitutionDegreesOffered = new SourceBackgroundInstitutionDegreesOffered(
                demographicYear: 2014,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
                degree: Degree.findWhere(code: "PHD"),
        )
        return sourceBackgroundInstitutionDegreesOffered
    }


    private def newInvalidForCreateSourceBackgroundInstitutionDegreesOffered() {
        def sourceBackgroundInstitutionDegreesOffered = new SourceBackgroundInstitutionDegreesOffered(
                demographicYear: null,
                sourceAndBackgroundInstitution: null,
                degree: null,
        )
        return sourceBackgroundInstitutionDegreesOffered
    }
}

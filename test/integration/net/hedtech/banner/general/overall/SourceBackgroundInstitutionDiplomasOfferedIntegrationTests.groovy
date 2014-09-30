/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import grails.validation.ValidationException
import net.hedtech.banner.general.system.DiplomaType
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase

import java.text.SimpleDateFormat

class SourceBackgroundInstitutionDiplomasOfferedIntegrationTests extends BaseIntegrationTestCase {

    void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    void tearDown() {
        super.tearDown()
    }


    void testCreateValidSourceBackgroundInstitutionDiplomasOffered() {
        def sourceBackgroundInstitutionDiplomasOffered = newValidForCreateSourceBackgroundInstitutionDiplomasOffered()
        sourceBackgroundInstitutionDiplomasOffered.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sourceBackgroundInstitutionDiplomasOffered.id
    }


    void testCreateInvalidSourceBackgroundInstitutionDiplomasOffered() {
        def sourceBackgroundInstitutionDiplomasOffered = newInvalidForCreateSourceBackgroundInstitutionDiplomasOffered()
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionDiplomasOffered.save(failOnError: true, flush: true)
        }
    }

    // NOTE: No Updates are allowed

    void testDates() {
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def sourceBackgroundInstitutionDiplomasOffered = newValidForCreateSourceBackgroundInstitutionDiplomasOffered()
        sourceBackgroundInstitutionDiplomasOffered.save(flush: true, failOnError: true)
        sourceBackgroundInstitutionDiplomasOffered.refresh()
        assertNotNull "SourceBackgroundInstitutionDiplomasOffered should have been saved", sourceBackgroundInstitutionDiplomasOffered.id

        // test date values -
        assertEquals date.format(today), date.format(sourceBackgroundInstitutionDiplomasOffered.lastModified)
        assertEquals hour.format(today), hour.format(sourceBackgroundInstitutionDiplomasOffered.lastModified)
    }


    void testDeleteSourceBackgroundInstitutionDiplomasOffered() {
        def sourceBackgroundInstitutionDiplomasOffered = newValidForCreateSourceBackgroundInstitutionDiplomasOffered()
        sourceBackgroundInstitutionDiplomasOffered.save(failOnError: true, flush: true)
        def id = sourceBackgroundInstitutionDiplomasOffered.id
        assertNotNull id
        sourceBackgroundInstitutionDiplomasOffered.delete()
        assertNull SourceBackgroundInstitutionDiplomasOffered.get(id)
    }


    void testValidation() {
        def sourceBackgroundInstitutionDiplomasOffered = newInvalidForCreateSourceBackgroundInstitutionDiplomasOffered()
        assertFalse "SourceBackgroundInstitutionDiplomasOffered could not be validated as expected due to ${sourceBackgroundInstitutionDiplomasOffered.errors}", sourceBackgroundInstitutionDiplomasOffered.validate()
    }


    void testNullValidationFailure() {
        def sourceBackgroundInstitutionDiplomasOffered = new SourceBackgroundInstitutionDiplomasOffered()
        assertFalse "SourceBackgroundInstitutionDiplomasOffered should have failed validation", sourceBackgroundInstitutionDiplomasOffered.validate()
        assertErrorsFor sourceBackgroundInstitutionDiplomasOffered, 'nullable',
                [
                        'demographicYear',
                        'sourceAndBackgroundInstitution',
                        'diplomaType'
                ]
    }


    void testFetchSearch() {
        // Create 2 diplomas
        def sourceBackgroundInstitutionDiplomasOffered = newValidForCreateSourceBackgroundInstitutionDiplomasOffered()
        sourceBackgroundInstitutionDiplomasOffered.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionDiplomasOffered.id
        assertEquals 0L, sourceBackgroundInstitutionDiplomasOffered.version

        def sourceAndBackgroundInstitution = sourceBackgroundInstitutionDiplomasOffered.sourceAndBackgroundInstitution

        sourceBackgroundInstitutionDiplomasOffered = new SourceBackgroundInstitutionDiplomasOffered(
                demographicYear: 2013,
                sourceAndBackgroundInstitution: sourceAndBackgroundInstitution,
                diplomaType: DiplomaType.findWhere(code: "TT"),
        )
        sourceBackgroundInstitutionDiplomasOffered.save(failOnError: true, flush: true)

        def pagingAndSortParams = [sortColumn: "demographicYear", sortDirection: "asc", max: 5, offset: 0]
        Map paramsMap = [sourceAndBackgroundInstitutionCode: sourceAndBackgroundInstitution.code, demographicYear: 2013]
        def criteriaMap = [[key: "demographicYear", binding: "demographicYear", operator: "equals"]]
        def filterData = [params: paramsMap, criteria: criteriaMap]

        def records = SourceBackgroundInstitutionDiplomasOffered.fetchSearch(filterData, pagingAndSortParams)
        assertTrue records.size() == 1
        records.each { record ->
            assertTrue record.demographicYear == 2013
        }
    }


    private def newValidForCreateSourceBackgroundInstitutionDiplomasOffered() {
        def diplomaType = newDiplomaType()
        diplomaType.save(failOnError: true, flush: true)
        def sourceBackgroundInstitutionDiplomasOffered = new SourceBackgroundInstitutionDiplomasOffered(
                demographicYear: 2014,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
                diplomaType: diplomaType,
        )
        return sourceBackgroundInstitutionDiplomasOffered
    }


    private def newInvalidForCreateSourceBackgroundInstitutionDiplomasOffered() {
        def sourceBackgroundInstitutionDiplomasOffered = new SourceBackgroundInstitutionDiplomasOffered(
                demographicYear: null,
                sourceAndBackgroundInstitution: null,
                diplomaType: null,
        )
        return sourceBackgroundInstitutionDiplomasOffered
    }


    private def newDiplomaType() {
        def diplomaType = new DiplomaType(
                code: "TT",
                description: "TTTT"
        )
        return diplomaType
    }
}

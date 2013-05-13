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

class SourceBackgroundInstitutionAcademicIntegrationTests extends BaseIntegrationTestCase {

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateValidSourceBackgroundInstitutionAcademic() {
        def sourceBackgroundInstitutionAcademic = newValidForCreateSourceBackgroundInstitutionAcademic()
        sourceBackgroundInstitutionAcademic.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sourceBackgroundInstitutionAcademic.id
    }


    void testCreateInvalidSourceBackgroundInstitutionAcademic() {
        def sourceBackgroundInstitutionAcademic = newInvalidForCreateSourceBackgroundInstitutionAcademic()
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionAcademic.save(failOnError: true, flush: true)
        }
    }


    void testUpdateValidSourceBackgroundInstitutionAcademic() {
        def sourceBackgroundInstitutionAcademic = newValidForCreateSourceBackgroundInstitutionAcademic()
        sourceBackgroundInstitutionAcademic.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionAcademic.id
        assertEquals 0L, sourceBackgroundInstitutionAcademic.version
        assertEquals 2014, sourceBackgroundInstitutionAcademic.demographicYear
        assertEquals "Y", sourceBackgroundInstitutionAcademic.stateApprovIndicator
        assertEquals "STANDARD", sourceBackgroundInstitutionAcademic.calendarType
        assertEquals "TTTT", sourceBackgroundInstitutionAcademic.accreditationType
        assertEquals 5.5, sourceBackgroundInstitutionAcademic.creditTransactionValue

        //Update the entity
        sourceBackgroundInstitutionAcademic.stateApprovIndicator = null
        sourceBackgroundInstitutionAcademic.calendarType = null
        sourceBackgroundInstitutionAcademic.accreditationType = "UPDT"
        sourceBackgroundInstitutionAcademic.creditTransactionValue = 0.0
        sourceBackgroundInstitutionAcademic.save(failOnError: true, flush: true)

        //Assert for sucessful update
        sourceBackgroundInstitutionAcademic = SourceBackgroundInstitutionAcademic.get(sourceBackgroundInstitutionAcademic.id)
        assertEquals 1L, sourceBackgroundInstitutionAcademic?.version
        assertNull sourceBackgroundInstitutionAcademic.stateApprovIndicator
        assertNull sourceBackgroundInstitutionAcademic.calendarType
        assertEquals "UPDT", sourceBackgroundInstitutionAcademic.accreditationType
        assertEquals 0.0, sourceBackgroundInstitutionAcademic.creditTransactionValue
    }


    void testUpdateInvalidSourceBackgroundInstitutionAcademic() {
        def sourceBackgroundInstitutionAcademic = newValidForCreateSourceBackgroundInstitutionAcademic()
        sourceBackgroundInstitutionAcademic.save(failOnError: true, flush: true)
        assertNotNull sourceBackgroundInstitutionAcademic.id
        assertEquals 0L, sourceBackgroundInstitutionAcademic.version
        assertEquals 2014, sourceBackgroundInstitutionAcademic.demographicYear
        assertEquals "Y", sourceBackgroundInstitutionAcademic.stateApprovIndicator
        assertEquals "STANDARD", sourceBackgroundInstitutionAcademic.calendarType
        assertEquals "TTTT", sourceBackgroundInstitutionAcademic.accreditationType
        assertEquals 5.5, sourceBackgroundInstitutionAcademic.creditTransactionValue

        //Update the entity with invalid values
        sourceBackgroundInstitutionAcademic.demographicYear = null
        shouldFail(ValidationException) {
            sourceBackgroundInstitutionAcademic.save(failOnError: true, flush: true)
        }
    }


    void testDates() {
        def time = new SimpleDateFormat('HHmmss')
        def hour = new SimpleDateFormat('HH')
        def date = new SimpleDateFormat('yyyy-M-d')
        def today = new Date()

        def sourceBackgroundInstitutionAcademic = newValidForCreateSourceBackgroundInstitutionAcademic()

        sourceBackgroundInstitutionAcademic.save(flush: true, failOnError: true)
        sourceBackgroundInstitutionAcademic.refresh()
        assertNotNull "SourceBackgroundInstitutionAcademic should have been saved", sourceBackgroundInstitutionAcademic.id

        // test date values -
        assertEquals date.format(today), date.format(sourceBackgroundInstitutionAcademic.lastModified)
        assertEquals hour.format(today), hour.format(sourceBackgroundInstitutionAcademic.lastModified)
    }


    void testOptimisticLock() {
        def sourceBackgroundInstitutionAcademic = newValidForCreateSourceBackgroundInstitutionAcademic()
        sourceBackgroundInstitutionAcademic.save(failOnError: true, flush: true)

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update SORBACD set SORBACD_VERSION = 999 where SORBACD_SURROGATE_ID = ?", [sourceBackgroundInstitutionAcademic.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        //Update the entity
        sourceBackgroundInstitutionAcademic.stateApprovIndicator = null
        sourceBackgroundInstitutionAcademic.calendarType = null
        sourceBackgroundInstitutionAcademic.accreditationType = "UPDT"
        sourceBackgroundInstitutionAcademic.creditTransactionValue = 0.0
        shouldFail(HibernateOptimisticLockingFailureException) {
            sourceBackgroundInstitutionAcademic.save(failOnError: true, flush: true)
        }
    }


    void testDeleteSourceBackgroundInstitutionAcademic() {
        def sourceBackgroundInstitutionAcademic = newValidForCreateSourceBackgroundInstitutionAcademic()
        sourceBackgroundInstitutionAcademic.save(failOnError: true, flush: true)
        def id = sourceBackgroundInstitutionAcademic.id
        assertNotNull id
        sourceBackgroundInstitutionAcademic.delete()
        assertNull SourceBackgroundInstitutionAcademic.get(id)
    }


    void testValidation() {
        def sourceBackgroundInstitutionAcademic = newInvalidForCreateSourceBackgroundInstitutionAcademic()
        assertFalse "SourceBackgroundInstitutionAcademic could not be validated as expected due to ${sourceBackgroundInstitutionAcademic.errors}", sourceBackgroundInstitutionAcademic.validate()
    }


    void testNullValidationFailure() {
        def sourceBackgroundInstitutionAcademic = new SourceBackgroundInstitutionAcademic()
        assertFalse "SourceBackgroundInstitutionAcademic should have failed validation", sourceBackgroundInstitutionAcademic.validate()
        assertErrorsFor sourceBackgroundInstitutionAcademic, 'nullable',
                [
                        'demographicYear',
                        'sourceAndBackgroundInstitution'
                ]
        assertNoErrorsFor sourceBackgroundInstitutionAcademic,
                [
                        'stateApprovIndicator',
                        'calendarType',
                        'accreditationType',
                        'creditTransactionValue'
                ]
    }


    void testMaxSizeValidationFailures() {
        def sourceBackgroundInstitutionAcademic = new SourceBackgroundInstitutionAcademic(
                stateApprovIndicator: 'XXX',
                calendarType: 'XXXXXXXXXXXX',
                accreditationType: 'XXXXXXXXXXXXXXXXX')
        assertFalse "SourceBackgroundInstitutionAcademic should have failed validation", sourceBackgroundInstitutionAcademic.validate()
        assertErrorsFor sourceBackgroundInstitutionAcademic, 'maxSize', ['stateApprovIndicator', 'calendarType', 'accreditationType']
    }


    private def newValidForCreateSourceBackgroundInstitutionAcademic() {
        def sourceBackgroundInstitutionAcademic = new SourceBackgroundInstitutionAcademic(
                demographicYear: 2014,
                stateApprovIndicator: "Y",
                calendarType: "STANDARD",
                accreditationType: "TTTT",
                creditTransactionValue: 5.5,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
        )
        return sourceBackgroundInstitutionAcademic
    }


    private def newInvalidForCreateSourceBackgroundInstitutionAcademic() {
        def sourceBackgroundInstitutionAcademic = new SourceBackgroundInstitutionAcademic(
                demographicYear: null,
                sourceAndBackgroundInstitution: null,
        )
        return sourceBackgroundInstitutionAcademic
    }
}
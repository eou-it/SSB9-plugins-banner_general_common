/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import groovy.sql.Sql
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

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


//    void testCreateInvalidSourceBackgroundInstitutionDemographic() {
//        def sourceBackgroundInstitutionDemographic = newInvalidForCreateSourceBackgroundInstitutionDemographic()
//        shouldFail(ValidationException) {
//            sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)
//        }
//    }
//
//
//    void testUpdateValidSourceBackgroundInstitutionDemographic() {
//        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
//        sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)
//        assertNotNull sourceBackgroundInstitutionDemographic.id
//        assertEquals 0L, sourceBackgroundInstitutionDemographic.version
//        assertEquals i_success_demographicYear, sourceBackgroundInstitutionDemographic.demographicYear
//        assertEquals i_success_enrollment, sourceBackgroundInstitutionDemographic.enrollment
//        assertEquals i_success_numberOfSeniors, sourceBackgroundInstitutionDemographic.numberOfSeniors
//        assertEquals i_success_meanFamilyIncome, sourceBackgroundInstitutionDemographic.meanFamilyIncome
//        assertEquals i_success_percentCollegeBound, sourceBackgroundInstitutionDemographic.percentCollegeBound
//
//        //Update the entity
//        sourceBackgroundInstitutionDemographic.enrollment = u_success_enrollment
//        sourceBackgroundInstitutionDemographic.numberOfSeniors = u_success_numberOfSeniors
//        sourceBackgroundInstitutionDemographic.meanFamilyIncome = u_success_meanFamilyIncome
//        sourceBackgroundInstitutionDemographic.percentCollegeBound = u_success_percentCollegeBound
//
//        sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)
//        //Assert for sucessful update
//        sourceBackgroundInstitutionDemographic = SourceBackgroundInstitutionDemographic.get(sourceBackgroundInstitutionDemographic.id)
//        assertEquals 1L, sourceBackgroundInstitutionDemographic?.version
//        assertEquals u_success_enrollment, sourceBackgroundInstitutionDemographic.enrollment
//        assertEquals u_success_numberOfSeniors, sourceBackgroundInstitutionDemographic.numberOfSeniors
//        assertEquals u_success_meanFamilyIncome, sourceBackgroundInstitutionDemographic.meanFamilyIncome
//        assertEquals u_success_percentCollegeBound, sourceBackgroundInstitutionDemographic.percentCollegeBound
//
//    }
//
//
//    void testUpdateInvalidSourceBackgroundInstitutionDemographic() {
//        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
//        sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)
//        assertNotNull sourceBackgroundInstitutionDemographic.id
//        assertEquals 0L, sourceBackgroundInstitutionDemographic.version
//        assertEquals i_success_demographicYear, sourceBackgroundInstitutionDemographic.demographicYear
//        assertEquals i_success_enrollment, sourceBackgroundInstitutionDemographic.enrollment
//        assertEquals i_success_numberOfSeniors, sourceBackgroundInstitutionDemographic.numberOfSeniors
//        assertEquals i_success_meanFamilyIncome, sourceBackgroundInstitutionDemographic.meanFamilyIncome
//        assertEquals i_success_percentCollegeBound, sourceBackgroundInstitutionDemographic.percentCollegeBound
//
//        //Update the entity with invalid values
//        sourceBackgroundInstitutionDemographic.enrollment = u_failure_enrollment
//        sourceBackgroundInstitutionDemographic.numberOfSeniors = u_failure_numberOfSeniors
//        sourceBackgroundInstitutionDemographic.meanFamilyIncome = u_failure_meanFamilyIncome
//        sourceBackgroundInstitutionDemographic.percentCollegeBound = u_failure_percentCollegeBound
//
//        shouldFail(ValidationException) {
//            sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)
//        }
//    }
//
//
//    void testDates() {
//        def time = new SimpleDateFormat('HHmmss')
//        def hour = new SimpleDateFormat('HH')
//        def date = new SimpleDateFormat('yyyy-M-d')
//        def today = new Date()
//
//        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
//
//
//
//        sourceBackgroundInstitutionDemographic.save(flush: true, failOnError: true)
//        sourceBackgroundInstitutionDemographic.refresh()
//        assertNotNull "SourceBackgroundInstitutionDemographic should have been saved", sourceBackgroundInstitutionDemographic.id
//
//        // test date values -
//        assertEquals date.format(today), date.format(sourceBackgroundInstitutionDemographic.lastModified)
//        assertEquals hour.format(today), hour.format(sourceBackgroundInstitutionDemographic.lastModified)
//
//
//    }
//
//
//    void testOptimisticLock() {
//        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
//        sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)
//
//        def sql
//        try {
//            sql = new Sql(sessionFactory.getCurrentSession().connection())
//            sql.executeUpdate("update SORBDMO set SORBDMO_VERSION = 999 where SORBDMO_SURROGATE_ID = ?", [sourceBackgroundInstitutionDemographic.id])
//        } finally {
//            sql?.close() // note that the test will close the connection, since it's our current session's connection
//        }
//        //Try to update the entity
//        //Update the entity
//        sourceBackgroundInstitutionDemographic.enrollment = u_success_enrollment
//        sourceBackgroundInstitutionDemographic.numberOfSeniors = u_success_numberOfSeniors
//        sourceBackgroundInstitutionDemographic.meanFamilyIncome = u_success_meanFamilyIncome
//        sourceBackgroundInstitutionDemographic.percentCollegeBound = u_success_percentCollegeBound
//        shouldFail(HibernateOptimisticLockingFailureException) {
//            sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)
//        }
//    }
//
//
//    void testDeleteSourceBackgroundInstitutionDemographic() {
//        def sourceBackgroundInstitutionDemographic = newValidForCreateSourceBackgroundInstitutionDemographic()
//        sourceBackgroundInstitutionDemographic.save(failOnError: true, flush: true)
//        def id = sourceBackgroundInstitutionDemographic.id
//        assertNotNull id
//        sourceBackgroundInstitutionDemographic.delete()
//        assertNull SourceBackgroundInstitutionDemographic.get(id)
//    }
//
//
//    void testValidation() {
//        def sourceBackgroundInstitutionDemographic = newInvalidForCreateSourceBackgroundInstitutionDemographic()
//        assertFalse "SourceBackgroundInstitutionDemographic could not be validated as expected due to ${sourceBackgroundInstitutionDemographic.errors}", sourceBackgroundInstitutionDemographic.validate()
//    }
//
//
//    void testNullValidationFailure() {
//        def sourceBackgroundInstitutionDemographic = new SourceBackgroundInstitutionDemographic()
//        assertFalse "SourceBackgroundInstitutionDemographic should have failed validation", sourceBackgroundInstitutionDemographic.validate()
//        assertErrorsFor sourceBackgroundInstitutionDemographic, 'nullable',
//                [
//                        'demographicYear',
//                        'sourceAndBackgroundInstitution'
//                ]
//        assertNoErrorsFor sourceBackgroundInstitutionDemographic,
//                [
//                        'enrollment',
//                        'numberOfSeniors',
//                        'meanFamilyIncome',
//                        'percentCollegeBound'
//                ]
//    }


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

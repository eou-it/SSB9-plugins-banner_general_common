/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import groovy.sql.Sql
import net.hedtech.banner.general.system.Degree
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

class SourceBackgroundInstitutionDegreesOfferedIntegrationTests extends BaseIntegrationTestCase {

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateValidSourceBackgroundInstitutionDegreesOffered() {
        def sourceBackgroundInstitutionDegreesOffered = newValidForCreateSourceBackgroundInstitutionDegreesOffered()
        sourceBackgroundInstitutionDegreesOffered.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sourceBackgroundInstitutionDegreesOffered.id
    }


//    void testCreateInvalidSourceBackgroundInstitutionDegreesOffered() {
//        def sourceBackgroundInstitutionDegreesOffered = newInvalidForCreateSourceBackgroundInstitutionDegreesOffered()
//        shouldFail(ValidationException) {
//            sourceBackgroundInstitutionDegreesOffered.save(failOnError: true, flush: true)
//        }
//    }
//
//
//    void testUpdateValidSourceBackgroundInstitutionDegreesOffered() {
//        def sourceBackgroundInstitutionDegreesOffered = newValidForCreateSourceBackgroundInstitutionDegreesOffered()
//        sourceBackgroundInstitutionDegreesOffered.save(failOnError: true, flush: true)
//        assertNotNull sourceBackgroundInstitutionDegreesOffered.id
//        assertEquals 0L, sourceBackgroundInstitutionDegreesOffered.version
//        assertEquals i_success_demographicYear, sourceBackgroundInstitutionDegreesOffered.demographicYear
//
//        //Update the entity
//
//        sourceBackgroundInstitutionDegreesOffered.save(failOnError: true, flush: true)
//        //Assert for sucessful update
//        sourceBackgroundInstitutionDegreesOffered = SourceBackgroundInstitutionDegreesOffered.get(sourceBackgroundInstitutionDegreesOffered.id)
//        assertEquals 1L, sourceBackgroundInstitutionDegreesOffered?.version
//
//    }
//
//
//    void testUpdateInvalidSourceBackgroundInstitutionDegreesOffered() {
//        def sourceBackgroundInstitutionDegreesOffered = newValidForCreateSourceBackgroundInstitutionDegreesOffered()
//        sourceBackgroundInstitutionDegreesOffered.save(failOnError: true, flush: true)
//        assertNotNull sourceBackgroundInstitutionDegreesOffered.id
//        assertEquals 0L, sourceBackgroundInstitutionDegreesOffered.version
//        assertEquals i_success_demographicYear, sourceBackgroundInstitutionDegreesOffered.demographicYear
//
//        //Update the entity with invalid values
//
//        shouldFail(ValidationException) {
//            sourceBackgroundInstitutionDegreesOffered.save(failOnError: true, flush: true)
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
//        def sourceBackgroundInstitutionDegreesOffered = newValidForCreateSourceBackgroundInstitutionDegreesOffered()
//
//
//
//        sourceBackgroundInstitutionDegreesOffered.save(flush: true, failOnError: true)
//        sourceBackgroundInstitutionDegreesOffered.refresh()
//        assertNotNull "SourceBackgroundInstitutionDegreesOffered should have been saved", sourceBackgroundInstitutionDegreesOffered.id
//
//        // test date values -
//        assertEquals date.format(today), date.format(sourceBackgroundInstitutionDegreesOffered.lastModified)
//        assertEquals hour.format(today), hour.format(sourceBackgroundInstitutionDegreesOffered.lastModified)
//
//
//    }
//
//
//    void testOptimisticLock() {
//        def sourceBackgroundInstitutionDegreesOffered = newValidForCreateSourceBackgroundInstitutionDegreesOffered()
//        sourceBackgroundInstitutionDegreesOffered.save(failOnError: true, flush: true)
//
//        def sql
//        try {
//            sql = new Sql(sessionFactory.getCurrentSession().connection())
//            sql.executeUpdate("update SORBDEG set SORBDEG_VERSION = 999 where SORBDEG_SURROGATE_ID = ?", [sourceBackgroundInstitutionDegreesOffered.id])
//        } finally {
//            sql?.close() // note that the test will close the connection, since it's our current session's connection
//        }
//        //Try to update the entity
//        //Update the entity
//        shouldFail(HibernateOptimisticLockingFailureException) {
//            sourceBackgroundInstitutionDegreesOffered.save(failOnError: true, flush: true)
//        }
//    }
//
//
//    void testDeleteSourceBackgroundInstitutionDegreesOffered() {
//        def sourceBackgroundInstitutionDegreesOffered = newValidForCreateSourceBackgroundInstitutionDegreesOffered()
//        sourceBackgroundInstitutionDegreesOffered.save(failOnError: true, flush: true)
//        def id = sourceBackgroundInstitutionDegreesOffered.id
//        assertNotNull id
//        sourceBackgroundInstitutionDegreesOffered.delete()
//        assertNull SourceBackgroundInstitutionDegreesOffered.get(id)
//    }
//
//
//    void testValidation() {
//        def sourceBackgroundInstitutionDegreesOffered = newInvalidForCreateSourceBackgroundInstitutionDegreesOffered()
//        assertFalse "SourceBackgroundInstitutionDegreesOffered could not be validated as expected due to ${sourceBackgroundInstitutionDegreesOffered.errors}", sourceBackgroundInstitutionDegreesOffered.validate()
//    }
//
//
//    void testNullValidationFailure() {
//        def sourceBackgroundInstitutionDegreesOffered = new SourceBackgroundInstitutionDegreesOffered()
//        assertFalse "SourceBackgroundInstitutionDegreesOffered should have failed validation", sourceBackgroundInstitutionDegreesOffered.validate()
//        assertErrorsFor sourceBackgroundInstitutionDegreesOffered, 'nullable',
//                [
//                        'demographicYear',
//                        'sourceAndBackgroundInstitution',
//                        'degree'
//                ]
//    }


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

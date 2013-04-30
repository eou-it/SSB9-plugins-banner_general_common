/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.DiplomaType
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase

class SourceBackgroundInstitutionDiplomasOfferedIntegrationTests extends BaseIntegrationTestCase {

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateValidSourceBackgroundInstitutionDiplomasOffered() {
        def sourceBackgroundInstitutionDiplomasOffered = newValidForCreateSourceBackgroundInstitutionDiplomasOffered()
        sourceBackgroundInstitutionDiplomasOffered.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sourceBackgroundInstitutionDiplomasOffered.id
    }

//    void testCreateInvalidSourceBackgroundInstitutionDiplomasOffered() {
//        def sourceBackgroundInstitutionDiplomasOffered = newInvalidForCreateSourceBackgroundInstitutionDiplomasOffered()
//        shouldFail(ValidationException) {
//            sourceBackgroundInstitutionDiplomasOffered.save(failOnError: true, flush: true)
//        }
//    }
//
//
//    void testUpdateValidSourceBackgroundInstitutionDiplomasOffered() {
//        def sourceBackgroundInstitutionDiplomasOffered = newValidForCreateSourceBackgroundInstitutionDiplomasOffered()
//        sourceBackgroundInstitutionDiplomasOffered.save(failOnError: true, flush: true)
//        assertNotNull sourceBackgroundInstitutionDiplomasOffered.id
//        assertEquals 0L, sourceBackgroundInstitutionDiplomasOffered.version
//        assertEquals i_success_demographicYear, sourceBackgroundInstitutionDiplomasOffered.demographicYear
//
//        //Update the entity
//
//        sourceBackgroundInstitutionDiplomasOffered.save(failOnError: true, flush: true)
//        //Assert for sucessful update
//        sourceBackgroundInstitutionDiplomasOffered = SourceBackgroundInstitutionDiplomasOffered.get(sourceBackgroundInstitutionDiplomasOffered.id)
//        assertEquals 1L, sourceBackgroundInstitutionDiplomasOffered?.version
//
//    }
//
//
//    void testUpdateInvalidSourceBackgroundInstitutionDiplomasOffered() {
//        def sourceBackgroundInstitutionDiplomasOffered = newValidForCreateSourceBackgroundInstitutionDiplomasOffered()
//        sourceBackgroundInstitutionDiplomasOffered.save(failOnError: true, flush: true)
//        assertNotNull sourceBackgroundInstitutionDiplomasOffered.id
//        assertEquals 0L, sourceBackgroundInstitutionDiplomasOffered.version
//        assertEquals i_success_demographicYear, sourceBackgroundInstitutionDiplomasOffered.demographicYear
//
//        //Update the entity with invalid values
//
//        shouldFail(ValidationException) {
//            sourceBackgroundInstitutionDiplomasOffered.save(failOnError: true, flush: true)
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
//        def sourceBackgroundInstitutionDiplomasOffered = newValidForCreateSourceBackgroundInstitutionDiplomasOffered()
//
//
//
//        sourceBackgroundInstitutionDiplomasOffered.save(flush: true, failOnError: true)
//        sourceBackgroundInstitutionDiplomasOffered.refresh()
//        assertNotNull "SourceBackgroundInstitutionDiplomasOffered should have been saved", sourceBackgroundInstitutionDiplomasOffered.id
//
//        // test date values -
//        assertEquals date.format(today), date.format(sourceBackgroundInstitutionDiplomasOffered.lastModified)
//        assertEquals hour.format(today), hour.format(sourceBackgroundInstitutionDiplomasOffered.lastModified)
//
//
//    }
//
//
//    void testOptimisticLock() {
//        def sourceBackgroundInstitutionDiplomasOffered = newValidForCreateSourceBackgroundInstitutionDiplomasOffered()
//        sourceBackgroundInstitutionDiplomasOffered.save(failOnError: true, flush: true)
//
//        def sql
//        try {
//            sql = new Sql(sessionFactory.getCurrentSession().connection())
//            sql.executeUpdate("update SORBDPL set SORBDPL_VERSION = 999 where SORBDPL_SURROGATE_ID = ?", [sourceBackgroundInstitutionDiplomasOffered.id])
//        } finally {
//            sql?.close() // note that the test will close the connection, since it's our current session's connection
//        }
//        //Try to update the entity
//        //Update the entity
//        shouldFail(HibernateOptimisticLockingFailureException) {
//            sourceBackgroundInstitutionDiplomasOffered.save(failOnError: true, flush: true)
//        }
//    }
//
//
//    void testDeleteSourceBackgroundInstitutionDiplomasOffered() {
//        def sourceBackgroundInstitutionDiplomasOffered = newValidForCreateSourceBackgroundInstitutionDiplomasOffered()
//        sourceBackgroundInstitutionDiplomasOffered.save(failOnError: true, flush: true)
//        def id = sourceBackgroundInstitutionDiplomasOffered.id
//        assertNotNull id
//        sourceBackgroundInstitutionDiplomasOffered.delete()
//        assertNull SourceBackgroundInstitutionDiplomasOffered.get(id)
//    }
//
//
//    void testValidation() {
//        def sourceBackgroundInstitutionDiplomasOffered = newInvalidForCreateSourceBackgroundInstitutionDiplomasOffered()
//        assertFalse "SourceBackgroundInstitutionDiplomasOffered could not be validated as expected due to ${sourceBackgroundInstitutionDiplomasOffered.errors}", sourceBackgroundInstitutionDiplomasOffered.validate()
//    }
//
//
//    void testNullValidationFailure() {
//        def sourceBackgroundInstitutionDiplomasOffered = new SourceBackgroundInstitutionDiplomasOffered()
//        assertFalse "SourceBackgroundInstitutionDiplomasOffered should have failed validation", sourceBackgroundInstitutionDiplomasOffered.validate()
//        assertErrorsFor sourceBackgroundInstitutionDiplomasOffered, 'nullable',
//                [
//                        'demographicYear',
//                        'sourceAndBackgroundInstitution',
//                        'diplomaType'
//                ]
//    }


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

/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.BackgroundInstitutionCharacteristic
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase

class SourceBackgroundInstitutionCharacteristicIntegrationTests extends BaseIntegrationTestCase {

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateValidSourceBackgroundInstitutionCharacteristic() {
        def sourceBackgroundInstitutionCharacteristic = newValidForCreateSourceBackgroundInstitutionCharacteristic()
        sourceBackgroundInstitutionCharacteristic.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sourceBackgroundInstitutionCharacteristic.id
    }

//    void testCreateInvalidSourceBackgroundInstitutionCharacteristic() {
//        def sourceBackgroundInstitutionCharacteristic = newInvalidForCreateSourceBackgroundInstitutionCharacteristic()
//        shouldFail(ValidationException) {
//            sourceBackgroundInstitutionCharacteristic.save(failOnError: true, flush: true)
//        }
//    }
//
//
//    void testUpdateValidSourceBackgroundInstitutionCharacteristic() {
//        def sourceBackgroundInstitutionCharacteristic = newValidForCreateSourceBackgroundInstitutionCharacteristic()
//        sourceBackgroundInstitutionCharacteristic.save(failOnError: true, flush: true)
//        assertNotNull sourceBackgroundInstitutionCharacteristic.id
//        assertEquals 0L, sourceBackgroundInstitutionCharacteristic.version
//        assertEquals i_success_demographicYear, sourceBackgroundInstitutionCharacteristic.demographicYear
//
//        //Update the entity
//
//        sourceBackgroundInstitutionCharacteristic.save(failOnError: true, flush: true)
//        //Assert for sucessful update
//        sourceBackgroundInstitutionCharacteristic = SourceBackgroundInstitutionCharacteristic.get(sourceBackgroundInstitutionCharacteristic.id)
//        assertEquals 1L, sourceBackgroundInstitutionCharacteristic?.version
//
//    }
//
//
//    void testUpdateInvalidSourceBackgroundInstitutionCharacteristic() {
//        def sourceBackgroundInstitutionCharacteristic = newValidForCreateSourceBackgroundInstitutionCharacteristic()
//        sourceBackgroundInstitutionCharacteristic.save(failOnError: true, flush: true)
//        assertNotNull sourceBackgroundInstitutionCharacteristic.id
//        assertEquals 0L, sourceBackgroundInstitutionCharacteristic.version
//        assertEquals i_success_demographicYear, sourceBackgroundInstitutionCharacteristic.demographicYear
//
//        //Update the entity with invalid values
//
//        shouldFail(ValidationException) {
//            sourceBackgroundInstitutionCharacteristic.save(failOnError: true, flush: true)
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
//        def sourceBackgroundInstitutionCharacteristic = newValidForCreateSourceBackgroundInstitutionCharacteristic()
//
//
//
//        sourceBackgroundInstitutionCharacteristic.save(flush: true, failOnError: true)
//        sourceBackgroundInstitutionCharacteristic.refresh()
//        assertNotNull "SourceBackgroundInstitutionCharacteristic should have been saved", sourceBackgroundInstitutionCharacteristic.id
//
//        // test date values -
//        assertEquals date.format(today), date.format(sourceBackgroundInstitutionCharacteristic.lastModified)
//        assertEquals hour.format(today), hour.format(sourceBackgroundInstitutionCharacteristic.lastModified)
//
//
//    }
//
//
//    void testOptimisticLock() {
//        def sourceBackgroundInstitutionCharacteristic = newValidForCreateSourceBackgroundInstitutionCharacteristic()
//        sourceBackgroundInstitutionCharacteristic.save(failOnError: true, flush: true)
//
//        def sql
//        try {
//            sql = new Sql(sessionFactory.getCurrentSession().connection())
//            sql.executeUpdate("update SORBCHR set SORBCHR_VERSION = 999 where SORBCHR_SURROGATE_ID = ?", [sourceBackgroundInstitutionCharacteristic.id])
//        } finally {
//            sql?.close() // note that the test will close the connection, since it's our current session's connection
//        }
//        //Try to update the entity
//        //Update the entity
//        shouldFail(HibernateOptimisticLockingFailureException) {
//            sourceBackgroundInstitutionCharacteristic.save(failOnError: true, flush: true)
//        }
//    }
//
//
//    void testDeleteSourceBackgroundInstitutionCharacteristic() {
//        def sourceBackgroundInstitutionCharacteristic = newValidForCreateSourceBackgroundInstitutionCharacteristic()
//        sourceBackgroundInstitutionCharacteristic.save(failOnError: true, flush: true)
//        def id = sourceBackgroundInstitutionCharacteristic.id
//        assertNotNull id
//        sourceBackgroundInstitutionCharacteristic.delete()
//        assertNull SourceBackgroundInstitutionCharacteristic.get(id)
//    }
//
//
//    void testValidation() {
//        def sourceBackgroundInstitutionCharacteristic = newInvalidForCreateSourceBackgroundInstitutionCharacteristic()
//        assertFalse "SourceBackgroundInstitutionCharacteristic could not be validated as expected due to ${sourceBackgroundInstitutionCharacteristic.errors}", sourceBackgroundInstitutionCharacteristic.validate()
//    }
//
//
//    void testNullValidationFailure() {
//        def sourceBackgroundInstitutionCharacteristic = new SourceBackgroundInstitutionCharacteristic()
//        assertFalse "SourceBackgroundInstitutionCharacteristic should have failed validation", sourceBackgroundInstitutionCharacteristic.validate()
//        assertErrorsFor sourceBackgroundInstitutionCharacteristic, 'nullable',
//                [
//                        'demographicYear',
//                        'sourceAndBackgroundInstitution',
//                        'backgroundInstitutionCharacteristic'
//                ]
//    }
//

    private def newValidForCreateSourceBackgroundInstitutionCharacteristic() {
        def backgroundInstitutionCharacteristic = newBackgroundInstitutionCharacteristic()
        backgroundInstitutionCharacteristic.save(failOnError: true, flush: true)

        def sourceBackgroundInstitutionCharacteristic = new SourceBackgroundInstitutionCharacteristic(
                demographicYear: 2014,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
                backgroundInstitutionCharacteristic: backgroundInstitutionCharacteristic,
        )
        return sourceBackgroundInstitutionCharacteristic
    }


    private def newInvalidForCreateSourceBackgroundInstitutionCharacteristic() {
        def sourceBackgroundInstitutionCharacteristic = new SourceBackgroundInstitutionCharacteristic(
                demographicYear: null,
                sourceAndBackgroundInstitution: null,
                backgroundInstitutionCharacteristic: null,
        )
        return sourceBackgroundInstitutionCharacteristic
    }


    private def newBackgroundInstitutionCharacteristic() {
        def backgroundInstitutionCharacteristic = new BackgroundInstitutionCharacteristic(
                code: "T",
                description: "TTTT",
        )
        return backgroundInstitutionCharacteristic
    }
}

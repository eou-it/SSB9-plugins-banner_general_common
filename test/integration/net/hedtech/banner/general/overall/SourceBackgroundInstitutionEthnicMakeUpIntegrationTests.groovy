/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.general.system.Ethnicity


class SourceBackgroundInstitutionEthnicMakeUpIntegrationTests extends BaseIntegrationTestCase {

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateValidSourceBackgroundInstitutionEthnicMakeUp() {
        def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
        sourceBackgroundInstitutionEthnicMakeUp.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.id
    }

//	void testCreateInvalidSourceBackgroundInstitutionEthnicMakeUp() {
//		def sourceBackgroundInstitutionEthnicMakeUp = newInvalidForCreateSourceBackgroundInstitutionEthnicMakeUp()
//		shouldFail(ValidationException) {
//            sourceBackgroundInstitutionEthnicMakeUp.save( failOnError: true, flush: true )
//		}
//	}
//
//	void testUpdateValidSourceBackgroundInstitutionEthnicMakeUp() {
//		def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
//		sourceBackgroundInstitutionEthnicMakeUp.save( failOnError: true, flush: true )
//        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.id
//        assertEquals 0L, sourceBackgroundInstitutionEthnicMakeUp.version
//        assertEquals i_success_demographicYear, sourceBackgroundInstitutionEthnicMakeUp.demographicYear
//        assertEquals i_success_ethnicPercent, sourceBackgroundInstitutionEthnicMakeUp.ethnicPercent
//
//		//Update the entity
//		sourceBackgroundInstitutionEthnicMakeUp.ethnicPercent = u_success_ethnicPercent
//
//		sourceBackgroundInstitutionEthnicMakeUp.save( failOnError: true, flush: true )
//		//Assert for sucessful update
//        sourceBackgroundInstitutionEthnicMakeUp = SourceBackgroundInstitutionEthnicMakeUp.get( sourceBackgroundInstitutionEthnicMakeUp.id )
//        assertEquals 1L, sourceBackgroundInstitutionEthnicMakeUp?.version
//        assertEquals u_success_ethnicPercent, sourceBackgroundInstitutionEthnicMakeUp.ethnicPercent
//
//	}
//
//	void testUpdateInvalidSourceBackgroundInstitutionEthnicMakeUp() {
//		def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
//		sourceBackgroundInstitutionEthnicMakeUp.save( failOnError: true, flush: true )
//        assertNotNull sourceBackgroundInstitutionEthnicMakeUp.id
//        assertEquals 0L, sourceBackgroundInstitutionEthnicMakeUp.version
//        assertEquals i_success_demographicYear, sourceBackgroundInstitutionEthnicMakeUp.demographicYear
//        assertEquals i_success_ethnicPercent, sourceBackgroundInstitutionEthnicMakeUp.ethnicPercent
//
//		//Update the entity with invalid values
//		sourceBackgroundInstitutionEthnicMakeUp.ethnicPercent = u_failure_ethnicPercent
//
//		shouldFail(ValidationException) {
//            sourceBackgroundInstitutionEthnicMakeUp.save( failOnError: true, flush: true )
//		}
//	}
//
//    void testDates() {
//        def time = new SimpleDateFormat('HHmmss')
//        def hour = new SimpleDateFormat('HH')
//        def date = new SimpleDateFormat('yyyy-M-d')
//        def today = new Date()
//
//    	def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
//
//
//
//    	sourceBackgroundInstitutionEthnicMakeUp.save(flush: true, failOnError: true)
//    	sourceBackgroundInstitutionEthnicMakeUp.refresh()
//    	assertNotNull "SourceBackgroundInstitutionEthnicMakeUp should have been saved", sourceBackgroundInstitutionEthnicMakeUp.id
//
//    	// test date values -
//    	assertEquals date.format(today), date.format(sourceBackgroundInstitutionEthnicMakeUp.lastModified)
//    	assertEquals hour.format(today), hour.format(sourceBackgroundInstitutionEthnicMakeUp.lastModified)
//
//
//    }
//
//    void testOptimisticLock() {
//		def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
//		sourceBackgroundInstitutionEthnicMakeUp.save( failOnError: true, flush: true )
//
//        def sql
//        try {
//            sql = new Sql( sessionFactory.getCurrentSession().connection() )
//            sql.executeUpdate( "update SORBETH set SORBETH_VERSION = 999 where SORBETH_SURROGATE_ID = ?", [ sourceBackgroundInstitutionEthnicMakeUp.id ] )
//        } finally {
//            sql?.close() // note that the test will close the connection, since it's our current session's connection
//        }
//		//Try to update the entity
//		//Update the entity
//		sourceBackgroundInstitutionEthnicMakeUp.ethnicPercent = u_success_ethnicPercent
//        shouldFail( HibernateOptimisticLockingFailureException ) {
//            sourceBackgroundInstitutionEthnicMakeUp.save( failOnError: true, flush: true )
//        }
//    }
//
//	void testDeleteSourceBackgroundInstitutionEthnicMakeUp() {
//		def sourceBackgroundInstitutionEthnicMakeUp = newValidForCreateSourceBackgroundInstitutionEthnicMakeUp()
//		sourceBackgroundInstitutionEthnicMakeUp.save( failOnError: true, flush: true )
//		def id = sourceBackgroundInstitutionEthnicMakeUp.id
//		assertNotNull id
//		sourceBackgroundInstitutionEthnicMakeUp.delete()
//		assertNull SourceBackgroundInstitutionEthnicMakeUp.get( id )
//	}
//
//    void testValidation() {
//       def sourceBackgroundInstitutionEthnicMakeUp = newInvalidForCreateSourceBackgroundInstitutionEthnicMakeUp()
//       assertFalse "SourceBackgroundInstitutionEthnicMakeUp could not be validated as expected due to ${sourceBackgroundInstitutionEthnicMakeUp.errors}", sourceBackgroundInstitutionEthnicMakeUp.validate()
//    }
//
//    void testNullValidationFailure() {
//        def sourceBackgroundInstitutionEthnicMakeUp = new SourceBackgroundInstitutionEthnicMakeUp()
//        assertFalse "SourceBackgroundInstitutionEthnicMakeUp should have failed validation", sourceBackgroundInstitutionEthnicMakeUp.validate()
//        assertErrorsFor sourceBackgroundInstitutionEthnicMakeUp, 'nullable',
//                                               [
//                                                 'demographicYear',
//                                                 'sourceAndBackgroundInstitution',
//                                                 'ethnicity'
//                                               ]
//        assertNoErrorsFor sourceBackgroundInstitutionEthnicMakeUp,
//        									   [
//             									 'ethnicPercent'
//											   ]
//    }


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

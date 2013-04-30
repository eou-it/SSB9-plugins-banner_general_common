/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.general.system.TestScore


class SourceBackroundInstitutionTestScoreIntegrationTests extends BaseIntegrationTestCase {

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateValidSourceBackroundInstitutionTestScore() {
        def sourceBackroundInstitutionTestScore = newValidForCreateSourceBackroundInstitutionTestScore()
        sourceBackroundInstitutionTestScore.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sourceBackroundInstitutionTestScore.id
    }

//	void testCreateInvalidSourceBackroundInstitutionTestScore() {
//		def sourceBackroundInstitutionTestScore = newInvalidForCreateSourceBackroundInstitutionTestScore()
//		shouldFail(ValidationException) {
//            sourceBackroundInstitutionTestScore.save( failOnError: true, flush: true )
//		}
//	}
//
//	void testUpdateValidSourceBackroundInstitutionTestScore() {
//		def sourceBackroundInstitutionTestScore = newValidForCreateSourceBackroundInstitutionTestScore()
//		sourceBackroundInstitutionTestScore.save( failOnError: true, flush: true )
//        assertNotNull sourceBackroundInstitutionTestScore.id
//        assertEquals 0L, sourceBackroundInstitutionTestScore.version
//        assertEquals i_success_demographicYear, sourceBackroundInstitutionTestScore.demographicYear
//        assertEquals i_success_testScore, sourceBackroundInstitutionTestScore.testScore
//
//		//Update the entity
//		sourceBackroundInstitutionTestScore.testScore = u_success_testScore
//
//		sourceBackroundInstitutionTestScore.save( failOnError: true, flush: true )
//		//Assert for sucessful update
//        sourceBackroundInstitutionTestScore = SourceBackroundInstitutionTestScore.get( sourceBackroundInstitutionTestScore.id )
//        assertEquals 1L, sourceBackroundInstitutionTestScore?.version
//        assertEquals u_success_testScore, sourceBackroundInstitutionTestScore.testScore
//
//	}
//
//	void testUpdateInvalidSourceBackroundInstitutionTestScore() {
//		def sourceBackroundInstitutionTestScore = newValidForCreateSourceBackroundInstitutionTestScore()
//		sourceBackroundInstitutionTestScore.save( failOnError: true, flush: true )
//        assertNotNull sourceBackroundInstitutionTestScore.id
//        assertEquals 0L, sourceBackroundInstitutionTestScore.version
//        assertEquals i_success_demographicYear, sourceBackroundInstitutionTestScore.demographicYear
//        assertEquals i_success_testScore, sourceBackroundInstitutionTestScore.testScore
//
//		//Update the entity with invalid values
//		sourceBackroundInstitutionTestScore.testScore = u_failure_testScore
//
//		shouldFail(ValidationException) {
//            sourceBackroundInstitutionTestScore.save( failOnError: true, flush: true )
//		}
//	}
//
//    void testDates() {
//        def time = new SimpleDateFormat('HHmmss')
//        def hour = new SimpleDateFormat('HH')
//        def date = new SimpleDateFormat('yyyy-M-d')
//        def today = new Date()
//
//    	def sourceBackroundInstitutionTestScore = newValidForCreateSourceBackroundInstitutionTestScore()
//
//
//
//    	sourceBackroundInstitutionTestScore.save(flush: true, failOnError: true)
//    	sourceBackroundInstitutionTestScore.refresh()
//    	assertNotNull "SourceBackroundInstitutionTestScore should have been saved", sourceBackroundInstitutionTestScore.id
//
//    	// test date values -
//    	assertEquals date.format(today), date.format(sourceBackroundInstitutionTestScore.lastModified)
//    	assertEquals hour.format(today), hour.format(sourceBackroundInstitutionTestScore.lastModified)
//
//
//    }
//
//    void testOptimisticLock() {
//		def sourceBackroundInstitutionTestScore = newValidForCreateSourceBackroundInstitutionTestScore()
//		sourceBackroundInstitutionTestScore.save( failOnError: true, flush: true )
//
//        def sql
//        try {
//            sql = new Sql( sessionFactory.getCurrentSession().connection() )
//            sql.executeUpdate( "update SORBTST set SORBTST_VERSION = 999 where SORBTST_SURROGATE_ID = ?", [ sourceBackroundInstitutionTestScore.id ] )
//        } finally {
//            sql?.close() // note that the test will close the connection, since it's our current session's connection
//        }
//		//Try to update the entity
//		//Update the entity
//		sourceBackroundInstitutionTestScore.testScore = u_success_testScore
//        shouldFail( HibernateOptimisticLockingFailureException ) {
//            sourceBackroundInstitutionTestScore.save( failOnError: true, flush: true )
//        }
//    }
//
//	void testDeleteSourceBackroundInstitutionTestScore() {
//		def sourceBackroundInstitutionTestScore = newValidForCreateSourceBackroundInstitutionTestScore()
//		sourceBackroundInstitutionTestScore.save( failOnError: true, flush: true )
//		def id = sourceBackroundInstitutionTestScore.id
//		assertNotNull id
//		sourceBackroundInstitutionTestScore.delete()
//		assertNull SourceBackroundInstitutionTestScore.get( id )
//	}
//
//    void testValidation() {
//       def sourceBackroundInstitutionTestScore = newInvalidForCreateSourceBackroundInstitutionTestScore()
//       assertFalse "SourceBackroundInstitutionTestScore could not be validated as expected due to ${sourceBackroundInstitutionTestScore.errors}", sourceBackroundInstitutionTestScore.validate()
//    }
//
//    void testNullValidationFailure() {
//        def sourceBackroundInstitutionTestScore = new SourceBackroundInstitutionTestScore()
//        assertFalse "SourceBackroundInstitutionTestScore should have failed validation", sourceBackroundInstitutionTestScore.validate()
//        assertErrorsFor sourceBackroundInstitutionTestScore, 'nullable',
//                                               [
//                                                 'demographicYear',
//                                                 'testScore',
//                                                 'sourceAndBackgroundInstitution',
//                                                 'testScore'
//                                               ]
//    }


    private def newValidForCreateSourceBackroundInstitutionTestScore() {
        def sourceBackroundInstitutionTestScore = new SourceBackroundInstitutionTestScore(
                demographicYear: 2014,
                meanTestScore: 100,
                sourceAndBackgroundInstitution: SourceAndBackgroundInstitution.findWhere(code: "999999"),
                testScore: TestScore.findWhere(code: "JL"),
        )
        return sourceBackroundInstitutionTestScore
    }


    private def newInvalidForCreateSourceBackroundInstitutionTestScore() {
        def sourceBackroundInstitutionTestScore = new SourceBackroundInstitutionTestScore(
                demographicYear: null,
                meanTestScore: null,
                sourceAndBackgroundInstitution: null,
                testScore: null,
        )
        return sourceBackroundInstitutionTestScore
    }
}

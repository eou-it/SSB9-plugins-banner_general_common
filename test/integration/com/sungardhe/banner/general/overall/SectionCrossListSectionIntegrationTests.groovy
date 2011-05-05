
/*******************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 *******************************************************************************/
 
package com.sungardhe.banner.general.overall

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import com.sungardhe.banner.general.system.Term



class SectionCrossListSectionIntegrationTests extends BaseIntegrationTestCase {


	
	protected void setUp() {
		formContext = ['SSAXLST', 'SSAXLSQ'] // Since we are not testing a controller, we need to explicitly set this
		super.setUp()
	}

	protected void tearDown() {
		super.tearDown()
	}

	void testCreateSectionCrossListSection() {
		def sectionCrossListSection = newSectionCrossListSection()
		save sectionCrossListSection
		//Test if the generated entity now has an id assigned		
        assertNotNull sectionCrossListSection.id
	}

	void testUpdateSectionCrossListSection() {
		def sectionCrossListSection = newSectionCrossListSection()
		save sectionCrossListSection
       
        assertNotNull sectionCrossListSection.id
        assertEquals 0L, sectionCrossListSection.version
        assertEquals "TT", sectionCrossListSection.xlstGroup
        assertEquals "TTTTT", sectionCrossListSection.courseReferenceNumber
        
		//Update the entity
		def testDate = new Date()
		sectionCrossListSection.xlstGroup = "UU"
		sectionCrossListSection.courseReferenceNumber = "UUUUU"
		sectionCrossListSection.lastModified = testDate
		sectionCrossListSection.lastModifiedBy = "test"
		sectionCrossListSection.dataOrigin = "Banner" 
        save sectionCrossListSection
        
        sectionCrossListSection = SectionCrossListSection.get( sectionCrossListSection.id )
        assertEquals 1L, sectionCrossListSection?.version
        assertEquals "UU", sectionCrossListSection.xlstGroup
        assertEquals "UUUUU", sectionCrossListSection.courseReferenceNumber
	}

    void testOptimisticLock() { 
		def sectionCrossListSection = newSectionCrossListSection()
		save sectionCrossListSection
        
        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update SSRXLST set SSRXLST_VERSION = 999 where SSRXLST_SURROGATE_ID = ?", [ sectionCrossListSection.id ] )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
		//Try to update the entity
		sectionCrossListSection.xlstGroup="UU"
		sectionCrossListSection.courseReferenceNumber="UUUUU"
		sectionCrossListSection.lastModified= new Date()
		sectionCrossListSection.lastModifiedBy="test"
		sectionCrossListSection.dataOrigin= "Banner" 
        shouldFail( HibernateOptimisticLockingFailureException ) {
            sectionCrossListSection.save( flush: true )
        }
    }
	
	void testDeleteSectionCrossListSection() {
		def sectionCrossListSection = newSectionCrossListSection()
		save sectionCrossListSection
		def id = sectionCrossListSection.id
		assertNotNull id
		sectionCrossListSection.delete()
		assertNull SectionCrossListSection.get( id )
	}
	
    void testValidation() {
       def sectionCrossListSection = newSectionCrossListSection()
       assertTrue "SectionCrossListSection could not be validated as expected due to ${sectionCrossListSection.errors}", sectionCrossListSection.validate()
    }

    void testNullValidationFailure() {
      def sectionCrossListSection = new SectionCrossListSection(
              xlstGroup: null,
              courseReferenceNumber: null,
              term: null,
              lastModified: new Date(),
              lastModifiedBy: "test",
              dataOrigin: "Banner"
          )
        assertFalse "SectionCrossListSection should have failed validation", sectionCrossListSection.validate()
        assertErrorsFor sectionCrossListSection, 'nullable', 
                                               [ 
                                                 'xlstGroup', 
                                                 'courseReferenceNumber',
                                                 'term'
                                               ]
    }
    
    
	void testValidationMessages() {
	    def sectionCrossListSection = newSectionCrossListSection()
	    sectionCrossListSection.xlstGroup = null
	    assertFalse sectionCrossListSection.validate()
	    assertLocalizedError sectionCrossListSection, 'nullable', /.*Field.*xlstGroup.*of class.*SectionCrossListSection.*cannot be null.*/, 'xlstGroup'
	    sectionCrossListSection.courseReferenceNumber = null
	    assertFalse sectionCrossListSection.validate()
	    assertLocalizedError sectionCrossListSection, 'nullable', /.*Field.*courseReferenceNumber.*of class.*SectionCrossListSection.*cannot be null.*/, 'courseReferenceNumber'
	}
  
    
  private def newSectionCrossListSection() {
    def term = Term.findWhere(code: "201410")

    def sectionCrossListSection = new SectionCrossListSection(
    		xlstGroup: "TT", 
    		courseReferenceNumber: "TTTTT", 
			term: term, 
            lastModified: new Date(),
			lastModifiedBy: "test", 
			dataOrigin: "Banner"
        )
        return sectionCrossListSection
    }

   /**
     * Please put all the custom tests in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(sectioncrosslistsection_custom_integration_test_methods) ENABLED START*/
    /*PROTECTED REGION END*/
}

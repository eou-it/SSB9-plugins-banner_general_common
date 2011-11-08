/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/

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
        assertNotNull sectionCrossListSection.lastModified
        assertNotNull sectionCrossListSection.lastModifiedBy
        assertNotNull sectionCrossListSection.dataOrigin
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

        sectionCrossListSection = SectionCrossListSection.get(sectionCrossListSection.id)
        assertEquals 1L, sectionCrossListSection?.version
        assertEquals "UU", sectionCrossListSection.xlstGroup
        assertEquals "UUUUU", sectionCrossListSection.courseReferenceNumber
    }


    void testOptimisticLock() {
        def sectionCrossListSection = newSectionCrossListSection()
        save sectionCrossListSection

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update SSRXLST set SSRXLST_VERSION = 999 where SSRXLST_SURROGATE_ID = ?", [sectionCrossListSection.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        sectionCrossListSection.xlstGroup = "UU"
        sectionCrossListSection.courseReferenceNumber = "UUUUU"
        sectionCrossListSection.lastModified = new Date()
        sectionCrossListSection.lastModifiedBy = "test"
        sectionCrossListSection.dataOrigin = "Banner"
        shouldFail(HibernateOptimisticLockingFailureException) {
            sectionCrossListSection.save(flush: true, failOnError: true)
        }
    }


    void testDeleteSectionCrossListSection() {
        def sectionCrossListSection = newSectionCrossListSection()
        save sectionCrossListSection
        def id = sectionCrossListSection.id
        assertNotNull id
        sectionCrossListSection.delete()
        assertNull SectionCrossListSection.get(id)
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



    private def newSectionCrossListSection() {
        def term = Term.findWhere(code: "201410")

        def sectionCrossListSection = new SectionCrossListSection(
                xlstGroup: "TT",
                courseReferenceNumber: "TTTTT",
                term: term
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

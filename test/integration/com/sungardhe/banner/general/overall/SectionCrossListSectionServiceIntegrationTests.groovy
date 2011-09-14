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
import com.sungardhe.banner.exceptions.ApplicationException

import com.sungardhe.banner.general.system.Term
import groovy.sql.Sql


class SectionCrossListSectionServiceIntegrationTests extends BaseIntegrationTestCase {

    def sectionCrossListSectionService


    protected void setUp() {
        formContext = ['SSAXLST', 'SSAXLSQ']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateSectionCrossListSection() {
        def sectionCrossListSection = newSectionCrossListSection()
        def keyBlockMap = [term: "201410", xlstGroup: "B1"]
        def map = [domainModel: sectionCrossListSection, keyBlock: keyBlockMap]
        sectionCrossListSection = sectionCrossListSectionService.create(map)
        assertNotNull "SectionCrossListSection ID is null in SectionCrossListSection Service Tests Create", sectionCrossListSection.id
        assertNotNull sectionCrossListSection.dataOrigin
        assertNotNull sectionCrossListSection.lastModifiedBy
        assertNotNull sectionCrossListSection.lastModified
        assertNotNull "SectionCrossListSection term is null in SectionCrossListSection Service Tests", sectionCrossListSection.term
    }


    void testSectionCrossListSectionInvalidCreate() {
        def sectionCrossListSection = newSectionCrossListSection()
        def keyBlockMap = [term: "999999", xlstGroup: "TT"]
        def map = [domainModel: sectionCrossListSection, keyBlock: keyBlockMap]

        try {
            sectionCrossListSectionService.create(map)
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "invalid.code.message:Term"
        }
    }


    void testUpdateWithDuplicateSection() {
        def term = Term.findWhere(code: "201410")

        def sectionCrossListSection = new SectionCrossListSection(
                xlstGroup: "B1",
                courseReferenceNumber: "20165",
                term: term
        )
        def keyBlockMap = [term: "201410", xlstGroup: "B1"]
        def map = [domainModel: sectionCrossListSection, keyBlock: keyBlockMap]

        try {
            sectionCrossListSectionService.create(map)
        }
        catch (ApplicationException e) {
            assertApplicationException e, "section_already_cross_listed"
        }
    }


    void testInsertWithBlankSection() {
        def term = Term.findWhere(code: "201410")

        def sectionCrossListSection = new SectionCrossListSection(
                xlstGroup: "B1" ,
                term: term
        )
        def keyBlockMap = [term: "201410", xlstGroup: "B1"]
        def map = [domainModel: sectionCrossListSection, keyBlock: keyBlockMap]

        try {
            sectionCrossListSectionService.create(map)
        }
        catch (ApplicationException e) {
            assertApplicationException e, "crn_required"
        }
    }


    void testSectionCrossListSectionDelete() {
        def sectionCrossListSection = newSectionCrossListSection()
        def keyBlockMap = [term: "201410", xlstGroup: "B1"]
        def map = [domainModel: sectionCrossListSection, keyBlock: keyBlockMap]
        sectionCrossListSection = sectionCrossListSectionService.create(map)

        map.domainModel = sectionCrossListSection

        sectionCrossListSectionService.delete(map)
        def found
        try {
            found = sectionCrossListSectionService.read(sectionCrossListSection.id)
            fail "ApplicationException expect a read fail"
        } catch (ApplicationException e) {

            assertApplicationException e, "NotFoundException"
        }

        assertNull(found)
    }


    void testSectionCrossListSectionDeleteOneOfMany() {
        def sectionCrossListSection = newSectionCrossListSection()
        def keyBlockMap = [term: "201410", xlstGroup: "B1"]
        def map = [domainModel: sectionCrossListSection, keyBlock: keyBlockMap]
        sectionCrossListSection = sectionCrossListSectionService.create(map)

        assertNotNull findSection("201410", "20008")
        sectionCrossListSection = newSectionCrossListSection()
        sectionCrossListSection.courseReferenceNumber = "20008"
        map.domainModel = sectionCrossListSection
        sectionCrossListSection = sectionCrossListSectionService.create(map)

        def crossLists = SectionCrossListSection.findAllByTermAndXlstGroup(Term.findByCode("201410"), "B1")
        assertTrue crossLists.size() > 1
        def originalSize = crossLists.size()
        map = [domainModel: sectionCrossListSection, keyBlock: keyBlockMap]
        sectionCrossListSectionService.delete(map)
        crossLists = SectionCrossListSection.findAllByTermAndXlstGroup(Term.findByCode("201410"), "B1")
        assertEquals crossLists.size(), originalSize - 1

    }

    /**
     * Test for the ReadOnly field "XLST Group"
     */

    void testReadOnlyXlstGroup() {
        def sectionCrossListSection = newSectionCrossListSection()
        def keyBlockMap = [term: sectionCrossListSection.term.code,
                courseReferenceNumber: sectionCrossListSection.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionCrossListSection]

        sectionCrossListSection = sectionCrossListSectionService.create(map)
        //Update the entity readonly field
        sectionCrossListSection.xlstGroup = "update"
        map.domainModel = sectionCrossListSection
        try {
            sectionCrossListSectionService.update(map)
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }

    /**
     * Test for the ReadOnly field "Term"
     */

    void testReadOnlyTerm() {
        def sectionCrossListSection = newSectionCrossListSection()
        def keyBlockMap = [term: sectionCrossListSection.term.code,
                courseReferenceNumber: sectionCrossListSection.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionCrossListSection]

        sectionCrossListSection = sectionCrossListSectionService.create(map)
        //Update the entity readonly field
        sectionCrossListSection.term = Term.findByCode("201430")
        map.domainModel = sectionCrossListSection
        try {
            sectionCrossListSectionService.update(map)
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }

    /**
     * Test for the ReadOnly field "CourseReferenceNumber"
     */

    void testReadOnlyCourseReferenceNumber() {
        def sectionCrossListSection = newSectionCrossListSection()
        def keyBlockMap = [term: sectionCrossListSection.term.code,
                courseReferenceNumber: sectionCrossListSection.courseReferenceNumber]
        def map = [keyBlock: keyBlockMap,
                domainModel: sectionCrossListSection]

        sectionCrossListSection = sectionCrossListSectionService.create(map)

        sectionCrossListSection.courseReferenceNumber = "20113"
        map.domainModel = sectionCrossListSection
        try {
            sectionCrossListSectionService.update(map)
            fail("This should have failed with @@r1:readonlyFieldsCannotBeModified")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "readonlyFieldsCannotBeModified"
        }
    }


    private def newSectionCrossListSection() {
        def term = Term.findWhere(code: "201410")
        assertNotNull findSection(term.code, "20344")

        def sectionCrossListSection = new SectionCrossListSection(
                xlstGroup: "B1",
                courseReferenceNumber: "20344",
                term: term
        )
        return sectionCrossListSection
    }


    private def findSection(String term, String crn) {
        def sql
        def foundCrn = null
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            foundCrn = sql.firstRow("select ssbsect_crn from ssbsect where ssbsect_crn = ? and ssbsect_term_code = ?", [crn, term])

        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        return foundCrn
    }

    /**
     * Please put all the custom service tests in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(sectioncrosslistsection_custom_service_integration_test_methods) ENABLED START*/
    /*PROTECTED REGION END*/
}  

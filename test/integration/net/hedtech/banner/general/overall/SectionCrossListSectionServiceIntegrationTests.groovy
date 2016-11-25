/*********************************************************************************
 Copyright 2010-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException

import net.hedtech.banner.general.system.Term
import groovy.sql.Sql


class SectionCrossListSectionServiceIntegrationTests extends BaseIntegrationTestCase {

    def sectionCrossListSectionService


    @Before
    public void setUp() {
        formContext = ['GUAGMNU', 'SSAXLST', 'SSAXLSQ']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
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


    @Test
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


    @Test
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


    @Test
    void testInsertWithBlankSection() {
        def term = Term.findWhere(code: "201410")

        def sectionCrossListSection = new SectionCrossListSection(
                xlstGroup: "B1",
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


    @Test
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


    @Test
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

    @Test
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

    @Test
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

    @Test
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


    @Test
    void testFetchByTermAndCrossListGroupIndicator() {
        def sectionCrossListSections = SectionCrossListSection.fetchByTermAndXlstGroup('201410', 'B1')
        assertTrue sectionCrossListSections.size() >= 1
        assertEquals sectionCrossListSections[0].courseReferenceNumber, '20165'
        assertEquals sectionCrossListSections[1].courseReferenceNumber, '20334'
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

}

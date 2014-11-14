/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import net.hedtech.banner.general.system.Term


class SectionCrossListSectionIntegrationTests extends BaseIntegrationTestCase {


	@Before
	public void setUp() {
        formContext = ['SSAXLST', 'SSAXLSQ'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }


	@After
	public void tearDown() {
        super.tearDown()
    }


	@Test
    void testCreateSectionCrossListSection() {
        def sectionCrossListSection = newSectionCrossListSection()
        sectionCrossListSection.save(flush: true, failOnError: true)
        //Test if the generated entity now has an id assigned
        assertNotNull sectionCrossListSection.id
        assertNotNull sectionCrossListSection.lastModified
        assertNotNull sectionCrossListSection.lastModifiedBy
        assertNotNull sectionCrossListSection.dataOrigin
    }


	@Test
     void testFetchByTermAndCourseReferenceNumber() {
        def sectionCrossListSection = newSectionCrossListSection()
        sectionCrossListSection.save(flush: true, failOnError: true)

        def sections = SectionCrossListSection.fetchByTermAndCourseReferenceNumber("201410", sectionCrossListSection.courseReferenceNumber)
        assertTrue sections.size() == 1
    }

	@Test
    void testUpdateSectionCrossListSection() {
        def sectionCrossListSection = newSectionCrossListSection()
        sectionCrossListSection.save(flush: true, failOnError: true)

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
        sectionCrossListSection.save(flush: true, failOnError: true)

        sectionCrossListSection = SectionCrossListSection.get(sectionCrossListSection.id)
        assertEquals 1L, sectionCrossListSection?.version
        assertEquals "UU", sectionCrossListSection.xlstGroup
        assertEquals "UUUUU", sectionCrossListSection.courseReferenceNumber
    }


	@Test
    void testOptimisticLock() {
        def sectionCrossListSection = newSectionCrossListSection()
        sectionCrossListSection.save(flush: true, failOnError: true)

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


	@Test
    void testDeleteSectionCrossListSection() {
        def sectionCrossListSection = newSectionCrossListSection()
        sectionCrossListSection.save(flush: true, failOnError: true)
        def id = sectionCrossListSection.id
        assertNotNull id
        sectionCrossListSection.delete()
        assertNull SectionCrossListSection.get(id)
    }


	@Test
    void testValidation() {
        def sectionCrossListSection = newSectionCrossListSection()
        assertTrue "SectionCrossListSection could not be validated as expected due to ${sectionCrossListSection.errors}", sectionCrossListSection.validate()
    }


	@Test
    void testNullValidationFailure() {
        def sectionCrossListSection = new SectionCrossListSection()
        assertFalse "SectionCrossListSection should have failed validation", sectionCrossListSection.validate()
        assertErrorsFor sectionCrossListSection, 'nullable',
                        [
                        'xlstGroup',
                        'courseReferenceNumber',
                        'term'
                        ]
    }


	@Test
    void testMaxSizeValidationFailures() {
        def sectionCrossListSection = new SectionCrossListSection(
                xlstGroup: 'XXX',
                courseReferenceNumber: 'XXXXXX')
        assertFalse "SectionCrossListSection should have failed validation", sectionCrossListSection.validate()
        assertErrorsFor sectionCrossListSection, 'maxSize',
                        [
                        'xlstGroup',
                        'courseReferenceNumber'
                        ]
    }


    private def newSectionCrossListSection() {
        def term = Term.findByCode ( "201410")

        def sectionCrossListSection = new SectionCrossListSection(
                xlstGroup: "TT",
                courseReferenceNumber: "TTTTT",
                term: term
        )
        return sectionCrossListSection
    }


}

/*********************************************************************************
  Copyright 2010-2019 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Before
import org.junit.Test
import org.junit.After
import static groovy.test.GroovyAssert.*
import net.hedtech.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException
import net.hedtech.banner.general.system.Term


@Integration
@Rollback
class SectionCrossListSectionIntegrationTests extends BaseIntegrationTestCase {


    @Before
    public void setUp() {
        formContext = ['GUAGMNU','SSAXLST', 'SSAXLSQ'] // Since we are not testing a controller, we need to explicitly set this
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
        assertEquals "B1", sectionCrossListSection.xlstGroup
        assertEquals "TTTTT", sectionCrossListSection.courseReferenceNumber

        //Update the entity
        def testDate = new Date()
        sectionCrossListSection.xlstGroup = "J1"
        sectionCrossListSection.courseReferenceNumber = "UUUUU"
        sectionCrossListSection.lastModified = testDate
        sectionCrossListSection.lastModifiedBy = "test"
        sectionCrossListSection.dataOrigin = "Banner"
        sectionCrossListSection.save(flush: true, failOnError: true)

        sectionCrossListSection = SectionCrossListSection.get(sectionCrossListSection.id)
        assertEquals 1L, sectionCrossListSection?.version
        assertEquals "J1", sectionCrossListSection.xlstGroup
        assertEquals "UUUUU", sectionCrossListSection.courseReferenceNumber
    }

    @Test
    void testValidateSectionCrossListXlstGroup() {
        def sectionCrossListSection = newSectionCrossListXlstGroupWithFifteenChar()
        sectionCrossListSection.save(flush: true, failOnError: true)

        assertNotNull sectionCrossListSection.id
        assertEquals 0L, sectionCrossListSection.version
        assertEquals "WWWWWWWWWWWWWWW", sectionCrossListSection.xlstGroup
        assertEquals "TTTTT", sectionCrossListSection.courseReferenceNumber

        //Update the entity
        def testDate = new Date()
        sectionCrossListSection.xlstGroup = "123456789ABCDEF"
        sectionCrossListSection.courseReferenceNumber = "UUUUU"
        sectionCrossListSection.lastModified = testDate
        sectionCrossListSection.lastModifiedBy = "test"
        sectionCrossListSection.dataOrigin = "Banner"
        sectionCrossListSection.save(flush: true, failOnError: true)

        sectionCrossListSection = SectionCrossListSection.get(sectionCrossListSection.id)
        assertEquals 1L, sectionCrossListSection?.version
        assertEquals "123456789ABCDEF", sectionCrossListSection.xlstGroup
        assertEquals "UUUUU", sectionCrossListSection.courseReferenceNumber
    }

    @Test
    void testUpdateSectionCrossListXlstGroupToMaxLength() {
        def sectionCrossListSection = newSectionCrossListXlstGroupWithTwoChar()
        sectionCrossListSection.save(flush: true, failOnError: true)

        assertNotNull sectionCrossListSection.id
        assertEquals 0L, sectionCrossListSection.version
        assertEquals "A1", sectionCrossListSection.xlstGroup
        assertEquals "TTTTT", sectionCrossListSection.courseReferenceNumber

        //Update the entity
        def testDate = new Date()
        sectionCrossListSection.xlstGroup = "WWWWWWWWWWWWWWW"
        sectionCrossListSection.courseReferenceNumber = "UUUUU"
        sectionCrossListSection.lastModified = testDate
        sectionCrossListSection.lastModifiedBy = "test"
        sectionCrossListSection.dataOrigin = "Banner"
        sectionCrossListSection.save(flush: true, failOnError: true)

        sectionCrossListSection = SectionCrossListSection.get(sectionCrossListSection.id)
        assertEquals 1L, sectionCrossListSection?.version
        assertEquals "WWWWWWWWWWWWWWW", sectionCrossListSection.xlstGroup
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
//            sql?.close() // note that the test will close the connection, since it's our current session's connection
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
                xlstGroup: 'XXXXXXXXXXXXXXXX',
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
                xlstGroup: "B1",
                courseReferenceNumber: "TTTTT",
                term: term
        )
        return sectionCrossListSection
    }

    private def newSectionCrossListXlstGroupWithFifteenChar() {
        def term = Term.findByCode ( "201842")

        def sectionCrossListSection = new SectionCrossListSection(
                xlstGroup: "WWWWWWWWWWWWWWW",
                courseReferenceNumber: "TTTTT",
                term: term
        )
        return sectionCrossListSection
    }

    private def newSectionCrossListXlstGroupWithTwoChar() {
        def term = Term.findByCode ( "201842")

        def sectionCrossListSection = new SectionCrossListSection(
                xlstGroup: "A1",
                courseReferenceNumber: "TTTTT",
                term: term
        )
        return sectionCrossListSection
    }

}

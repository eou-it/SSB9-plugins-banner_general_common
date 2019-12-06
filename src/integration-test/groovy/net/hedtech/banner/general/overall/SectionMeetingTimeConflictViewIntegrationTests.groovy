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
import org.springframework.dao.InvalidDataAccessResourceUsageException

@Integration
@Rollback
class SectionMeetingTimeConflictViewIntegrationTests extends net.hedtech.banner.testing.BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testFetchConflict() {
        def meeting20201 = SectionMeetingTime.findByTermAndCourseReferenceNumber("201410", "20201")
        def meeting20228 = SectionMeetingTime.findByTermAndCourseReferenceNumber("201410", "20228")
        assertEquals meeting20201.beginTime, meeting20228.beginTime
        assertEquals meeting20201.endTime, meeting20228.endTime
        assertEquals meeting20201.tuesday, meeting20228.tuesday

        def findResults = SectionMeetingTimeConflictView.fetchByTermAndCourseReferenceNumber(
                "201410", "20201", "20228")
        assertNotNull findResults[0]

    }

/**
 * Prove that you cannot save
 */

    @Test
    void testCreateExceptionResults() {

        SectionMeetingTimeConflictView newSectionList = new SectionMeetingTimeConflictView()
        newSectionList.courseReferenceNumber = '99999'
        newSectionList.courseReferenceNumberConflict = '99999'
        newSectionList.term = '999999'
        newSectionList.version = new Long(999)
        newSectionList.id = 2222222
        shouldFail(InvalidDataAccessResourceUsageException) {
            newSectionList.save(flush: true, failOnError: true)
        }
    }


    @Test
    void testUpdateExceptionResults() {
        def existingSection = SectionMeetingTimeConflictView.fetchByTermAndCourseReferenceNumber(
                "201410", "20201", "20228")[0]
        assertNotNull existingSection
        existingSection.version = new Long(1)
        existingSection.courseReferenceNumberConflict = "New Value"
        existingSection.courseReferenceNumber = "New Value"
        existingSection.term = "New Value"
        shouldFail(InvalidDataAccessResourceUsageException) {
            existingSection.save(flush: true, failOnError: true)
        }
    }


    @Test
    void testDeleteExceptionResults() {
        def existingSection = SectionMeetingTimeConflictView.fetchByTermAndCourseReferenceNumber(
                "201410", "20201", "20228") [0]
        assertNotNull existingSection
        shouldFail(InvalidDataAccessResourceUsageException) {
            existingSection.delete(flush: true, failOnError: true)
        }
    }

}

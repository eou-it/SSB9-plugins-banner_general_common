/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.general.overall

import org.springframework.dao.InvalidDataAccessResourceUsageException

class SectionMeetingTimeConflictViewIntegrationTests extends net.hedtech.banner.testing.BaseIntegrationTestCase {

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


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

    void testCreateExceptionResults() {

        SectionMeetingTimeConflictView newSectionList = new SectionMeetingTimeConflictView()
        newSectionList.courseReferenceNumber = '20002'
        newSectionList.courseReferenceNumberConflict = '20202'
        newSectionList.term = '201410'
        newSectionList.version = 0
        newSectionList.id = 2222222
        shouldFail(InvalidDataAccessResourceUsageException) {
            newSectionList.save(flush: true, onError: true)
        }
    }


    void testUpdateExceptionResults() {
        def existingSection = SectionMeetingTimeConflictView.fetchByTermAndCourseReferenceNumber(
                "201410", "20201", "20228")[0]
        assertNotNull existingSection
        existingSection.version = new Long(1)
        shouldFail(InvalidDataAccessResourceUsageException) {
            existingSection.save(flush: true, onError: true)
        }
    }


    void testDeleteExceptionResults() {
        def existingSection = SectionMeetingTimeConflictView.fetchByTermAndCourseReferenceNumber(
                "201410", "20201", "20228") [0]
        assertNotNull existingSection
        shouldFail(InvalidDataAccessResourceUsageException) {
            existingSection.delete(flush: true, onError: true)
        }
    }

}

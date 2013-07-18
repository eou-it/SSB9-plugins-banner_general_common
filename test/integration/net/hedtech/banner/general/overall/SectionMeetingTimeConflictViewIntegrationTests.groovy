/** *******************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 ********************************************************************************* */

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

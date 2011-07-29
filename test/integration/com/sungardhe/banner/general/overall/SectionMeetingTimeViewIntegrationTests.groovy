/** *****************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

package com.sungardhe.banner.general.overall

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import org.springframework.dao.InvalidDataAccessResourceUsageException

class SectionMeetingTimeViewIntegrationTests extends BaseIntegrationTestCase {

    protected void setUp() {
        formContext = ['SSAMATX']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testFetchByTermAndCourseReferenceNumberExistingRecord() {

        def findResults = SectionMeetingTimeView.findAllByTermAndCourseReferenceNumber(
                "201410", "20001")
        assertNotNull findResults[0]
        def result = SectionMeetingTimeView.fetchByTermAndCourseReferenceNumber(
                "201410", "20001")

        assertEquals 1, result.size()
        assertTrue result[0].monday
        assertTrue result[0].wednesday
        assertTrue result[0].friday
        assertFalse result[0].saturday
        assertFalse result[0].sunday
        assertFalse result[0].tuesday
        assertFalse result[0].thursday
        assertEquals "0900", result[0].beginTime
        assertEquals "1000", result[0].endTime
    }

    /**
     * Prove that you cannot save
     */

    void testCreateExceptionResults() {
        def existingSection = SectionMeetingTimeView.findAllByTermAndCourseReferenceNumber(
                "201410", "20001")[0]
        assertNotNull existingSection
        SectionMeetingTimeView newSectionList = new SectionMeetingTimeView(existingSection.properties)
        newSectionList.courseReferenceNumber = '20002'
        newSectionList.version = 0
        newSectionList.id = 2222222
        shouldFail(InvalidDataAccessResourceUsageException) {
            newSectionList.save(flush: true, onError: true)
        }

    }


    void testUpdateExceptionResults() {
        def existingSection = SectionMeetingTimeView.findByTermAndCourseReferenceNumber(
                "201410", "20001")
        assertNotNull existingSection
        existingSection.version = new Long(1)
        shouldFail(InvalidDataAccessResourceUsageException) {
            existingSection.save(flush: true, onError: true)
        }
    }


    void testDeleteExceptionResults() {
        def existingSection = SectionMeetingTimeView.findByTermAndCourseReferenceNumber(
                "201410", "20001")
        assertNotNull existingSection
        shouldFail(InvalidDataAccessResourceUsageException) {
            existingSection.delete(flush: true, onError: true)
        }
    }
}

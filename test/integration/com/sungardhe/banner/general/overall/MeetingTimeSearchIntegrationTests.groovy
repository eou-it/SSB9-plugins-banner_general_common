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

package com.sungardhe.banner.general.overall

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import org.springframework.jdbc.UncategorizedSQLException

class MeetingTimeSearchIntegrationTests extends BaseIntegrationTestCase {

    protected void setUp() {
        formContext = ['SSASECQ']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCrnList() {

        def term = "201410"

        // section has two meetings
        def crn = "20199"
        def courseLists = MeetingTimeSearch.findAllByTermAndCourseReferenceNumber(term, crn)
        assertEquals courseLists.size(), 2

        // section does not have any meetings
        crn = "20408"
        courseLists = MeetingTimeSearch.findAllByTermAndCourseReferenceNumber(term, crn)
        assertEquals courseLists.size(), 0

    }


    void testFetch() {
        Map filterData = [courseReferenceNumber: "20199", term: "201410"]
        def meetings = MeetingTimeSearch.fetchSearch(filterData)
        assertEquals meetings.size(), 2
        assertEquals meetings[0].category, "03"
        assertEquals meetings[1].category, "04"

        def countMeetings = MeetingTimeSearch.countAll(filterData)
        assertEquals countMeetings, 2

        filterData = [monday: true, wednesday: true, beginTime: "0800", endTime: '0900',  term: "201410"]
        meetings = MeetingTimeSearch.fetchSearch(filterData)
        meetings.each { meet ->
            assertTrue meet.monday
            assertTrue meet.wednesday
            assertTrue meet.beginTime >=  "0800" && meet.endTime <= "0900"
        }

        filterData = [monday: true, wednesday: true, friday:"not true", term: "201410"]
        meetings = MeetingTimeSearch.fetchSearch(filterData)
        meetings.each { meet ->
            assertTrue meet.monday
            assertTrue meet.wednesday
            assertFalse meet.friday
        }

        filterData = [tuesday: true, thursday: true, beginTime: "1000",  endTime: "1100", term: "201410"]
        meetings = MeetingTimeSearch.fetchSearch(filterData)
        meetings.each { meet ->
            assertTrue meet.tuesday
            assertTrue meet.thursday
            assertTrue meet.beginTime >=  "1000" && meet.endTime <= "1100"
        }
        filterData = [friday: true, term: "201410"]
        meetings = MeetingTimeSearch.fetchSearch(filterData)
        meetings.each { meet ->
            assertTrue meet.friday
        }
        filterData = [saturday: true, term: "201410"]
        meetings = MeetingTimeSearch.fetchSearch(filterData)
        meetings.each { meet ->
            assertTrue meet.saturday
        }
        filterData = [sunday: true, term: "201410"]
        meetings = MeetingTimeSearch.fetchSearch(filterData)
        meetings.each { meet ->
            assertTrue meet.sunday
        }
    }

    /**
     * Tests that view does not allow crud (create,update,delete) operations and is readonly
     */

    void testCreateExceptionResults() {
        def existingMeeting = MeetingTimeSearch.findByCourseReferenceNumberAndTerm("20001", "201410")
        assertNotNull existingMeeting
        MeetingTimeSearch newMeetingList = new MeetingTimeSearch(existingMeeting.properties)
        newMeetingList.courseReferenceNumber = '4444'
        newMeetingList.version = 0
        newMeetingList.id = 2222222
        shouldFail(UncategorizedSQLException) {
            newMeetingList.save(flush: true, onError: true)
        }

    }


    void testUpdateExceptionResults() {
        def existingMeeting = MeetingTimeSearch.findByCourseReferenceNumberAndTerm("20001", "201410")
        assertNotNull existingMeeting
        existingMeeting.room = "100"
        shouldFail(UncategorizedSQLException) {
            existingMeeting.save(flush: true, onError: true)
        }
    }


    void testDeleteExceptionResults() {
        def existingMeeting = MeetingTimeSearch.findByCourseReferenceNumberAndTerm("20001", "201410")
        assertNotNull existingMeeting
        shouldFail(UncategorizedSQLException) {
            existingMeeting.delete(flush: true, onError: true)
        }
    }

}

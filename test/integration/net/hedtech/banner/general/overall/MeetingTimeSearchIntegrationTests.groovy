/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.general.overall

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.jdbc.UncategorizedSQLException

class MeetingTimeSearchIntegrationTests extends BaseIntegrationTestCase {

    void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    void tearDown() {
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


    void testFetchWithParseQuery() {
        Map filterData = [courseReferenceNumber: "20199", term: "201410"]
        def meetings = MeetingTimeSearch.fetchSearch(filterData)
        assertEquals meetings.size(), 2
        assertEquals meetings[0].category, "03"
        assertEquals meetings[1].category, "04"

        def countMeetings = MeetingTimeSearch.countAll(filterData)
        assertEquals countMeetings, 2

        filterData = [monday: true, wednesday: true, beginTime: "0800", endTime: '0900', term: "201410"]
        meetings = MeetingTimeSearch.fetchSearch(filterData)
        meetings.each { meet ->
            assertTrue meet.monday
            assertTrue meet.wednesday
            assertTrue meet.beginTime >= "0800" && meet.endTime <= "0900"
        }

        filterData = [monday: true, wednesday: true, friday: "not true", term: "201410"]
        meetings = MeetingTimeSearch.fetchSearch(filterData)
        meetings.each { meet ->
            assertTrue meet.monday
            assertTrue meet.wednesday
            assertFalse meet.friday
        }

        filterData = [tuesday: true, thursday: true, beginTime: "1000", endTime: "1100", term: "201410"]
        meetings = MeetingTimeSearch.fetchSearch(filterData)
        meetings.each { meet ->
            assertTrue meet.tuesday
            assertTrue meet.thursday
            assertTrue meet.beginTime >= "1000" && meet.endTime <= "1100"
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


    void testFetchWithParseQueryString() {
        String.metaClass.flattenString = {
            return delegate.replace("\n", "").replaceAll(/  */, " ")
        }
        Map filterData = [courseReferenceNumber: "20199", term: "201410"]
        def meetingQuery = MeetingTimeSearch.parseQueryString(filterData, "sr", filterData)
        assertNotNull meetingQuery.query
        assertNotNull meetingQuery.filter
        def query = "from MeetingTimeSearch sr where ${meetingQuery.query}"
        def meetings = MeetingTimeSearch.findAll(query.flattenString(), meetingQuery.filter)
        assertEquals meetings.size(), 2
        assertEquals meetings[0].category, "03"
        assertEquals meetings[1].category, "04"

        def countMeetings = MeetingTimeSearch.countAll(filterData)
        assertEquals countMeetings, 2

        filterData = [monday: true, wednesday: true, beginTime: "0800", endTime: '0900', term: "201410"]
        meetingQuery = MeetingTimeSearch.parseQueryString(filterData, "sr", filterData)
        assertNotNull meetingQuery
        query = "from MeetingTimeSearch sr where ${meetingQuery.query}"
        meetings = MeetingTimeSearch.findAll(query.flattenString(), meetingQuery.filter)
        meetings.each { meet ->
            assertTrue meet.monday
            assertTrue meet.wednesday
            assertTrue meet.beginTime >= "0800" && meet.endTime <= "0900"
        }

        filterData = [monday: true, wednesday: true, friday: "not true", term: "201410"]
        meetingQuery = MeetingTimeSearch.parseQueryString(filterData, "sr", filterData)
        assertNotNull meetingQuery
        query = "from MeetingTimeSearch sr where ${meetingQuery.query}"
        meetings = MeetingTimeSearch.findAll(query.flattenString() , meetingQuery.filter)
        meetings.each { meet ->
            assertTrue meet.monday
            assertTrue meet.wednesday
            assertFalse meet.friday
        }

        filterData = [tuesday: true, thursday: true, beginTime: "1000", endTime: "1100", term: "201410"]
        meetingQuery = MeetingTimeSearch.parseQueryString(filterData, "sr", filterData)
        assertNotNull meetingQuery
        query = "from MeetingTimeSearch sr where ${meetingQuery.query}"
        meetings = MeetingTimeSearch.findAll(query.flattenString(), meetingQuery.filter)
        meetings.each { meet ->
            assertTrue meet.tuesday
            assertTrue meet.thursday
            assertTrue meet.beginTime >= "1000" && meet.endTime <= "1100"
        }
        filterData = [friday: true, term: "201410"]
        meetingQuery = MeetingTimeSearch.parseQueryString(filterData, "sr", filterData)
        assertNotNull meetingQuery
        query = "from MeetingTimeSearch sr where ${meetingQuery.query}"
        meetings = MeetingTimeSearch.findAll(query.flattenString(), meetingQuery.filter)
        meetings.each { meet ->
            assertTrue meet.friday
        }
        filterData = [saturday: true, term: "201410"]
        meetingQuery = MeetingTimeSearch.parseQueryString(filterData, "sr", filterData)
        assertNotNull meetingQuery
        meetings = MeetingTimeSearch.findAll("from MeetingTimeSearch sr where ${meetingQuery.query}" , meetingQuery.filter)
        meetings.each { meet ->
            assertTrue meet.saturday
        }
        filterData = [sunday: true, term: "201410"]
        meetingQuery = MeetingTimeSearch.parseQueryString(filterData, "sr", filterData)
        assertNotNull meetingQuery
        query = "from MeetingTimeSearch sr where ${meetingQuery.query}"
        meetings = MeetingTimeSearch.findAll(query.flattenString(), meetingQuery.filter)
        meetings.each { meet ->
            assertTrue meet.sunday
        }

        filterData = [termCourseReferenceNumber: ["20141020001", "20141020002"], term: "201410"]
        meetingQuery = MeetingTimeSearch.parseQueryString(filterData, "sr", filterData)
        assertNotNull meetingQuery
        query = "from MeetingTimeSearch sr where ${meetingQuery.query}"
        meetings = MeetingTimeSearch.findAll(query.flattenString(), meetingQuery.filter)
        meetings.each { meet ->
            assertTrue meet.term == "201410" && (meet.courseReferenceNumber == "20001" ||
                    meet.courseReferenceNumber == '20002')
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


    void testFetch() {
        def term = "201410"
        def courseReferenceNumber = "20001"
        def list = MeetingTimeSearch.fetchByTermAndCourseReferenceNumber(term, courseReferenceNumber)
        assertEquals 1, list.size()
    }


    void testMultipleInCrn() {
        def list = MeetingTimeSearch.fetchByTermAndCourseReferenceNumber("201110", "8007")
        assertEquals 2, list.size()
    }


    void testFetchListMeetingTimeDetailByTermAndCourseReferenceNumber()
    {
        //String termCode = "201410"
        List termList= new ArrayList()
        List setCRNs = new ArrayList()

        termList.add("201410")
        termList.add("201310")
        setCRNs.add("20199")
        setCRNs.add("20408")
        setCRNs.add("20001")
        def list = MeetingTimeSearch.fetchListMeetingTimeDetailByTermAndCourseReferenceNumber(termList, setCRNs)

        assertTrue list.size()>=3
        list.each{
           assertTrue setCRNs.contains(it.courseReferenceNumber)
        }

        setCRNs = new ArrayList()
        list = MeetingTimeSearch.fetchListMeetingTimeDetailByTermAndCourseReferenceNumber(termList, setCRNs)
        assertTrue list.size()==0

    }
}

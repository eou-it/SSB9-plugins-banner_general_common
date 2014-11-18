/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import java.text.SimpleDateFormat
import org.springframework.dao.InvalidDataAccessResourceUsageException

class SectionMeetingTimeViewIntegrationTests extends net.hedtech.banner.testing.BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['SSAMATX']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
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


    @Test
    void testFetchSearch() {
        // find base list
        def pagingAndSortParams = [:]
        Map paramsMap = [term: "201410"]
        def criteriaMap = [[key: "term", binding: "term", operator: "contains"]]
        def filterData = [params: paramsMap, criteria: criteriaMap]
        def meetings = SectionMeetingTimeView.fetchSearch(filterData, pagingAndSortParams)
        assertTrue meetings.size() > 0
        assertEquals meetings[0].term, "201410"

        // test search on individual fields
        verifyFilterForField("campus", "campus", "%M%", "contains")
        verifyFilterForField("courseReferenceNumber", "courseReferenceNumber", "20001", "equals")
        verifyFilterForField("partOfTerm", "partOfTerm", "1", "equals")
        verifyFilterForField("monday", "monday", true, "equals")
        verifyFilterForField("monday", "monday", true, "notequals")
        verifyFilterForField("monday", "monday", false, "equals")
        verifyFilterForField("tuesday", "tuesday", true, "equals")
        verifyFilterForField("tuesday", "tuesday", false, "equals")
        verifyFilterForField("wednesday", "wednesday", true, "equals")
        verifyFilterForField("wednesday", "wednesday", false, "equals")
        verifyFilterForField("thursday", "thursday", true, "equals")
        verifyFilterForField("friday", "friday", true, "equals")
        verifyFilterForField("beginTime", "beginTime", "0900", "equals")
        verifyFilterForField("endTime", "endTime", "1000", "equals")
        verifyFilterForField("subject", "subject", "ART", "equals")
        verifyFilterForField("courseNumber", "courseNumber", "100", "equals")
        verifyFilterForField("xlstGroup", "xlstGroup", "B1", "equals")
        verifyFilterForField("room", "room", "%1%", "contains")
        verifyFilterForField("building", "building", "%I%", "contains")
    }


    @Test
    void testFetchSearchAndDates() {
        def newDate = "2010-09-01"
        def df1 = new SimpleDateFormat("yyyy-MM-dd")
        def startDate = df1.parse(newDate)
        def meetings = SectionMeetingTimeView.findAllByTermAndStartDate("201410", startDate)
        assertTrue meetings.size() > 0

        def starts = verifyFilterForField("startDate", "startDate", startDate, "equals")
        assertEquals meetings.size(), starts

        def andDate = df1.parse("2010-09-30")
        verifyFilterForField("startDate", "startDate", startDate, "between", andDate)
        verifyFilterForField("startDate", "startDate", startDate, "greaterthan")
        verifyFilterForField("startDate", "startDate", startDate, "lessthan")

        // 10-MAR-2014 00:00:00  test end dates
        def endDate = df1.parse("2014-03-10")
        meetings = SectionMeetingTimeView.findAllByTermAndEndDate("201410", endDate)
        assertTrue meetings.size() > 0
        def ends = verifyFilterForField("endDate", "endDate", endDate, "equals")
        assertEquals meetings.size(), ends
        verifyFilterForField("endDate", "endDate", df1.parse("2014-03-01"), "greaterthan")
        verifyFilterForField("endDate", "endDate", endDate, "lessthan")
        andDate = df1.parse("2013-12-31")
        verifyFilterForField("endDate", "endDate", andDate, "between", endDate)
    }


    private def verifyFilterForField(String fieldName, String binding, def value, String operator, def andValue = null) {
        def pagingAndSortParams = [:]
        Map paramsMap = [:]
        if (andValue) {
            def andFieldName = fieldName + "_and"
            paramsMap = [term: "201410", (fieldName): value, (andFieldName): andValue]
        }
        else {
            paramsMap = [term: "201410", (fieldName): value]
        }
        def criteriaMap = [[key: "term", binding: "term", operator: "equals"],
                [key: fieldName, binding: binding, operator: operator]]
        def filterData = [params: paramsMap, criteria: criteriaMap]
        def meetings = SectionMeetingTimeView.fetchSearch(filterData, pagingAndSortParams)
        assertTrue "Field ${fieldName} failed to be found for value ${value}", meetings.size() > 0
        return meetings.size()
    }

/**
 * Prove that you cannot save
 */

    @Test
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


    @Test
    void testUpdateExceptionResults() {
        def existingSection = SectionMeetingTimeView.findByTermAndCourseReferenceNumber(
                "201410", "20001")
        assertNotNull existingSection
        existingSection.version = new Long(1)
        shouldFail(InvalidDataAccessResourceUsageException) {
            existingSection.save(flush: true, onError: true)
        }
    }


    @Test
    void testDeleteExceptionResults() {
        def existingSection = SectionMeetingTimeView.findByTermAndCourseReferenceNumber(
                "201410", "20001")
        assertNotNull existingSection
        shouldFail(InvalidDataAccessResourceUsageException) {
            existingSection.delete(flush: true, onError: true)
        }
    }

}

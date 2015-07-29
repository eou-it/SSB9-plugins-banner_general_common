/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class UserRoleCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def userRoleCompositeService


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testListStudentPersons() {
        def results = userRoleCompositeService.fetchAllByRole([role: "student", max: '10', offset: '5'], false)

        assertTrue results?.size() > 0
        results.find { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
    }


    @Test
    void testListStudentPersonsGivenPidms() {
        def pidmList = []
        ["HOS00001", "HOSWEB001", "HOSWEB002", "HOSWEB003", "HOSWEB004", "HOSWEB005", "HOSWEB006"].each {
            def person = PersonUtility.getPerson(it)
            pidmList << person.pidm
        }
        assertEquals 7, pidmList.size()
        assertTrue pidmList instanceof List
        def cnt
        Map returnList
        returnList = userRoleCompositeService.fetchAllRolesByPidmInList(pidmList, true)

        assertEquals 7, returnList.size()
    }


    @Test
    void testListFacultyPersonsGivenPidms() {
        def pidmList = []
        ["HOF00741", "HOF00742", "HOF00743", "HOF00744", "HOF00745", "HOF00746", "HOF00747"].each {
            def person = PersonUtility.getPerson(it)
            pidmList << person.pidm
        }
        assertEquals 7, pidmList.size()
        assertTrue pidmList instanceof List
        def cnt
        Map returnList
        returnList = userRoleCompositeService.fetchAllRolesByPidmInList(pidmList, false)

        assertEquals 7, returnList.size()
    }


    @Test
    void testListStudentPersonsCount() {
        def results = userRoleCompositeService.fetchAllByRole([role: "student"], true)

        assertTrue results > 0
    }


    @Test
    void testListFacultyPersonsCount() {
        def results = userRoleCompositeService.fetchAllByRole([role: "faculty"], true)

        assertTrue results > 0
    }

    @Test
    void testListFacultyPersonsWithPagination() {
        def count = userRoleCompositeService.fetchAllByRole([role: "faculty"], true)
        def results = userRoleCompositeService.fetchAllByRole([role: "faculty", max: '10', offset: '50'])
        // pagination will bring rows 50-59 ,  make sure we have more than 60 rows
        assertTrue count > 60
        assertEquals 10, results?.size()
        results.each { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
        // paginate beyond number of rows to test we get 0 back
        def maxPages = Math.round((count / 500) + 1) * 500
        def results3 = userRoleCompositeService.fetchAllByRole([role: "faculty", max: '500', offset: maxPages.toString()])

        assertEquals 0, results3?.size()
    }

    @Test
    void testListFacultyPersonsWithoutPagination() {
        def count = userRoleCompositeService.fetchAllByRole([role: "faculty"], true)
        def results = userRoleCompositeService.fetchAllByRole([role: "faculty"])
        def actual = 0
        if ( count > 500 ) actual = 500
        else actual = count.toInteger()
        assertEquals actual, results?.size()
        results.each { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
    }

    @Test
    void testListStudentPersonsWithPagination() {
        def count = userRoleCompositeService.fetchAllByRole([role: "student"], true)
        assertTrue count > 500
        def results = userRoleCompositeService.fetchAllByRole([role: "student", max: '10', offset: '50'])
        // expect to get rows 50-59 back
        assertEquals 10, results?.size()
        results.find { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
        // expect to get rows 500-999 back
        def results2 = userRoleCompositeService.fetchAllByRole([role: "student", max: '500', offset: '500'])
        assertTrue count > 1000
        assertEquals 500, results2?.size()
        results.each { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
        // test pgination beyond number of rows we have to test we get 0 back
        def maxPages = Math.round((count / 500) + 1) * 500
        def results3 = userRoleCompositeService.fetchAllByRole([role: "student", max: '500', offset: maxPages.toString()])

        assertEquals 0, results3?.size()

    }

    @Test
    void testListStudentPersonsWithOutPagination() {
        def count = userRoleCompositeService.fetchAllByRole([role: "student"], true)
        def results = userRoleCompositeService.fetchAllByRole([role: "student"])

        def actual = 0
        if ( count > 500 ) actual = 500
        else actual = count.toInteger()

        assertEquals actual, results?.size()
        results.each { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
    }

}

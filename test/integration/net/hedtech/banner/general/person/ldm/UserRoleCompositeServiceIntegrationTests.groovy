/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.InstitutionalDescription
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

        def institution = InstitutionalDescription.fetchByKey()
        assertTrue institution.studentInstalled
        assertTrue institution.financeInstalled
        assertTrue institution.hrInstalled
        assertTrue institution.alumniInstalled
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testListStudentPersons() {
        def results = userRoleCompositeService.fetchAllByRole([role: "student", max: '10', offset: '5'])

        assertTrue results?.pidms?.size() > 0
        results.pidms?.find { pidm ->
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
        Map returnList = userRoleCompositeService.fetchAllRolesByPidmInList(pidmList, false)
        returnList.each { entry ->
            entry.value.effectiveEndDate.each {
                assertTrue it.format("yyyy-MM-dd").matches("\\d{4}\\-\\d{2}\\-\\d{2}")
            }
            entry.value.effectiveStartDate.each {
                assertTrue it.format("yyyy-MM-dd").matches("\\d{4}\\-\\d{2}\\-\\d{2}")
            }
        }
        assertEquals 7, returnList.size()
    }


    @Test
    void testListStudentPersonsCount() {
        def results = userRoleCompositeService.fetchAllByRole([role: "student"])

        assertTrue results.count > 0
    }


    @Test
    void testListFacultyPersonsCount() {
        def results = userRoleCompositeService.fetchAllByRole([role: "faculty"])

        assertTrue results.count > 0
    }


    @Test
    void testListFacultyPersonsWithPagination() {
        def results = userRoleCompositeService.fetchAllByRole([role: "faculty", max: '10', offset: '50'])
        // pagination will bring rows 50-59 ,  make sure we have more than 60 rows
        assertTrue results.count > 60
        assertEquals 10, results.pidms?.size()
        results.pidms?.each { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
        // paginate beyond number of rows to test we get 0 back
        def maxPages = Math.round((results.count / 500) + 1) * 500
        def results3 = userRoleCompositeService.fetchAllByRole([role: "faculty", max: '500', offset: maxPages.toString()])

        assertEquals 0, results3?.pidms?.size()
    }


    @Test
    void testListFacultyPersonsWithoutPagination() {
        def results = userRoleCompositeService.fetchAllByRole([role: "faculty"])
        def actual = 0
        if (results.count > 500) actual = 500
        else actual = results.count.toInteger()
        assertEquals actual, results?.pidms?.size()
        results?.pidms.each { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
    }


    @Test
    void testListStudentPersonsWithPagination() {
        def results1 = userRoleCompositeService.fetchAllByRole([role: "student"])
        assertTrue results1.count > 500
        def results = userRoleCompositeService.fetchAllByRole([role: "student", max: '10', offset: '50'])
        // expect to get rows 50-59 back
        assertEquals 10, results?.pidms?.size()
        results.pidms?.find { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
        // expect to get rows 500-999 back
        def results2 = userRoleCompositeService.fetchAllByRole([role: "student", max: '500', offset: '500'])
        assertTrue results1.count > 1000
        assertEquals 500, results2?.pidms?.size()
        results.pidms?.each { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
        // test pgination beyond number of rows we have to test we get 0 back
        def maxPages = Math.round((results1.count / 500) + 1) * 500
        def results3 = userRoleCompositeService.fetchAllByRole([role: "student", max: '500', offset: maxPages.toString()])

        assertEquals 0, results3?.pidms?.size()

    }


    @Test
    void testListStudentPersonsWithOutPagination() {
        def results = userRoleCompositeService.fetchAllByRole([role: "student"])
        def actual = 0
        if (results.count > 500) actual = 500
        else actual = results.count.toInteger()

        // service forces pagination of 500
        assertEquals actual, results?.pidms?.size()
        results?.pidms?.each { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
    }


    @Test
    void testFacultyListWhenStudentNotInstalled() {
        def institution = InstitutionalDescription.fetchByKey()
        institution.studentInstalled = false
        institution.save(flush: true, failOnError: true)

        def results = userRoleCompositeService.fetchAllByRole([role: "faculty"])
        assertEquals 0, results.count
        assertEquals 0, results.pidms?.size()
    }


    @Test
    void testStudentListWhenStudentNotInstalled() {
        def institution = InstitutionalDescription.fetchByKey()
        institution.studentInstalled = false
        institution.save(flush: true, failOnError: true)

        def results = userRoleCompositeService.fetchAllByRole([role: "student"])
        assertEquals 0, results.count
        def results1 = userRoleCompositeService.fetchAllByRole([role: "student"])
        assertEquals 0, results1.pidms?.size()
    }


    @Test
    void testEmployeeListWhenHRNotInstalled() {
        def institution = InstitutionalDescription.fetchByKey()
        institution.hrInstalled = false
        institution.save(flush: true, failOnError: true)

        def results = userRoleCompositeService.fetchAllByRole([role: "employee"])
        assertEquals 0, results.count
        assertEquals 0, results.pidms?.size()
    }


    @Test
    void testAlumniListWhenAlumniNotInstalled() {
        def institution = InstitutionalDescription.fetchByKey()
        institution.alumniInstalled = false
        institution.save(flush: true, failOnError: true)

        def results = userRoleCompositeService.fetchAllByRole([role: "employee"])
        assertEquals 0, results.count
        assertEquals 0, results.pidms?.size()
    }

    @Test
    void testListEmployeePersonsCount() {
        def results = userRoleCompositeService.fetchEmployees(params.sort?.trim(), params.order?.trim(), 500, 0)

        assertTrue results.totalCount > 0
    }

    @Test
    void testListAlumniPersonsCount() {
        def results = userRoleCompositeService.fetchAlumnis(params.sort?.trim(), params.order?.trim(), 500, 0)

        assertTrue results.totalCount > 0
    }


    @Test
    void testListEmployeePersonsWithOutPagination() {
        def results = userRoleCompositeService.fetchEmployees(params.sort?.trim(), params.order?.trim(), 500, 0)
        def actual = 0
        if (results.totalCount > 500) actual = 500
        else actual = results.totalCount.toInteger()

        // service forces pagination of 500
        assertEquals actual, results?.pidms?.size()
        results?.pidms?.each { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
    }


    @Test
    void testListEmployeePersonsWithPagination() {
        def results1 = userRoleCompositeService.fetchEmployees(params.sort?.trim(), params.order?.trim(), 500, 0)
        def results = userRoleCompositeService.fetchEmployees(params.sort?.trim(), params.order?.trim(), 10, 10)
        // expect to get rows 50-59 back
        assertEquals 10, results?.pidms?.size()
        results.pidms?.find { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }

        // test pgination beyond number of rows we have to test we get 0 back
        int maxPages = Math.round((results1.totalCount / 500) + 1) * 500
        def results3 = userRoleCompositeService.fetchEmployees(params.sort?.trim(), params.order?.trim(), 500, maxPages)

        assertEquals 0, results3?.pidms?.size()

    }


    @Test
    void testListAlumniPersonsWithOutPagination() {
        def results = userRoleCompositeService.fetchAlumnis(params.sort?.trim(), params.order?.trim(), 500, 0)
        def actual = 0
        if (results.totalCount > 500) actual = 500
        else actual = results.totalCount.toInteger()

        // service forces pagination of 500
        assertEquals actual, results?.pidms?.size()
        results?.pidms?.each { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
    }


    @Test
    void testListAlumniPersonsWithPagination() {
        def results1 = userRoleCompositeService.fetchAlumnis(params.sort?.trim(), params.order?.trim(), 500, 0)
        def results = userRoleCompositeService.fetchAlumnis(params.sort?.trim(), params.order?.trim(), 10, 10)
        // expect to get rows 50-59 back
        assertEquals 10, results?.pidms?.size()
        results.pidms?.find { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }

        // test pgination beyond number of rows we have to test we get 0 back
        int maxPages = Math.round((results1.totalCount / 500) + 1) * 500
        def results3 = userRoleCompositeService.fetchAlumnis(params.sort?.trim(), params.order?.trim(), 500, maxPages)

        assertEquals 0, results3?.pidms?.size()

    }


    @Test
    void testListAlumniPersonsGivenPidms() {
        def pidmList = []
        ["A00000613", "A00000614", "A00000615", "A00000616"].each {
            def person = PersonUtility.getPerson(it)
            pidmList << person.pidm
        }
        assertEquals 4, pidmList.size()
        assertTrue pidmList instanceof List
        def returnList
        returnList = userRoleCompositeService.fetchAlumnisByPIDMs(pidmList)
        assertEquals 4, returnList.size()
    }


    @Test
    void testListEmployeePersonsGivenPidms() {
        def pidmList = []
        ["FICA00001", "FLACC0002", "FLACC0003", "FLACS0001"].each {
            def person = PersonUtility.getPerson(it)
            pidmList << person.pidm
        }
        assertEquals 4, pidmList.size()
        assertTrue pidmList instanceof List
        def returnList
        returnList = userRoleCompositeService.fetchEmployeesByPIDMs(pidmList)
        assertEquals 4, returnList.size()
    }

    @Test
    void testListVendorPersons() {
        def results = userRoleCompositeService.fetchVendors("firstName", "asc", 10, 5)
        assertTrue results?.pidms?.size() > 0
        results.pidms?.find { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
    }

    @Test
    void testListVendorPersonsCount() {
        def results = userRoleCompositeService.fetchVendors("firstName", "asc", 10, 5)
        assertTrue results.totalCount > 0
    }

    @Test
    void testListVendorPersonsWithPagination() {
        def results1 = userRoleCompositeService.fetchVendors("firstName", "asc", 0, 0)
        assertTrue results1.totalCount > 10
        def results = userRoleCompositeService.fetchVendors("firstName", "asc", 10, 50)
        // expect to get rows 50-59 back
        assertEquals 10, results?.pidms?.size()
        results.pidms?.find { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
        // test pgination beyond number of rows we have to test we get 0 back
        Long maxPages = Math.round((results1.totalCount / 500) + 1) * 500
        def results3 = userRoleCompositeService.fetchVendors("firstName", "asc", 500, maxPages.intValue())
        assertEquals 0, results3?.pidms?.size()

    }


    @Test
    void testListVendorPersonsWithOutPagination() {
        def results = userRoleCompositeService.fetchVendors("firstName", "asc", 0, 0)
        def actual = 0
        if (results.totalCount > 500) actual = 500
        else actual = results.totalCount.toInteger()

        // service forces pagination of 500
        assertEquals actual, results?.pidms?.size()
        results?.pidms?.each { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
    }


    @Test
    void testVendorListWhenFinanceNotInstalled() {
        def institution = InstitutionalDescription.fetchByKey()
        institution.financeInstalled = false
        institution.save(flush: true, failOnError: true)

        def results = userRoleCompositeService.fetchVendors("firstName", "asc", 0, 0)
        assertEquals 0, results.totalCount
        assertEquals 0, results.pidms?.size()
    }

    @Test
    void testListVendorPersonsGivenPidms() {
        def pidmList = []
        ["A00010104", "A00010107", "A00010100", "A00010210"].each {
            def person = PersonUtility.getPerson(it)
            pidmList << person.pidm
        }
        assertEquals 4, pidmList.size()
        assertTrue pidmList instanceof List
        def cnt
        def returnList
        returnList = userRoleCompositeService.fetchVendorsByPIDMs(pidmList)
        assertNotNull returnList.size()
        assertEquals 4, returnList.size()
    }

    @Test
    void testListProspectiveStudentPersons() {
        def results = userRoleCompositeService.fetchProspectiveStudents("firstName","asc",10,5)
        assertFalse results.isEmpty()
        assertEquals results?.pidms?.size() , 10
        results.pidms?.find { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
    }

    @Test
    void testListProspectiveStudentPersonsCount() {
        def results = userRoleCompositeService.fetchProspectiveStudents("firstName","asc",10,5)
        assertFalse results.isEmpty()
        assertEquals results.pidms.size() , 10
        assertTrue results.totalCount > 0
    }

    @Test
    void testListProspectiveStudentPersonsWithPagination() {
        def results1 = userRoleCompositeService.fetchProspectiveStudents("firstName","asc",0,0)
        assertTrue results1.totalCount > 0
        def results = userRoleCompositeService.fetchProspectiveStudents("firstName","asc",5,10)
        // expect to get rows 10-14 back
        assertEquals 5, results?.pidms?.size()
        results.pidms?.find { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
        // test pgination beyond number of rows we have to test we get 0 back
        Long maxPages = Math.round((results1.totalCount / 500) + 1) * 500
        def results3 = userRoleCompositeService.fetchProspectiveStudents("firstName","asc",500,maxPages.intValue())
        assertEquals 0, results3?.pidms?.size()

    }

    @Test
    void testListProspectiveStudentPersonsWithOutPagination() {
        def results = userRoleCompositeService.fetchProspectiveStudents("firstName","asc",0,0)
        def actual = 0
        if (results.totalCount > 500) actual = 500
        else actual = results.totalCount.toInteger()

        // service forces pagination of 500
        assertEquals actual, results?.pidms?.size()
        results?.pidms?.each { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
    }

    @Test
    void testProspectiveStudentListWhenStudentNotInstalled() {
        InstitutionalDescription institution = InstitutionalDescription.fetchByKey()
        institution.studentInstalled = false
        institution.save(flush: true, failOnError: true)

        def results = userRoleCompositeService.fetchProspectiveStudents("firstName","asc",0,0)
        assertEquals 0, results.totalCount
        assertEquals 0, results.pidms?.size()
    }

    @Test
    void testListProspectiveStudentPersonsGivenPidms() {
        def pidmList = []
        ["HOSH00001", "HOSH00002", "HOR000002", "HOSA0004"].each {
            def person = PersonUtility.getPerson(it)
            pidmList << person.pidm
        }
        assertEquals 4, pidmList.size()
        assertTrue pidmList instanceof List
        def cnt
        def returnList
        returnList = userRoleCompositeService.fetchProspectiveStudentByPIDMs(pidmList)
        assertNotNull  returnList.size()
        assertEquals 4, returnList.size()
    }

    @Test
    void testListAdvisorPersons() {
        def results = userRoleCompositeService.fetchAdvisors("firstName","asc",10,5)
        assertFalse results.isEmpty()
        assertEquals results?.pidms?.size() , 10
        results.pidms?.find { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
    }

    @Test
    void testListAdvisorPersonsCount() {
        def results = userRoleCompositeService.fetchAdvisors("firstName","asc",10,5)
        assertFalse results.isEmpty()
        assertEquals results.pidms.size() , 10
        assertTrue results.totalCount > 0
    }

    @Test
    void testListAdvisorPersonsWithPagination() {
        def results1 = userRoleCompositeService.fetchAdvisors("firstName","asc",0,0)
        assertTrue results1.totalCount > 0
        def results = userRoleCompositeService.fetchAdvisors("firstName","asc",5,10)
        // expect to get rows 10-14 back
        assertEquals 5, results?.pidms?.size()
        results.pidms?.find { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
        // test pgination beyond number of rows we have to test we get 0 back
        Long maxPages = Math.round((results1.totalCount / 500) + 1) * 500
        def results3 = userRoleCompositeService.fetchAdvisors("firstName","asc",500,maxPages.intValue())
        assertEquals 0, results3?.pidms?.size()

    }

    @Test
    void testListAdvisorPersonsWithOutPagination() {
        def results = userRoleCompositeService.fetchAdvisors("firstName","asc",0,0)
        def actual = 0
        if (results.totalCount > 500) actual = 500
        else actual = results.totalCount.toInteger()

        // service forces pagination of 500
        assertEquals actual, results?.pidms?.size()
        results?.pidms?.each { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
    }

    @Test
    void testAdvisorListWhenStudentNotInstalled() {
        InstitutionalDescription institution = InstitutionalDescription.fetchByKey()
        institution.studentInstalled = false
        institution.save(flush: true, failOnError: true)

        def results = userRoleCompositeService.fetchAdvisors("firstName","asc",0,0)
        assertEquals 0, results.totalCount
        assertEquals 0, results.pidms?.size()
    }

    @Test
    void testListAdvisorPersonsGivenPidms() {
        def pidmList = []
        ["HOF00741", "HOF00742", "HOF00743", "HOF00744", "HOF00745", "HOF00746", "HOF00747"].each {
            def person = PersonUtility.getPerson(it)
            pidmList << person.pidm
        }
        assertEquals 7, pidmList.size()
        assertTrue pidmList instanceof List
        def cnt
        def returnList
        returnList = userRoleCompositeService.fetchAdvisorByPIDMs(pidmList)
        assertNotNull  returnList.size()
        assertEquals 7, returnList.size()
    }

}

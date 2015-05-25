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
        returnList = userRoleCompositeService.fetchAllRolesByPidmInList(pidmList, false)

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
    void testListFacultyPersons() {
        def results = userRoleCompositeService.fetchAllByRole([role: "faculty", max: '10', offset: '5'])

        assertTrue results?.size() > 0
        results.find { pidm ->
            assertNotNull PersonUtility.getPerson(pidm)
        }
    }

}

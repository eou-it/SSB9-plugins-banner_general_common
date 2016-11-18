/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.person.view.NonPersonPersonView
import net.hedtech.banner.general.system.InstitutionalDescription
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before
import org.junit.Test

class NonPersonRoleCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

NonPersonRoleCompositeService nonPersonRoleCompositeService


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @Test
    void testListVendorPersons() {
        def results = nonPersonRoleCompositeService.fetchVendors(10, 5)
        assertTrue results?.pidms?.size() > 0
        results.pidms?.each{ pidm ->
            assertNotNull NonPersonPersonView.findByPidmAndChangeIndicatorIsNullAndEntityIndicator(pidm,'C')
        }
    }

    @Test
    void testListVendorPersonsCount() {
        def results = nonPersonRoleCompositeService.fetchVendors(10, 5)
        assertTrue results.totalCount > 0
    }

    @Test
    void testListVendorPersonsWithPagination() {
        def results1 = nonPersonRoleCompositeService.fetchVendors(0, 0)
        assertTrue results1.totalCount > 10
        def results = nonPersonRoleCompositeService.fetchVendors(10, 50)
        // expect to get rows 50-59 back
        assertEquals 10, results?.pidms?.size()
        results.pidms?.find { pidm ->
            assertNotNull NonPersonPersonView.findByPidmAndChangeIndicatorIsNullAndEntityIndicator(pidm,'C')
        }
        // test pgination beyond number of rows we have to test we get 0 back
        Long maxPages = Math.round((results1.totalCount / 500) + 1) * 500
        def results3 = nonPersonRoleCompositeService.fetchVendors(500, maxPages.intValue())
        assertEquals 0, results3?.pidms?.size()

    }


    @Test
    void testListVendorPersonsWithOutPagination() {
        def results = nonPersonRoleCompositeService.fetchVendors( 0, 0)
        def actual = 0
        if (results.totalCount > 500) actual = 500
        else actual = results.totalCount.toInteger()

        // service forces pagination of 500
        assertEquals actual, results?.pidms?.size()
        results?.pidms?.each { pidm ->
            assertNotNull NonPersonPersonView.findByPidmAndChangeIndicatorIsNullAndEntityIndicator(pidm,'C')
        }
    }


    @Test
    void testVendorListWhenFinanceNotInstalled() {
        def institution = InstitutionalDescription.fetchByKey()
        institution.financeInstalled = false
        institution.save(flush: true, failOnError: true)

        def results = nonPersonRoleCompositeService.fetchVendors(0, 0)
        assertEquals 0, results.totalCount
        assertEquals 0, results.pidms?.size()
    }

    @Test
    void testListVendorPersonsGivenPidms() {
       def pidmList = nonPersonRoleCompositeService.fetchVendors(4, 0)?.pidms
        assertEquals 4, pidmList.size()
        assertTrue pidmList instanceof List
        def returnList
        returnList = nonPersonRoleCompositeService.fetchVendorsByPIDMs(pidmList)
        assertNotNull returnList.size()
        assertEquals 4, returnList.size()
    }



}

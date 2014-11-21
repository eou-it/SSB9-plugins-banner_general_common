/** *******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.system.ldm

import junit.framework.Assert
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.Campus
import net.hedtech.banner.general.system.ldm.v1.SiteDetail
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class SiteDetailCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    Campus campus
    def siteDetailCompositeService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initializeDataReferences()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    private void initializeDataReferences() {
        campus = Campus.findByCode('A')
    }

    /**
     * Test case for List method
     */
    @Test
    void testListWithPagination() {
        def paginationParams = [max: '20', offset: '0']
        List siteList = siteDetailCompositeService.list(paginationParams)
        assertNotNull siteList
        assertFalse siteList.isEmpty()
        assertTrue siteList.code.contains(campus.code)
    }

    /**
     * Testcase for count method
     */
    @Test
    void testCount() {
        assertEquals Campus.count(), siteDetailCompositeService.count()
    }

    /**
     * Testcase for show method
     */
    @Test
    void testGet() {
        def paginationParams = [max: '20', offset: '0']
        List siteList = siteDetailCompositeService.list(paginationParams)
        assertNotNull siteList
        assertTrue siteList.size() > 0
        assertNotNull siteList[0].guid
        def site = siteDetailCompositeService.get(siteList[0].guid)
        assertNotNull site
        assertEquals siteList[0].code, site.code
        assertEquals siteList[0].description, site.description
        assertEquals siteList[0].guid, site.guid
        assertEquals siteList[0].metadata, site.metadata
        assertEquals siteList[0].buildings, site.buildings
    }

    /**
     * Testcase for show method with ApplicationException
     */
    @Test
    void testGetWithInvalidGuid() {
        shouldFail( ApplicationException  ) {
            siteDetailCompositeService.get(null)
        }

    }

    /**
     * Testcase for fetchByCampusId method
     */
    @Test
    void testFetchByCampusId() {
        def siteDetail = siteDetailCompositeService.fetchByCampusId(campus.id)
        assertNotNull siteDetail
        Assert.assertEquals campus.id, siteDetail.id
        GroovyTestCase.assertEquals campus.code, siteDetail.code
        GroovyTestCase.assertEquals campus.description, siteDetail.description
        GroovyTestCase.assertEquals campus.dataOrigin, siteDetail.metadata.dataOrigin
    }

    /**
     * Testcase for fetchByCampusCode
     */
    @Test
    void testFetchFetchByCampusCode() {
        SiteDetail site = siteDetailCompositeService.fetchByCampusCode(campus.code)
        assertNotNull site
        assertEquals campus.id, site.id
        assertEquals campus.code, site.code
        assertEquals campus.description, site.description
        assertEquals campus.dataOrigin, site.metadata.dataOrigin
    }

}

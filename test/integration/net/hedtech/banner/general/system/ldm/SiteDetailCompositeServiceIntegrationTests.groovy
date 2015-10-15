/** *******************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.system.ldm

import junit.framework.Assert
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.system.Campus
import net.hedtech.banner.general.system.ldm.v1.SiteDetail
import net.hedtech.banner.restfulapi.RestfulApiValidationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.After
import org.junit.Before
import org.junit.Test

class SiteDetailCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    Campus campus
    def siteDetailCompositeService
    private String invalid_sort_orderErrorMessage = 'RestfulApiValidationUtility.invalidSortField'

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
    void testGetUptoV3() {
        //we will forcefully set the accept header so that the tests go through all possible code flows
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", "application/vnd.hedtech.integration.v3+json")

        def paginationParams = [max: '20', offset: '0']
        List siteList = siteDetailCompositeService.list(paginationParams)
        assertNotNull siteList
        assertTrue siteList.size() > 0
        assertNotNull siteList[0].guid
        def site = siteDetailCompositeService.get(siteList[0].guid)
        assertNotNull site.toString()
        assertEquals siteList[0].code, site.code
        assertEquals siteList[0].description, site.description
        assertEquals siteList[0].guid, site.guid
        assertEquals siteList[0].metadata, site.metadata
        assertEquals siteList[0].buildings, site.buildings
        assertEquals siteList[0], site
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
        assertNotNull site.toString()
        assertEquals siteList[0].code, site.code
        assertEquals siteList[0].description, site.description
        assertEquals siteList[0].guid, site.guid
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

    /**
     * Test to check the SiteDetailCompositeService list method with valid sort and order field and supported version
     * If No "Accept" header is provided, by default it takes the latest supported version
     */
    @Test
    void testListWithValidSortAndOrderFieldWithSupportedVersion() {
        def params = [order: 'ASC', sort: 'code']
        def siteList = siteDetailCompositeService.list(params)
        assertNotNull siteList
        assertFalse siteList.isEmpty()
        assertNotNull siteList.code
        assertEquals Campus.count(), siteList.size()
        assertNotNull campus
        assertTrue siteList.id.contains(campus.id)
        assertTrue siteList.code.contains(campus.code)
        assertTrue siteList.description.contains(campus.description)
        assertTrue siteList.dataOrigin.contains(campus.dataOrigin)
    }

    /**
     * Test to check the sort by code on SiteDetailCompositeService
     * */
    @Test
    public void testSortByCode(){
        params.order='ASC'
        params.sort='code'
        List list = siteDetailCompositeService.list(params)
        assertNotNull list
        def tempParam=null
        list.each{
            site->
                String code=site.code
                if(!tempParam){
                    tempParam=code
                }
                assertTrue tempParam.compareTo(code)<0 || tempParam.compareTo(code)==0
                tempParam=code
        }

        params.clear()
        params.order='DESC'
        params.sort='code'
        list = siteDetailCompositeService.list(params)
        assertNotNull list
        tempParam=null
        list.each{
            site->
                String code=site.code
                if(!tempParam){
                    tempParam=code
                }
                assertTrue tempParam.compareTo(code)>0 || tempParam.compareTo(code)==0
                tempParam=code
        }
    }

    /**
     * Test to check the SiteDetailCompositeService list method with invalid sort field
     */
    @Test
    void testListWithInvalidSortField() {
        try {
            def map = [sort: 'test']
            siteDetailCompositeService.list(map)
            fail()
        } catch (RestfulApiValidationException e) {
            assertEquals 400, e.getHttpStatusCode()
            assertEquals invalid_sort_orderErrorMessage , e.messageCode.toString()
        }
    }

    /**
     * Test to check the SiteDetailCompositeService list method with invalid order field
     */
    @Test
    void testListWithInvalidOrderField() {
        shouldFail(RestfulApiValidationException) {
            def map = [order: 'test']
            siteDetailCompositeService.list(map)
        }
    }

}

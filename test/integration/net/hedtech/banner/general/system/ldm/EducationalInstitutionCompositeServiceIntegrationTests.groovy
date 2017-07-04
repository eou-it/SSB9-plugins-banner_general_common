/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.system.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.system.EducationalInstitutionView
import net.hedtech.banner.general.system.ldm.v6.EducationalInstitutionV6
import net.hedtech.banner.restfulapi.RestfulApiValidationException
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.Before
import org.junit.Test

class EducationalInstitutionCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    EducationalInstitutionCompositeService educationalInstitutionCompositeService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @Test
    void testValidList() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        def params = [:]
        List<EducationalInstitutionV6> institutionList = educationalInstitutionCompositeService.list(params)
        assertNotNull institutionList
        assertNotNull institutionList[0].guid
    }

    @Test
    void testListWithSort() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        def params = [:]
        params.sort='type'
        params.order='DESC'
        List<EducationalInstitutionV6> institutionList = educationalInstitutionCompositeService.list(params)
        assertNotNull institutionList
        assertTrue institutionList.size > 0 && institutionList.size <= RestfulApiValidationUtility.MAX_UPPER_LIMIT
        assertListIsSortedOnField(institutionList,params.sort,params.order)
    }

    @Test
    void testListWithInvalidSortAndOrder() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        def params = [:]
        params.put('sort', 'invalid-sort-field')
        shouldFail(RestfulApiValidationException) {
            educationalInstitutionCompositeService.list(params)
        }
        params.clear()
        params.put('order', 'invalid-order-field')
        shouldFail(RestfulApiValidationException) {
            educationalInstitutionCompositeService.list(params)
        }
    }

    @Test
    void testListWithSearch() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        def params = [:]
        def searchField='type'
        params.put(searchField,'secondarySchool')
        List<EducationalInstitutionV6> institutionList = educationalInstitutionCompositeService.list(params)
        assertListIsFilteredOnField(institutionList,searchField,'secondarySchool')
    }

    @Test
    void testListWithPagination(){
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        def paginationParams = [max: '2', offset: '1']
        List<EducationalInstitutionV6> institutionList = educationalInstitutionCompositeService.list(paginationParams)
        assertNotNull institutionList
        assertEquals institutionList.size, 2
    }

    @Test
    void testCount() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        assertEquals EducationalInstitutionView.count(), educationalInstitutionCompositeService.count([:])
    }

    @Test
    void testShow_EducationalInstitution() {
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")
        def params = [:]
        List<EducationalInstitutionV6> institutionList = educationalInstitutionCompositeService.list(params)
        assertNotNull institutionList
        String guid = institutionList.get(0).guid
        def institution = educationalInstitutionCompositeService.get(guid)
        assertNotNull institution
        assertEquals institution.guid, guid
    }

    @Test
    void testShow_InvalidEducationalInstitution(){
        setAcceptHeader("application/vnd.hedtech.integration.v6+json")

        try{
            educationalInstitutionCompositeService.get("abc")
        }
        catch (ApplicationException ae) {
            assertApplicationException ae, "NotFoundException"
        }
    }

    private void assertListIsSortedOnField(def list, String field, String sortOrder = "ASC") {
        def prevListItemVal
        list.each {
            String curListItemVal = it[field]
            if (!prevListItemVal) {
                prevListItemVal = curListItemVal
            }
            if (sortOrder == "ASC") {
                assertTrue prevListItemVal.compareTo(curListItemVal) < 0 || prevListItemVal.compareTo(curListItemVal) == 0
            } else {
                assertTrue prevListItemVal.compareTo(curListItemVal) > 0 || prevListItemVal.compareTo(curListItemVal) == 0
            }
            prevListItemVal = curListItemVal
        }
    }

    private void assertListIsFilteredOnField(def list, def field, def value){
        list.each {
            def itemValue = it[field]
            assertEquals itemValue, value
        }
    }

    private void setAcceptHeader(String mediaType) {
        GrailsMockHttpServletRequest request = LdmService.getHttpServletRequest()
        request.addHeader("Accept", mediaType)
    }

}

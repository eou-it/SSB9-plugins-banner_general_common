/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.IntegrationPartnerSystemRule
import net.hedtech.banner.general.overall.ldm.v2.InstructionalPlatform
import net.hedtech.banner.general.system.IntegrationPartner
import net.hedtech.banner.restfulapi.RestfulApiValidationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before
import org.junit.Test

/**
 * InstructionalPlatformCompositeServiceIntegrationTests.
 */
class InstructionalPlatformCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    InstructionalPlatformCompositeService instructionalPlatformCompositeService

    IntegrationPartnerSystemRule i_success_integrationPartnerSystem
    IntegrationPartner i_success_integrationPartner

    String i_success_code = "TW"
    String i_success_description = "Test IntegPartner Description"
    private String invalid_sort_orderErrorMessage = 'RestfulApiValidationUtility.invalidSortField'


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initiializeDataReferences()
    }


    private void initiializeDataReferences() {
        i_success_integrationPartnerSystem = IntegrationPartnerSystemRule.findByCode( 'WEB' )
        i_success_integrationPartner = IntegrationPartner.findByCode( 'BB' )
    }


    @Test
    void testListWithoutPaginationParams() {
        List<InstructionalPlatform> instructionalPlatforms = instructionalPlatformCompositeService.list( [:] )
        assertNotNull instructionalPlatforms
        assertFalse instructionalPlatforms.isEmpty()
    }


    @Test
    void testListWithPagination() {
        Map paginationParams = [max: '20', offset: '0']
        List instructionalPlatforms = instructionalPlatformCompositeService.list( paginationParams )
        assertNotNull instructionalPlatforms
        assertTrue instructionalPlatforms.size()<=20
    }


    @Test
    void testCount() {
        assertEquals IntegrationPartnerSystemRule.count(), instructionalPlatformCompositeService.count()
    }


    @Test
    void testGetInvalidGuid() {
        try {
            instructionalPlatformCompositeService.get( 'Invalid-guid' )
            fail()
        } catch (ApplicationException ae) {
            assertApplicationException ae, "NotFoundException"
        }
    }


    @Test
    void testGetInvalidNonExistentInstructionalPlatform() {
        IntegrationPartnerSystemRule integrationPartnerSystemRule = newValidForCreateIntegrationPartnerSystem()
        save integrationPartnerSystemRule
        assertNotNull integrationPartnerSystemRule.id
        InstructionalPlatform instructionalPlatform = instructionalPlatformCompositeService.fetchByIntegrationPartnerSystemId( integrationPartnerSystemRule.id )
        assertNotNull instructionalPlatform
        assertNotNull instructionalPlatform.guid
        assertEquals instructionalPlatform.id, integrationPartnerSystemRule.id

        integrationPartnerSystemRule.delete( flush: true )
        assertNull integrationPartnerSystemRule.get( instructionalPlatform.id )

        try {
            instructionalPlatformCompositeService.get( instructionalPlatform.guid )
            fail()
        } catch (ApplicationException ae) {
            assertApplicationException ae, "NotFoundException"
        }
    }


    @Test
    void testGet() {
        Map paginationParams = [max: '1', offset: '0']
        List instructionalPlatforms = instructionalPlatformCompositeService.list( paginationParams )
        assertNotNull instructionalPlatforms
        assertFalse instructionalPlatforms.isEmpty()

        assertNotNull instructionalPlatforms[0].guid
        InstructionalPlatform instructionalPlatform = instructionalPlatformCompositeService.get( instructionalPlatforms[0].guid )
        assertNotNull instructionalPlatform.toString()
        assertEquals instructionalPlatforms[0].guid, instructionalPlatform.guid
        assertEquals instructionalPlatforms[0].code, instructionalPlatform.code
        assertEquals instructionalPlatforms[0].description, instructionalPlatform.description
        assertEquals instructionalPlatforms[0].metadata.dataOrigin, instructionalPlatform.metadata.dataOrigin
        assertEquals instructionalPlatforms[0], instructionalPlatform
    }


    @Test
    void testFetchByIntegrationPartnerSystemId() {
        assertNotNull i_success_integrationPartner
        assertNotNull i_success_integrationPartner.id
        InstructionalPlatform instructionalPlatform = instructionalPlatformCompositeService.fetchByIntegrationPartnerSystemId( i_success_integrationPartnerSystem.id )
        assertNotNull instructionalPlatform
        assertEquals i_success_integrationPartnerSystem.id, instructionalPlatform.id
        assertEquals i_success_integrationPartnerSystem.code, instructionalPlatform.code
        assertEquals i_success_integrationPartnerSystem.description, instructionalPlatform.description
        assertEquals i_success_integrationPartnerSystem.dataOrigin, instructionalPlatform.metadata.dataOrigin
    }


    @Test
    void testFetchByIntegrationPartnerSystemIdInvalid() {
        assertNull instructionalPlatformCompositeService.fetchByIntegrationPartnerSystemId( null )
    }


    @Test
    void testFetchByIntegrationPartnerSystemCode() {
        assertNotNull i_success_integrationPartner
        assertNotNull i_success_integrationPartner.code
        InstructionalPlatform instructionalPlatform = instructionalPlatformCompositeService.fetchByIntegrationPartnerSystemCode( i_success_integrationPartnerSystem.code )
        assertNotNull instructionalPlatform
        assertEquals i_success_integrationPartnerSystem.id, instructionalPlatform.id
        assertEquals i_success_integrationPartnerSystem.code, instructionalPlatform.code
        assertEquals i_success_integrationPartnerSystem.description, instructionalPlatform.description
        assertEquals i_success_integrationPartnerSystem.dataOrigin, instructionalPlatform.metadata.dataOrigin
    }


    @Test
    void testFetchByIntegrationPartnerSystemCodeInvalid() {
        assertNull instructionalPlatformCompositeService.fetchByIntegrationPartnerSystemCode( null )

        String invalidIntegrationPartnerCode = 'A5'
        if (!IntegrationPartnerSystemRule.findByCode( invalidIntegrationPartnerCode )) {
            assertNull instructionalPlatformCompositeService.fetchByIntegrationPartnerSystemCode( invalidIntegrationPartnerCode )
        }
    }


    @Test
    void testFetchAllByIntegrationPartnerSystemCode(){
        assertEquals([],instructionalPlatformCompositeService.fetchAllByIntegrationPartnerSystemCode(null))
        assertEquals([],instructionalPlatformCompositeService.fetchAllByIntegrationPartnerSystemCode([]))

        List<InstructionalPlatform> instructionalPlatformList =  instructionalPlatformCompositeService.fetchAllByIntegrationPartnerSystemCode(['1', '2'])
        assertNotNull(instructionalPlatformList)
        assertEquals(2,instructionalPlatformList.size())
        assertEquals('1',instructionalPlatformList[0].code)
        assertEquals('2',instructionalPlatformList[1].code)
    }

    private IntegrationPartnerSystemRule newValidForCreateIntegrationPartnerSystem() {
        assertNotNull i_success_integrationPartner
        assertNotNull i_success_integrationPartner.code
        IntegrationPartnerSystemRule integrationPartnerSystemRule = new IntegrationPartnerSystemRule(
                code: i_success_code,
                description: i_success_description,
                integrationPartner: i_success_integrationPartner
        )
        return integrationPartnerSystemRule
    }

    /**
     * Test to check the InstructionalPlatformCompositeService list method with valid sort and order field and supported version
     * If No "Accept" header is provided, by default it takes the latest supported version
     */
    @Test
    void testListWithValidSortAndOrderFieldWithSupportedVersion() {
        def params = [order: 'ASC', sort: 'code']
        def instructionalPlatformList = instructionalPlatformCompositeService.list(params)
        assertNotNull instructionalPlatformList
        assertFalse instructionalPlatformList.isEmpty()
        assertNotNull instructionalPlatformList.code
        assertEquals IntegrationPartnerSystemRule.count(), instructionalPlatformList.size()
        assertNotNull i_success_integrationPartnerSystem
        assertTrue instructionalPlatformList.code.contains(i_success_integrationPartnerSystem.code)
        assertTrue instructionalPlatformList.description.contains(i_success_integrationPartnerSystem.description)
        assertTrue instructionalPlatformList.dataOrigin.contains(i_success_integrationPartnerSystem.dataOrigin)

    }

    /**
     * Test to check the sort by code on InstructionalPlatformCompositeService
     * */
    @Test
    public void testSortByCode(){
        params.order='ASC'
        params.sort='code'
        List list = instructionalPlatformCompositeService.list(params)
        assertNotNull list
        def tempParam=null
        list.each{
            instructionalPlatform->
                String code=instructionalPlatform.code
                if(!tempParam){
                    tempParam=code
                }
                assertTrue tempParam.compareTo(code)<0 || tempParam.compareTo(code)==0
                tempParam=code
        }

        params.clear()
        params.order='DESC'
        params.sort='code'
        list = instructionalPlatformCompositeService.list(params)
        assertNotNull list
        tempParam=null
        list.each{
            instructionalPlatform->
                String code=instructionalPlatform.code
                if(!tempParam){
                    tempParam=code
                }
                assertTrue tempParam.compareTo(code)>0 || tempParam.compareTo(code)==0
                tempParam=code
        }
    }

    /**
     * Test to check the InstructionalPlatformCompositeService list method with invalid sort field
     */
    @Test
    void testListWithInvalidSortOrder() {
        try {
            def map = [sort: 'test']
            instructionalPlatformCompositeService.list(map)
            fail()
        } catch (RestfulApiValidationException e) {
            assertEquals 400, e.getHttpStatusCode()
            assertEquals invalid_sort_orderErrorMessage , e.messageCode.toString()
        }
    }

    /**
     * Test to check the InstructionalPlatformCompositeService list method with invalid sort field
     */
    @Test
    void testListWithInvalidSortField() {
        shouldFail(RestfulApiValidationException) {
            def map = [order: 'test']
            instructionalPlatformCompositeService.list(map)
        }
    }
}

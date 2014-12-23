/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.IntegrationPartnerSystemRule
import net.hedtech.banner.general.overall.ldm.v2.InstructionalPlatform
import net.hedtech.banner.general.system.IntegrationPartner
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
        assertEquals 20, instructionalPlatforms.size()
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
        assertNotNull instructionalPlatform
        assertEquals instructionalPlatforms[0].guid, instructionalPlatform.guid
        assertEquals instructionalPlatforms[0].code, instructionalPlatform.code
        assertEquals instructionalPlatforms[0].description, instructionalPlatform.description
        assertEquals instructionalPlatforms[0].metadata.dataOrigin, instructionalPlatform.metadata.dataOrigin
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
}

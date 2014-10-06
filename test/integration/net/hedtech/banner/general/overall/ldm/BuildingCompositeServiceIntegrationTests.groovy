/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.HousingLocationBuildingDescription
import net.hedtech.banner.general.overall.ldm.v1.BuildingDetail
import net.hedtech.banner.general.system.ldm.v1.SiteDetail
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before
import org.junit.Test

class BuildingCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    HousingLocationBuildingDescription i_success_housingLocationBuildingDescription
    def buildingCompositeService

    @Before
    void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initiializeDataReferences()
    }


    private void initiializeDataReferences() {
        net.hedtech.banner.general.system.Building building = net.hedtech.banner.general.system.Building.findByCode( 'CIS' )
        i_success_housingLocationBuildingDescription = HousingLocationBuildingDescription.findByBuilding( building )
    }

    @Test
    void testListWithoutPaginationParams() {
        List buildings = buildingCompositeService.list( [max:'10'] )
        assertNotNull buildings
        assertFalse buildings.isEmpty()
        assertEquals 10, buildings.size()
    }

    @Test
    void testListWithPagination() {
        def paginationParams = [max: '20', offset: '0']
        List buildings = buildingCompositeService.list( paginationParams )
        assertNotNull buildings
        assertFalse buildings.isEmpty()
        assertTrue buildings.size() <= 20
    }

    @Test
    void testCount() {
        assertNotNull i_success_housingLocationBuildingDescription
        assertEquals HousingLocationBuildingDescription.count(), buildingCompositeService.count()
    }

    @Test
    void testGetInvalidGuid() {
        try {
            buildingCompositeService.get( 'Invalid-guid' )
        } catch (ApplicationException ae) {
            assertApplicationException ae, "NotFoundException"
        }
    }

    @Test
    void testGetNullGuid() {
        try {
            buildingCompositeService.get( null )
        } catch (ApplicationException ae) {
            assertApplicationException ae, "NotFoundException"
        }
    }

    @Test
    void testGetInvalidNonExistentHousingLocationBuildingDescription() {
        HousingLocationBuildingDescription housingLocationBuildingDescription = HousingLocationBuildingDescription.findByBuilding( net.hedtech.banner.general.system.Building.findByCode( 'NORTH' ) )
        assertNotNull housingLocationBuildingDescription.id
        def id = housingLocationBuildingDescription.id
        String guid = GlobalUniqueIdentifier.findByLdmNameAndDomainId( 'buildings', id ).guid
        assertNotNull guid

        housingLocationBuildingDescription.delete( flush: true )
        assertNull HousingLocationBuildingDescription.get( id )

        try {
            buildingCompositeService.get( guid )
        } catch (ApplicationException ae) {
            assertApplicationException ae, "NotFoundException"
        }
    }

    @Test
    void testGet() {
        def paginationParams = [max: '1', offset: '0']
        List<BuildingDetail> buildings = buildingCompositeService.list( paginationParams )
        assertNotNull buildings
        assertFalse buildings.isEmpty()

        assertNotNull buildings[0].guid
        BuildingDetail building = buildingCompositeService.get( buildings[0].guid )
        assertNotNull building
        assertEquals buildings[0], building
        assertEquals buildings[0].guid, building.guid
        assertEquals buildings[0].rooms, building.rooms
        assertEquals buildings[0].metadata, building.metadata
        assertEquals buildings[0].site, building.site
        assertEquals buildings[0].building.code, building.building.code
        assertNotNull building.guid
        assertNotSame '', building.guid
    }

    @Test
    void testFetchByBuildingId() {
        BuildingDetail building = buildingCompositeService.fetchByBuildingId( i_success_housingLocationBuildingDescription.id )
        assertNotNull building
        assertNotNull building.guid
        assertEquals i_success_housingLocationBuildingDescription.id, building.id
        assertEquals i_success_housingLocationBuildingDescription.building.code, building.building.code
        assertEquals i_success_housingLocationBuildingDescription.building.dataOrigin, building.metadata.dataOrigin
    }

    @Test
    void testInvalidFetchByBuildingId() {
        assertNull buildingCompositeService.fetchByBuildingId( -1 )
        assertNull buildingCompositeService.fetchByBuildingId( null )
    }

    @Test
    void testFetchByBuildingCode() {
        BuildingDetail building = buildingCompositeService.fetchByBuildingCode( i_success_housingLocationBuildingDescription.building.code )
        assertNotNull building
        assertNotNull building.guid
        assertEquals i_success_housingLocationBuildingDescription.id, building.id
        assertEquals i_success_housingLocationBuildingDescription.building.code, building.building.code
        assertEquals i_success_housingLocationBuildingDescription.building.dataOrigin, building.metadata.dataOrigin
    }

    @Test
    void testInvalidFetchByBuildingCode() {
        assertNull buildingCompositeService.fetchByBuildingCode( '' )
        assertNull buildingCompositeService.fetchByBuildingCode( null )
    }

    @Test
    void testFetchAllCampus() {
        def paginationParams = [max: '20', offset: '0']

        List buildings = buildingCompositeService.fetchByCampusCode(i_success_housingLocationBuildingDescription.campus.code )
        assertNotNull buildings
        assertFalse buildings.isEmpty()
        assertTrue buildings.size() <= 20
    }



}

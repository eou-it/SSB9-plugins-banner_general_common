/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.HousingLocationBuildingDescription
import net.hedtech.banner.general.overall.ldm.v1.Building
import net.hedtech.banner.testing.BaseIntegrationTestCase

class BuildingCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    HousingLocationBuildingDescription i_success_housingLocationBuildingDescription
    def buildingCompositeService


    void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        initiializeDataReferences()
    }


    private void initiializeDataReferences() {
        net.hedtech.banner.general.system.Building building = net.hedtech.banner.general.system.Building.findByCode( 'CIS' )
        i_success_housingLocationBuildingDescription = HousingLocationBuildingDescription.findByBuilding( building )
    }


    void testListWithoutPaginationParams() {
        List buildings = buildingCompositeService.list( [:] )
        assertNotNull buildings
        assertFalse buildings.isEmpty()
        assertEquals 10, buildings.size()
    }


    void testList() {
        def paginationParams = [max: '20', offset: '0']
        List buildings = buildingCompositeService.list( paginationParams )
        assertNotNull buildings
        assertFalse buildings.isEmpty()
        assertTrue buildings.size() <= 20
    }


    void testCount() {
        assertNotNull i_success_housingLocationBuildingDescription
        assertTrue buildingCompositeService.count() > 0
    }


    void testGetInvalidGuid() {
        try {
            buildingCompositeService.get( 'Invalid-guid' )
        } catch (ApplicationException ae) {
            assertApplicationException ae, "NotFoundException"
        }
    }


    void testGetNullGuid() {
        try {
            buildingCompositeService.get( null )
        } catch (ApplicationException ae) {
            assertApplicationException ae, "NotFoundException"
        }
    }


    void testGetInvalidNonExistentHousingLocationBuildingDescription() {
        HousingLocationBuildingDescription housingLocationBuildingDescription = i_success_housingLocationBuildingDescription
        assertNotNull housingLocationBuildingDescription.id
        def id = i_success_housingLocationBuildingDescription.id
        String guid = GlobalUniqueIdentifier.findByLdmNameAndDomainId( 'buildings', i_success_housingLocationBuildingDescription.id ).guid
        assertNotNull guid

        housingLocationBuildingDescription.delete( flush: true )
        assertNull HousingLocationBuildingDescription.get( id )

        try {
            buildingCompositeService.get( guid )
        } catch (ApplicationException ae) {
            assertApplicationException ae, "NotFoundException"
        }
    }


    void testGet() {
        def paginationParams = [max: '1', offset: '0']
        List<Building> buildings = buildingCompositeService.list( paginationParams )
        assertNotNull buildings
        assertFalse buildings.isEmpty()

        assertNotNull buildings[0].guid
        Building building = buildingCompositeService.get( buildings[0].guid )
        assertNotNull building
        assertEquals buildings[0], building

        assertNotNull building.guid
        assertNotSame '', building.guid
    }


    void testFetchByBuildingId() {
        Building building = buildingCompositeService.fetchByBuildingId( i_success_housingLocationBuildingDescription.id )
        assertNotNull building
        assertNotNull building.guid
        assertEquals i_success_housingLocationBuildingDescription.id, building.id
    }


    void testInvalidFetchByBuildingId() {
        assertNull buildingCompositeService.fetchByBuildingId( -1 )
        assertNull buildingCompositeService.fetchByBuildingId( null )
    }


    void testFetchByBuildingCode() {
        Building building = buildingCompositeService.fetchByBuildingCode( i_success_housingLocationBuildingDescription.building.code )
        assertNotNull building
        assertNotNull building.guid
        assertEquals i_success_housingLocationBuildingDescription.id, building.id
        assertEquals i_success_housingLocationBuildingDescription.building.code, building.building.code
    }


    void testInvalidFetchByBuildingCode() {
        assertNull buildingCompositeService.fetchByBuildingCode( '' )
        assertNull buildingCompositeService.fetchByBuildingCode( null )
    }

}

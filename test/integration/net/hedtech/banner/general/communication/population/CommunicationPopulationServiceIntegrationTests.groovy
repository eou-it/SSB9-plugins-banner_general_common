/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Integration tests for PopulationService service
 */
class CommunicationPopulationServiceIntegrationTests extends BaseIntegrationTestCase {

    def selfServiceBannerAuthenticationProvider
    def communicationPopulationService

    def CommunicationFolder testFolder


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        testFolder = CommunicationManagementTestingSupport.newValidForCreateFolderWithSave()
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testList() {
        def expList = communicationPopulationService.list( sort: "name", order: "asc" )
        def originalCount = expList.size()
        def population = newPopulation("TEST" )
        population = communicationPopulationService.create( [domainModel: population] )

        expList = communicationPopulationService.list( sort: "name", order: "asc" )
        assertNotNull expList
        assertTrue originalCount + 1 == expList.size()
    }


    @Test
    void testCreatePopulationWithBcmAdmin() {
        def population = newPopulation("TEST" )
        population = communicationPopulationService.create( [domainModel: population] )

        // Assert domain values
        assertNotNull population?.id
        assertEquals testFolder.name, population.folder.name
        assertEquals getUser(), population.createdBy
        assertEquals "Population Description", population.description
        assertEquals "TEST", population.name
    }


    @Test
    void testCreatePopulationWithBcmUser() {
        logout()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMUSER', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        def population = newPopulation("TEST" )
        population = communicationPopulationService.create( [domainModel: population] )

        // Assert domain values
        assertNotNull population?.id
        assertEquals testFolder.name, population.folder.name
        assertEquals 'BCMUSER', population.createdBy
        assertEquals "Population Description", population.description
        assertEquals "TEST", population.name
    }


    @Test
    void testCreatePopulationWithGrailsUser() {
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('grails_user', 'u_pick_it'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        def population = newPopulation("TEST" )
        try {
            population = communicationPopulationService.create( [domainModel: population] )
            fail( 'expected failure' )
        } catch (ApplicationException e) {
            assertTrue(e.getMessage().toString().contains("operation.not.authorized"))
        }
    }

    @Test
    void testCreateWithMissingFolder() {
        def population = newPopulation("TEST" )
        population.folder = null
        def message = shouldFail( ApplicationException ) {
            communicationPopulationService.create( [domainModel: population] )
        }
        assertEquals "Incorrect failure message returned", "@@r1:folderCannotBeNull@@", message
    }


    @Test
    void testUpdatePopulation() {
        def population = newPopulation("TEST" )
        population = communicationPopulationService.create( [domainModel: population] )
        assertNotNull population
        def id = population.id

        // Find the domain
        population = population.get( id )
        assertNotNull population?.id

        // Update domain values
        population.description = "###"
        population = communicationPopulationService.update( [domainModel: population] )

        // Find the updated domain
        population = population.get( id )

        // Assert updated domain values
        assertNotNull population
        assertEquals "###", population.description
    }


    @Test
    void testDeletePopulation() {

        def population = newPopulation("TEST" )

        population = communicationPopulationService.create( [domainModel: population] )
        assertNotNull population
        def id = population.id

        // Find the domain
        population = population.get( id )
        assertNotNull population


        def population1 = newPopulation("TEST 1" )
        population1 = communicationPopulationService.create( [domainModel: population1] )
        assertNotNull population1
        def id1 = population1.id

        // Find the domain
        population1 = population1.get( id )
        assertNotNull population1

        // Delete the domain
        communicationPopulationService.delete( [domainModel: population] )

        // Attempt to find the deleted domain
        population = population.get( id )
        assertNull population
    }

    @Test
    void testDynamicFinder() {

        def population = newPopulation("TestName1" )
        communicationPopulationService.create( [domainModel: population] )
        def population1 = newPopulation("TestName2" )
        communicationPopulationService.create( [domainModel: population1] )
        // Find the domain
        def List<CommunicationPopulation> populations = population.findAllByPopulationName( "TestName" )
        assertNotNull populations
        assertEquals( 2, populations.size() )
    }


    private String getUser() {
        return 'BCMADMIN'
    }


    private def newPopulation(String populationName) {
        def population = new CommunicationPopulation(
                // Required fields
                folder: testFolder,
                name: populationName,
                // Nullable fields
                description: "Population Description",
        )
        return population
    }
}

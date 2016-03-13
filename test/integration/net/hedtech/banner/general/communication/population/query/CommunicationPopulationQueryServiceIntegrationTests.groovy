/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population.query

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.population.CommunicationPopulation
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Integration tests for PopulationQueryService service
 */
class CommunicationPopulationQueryServiceIntegrationTests extends BaseIntegrationTestCase {

    def selfServiceBannerAuthenticationProvider
    def communicationPopulationQueryService
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
        def expList = communicationPopulationQueryService.list( sort: "name", order: "asc" )
        def originalCount = expList.size()
        def populationQuery = newPopulationQuery( false, false, "TTTTTTTTTT" )
        populationQuery = communicationPopulationQueryService.create( [domainModel: populationQuery] )

        expList = communicationPopulationQueryService.list( sort: "name", order: "asc" )
        assertNotNull expList
        assertTrue originalCount + 1 == expList.size()
    }


    @Test
    void testCreatePopulationQuery() {
        def populationQuery = newPopulationQuery( true, true, "TTTTTTTTTT" )
        populationQuery = communicationPopulationQueryService.create( [domainModel: populationQuery] )

        // Assert domain values
        assertNotNull populationQuery?.id
        assertEquals testFolder.name, populationQuery.folder.name
        assertEquals getUser(), populationQuery.createdBy
        assertEquals "TTTTTTTTTT", populationQuery.description
        assertEquals "TTTTTTTTTT", populationQuery.name
    }


    @Test
    void testCreateWithMissingFolder() {
        def populationQuery = newPopulationQuery( true, true, "TTTTTTTTTT" )
        populationQuery.folder = null
        def message = shouldFail( ApplicationException ) {
            communicationPopulationQueryService.create( [domainModel: populationQuery] )
        }
        assertEquals "Incorrect failure message returned", "@@r1:folderCannotBeNull@@", message
    }


    @Test
    void testUpdatePopulationQuery() {
        def populationQuery = newPopulationQuery( false, false, "TTTTTTTTTT" )
        populationQuery = communicationPopulationQueryService.create( [domainModel: populationQuery] )
        assertNotNull populationQuery
        def id = populationQuery.id

        // Find the domain
        populationQuery = populationQuery.get( id )
        assertNotNull populationQuery?.id

        // Update domain values
        populationQuery.description = "###"
        populationQuery = communicationPopulationQueryService.update( [domainModel: populationQuery] )

        // Find the updated domain
        populationQuery = populationQuery.get( id )

        // Assert updated domain values
        assertNotNull populationQuery
        assertEquals "###", populationQuery.description
    }


    @Test
    void testUpdateInvalidStatementPopulationQuery() {
        def populationQuery = newPopulationQuery( false, false, "TTTTTTTTTT" )
        populationQuery = communicationPopulationQueryService.create( [domainModel: populationQuery] )
        assertNotNull populationQuery
        def id = populationQuery.id

        // Find the domain
        populationQuery = populationQuery.get( id )
        assertNotNull populationQuery?.id
    }


    @Test
    void testUpdateValidStatementPopulationQuery() {
        def populationQuery = newPopulationQuery( false, false, "TTTTTTTTTT" )
        populationQuery = communicationPopulationQueryService.create( [domainModel: populationQuery] )
        assertNotNull populationQuery
        def id = populationQuery.id

        // Find the domain
        populationQuery = populationQuery.get( id )
        assertNotNull populationQuery?.id
    }


    @Test
    void testDeletePopulationQuery() {

        def populationQuery = newPopulationQuery( false, false, "TTTTTTTTTT" )

        populationQuery = communicationPopulationQueryService.create( [domainModel: populationQuery] )
        assertNotNull populationQuery
        def id = populationQuery.id

        // Find the domain
        populationQuery = populationQuery.get( id )
        assertNotNull populationQuery


        def populationQuery1 = newPopulationQuery( false, false, "MMMMMMMMMM" )
        populationQuery1 = communicationPopulationQueryService.create( [domainModel: populationQuery1] )
        assertNotNull populationQuery1
        def id1 = populationQuery1.id

        // Find the domain
        populationQuery1 = populationQuery1.get( id )
        assertNotNull populationQuery1

        // Delete the domain
        communicationPopulationQueryService.delete( [domainModel: populationQuery] )

        // Attempt to find the deleted domain
        populationQuery = populationQuery.get( id )
        assertNull populationQuery
    }

    @Test
    void testDynamicFinder() {

        def populationQuery = newPopulationQuery( false, false, "TestName1" )
        communicationPopulationQueryService.create( [domainModel: populationQuery] )
        def populationQuery1 = newPopulationQuery( false, false, "TestName2" )
        communicationPopulationQueryService.create( [domainModel: populationQuery1] )
        // Find the domain
        def List<CommunicationPopulationQuery> populationQueries = populationQuery.findAllByQueryName( "TestName" )
        assertNotNull populationQueries
        assertEquals( 2, populationQueries.size() )
    }


    private String getUser() {
        return 'BCMADMIN'
    }


    private def newPopulationQuery( boolean locked, boolean published, String queryName ) {
        def populationQuery = new CommunicationPopulationQuery(
                // Required fields
                folder: testFolder,
                name: queryName,
                changesPending: false,

                // Nullable fields
                description: "TTTTTTTTTT",
                sqlString: ""
        )

        return populationQuery
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

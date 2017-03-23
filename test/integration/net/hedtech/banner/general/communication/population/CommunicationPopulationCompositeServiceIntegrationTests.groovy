/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.population.query.*
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionListEntry
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Integration tests for PopulationQueryService service
 */
class CommunicationPopulationCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def selfServiceBannerAuthenticationProvider
    def communicationPopulationCompositeService
    def CommunicationFolder testFolder


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken( getUser(), '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        testFolder = CommunicationManagementTestingSupport.newValidForCreateFolderWithSave()
    }

    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    @Test
    public void testCreateEmptyPopulation() {
        CommunicationPopulation population = communicationPopulationCompositeService.createPopulation( testFolder, "testPopulation", "testPopulation description" )
        assertNotNull population.id
        assertEquals( testFolder, population.folder )
        assertEquals( "testPopulation", population.name )
        assertEquals( "testPopulation description", population.description )
        assertEquals( true, population.changesPending )
    }

    @Test void testAddPersonToIncludeList() {
        CommunicationPopulation population = communicationPopulationCompositeService.createPopulation( testFolder, "testPopulation", "testPopulation description" )
        population = communicationPopulationCompositeService.addPersonToIncludeList( population, 'BCMADMIN' )
        assertNotNull( population.includeList )

        ArrayList entries = CommunicationPopulationSelectionListEntry.findAllByPopulationSelectionList( population.includeList )
        assertEquals( 1, entries.size() )

        population = communicationPopulationCompositeService.addPersonToIncludeList( population, 'BCMUSER' )

        entries = CommunicationPopulationSelectionListEntry.findAllByPopulationSelectionList( population.includeList, [sort: "lastModified", order: "asc"] )
        assertEquals( 2, entries.size() )
        assertEquals( PersonUtility.getPerson( 'BCMADMIN' ).pidm, entries.get( 0 ).pidm )
        assertEquals( PersonUtility.getPerson( 'BCMUSER' ).pidm, entries.get( 1 ).pidm )
    }

    @Test void testAddPersonsToIncludeList() {
        CommunicationPopulation population = communicationPopulationCompositeService.createPopulation( testFolder, "testPopulation", "testPopulation description" )
        List<String> persons = ['BCMADMIN', 'BCMUSER', 'BCMAUTHOR']

        CommunicationPopulationSelectionListBulkResults results = communicationPopulationCompositeService.addPersonsToIncludeList( population, persons )
        assertNotNull( results.population.includeList )
        def entryCount = CommunicationPopulationSelectionListEntry.countByPopulationSelectionList( population.includeList )
        assertEquals( 3, entryCount )
        assertEquals( 3, results.insertedCount )

        persons = [ 'CMOORE', '710000051' ]
        results = communicationPopulationCompositeService.addPersonsToIncludeList( population, persons )
        entryCount = CommunicationPopulationSelectionListEntry.countByPopulationSelectionList( results.population.includeList )
        assertEquals( 4, entryCount )
        assertEquals( 1, results.insertedCount )
        assertEquals( 1, results.notExistCount )
        assertEquals( 1, results.ignoredCount)

        persons = [ 'CMOORE', '710000051','BCMADMIN', 'BCMUSER' ]
        results = communicationPopulationCompositeService.addPersonsToIncludeList( population, persons )
        entryCount = CommunicationPopulationSelectionListEntry.countByPopulationSelectionList( results.population.includeList )
        assertEquals( 4, entryCount )
        assertEquals( 0, results.insertedCount )
        assertEquals( 1, results.notExistCount )
        assertEquals( 3, results.duplicateCount)
        assertEquals( 4, results.ignoredCount )

        persons = [ 'BCMEMPL','CMOORE', '710000051','BCMADMIN', 'BCMUSER', 'BCMEMPL' ]
        results = communicationPopulationCompositeService.addPersonsToIncludeList( population, persons )
        entryCount = CommunicationPopulationSelectionListEntry.countByPopulationSelectionList( results.population.includeList )
        assertEquals( 5, entryCount )
        assertEquals( 1, results.insertedCount )
        assertEquals( 1, results.notExistCount )
        assertEquals( 4, results.duplicateCount)
        assertEquals( 5, results.ignoredCount )
    }

    @Test void testRemovePersonFromIncludeList() {
        CommunicationPopulation population = communicationPopulationCompositeService.createPopulation( testFolder, "testPopulation", "testPopulation description" )
        population = communicationPopulationCompositeService.addPersonToIncludeList( population, 'BCMADMIN' )
        population = communicationPopulationCompositeService.addPersonToIncludeList( population, 'BCMUSER' )

        def entryCount = CommunicationPopulationSelectionListEntry.countByPopulationSelectionList( population.includeList )
        assertEquals( 2, entryCount )

        population = communicationPopulationCompositeService.removePersonFromIncludeList( population, 'BCMUSER' )
        entryCount = CommunicationPopulationSelectionListEntry.countByPopulationSelectionList( population.includeList )
        assertEquals( 1, entryCount )

        try {
            population = communicationPopulationCompositeService.removePersonFromIncludeList( population, 'MBRZYCKI' )
            fail "Expected application exception"
        } catch (ApplicationException e) {
            assertEquals( "@@r1:bannerIdNotFound:MBRZYCKI@@", e.message )
        }

        population = communicationPopulationCompositeService.removePersonFromIncludeList( population, 'BCMADMIN' )
        entryCount = CommunicationPopulationSelectionListEntry.countByPopulationSelectionList( population.includeList )
        assertEquals( 0, entryCount )

        population = communicationPopulationCompositeService.removePersonFromIncludeList( population, 'BCMADMIN' )
        entryCount = CommunicationPopulationSelectionListEntry.countByPopulationSelectionList( population.includeList )
        assertEquals( 0, entryCount )
    }

    @Test void testRemoveAllPersonsFromIncludeList() {
        CommunicationPopulation population = communicationPopulationCompositeService.createPopulation( testFolder, "testPopulation", "testPopulation description" )
        population = communicationPopulationCompositeService.removeAllPersonsFromIncludeList( population )
        def entryCount = CommunicationPopulationSelectionListEntry.countByPopulationSelectionList( population.includeList )
        assertEquals( 0, entryCount )

        List<String> persons = ['BCMADMIN', 'BCMUSER', 'BCMAUTHOR']
        CommunicationPopulationSelectionListBulkResults results = communicationPopulationCompositeService.addPersonsToIncludeList( population, persons )

        entryCount = CommunicationPopulationSelectionListEntry.countByPopulationSelectionList( population.includeList )
        assertEquals( 3, entryCount )

        population = communicationPopulationCompositeService.removeAllPersonsFromIncludeList( population )
        entryCount = CommunicationPopulationSelectionListEntry.countByPopulationSelectionList( population.includeList )
        assertEquals( 0, entryCount )
    }

    private String getUser() {
        return 'BCMADMIN'
    }

}

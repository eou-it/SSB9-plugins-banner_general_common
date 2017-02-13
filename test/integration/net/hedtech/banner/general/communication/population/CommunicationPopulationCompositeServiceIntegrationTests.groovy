/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import net.hedtech.banner.exceptions.ApplicationException
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

    private String getUser() {
        return 'BCMADMIN'
    }

}

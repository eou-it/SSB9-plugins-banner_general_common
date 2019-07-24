/*******************************************************************************
 Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import static groovy.test.GroovyAssert.*
import grails.util.GrailsWebMockUtil
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import grails.web.servlet.context.GrailsWebApplicationContext

/**
 * Integration tests for PopulationService service
 */
@Integration
@Rollback
class CommunicationPopulationServiceIntegrationTests extends BaseIntegrationTestCase {

    def selfServiceBannerAuthenticationProvider
    def communicationPopulationService

    def CommunicationFolder testFolder


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        webAppCtx = new GrailsWebApplicationContext()
        mockRequest()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
    }

    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    public GrailsWebRequest mockRequest() {
        GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();
        GrailsWebMockUtil.bindMockWebRequest(webAppCtx, mockRequest, mockResponse)
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
        assertFalse population.systemIndicator
    }


    @Test
    void testCreatePopulationWithBcmUser() {
        logout()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMUSER', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        def population = newPopulation("TEST" )
        population = communicationPopulationService.create( [domainModel: population] )
        System.err.println(population)
        // Assert domain values
        assertNotNull population?.id
        assertEquals testFolder.name, population.folder.name
        assertEquals 'BCMUSER', population.createdBy
        assertEquals "Population Description", population.description
        assertEquals "TEST", population.name
        assertFalse population.systemIndicator
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

        def population2 = newPopulation( "Duplicate Query" )
        population2 = communicationPopulationService.create( [domainModel: population2] )
        assertNotNull population2
        def id2 = population2.id

        // Find the domain
        population2 = population2.get( id2 )
        assertNotNull population2?.id

        // Update domain values
        population2.name = population.name
        try {
            population2 = communicationPopulationService.update( [domainModel: population2] )
            Assert.fail "Expected sameNameFolder to fail because of name unique constraint."
        } catch (ApplicationException e) {
            assertEquals("@@r1:not.unique.message@@", e.message)
        }
        
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
        testFolder = CommunicationManagementTestingSupport.newValidForCreateFolderWithSave()
        def population = new CommunicationPopulation(
                // Required fields
                folder: testFolder,
                name: populationName,
                systemIndicator: false,
                // Nullable fields
                description: "Population Description",
        )
        return population
    }
}

/*******************************************************************************
 Copyright 2016-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.sql.Sql
import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException
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
 * Integration tests for Population entity
 */
@Integration
@Rollback
class CommunicationPopulationIntegrationTests  extends BaseIntegrationTestCase {

    def testFolder
    def selfServiceBannerAuthenticationProvider

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

    void setUpData() {
        testFolder = CommunicationManagementTestingSupport.newValidForCreateFolder()
        testFolder.save()
    }

    @Test
    void testCreatePopulation() {
        def population = newPopulation("TEST")
        population.save(failOnError: true, flush: true)

        // Assert domain values
        assertEquals testFolder.name, population.folder.name
        assertEquals getUser(), population.createdBy
        assertEquals "TEST", population.name
        assertEquals "Population Description", population.description
        assertNotNull population.id
        assertNotNull population.createDate
        assertNotNull population.lastModifiedBy
        assertNotNull population.version
        assertNotNull population.dataOrigin
        assertNotNull population.lastModified
        assertFalse population.systemIndicator

    }


    @Test
    void testPopulationNamedQuery() {
        def population = newPopulation("TEST")
        population.save(failOnError: true, flush: true)

        // Assert domain values
        assertNotNull population?.id

        def fetchedList = CommunicationPopulation.findAll()
        assertNotNull(fetchedList)

        def fetchExp = CommunicationPopulation.fetchById(population?.id)
        assertNotNull(fetchExp)

        fetchedList = CommunicationPopulation.findAllByFolderName(testFolder.name)
        assertNotNull(fetchedList)
    }




    @Test
    void testUpdatePopulation() {
        def population = newPopulation("TEST")
        population.save(failOnError: true, flush: true)

        // Assert domain values
        assertNotNull population?.id
        def id = population.id

        // Find the domain
        population = CommunicationPopulation.get(id)
        assertNotNull population

        // Update domain values
        population.description = "###" //<<< Update the value
        population.save(failOnError: true, flush: true)

        // Find the updated domain
        population = CommunicationPopulation.get(id)

        // Assert updated domain values
        assertNotNull population?.id
        assertEquals("###", population.description) //<<< Assert updated value
    }

    @Test
    void testExistsAnotherNameFolder() {
        def population = newPopulation("TEST")
        population.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull population.id

        Boolean falseResult = CommunicationPopulation.existsAnotherNameFolder(population.id, population.name, population.folder.name)
        assertFalse(falseResult)

        def population2 = newPopulation("TEST")
        population2.name = "Duplicate Folder"
        population2.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull population2.id

        Boolean trueResult = CommunicationPopulation.existsAnotherNameFolder(population.id, population2.name, population2.folder.name)
        assertTrue(trueResult)
    }

    @Test
    void testDeletePopulation() {
        def population = newPopulation("TEST")
        population.save(failOnError: true, flush: true)

        // Assert domain values
        assertNotNull population?.id
        def id = population.id

        // Find the domain
        population = CommunicationPopulation.get(id)
        assertNotNull population

        def population1 = newPopulation("MMMMMMMMMMVVVVVV")
        population1.save(failOnError: true, flush: true)

        assertNotNull population1.get(id)

        // Delete the domain
        population.delete()

        // Attempt to find the deleted domain
        population = CommunicationPopulation.get(id)
        assertNull population
    }


    @Test
    void testNullValidationFailure() {
        // Instantiate an empty domain
        def population = new CommunicationPopulation()

        // Assert for domain validation
        assertFalse "Population should have failed null value validation", population.validate()

        // Assert for specific field validation
        assertErrorsFor population, 'nullable',
                [
                        'folder',
                        'createDate',
                        'createdBy',
                        'name',
                ]

    }

    @Test
    void testOptimisticLock() {
        def population = newPopulation("TEST")
        population.save(failOnError: true, flush: true)
        assertNotNull population?.id

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("UPDATE gcbpopl SET gcbpopl_version = 999 WHERE gcbpopl_surrogate_id = ?", [population.id])
        } finally {
            //sql?.close()
        }

        // Update the entity
        population.dataOrigin = "OPT_TEST"
        shouldFail(HibernateOptimisticLockingFailureException) { population.save(failOnError: true, flush: true) }
    }

    /*--------------------------------------------------------------*/


    private String getUser() {
        return 'BCMADMIN'
    }


    private def newPopulation(String populationName) {
        setUpData()
        def population = new CommunicationPopulation(
                // Required fields
                folder: testFolder,
                name: populationName,
                createDate: new Date(),
                createdBy: getUser(),
                systemIndicator: false,
                // Nullable fields
                description: "Population Description",
        )
        return population
    }

}

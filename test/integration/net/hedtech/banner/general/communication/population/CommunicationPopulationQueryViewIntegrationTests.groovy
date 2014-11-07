/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.hibernate.FlushMode
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for PopulationQueryView entity
 */
class CommunicationPopulationQueryViewIntegrationTests extends BaseIntegrationTestCase {

    def CommunicationFolder testFolder


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        testFolder = CommunicationManagementTestingSupport.newValidForCreateFolderWithSave()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testPopulationQueryView() {
        def populationQuery = newPopulationQuery("MyTestPop")
        populationQuery.save(failOnError: true, flush: true)

        // Assert domain values
        assertNotNull populationQuery?.id

        assertEquals testFolder.name, populationQuery.folder.name
        assertNotNull populationQuery.createDate
        assertEquals "TTTTTTTTTT", populationQuery.createdBy
        assertEquals "TTTTTTTTTT", populationQuery.description
        assertEquals "MyTestPop", populationQuery.name
        assertNull populationQuery.sqlString
        assertTrue populationQuery.valid

        def CommunicationPopulationQueryView queryView
        queryView = CommunicationPopulationQueryView.fetchById(populationQuery?.id)
        assertNotNull queryView
    }


    @Test
    void testPopulationQueryViewPagination() {

        def populationQuery = newPopulationQuery("MyTestPop")
        populationQuery.save(failOnError: true, flush: true)

        def queries = CommunicationPopulationQueryView.findByFilterPagingParams([params: [folderName: testFolder.name]],
                [sortColumn: "createdBy", sortDirection: "asc", max: 5, offset: 0])
        assertEquals 1, queries.size()
        def queryCount = CommunicationPopulationQueryView.countByFilterParams([params: [folderName: testFolder.name]])
        assertEquals 1, queryCount
    }


    @Test
    void testDeletePopulationQueryView() {
        sessionFactory.currentSession.flushMode = FlushMode.AUTO
        def populationQuery = newPopulationQuery("MyTestPop")
        populationQuery.save(failOnError: true, flush: true)

        // Assert domain values
        assertNotNull populationQuery?.id
        def id = populationQuery.id
        def userid = populationQuery.createdBy

        // Find the domain
        populationQuery = CommunicationPopulationQuery.get(id)
        assertNotNull populationQuery

        def view = CommunicationPopulationQueryView.fetchById(id)
        assertNotNull(view)
        assertNotNull(view.name)

        //sessionFactory.currentSession.flushMode = FlushMode.AUTO
        // Delete the domain
        populationQuery.delete(failOnError: true, flush: true)

        // Attempt to find the deleted domain
        def popQuery = CommunicationPopulationQuery.get(id)
        assertNull popQuery

        // Attempt to find the deleted domain
        def queryView = CommunicationPopulationQueryView.fetchById(id)
        assertNull queryView
    }


    @Test
    void testDynamicFinder() {
        def populationQuery = newPopulationQuery("TestName1")
        populationQuery.save(failOnError: true, flush: true)
        populationQuery = newPopulationQuery("TestName2")
        populationQuery.save(failOnError: true, flush: true)
        // Find the domain
        def List<CommunicationPopulationQuery> populationQueries = CommunicationPopulationQuery.findAllByQueryName("TestName")
        assertNotNull populationQueries
        assertEquals(2, populationQueries.size())
    }


    private def newPopulationQuery(String popName) {
        def populationQuery = new CommunicationPopulationQuery(
                // Required fields
                folder: testFolder,
                createDate: new Date(),
                createdBy: "TTTTTTTTTT",
                name: popName,
                valid: true,

                // Nullable fields
                description: "TTTTTTTTTT",
                sqlString: ""
        )

        return populationQuery
    }

}

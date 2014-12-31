/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.sql.Sql
import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Integration tests for PopulationQuery entity
 */
class CommunicationPopulationQueryIntegrationTests extends BaseIntegrationTestCase {

    def testFolder
    def selfServiceBannerAuthenticationProvider

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        testFolder = CommunicationManagementTestingSupport.newValidForCreateFolder()
        testFolder.save()
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testCreatePopulationQuery() {
        def populationQuery = newPopulationQuery("TTTTTTTTTT")
        populationQuery.save(failOnError: true, flush: true)

        // Assert domain values
        assertEquals testFolder.name, populationQuery.folder.name
        assertEquals getUser(), populationQuery.createdBy
        assertEquals "TTTTTTTTTT", populationQuery.description
        assertEquals "TTTTTTTTTT", populationQuery.name
        assertNull populationQuery.sqlString
        assertFalse populationQuery.valid
        assertNotNull populationQuery.id
        assertNotNull populationQuery.createDate
        assertNotNull populationQuery.lastModifiedBy
        assertNotNull populationQuery.version
        assertNotNull populationQuery.dataOrigin
        assertNotNull populationQuery.lastModified

    }


    @Test
    void testPopulationQueryNamedQuery() {
        def populationQuery = newPopulationQuery("TTTTTTTTTT")
        populationQuery.save(failOnError: true, flush: true)

        // Assert domain values
        assertNotNull populationQuery?.id

        def fetchedList = CommunicationPopulationQuery.findAll()
        assertNotNull(fetchedList)

        def fetchExp = CommunicationPopulationQuery.fetchById(populationQuery?.id)
        assertNotNull(fetchExp)

        fetchedList = CommunicationPopulationQuery.findAllByFolderName(testFolder.name)
        assertNotNull(fetchedList)
    }




    @Test
    void testUpdatePopulationQuery() {
        def populationQuery = newPopulationQuery("TTTTTTTTTT")
        populationQuery.save(failOnError: true, flush: true)

        // Assert domain values
        assertNotNull populationQuery?.id
        def id = populationQuery.id

        // Find the domain
        populationQuery = CommunicationPopulationQuery.get(id)
        assertNotNull populationQuery

        // Update domain values
        populationQuery.description = "###" //<<< Update the value
        populationQuery.save(failOnError: true, flush: true)

        // Find the updated domain
        populationQuery = CommunicationPopulationQuery.get(id)

        // Assert updated domain values
        assertNotNull populationQuery?.id
        assertEquals("###", populationQuery.description) //<<< Assert updated value
    }


    @Test
    void testDeletePopulationQuery() {
        def populationQuery = newPopulationQuery("TTTTTTTTTT")
        populationQuery.save(failOnError: true, flush: true)

        // Assert domain values
        assertNotNull populationQuery?.id
        def id = populationQuery.id

        // Find the domain
        populationQuery = CommunicationPopulationQuery.get(id)
        assertNotNull populationQuery

        def populationQuery1 = newPopulationQuery("MMMMMMMMMMVVVVVV")
        populationQuery1.save(failOnError: true, flush: true)

        assertNotNull populationQuery1.get(id)

        // Delete the domain
        populationQuery.delete()

        // Attempt to find the deleted domain
        populationQuery = CommunicationPopulationQuery.get(id)
        assertNull populationQuery
    }


    @Test
    void testDynamicFinder() {

        def populationQuery = newPopulationQuery("TestName1")
        populationQuery.save([domainModel: populationQuery])
        populationQuery = newPopulationQuery("TestName2")
        populationQuery.save([domainModel: populationQuery])
        // Find the domain
        def List<CommunicationPopulationQuery> populationQueries = CommunicationPopulationQuery.findAllByNameLike("TestName%")
        assertNotNull populationQueries
        assertEquals(2, populationQueries.size())
    }


    @Test
    void testNullValidationFailure() {
        // Instantiate an empty domain
        def populationQuery = new CommunicationPopulationQuery()

        // Assert for domain validation
        assertFalse "PopulationQuery should have failed null value validation", populationQuery.validate()

        // Assert for specific field validation
        assertErrorsFor populationQuery, 'nullable',
                [
                        'folder',
                        'createDate',
                        'createdBy',
                        'name',
                ]

    }


    @Test
    void testOptimisticLock() {
        def populationQuery = newPopulationQuery("TTTTTTTTTT")
        populationQuery.save(failOnError: true, flush: true)
        assertNotNull populationQuery?.id

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("UPDATE gcbqury SET gcbqury_version = 999 WHERE gcbqury_surrogate_id = ?", [populationQuery.id])
        } finally {
            sql?.close()
        }

        // Update the entity
        populationQuery.dataOrigin = "OPT_TEST"
        shouldFail(HibernateOptimisticLockingFailureException) { populationQuery.save(failOnError: true, flush: true) }
    }

    /*--------------------------------------------------------------*/


    private String getUser() {
        return 'BCMADMIN'
    }


    private def newPopulationQuery(String queryName) {
        def populationQuery = new CommunicationPopulationQuery(
                // Required fields
                folder: testFolder,
                name: queryName,
                createDate: new Date(),
                createdBy: getUser(),
                valid: false,

                // Nullable fields
                description: "TTTTTTTTTT",
                sqlString: ""
        )


        return populationQuery
    }

}

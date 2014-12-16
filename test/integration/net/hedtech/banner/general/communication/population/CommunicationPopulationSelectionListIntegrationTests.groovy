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
 * Integration tests for PopulationSelectionList entity
 */
class CommunicationPopulationSelectionListIntegrationTests extends BaseIntegrationTestCase {

    def CommunicationPopulationQuery globalTestPopulationQuery
    def selfServiceBannerAuthenticationProvider

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        globalTestPopulationQuery = newPopulation().save()
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testCreatePopulationSelectionList() {
        def populationSelectionList = newPopulationSelectionList()
        populationSelectionList.save(failOnError: true, flush: true)

        // Assert domain values
        assertNotNull populationSelectionList?.id
        assertEquals globalTestPopulationQuery.id, populationSelectionList.populationQueryId
        assertEquals CommunicationPopulationQueryExecutionStatus.PENDING_EXECUTION, populationSelectionList.status
    }


    @Test
    void testUpdatePopulationSelectionList() {
        def populationSelectionList = newPopulationSelectionList()
        populationSelectionList.save(failOnError: true, flush: true)

        // Assert domain values
        assertNotNull populationSelectionList?.id
        def id = populationSelectionList.id

        // Find the domain
        populationSelectionList = CommunicationPopulationSelectionList.get(id)
        assertNotNull populationSelectionList

        // Update domain values
        populationSelectionList.status = CommunicationPopulationQueryExecutionStatus.AVAILABLE //<<< Update the value
        populationSelectionList.save(failOnError: true, flush: true)

        // Find the updated domain
        populationSelectionList = CommunicationPopulationSelectionList.get(id)

        // Assert updated domain values
        assertNotNull populationSelectionList?.id
        assertEquals(CommunicationPopulationQueryExecutionStatus.AVAILABLE, populationSelectionList.status)
        //<<< Assert updated value
    }


    @Test
    void testDeletePopulationSelectionList() {
        def populationSelectionList = newPopulationSelectionList()
        populationSelectionList.save(failOnError: true, flush: true)

        // Assert domain values
        assertNotNull populationSelectionList?.id
        def id = populationSelectionList.id

        // Find the domain
        populationSelectionList = CommunicationPopulationSelectionList.get(id)
        assertNotNull populationSelectionList

        // Delete the domain
        populationSelectionList.delete()

        // Attempt to find the deleted domain
        populationSelectionList = CommunicationPopulationSelectionList.get(id)
        assertNull populationSelectionList
    }


    @Test
    void testNullValidationFailure() {
        // Instantiate an empty domain
        def populationSelectionList = new CommunicationPopulationSelectionList()

        // Assert for domain validation
        assertFalse "PopulationSelectionList should have failed null value validation", populationSelectionList.validate()

        // Assert for specific field validation
        assertErrorsFor populationSelectionList, 'nullable',
                [
                        'populationQueryId',
                ]
    }


    @Test
    void testMaxSizeValidationFailure() {
        def populationSelectionList = newPopulationSelectionList()

        // Set domain values to exceed maximum allowed length

        populationSelectionList.dataOrigin = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"

        // Assert for domain
        assertFalse "PopulationSelectionList should have failed max size validation", populationSelectionList.validate()

        // Assert for specific fields
        assertErrorsFor populationSelectionList, 'maxSize',
                [
                        'dataOrigin',
                ]
    }


    @Test
    void testOptimisticLock() {
        def populationSelectionList = newPopulationSelectionList()
        populationSelectionList.save(failOnError: true, flush: true)
        assertNotNull populationSelectionList?.id

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("UPDATE gcrslis SET gcrslis_version = 999 WHERE gcrslis_surrogate_id = ?", [populationSelectionList.id])
        } finally {
            sql?.close()
        }

        // Update the entity
        populationSelectionList.dataOrigin = "OPT_TEST"
        shouldFail(HibernateOptimisticLockingFailureException) {
            populationSelectionList.save(failOnError: true, flush: true)
        }
    }


    private def newPopulationSelectionList() {
        def populationSelectionList = new CommunicationPopulationSelectionList(
                // Required fields
                populationQueryId: globalTestPopulationQuery.id,

                // Nullable fields
                executedsql: "",
                status: CommunicationPopulationQueryExecutionStatus.PENDING_EXECUTION,
        )

        return populationSelectionList
    }


    private def newPopulation() {
        def populationQuery = new CommunicationPopulationQuery(
                // Required fields
                folder: CommunicationManagementTestingSupport.newValidForCreateFolderWithSave(),
                createDate: new Date(),
                createdBy: "TTTTTTTTTT",
                name: "TTTTTTTTTT",
                valid: true,

                // Nullable fields
                description: "TTTTTTTTTT",
                sqlString: "",
                syntaxErrors: "TTTTTTTTTT",
        )

        return populationQuery
    }

}
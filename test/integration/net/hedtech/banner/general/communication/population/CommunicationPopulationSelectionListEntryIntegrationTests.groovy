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

/**
 * Integration tests for PopulationSelectionListEntry entity
 */
class CommunicationPopulationSelectionListEntryIntegrationTests extends BaseIntegrationTestCase {
    def CommunicationPopulationQuery globalTestPopulationQuery
    def CommunicationPopulationSelectionList globalTestPopulationSelectionList


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()

        globalTestPopulationQuery = newPopulationQuery().save()
        globalTestPopulationSelectionList = newPopulationSelectionList()
        globalTestPopulationSelectionList.populationQueryId = globalTestPopulationQuery.id
        globalTestPopulationSelectionList.save()
        assertNotNull(globalTestPopulationSelectionList.id)
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreatePopulationSelectionListEntry() {

        def populationSelectionListEntry = newPopulationSelectionListEntry()
        populationSelectionListEntry.populationSelectionList = globalTestPopulationSelectionList
        populationSelectionListEntry.save()
        // Assert domain values
        assertNotNull populationSelectionListEntry?.id
        assertEquals 9199999999999999999, populationSelectionListEntry.pidm
        assertEquals globalTestPopulationSelectionList, populationSelectionListEntry.populationSelectionList
    }


    @Test
    void testDeletePopulationSelectionListEntry() {
        def populationSelectionListEntry = newPopulationSelectionListEntry()
        populationSelectionListEntry.populationSelectionList = globalTestPopulationSelectionList
        populationSelectionListEntry.save()

        // Assert domain values
        assertNotNull populationSelectionListEntry?.id
        def id = populationSelectionListEntry.id

        // Find the domain
        populationSelectionListEntry = CommunicationPopulationSelectionListEntry.get(id)
        assertNotNull populationSelectionListEntry

        // Delete the domain
        populationSelectionListEntry.delete()

        // Attempt to find the deleted domain
        populationSelectionListEntry = CommunicationPopulationSelectionListEntry.get(id)
        assertNull populationSelectionListEntry
    }

    /*
    Test the named query that returns entires for a selection list
     */


    @Test
    void testFetchBySelectionListId() {
        def populationSelectionListEntry1 = newPopulationSelectionListEntry()
        populationSelectionListEntry1.populationSelectionList = globalTestPopulationSelectionList
        populationSelectionListEntry1.save()
        def populationSelectionListEntry2 = newPopulationSelectionListEntry()
        populationSelectionListEntry2.pidm = 14
        populationSelectionListEntry2.populationSelectionList = globalTestPopulationSelectionList
        populationSelectionListEntry2.save()

        assertEquals(CommunicationPopulationSelectionListEntry.fetchBySelectionListId(globalTestPopulationSelectionList.id).size(), 2)
    }


    @Test
    void testNullValidationFailure() {
        // Instantiate an empty domain
        def populationSelectionListEntry = new CommunicationPopulationSelectionListEntry()

        // Assert for domain validation
        assertFalse "PopulationSelectionListEntry should have failed null value validation", populationSelectionListEntry.validate()

        // Assert for specific field validation
        assertErrorsFor populationSelectionListEntry, 'nullable',
                [
                        'pidm',
                        'populationSelectionList',
                ]
    }


    @Test
    void testOptimisticLock() {
        def populationSelectionListEntry = newPopulationSelectionListEntry()
        populationSelectionListEntry.populationSelectionList = globalTestPopulationSelectionList
        populationSelectionListEntry.save(failOnError: true, flush: true)
        assertNotNull populationSelectionListEntry?.id

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("UPDATE gcrlent SET gcrlent_version = 999 WHERE gcrlent_surrogate_id = ?", [populationSelectionListEntry.id])
        } finally {
            sql?.close()
        }

        // Update the entity
        populationSelectionListEntry.dataOrigin = "OPT_TEST"
        shouldFail(HibernateOptimisticLockingFailureException) {
            populationSelectionListEntry.save(failOnError: true, flush: true)
        }
    }


    private def newPopulationSelectionListEntry() {
        def populationSelectionListEntry = new CommunicationPopulationSelectionListEntry(
                // Required fields
                pidm: 9199999999999999999,

        )

        return populationSelectionListEntry
    }


    private def newPopulationQuery() {
        def populationQuery = new CommunicationPopulationQuery(
                // Required fields
                folder: CommunicationManagementTestingSupport.newValidForCreateFolderWithSave(),
                createDate: new Date(),
                createdBy: "TTTTTTTTTT",
                name: "TTTTTTTTTT",
                valid: true,

                // Nullable fields
                description: "TTTTTTTTTT",
                lastCalculatedBy: "TTTTTTTTTT",
                lastCalculatedTime: new Date(),
                sqlString: "",
                syntaxErrors: "TTTTTTTTTT",
        )

        return populationQuery
    }


    private def newPopulationSelectionList() {
        def populationSelectionList = new CommunicationPopulationSelectionList(
                // Required fields
                // Nullable fields
                status: CommunicationPopulationQueryExecutionStatus.PENDING_EXECUTION,
        )

        return populationSelectionList
    }

}

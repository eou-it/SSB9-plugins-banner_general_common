/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryExecutionStatus
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionList
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionListEntry
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Integration tests for PopulationSelectionListEntry entity
 */
class CommunicationPopulationListViewIntegrationTests extends BaseIntegrationTestCase {
    def CommunicationPopulationQuery globalTestPopulationQuery
    def CommunicationPopulationSelectionList globalTestPopulationSelectionList
    def CommunicationFolder folder
    def i_valid_foldername = "TestFolderName"
    def selfServiceBannerAuthenticationProvider


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()

        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        folder = newValidForCreateFolder()
        folder.save(failOnError: true, flush: true)
        //Test if the generated entity now has an id assigned
        assertNotNull folder.id
        globalTestPopulationQuery = newPopulationQuery().save(failOnError: true, flush: true)
        globalTestPopulationSelectionList = newPopulationSelectionList(globalTestPopulationQuery.name)
        globalTestPopulationSelectionList.save(failOnError: true, flush: true)
        assertNotNull(globalTestPopulationSelectionList.id)

    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testCreatePopulationSelectionListEntry() {

        def populationSelectionListEntry = newPopulationSelectionListEntry()
        populationSelectionListEntry.populationSelectionList = globalTestPopulationSelectionList
        populationSelectionListEntry.save(failOnError: true, flush: true)
        // Assert domain values
        assertNotNull populationSelectionListEntry?.id
        assertEquals 9199999999999999999, populationSelectionListEntry.pidm
        assertEquals globalTestPopulationSelectionList, populationSelectionListEntry.populationSelectionList

        def listView = CommunicationPopulationSelectionList.fetchById(populationSelectionListEntry.populationSelectionList.id)
        assertNotNull(listView)

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
                changesPending: true,

                // Nullable fields
                description: "TTTTTTTTTT",
                queryString: "",
        )

        return populationQuery
    }


    private def newPopulationSelectionList(String popname) {
        def populationSelectionList = new CommunicationPopulationSelectionList(
                // Required fields
                // Nullable fields
                name: popname,
                status: CommunicationPopulationQueryExecutionStatus.PENDING_EXECUTION,
        )

        return populationSelectionList
    }


    private def newValidForCreateFolder() {
        def folder = new CommunicationFolder(
                description: "Test Folder",
                internal: false,
                name: i_valid_foldername
        )
        return folder
    }

}

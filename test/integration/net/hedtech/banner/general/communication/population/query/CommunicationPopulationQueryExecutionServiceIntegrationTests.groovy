/*********************************************************************************
 Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.population.query

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionList
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionListEntry
import net.hedtech.banner.security.BannerUser
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder


class CommunicationPopulationQueryExecutionServiceIntegrationTests extends BaseIntegrationTestCase {
    def communicationPopulationQueryExecutionService
    def communicationPopulationQueryStatementParseService
    def communicationPopulationSelectionListService
    def communicationPopulationQueryCompositeService
    def i_success_sqlStatement = "select 2086 spriden_pidm from dual"
    def i_fail_sqlStatement = "select 2086 spriden_pidm kjlkj from dual"
    def i_fail_multiValueSqlStatement = "select 2086 spriden_pidm, sysdate from dual where select y, m from dual"
    def BannerUser
    def selfServiceBannerAuthenticationProvider


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('BCMADMIN', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)
        bannerUser = SecurityContextHolder?.context?.authentication?.principal as BannerUser;
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testValidParseString() {
        def populationQueryParseResult = communicationPopulationQueryStatementParseService.parse(i_success_sqlStatement)
        assertEquals("Y", populationQueryParseResult.status)
        assertNull(populationQueryParseResult.message)
        assertNotNull(populationQueryParseResult.cost)
        assertNotNull(populationQueryParseResult.cardinality)

    }


    @Test
    void testInvalidParseString() {
        def populationQueryParseResult = communicationPopulationQueryStatementParseService.parse(i_fail_sqlStatement)
        assertEquals("N", populationQueryParseResult.status)
        assertNotNull(populationQueryParseResult.message)
        assertEquals(0, populationQueryParseResult.cost)
        assertEquals(0, populationQueryParseResult.cardinality)
    }


    @Test
    void testExecuteMultiValueSqlStatement() {
        def populationQuery = newValidPopulationQuery()
        populationQuery.queryString = i_fail_multiValueSqlStatement
        shouldFail {
            def savedPopulationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        }
        def populationSelectionListId
        shouldFail {
            populationSelectionListId = communicationPopulationQueryExecutionService.execute(savedPopulationQuery.id)
        }

        assertNull(populationSelectionListId)
    }


    @Test
    void testExecute() {
        CommunicationPopulationQuery populationQuery = newValidPopulationQuery()
        CommunicationPopulationQuery savedPopulationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        savedPopulationQuery = communicationPopulationQueryCompositeService.publishPopulationQuery( savedPopulationQuery ).query
        savedPopulationQuery.refresh()

        //get the versions from the query
        List queryVersionList = CommunicationPopulationQueryVersion.findByQueryId( savedPopulationQuery.id )
        assertEquals( 1, queryVersionList.size() )
        CommunicationPopulationQueryVersion queryVersion = queryVersionList.get( 0 )

        //execute the sql of the version
        CommunicationPopulationQueryExecutionResult queryExecutionResult = communicationPopulationQueryExecutionService.execute(queryVersion.id)
        def populationQuerySelectionList = CommunicationPopulationSelectionList.fetchById(queryExecutionResult.selectionListId)
        assertNotNull populationQuerySelectionList
        assertEquals(queryExecutionResult.selectionListId, populationQuerySelectionList.id)

        /* Test that the new count values are populated in the selectionList */
        assertNotNull(populationQuerySelectionList.lastModifiedBy)
        /* make sure it created list entries */
        def populationQuerySelectionListEntry = CommunicationPopulationSelectionListEntry.fetchBySelectionListId(populationQuerySelectionList.id)
        assertNotNull populationQuerySelectionListEntry

        def calculatedPopulationQueryVersion = CommunicationPopulationQueryVersion.fetchById(queryVersion.id)
        calculatedPopulationQueryVersion.refresh()

        def queryVersionSelectionList = CommunicationPopulationSelectionList.fetchById(queryExecutionResult.selectionListId)
        assertNotNull queryVersionSelectionList
        assertEquals(queryExecutionResult.selectionListId, queryVersionSelectionList.id)

        /* Test that the new count values are populated in the selectionList */
        assertNotNull(queryVersionSelectionList.lastModifiedBy)
        /* make sure it created list entries */
        def queryVersionSelectionListEntry = CommunicationPopulationSelectionListEntry.fetchBySelectionListId(populationQuerySelectionList.id)
        assertNotNull populationQuerySelectionListEntry

    }


    @Test
    void testReExecute() {
        def populationQuery = newValidPopulationQuery()
        CommunicationPopulationQuery savedPopulationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        savedPopulationQuery = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery ).query

        //get the versions from the query
        List queryVersionList = CommunicationPopulationQueryVersion.findByQueryId( savedPopulationQuery.id )
        assertEquals( 1, queryVersionList.size() )
        CommunicationPopulationQueryVersion queryVersion = queryVersionList.get( 0 )

        communicationPopulationQueryExecutionService.execute(queryVersion.id)
        /* Now make sure you can execute the same populationQuery multiple times without error */
        communicationPopulationQueryExecutionService.execute(queryVersion.id)
        communicationPopulationQueryExecutionService.execute(queryVersion.id)
        communicationPopulationQueryExecutionService.execute(queryVersion.id)
    }


    @Test
    void testBadSqlExecute() {
        def populationQuery = newInvalidPopulationQuery()
        def savedPopulationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )

        def populationListId
        shouldFail(ApplicationException) {
            populationListId = communicationPopulationQueryExecutionService.executeQuery(savedPopulationQuery.id)
        }
        assertNull populationListId
        populationQuery = communicationPopulationQueryCompositeService.fetchPopulationQuery(savedPopulationQuery.id)
        populationQuery.refresh()
    }


    private def newValidPopulationQuery() {
        def populationQuery = new CommunicationPopulationQuery(
                // Required fields
                folder: CommunicationManagementTestingSupport.newValidForCreateFolderWithSave(),
                createDate: new Date(),
                createdBy: "TTTTTTTTTT",
                name: "TTTTTTTTTT",
                changesPending: true,

                // Nullable fields
                description: "TTTTTTTTTT",
                calculatedBy: "TTTTTTTTTT",
                lastCalculatedTime: new Date(),
                queryString: i_success_sqlStatement
        )

        return populationQuery
    }


    private def newInvalidPopulationQuery() {
        def populationQuery = new CommunicationPopulationQuery(
                // Required fields
                folder: CommunicationManagementTestingSupport.newValidForCreateFolderWithSave(),
                createDate: new Date(),
                createdBy: "TTTTTTTTTT",
                name: "TTTTTTTTTT",
                changesPending: false,

                // Nullable fields
                description: "TTTTTTTTTT",
                calculatedBy: "TTTTTTTTTT",
                lastCalculatedTime: new Date(),
                queryString: i_fail_sqlStatement
        )

        return populationQuery
    }

}

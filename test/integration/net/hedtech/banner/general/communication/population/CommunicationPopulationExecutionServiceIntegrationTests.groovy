/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.population

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.security.BannerUser
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder


class CommunicationPopulationExecutionServiceIntegrationTests extends BaseIntegrationTestCase {
    def communicationPopulationExecutionService
    def communicationPopulationQueryStatementParseService
    def communicationPopulationSelectionListService
    def communicationPopulationQueryService
    def i_success_sqlStatement = "select 2086 spriden_pidm from dual"
    def i_fail_sqlStatement = "select 2086 spriden_pidm kjlkj from dual"
    def i_fail_multiValueSqlStatement = "select 2086 spriden_pidm, sysdate from dual where select y, m from dual"
    def BannerUser
    def selfServiceBannerAuthenticationProvider


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
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
    void testValidParse() {
        def populationQuery = newValidPopulationQuery()
        def savedPopulationQuery = communicationPopulationQueryService.create([domainModel: populationQuery])

        def populationQueryParseResult = communicationPopulationExecutionService.parse(savedPopulationQuery.id)
        assertEquals("Y", populationQueryParseResult.status)
        assertNull(populationQueryParseResult.message)
        assertNotNull(populationQueryParseResult.cost)
        assertNotNull(populationQueryParseResult.cardinality)
        /* Make sure the populationQuery got flagged as good */
        def validatedPopulationQuery = communicationPopulationQueryService.get(savedPopulationQuery.id)
        assertTrue(validatedPopulationQuery.valid)
    }


    @Test
    void testInValidParse() {
        def populationQuery = newInvalidPopulationQuery()
        def savedPopulationQuery = communicationPopulationQueryService.create([domainModel: populationQuery])
        def populationQueryParseResult = communicationPopulationExecutionService.parse(savedPopulationQuery.id)
        assertEquals("N", populationQueryParseResult.status)
        assertNotNull(populationQueryParseResult.message)
        assertEquals(0, populationQueryParseResult.cost)
        assertEquals(0, populationQueryParseResult.cardinality)
        /* Make sure the populationQuery got flagged as bad */
        def validatedPopulationQuery = communicationPopulationQueryService.get(savedPopulationQuery.id)
        assertFalse(validatedPopulationQuery.valid)
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
        populationQuery.sqlString = i_fail_multiValueSqlStatement
        shouldFail {
            def savedPopulationQuery = communicationPopulationQueryService.create([domainModel: populationQuery])
        }
        def populationSelectionListId
        shouldFail {
            populationSelectionListId = communicationPopulationExecutionService.execute(savedPopulationQuery.id)
        }

        assertNull(populationSelectionListId)
    }


    @Test
    void testExecute() {
        def populationQuery = newValidPopulationQuery()
        def savedPopulationQuery = communicationPopulationQueryService.create([domainModel: populationQuery])
        def populationSelectionListId = communicationPopulationExecutionService.execute(savedPopulationQuery.id)
        savedPopulationQuery.refresh()

        def calculatedPopulationQuery = CommunicationPopulationQuery.fetchById(savedPopulationQuery.id)
        calculatedPopulationQuery.refresh()


        def populationQuerySelectionList = communicationPopulationSelectionListService.fetchByNameAndId(calculatedPopulationQuery.id, 'BCMADMIN')
        if (populationQuerySelectionList == null)
             populationQuerySelectionList = communicationPopulationSelectionListService.fetchByNameAndId(calculatedPopulationQuery.id, 'GRAILS_USER')
        assertNotNull populationQuerySelectionList
        assertEquals(populationSelectionListId, populationQuerySelectionList.id)

        /* Test that the new count values are populated in the selectionList */
        assertTrue(populationQuerySelectionList.lastCalculatedCount > 0)
        assertNotNull(populationQuerySelectionList.lastModifiedBy)
        assertNotNull(populationQuerySelectionList.lastCalculatedTime)
        /* make sure it created list entries */
        def populationQuerySelectionListEntry = CommunicationPopulationSelectionListEntry.fetchBySelectionListId(populationQuerySelectionList.id)
        assertNotNull populationQuerySelectionListEntry

    }


    @Test
    void testReExecute() {
        def populationQuery = newValidPopulationQuery()
        def savedPopulationQuery = communicationPopulationQueryService.create([domainModel: populationQuery])
        communicationPopulationExecutionService.execute(savedPopulationQuery.id)
        /* Now make sure you can execute the same populationQuery multiple times without error */
        communicationPopulationExecutionService.execute(savedPopulationQuery.id)
        communicationPopulationExecutionService.execute(savedPopulationQuery.id)
        communicationPopulationExecutionService.execute(savedPopulationQuery.id)
    }


    @Test
    void testBadSqlExecute() {
        def populationQuery = newInvalidPopulationQuery()
        def savedPopulationQuery = communicationPopulationQueryService.create([domainModel: populationQuery])

        def populationListId
        shouldFail(ApplicationException) {
            populationListId = communicationPopulationExecutionService.execute(savedPopulationQuery.id)
        }
        assertNull populationListId
        populationQuery = communicationPopulationQueryService.get(savedPopulationQuery.id)
        populationQuery.refresh()
    }


    private def newValidPopulationQuery() {
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
                sqlString: i_success_sqlStatement
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
                valid: false,

                // Nullable fields
                description: "TTTTTTTTTT",
                lastCalculatedBy: "TTTTTTTTTT",
                lastCalculatedTime: new Date(),
                sqlString: i_fail_sqlStatement
        )

        return populationQuery
    }

}

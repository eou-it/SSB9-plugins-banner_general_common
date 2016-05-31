/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population.query

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationManagementTestingSupport
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Integration tests for PopulationQueryService service
 */
class CommunicationPopulationQueryCompositeServiceIntegrationTests extends BaseIntegrationTestCase {

    def selfServiceBannerAuthenticationProvider
    def communicationPopulationQueryCompositeService
    def CommunicationFolder testFolder


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
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
    void testCreatePopulationQuery() {
        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
            name: "testCreatePopulationQuery",
            description: "testCreatePopulationQuery description",
            folder: testFolder,
            queryString: "select spriden_pidm from spriden where rownum < 6 and spriden_change_ind is null"
        )
        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        assertNotNull populationQuery?.id
        assertEquals testFolder.name, populationQuery.folder.name
        assertEquals getUser(), populationQuery.createdBy
        assertEquals "testCreatePopulationQuery", populationQuery.name
        assertEquals "testCreatePopulationQuery description", populationQuery.description
        assertEquals "select spriden_pidm from spriden where rownum < 6 and spriden_change_ind is null", populationQuery.queryString
    }

    @Test
    void testCreatePopulationSelectionExtractQuery() {
        CommunicationPopulationQueryExtractStatement extractStatement = new CommunicationPopulationQueryExtractStatement()
        extractStatement.application = 'ADMISSIONS'
        extractStatement.selection = '199610_APPLICANTS'
        extractStatement.creatorId = 'SAISUSR'
        extractStatement.userId = 'SAISUSR'
        String extractQueryString = extractStatement.getQueryString()

        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
                name: "testCreatePopulationSelectionExtractQuery",
                description: "testCreatePopulationSelectionExtractQuery description",
                folder: testFolder,
                queryString: extractQueryString,
                type: CommunicationPopulationQueryType.POPULATION_SELECTION_EXTRACT
        )
        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        assertNotNull populationQuery.id
        assertEquals testFolder.name, populationQuery.folder.name
        assertEquals getUser(), populationQuery.createdBy
        assertEquals "testCreatePopulationSelectionExtractQuery", populationQuery.name
        assertEquals "testCreatePopulationSelectionExtractQuery description", populationQuery.description
        assertEquals CommunicationPopulationQueryType.POPULATION_SELECTION_EXTRACT, populationQuery.type
        assertEquals extractQueryString, populationQuery.queryString
    }

    @Test
    void testCreatePopulationSelectionExtractInvalidQuery() {
        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
                name: "testCreatePopulationSelectionExtractQuery",
                description: "testCreatePopulationSelectionExtractQuery description",
                folder: testFolder,
                type: CommunicationPopulationQueryType.POPULATION_SELECTION_EXTRACT,
                queryString: "select spriden_pidm from spriden where rownum < 6 and spriden_change_ind is null"
        )
        try {
            populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
            fail( "Expected query string is not correct for type" )
        } catch (ApplicationException ae) {
            assertEquals( "@@r1:badSyntax@@", ae.getMessage() )
        }

        CommunicationPopulationQueryExtractStatement extractStatement = new CommunicationPopulationQueryExtractStatement()
        extractStatement.selection = '199610_APPLICANTS'
        extractStatement.creatorId = 'SAISUSR'
        extractStatement.userId = 'SAISUSR'
        populationQuery.queryString = extractStatement.getQueryString()
        try {
            populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
            fail( "Missing application" )
        } catch (ApplicationException ae) {
            assertEquals( "@@r1:emptyApplication@@", ae.getMessage() )
        }

        extractStatement = new CommunicationPopulationQueryExtractStatement()
        extractStatement.application = 'ADMISSIONS'
        extractStatement.creatorId = 'SAISUSR'
        extractStatement.userId = 'SAISUSR'
        populationQuery.queryString = extractStatement.getQueryString()
        try {
            populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
            fail( "Missing selection" )
        } catch (ApplicationException ae) {
            assertEquals( "@@r1:emptySelection@@", ae.getMessage() )
        }

        extractStatement = new CommunicationPopulationQueryExtractStatement()
        extractStatement.application = 'ADMISSIONS'
        extractStatement.selection = '199610_APPLICANTS'
        extractStatement.userId = 'SAISUSR'
        populationQuery.queryString = extractStatement.getQueryString()
        try {
            populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
            fail( "Missing creatorId" )
        } catch (ApplicationException ae) {
            assertEquals( "@@r1:emptyCreatorId@@", ae.getMessage() )
        }

        extractStatement = new CommunicationPopulationQueryExtractStatement()
        extractStatement.application = 'ADMISSIONS'
        extractStatement.selection = '199610_APPLICANTS'
        extractStatement.creatorId = 'SAISUSR'
        populationQuery.queryString = extractStatement.getQueryString()
        try {
            populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
            fail( "Missing userId" )
        } catch (ApplicationException ae) {
            assertEquals( "@@r1:emptyUserId@@", ae.getMessage() )
        }

        extractStatement = new CommunicationPopulationQueryExtractStatement()
        extractStatement.application = 'ADMISSIONS'
        extractStatement.selection = '199610_APPLICANTS'
        extractStatement.creatorId = 'SAISUSR'
        extractStatement.userId = 'SAISUSR'
        populationQuery.queryString = extractStatement.getQueryString()
        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        assertNotNull populationQuery.id
        assertEquals( extractStatement.getQueryString(), populationQuery.queryString )
    }

    @Test
    void testUpdatePopulationQuery() {
        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
            name: "testUpdatePopulationQuery",
            description: "testUpdatePopulationQuery description",
            folder: testFolder,
            queryString: "select spriden_pidm from spriden where rownum < 6 and spriden_change_ind is null"
        )
        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        assertNotNull( populationQuery.id )
        assertEquals( 0, populationQuery.version )

        Map queryAsMap = [
            id: populationQuery.id,
            name: "testUpdatePopulationQuery2",
            description: "testUpdatePopulationQuery description2",
            folder: populationQuery.folder,
            queryString: "select pidm from spriden",
            version: populationQuery.version,
            type: CommunicationPopulationQueryType.SQL_STATEMENT
        ]
        populationQuery = communicationPopulationQueryCompositeService.updatePopulationQuery( queryAsMap )
        assertNotNull( populationQuery.id )
        assertTrue( populationQuery.version > 0 )
        assertEquals "testUpdatePopulationQuery2", populationQuery.name
        assertEquals "testUpdatePopulationQuery description2", populationQuery.description
        assertEquals "select pidm from spriden", populationQuery.queryString
    }


    @Test
    void testPublishQuery() {
        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
                name: "testPublishQuery",
                folder: testFolder,
                queryString: "select spriden_pidm from spriden where rownum < 6 and spriden_change_ind is null"
        )
        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        assertTrue( populationQuery.changesPending )
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )
        assertFalse( populationQuery.changesPending )
        List queryVersionList = CommunicationPopulationQueryVersion.findByQueryId( populationQuery.id )
        assertEquals( 1, queryVersionList.size() )
        CommunicationPopulationQueryVersion fetchedQueryVersion = queryVersionList.get( 0 )
        assertEquals( queryVersion.id,fetchedQueryVersion.id )
        assertEquals( "select spriden_pidm from spriden where rownum < 6 and spriden_change_ind is null", fetchedQueryVersion.queryString )
        assertNotNull( fetchedQueryVersion.getCreateDate() )
    }

    @Test
    void testDeleteQuery() {
        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
                name: "testPublishQuery",
                folder: testFolder,
                queryString: "select spriden_pidm from spriden where rownum < 6 and spriden_change_ind is null"
        )
        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        assertTrue( populationQuery.changesPending )
        populationQuery = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery ).query
        assertFalse( populationQuery.changesPending )
        communicationPopulationQueryCompositeService.deletePopulationQuery(populationQuery)
        List queryVersionList = CommunicationPopulationQueryVersion.findByQueryId( populationQuery.id )
        assertEquals(0, queryVersionList.size())
        CommunicationPopulationQuery populationQuery1 = CommunicationPopulationQuery.fetchById(populationQuery.id)
        assertNull(populationQuery1)
    }

    @Test
    void testDeleteQueryWithCurrentVersion() {
        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
                name: "testPublishQuery",
                folder: testFolder,
                queryString: "select spriden_pidm from spriden where rownum < 2 and spriden_change_ind is null"
        )
        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        assertTrue( populationQuery.changesPending )
        communicationPopulationQueryCompositeService.deletePopulationQuery(populationQuery)
        List queryVersionList = CommunicationPopulationQueryVersion.findByQueryId( populationQuery.id )
        assertEquals(0, queryVersionList.size())
        CommunicationPopulationQuery populationQuery1 = CommunicationPopulationQuery.fetchById(populationQuery.id)
        assertNull(populationQuery1)
    }

    @Test
    void testDeleteQueryWithOnePublishedVersion() {
        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
                name: "testPublishQuery",
                folder: testFolder,
                queryString: "select spriden_pidm from spriden where rownum < 2 and spriden_change_ind is null"
        )
        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        assertTrue( populationQuery.changesPending )
        populationQuery = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery ).query
        assertFalse( populationQuery.changesPending )
        CommunicationPopulationQueryVersion queryVersion = CommunicationPopulationQueryVersion.findByQueryId( populationQuery.id ).get(0)

        communicationPopulationQueryCompositeService.deletePopulationQueryVersion(populationQuery, queryVersion)

        List queryVersionList = CommunicationPopulationQueryVersion.findByQueryId( populationQuery.id )
        assertEquals(0, queryVersionList.size())
        CommunicationPopulationQuery populationQuery1 = CommunicationPopulationQuery.fetchById(populationQuery.id)
        assertNull(populationQuery1)
    }

    @Test
    void testDeleteQueryWithOnePublishedAndCurrentVersion() {
        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
                name: "testPublishQuery",
                folder: testFolder,
                queryString: "select spriden_pidm from spriden where rownum < 2 and spriden_change_ind is null"
        )
        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )
        assertTrue( populationQuery.changesPending )
        populationQuery = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery ).query
        assertFalse( populationQuery.changesPending )

        Map queryAsMap = [
            id: populationQuery.id,
            queryString: "select spriden_pidm from spriden where rownum < 3 and spriden_change_ind is null",
            version: populationQuery.version,
            type: CommunicationPopulationQueryType.SQL_STATEMENT
        ]
        populationQuery = communicationPopulationQueryCompositeService.updatePopulationQuery(queryAsMap)
        communicationPopulationQueryCompositeService.deletePopulationQueryVersion(populationQuery.id, populationQuery.version, populationQuery.id, populationQuery.version)

        List queryVersionList = CommunicationPopulationQueryVersion.findByQueryId( populationQuery.id )
        assertEquals(1, queryVersionList.size())
        CommunicationPopulationQuery populationQuery1 = CommunicationPopulationQuery.fetchById(populationQuery.id)
        assertEquals "select spriden_pidm from spriden where rownum < 2 and spriden_change_ind is null", populationQuery1.queryString
    }

    @Test
    void testValidatePopulationQuery() {
        CommunicationPopulationQuery populationQuery = new CommunicationPopulationQuery(
                name: "testCreatePopulationQuery",
                description: "testCreatePopulationQuery description",
                folder: testFolder,
                queryString: "select spriden_pidm from spriden where rownum < 6 and spriden_change_ind is null"
        )
        populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( populationQuery )

        assertNotNull populationQuery?.id
        assertEquals testFolder.name, populationQuery.folder.name
        assertEquals getUser(), populationQuery.createdBy
        assertEquals "testCreatePopulationQuery description", populationQuery.description
        assertEquals "testCreatePopulationQuery", populationQuery.name
    }


    @Test
    void testValidateSqlStatement() {
        CommunicationPopulationQueryParseResult result = communicationPopulationQueryCompositeService.validateSqlStatement(
                "select spriden_pidm from spriden where rownum < 6 and spriden_change_ind is null"
        )
        assertEquals("Y", result.status)
        assertNull(result.message)
        assertEquals(0, result.cost)
        assertEquals(0, result.cardinality)
    }

    @Test
    void testIncompleteSqlStatement() {
        CommunicationPopulationQueryParseResult result = communicationPopulationQueryCompositeService.validateSqlStatement(
                "select bogus"
        )
        assertEquals("N", result.status)
        assertNotNull(result.message)
        assertEquals(0, result.cost)
        assertEquals(0, result.cardinality)
    }

    @Test
    void testColumnDoesNotExist() {
        CommunicationPopulationQueryParseResult result = communicationPopulationQueryCompositeService.validateSqlStatement(
                "select pidm from spriden"
        )
        assertEquals("N", result.status)
        assertTrue(result.message.contains("ORA-00904: \"PIDM\": invalid identifier"))
        assertEquals(0, result.cost)
        assertEquals(0, result.cardinality)
    }

    @Test
    void testWildcardInSql() {
        // this is throwing an exception instead of status of N
        CommunicationPopulationQueryParseResult result
        try {
            result = communicationPopulationQueryCompositeService.validateSqlStatement(
                    "select * from spriden"
            )
            fail("Expected application exception for having a wildcard")
        } catch (ApplicationException e) {
            // expected
        }
    }

    @Test
    void testMultipleColumnsInSql() {
        // this is throwing an exception instead of status of N
        CommunicationPopulationQueryParseResult result
        try {
            result = communicationPopulationQueryCompositeService.validateSqlStatement(
                    "select spriden_pidm, spriden_id from spriden"
            )
            fail("Expected application exception for having a wildcard")
        } catch (ApplicationException e) {
            // expected
        }
    }

    @Test
    void testMultipleStatementsInSql() {
        // this is throwing an exception instead of status of N
        CommunicationPopulationQueryParseResult result
        try {
            result = communicationPopulationQueryCompositeService.validateSqlStatement(
                    "select spriden_pidm from spriden; delete from spriden"
            )
            fail("Expected application exception for having multiple statements.")
        } catch (ApplicationException e) {
            // expected
        }
    }

    @Test
    void testNoPidmInSql() {
        // this is throwing an exception instead of status of N
        CommunicationPopulationQueryParseResult result
        try {
            result = communicationPopulationQueryCompositeService.validateSqlStatement(
                    "select spriden_id from spriden"
            )
            fail( "Expected application exception for not specifying a pidm." )
        } catch( ApplicationException e ) {
            // expected
        }
    }



//    @Test
//    void testList() {
//        def expList = communicationPopulationQueryCompositeService.list( sort: "name", order: "asc" )
//        def originalCount = expList.size()
//        def populationQuery = newPopulationQuery( false, false, "TTTTTTTTTT" )
//        populationQuery = communicationPopulationQueryCompositeService.create( [domainModel: populationQuery] )
//
//        expList = communicationPopulationQueryCompositeService.list( sort: "name", order: "asc" )
//        assertNotNull expList
//        assertTrue originalCount + 1 == expList.size()
//    }
//
//
//    @Test
//    void testCreateWithMissingFolder() {
//        def populationQuery = newPopulationQuery( true, true, "TTTTTTTTTT" )
//        populationQuery.folder = null
//        def message = shouldFail( ApplicationException ) {
//            communicationPopulationQueryCompositeService.create( [domainModel: populationQuery] )
//        }
//        assertEquals "Incorrect failure message returned", "@@r1:folderCannotBeNull@@", message
//    }
//
//
//    @Test
//    void testUpdatePopulationQuery() {
//        def populationQuery = newPopulationQuery( false, false, "TTTTTTTTTT" )
//        populationQuery = communicationPopulationQueryCompositeService.create( [domainModel: populationQuery] )
//        assertNotNull populationQuery
//        def id = populationQuery.id
//
//        // Find the domain
//        populationQuery = populationQuery.get( id )
//        assertNotNull populationQuery?.id
//
//        // Update domain values
//        populationQuery.description = "###"
//        populationQuery = communicationPopulationQueryCompositeService.update( [domainModel: populationQuery] )
//
//        // Find the updated domain
//        populationQuery = populationQuery.get( id )
//
//        // Assert updated domain values
//        assertNotNull populationQuery
//        assertEquals "###", populationQuery.description
//    }
//
//
//    @Test
//    void testUpdateInvalidStatementPopulationQuery() {
//        def populationQuery = newPopulationQuery( false, false, "TTTTTTTTTT" )
//        populationQuery = communicationPopulationQueryCompositeService.create( [domainModel: populationQuery] )
//        assertNotNull populationQuery
//        def id = populationQuery.id
//
//        // Find the domain
//        populationQuery = populationQuery.get( id )
//        assertNotNull populationQuery?.id
//    }
//
//
//    @Test
//    void testUpdateValidStatementPopulationQuery() {
//        def populationQuery = newPopulationQuery( false, false, "TTTTTTTTTT" )
//        populationQuery = communicationPopulationQueryCompositeService.create( [domainModel: populationQuery] )
//        assertNotNull populationQuery
//        def id = populationQuery.id
//
//        // Find the domain
//        populationQuery = populationQuery.get( id )
//        assertNotNull populationQuery?.id
//    }
//
//
//    @Test
//    void testDeletePopulationQuery() {
//
//        def populationQuery = newPopulationQuery( false, false, "TTTTTTTTTT" )
//
//        populationQuery = communicationPopulationQueryCompositeService.create( [domainModel: populationQuery] )
//        assertNotNull populationQuery
//        def id = populationQuery.id
//
//        // Find the domain
//        populationQuery = populationQuery.get( id )
//        assertNotNull populationQuery
//
//
//        def populationQuery1 = newPopulationQuery( false, false, "MMMMMMMMMM" )
//        populationQuery1 = communicationPopulationQueryCompositeService.create( [domainModel: populationQuery1] )
//        assertNotNull populationQuery1
//        def id1 = populationQuery1.id
//
//        // Find the domain
//        populationQuery1 = populationQuery1.get( id )
//        assertNotNull populationQuery1
//
//        // Delete the domain
//        communicationPopulationQueryCompositeService.delete( [domainModel: populationQuery] )
//
//        // Attempt to find the deleted domain
//        populationQuery = populationQuery.get( id )
//        assertNull populationQuery
//    }
//
//
//    @Test
//    void testDynamicFinder() {
//
//        def populationQuery = newPopulationQuery( false, false, "TestName1" )
//        communicationPopulationQueryCompositeService.create( [domainModel: populationQuery] )
//        def populationQuery1 = newPopulationQuery( false, false, "TestName2" )
//        communicationPopulationQueryCompositeService.create( [domainModel: populationQuery1] )
//        // Find the domain
//        def List<CommunicationPopulationQuery> populationQueries = populationQuery.findAllByQueryName( "TestName" )
//        assertNotNull populationQueries
//        assertEquals( 2, populationQueries.size() )
//    }


    private String getUser() {
        return 'BCMADMIN'
    }

}

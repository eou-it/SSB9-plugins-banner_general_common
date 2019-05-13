/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for communication common utility
 */
@Integration
@Rollback
class CommunicationCommonUtilityIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

//Testing sql pattern
    @Test
    void testGoodSql() {
        def sqlstring = "select spriden_pidm from spriden where spriden_change_ind is null  AND spriden_id LIKE 'A0000091%'"
        def result = CommunicationCommonUtility.sqlStatementNotAllowed(sqlstring)
        assertFalse(result)
    }

    @Test
    void testOrderBySql() {
        String sqlStatement = "select spriden_pidm from spriden where spriden_change_ind is null AND spriden_id LIKE 'A0000091%'"
        String orderByClause = " Order By spriden_pidm"

        final boolean forDataField = true
        final boolean forPopulationQuery = false

        assertFalse( CommunicationCommonUtility.sqlStatementNotAllowed( sqlStatement ) )
        assertFalse( CommunicationCommonUtility.sqlStatementNotAllowed( sqlStatement + orderByClause ) )
        assertFalse( CommunicationCommonUtility.sqlStatementNotAllowed( sqlStatement + orderByClause, forDataField ) )
        assertTrue( CommunicationCommonUtility.sqlStatementNotAllowed( sqlStatement + orderByClause, forPopulationQuery ) )

        String goofyOrderByClause = "    ORDER            BY  spriden_pidm"
        assertFalse( CommunicationCommonUtility.sqlStatementNotAllowed( sqlStatement + goofyOrderByClause ) )
        assertFalse( CommunicationCommonUtility.sqlStatementNotAllowed( sqlStatement + goofyOrderByClause, forDataField ) )
        assertTrue( CommunicationCommonUtility.sqlStatementNotAllowed( sqlStatement + goofyOrderByClause, forPopulationQuery ) )
    }


    @Test
    void testInsertSql() {
        def sqlstring = "insert into dual; select spriden_pidm from spriden where spriden_change_ind is null  AND spriden_id LIKE 'A0000091%'"
        def result = CommunicationCommonUtility.sqlStatementNotAllowed(sqlstring)
        assertTrue(result)
    }


    @Test
    void testInsert2Sql() {
        def sqlstring = "select spriden_pidm from spriden where spriden_change_ind is null  AND spriden_id LIKE 'A0000091%';insert into dual;"
        def result = CommunicationCommonUtility.sqlStatementNotAllowed(sqlstring)
        assertTrue(result)
    }

//Testing input scrubbing

    @Test
    void testGoodStringInput() {
        def userinput = "Test"
        def expectedOutput = concatWildcard(userinput)

        def result = CommunicationCommonUtility.getScrubbedInput(userinput)
        assertTrue(result.equals(expectedOutput))
    }


   // @Test
    void testBadStringInput() {
        def userinput = "Tes@#\$&()t"
        def expectedOutput = concatWildcard("test")

        def result = CommunicationCommonUtility.getScrubbedInput(userinput)
        assertTrue(result.equals(expectedOutput))
    }


    @Test
    void testReplaceWildcardInput() {
        def userinput = "Tes?%*_t"
        def expectedOutput = concatWildcard("Tes_%%_t")

        def result = CommunicationCommonUtility.getScrubbedInput(userinput)
        assertTrue(result.equals(expectedOutput))
    }


   // @Test
    void testReplaceWildcardOtherInput() {
        def userinput = "Tes?(#%<>:*_t"
        def expectedOutput = concatWildcard("tes_%%_t")

        def result = CommunicationCommonUtility.getScrubbedInput(userinput)
        assertTrue(result.equals(expectedOutput))
    }


    private def String concatWildcard(String userinput) {
        def wildchar = CommunicationCommonUtility.wildcardChar
        return (wildchar + userinput + wildchar)
    }

}

/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.jdbc.UncategorizedSQLException


class DisplayMaskingColumnRuleViewIntegrationTests extends BaseIntegrationTestCase {


	@Before
	public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


	@After
	public void tearDown() {
        super.tearDown()
    }

    /**
     * Tests that view does not allow crud (create,update,delete) operations and is readonly
     */

	@Test
    void testCreateExceptionResults() {
        def existingMask = DisplayMaskingColumnRuleView.findAll()[0]
        assertNotNull existingMask
        DisplayMaskingColumnRuleView newMask = new DisplayMaskingColumnRuleView(existingMask.properties)
        newMask.concealIndicator = true
        newMask.displayIndicator = true
        newMask.version = 0
        newMask.id = 2222222
        shouldFail(UncategorizedSQLException) {
            newMask.save(flush: true, onError: true)
        }

    }


	@Test
    void testUpdateExceptionResults() {
        def existingMask = DisplayMaskingColumnRuleView.findAll()[0]
        assertNotNull existingMask
        existingMask.concealIndicator = true
        existingMask.displayIndicator = true
        shouldFail(UncategorizedSQLException) {
            existingMask.save(flush: true, onError: true)
        }
    }


	@Test
    void testDeleteExceptionResults() {
        def existingMask = DisplayMaskingColumnRuleView.findAll()[0]
        assertNotNull existingMask
        shouldFail(UncategorizedSQLException) {
            existingMask.delete(flush: true, onError: true)
        }
    }


    @Test
    void testFetchSsbNameDisplay() {
        def showNameSuffix
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call("{? = call GB_DISPLAYMASK.F_SSB_FORMAT_NAME()}", [Sql.VARCHAR]) {
            result -> showNameSuffix = result }

        def display = DisplayMaskingColumnRuleView.fetchSSBNameMask()
        assertNotNull display
        assertEquals display, showNameSuffix

    }


    @Test
    void testFetchSSBMaskByBlockNameAndColumnName() {
        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GORDMSK set GORDMSK_DATA_MASK = '*****XXXX' where GORDMSK_BLOCK_NAME = 'BWPKHSTB_ALL' AND GORDMSK_COLUMN_NAME = '%_SSN'")
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        def display = DisplayMaskingColumnRuleView.fetchSSBMaskByBlockNameAndColumnName([blockName:'BWPKHSTB',columnName:'%_SSN'])
        assertNotNull display
        assertEquals display.dataMask,"*****XXXX"

    }


    @Test
    void testFetchSSBMaskByBlockName() {
        def displayRules = DisplayMaskingColumnRuleView.fetchSSBMaskByBlockName([blockName:'BWGKOADR'])
        assertNotNull displayRules
        assertEquals displayRules.size,4

    }
}

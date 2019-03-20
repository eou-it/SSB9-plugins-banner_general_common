/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.jdbc.UncategorizedSQLException

/**
 * Integration tests for FlexibleWorkWeekReadOnly entity
 */
class FlexibleWorkWeekReadOnlyIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE', 'GUAGMNU']
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testFetchByWorkWeekReference() {
        def fww = FlexibleWorkWeekReadOnly.fetchByWorkWeekReference(1)
        // Assert domain values
        assertNotNull fww?.id
        assertEquals 1, fww.workWeekReference
        assertEquals "Monday to Friday Work Week ", fww.description
        assertEquals 2451916, fww.firstWeekendDay
        assertEquals 6, fww.firstWeekendDayIso
        assertEquals 2451917, fww.secondWeekendDay
        assertEquals 7, fww.secondWeekendDayIso
        assertFalse fww.isInstitutionalDefault
        assertTrue fww.isSystemRequired
        assertNull fww.campusCode
    }


    @Test
    void testFetchByInstitutionDefault() {
        def fww = FlexibleWorkWeekReadOnly.fetchByInstitutionDefault()
        assertNull fww?.id
    }


    @Test
    void testFetchBySystemRequired() {
        def fww = FlexibleWorkWeekReadOnly.fetchBySystemRequired()
        // Assert domain values
        assertNotNull fww?.id
        assertEquals 1, fww.workWeekReference
    }


    @Test
    void testCreateFlexibleWorkWeekReadOnly() {
        def fww = new FlexibleWorkWeekReadOnly()
        fww.id = 11111111
        fww.version = 11111111
        shouldFail(UncategorizedSQLException) {
            fww.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testUpdateFlexibleWorkWeekReadOnly() {
        def fww = FlexibleWorkWeekReadOnly.fetchByWorkWeekReference(1)
        assertNotNull fww?.id

        // Update domain values
        fww.firstWeekendDay = 999999
        shouldFail(UncategorizedSQLException) {
            fww.save(failOnError: true, flush: true)
        }
    }


    @Test
    void testDeleteFlexibleWorkWeekReadOnly() {
        def fww = FlexibleWorkWeekReadOnly.fetchByWorkWeekReference(1)
        assertNotNull fww?.id

        shouldFail(UncategorizedSQLException) {
            fww.delete(flush: true, onError: true)
        }
    }

}

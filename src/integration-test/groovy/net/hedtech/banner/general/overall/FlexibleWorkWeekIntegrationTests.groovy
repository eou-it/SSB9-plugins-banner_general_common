/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

/**
 * Integration tests for FlexibleWorkWeek entity
 */
class FlexibleWorkWeekIntegrationTests extends BaseIntegrationTestCase {

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
        def fww = FlexibleWorkWeek.fetchByWorkWeekReference(1)
        assertNotNull fww?.id
        assertEquals 1, fww.workWeekReference
        assertEquals "Monday to Friday Work Week ", fww.description
        assertEquals 2451916, fww.firstWeekendDay
        assertEquals 2451917, fww.secondWeekendDay
        assertFalse fww.isInstitutionalDefault
        assertTrue fww.isSystemRequired
        assertNull fww.campusCode
    }


    @Test
    void testCreateFlexibleWorkWeek() {
        def fww = newFlexibleWorkWeek()
        fww.save(failOnError: true, flush: true)

        // Assert domain values
        assertNotNull fww?.id
        assertEquals 9999, fww.workWeekReference
        assertEquals "TTTTTTTTTT", fww.description
        assertEquals 2451916, fww.firstWeekendDay
        assertEquals 2451917, fww.secondWeekendDay
        assertTrue fww.isInstitutionalDefault
        assertTrue fww.isSystemRequired
        assertEquals "M", fww.campusCode
    }


    @Test
    void testUpdateFlexibleWorkWeek() {
        def fww = newFlexibleWorkWeek()
        fww.save(failOnError: true, flush: true)

        // Assert domain values
        assertNotNull fww?.id
        def id = fww.id

        // Find the domain
        fww = FlexibleWorkWeek.get(id)
        assertNotNull fww

        // Update domain values
        fww.firstWeekendDay = 2451917
        fww.save(failOnError: true, flush: true)

        // Find the updated domain
        fww = fww.get(id)

        // Assert updated domain values
        assertNotNull fww?.id
        assertEquals 2451917, fww.firstWeekendDay
    }


    @Test
    void testDeleteFlexibleWorkWeek() {
        def fww = newFlexibleWorkWeek()
        fww.save(failOnError: true, flush: true)

        // Assert domain values
        assertNotNull fww?.id
        def id = fww.id

        // Find the domain
        fww = FlexibleWorkWeek.get(id)
        assertNotNull fww

        // Delete the domain
        fww.delete()

        // Attempt to find the deleted domain
        fww = FlexibleWorkWeek.get(id)
        assertNull fww
    }


    @Test
    void testNullValidationFailure() {
        // Instantiate an empty domain
        def fww = new FlexibleWorkWeek()

        // Assert for domain validation
        assertFalse "FlexibleWorkWeek should have failed null value validation", fww.validate()

        // Assert for specific field validation
        assertErrorsFor fww, 'nullable', ['workWeekReference',
                                          'description',
                                          'firstWeekendDay',
                                          'secondWeekendDay',
                                          'isInstitutionalDefault',
                                          'isSystemRequired']
        assertNoErrorsFor fww, ['campusCode']
    }


    @Test
    void testMaxSizeValidationFailure() {
        def fww = newFlexibleWorkWeek()

        // Set domain values to exceed maximum allowed length
        fww.description = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"
        fww.campusCode = "TTTT"

        // Assert for domain
        assertFalse "FlexibleWorkWeek should have failed max size validation", fww.validate()

        // Assert for specific fields
        assertErrorsFor fww, 'maxSize', ['description',
                                         'campusCode']
    }


    @Test
    void testOptimisticLock() {
        def fww = newFlexibleWorkWeek()
        fww.save(failOnError: true, flush: true)
        assertNotNull fww?.id

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("UPDATE gurfwwk SET gurfwwk_version = 999 WHERE gurfwwk_surrogate_id = ?", [fww.id])
        } finally {
            sql?.close()
        }

        // Update the entity
        fww.dataOrigin = "OPT_TEST"
        shouldFail(HibernateOptimisticLockingFailureException) { fww.save(failOnError: true, flush: true) }
    }


    @Test
    void testInvalidFirstWeekendDay() {
        def fww = newFlexibleWorkWeek()

        fww.firstWeekendDay = 999999
        assertFalse fww.validate()
        assertErrorsFor fww, 'validator', ['firstWeekendDay']
    }


    @Test
    void testInvalidSecondWeekendDay() {
        def fww = newFlexibleWorkWeek()

        fww.secondWeekendDay = 999999
        assertFalse fww.validate()
        assertErrorsFor fww, 'validator', ['secondWeekendDay']
    }


    private def newFlexibleWorkWeek() {
        def fww = new FlexibleWorkWeek(
                // Required fields
                workWeekReference: 9999,
                description: "TTTTTTTTTT",
                firstWeekendDay: 2451916,
                secondWeekendDay: 2451917,
                isInstitutionalDefault: true,
                isSystemRequired: true,

                // Nullable fields
                campusCode: "M"
        )

        return fww
    }

}

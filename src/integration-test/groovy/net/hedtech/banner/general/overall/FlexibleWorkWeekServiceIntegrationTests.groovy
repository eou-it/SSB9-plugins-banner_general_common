/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for FlexibleWorkWeekService service
 */
@Integration
@Rollback
class FlexibleWorkWeekServiceIntegrationTests extends BaseIntegrationTestCase {

    def flexibleWorkWeekService


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
    void testFindByWorkWeekReference() {
        def fww = flexibleWorkWeekService.findByWorkWeekReference(1)
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
        fww = flexibleWorkWeekService.create(fww)

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
    void testInvalidWeekendDay() {
        def fww = newFlexibleWorkWeek()
        fww.firstWeekendDay = 9999999
        try {
            flexibleWorkWeekService.create(fww)
            fail("This should have failed with @@r1:invalid.firstWeekendDay")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "invalid.firstWeekendDay"
        }

        fww = newFlexibleWorkWeek()
        fww.secondWeekendDay = 9999999
        try {
            flexibleWorkWeekService.create(fww)
            fail("This should have failed with @@r1:invalid.secondWeekendDay")
        } catch (ApplicationException ae) {
            assertApplicationException ae, "invalid.secondWeekendDay"
        }
    }


    @Test
    void testUpdateFlexibleWorkWeek() {
        def fww = newFlexibleWorkWeek()
        fww = flexibleWorkWeekService.create(fww)
        assertNotNull fww
        def id = fww.id

        // Find the domain
        fww = fww.get(id)
        assertNotNull fww?.id

        // Update domain values
        fww.firstWeekendDay = 2451911
        fww = flexibleWorkWeekService.update(fww)

        // Find the updated domain
        fww = fww.get(id)

        // Assert updated domain values
        assertNotNull fww
        assertEquals 2451911, fww.firstWeekendDay
    }


    @Test
    void testDeleteFlexibleWorkWeek() {
        def fww = newFlexibleWorkWeek()
        fww = flexibleWorkWeekService.create(fww)
        assertNotNull fww
        def id = fww.id

        // Find the domain
        fww = fww.get(id)
        assertNotNull fww

        // Delete the domain
        flexibleWorkWeekService.delete(fww)

        // Attempt to find the deleted domain
        fww = fww.get(id)
        assertNull fww
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

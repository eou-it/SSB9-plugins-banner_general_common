/*********************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.utility

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class MailServiceIntegrationTests extends BaseIntegrationTestCase {
    def mailService

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    public void testGetMailDetails() {
        Integer pidm = PersonUtility.getPerson('A00050984').pidm
        def result = mailService.getMailDetails(pidm, '201901')
        assertNotNull(result)
    }

    @Test
    public void testGetMaiDetailsByPidmTermSystemIndAndLettrCode() {
        Integer pidm = PersonUtility.getPerson('A00050984').pidm
        def result = mailService.getMaiDetailsByPidmTermSystemIndAndLettrCode(pidm, '201901', 'S', 'GRDS_PRNT_TKT')
        assertNotNull(result)
    }
}

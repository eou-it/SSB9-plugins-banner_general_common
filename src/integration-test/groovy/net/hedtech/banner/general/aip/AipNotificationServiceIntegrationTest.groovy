/*******************************************************************************
 Copyright 2018-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.aip

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

import static net.hedtech.banner.general.aip.AipNotificationConstants.YES
import static net.hedtech.banner.general.aip.AipNotificationConstants.NO
import static net.hedtech.banner.general.aip.AipNotificationConstants.ENABLED
import static net.hedtech.banner.general.aip.AipNotificationConstants.DISABLED
import static net.hedtech.banner.general.aip.AipNotificationConstants.SQPR_CODE_GENERAL_SSB
import static net.hedtech.banner.general.aip.AipNotificationConstants.ICSN_CODE_ENABLE_ACTION_ITEMS
import static org.junit.Assert.*


@Integration
@Rollback
class AipNotificationServiceIntegrationTest extends BaseIntegrationTestCase {

    def aipNotificationService
    def existingValue


    @Before
    void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()

    }


    @After
    void tearDown() {
        super.tearDown()
    }

    @Test
    void testHasActiveActionItemsForUserWithActionItems() {
        Integer pidm = getPidmBySpridenId("CSRSTU001")
        assertNotNull pidm

        def hasActiveRows = UserActiveActionItem.checkIfActionItemPresent(pidm)
        assertNotNull hasActiveRows

        Boolean hasActiveItem = aipNotificationService.hasActiveActionItems(pidm)
        assertEquals hasActiveRows,hasActiveItem
    }

    @Test
    void testHasActiveActionItemsForUserWithOutActionItems() {
        //AIPADM001 is admin user who does not have action items
        Integer pidm = getPidmBySpridenId("AIPADM001")
        assertNotNull pidm

        def hasActiveRows = UserActiveActionItem.checkIfActionItemPresent(pidm)
        assertNotNull hasActiveRows

        Boolean hasActiveItem = aipNotificationService.hasActiveActionItems(pidm)
        assertEquals hasActiveRows,hasActiveItem
    }

    @Test
    void testGetGoriccrFlagForDisabled() {
        //Checking for goriccr value before updating it
        def oldValue = getGoriicrValue(SQPR_CODE_GENERAL_SSB, ICSN_CODE_ENABLE_ACTION_ITEMS)
        assertNotNull oldValue

        updateGoriccrRule(SQPR_CODE_GENERAL_SSB, ICSN_CODE_ENABLE_ACTION_ITEMS, NO)

        String flag = aipNotificationService.getAipEnabledFlag()
        assertNotNull flag
        assertEquals(DISABLED, flag)
    }

    @Test
    void testGetGoriccrFlagForEnable() {
        def oldValue = getGoriicrValue(SQPR_CODE_GENERAL_SSB, ICSN_CODE_ENABLE_ACTION_ITEMS)
        assertNotNull oldValue

        updateGoriccrRule(SQPR_CODE_GENERAL_SSB, ICSN_CODE_ENABLE_ACTION_ITEMS, YES)

        String flag = aipNotificationService.getAipEnabledFlag()
        assertNotNull flag
        assertEquals(ENABLED, flag)
    }


    private Integer getPidmBySpridenId(def spridenId) {
        def pidm = PersonUtility.getPerson(spridenId)?.pidm
        pidm
    }


    private def getGoriicrValue(def sqpr_code, def icsn_code) {
        IntegrationConfiguration integrationConfiguration = IntegrationConfiguration.fetchByProcessCodeAndSettingName(sqpr_code, icsn_code)
        integrationConfiguration?.value
    }
    /*
    * This method will set the GORRICCR Rule
    * */

    private void updateGoriccrRule(def sqpr_code, def icsn_code, def value) {
        IntegrationConfiguration integrationConfiguration = IntegrationConfiguration.fetchByProcessCodeAndSettingName(sqpr_code, icsn_code)
        integrationConfiguration.value = value
        integrationConfiguration.save(flush: true, failOnError: true)
    }
}
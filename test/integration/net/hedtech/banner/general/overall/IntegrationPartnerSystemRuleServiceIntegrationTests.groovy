/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.general.overall
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.testing.BaseIntegrationTestCase

import net.hedtech.banner.general.system.IntegrationPartner

class IntegrationPartnerSystemRuleServiceIntegrationTests extends BaseIntegrationTestCase {

    def integrationPartnerSystemRuleService


	@Before
	public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }


	@After
	public void tearDown() {
        super.tearDown()
    }


	@Test
    void testCreate() {
        def integrationPartnerSystemRule = newIntegrationPartnerSystemRule()
        integrationPartnerSystemRule = integrationPartnerSystemRuleService.create(integrationPartnerSystemRule)
        assertNotNull "Integration Partner Rule ID is null in Integration Partner Rule Service Tests", integrationPartnerSystemRule.id
        assertNotNull "Integration Partner is null in Integration Partner Rule Service Tests", integrationPartnerSystemRule.code
        assertEquals "Integration Partner not TTTTT", integrationPartnerSystemRule.code, "TTTTT"

    }


	@Test
    void testUpdate() {
        def integrationPartnerSystemRules = newIntegrationPartnerSystemRule()
        integrationPartnerSystemRules = integrationPartnerSystemRuleService.create(integrationPartnerSystemRules)

        IntegrationPartnerSystemRule integrationPartnerSystemRulesUpdate = integrationPartnerSystemRules.findWhere(code: "TTTTT")
        assertNotNull "Program Rule ID is null in Test Update", integrationPartnerSystemRulesUpdate.id

        integrationPartnerSystemRulesUpdate.description = "XXXXX"
        integrationPartnerSystemRulesUpdate = integrationPartnerSystemRuleService.update(integrationPartnerSystemRulesUpdate)
        assertEquals "XXXXX", integrationPartnerSystemRulesUpdate.description
    }


	@Test
    void testIntegrationPartnerSystemRulesDelete() {
        def integrationPartnerSystemRules = newIntegrationPartnerSystemRule()
        integrationPartnerSystemRules = integrationPartnerSystemRuleService.create(integrationPartnerSystemRules)

        IntegrationPartnerSystemRule integrationPartnerSystemRulesUpdate = integrationPartnerSystemRules.findWhere(code: "TTTTT")
        integrationPartnerSystemRuleService.delete(integrationPartnerSystemRulesUpdate.id)

        assertNull "Integration should have been deleted", integrationPartnerSystemRules.get(integrationPartnerSystemRulesUpdate.id)
    }


    private def newIntegrationPartnerSystemRule() {
        def intp = new IntegrationPartner(code: "TTTTT", description: "TTTTT", lastModified: new Date(),
                                          lastModifiedBy: "test", dataOrigin: "Banner")
        intp.save(flush: true, failOnError: true)
        def integration = new IntegrationPartnerSystemRule(code: "TTTTT",
                                                           description: "TTTTT",
                                                           integrationPartner: intp
        )
        return integration

    }
}

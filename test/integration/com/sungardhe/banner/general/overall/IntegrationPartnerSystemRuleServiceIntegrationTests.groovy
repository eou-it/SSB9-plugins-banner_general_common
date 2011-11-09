/** *******************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 ********************************************************************************* */

package com.sungardhe.banner.general.overall

import com.sungardhe.banner.testing.BaseIntegrationTestCase

import com.sungardhe.banner.general.system.IntegrationPartner

class IntegrationPartnerSystemRuleServiceIntegrationTests extends BaseIntegrationTestCase {

    def integrationPartnerSystemRuleService


    protected void setUp() {
        formContext = ['GORINTG'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreate() {
        def integrationPartnerSystemRule = newIntegrationPartnerSystemRule()
        integrationPartnerSystemRule = integrationPartnerSystemRuleService.create(integrationPartnerSystemRule)
        assertNotNull "Integration Partner Rule ID is null in Integration Partner Rule Service Tests", integrationPartnerSystemRule.id
        assertNotNull "Integration Partner is null in Integration Partner Rule Service Tests", integrationPartnerSystemRule.code
        assertEquals "Integration Partner not TTTTT", integrationPartnerSystemRule.code, "TTTTT"

    }


    void testUpdate() {
        def integrationPartnerSystemRules = newIntegrationPartnerSystemRule()
        integrationPartnerSystemRules = integrationPartnerSystemRuleService.create(integrationPartnerSystemRules)

        IntegrationPartnerSystemRule integrationPartnerSystemRulesUpdate = integrationPartnerSystemRules.findWhere(code: "TTTTT")
        assertNotNull "Program Rule ID is null in Test Update", integrationPartnerSystemRulesUpdate.id

        integrationPartnerSystemRulesUpdate.description = "XXXXX"
        integrationPartnerSystemRulesUpdate = integrationPartnerSystemRuleService.update(integrationPartnerSystemRulesUpdate)
        assertEquals "XXXXX", integrationPartnerSystemRulesUpdate.description
    }


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
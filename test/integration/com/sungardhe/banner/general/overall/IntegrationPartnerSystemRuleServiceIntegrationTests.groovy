/** *****************************************************************************

 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

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
    assertEquals "Integration Partner not TTTTT", integrationPartnerSystemRule.code,"TTTTT"

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
    def intp =  new IntegrationPartner(code: "TTTTT", description: "TTTTT" , lastModified: new Date(),
			lastModifiedBy: "test", dataOrigin: "Banner" )
    intp.save(flush:true, failOnError:true)
    def integration =  new IntegrationPartnerSystemRule(code: "TTTTT",
            description: "TTTTT",
            integrationPartner: intp //,
          //  lastModified: new Date(),
          //  lastModifiedBy: "test",
          //  dataOrigin: "Banner"
         )
    return integration

  }
}
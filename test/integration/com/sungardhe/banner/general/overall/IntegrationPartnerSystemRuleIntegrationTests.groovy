/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.general.overall

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql

import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import com.sungardhe.banner.general.system.IntegrationPartner
import org.junit.Ignore

class IntegrationPartnerSystemRuleIntegrationTests extends BaseIntegrationTestCase {

  def integrationPartnerSystemRuleService

  protected void setUp() {
    formContext = ['GORINTG'] // Since we are not testing a controller, we need to explicitly set this
    super.setUp()
  }

  protected void tearDown() {
    super.tearDown()
  }

  void testCreateIntegrationPartnerSystemRule() {
    def integrationPartnerSystemRule = newIntegrationPartnerSystemRule()
    save integrationPartnerSystemRule
    //Test if the generated entity now has an id assigned
    assertNotNull integrationPartnerSystemRule.id
  } 
  
  @Ignore
  void testUpdateIntegrationPartnerSystemRule() {
    def integrationPartnerSystemRule = newIntegrationPartnerSystemRule()
    assertTrue integrationPartnerSystemRule.validate()
    integrationPartnerSystemRule.save(flush:true)

    assertNotNull integrationPartnerSystemRule.id
    assertEquals(0L, integrationPartnerSystemRule.version)
    assertEquals("TTTTT", integrationPartnerSystemRule.code)
    assertEquals("TTTTT", integrationPartnerSystemRule.description)
    assertEquals("TTTTT", integrationPartnerSystemRule.integrationPartner.code)

    //Update the entity
    integrationPartnerSystemRule.description = "UUUUU"
    integrationPartnerSystemRule.lastModified = new Date()
    integrationPartnerSystemRule.lastModifiedBy = "test"
    integrationPartnerSystemRule.dataOrigin = "Banner"
    println "version old ${integrationPartnerSystemRule.version}  "
    assertTrue integrationPartnerSystemRule.validate()
   
    integrationPartnerSystemRule.save(flush:true)
    println "version check"
    integrationPartnerSystemRule = IntegrationPartnerSystemRule.get(integrationPartnerSystemRule.id)
    assertEquals new Long(1), integrationPartnerSystemRule?.version
    assertEquals("UUUUU", integrationPartnerSystemRule.description)

  }

  void testOptimisticLock() {
    def integrationPartnerSystemRule = newIntegrationPartnerSystemRule()
    save integrationPartnerSystemRule

    def sql
    try {
       
      sql = new Sql(sessionFactory.getCurrentSession().connection())
      sql.executeUpdate("update gv_gorintg set gorintg_VERSION = 999, gorintg_data_origin = 'Margy'  where GORINTG_SURROGATE_ID = ${integrationPartnerSystemRule.id}")

    } finally {
      sql?.close() // note that the test will close the connection, since it's our current session's connection
    }

    //Try to update the entity
    integrationPartnerSystemRule.code = "UUUUU"
    integrationPartnerSystemRule.description = "UUUUU"
    integrationPartnerSystemRule.lastModified = new Date()
    integrationPartnerSystemRule.lastModifiedBy = "test"
    integrationPartnerSystemRule.dataOrigin = "Banner"
    shouldFail(HibernateOptimisticLockingFailureException) {
      integrationPartnerSystemRule.save(flush: true)
    }
  }

  void testDeleteIntegrationPartnerSystemRule() {
    def integrationPartnerSystemRule = newIntegrationPartnerSystemRule()
    save integrationPartnerSystemRule
    def id = integrationPartnerSystemRule.id
    assertNotNull id
    integrationPartnerSystemRule.delete()
    assertNull IntegrationPartnerSystemRule.get(id)
  }

  void testValidation() {
    def integrationPartnerSystemRule = newIntegrationPartnerSystemRule()
    assertTrue "IntegrationPartnerSystemRule could not be validated as expected due to ${integrationPartnerSystemRule.errors}", integrationPartnerSystemRule.validate()
  }

  void testNullValidationFailure() {
    def integrationPartnerSystemRule = new IntegrationPartnerSystemRule()
    assertFalse "IntegrationPartnerSystemRule should have failed validation", integrationPartnerSystemRule.validate()
    assertErrorsFor(integrationPartnerSystemRule, 'nullable', ['code', 'description', 'integrationPartner'])
  }

  void testMaxSizeValidationFailures() {
    def integrationPartnerSystemRule = new IntegrationPartnerSystemRule(
            code: 'XXXXXXX',
            description: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX' )
    assertFalse "IntegrationPartnerSystemRule should have failed validation", integrationPartnerSystemRule.validate()
    assertErrorsFor(integrationPartnerSystemRule, 'maxSize', ['code', 'description'])
  }

  void testValidationMessages() {
    def integrationPartnerSystemRule = newIntegrationPartnerSystemRule()

  }


  private def newIntegrationPartnerSystemRule() {
    def intp =  new IntegrationPartner(code: "TTTTT", description: "TTTTT" , lastModified: new Date(),
			lastModifiedBy: "test", dataOrigin: "Banner" )
    intp.save(flush:true)
    new IntegrationPartnerSystemRule(code: "TTTTT",
            description: "TTTTT",
            integrationPartner: intp,
            lastModified: new Date(),
            lastModifiedBy: "test",
            dataOrigin: "Banner"  )

  }

  /**
   * Please put all the custom tests in this protected section to protect the code
   * from being overwritten on re-generation
   */
  /*PROTECTED REGION ID(integrationpartnersystemrule_custom_integration_test_methods) ENABLED START*/
  /*PROTECTED REGION END*/
}

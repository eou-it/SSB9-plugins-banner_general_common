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
package net.hedtech.banner.general.overall

import net.hedtech.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql

import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import net.hedtech.banner.general.system.IntegrationPartner

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
        integrationPartnerSystemRule.save(flush: true, failOnError: true)
        //Test if the generated entity now has an id assigned
        assertNotNull integrationPartnerSystemRule.id
        assertNotNull integrationPartnerSystemRule.lastModified
        assertNotNull integrationPartnerSystemRule.lastModifiedBy
        assertNotNull integrationPartnerSystemRule.dataOrigin
    }


    void testUpdateIntegrationPartnerSystemRule() {
        def integrationPartnerSystemRule = newIntegrationPartnerSystemRule()
        assertTrue integrationPartnerSystemRule.validate()
        integrationPartnerSystemRule.save(flush: true, failOnError: true)

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
        assertTrue integrationPartnerSystemRule.validate()

        integrationPartnerSystemRule.save(flush: true, failOnError: true)
        integrationPartnerSystemRule = IntegrationPartnerSystemRule.get(integrationPartnerSystemRule.id)
        assertEquals new Long(1), integrationPartnerSystemRule?.version
        assertEquals("UUUUU", integrationPartnerSystemRule.description)

    }


    void testOptimisticLock() {
        def integrationPartnerSystemRule = newIntegrationPartnerSystemRule()
        integrationPartnerSystemRule.save(flush: true, failOnError: true)

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
            integrationPartnerSystemRule.save(flush: true, failOnError: true)
        }
    }


    void testDeleteIntegrationPartnerSystemRule() {
        def integrationPartnerSystemRule = newIntegrationPartnerSystemRule()
        integrationPartnerSystemRule.save(flush: true, failOnError: true)
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
                description: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX')
        assertFalse "IntegrationPartnerSystemRule should have failed validation", integrationPartnerSystemRule.validate()
        assertErrorsFor(integrationPartnerSystemRule, 'maxSize', ['code', 'description'])
    }


    private def newIntegrationPartnerSystemRule() {
        def intp = new IntegrationPartner(
                code: "TTTTT",
                description: "TTTTT")
        intp.save(flush: true, failOnError: true)
        new IntegrationPartnerSystemRule(code: "TTTTT",
                                         description: "TTTTT",
                                         integrationPartner: intp)
    }

    /**
     * Please put all the custom tests in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(integrationpartnersystemrule_custom_integration_test_methods) ENABLED START*/
    /*PROTECTED REGION END*/
}

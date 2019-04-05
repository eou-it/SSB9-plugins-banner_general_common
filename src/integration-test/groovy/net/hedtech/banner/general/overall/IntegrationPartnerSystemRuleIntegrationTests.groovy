/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Before
import org.junit.Test
import org.junit.After

import net.hedtech.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql

import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException
import net.hedtech.banner.general.system.IntegrationPartner
import static groovy.test.GroovyAssert.*

@Integration
@Rollback
class IntegrationPartnerSystemRuleIntegrationTests extends BaseIntegrationTestCase {

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
    void testCreateIntegrationPartnerSystemRule() {
        def integrationPartnerSystemRule = newIntegrationPartnerSystemRule()
        integrationPartnerSystemRule.save(flush: true, failOnError: true)
        //Test if the generated entity now has an id assigned
        assertNotNull integrationPartnerSystemRule.id
        assertNotNull integrationPartnerSystemRule.lastModified
        assertNotNull integrationPartnerSystemRule.lastModifiedBy
        assertNotNull integrationPartnerSystemRule.dataOrigin
    }


    @Test
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


    @Test
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


    @Test
    void testDeleteIntegrationPartnerSystemRule() {
        def integrationPartnerSystemRule = newIntegrationPartnerSystemRule()
        integrationPartnerSystemRule.save(flush: true, failOnError: true)
        def id = integrationPartnerSystemRule.id
        assertNotNull id
        integrationPartnerSystemRule.delete()
        assertNull IntegrationPartnerSystemRule.get(id)
    }


    @Test
    void testValidation() {
        def integrationPartnerSystemRule = newIntegrationPartnerSystemRule()
        assertTrue "IntegrationPartnerSystemRule could not be validated as expected due to ${integrationPartnerSystemRule.errors}", integrationPartnerSystemRule.validate()
    }


    @Test
    void testNullValidationFailure() {
        def integrationPartnerSystemRule = new IntegrationPartnerSystemRule()
        assertFalse "IntegrationPartnerSystemRule should have failed validation", integrationPartnerSystemRule.validate()
        assertErrorsFor(integrationPartnerSystemRule, 'nullable', ['code', 'description', 'integrationPartner'])
    }


    @Test
    void testMaxSizeValidationFailures() {
        def integrationPartnerSystemRule = new IntegrationPartnerSystemRule(
                code: 'XXXXXXX',
                description: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX')
        assertFalse "IntegrationPartnerSystemRule should have failed validation", integrationPartnerSystemRule.validate()
        assertErrorsFor(integrationPartnerSystemRule, 'maxSize', ['code', 'description'])
    }

    @Test
    void testFetchAllByCode(){
        IntegrationPartnerSystemRule integrationPartnerSystemRule1 = newIntegrationPartnerSystemRuleForCodeAndDesc('test1','test-desc1')
        IntegrationPartnerSystemRule integrationPartnerSystemRule2 = newIntegrationPartnerSystemRuleForCodeAndDesc('test2','test-desc2')

        integrationPartnerSystemRule1.save(flush:true, failOnError: true)
        integrationPartnerSystemRule2.save(flush:true, failOnError: true)

        List<IntegrationPartnerSystemRule> integrationPartnerSystemRuleList = IntegrationPartnerSystemRule.fetchAllByCode(['test1','test2'])
        assertNotNull(integrationPartnerSystemRuleList)
        assertEquals(2, integrationPartnerSystemRuleList.size())
        assertEquals(integrationPartnerSystemRule1,integrationPartnerSystemRuleList[0])
        assertEquals(integrationPartnerSystemRule2,integrationPartnerSystemRuleList[1])
    }

    private def newIntegrationPartnerSystemRuleForCodeAndDesc(String code, String desc){
        def intp = new IntegrationPartner(
                    code: code,
                    description: desc)
        intp.save(flush: true, failOnError: true)
        new IntegrationPartnerSystemRule(code: code,
                                        description: desc,
                                        integrationPartner: intp)
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

}

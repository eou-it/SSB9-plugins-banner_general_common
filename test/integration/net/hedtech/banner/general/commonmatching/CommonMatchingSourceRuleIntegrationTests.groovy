 /** *****************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.commonmatching

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.system.AddressType
import net.hedtech.banner.general.system.CommonMatchingSource
import net.hedtech.banner.general.system.TelephoneType
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import org.junit.Ignore

class CommonMatchingSourceRuleIntegrationTests extends BaseIntegrationTestCase {

    def i_success_commonMatchingSource = 'SS'
    def i_success_addressType
    def i_success_telephoneType
    def i_success_emailType

    def i_success_onlineMatchIndicator = true
    def i_success_entity = "P"
    def i_success_transposeDateIndicator = true
    def i_success_transposeNameIndicator = true
    def i_success_aliasWildcardIndicator = true
    def i_success_lengthOverrideIndicator = true
    def i_success_apiFailureIndicator = true

    //Invalid test data (For failure tests)
    def i_failure_commonMatchingSource = 'TT'
    def i_failure_addressType
    def i_failure_telephoneType
    def i_failure_emailType

    /*PROTECTED REGION END*/


    protected void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
        initializeTestDataForReferences()
    }


    void initializeTestDataForReferences() {
        i_success_addressType = AddressType.findByCode('MA')
        i_success_telephoneType = TelephoneType.findByCode('MA')

    }


    protected void tearDown() {
        super.tearDown()
    }


    void testCreateValidCommonMatchingSourceRule() {
        def commonMatchingSourceRule = newValidForCreateCommonMatchingSourceRule()
        commonMatchingSourceRule.save(flush: true, failOnError: true)
        //Test if the generated entity now has an id assigned
        assertNotNull commonMatchingSourceRule.id
    }


    void testCreateInvalidCommonMatchingSourceRule() {
        def commonMatchingSource = new CommonMatchingSourceRule(code: i_success_commonMatchingSource)
        shouldFail(ValidationException) {
            commonMatchingSource.save(failOnError: true, flush: true)
        }
    }


    void testUpdateValidCommonMatchingSourceRule() {
        def commonMatchingSourceRule = newValidForCreateCommonMatchingSourceRule()
        commonMatchingSourceRule.save(flush: true, failOnError: true)
        assertNotNull commonMatchingSourceRule.id
        assertEquals 0L, commonMatchingSourceRule.version
        assertEquals i_success_onlineMatchIndicator, commonMatchingSourceRule.onlineMatchIndicator
        assertEquals i_success_entity, commonMatchingSourceRule.entity
        assertEquals i_success_transposeDateIndicator, commonMatchingSourceRule.transposeDateIndicator
        assertEquals i_success_transposeNameIndicator, commonMatchingSourceRule.transposeNameIndicator
        assertEquals i_success_aliasWildcardIndicator, commonMatchingSourceRule.aliasWildcardIndicator
        assertEquals i_success_lengthOverrideIndicator, commonMatchingSourceRule.lengthOverrideIndicator
        assertEquals i_success_apiFailureIndicator, commonMatchingSourceRule.apiFailureIndicator
        assertEquals i_success_addressType, commonMatchingSourceRule.addressType
        assertEquals i_success_telephoneType, commonMatchingSourceRule.telephoneType
        assertEquals i_success_emailType, commonMatchingSourceRule.emailType
        //Update the entity
        commonMatchingSourceRule.addressType = AddressType.findByCode('SC')
        commonMatchingSourceRule.save(flush: true, failOnError: true)
        //Asset for sucessful update
        commonMatchingSourceRule = CommonMatchingSourceRule.get(commonMatchingSourceRule.id)
        assertEquals 1L, commonMatchingSourceRule?.version
        assertEquals i_success_onlineMatchIndicator, commonMatchingSourceRule.onlineMatchIndicator
        assertEquals i_success_entity, commonMatchingSourceRule.entity
        assertEquals i_success_transposeDateIndicator, commonMatchingSourceRule.transposeDateIndicator
        assertEquals i_success_transposeNameIndicator, commonMatchingSourceRule.transposeNameIndicator
        assertEquals i_success_aliasWildcardIndicator, commonMatchingSourceRule.aliasWildcardIndicator
        assertEquals i_success_lengthOverrideIndicator, commonMatchingSourceRule.lengthOverrideIndicator
        assertEquals i_success_apiFailureIndicator, commonMatchingSourceRule.apiFailureIndicator
        assertEquals 'SC', commonMatchingSourceRule.addressType.code
        assertEquals i_success_telephoneType, commonMatchingSourceRule.telephoneType
        assertEquals i_success_emailType, commonMatchingSourceRule.emailType
    }


    void testUpdateInvalidCommonMatchingSourceRule() {
        def commonMatchingSourceRule = newValidForCreateCommonMatchingSourceRule()
        commonMatchingSourceRule.save(flush: true, failOnError: true)
        assertNotNull commonMatchingSourceRule.id
        assertEquals 0L, commonMatchingSourceRule.version
        assertEquals i_success_onlineMatchIndicator, commonMatchingSourceRule.onlineMatchIndicator
        assertEquals i_success_entity, commonMatchingSourceRule.entity
        assertEquals i_success_transposeDateIndicator, commonMatchingSourceRule.transposeDateIndicator
        assertEquals i_success_transposeNameIndicator, commonMatchingSourceRule.transposeNameIndicator
        assertEquals i_success_aliasWildcardIndicator, commonMatchingSourceRule.aliasWildcardIndicator
        assertEquals i_success_lengthOverrideIndicator, commonMatchingSourceRule.lengthOverrideIndicator
        assertEquals i_success_apiFailureIndicator, commonMatchingSourceRule.apiFailureIndicator
        assertEquals i_success_addressType, commonMatchingSourceRule.addressType
        assertEquals i_success_telephoneType, commonMatchingSourceRule.telephoneType
        assertEquals i_success_emailType, commonMatchingSourceRule.emailType

        //Update the entity with invalid values
        commonMatchingSourceRule.entity = 'T'
        shouldFail(ValidationException) {
            commonMatchingSourceRule.save(failOnError: true, flush: true)
        }
    }



    void testOptimisticLock() {
        def commonMatchingSourceRule = newValidForCreateCommonMatchingSourceRule()
        commonMatchingSourceRule = commonMatchingSourceRule.save(failOnError: true, flush: true)
        println(' the id is ' + commonMatchingSourceRule.version)
        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GV_GORCMSC set GORCMSC_VERSION = 999 where GORCMSC_SURROGATE_ID = ?", [commonMatchingSourceRule.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }

        commonMatchingSourceRule.entity = "C"

        shouldFail(HibernateOptimisticLockingFailureException) {
            commonMatchingSourceRule.save(failOnError: true, flush: true)
        }
    }


    void testDeleteCommonMatchingSourceRule() {
        def commonMatchingSourceRule = newValidForCreateCommonMatchingSourceRule()
        commonMatchingSourceRule.save(failOnError: true, flush: true)
        def id = commonMatchingSourceRule.id
        assertNotNull id
        commonMatchingSourceRule.delete()
        assertNull CommonMatchingSourceRule.get(id)
    }


    void testValidation() {
        def commonMatchingSourceRule = new CommonMatchingSourceRule(code: i_success_commonMatchingSource)
        assertFalse "CommonMatchingSourceRule could not be validated as expected due to ${commonMatchingSourceRule.errors}", commonMatchingSourceRule.validate()
    }


    void testNullValidationFailure() {
        def commonMatchingSourceRule = new CommonMatchingSourceRule()
        assertFalse "CommonMatchingSourceRule should have failed validation", commonMatchingSourceRule.validate()
        assertErrorsFor commonMatchingSourceRule, 'nullable',
                [
                        'onlineMatchIndicator',
                        'entity',
                        'transposeDateIndicator',
                        'transposeNameIndicator',
                        'aliasWildcardIndicator',
                        'lengthOverrideIndicator',
                        'apiFailureIndicator',
                        'commonMatchingSource'
                ]
        assertNoErrorsFor commonMatchingSourceRule,
                [
                        'addressType',
                        'telephoneType',
                        'emailType'
                ]
    }


    private def newValidForCreateCommonMatchingSourceRule() {
        def commonMatchingSource = new CommonMatchingSource(code: i_success_commonMatchingSource, description: 'test')
        commonMatchingSource.save(failOnError: true, flush: true)
        def commonMatchingSourceRule = new CommonMatchingSourceRule(
                onlineMatchIndicator: i_success_onlineMatchIndicator,
                entity: i_success_entity,
                transposeDateIndicator: i_success_transposeDateIndicator,
                transposeNameIndicator: i_success_transposeNameIndicator,
                aliasWildcardIndicator: i_success_aliasWildcardIndicator,
                lengthOverrideIndicator: i_success_lengthOverrideIndicator,
                apiFailureIndicator: i_success_apiFailureIndicator,
                commonMatchingSource: commonMatchingSource,
                addressType: i_success_addressType,
                telephoneType: i_success_telephoneType,
                emailType: i_success_emailType,
        )
        return commonMatchingSourceRule
    }


}

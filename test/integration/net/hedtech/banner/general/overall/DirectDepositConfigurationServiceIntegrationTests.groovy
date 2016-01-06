/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 *
 */
class DirectDepositConfigurationServiceIntegrationTests extends BaseIntegrationTestCase {

    def directDepositConfigurationService
    Sql sql
    
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()

        sql = new Sql(sessionFactory.getCurrentSession().connection())
    }


    @After
    public void tearDown() {
        super.tearDown()
        sql?.close()
    }


    // Test getDirectDepositParams
    //////////////////////////////

    /*
     * This single test on getDirectDepositParams() tests its basic functionality.
     * getDirectDepositParams() calls getParamFromWebTailor(), and more comprehensive
     * tests are done directly on that down below.
     */

    @Test
    void testGetDirectDepositParamsFromWebTailorWithOneKeyAndNoDefaultValue() {
        def retParams = []

        def testParams = [
                [paramKey: "SYSTEM_NAME"]
        ]

        def origParams = directDepositConfigurationService.directDepositConfigParams
        directDepositConfigurationService.directDepositConfigParams = testParams

        def params = directDepositConfigurationService.getDirectDepositParams()

        directDepositConfigurationService.directDepositConfigParams = origParams

        assertEquals "Banner", params[testParams[0].paramKey]
    }


    // Test getParam()
    //////////////////

    @Test
    void testGetParamWithGoodKeyAndNoDefaultValue() {
        def val = directDepositConfigurationService.getParam('SYSTEM_NAME')

        assertEquals "Banner", val
    }

    @Test
    void testGetParamWithGoodKeyAndDefaultValue() {
        def val = directDepositConfigurationService.getParam('SYSTEM_NAME', 'dummy_default_value')

        assertEquals "Banner", val
    }

    @Test
    void testGetParamWithBadKeyAndDefaultValue() {
        def val = directDepositConfigurationService.getParam('I_DONT_EXIST', 'dummy_default_value')

        assertEquals "dummy_default_value", val
    }

    @Test(expected = ApplicationException.class)
    void testGetParamWithBadKeyAndNoDefaultValue() {
        def val = directDepositConfigurationService.getParam('I_DONT_EXIST')

        // The "expected=..." in the annotation above is the assertion.
    }


    // Test getParamFromWebTailor
    /////////////////////////////

    @Test
    void testGetParamFromWebTailorWithOneKeyAndNoDefaultValue() {
        def retParams = []
        def params = [
                [paramKey: "SYSTEM_NAME"]
        ]

        params.each {
            retParams.push directDepositConfigurationService.getParamFromWebTailor(sql, it)
        }

        assertEquals 1, retParams.size()

        def item = retParams[0]
        assertEquals "Banner", item.value
    }

    @Test
    void testgetDirectDepositParamsFromWebTailorWithTwoKeysAndNoDefaultValue() {
        def retParams = []
        def params = [
                [paramKey: "SYSTEM_NAME"],
                [paramKey: "AUDITUSERID"]
        ]

        params.each {
            retParams.push directDepositConfigurationService.getParamFromWebTailor(sql, it)
        }

        assertEquals 2, retParams.size()

        def item = retParams[0]
        assertEquals "Banner", item.value

        item = retParams[1]
        assertEquals "WEBUSER", item.value
    }

    @Test
    void testGetParamFromWebTailorWithOneKeyAndDefaultValue() {
        def retParams = []
        def params = [
                [paramKey: "SYSTEM_NAME", defaultValue: "default_val"]
        ]

        params.each {
            retParams.push directDepositConfigurationService.getParamFromWebTailor(sql, it)
        }

        assertEquals 1, retParams.size()

        def item = retParams[0]
        assertEquals "Banner", item.value
    }

    @Test
    void testGetParamFromWebTailorWithTwoKeysAndTwoDefaultValues() {
        def retParams = []
        def params = [
                [paramKey: "SYSTEM_NAME", defaultValue: "default_val1"],
                [paramKey: "AUDITUSERID", defaultValue: "default_val2"]
        ]

        params.each {
            retParams.push directDepositConfigurationService.getParamFromWebTailor(sql, it)
        }

        assertEquals 2, retParams.size()

        def item = retParams[0]
        assertEquals "Banner", item.value

        item = retParams[1]
        assertEquals "WEBUSER", item.value
    }

    @Test
    void testGetParamFromWebTailorWithOneBadKeyAndDefaultValue() {
        def retParams = []
        def params = [
                [paramKey: "key_does_not_exist", defaultValue: "default_val"]
        ]

        params.each {
            retParams.push directDepositConfigurationService.getParamFromWebTailor(sql, it)
        }

        assertEquals 1, retParams.size()

        def item = retParams[0]
        assertEquals "default_val", item.value
    }

    @Test
    void testGetParamFromWebTailorWithTwoBadKeysAndDefaultValues() {
        def retParams = []
        def params = [
                [paramKey: "key_does_not_exist1", defaultValue: "default_val1"],
                [paramKey: "key_does_not_exist2", defaultValue: "default_val2"]
        ]

        params.each {
            retParams.push directDepositConfigurationService.getParamFromWebTailor(sql, it)
        }

        assertEquals 2, retParams.size()

        def item = retParams[0]
        assertEquals "default_val1", item.value

        item = retParams[1]
        assertEquals "default_val2", item.value
    }

    @Test(expected = ApplicationException.class)
    void testGetWebTailorParameterValueWithOneNullKeyAndNoDefaultValue() {
        def retParams = []
        def params = [
                [paramKey: null]
        ]

        params.each {
            retParams.push directDepositConfigurationService.getParamFromWebTailor(sql, it)
        }

        // The "expected=..." in the annotation above is the assertion.
    }
}

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


    @Test
    void testGetWebTailorParameterValuesWithOneKeyAndNoDefaultValue() {
        def retParams = []
        def params = [
                [paramKey: "SYSTEM_NAME"]
        ]

        params.each {
            retParams.push directDepositConfigurationService.getParamFromWebTailor(sql, it)
        }

        assertEquals 1, retParams.size()

        def item = retParams[0]
        assertEquals "Banner", item.paramValue
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
        assertEquals "Banner", item.paramValue

        item = retParams[1]
        assertEquals "WEBUSER", item.paramValue
    }

    @Test
    void testgetDirectDepositParamsFromWebTailorWithOneKeyAndDefaultValue() {
        def retParams = []
        def params = [
                [paramKey: "SYSTEM_NAME", defaultValue: "default_val"]
        ]

        params.each {
            retParams.push directDepositConfigurationService.getParamFromWebTailor(sql, it)
        }

        assertEquals 1, retParams.size()

        def item = retParams[0]
        assertEquals "Banner", item.paramValue
    }

    @Test
    void testgetDirectDepositParamsFromWebTailorWithTwoKeysAndTwoDefaultValues() {
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
        assertEquals "Banner", item.paramValue

        item = retParams[1]
        assertEquals "WEBUSER", item.paramValue
    }

    @Test
    void testgetDirectDepositParamsFromWebTailorWithOneBadKeyAndDefaultValue() {
        def retParams = []
        def params = [
                [paramKey: "key_does_not_exist", defaultValue: "default_val"]
        ]

        params.each {
            retParams.push directDepositConfigurationService.getParamFromWebTailor(sql, it)
        }

        assertEquals 1, retParams.size()

        def item = retParams[0]
        assertEquals "default_val", item.paramValue
    }

    @Test
    void testgetDirectDepositParamsFromWebTailorWithTwoBadKeysAndDefaultValues() {
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
        assertEquals "default_val1", item.paramValue

        item = retParams[1]
        assertEquals "default_val2", item.paramValue
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

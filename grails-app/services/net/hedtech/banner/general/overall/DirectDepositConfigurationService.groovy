/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
package net.hedtech.banner.general.overall

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

class DirectDepositConfigurationService extends ServiceBase {

    static transactional = true

    static final def SHOW_USER_PRENOTE_STATUS = 'SHOW_USER_PRENOTE_STATUS'
    static final def MAX_USER_PAYROLL_ALLOCATIONS = 'MAX_USER_PAYROLL_ALLOCATIONS'

    def userRoleService
    def directDepositAccountCompositeService

    /**
     * Direct Deposit app configuration parameters to retrieve
     */
    def directDepositConfigParams = [
            [paramKey: SHOW_USER_PRENOTE_STATUS, defaultValue: 'Y'],
            [paramKey: MAX_USER_PAYROLL_ALLOCATIONS, defaultValue: '99']
    ]

    /**
     * Get all configuration params for the Direct Deposit app from Web Tailor.
     * @return Map of all Direct Deposit configuration parameter keys and values
     */
    def getDirectDepositParams () {
        def retParams = [:]
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

        // Gather Web Tailor params
        try {
            directDepositConfigParams.each {
                def param = getParamFromWebTailor(sql, it)
                retParams[param.key] = param.value
            }
        } finally {
            sql?.close()
        }

        // Add role params
        retParams.roles = userRoleService.getRoles();

        // Add "are accounts updatable" param
        retParams.areAccountsUpdatable = directDepositAccountCompositeService.areAccountsUpdatable()

        retParams
    }

    /**
     * Get the value for the specified Web Tailor parameter key.
     * "defaultValue" can optionally be provided to use as a value in
     * the event the parameter is not configured in Web Tailor.
     * @param key
     * @param defaultValue
     * @return Value for the specified parameter key
     */
    def getParam (key, defaultValue = null) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def requestedParam = [
                paramKey: key,
                defaultValue: defaultValue
        ]

        try {
            getParamFromWebTailor(sql, requestedParam).value
        } finally {
            sql?.close()
        }
    }

    def getParamFromWebTailor (sql, map) {
        def key = map.paramKey
        def defaultVal = map.defaultValue
        def val
        def retMap = [:]

        try {
            sql.call("{? = call twbkwbis.f_fetchwtparam (?)}", [Sql.VARCHAR, key]) { result -> val = result }
        } catch (e) {
            log.error("Error retrieving value for Web Tailor parameter \"" + key + "\". " +
                    "Will attempt to use default value of \"" + defaultVal + "\".", e)

            if (defaultVal) {
                val = defaultVal
            } else {
                log.error("Web Tailor parameter key retrieval failed, and no default value is provided; failed to retrieve value")
                throw new ApplicationException(DirectDepositConfigurationService, "@@r1:configValueError@@")
            }
        }

        if (!val) {
            if (defaultVal) {
                log.error("No value found for Web Tailor parameter key \"" + key + "\". " +
                        "This should be configured in Web Tailor. Using default value of \"" + defaultVal + "\".")
                val = defaultVal
            } else {
                log.error("No value found for Web Tailor parameter key \"" + key + "\", and default value was not provided." +
                        "This should be configured in Web Tailor.")
                throw new ApplicationException(DirectDepositConfigurationService, "@@r1:configValueError@@")
            }
        }

        retMap.key = key
        retMap.value = val

        retMap
    }
}

package net.hedtech.banner.general.overall

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

class DirectDepositConfigurationService extends ServiceBase {

    static transactional = true

    /**
     * Direct Deposit app configuration parameters to retrieve
     */
    private static final directDepositConfigParams = [
            [paramKey: "SHOW_USER_PRENOTE_STATUS", defaultValue: "Y"],
            [paramKey: "MAX_USER_PAYROLL_ALLOCATIONS", defaultValue: "99"]
    ]

    def getDirectDepositParamsFromWebTailor () {
        def retParams = []
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

        try {
            directDepositConfigParams.each {
                retParams.push getParamFromWebTailor(sql, it)
            }
        } finally {
            sql?.close()
        }

        return retParams;
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

        retMap.paramKey = key
        retMap.paramValue = val

        retMap
    }
}

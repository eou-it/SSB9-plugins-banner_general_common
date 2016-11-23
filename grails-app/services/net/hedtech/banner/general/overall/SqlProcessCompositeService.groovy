/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import groovy.sql.Sql
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
class SqlProcessCompositeService {

    def sessionFactory

    List getSqlProcessResults( Map params ) {
        if ( params.sqlCode && params.sqlProcessCode ) {
            def parsedSql = SqlProcess.fetchActiveValidatedPriorityProcessSql(params.sqlCode, params.sqlProcessCode)
            def bindValues = [:]
            SqlProcessParameterByProcess.findAllByEntriesForSqlProcess(params.sqlProcessCode).each { SqlProcessParameterByProcess parameter ->
                if( parsedSql.contains(":" + parameter.parameterForSqlProcess.toLowerCase())) {
                    bindValues.put(parameter.parameterForSqlProcess.toLowerCase(),
                            params.get(parameter.parameterForSqlProcess.toLowerCase() ?: parameter.parameterForSqlProcess.toUpperCase()))
                }
                if( parsedSql.contains(":" + parameter.parameterForSqlProcess.toUpperCase())) {
                    bindValues.put(parameter.parameterForSqlProcess.toUpperCase(),
                            params.get(parameter.parameterForSqlProcess.toLowerCase() ?: parameter.parameterForSqlProcess.toUpperCase()))
                }
            }
            log.debug "Parsed SQL: ${parsedSql}"
            log.debug "Bind variables: ${bindValues.toString()}"
            def rows
            def conn
            try {
                conn = sessionFactory.currentSession.connection()
                Sql db = new Sql(conn)
                if (bindValues.size() > 0) {
                    rows = db.rows(parsedSql, bindValues)
                } else {
                    rows = db.rows(parsedSql)
                }
            }
            finally {
                conn?.close()
            }
            return rows
        }
        return null

    }

    /**
     * This method will call subsequent SQLProcess' based on:
     *      Active
     *      SequenceNumber
     *      Date being within range.
     *
     * It fetches all of the active process objects and goes one by one in sequence order until one returns a non-null
     * result. The result must match up with a Term record. It then returns that result. If none return a result, it returns an empty list.
     * @param params A map of the following:
     *      sqlCode - The SqlProceess.entriesForSql code
     *      sqlProcessCode - The SqlProceess.entriesForSqlProcess process code
     *      <any additional params> - Any additional params needed for the SQL. These should match what is in the
     *      SqlProcessParameterByProcess table for the given process.
     * @return A Term returned by the query. null if no values were returned.
     */
    def getSqlProcessResultsFromHierarchy( Map params ) {
        def rows
        if ( params.sqlCode && params.sqlProcessCode ) {
            def parsedSqlList = SqlProcess.fetchAllActiveValidatedPriorityProcessSql(params.sqlCode, params.sqlProcessCode)
            for (def i=0;i<parsedSqlList.size();i++) {
                def parsedSql = parsedSqlList[i]
                def bindValues = [:]
                SqlProcessParameterByProcess.findAllByEntriesForSqlProcess(params.sqlProcessCode).each { SqlProcessParameterByProcess parameter ->
                    if (parsedSql.contains(":" + parameter.parameterForSqlProcess.toLowerCase())) {
                        bindValues.put(parameter.parameterForSqlProcess.toLowerCase(),
                                params.get(getParameterValue(parameter.parameterForSqlProcess, params)))
                    }
                    if (parsedSql.contains(":" + parameter.parameterForSqlProcess.toUpperCase())) {
                        bindValues.put(parameter.parameterForSqlProcess.toUpperCase(),
                                getParameterValue(parameter.parameterForSqlProcess, params))
                    }
                }
                log.debug "Parsed SQL: ${parsedSql}"
                log.debug "Bind variables: ${bindValues.toString()}"

                def conn
                try {
                    conn = sessionFactory.currentSession.connection()
                    Sql db = new Sql(conn)
                    if (bindValues.size() > 0) {
                        rows = db.rows(parsedSql, bindValues)
                    } else {
                        rows = db.rows(parsedSql)
                    }
                    if (null != rows && rows.size() > 0 && rows[0][0] != null) {
                        break;
                    }
                }
                finally {
                    conn?.close()
                }

            }
        }
        // Empty rows
        if (rows != null && rows.size() == 0) {
            return null;
        }
        // function called but null returned as data
        else if (rows != null && rows[0][0] == null) {
            return null;
        }
        return rows

    }

    /**
     * The GORRSQL rules will allow checking using the standard SSB roles, the pidm, and the telephone type
     * or email type.
     * The first GORRSQL rule that passes back a Y or N will be the one that determines whether or not
     * the telephone type is valid for insertion. If no active rules are found, then it is assumed that all
     * the types are valid. If there is at least one rule active, but no rule ever passes back an N or Y,
     * then it is assumed that the type is invalid.
     * @param rule - the SSB rule to be executed, 'SSB_TELEPHONE_UPDATE' or 'SSB_EMAIL_UPDATE'
     * @param params A map of the following:
     *      PIDM - the user's pidm
     *      ROLE_<standard SSB role> - set to 'Y' to indicate the user's role
     *      TELEPHONE_TYPE - the telephone type code from STVTELE that will be validated when rule is set to
     *      'SSB_TELEPHONE_UPDATE'
     *      EMAIL_TYPE - the email type code from GTVEMAL that will be validated when rule is set to
     *      'SSB_EMAIL_UPDATE'
     * @return boolean - true if no rules are active, or if the rules hierarchy returns 'Y'. false if no active
     * rule returns 'Y'
     */
    boolean getSsbRuleResult(rule, params) {
        params.sqlCode = rule
        params.sqlProcessCode = rule

        if(SqlProcess.fetchAllActiveValidatedPriorityProcessSql(rule, rule).size() == 0){
            return true;
        }

        def result = getSqlProcessResultsFromHierarchy(params)

        return (result != null && result[0][0] == 'Y') as boolean
    }


    private def getParameterValue(def key, def fromThis) {
        def value = fromThis[key.toUpperCase()]
        if (null == value) {
            value = fromThis[key.toLowerCase()]
        }
        return value
    }
}

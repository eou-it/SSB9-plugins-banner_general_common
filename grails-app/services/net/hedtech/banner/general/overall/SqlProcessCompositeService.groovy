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
            SqlProcessParameter.findAllByEntriesForSqlProcess(params.sqlProcessCode).each { SqlProcessParameter parameter ->
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
     * result. It then returns that result. If none return a result, it returns an empty list.
     * @param params A map of the following:
     *      sqlCode - The SqlProceess.entriesForSql code
     *      sqlProcessCode - The SqlProceess.entriesForSqlProcess process code
     *      <any additional params> - Any additional params needed for the SQL. These should match what is in the
     *      SqlProcessParameter table for the given process.
     * @return A list of values returned by the query. an empty list if no values were returned.
     */
    List getSqlProcessResultsFromHierarchy( Map params ) {
        def results = []
        if ( params.sqlCode && params.sqlProcessCode ) {
            def parsedSqlList = SqlProcess.fetchAllActiveValidatedPriorityProcessSql(params.sqlCode, params.sqlProcessCode)
            for (def i=0;i<parsedSqlList.size();i++) {
                def parsedSql = parsedSqlList[i]
                def bindValues = [:]
                SqlProcessParameter.findAllByEntriesForSqlProcess(params.sqlProcessCode).each { SqlProcessParameter parameter ->
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
                if (rows) {
                    results = rows
                    break
                }
            }
        }
        return results

    }


    private def getParameterValue(def key, def fromThis) {
        def value = fromThis[key.toUpperCase()]
        if (null == value) {
            value = fromThis[key.toLowerCae()]
        }
        return value
    }
}

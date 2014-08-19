/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import groovy.sql.Sql
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
class SqlProcessCompositeService {

    def dataSource

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
                conn = dataSource.getConnection()
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
}

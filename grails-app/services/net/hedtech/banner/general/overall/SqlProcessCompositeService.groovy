/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import groovy.sql.Sql
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import net.hedtech.banner.general.system.EntriesForSqlProcesss

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
class SqlProcessCompositeService {

    def sessionFactory

    List getSqlProcessResults( Map params ) {
        if ( params.sqlCode && params.sqlProcessCode ) {
            def parsedSql = SqlProcess.fetchActiveValidatedPriorityProcessSql(params.sqlCode, params.sqlProcessCode)
            def bindValues = [:]
            SqlProcessParameterByProcess.findAllByEntriesForSqlProcesss(EntriesForSqlProcesss.findByCode(params.sqlProcessCode)).each { SqlProcessParameterByProcess parameter ->
                if( parsedSql.contains(":" + parameter.sqlProcessParameter.code.toLowerCase())) {
                    bindValues.put(parameter.sqlProcessParameter.code.toLowerCase(),
                            params.get(parameter.sqlProcessParameter.code.toLowerCase() ?: parameter.sqlProcessParameter.code.toUpperCase()))
                }
                if( parsedSql.contains(":" + parameter.sqlProcessParameter.code.toUpperCase())) {
                    bindValues.put(parameter.sqlProcessParameter.code.toUpperCase(),
                            params.get(parameter.sqlProcessParameter.code.toLowerCase() ?: parameter.sqlProcessParameter.code.toUpperCase()))
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
}

/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.population

import groovy.sql.Sql

import java.sql.SQLException

/*********************************************************************************
 Copyright 2012 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/**
 * Created by edelaney on 3/26/14.
 */

class CommunicationPopulationQueryStatementParseService {

    def sessionFactory
    def sql


    def CommunicationPopulationQueryParseResult parse(String statement) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def exceptionMessage
        def populationQueryParseResult = new CommunicationPopulationQueryParseResult()
        println "parsing populationQuery string =" + statement

        try {
            //def stmt = '{call gokextr.p_validate_sql(?,?,?,?,?)}'
            def stmt = '{call gokextr.p_validate_sql(?,?,?,?,?)}'
            def params = [statement, Sql.VARCHAR, Sql.VARCHAR, Sql.INTEGER, Sql.INTEGER]
            sql.call stmt, params, { status, message, cost, cardinality ->
                populationQueryParseResult.status = status
                populationQueryParseResult.message = message
                populationQueryParseResult.cost = cost
                populationQueryParseResult.cardinality = cardinality
            }
        }
        catch (SQLException ae) {
            log.debug "SqlException in parse ${ae}"
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug "Exception in parse ${ae}"
            log.debug ae.stackTrace
            throw ae
        }
        finally {

        }
        return populationQueryParseResult
    }

}

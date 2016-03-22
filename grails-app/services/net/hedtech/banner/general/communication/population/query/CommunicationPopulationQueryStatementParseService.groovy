/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.population.query

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.field.CommunicationField

import java.sql.SQLException

/*********************************************************************************
 Copyright 2012 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/**
 * Created by edelaney on 3/26/14.
 */

class CommunicationPopulationQueryStatementParseService {

    def sessionFactory


    def CommunicationPopulationQueryParseResult parse(String statement, Boolean multiSelectColumnAllowed=true) {
        def conn = sessionFactory.getCurrentSession().connection()
        def sql = new Sql(conn)
        def populationQueryParseResult = new CommunicationPopulationQueryParseResult()

        if (statement == null || statement == "") {
            sql?.close()
            conn?.close()
            return
        }

        try {
            //test for sql injection and throw exception if found
            // TODO: Remove the if statement by providing seperate methods for communication field and
            // population query parsing.
            if (CommunicationCommonUtility.sqlStatementNotAllowed(statement,multiSelectColumnAllowed )) {
                if (multiSelectColumnAllowed)
                    throw new ApplicationException(CommunicationField, "@@r1:sqlStatementInvalidCall@@")
                else {
                    // TODO: Should be refactored to return the parse result with a status of N
                    // instead of throwing an exception. The localization is different given how the results
                    // coming back from oracle.
                    throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryInvalidCall@@")
                }
            }

            //get the browser locale and set the nls language of the database
            CommunicationCommonUtility.setLocaleInDatabase(sql)

            def stmt = '{call gckextr.p_validate_sql(?,?,?,?,?)}'
            def params = [statement, Sql.VARCHAR, Sql.VARCHAR, Sql.INTEGER, Sql.INTEGER]
            sql.call stmt, params, { status, message, cost, cardinality ->
                populationQueryParseResult.status = status
                populationQueryParseResult.message = message
                populationQueryParseResult.cost = cost
                populationQueryParseResult.cardinality = cardinality
            }
        }
        catch (SQLException sqle) {
            populationQueryParseResult.status = 'N'
            populationQueryParseResult.message = sqle.message
            log.debug "SqlException in parse ${sqle}"
            log.debug sqle.stackTrace
            //throw ae
        }
        catch (Exception ae) {
            populationQueryParseResult.status = 'N'
            populationQueryParseResult.message = ae.message
            log.debug "Exception in parse ${ae}"
            log.debug ae.stackTrace
            throw ae
        } finally {
            //close the sql and the connection as it was for just this parse
            sql?.close()
            conn?.close()
        }

        return populationQueryParseResult
    }

}

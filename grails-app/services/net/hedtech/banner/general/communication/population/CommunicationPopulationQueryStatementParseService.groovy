/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.population

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
    def sql


    def CommunicationPopulationQueryParseResult parse(String statement, Boolean multiSelectColumnAllowed=true) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def populationQueryParseResult = new CommunicationPopulationQueryParseResult()

        if (statement == null || statement == "") {
            return
        }

        try {
            //test for sql injec tion and throw exception if found
            if (CommunicationCommonUtility.sqlStatementNotAllowed(statement,multiSelectColumnAllowed )) {
                if (multiSelectColumnAllowed)
                    throw new ApplicationException(CommunicationField, "@@r1:sqlStatementInvalidCall@@")
                else
                    throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryInvalidCall@@")
            }

            def stmt = '{call gckextr.p_validate_sql(?,?,?,?,?)}'
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

        return populationQueryParseResult
    }

}

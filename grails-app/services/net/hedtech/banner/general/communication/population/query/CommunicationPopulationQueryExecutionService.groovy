/*********************************************************************************
 Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.population.query

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import grails.web.context.ServletContextHolder
import grails.util.Holders

import java.sql.SQLException
import java.util.regex.Matcher
import java.util.regex.Pattern

@Transactional
class CommunicationPopulationQueryExecutionService {

    def communicationPopulationQueryService
    def communicationPopulationQueryVersionService
    def communicationPopulationQueryStatementParseService
    def sessionFactory
    def sql
    def Pattern multipattern = Pattern.compile("SELECT(.*?)FROM");


//    def CommunicationPopulationQueryParseResult parse(Long populationQueryId) {
//
//        //throw exception if the banner security for query execution is not setup for this user
//        if (!CommunicationCommonUtility.userCanExecuteQuery()) {
//            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:operation.not.authorized@@")
//        }
//
//        def populationQueryParseResult = new CommunicationPopulationQueryParseResult()
//        def populationQuery = communicationPopulationQueryService.get(populationQueryId)
//
//        populationQueryParseResult = communicationPopulationQueryStatementParseService.parse(populationQuery.queryString, false)
//        return populationQueryParseResult
//    }


    /**
     *
     * @param queryVersionId
     * @returns the population selection list id
     */
    CommunicationPopulationQueryExecutionResult execute(Long queryVersionId, String requestedBy=null) {
        assert queryVersionId != null
        try {
            //throw exception if the banner security for query execution is not setup for this user
            if (!CommunicationCommonUtility.userCanExecuteQuery()) {
                throw new ApplicationException(CommunicationPopulationQuery, "@@r1:operation.not.authorized@@")
            }

            if (!queryVersionId) {
                throw new ApplicationException(CommunicationPopulationQuery, "@@r1:nullPopulationQueryId@@")
            }
            CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryVersionService.get(queryVersionId)
            if (!queryVersion) {
                throw new ApplicationException(CommunicationPopulationQuery, "@@r1:populationQueryDoesNotExist@@")
            }

            String sqlStatement = getSqlStatement( queryVersion )

//            def ctx = Holders.grailsApplication.getMainContext()
//            def sessionFactory = ctx.sessionFactory
            def session = sessionFactory.currentSession
            def sql = new Sql(session.connection())

            def stmt = "{call gckextr.p_execute_pop_queryVersion(?,?,?,?,?,?)}"
            def params = [sqlStatement, Sql.INTEGER, Sql.INTEGER, Sql.VARCHAR, Sql.VARCHAR, requestedBy]

            CommunicationPopulationQueryExecutionResult result
            sql.call stmt, params, { selectionListId, calculatedCount, calculatedBy, errorString ->
                result = new CommunicationPopulationQueryExecutionResult( [
                    selectionListId: selectionListId as Long,
                    calculatedCount: calculatedCount as Long,
                    calculatedBy: calculatedBy as String,
                    errorString: errorString as String
                ] )
            }
            return result
        }
        catch (SQLException sqle) {
            log.error "SqlException in gckextr.p_execute_pop_queryVersion ${sqle}"
            log.error sqle.stackTrace
            throw sqle
        }
        catch (Exception ae) {
            log.error "Exception in gckextr.p_execute_pop_queryVersion ${ae}"
            log.error ae.stackTrace
            throw ae
        }
        finally {
//            try {
//                if (sql) sql.close()
//            }
//            catch (SQLException af) {
//                // ignore
//            }
        }
    }

    /**
     *
     * @param populationQueryId
     * @returns the population selection list id
     */
    CommunicationPopulationQueryExecutionResult executeQuery(Long populationQueryId, String requestedBy=null) {

        try {
            //throw exception if the banner security for query execution is not setup for this user
            if (!CommunicationCommonUtility.userCanExecuteQuery()) {
                throw new ApplicationException(CommunicationPopulationQuery, "@@r1:operation.not.authorized@@")
            }

            if (!populationQueryId) {
                throw new ApplicationException(CommunicationPopulationQuery, "@@r1:nullPopulationQueryId@@")
            }
            CommunicationPopulationQuery populationQuery = communicationPopulationQueryService.get(populationQueryId)
            if (!populationQuery) {
                throw new ApplicationException(CommunicationPopulationQuery, "@@r1:populationQueryDoesNotExist@@")
            }

            List queryVersions = CommunicationPopulationQueryVersion.findByQueryId( populationQuery.id )
            if (queryVersions.size() == 0) {
                throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationQueryExecutionService.class, "populationQueryNotPublished" )
            }
            CommunicationPopulationQueryVersion queryVersion = queryVersions?.get( 0 )

            /* Make sure it's valid */
            if (!queryVersion) {
                throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryInvalid@@")
            }

            //make sure the sql statement only selects one value
            String sqlStatement = getSqlStatement( queryVersion )

            Matcher matcher = multipattern.matcher( sqlStatement );
            while (matcher.find()) {
                if (matcher.group(1).contains(",")) {
                    throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryHasMultiple@@")
                }
            }

            def ctx = Holders.grailsApplication.getMainContext()
            def sessionFactory = ctx.sessionFactory
            def session = sessionFactory.currentSession
            def sql = new Sql(session.connection())

            def stmt = "{call gckextr.p_execute_pop_query(?,?,?,?,?,?)}"
            def params = [sqlStatement, Sql.INTEGER, Sql.INTEGER, Sql.VARCHAR, Sql.VARCHAR, requestedBy]

            CommunicationPopulationQueryExecutionResult result
            sql.call stmt, params, { selectionListId, calculatedCount, calculatedBy, errorString ->
                result = new CommunicationPopulationQueryExecutionResult( [
                        selectionListId: selectionListId as Long,
                        calculatedCount: calculatedCount as Long,
                        calculatedBy: calculatedBy as String,
                        errorString: errorString as String
                ] )
            }
            return result
        }
        catch (SQLException sqle) {
            log.debug "SqlException in gckextr.p_execute_pop_query ${sqle}"
            log.debug sqle.stackTrace
            throw sqle
        }
        catch (Exception ae) {
            log.debug "Exception in gckextr.p_execute_pop_query ${ae}"
            log.debug ae.stackTrace
            throw ae
        }
        finally {
            try {
             //TODO grails3   if (sql) sql.close()
            }
            catch (SQLException af) {
                // ignore
            }
        }
    }

    private String getSqlStatement(CommunicationPopulationQueryVersion queryVersion) {
        assert( queryVersion != null )

        if (queryVersion.type == CommunicationPopulationQueryType.POPULATION_SELECTION_EXTRACT) {
            CommunicationPopulationQueryExtractStatement extractStatement = new CommunicationPopulationQueryExtractStatement()
            extractStatement.setQueryString( queryVersion.queryString )
            return extractStatement.toSqlStatement()
        } else {
            return queryVersion.queryString
        }
    }

}

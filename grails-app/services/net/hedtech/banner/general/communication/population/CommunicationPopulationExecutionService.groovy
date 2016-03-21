/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.population

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersion
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionList
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes

import java.sql.SQLException
import java.util.regex.Matcher
import java.util.regex.Pattern

class CommunicationPopulationExecutionService {

    def communicationPopulationQueryService
    def communicationPopulationQueryVersionService
    def communicationPopulationQueryStatementParseService
    def sessionFactory
    def sql
    def Pattern multipattern = Pattern.compile("SELECT(.*?)FROM");


    def CommunicationPopulationQueryParseResult parse(Long populationQueryId) {

        //throw exception if the banner security for query execution is not setup for this user
        if (!CommunicationCommonUtility.userCanExecuteQuery()) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:operation.not.authorized@@")
        }

        def populationQueryParseResult = new CommunicationPopulationQueryParseResult()
        def populationQuery = communicationPopulationQueryService.get(populationQueryId)

        populationQueryParseResult = communicationPopulationQueryStatementParseService.parse(populationQuery.sqlString, false)
        return populationQueryParseResult
    }


    /**
     *
     * @param queryVersionId
     * @returns the population selection list id
     */
    def execute(Long queryVersionId) {

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

            def resultMap = [:]

            def ctx = ServletContextHolder.servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)
            def sessionFactory = ctx.sessionFactory
            def session = sessionFactory.currentSession
            def sql = new Sql(session.connection())

            def stmt = "{call gckextr.p_execute_pop_queryVersion(?,?,?,?,?)}"
            def params = [queryVersionId, Sql.INTEGER, Sql.INTEGER, Sql.VARCHAR, Sql.VARCHAR]

            sql.call stmt, params, { selectionListId, calculatedCount, calculatedBy, errorString ->
                resultMap.put("selectionListId" , selectionListId)
                resultMap.put("calculatedCount", calculatedCount)
                resultMap.put("calculatedBy", calculatedBy)
                resultMap.put("errorString", errorString)
            }

            resultMap
        }
        catch (SQLException ae) {
            log.debug "SqlException in gckextr.p_create_popsel ${ae}"
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug "Exception in gckextr.p_create_popsel ${ae}"
            log.debug ae.stackTrace
            throw ae
        }
        finally {
            try {
                if (sql) sql.close()
            }
            catch (SQLException af) {
                // ignore
            }
        }
    }

    /**
     *
     * @param populationQueryId
     * @returns the population selection list id
     */
    def executeQuery(Long populationQueryId) {

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
                throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationExecutionService.class, "populationQueryNotPublished" )
            }
            CommunicationPopulationQueryVersion queryVersion = queryVersions?.get( 0 )

            /* Make sure it's valid */
            if (!queryVersion) {
                throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryInvalid@@")
            }

            //make sure the sql statement only selects one value

            Matcher matcher = multipattern.matcher(queryVersion.sqlString.toUpperCase());
            while (matcher.find()) {
                if (matcher.group(1).contains(",")) {
                    throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryHasMultiple@@")
                }
            }

            def resultMap = [:]

            def ctx = ServletContextHolder.servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)
            def sessionFactory = ctx.sessionFactory
            def session = sessionFactory.currentSession
            def sql = new Sql(session.connection())

            def stmt = "{call gckextr.p_execute_pop_query(?,?,?,?,?)}"
            def params = [populationQueryId, Sql.INTEGER, Sql.INTEGER, Sql.VARCHAR, Sql.VARCHAR]

            sql.call stmt, params, { selectionListId, calculatedCount, calculatedBy, errorString ->
                resultMap.put("selectionListId" , selectionListId)
                resultMap.put("calculatedCount", calculatedCount)
                resultMap.put("calculatedBy", calculatedBy)
                resultMap.put("errorString", errorString)
            }

            resultMap.selectionListId
        }
        catch (SQLException ae) {
            log.debug "SqlException in gckextr.p_create_popsel ${ae}"
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug "Exception in gckextr.p_create_popsel ${ae}"
            log.debug ae.stackTrace
            throw ae
        }
        finally {
            try {
                if (sql) sql.close()
            }
            catch (SQLException af) {
                // ignore
            }
        }
    }

}

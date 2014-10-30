/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.population

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes

import java.sql.SQLException


class CommunicationPopulationExecutionService {

    def communicationPopulationQueryService
    def communicationPopulationQueryStatementParseService
//    def populationSelectionService
    def sessionFactory
    def sql


    def CommunicationPopulationQueryParseResult parse(Long populationQueryId) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def exceptionMessage
        def populationQueryParseResult = new CommunicationPopulationQueryParseResult()
        def populationQuery = communicationPopulationQueryService.get(populationQueryId)


        populationQueryParseResult = communicationPopulationQueryStatementParseService.parse(populationQuery.sqlString)
        /**
         * If status is N, update populationQuery set valid=N and syntax error=p_error
         */
        if (populationQueryParseResult.status == 'N') {
            populationQuery.valid = false
            populationQuery.save()
        } else {
            populationQuery.valid = true
            populationQuery.save()
        }
        return populationQueryParseResult
    }

    /**
     *
     * @param populationQueryId
     * @returns the population selection list id
     */
    def execute(Long populationQueryId) {

        if (!populationQueryId) {
            throw new ApplicationException(populationQueryId, "Null populationQuery id")
        }
        def populationQuery = communicationPopulationQueryService.get(populationQueryId)
        if (!populationQuery) {
            throw new ApplicationException(populationQuery, "No such populationQuery")
        }
        /* Make sure it's not locked */
        if (populationQuery?.locked == 'Y') {
            throw new ApplicationException(populationQuery, "@@r1:lockedPopulationQuery@@")
        }
        /* Make sure it's published */
        if (populationQuery?.published == 'N') {
            throw new ApplicationException(populationQuery, "@@r1:notPublishedPopulationQuery@@")
        }
        /* Make sure it's valid */

        if (populationQuery?.valid == 'N') {
            throw new ApplicationException(populationQuery, "@@r1:invalidPopulationQuery@@")
        }

        CommunicationPopulationSelectionList populationSelectionList = new CommunicationPopulationSelectionList()



        def populationSelectionListId = null
        try {
            def ctx = ServletContextHolder.servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)
            def sessionFactory = ctx.sessionFactory
            def session = sessionFactory.currentSession
            def sql = new Sql(session.connection())

            def stmt = "{call gokextr.p_execute_pop_query(?,?)}"
            def params = [populationQueryId, Sql.INTEGER]

            sql.call stmt, params, { pk ->
                populationSelectionListId = pk
            }
            populationSelectionListId
        }
        catch (SQLException ae) {
            log.debug "SqlException in gokextr.p_create_popsel ${ae}"
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug "Exception in gokextr.p_create_popsel ${ae}"
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

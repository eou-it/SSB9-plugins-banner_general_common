/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.population

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes

import java.sql.SQLException
import java.util.regex.Matcher
import java.util.regex.Pattern

class CommunicationPopulationExecutionService {

    def communicationPopulationQueryService
    def communicationPopulationQueryStatementParseService
    def sessionFactory
    def sql
    def Pattern multipattern = Pattern.compile("SELECT(.*?)FROM");


    def CommunicationPopulationQueryParseResult parse(Long populationQueryId) {

        //throw exception if the banner security for query execution is not setup for this user
        if (!CommunicationCommonUtility.userCanExecuteQuery()) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:operation.not.authorized@@")
        }

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def exceptionMessage
        def populationQueryParseResult = new CommunicationPopulationQueryParseResult()
        def populationQuery = communicationPopulationQueryService.get(populationQueryId)


        populationQueryParseResult = communicationPopulationQueryStatementParseService.parse(populationQuery.sqlString, false)
        return populationQueryParseResult
    }

    /**
     *
     * @param populationQueryId
     * @returns the population selection list id
     */
    def execute(Long populationQueryId) {

        //throw exception if the banner security for query execution is not setup for this user
        if (!CommunicationCommonUtility.userCanExecuteQuery()) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:operation.not.authorized@@")
        }

        if (!populationQueryId) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:nullPopulationQueryId@@")
        }
        def populationQuery = communicationPopulationQueryService.get(populationQueryId)
        if (!populationQuery) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:populationQueryDoesNotExist@@")
        }

        def parseresult = parse(populationQuery.id)
        populationQuery.valid = (parseresult?.status == 'Y')
        populationQuery.save()

        populationQuery = communicationPopulationQueryService.get(populationQueryId)

        /* Make sure it's valid */
        if (!populationQuery?.valid) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryInvalid@@")
        }

        //make sure the sql statement only selects one value

        Matcher matcher = multipattern.matcher(populationQuery.sqlString.toUpperCase());
        while (matcher.find()) {
            if (matcher.group(1).contains(",")) {
                throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryHasMultiple@@")
            }
        }

        CommunicationPopulationSelectionList populationSelectionList = new CommunicationPopulationSelectionList()



        def populationSelectionListId = null
        try {
            def ctx = ServletContextHolder.servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)
            def sessionFactory = ctx.sessionFactory
            def session = sessionFactory.currentSession
            def sql = new Sql(session.connection())

            def stmt = "{call gckextr.p_execute_pop_query(?,?)}"
            def params = [populationQueryId, Sql.INTEGER]

            sql.call stmt, params, { pk ->
                populationSelectionListId = pk
            }
            populationSelectionListId
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

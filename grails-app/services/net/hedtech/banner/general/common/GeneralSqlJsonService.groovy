/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.common

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.i18n.MessageHelper
import org.grails.web.converters.exceptions.ConverterException

import java.sql.SQLException

/**
 * Service to get Json Data from SQl.
 * Executes the wrapper function by putting PIDM into the context and returns the CLOB.
 * Clob is converted to JSON and returns it.
 */
@Transactional
@Slf4j
class GeneralSqlJsonService {
    def sessionFactory
    def springSecurityService

    private void setPidmToContext( def pidm ) {
        Sql sql = new Sql( sessionFactory.getCurrentSession().connection() )
        sql.call( 'call gb_common.p_set_context(?, ?, ?,?)', ['SS_ACC', 'LOG_ID', pidm.toString(), 'N'] )
    }

    /**
     * Sets Pidm to the context and executes function.
     * Wrapper function is expected to return a CLOB object.
     * Fetches the JSON from CLOB object.
     *
     * @param functionName
     * @param listOfParams
     * @return
     */
    def call( String functionName, List listOfParams ) {
        JSON json_data = null
        try {
            String json_string
            setPidmToContext( springSecurityService.getAuthentication()?.user?.pidm )
            Sql sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.call( functionName, listOfParams,
                      {result ->
                          json_string = result?.asciiStream?.text
                      }
            )
            json_data = new JsonSlurper().parseText( json_string.toString() )

        }
        catch (SQLException | ConverterException e) {
            log.error e
            String message = MessageHelper.message( 'banner.general.sql.exception' )
            throw new ApplicationException( GeneralSqlJsonService.class, new BusinessLogicValidationException( message, [] ) )
        }
        finally {
            try {
                clearPidmContext()
            }
            catch (SQLException e) {
                log.error e
                String message = MessageHelper.message( 'banner.general.sql.exception.clearContext' )
                throw new ApplicationException( GeneralSqlJsonService.class, new BusinessLogicValidationException( message, [] ) )
            }
        }
        json_data
    }

    private clearPidmContext() {
        setPidmToContext( '' )
    }

}

/** *****************************************************************************
 © 2014 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package net.hedtech.banner.general.communication.field

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import net.hedtech.banner.service.ServiceBase
import org.stringtemplate.v4.ST

import java.sql.SQLException
import java.util.regex.Pattern

class CommunicationFieldCalculationService extends ServiceBase {

    /**
     * Executes a data function and returns result set
     * @param communicationFieldId The id of the communication field
     * @param parameters Map of parameter values
     * @return
     */
    String calculateField( String immutableId, Map parameters ) {

        def Sql sql

        CommunicationField communicationField = CommunicationField.findByImmutableId( immutableId )
        String statement = communicationField.ruleContent
        if (statement == null) {
            /* there is no statement to execute, so just return the formatter contents */
            return communicationField.formatString
        } else {
            try {
                sql = new Sql( sessionFactory.getCurrentSession().connection() )

                List<GroovyRowResult> resultSet = sql.rows( statement, parameters ) /* TODO: change this to explicitly return one row */
                def results = []
                resultSet.each { resultRecord ->
                    def row = [:]
                    resultRecord.each { record ->
                        row.put( record.getKey().toString().toLowerCase(), record.value )
                    }
                    results.add( formatString( communicationField.formatString, row ) )
                }
                return results[0]


            } catch (SQLException e) {
                throw e
            } finally {
                sql?.close()
            }
        }
    }

    /**
     * Formats a map of parameters. The formatter is a string containing $ delimited fields, each of which
     * will be mapped by name to the items with the corresponding key in the map
     * @param formatter
     * @param parameters
     * @return
     */
    String formatString( String formatter, Map parameters ) {
        char delimiter = '$'
        ST st = new ST( formatter, delimiter, delimiter );
        parameters.keySet().each { key ->
            st.add( key, parameters[key] )
        }
        st.render()
    }

    /**
     *  Extracts all parameter strings delimited by $. These can be either $foo.bar$ or just $foo$, will extract foo.
     * @param template statement
     * @return set of unique string variables found in the template string
     */
    List<String> extractTemplateVariables( String statement ) {
//        Pattern pattern = Pattern.compile( /\$(\w*)\$/ );
        Pattern pattern = Pattern.compile( /\$(\w+)[.]|(\w+?)\$/ );
        def List<String> runTimeParms = []
        def matcher = pattern.matcher( statement )

        while (matcher.find()) {
            runTimeParms << matcher.group( 2 )

        }
        runTimeParms.removeAll( Collections.singleton( null ) );
        runTimeParms.unique( false )
    }
}

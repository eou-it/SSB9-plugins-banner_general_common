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
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.service.ServiceBase
import org.stringtemplate.v4.ST
import st4hidden.org.antlr.runtime.tree.CommonTree

import java.sql.SQLException
import java.util.regex.Pattern

class CommunicationFieldCalculationService extends ServiceBase {

    String calculateFieldByBannerId( String immutableId, String bannerId ) {

        def person = PersonUtility.getPerson( bannerId )

        if (person == null) {
            throw new ApplicationException( CommunicationFieldCalculationService, "Person is null" ) // TODO: I18N these
        }
        if (immutableId == null) {
            throw new ApplicationException( CommunicationFieldCalculationService, "ImmutableId is null" )
        }

        calculateFieldByPidm( immutableId, person.pidm )
    }


    String calculateFieldByPidm( String immutableId, Long pidm ) {
        def sqlParams = [:]
        sqlParams << ['pidm': pidm]
        calculateField( immutableId, sqlParams )
    }

    /**
     * Executes a data function and returns result set
     * @param communicationFieldId The id of the communication field
     * @param parameters Map of parameter values
     * @return
     */
    private String calculateField( String immutableId, Map parameters ) {
        def Sql sql
        CommunicationField communicationField = CommunicationField.findByImmutableId( immutableId )
        // ToDo: decide if the upper bound should be configurable
        int maxRows = (!communicationField.returnsArrayArguments) ? 1 : 50
        String statement = communicationField.ruleContent
        if (statement == null) {
            /* there is no statement to execute, so just return the formatter contents */
            return communicationField.formatString
        } else {
            try {
                sql = new Sql( sessionFactory.getCurrentSession().connection() )

                List<GroovyRowResult> resultSet = sql.rows( statement, parameters, 0, maxRows )
                def attributeMap = [:]
                resultSet.each { row ->
                    row.each { column ->
                        String attributeName = column.getKey().toString().toLowerCase()
                        Object attributeValue = column.value
                        if (maxRows <= 1) {
                            attributeMap.put( attributeName, attributeValue )
                        } else {
                            // handle array of values per column name
                            ArrayList values = attributeMap.containsKey( attributeName ) ? attributeMap.get( attributeName ) : new ArrayList()
                            values.add( attributeValue )
                            attributeMap.put( attributeName, values )
                        }
                    }
                }
                return formatString( communicationField.formatString, attributeMap )
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

}

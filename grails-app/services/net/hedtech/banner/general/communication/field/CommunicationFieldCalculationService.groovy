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
//import org.stringtemplate.v4.ST

import java.sql.SQLException

class CommunicationFieldCalculationService extends ServiceBase {

    /**
     * Executes a data function and returns result set
     * @param communicationFieldId The id of the communication field
     * @param parameters Map of parameter values
     * @return
     */
    List<GroovyRowResult> calculateField( String immutableId, parameters ) {
        def resultSet
        def Sql sql
        String stmt = CommunicationField.findByImmutableId( immutableId ).ruleContent

        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )

            resultSet = sql.rows( stmt, parameters )
        } catch (SQLException e) {
            throw e
        }
    }
    /**
     * Formats a map of parameters. The formatter is a string containing $ delimited fields, each of which
     * will be mapped by name to the items with the corresponding key in the map
     * @param formatter
     * @param parameters
     * @return
     */
    String formatString( String formatter, parameters ) {

        //String formatter = "hi \$result.firstname\$! \$result.lastname\$! ";
        char delimiter = '$'
        //ST st = new ST( formatter, delimiter, delimeter );
        Map<String, Object> data = new HashMap<String, Object>()
        data.put( "person", aPerson )
    }

    /**
     *  Extracts all parameter strings starting with : into a map that has the parameter name as the key, and the value as null
     * @param statement
     * @return
     */
    Map extractRuntimeParameters( String statement ) {
        def myRegEx = /:\w*/
        def matcher = (statement =~ myRegEx)
        def Map runTimeParms = [:]
        matcher.each { runTimeParms.put( it, null ) }
        println "runtime parameters"
        runTimeParms.each { println it }

    }

}

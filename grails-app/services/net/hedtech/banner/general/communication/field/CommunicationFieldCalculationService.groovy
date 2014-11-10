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
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.service.ServiceBase

import java.sql.SQLException
import java.util.regex.Pattern

class CommunicationFieldCalculationService extends ServiceBase {

    /**
     * Executes a data function and returns result set
     * @param communicationFieldId The id of the communication field
     * @param parameters Map of parameter values
     * @return
     */
    List<GroovyRowResult> calculateField( String immutableId, Map parameters ) {
        def resultSet
        if (parameters.bannerId && !parameters.pidm) {
            def person = PersonUtility.getPerson( parameters.bannerId )
            parameters.put( "pidm", person.pidm )
        } else {
            def person = PersonUtility.getPerson( parameters.pidm )
            parameters.put( "bannerId", person.bannerId )
        }
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

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
import org.stringtemplate.v4.NumberRenderer
import org.stringtemplate.v4.ST
import org.stringtemplate.v4.STGroup

import java.sql.SQLException

class CommunicationFieldCalculationService extends ServiceBase {

    /**
     * Merges the data from the parameter map into the string template
     * @param stringTemplate A stcring containing delimited token fields
     * @param parameters Map of name value pairs representing tokens in the template and their values
     * @return A fully rendered String
     */
    public String merge( String stringTemplate, Map<String, String> parameters ) {
        if (log.isDebugEnabled()) log.debug( "Merging parameters into field formatter string." );
        if (stringTemplate && parameters) {
            ST st = newST( stringTemplate );
            parameters.keySet().each { key ->
                st.add( key, parameters[key] )
            }
            return st.render()
        } else {
            // You have nothing to do, so just return the input content
            return stringTemplate
        }
    }


    /**
     *  Extracts all delimited parameter strings. Currently only supports $foo$, not $foo.bar$
     *  This will throw an application exception if the template string cannot be parsed.
     * @param content the text to analyze
     * @return set of unique string variables found in the template string
     * Will throw a run time application exception if the content is completely unparsable
     */
    public List<String> extractVariables( String content ) {
        if (log.isDebugEnabled()) log.debug( "Extracting field variables from field string." )

        if (content == null) {
            return new ArrayList<String>()
        } else {
            ST st = newST( content )
            st.render()
            CommunicationFieldMissingPropertyCapture missingPropertyCapture = (CommunicationFieldMissingPropertyCapture) st.groupThatCreatedThisInstance.getListener()
            return missingPropertyCapture.missingProperties.toList()
        }
    }


    Map calculateFieldByBannerId( String immutableId, String bannerId ) {

        def person = PersonUtility.getPerson( bannerId )

        if (person == null) {
            throw new ApplicationException( CommunicationFieldCalculationService, "@@r1:idInvalid@@" )
        }
        if (immutableId == null) {
            throw new ApplicationException( CommunicationFieldCalculationService, "@@r1:immutableIdInvalid@@" )
        }
        def returnmap = [:]

        def confdecmap = PersonUtility.isPersonConfidentialOrDeceased( person.pidm )
        returnmap << ["confidential": confdecmap.confidential]
        returnmap << ["deceased": confdecmap.deceased]
        returnmap << ["name": PersonUtility.formatName( [lastName: person.lastName, firstName: person.firstName, mi: person.middleName] )]
        returnmap << ["fieldResult": calculateFieldByPidm( immutableId, person.pidm )]
        returnmap
    }

    /**
     * Convenience method to calculate a field with only a pidm as input
     * @param immutableId A CommunicationField immutable identifier
     * @param pidm
     * @return
     */
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

                String formatString = communicationField.formatString ?: ""
                return merge( formatString, attributeMap )
            } catch (SQLException e) {
                throw new ApplicationException( CommunicationFieldCalculationService, e.message )
            } catch (Exception e) {
                throw new ApplicationException( CommunicationFieldCalculationService, e.message )
            } finally {
                sql?.close()
            }
        }
    }

    private ST newST( String templateString ) {
        char delimiter = '$'
        CommunicationFieldMissingPropertyCapture missingPropertyCapture = new CommunicationFieldMissingPropertyCapture()
        STGroup group = new STGroup( delimiter, delimiter )
        group.setListener( missingPropertyCapture )
        group.registerRenderer( Integer.class, new NumberRenderer() );
        return new org.stringtemplate.v4.ST( group, templateString );
    }
}

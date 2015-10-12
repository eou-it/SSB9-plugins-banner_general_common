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
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.service.ServiceBase
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.stringtemplate.v4.DateRenderer
import org.stringtemplate.v4.NumberRenderer
import org.stringtemplate.v4.ST
import org.stringtemplate.v4.STGroup

import java.sql.Connection
import java.sql.SQLException

class CommunicationFieldCalculationService extends ServiceBase {

    def asynchronousBannerAuthenticationSpoofer
    /**
     * Merges the data from the parameter map into the string template
     * @param stringTemplate A stcring containing delimited token fields
     * @param parameters Map of name value pairs representing tokens in the template and their values
     * @return A fully rendered String
     */
    public String merge( String stringTemplate, Map parameters ) {
        if (log.isDebugEnabled()) log.debug( "Merging parameters into field formatter string." );
        if (stringTemplate == null || stringTemplate.size() == 0) return ""

        if (parameters == null) parameters = [:]

        ST st = newST( stringTemplate );
        parameters.keySet().each { key ->
            st.add( key, parameters[key] )
        }
        String firstPass = st.render()

        // Check if any missing parameters and if so replace them with an empty String.
        CommunicationFieldMissingPropertyCapture missingPropertyCapture = (CommunicationFieldMissingPropertyCapture) st.groupThatCreatedThisInstance.getListener()
        if (missingPropertyCapture.missingProperties.size() == 0) {
            return firstPass
        } else {
            missingPropertyCapture.missingProperties.each { String property ->
                st.add( property, "" )
            }
            return st.render()
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

    /**
     * Calculates a field with only a pidm as input
     * @param immutableId A CommunicationField immutable identifier
     * @param pidm a unique identifier for a person in Banner
     * @return
     */
    @Transactional(propagation=Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = Throwable.class )
    public String calculateFieldByPidmWithNewTransaction( String sqlStatement, Boolean returnsArrayArguments, String formatString, Long pidm, String mepCode=null ) {
        calculateFieldByPidm( sqlStatement, returnsArrayArguments, formatString, pidm, mepCode )
    }

    public String calculateFieldByPidm( String sqlStatement, Boolean returnsArrayArguments, String formatString, Long pidm, String mepCode=null ) {
        boolean returnsArray = returnsArrayArguments ?: false
        def sqlParams = [:]
        sqlParams << ['pidm': pidm]
        calculateField( sqlStatement, returnsArray, formatString, sqlParams, mepCode )
    }

    public String calculateFieldByMap( String sqlStatement, Boolean returnsArrayArguments, String formatString, Map sqlParams, String mepCode=null ) {
        boolean returnsArray = returnsArrayArguments ?: false
        calculateField( sqlStatement, returnsArray, formatString, sqlParams, mepCode )
    }

    /**
     * Executes a data function and returns result set
     * @param communicationFieldId The id of the communication field
     * @param parameters Map of parameter values
     * @return
     */
    private String calculateField( String sqlStatement, boolean returnsArrayArguments, String formatString, Map parameters, String mepCode=null ) {
        def attributeMap = [:]
        def Sql sql
        try {
            if (sqlStatement != null && sqlStatement.size() > 0) {
                // ToDo: decide if the upper bound should be configurable
                int maxRows = (!returnsArrayArguments) ? 1 : 50
                Connection conn = (Connection) sessionFactory.getCurrentSession().connection()
                asynchronousBannerAuthenticationSpoofer.setMepContext(conn, mepCode)
                sql = new Sql( (Connection) sessionFactory.getCurrentSession().connection() )
                List<GroovyRowResult> resultSet = sql.rows( sqlStatement, parameters, 0, maxRows )
                resultSet.each { row ->
                    row.each { column ->
                        String attributeName = column.getKey().toString().toLowerCase()
                        Object attributeValue = column.value
                        if (maxRows <= 1) {
                            attributeMap.put( attributeName, attributeValue )
                        } else {
                            // handle array of values per column name
                            ArrayList values = attributeMap.containsKey( attributeName ) ? (ArrayList) attributeMap.get( attributeName ) : new ArrayList()
                            values.add( attributeValue )
                            attributeMap.put( attributeName, values )
                        }
                    }
                }
            }

            return merge( formatString ?: "", attributeMap )
        } catch (SQLException e) {
            throw new ApplicationException( CommunicationFieldCalculationService, e.message )
        } catch (Exception e) {
            throw CommunicationExceptionFactory.createApplicationException(CommunicationFieldCalculationService.class, e, CommunicationErrorCode.INVALID_DATA_FIELD.name())
        } finally {
            sql?.close()
        }
    }

    private static ST newST( String templateString ) {
        char delimiter = '$'
        CommunicationFieldMissingPropertyCapture missingPropertyCapture = new CommunicationFieldMissingPropertyCapture()
        STGroup group = new STGroup( delimiter, delimiter )
        group.setListener( missingPropertyCapture )
        group.registerRenderer( Integer.class, new NumberRenderer() );
        group.registerRenderer( Date.class, new DateRenderer() );
        return new ST( group, templateString );
    }
}

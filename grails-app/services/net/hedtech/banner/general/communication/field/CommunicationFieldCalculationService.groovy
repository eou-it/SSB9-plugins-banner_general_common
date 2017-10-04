/********************************************************************************
  Copyright 2017 Ellucian Company L.P. and its affiliates.
********************************************************************************/
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
import net.hedtech.banner.DateUtility
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.groupsend.CommunicationParameterValue
import net.hedtech.banner.general.communication.merge.CommunicationFieldValue
import net.hedtech.banner.general.communication.parameter.CommunicationParameter
import net.hedtech.banner.general.communication.parameter.CommunicationParameterType
import net.hedtech.banner.general.utility.InformationText
import net.hedtech.banner.service.ServiceBase
import net.hedtech.banner.exceptions.ApplicationException
import org.apache.commons.lang.StringUtils
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.stringtemplate.v4.AttributeRenderer
import org.stringtemplate.v4.DateRenderer
import org.stringtemplate.v4.NumberRenderer
import org.stringtemplate.v4.ST
import org.stringtemplate.v4.STGroup

import java.sql.Connection
import java.sql.SQLException

class CommunicationFieldCalculationService extends ServiceBase {

    def asynchronousBannerAuthenticationSpoofer
    def testTemplate
    /**
     * Merges the data from the parameter map into the string template
     * @param stringTemplate A string containing delimited token fields
     * @param parameters Map of name value pairs representing tokens in the template and their values
     * @return A fully rendered String
     */
    public String merge( String stringTemplate, Map parameters ) {
        if (log.isDebugEnabled()) log.debug( "Merging parameters into field formatter string." );
        if (stringTemplate == null || stringTemplate.size() == 0) return ""

        if (parameters == null) parameters = [:]

        ST st = newST( stringTemplate );
        parameters.keySet().each { String key ->
            st.add( key, parameters[key] )
        }
        String firstPass = st.render()

        // Check if any missing parameters and if so replace them with an empty String.
        CommunicationFieldMissingPropertyCapture missingPropertyCapture = (CommunicationFieldMissingPropertyCapture) st.groupThatCreatedThisInstance.getListener()
        if (missingPropertyCapture.missingProperties.size() == 0) {
            return firstPass
        } else {
            // For test template methods. Should alert user that data field value is empty/not found
            if (testTemplate) {
                def str = ""
                missingPropertyCapture.missingProperties.each { String property ->
                    str += " " + property + ","
                }
                if (str.length() > 0)
                    str = str.getAt(0..(str.length() - 2)) // remove extra comma
                throw CommunicationExceptionFactory.createApplicationException(this.class, new RuntimeException(str), CommunicationErrorCode.MISSING_DATA_FIELD.name())
            }
            // TODO Check if this is actually the desired functionality. Possibly allow users to require datafield
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
    public Map calculateFieldsByPidmWithNewTransaction( List<String> fieldNames, Map parameterNameValueMap, Long pidm, String mepCode=null , testTemplate = false, Boolean escapeFieldValue=false ) {
        this.testTemplate = testTemplate
        Map fieldNameValueMap = [:]
        for (String fieldName :fieldNames) {
            CommunicationField communicationField = CommunicationField.fetchByName( fieldName )
            if (communicationField) {
                String value = calculateSingleFieldByPidm(communicationField, parameterNameValueMap, pidm, mepCode, escapeFieldValue)
                CommunicationFieldValue communicationFieldValue = new CommunicationFieldValue( value: value, renderAsHtml: communicationField.renderAsHtml )
                fieldNameValueMap.put( fieldName, communicationFieldValue )
            } else {
              // Will ignore any not found communication fields (field may have been renamed or deleted, will skip for now.
              // Will come back to this to figure out desired behavior.
                // TODO What is this error?
            }
        }

        return fieldNameValueMap
    }

    public String calculateSingleFieldByPidm( CommunicationField communicationField, Map parameterNameValueMap, Long pidm, String mepCode=null, Boolean escapeFieldValue=false  ) {

        String value = calculateFieldByPidm(
                communicationField.ruleContent,
                communicationField.returnsArrayArguments,
                communicationField.formatString,
                parameterNameValueMap,
                pidm,
                mepCode,
                escapeFieldValue
        )

        return value
    }

    public String calculateFieldByPidm( String sqlStatement, Boolean returnsArrayArguments, String formatString, Map parameterNameValueMap, Long pidm, String mepCode=null, Boolean escapeFieldValue=false  ) {
        boolean returnsArray = returnsArrayArguments ?: false
        def sqlParams = [:]
        if (sqlStatement?.contains(":pidm")) {
            sqlParams << ['pidm': pidm]
        }

        for (String name:parameterNameValueMap.keySet()) {
            if (sqlStatement?.contains( ":" + name)) {
                CommunicationParameterValue value = (CommunicationParameterValue) parameterNameValueMap.get(name)
                if (value.type == CommunicationParameterType.DATE) {
                    sqlParams.put(name, new java.sql.Date(((Date) value.value).time))
                } else {
                    sqlParams.put(name, value.value)
                }
            }
        }

        calculateField( sqlStatement, returnsArray, formatString, sqlParams, mepCode, escapeFieldValue )
    }

    /**
     * Executes a data function and returns result set
     * @param communicationFieldId The id of the communication field
     * @param nameValueMap Map of parameter values
     * @return
     */
    private String calculateField( String sqlStatement, boolean returnsArrayArguments, String formatString, Map parameterNameValueMap, String mepCode=null, Boolean escapeFieldValue=false ) {
        def attributeMap = [:]
        def Sql sql = null
        try {
            if (sqlStatement && sqlStatement.trim().size() > 0) {
                // ToDo: decide if the upper bound should be configurable
                int maxRows = (!returnsArrayArguments) ? 1 : 50
                Connection conn = (Connection) sessionFactory.getCurrentSession().connection()
                asynchronousBannerAuthenticationSpoofer.setMepContext(conn, mepCode)
                sql = new Sql( (Connection) sessionFactory.getCurrentSession().connection() )
                List<GroovyRowResult> resultSet
                if (parameterNameValueMap && parameterNameValueMap.size() > 0) {
                    resultSet = sql.rows( sqlStatement, parameterNameValueMap, 0, maxRows )
                } else {
                    resultSet = sql.rows( sqlStatement, 0, maxRows )
                }
                resultSet.each { row ->
                    row.each { column ->
                        String attributeName = column.getKey().toString().toLowerCase()
                        String attributeNameActual = column.getKey().toString()
                        def String[] fromstring = ["&", "<", "\"", "'", ">"]
                        def String[] tostring = ["&amp;", "&lt;", "&quot;", "&apos;", "&gt;"]
                        Object attributeValue
                        if (escapeFieldValue) {
                            attributeValue = StringUtils.replaceEach(column.value, fromstring, tostring)
                        } else {
                            attributeValue = column.value
                        }
                        if (maxRows <= 1) {
                            attributeMap.put( attributeName, attributeValue )
                            attributeMap.put( attributeNameActual, attributeValue)
                        } else {
                            // handle array of values per column name
                            ArrayList values = attributeMap.containsKey( attributeName ) ? (ArrayList) attributeMap.get( attributeName ) : new ArrayList()
                            values.add( attributeValue )
                            attributeMap.put( attributeName, values )
                            attributeMap.put( attributeNameActual, values)
                        }
                    }
                }
            }

            return merge( formatString ?: "", attributeMap )
        } catch (SQLException e) {
            if (e.getMessage()?.contains("ORA-06553")) {
                throw new ApplicationException(CommunicationFieldCalculationService.class, e)
            } else {
                throw CommunicationExceptionFactory.createApplicationException(CommunicationFieldCalculationService.class, e, CommunicationErrorCode.DATA_FIELD_SQL_ERROR.name())
            }
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
        NumberRenderer numberRenderer = new NumberRenderer()
        group.registerRenderer( Integer.class, numberRenderer );
        group.registerRenderer( Double.class, numberRenderer );
        group.registerRenderer( Long.class, numberRenderer );
        group.registerRenderer( Date.class, new AttributeRenderer() {
            @Override
            String toString(Object o, String s, Locale locale) {
                String dateString = DateUtility.formatDate( (Date) o, "MM-dd-yyyy" )
                return dateString
            }
        } )
        return new ST( group, templateString );
    }
}

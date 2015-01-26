/** *****************************************************************************
 © 2014 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package net.hedtech.banner.general.communication.template

import groovy.text.SimpleTemplateEngine
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldCalculationService
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.person.PersonUtility
import org.antlr.runtime.tree.CommonTree
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.stringtemplate.v4.ST
import org.stringtemplate.v4.STGroup

/**
 * To clarify some naming confusion, the term CommunicationTemplate refers to the Communication Manager object
 * that contains several string fields. Each of these string fields can contain delimited template variables.
 * The open source tool we use to merge the variables with the template parts is called StringTemplate (ST).
 * The individual String objects that can contain these delimited variables will therefore be called templateString(s).
 *
 */
class CommunicationTemplateMergeService {
    private Log log = LogFactory.getLog( this.getClass() )
    def communicationFieldCalculationService
    def dataFieldNames = []

    /**
     * Convenience method to fully render an email template. Looks up template, and pidm using banner id, and delegates to calculateTemplateByPidm
     * @param templateId A CommunicationEmailTemplate to evaluate
     * @param bannerId
     * @return
     */
    CommunicationMergedEmailTemplate calculateTemplateByBannerId( Long templateId, String bannerId ) {
        CommunicationEmailTemplate communicationTemplate = CommunicationEmailTemplate.get( templateId )
        if (communicationTemplate == null) {
            throw new ApplicationException( CommunicationTemplateMergeService, "@@r1:templateNotExist@@", templateId )
        }
        def person = PersonUtility.getPerson( bannerId )

        if (person == null) {
            throw new ApplicationException( CommunicationFieldCalculationService, "@@r1:bannerIdNotExist@@", bannerId )
        }
        CommunicationMergedEmailTemplate communicationMergedEmailTemplate
        communicationMergedEmailTemplate = calculateTemplateByPidm( communicationTemplate, person.pidm )
        communicationMergedEmailTemplate

    }

    /**
     * Convenience method to fully render a template. Assumed Pidm is the only input parameter. In the future, the input
     * will be a map or a RecipientData object so that other values can be bound as input to the data functions.
     * @param communicationTemplate A CommunicationEmailTemplate to evaluate
     * @param pidm
     * @return
     */
    CommunicationMergedEmailTemplate calculateTemplateByPidm( CommunicationEmailTemplate communicationTemplate, Long pidm ) {
        def sqlParams = [:]
        sqlParams << ['pidm': pidm]
        List<String> templateVariables = extractTemplateVariables( communicationTemplate.content )
        def recipientDataMap = calculateRecipientData( templateVariables, sqlParams )
        def CommunicationMergedEmailTemplate communicationMergedEmailTemplate= new CommunicationMergedEmailTemplate()
        communicationMergedEmailTemplate.content = renderTemplate( communicationTemplate.content, recipientDataMap )
        communicationMergedEmailTemplate
    }


    /**
     * Compute each communication field in the list and return the results. This data will become what is known
     * as recipient data.
     * @param communicationFieldNames List of communicationField names
     * @params Input parameters that will be bound to data functions of the communicationFields. Must contain at least a pidm or bannerId.
     * If it contains a pidm, the bannerId parameter will be ignored.
     * @return
     */
    Map<String, String> calculateRecipientData( List<String> communicationFieldNames, Map parameters ) {
        def recipientData = [:]
        def CommunicationField communicationField
        if (parameters.containsKey( 'pidm' )) {
            log.debug( "Calculating recipient data for pidm " + parameters.pidm)
            communicationFieldNames.each {
                communicationField = CommunicationField.findByName( it )
                if (!(communicationField == null)) {
                    def fieldResult = communicationFieldCalculationService.calculateFieldByPidm( communicationField.immutableId, parameters.getAt( 'pidm' ) )
                    recipientData[communicationField.name] = fieldResult
                }
            }
        } else {
            throw new ApplicationException( CommunicationTemplateMergeService, "@@r1:pidmIsRequired:@@" )
        }
        recipientData
    }

    /**
     * Returns a string containing the fully rendered template with all variables replaced with their data values
     * @param templateString The template
     * @param recipientData The map of rendered recipient data values to be merged into the template
     * @return Fully rendered template as String
     */
    def String renderTemplate( String templateString, Map recipientData ) {
        log.debug( "Merging recipient data into a template string")
        char delimiter = '$'
        ST st = new ST( templateString, delimiter, delimiter );
        recipientData.keySet().each { key ->
            st.add( key, recipientData[key] )
        }
        st.render()
    }

    /**
     * Merges each of the email specific template fields with the recipient data previously calculated
     *
     * @param template The template containing the tokens
     * @param data the map of values that will be substituted for each matching token
     * @return CommunicationMergedEmailTemplate
     */
    CommunicationMergedEmailTemplate mergeEmailTemplate( CommunicationEmailTemplate communicationEmailTemplate, CommunicationRecipientData recipientData ) {
        log.debug( "Merging recipient data into a CommunicationEmailTemplate ")

        CommunicationMergedEmailTemplate communicationMergedEmailTemplate = new CommunicationMergedEmailTemplate()
        communicationMergedEmailTemplate.toList = merge( communicationEmailTemplate.toList, recipientData.fieldValues )
        communicationMergedEmailTemplate.subject = merge( communicationEmailTemplate.subject, recipientData.fieldValues )
        communicationMergedEmailTemplate.content = merge( communicationEmailTemplate.content, recipientData.fieldValues )
        communicationMergedEmailTemplate
    }

    /**
     * Returns the preview values for all the fields in the template rather than executing the data function.
     * These serve in the place of recipient data when a preview is requested
     * @param templateString
     * @return
     */
    Map<String, String> renderPreviewValues( String templateString ) {
        log.debug( "Extracting preview values from template string.")
        List<String> templateVariables = extractTemplateVariables( templateString )
        def renderedCommunicationFields = [:]

        if (templateVariables.size() > 0) {

            templateVariables.each {
                CommunicationField communicationField = CommunicationField.findByName( it )
                if (communicationField) {
                    renderedCommunicationFields.put( communicationField.name, communicationField.previewValue )
                    //throw new ApplicationException( CommunicationTemplateMergeService, "@@r1:invalidDataField@@", it )
                } else {
                    renderedCommunicationFields.put( it, null )
                }
            }
        }
        renderedCommunicationFields
    }

    /**
     * Returns a CommunicationMergedEmailTemplate containing the template content with all data fields replaced with their preview value
     * @param CommunicationEmailTemplate
     * @return CommunicationMergedEmailTemplate
     */
    CommunicationMergedEmailTemplate renderPreviewTemplate( CommunicationEmailTemplate communicationEmailTemplate ) {
        log.debug( "Rendering CommunicationTemplate with preview values only.")
        CommunicationMergedEmailTemplate communicationMergedEmailTemplate = new CommunicationMergedEmailTemplate()
        communicationMergedEmailTemplate.toList = merge( communicationEmailTemplate.toList?:" ", renderPreviewValues( communicationEmailTemplate.toList?:" " ) )
        communicationMergedEmailTemplate.subject = merge( communicationEmailTemplate.subject?:" ", renderPreviewValues( communicationEmailTemplate.subject?:" " ) )
        communicationMergedEmailTemplate.content = merge( communicationEmailTemplate.content?:" ", renderPreviewValues( communicationEmailTemplate.content?:" " ) )
        communicationMergedEmailTemplate
    }

    /**
     * Merges the data from the parameter map into the string template
     * @param stringTemplate A stcring containing delimited token fields
     * @param parameters Map of name value pairs representing tokens in the template and their values
     * @return A fully rendered String
     */
    String merge( String stringTemplate, Map<String, String> parameters ) {
        if (!(stringTemplate == null || parameters == null)) {
            char delimiter = '$'
            ST st = new ST( stringTemplate, delimiter, delimiter );
            parameters.keySet().each { key ->
                st.add( key, parameters[key] )
            }
            st.render()
        } else {
            // You have nothing to do, so just return the input templateString
            stringTemplate
        }
    }
    /**
     * Extracts all the template variables from the currently supported parts of an email template
     * @param communicationEmailTemplate
     * @return
     */

    List<String> extractTemplateVariables( CommunicationEmailTemplate communicationEmailTemplate ) {
        log.debug( "Extracting template variables from CommunicationEmailTemplate ${communicationEmailTemplate.name}.")
        def templateVariables = []
        extractTemplateVariables( communicationEmailTemplate.toList ).each {
            templateVariables << it
        }
        extractTemplateVariables( communicationEmailTemplate.subject ).each {
            templateVariables << it
        }
        extractTemplateVariables( communicationEmailTemplate.content ).each {
            templateVariables << it
        }

        templateVariables
    }

    /**
     *  Extracts all delimited parameter strings. Currently only supports $foo$, not $foo.bar$
     * @param template String
     * @return set of unique string variables found in the template string
     */
    List<String> extractTemplateVariables( String templateString ) {
        log.debug( "Extracting template variables from template string.")
        if (templateString == null) return new ArrayList<String>()

        dataFieldNames = []
        char delimiter = '$'
        /* TODO: get a listener working so  you can trap rendering errors
        STGroup group = new STGroup( delimiter, delimiter )
        CommunicationStringTemplateErrorListener errorListener = new CommunicationStringTemplateErrorListener()
        group.setListener( errorListener )
        group.defineTemplate( "foo", templateString ) */
        ST st = new org.stringtemplate.v4.ST( templateString, delimiter, delimiter );

        /* Each chunk of the ast is returned by getChildren, then examineNodes recursively walks
        down the branch looking for ID tokens to place in the global dataFieldNames */

        st.impl.ast.getChildren().each {
            if (it != null) {
                CommonTree child = it as CommonTree
                examineNodes( child )
            }
        }

        dataFieldNames.unique( false )

    }
    /**
     * Walks the ast tree and finds any tokens with the ID attribute
     * @param treeNode
     * @return
     */
    def examineNodes( CommonTree treeNode ) {
        final int ID = 25
        if (treeNode) {
            //println "token type = " + treeNode.getToken().getType() + " text= " + treeNode.getToken().getText()
            if (treeNode.getToken().getType() == ID) {
                //println "ChildIndex= " + treeNode.childIndex + "string= " + treeNode.toStringTree()
                dataFieldNames.add( treeNode.toString() )
            }
            treeNode.getChildren().each {
                CommonTree nextNode = it as CommonTree
                examineNodes( nextNode )
            }
        }
    }


    def String processGroovyTemplate( String templateString, bindings ) {
        def engine = new SimpleTemplateEngine()
        def template = engine.createTemplate( templateString ).make( bindings )
        template
    }


}

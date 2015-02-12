/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.template

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldCalculationService
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.person.PersonUtility
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.stringtemplate.v4.NumberRenderer
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

    /**
     * Convenience method to fully render an email template. Looks up template, and pidm using banner id, and delegates to calculateTemplateByPidm
     * @param templateId A CommunicationEmailTemplate to evaluate
     * @param bannerId
     * @return
     */
    CommunicationMergedEmailTemplate calculateTemplateByBannerId( Long templateId, String bannerId ) {
        CommunicationEmailTemplate communicationTemplate = (CommunicationEmailTemplate) CommunicationEmailTemplate.get( templateId )
        if (communicationTemplate == null) {
            throw new ApplicationException( CommunicationTemplateMergeService, "@@r1:templateNotExist:" + templateId + "@@")
        }
        def person = PersonUtility.getPerson( bannerId )

        if (person == null) {
            throw new ApplicationException( CommunicationFieldCalculationService, "@@r1:bannerIdNotExist:" + bannerId + "@@" )
        }
        CommunicationMergedEmailTemplate communicationMergedEmailTemplate
        communicationMergedEmailTemplate = calculateTemplateByPidm( communicationTemplate, (Long) person.pidm )
        return communicationMergedEmailTemplate
    }

    /**
     * Convenience method to fully render a template. Assumed Pidm is the only input parameter. In the future, the input
     * will be a map or a RecipientData object so that other values can be bound as input to the data functions.
     * @param communicationTemplate A CommunicationEmailTemplate to evaluate
     * @param pidm
     * @return
     */
    CommunicationMergedEmailTemplate calculateTemplateByPidm( CommunicationEmailTemplate communicationTemplate, Long pidm ) {
        if (log.isDebugEnabled())
            log.debug( "Calculating template by pidm." )
        def sqlParams = [:]
        sqlParams << ['pidm': pidm]
        List<String> templateVariables = extractTemplateVariables( communicationTemplate.content )
        def recipientDataMap = calculateRecipientData( templateVariables, sqlParams )
        def CommunicationMergedEmailTemplate communicationMergedEmailTemplate = new CommunicationMergedEmailTemplate()
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
        if (log.isDebugEnabled())
            log.debug( "Calculating recipient data." )
        def recipientData = [:]
        def CommunicationField communicationField
        if (parameters.containsKey( 'pidm' )) {
            Long pidm = (Long) parameters.get( 'pidm' )
            log.debug( "Calculating recipient data for pidm ${pidm}." )
            communicationFieldNames.each {
                communicationField = CommunicationField.findByName( it )
                if (!(communicationField == null)) {
                    def fieldResult = communicationFieldCalculationService.calculateFieldByPidm(
                        communicationField.getRuleContent(),
                        communicationField.returnsArrayArguments,
                        communicationField.getFormatString(),
                        pidm
                    )
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
        if (log.isDebugEnabled())
            log.debug( "Merging recipient data into a template string" )
        ST st = newST( templateString );
        recipientData.keySet().each { key ->
            st.add( (String) key, recipientData[key] )
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
        if (log.isDebugEnabled())
            log.debug( "Merging recipient data into a CommunicationEmailTemplate " )

        CommunicationMergedEmailTemplate communicationMergedEmailTemplate = new CommunicationMergedEmailTemplate()
        communicationMergedEmailTemplate.toList = merge( communicationEmailTemplate.toList, recipientData.fieldValues )
        communicationMergedEmailTemplate.subject = merge( communicationEmailTemplate.subject, recipientData.fieldValues )
        communicationMergedEmailTemplate.content = merge( communicationEmailTemplate.content, recipientData.fieldValues )
        communicationMergedEmailTemplate
    }

    /**
     * Returns the preview values for all the fields in the template rather than executing the data function.
     * These serve in the place of recipient data when a preview is requested.
     * If the variable is not a valid communicationField, it returns the just the name of the variable
     * @param templateString
     * @return
     */
    Map<String, String> renderPreviewValues( String templateString ) {
        if (log.isDebugEnabled())
            log.debug( "Extracting preview values from template string." )
        List<String> templateVariables = extractTemplateVariables( templateString )
        def renderedCommunicationFields = [:]

        if (templateVariables.size() > 0) {
            templateVariables.each { fieldName ->
                CommunicationField communicationField = CommunicationField.findByName( fieldName )
                String previewString = (communicationField && communicationField.previewValue && communicationField.previewValue.size() > 0) ? communicationField.previewValue : "\$${fieldName}\$"
                renderedCommunicationFields.put( fieldName, previewString )
            }
        }
        return renderedCommunicationFields
    }

    /**
     * Returns a CommunicationMergedEmailTemplate containing the template content with all data fields replaced with their preview value
     * @param CommunicationEmailTemplate
     * @return CommunicationMergedEmailTemplate
     */
    CommunicationMergedEmailTemplate renderPreviewTemplate( CommunicationEmailTemplate communicationEmailTemplate ) {
        if (log.isDebugEnabled()) log.debug( "Rendering CommunicationTemplate with preview values only." )
        CommunicationMergedEmailTemplate communicationMergedEmailTemplate = new CommunicationMergedEmailTemplate()
        communicationMergedEmailTemplate.toList = merge( communicationEmailTemplate.toList ?: "", renderPreviewValues( communicationEmailTemplate.toList ?: "" ) )
        communicationMergedEmailTemplate.subject = merge( communicationEmailTemplate.subject ?: "", renderPreviewValues( communicationEmailTemplate.subject ?: "" ) )
        communicationMergedEmailTemplate.content = merge( communicationEmailTemplate.content ?: "", renderPreviewValues( communicationEmailTemplate.content ?: "" ) )
        communicationMergedEmailTemplate
    }

    /**
     * Merges the data from the parameter map into the string template
     * @param stringTemplate A stcring containing delimited token fields
     * @param parameters Map of name value pairs representing tokens in the template and their values
     * @return A fully rendered String
     */
    String merge( String stringTemplate, Map<String, Object> parameters ) {
        if (log.isDebugEnabled()) log.debug( "Merging parameters into template string." );
        if (stringTemplate && parameters) {
            ST st = newST( stringTemplate );
            parameters.keySet().each { key ->
                Object o = parameters[key]
                st.add( key, o ?: "" )
            }
            return st.render()
        } else {
            // You have nothing to do, so just return the input templateString
            return stringTemplate
        }
    }


/**
 * Extracts all the template variables from the currently supported parts of an email template
 * @param communicationEmailTemplate
 * @return
 */

    List<String> extractTemplateVariables( CommunicationEmailTemplate communicationEmailTemplate ) {
        if (log.isDebugEnabled())
            log.debug( "Extracting template variables from CommunicationEmailTemplate ${communicationEmailTemplate.name}." )
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
     *  This will throw an application exception if the template string cannot be parsed.
     * @param template String
     * @return set of unique string variables found in the template string
     */
    List<String> extractTemplateVariables( String templateString ) {
        if (log.isDebugEnabled()) log.debug( "Extracting template variables from template string." )

        if (templateString == null) {
            return new ArrayList<String>()
        } else {
            ST st = newST( templateString )
            st.render()
            CommunicationTemplateMissingPropertyCapture missingPropertyCapture = (CommunicationTemplateMissingPropertyCapture) st.groupThatCreatedThisInstance.getListener()
            return missingPropertyCapture.missingProperties.toList()
        }
    }

    def ST newST( String templateString ) {
        char delimiter = '$'
        CommunicationTemplateMissingPropertyCapture missingPropertyCapture = new CommunicationTemplateMissingPropertyCapture()
        STGroup group = new STGroup( delimiter, delimiter )
        group.setListener( missingPropertyCapture )
        group.registerRenderer( Integer.class, new NumberRenderer() );
        return new ST( group, templateString );
    }
}


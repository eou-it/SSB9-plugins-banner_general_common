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
import net.hedtech.banner.general.person.PersonUtility
import org.stringtemplate.v4.ST

class CommunicationTemplateMergeService {
    def communicationFieldService
    def communicationFieldCalculationService
    def communicationTemplateService


    String calculateTemplateByBannerId( Long templateId, String bannerId ) {
        CommunicationEmailTemplate emailTemplate = CommunicationEmailTemplate.get( templateId )
        if (emailTemplate == null) {
            throw new ApplicationException( CommunicationTemplateMergeService, "@@r1:templateNotExist@@", templateId )
        }
        def person = PersonUtility.getPerson( bannerId )

        if (person == null) {
            throw new ApplicationException( CommunicationFieldCalculationService, "@@r1:bannerIdNotExist@@", bannerId )
        }

        calculateTemplateByPidm( emailTemplate, person.pidm )
    }

    /**
     * Convenience method to fully render a template. Assumed Pidm is the only input parameter. In the future, the input
     * will be a map so that other values can be bound as input to the data functions.
     * @param emailTemplate A CommunicationEmailTemplate to evaluate
     * @param pidm
     * @return
     */
    String calculateTemplateByPidm( CommunicationEmailTemplate emailTemplate, Long pidm ) {
        def sqlParams = [:]
        sqlParams << ['pidm': pidm]
        List<String> templateVariables = communicationTemplateService.extractTemplateVariables( emailTemplate.content )
        def recipientData = calculateRecipientData( templateVariables, sqlParams )
        renderTemplate( emailTemplate.content, recipientData )
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
        char delimiter = '$'
        ST st = new ST( templateString, delimiter, delimiter );
        recipientData.keySet().each { key ->
            st.add( key, recipientData[key] )
        }
        st.render()
    }

/**
 * Returns a string containing the template content with all data fields replaced with their preview value
 * @param templateString
 * @return
 */
    def String renderPreviewTemplate( String templateString ) {
        def templateVariables = communicationTemplateService.extractTemplateVariables( templateString )
        def renderedCommunicationFields = [:]
        char delimiter = '$'
        templateVariables.each { dataFieldName ->
            CommunicationField communicationField = CommunicationField.findByName( dataFieldName )
            if (communicationField) {
                renderedCommunicationFields.put( communicationField.name, communicationField.previewValue )
                //throw new ApplicationException( CommunicationTemplateMergeService, "@@r1:invalidDataField@@", dataFieldName )
            } else {
                renderedCommunicationFields.put( dataFieldName, null )
            }
        }
        ST st = new ST( templateString, delimiter, delimiter );
        renderedCommunicationFields.keySet().each { key ->
            st.add( key, renderedCommunicationFields[key] )
        }
        st.render()
    }


    def String processGroovyTemplate( String templateString, bindings ) {
        def engine = new SimpleTemplateEngine()
        def template = engine.createTemplate( templateString ).make( bindings )

    }


}

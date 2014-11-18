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


    String calculateTemplateByBannerId( String immutableId, String bannerId ) {

        def person = PersonUtility.getPerson( bannerId )

        if (person == null) {
            throw new ApplicationException( CommunicationFieldCalculationService, "Person is null" ) // TODO: I18N these
        }
        if (immutableId == null) {
            throw new ApplicationException( CommunicationFieldCalculationService, "ImmutableId is null" )
        }

        calculateFieldByPidm( immutableId, person.pidm )
    }


    String calculateTemplateByPidm( String immutableId, Long pidm ) {
        def sqlParams = [:]
        sqlParams << ['pidm': pidm]
        calculateField( immutableId, sqlParams )
    }


    def String renderPreviewTemplate( String templateString ) {
        def templateVariables = communicationTemplateService.extractTemplateVariables( templateString )
        def renderedCommunicationFields = [:]
        char delimiter = '$'
        templateVariables.each { dataFieldName ->
            CommunicationField communicationField = CommunicationField.findByName( dataFieldName )
            if (communicationField == null) {
                throw new ApplicationException( CommunicationTemplateMergeService, "@@r1:invalidDataField@@", dataFieldName )
            }
            renderedCommunicationFields.put(communicationField.name, communicationField.previewValue)

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

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

class CommunicationTemplateMergeService {
    def communicationFieldService
    def communicationFieldCalculationService
    def communicationTemplateService

    String calculateTempalteByBannerId( String immutableId, String bannerId ) {

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


    private String processTemplate( String templateString ) {
        def templateVariables = communicationTemplateService.extractTemplateVariables( templateString )
        templateVariables.each { dataFieldName ->
            def communicationField = CommunicationField.findByName( dataFieldName )
            if (communicationField == null) {
                throw new ApplicationException( CommunicationTemplateMergeService, "@@r1:invalidDataField@@", dataFieldName )
            }
            String fieldValue = communicationFieldCalculationService.calculateFieldByPidm( immutableId, pidm ) {
            }
        }
    }


    def String processGroovyTemplate( String templateString, bindings ) {
        def engine = new SimpleTemplateEngine()
        def template = engine.createTemplate( templateString ).make( bindings )

    }
}

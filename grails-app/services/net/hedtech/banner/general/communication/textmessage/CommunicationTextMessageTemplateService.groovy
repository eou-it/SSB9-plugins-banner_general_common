/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.textmessage

import grails.gorm.transactions.Transactional
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldStatus
import net.hedtech.banner.general.communication.letter.CommunicationLetterTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplateMergeService
import net.hedtech.banner.general.communication.template.CommunicationTemplateService

@Transactional
class CommunicationTextMessageTemplateService extends CommunicationTemplateService {

    @Override
    void validatePublished(CommunicationTemplate template ) {
        CommunicationTextMessageTemplate textMessageTemplate = (CommunicationTextMessageTemplate) template;

        if (!textMessageTemplate.toList || textMessageTemplate.toList.trim().size() == 0 ) {
            //TODO - new error messages
            throw CommunicationExceptionFactory.createApplicationException( CommunicationEmailTemplate.class, "toFieldRequiredToPublish" )
        }

        if (isEmpty( textMessageTemplate.message )) {
            //TODO - new error messages
            throw CommunicationExceptionFactory.createApplicationException( CommunicationLetterTemplate.class, "contentRequiredToPublish" )
        }

        if (isEmpty( textMessageTemplate.destinationLink ) && !isEmpty( textMessageTemplate.destinationLabel )) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationTextMessageTemplate.class, "destinationFieldsRequiredToPublish" )
        }

        if (!isEmpty( textMessageTemplate.destinationLink ) && isEmpty( textMessageTemplate.destinationLabel )) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationTextMessageTemplate.class, "destinationFieldsRequiredToPublish" )
        }

        try {
            List<String> fieldNameList = communicationTemplateMergeService.extractTemplateVariables( textMessageTemplate )
            if (fieldNameList) {
                fieldNameList.each { String fieldName ->
                    CommunicationField field = CommunicationField.fetchByName( fieldName )
                    if (!field) {
                        throw new ApplicationException(CommunicationTemplateMergeService, "@@r1:invalidDataField:${fieldName}@@")
                    }
                    if (field.status == CommunicationFieldStatus.DEVELOPMENT) {
                        throw new ApplicationException(CommunicationTemplateMergeService, "@@r1:invalidDataField:${fieldName}@@")
                    }
                }
            }
        } catch(Exception e) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationTemplateService.class, "parseSyntaxError" )
        }
    }

    private boolean isEmpty( String s ) {
        return (!s || s.trim().size() == 0)
    }
}
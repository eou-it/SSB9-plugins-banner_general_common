/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.textmessage

import grails.gorm.transactions.Transactional
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldStatus
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplateMergeService
import net.hedtech.banner.general.communication.template.CommunicationTemplateService

@Transactional
class CommunicationTextMessageTemplateService extends CommunicationTemplateService {

    @Override
    void validatePublished(CommunicationTemplate template ) {
        CommunicationTextMessageTemplate textMessageTemplate = (CommunicationTextMessageTemplate) template;

        if (isEmpty( textMessageTemplate.message )) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationTextMessageTemplate.class, "testMessageFieldRequiredToPublish" )
        }

        if (isEmpty( textMessageTemplate.destinationLink ) && !isEmpty( textMessageTemplate.destinationLabel )) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationTextMessageTemplate.class, "destinationFieldsRequiredToPublish" )
        }

        if (!isEmpty( textMessageTemplate.destinationLink ) && isEmpty( textMessageTemplate.destinationLabel )) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationTextMessageTemplate.class, "destinationFieldsRequiredToPublish" )
        }

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
    }

    private boolean isEmpty( String s ) {
        return (!s || s.trim().size() == 0)
    }
}
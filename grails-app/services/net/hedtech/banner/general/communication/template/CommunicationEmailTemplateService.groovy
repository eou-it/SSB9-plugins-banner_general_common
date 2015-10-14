/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.template

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldStatus


class CommunicationEmailTemplateService extends CommunicationTemplateService {

    @Override
    protected void validatePublished( CommunicationTemplate template ) {
        if (!template.toList || template.toList.trim().size() == 0 ) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationEmailTemplate.class, "toFieldRequiredToPublish" )
        }
        if (!template.subject || template.subject.size() == 0 ) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationEmailTemplate.class, "subjectFieldRequiredToPublish" )
        }

        List<String> fieldNameList = communicationTemplateMergeService.extractTemplateVariables(template)
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

}

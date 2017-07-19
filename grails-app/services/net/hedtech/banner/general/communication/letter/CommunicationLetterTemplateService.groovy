/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.letter

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldStatus
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplateMergeService
import net.hedtech.banner.general.communication.template.CommunicationTemplateService


class CommunicationLetterTemplateService extends CommunicationTemplateService {

    @Override
    void validatePublished( CommunicationTemplate template) {
        assert( template != null )
        CommunicationLetterTemplate letterTemplate = (CommunicationLetterTemplate) template
        if (!letterTemplate.toAddress || letterTemplate.toAddress.trim().size() == 0 ) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationLetterTemplate.class, "toAddressRequiredToPublish" )
        }
        if (!letterTemplate.content || letterTemplate.content.size() == 0 ) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationLetterTemplate.class, "contentRequiredToPublish" )
        }

        List<String> fieldNameList = communicationTemplateMergeService.extractTemplateVariables(letterTemplate)
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


    @Override
    protected void validateTemplate( CommunicationTemplate template ) {
        super.validateTemplate( template )
        CommunicationLetterTemplate letterTemplate = (CommunicationLetterTemplate) template
        if (letterTemplate.style) {
            CommunicationLetterPageSettings letterPageSettings = new CommunicationLetterPageSettings()
            letterPageSettings.setStyle( letterTemplate.style )
            letterPageSettings.validate()
        }
    }

}

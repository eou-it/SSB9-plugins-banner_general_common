/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.template

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldStatus

class CommunicationMobileNotificationTemplateService extends CommunicationTemplateService {

    @Override
    protected void validatePublished( CommunicationTemplate template ) {
        CommunicationMobileNotificationTemplate mobileNotificationTemplate = (CommunicationMobileNotificationTemplate) template;

        if (isEmpty( mobileNotificationTemplate.mobileHeadline )) {
            throw new ApplicationException(CommunicationTemplate, "@@r1:template.cannotBePublished@@")
        }

        if (isEmpty( mobileNotificationTemplate.destinationLink ) && !isEmpty( mobileNotificationTemplate.destinationLabel )) {
            throw new ApplicationException(CommunicationTemplate, "@@r1:template.cannotBePublished@@")
        }

        if (!isEmpty( mobileNotificationTemplate.destinationLink ) && isEmpty( mobileNotificationTemplate.destinationLabel )) {
            throw new ApplicationException(CommunicationTemplate, "@@r1:template.cannotBePublished@@")
        }

        switch(mobileNotificationTemplate.expirationPolicy) {
            case CommunicationMobileNotificationExpirationPolicy.NO_EXPIRATION:
                if (mobileNotificationTemplate.sticky) {
                    throw new ApplicationException(CommunicationMobileNotificationTemplate, "@@r1:expirationRequiredForSticky@@" )
                }
                break
            case CommunicationMobileNotificationExpirationPolicy.DURATION:
                if (!mobileNotificationTemplate.duration) {
                    throw new ApplicationException(CommunicationTemplate, "@@r1:template.cannotBePublished@@")
                }
                break
            case CommunicationMobileNotificationExpirationPolicy.DATE_TIME:
                if (!mobileNotificationTemplate.expirationDateTime) {
                    throw new ApplicationException(CommunicationTemplate, "@@r1:template.cannotBePublished@@")
                }
                break
        }

        List<String> fieldNameList = communicationTemplateMergeService.extractTemplateVariables( mobileNotificationTemplate )
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

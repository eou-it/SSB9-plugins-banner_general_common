/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.mobile

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldStatus
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationExpirationPolicy
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplateMergeService
import net.hedtech.banner.general.communication.template.CommunicationTemplateService

class CommunicationMobileNotificationTemplateService extends CommunicationTemplateService {

    @Override
    void validatePublished( CommunicationTemplate template ) {
        CommunicationMobileNotificationTemplate mobileNotificationTemplate = (CommunicationMobileNotificationTemplate) template;

        if (isEmpty( mobileNotificationTemplate.mobileHeadline )) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationMobileNotificationTemplate.class, "mobileHeadlineFieldRequiredToPublish" )
        }

        if (isEmpty( mobileNotificationTemplate.destinationLink ) && !isEmpty( mobileNotificationTemplate.destinationLabel )) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationMobileNotificationTemplate.class, "destinationFieldsRequiredToPublish" )
        }

        if (!isEmpty( mobileNotificationTemplate.destinationLink ) && isEmpty( mobileNotificationTemplate.destinationLabel )) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationMobileNotificationTemplate.class, "destinationFieldsRequiredToPublish" )
        }

        switch(mobileNotificationTemplate.expirationPolicy) {
            case CommunicationMobileNotificationExpirationPolicy.NO_EXPIRATION:
                if (mobileNotificationTemplate.sticky) {
                    throw CommunicationExceptionFactory.createApplicationException( CommunicationMobileNotificationTemplate.class, "expirationRequiredForSticky" )
                }
                break
            case CommunicationMobileNotificationExpirationPolicy.DURATION:
                if (!mobileNotificationTemplate.duration) {
                    throw CommunicationExceptionFactory.createApplicationException( CommunicationMobileNotificationTemplate.class, "durationRequiredToPublish" )
                }
                break
            case CommunicationMobileNotificationExpirationPolicy.DATE_TIME:
                if (!mobileNotificationTemplate.expirationDateTime) {
                    throw CommunicationExceptionFactory.createApplicationException( CommunicationMobileNotificationTemplate.class, "expirationDateRequiredToPublish" )
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

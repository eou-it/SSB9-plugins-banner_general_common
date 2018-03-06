/*********************************************************************************
 Copyright 2015-2017 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.merge

import net.hedtech.banner.general.communication.field.CommunicationFieldCalculationService
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendItem
import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.letter.CommunicationLetterTemplate
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplateMergeService
import net.hedtech.banner.general.communication.template.CommunicationTemplateVisitor
import org.apache.log4j.Logger

/**
 * Creates a recipient data object.
 */
class CommunicationRecipientDataFactory implements CommunicationTemplateVisitor {

    private static final log = Logger.getLogger(CommunicationRecipientDataFactory.class)

    CommunicationTemplateMergeService communicationTemplateMergeService
    CommunicationFieldCalculationService communicationFieldCalculationService
    CommunicationRecipientDataService communicationRecipientDataService
    def asynchronousBannerAuthenticationSpoofer

    CommunicationRecipientData recipientData
    private CommunicationGroupSendItem groupSendItem

    public CommunicationRecipientData create( CommunicationGroupSendItem groupSendItem ) {
        assert( communicationTemplateMergeService )
        assert( communicationFieldCalculationService )
        assert( communicationRecipientDataService )
        assert( asynchronousBannerAuthenticationSpoofer )

        assert( groupSendItem )
        assert( groupSendItem.communicationGroupSend )

        this.groupSendItem = groupSendItem
        recipientData = null

        CommunicationTemplate thisTemplate = CommunicationTemplate.fetchByIdAndMepCode(groupSendItem.communicationGroupSend.templateId, groupSendItem.mepCode)
        thisTemplate.accept(this)

        recipientData = (CommunicationRecipientData) communicationRecipientDataService.create( recipientData )
    }

    void visitEmail(CommunicationEmailTemplate template) {
        // Can this list be cached somewhere for similar processing on the same template but different user
        List<String> fieldNames = communicationTemplateMergeService.extractTemplateVariables(template.toList?.toString())
        communicationTemplateMergeService.extractTemplateVariables(template.subject?.toString()).each {
            fieldNames << it
        }
        communicationTemplateMergeService.extractTemplateVariables(template.content?.toString()).each {
            fieldNames << it
        }
        fieldNames = fieldNames.unique()
        recipientData = createCommunicationRecipientData( template, fieldNames )
    }

    void visitLetter(CommunicationLetterTemplate template) {
        // Can this list be cached somewhere for similar processing on the same template but different user
        List<String> fieldNames = communicationTemplateMergeService.extractTemplateVariables(template.toAddress?.toString())
        communicationTemplateMergeService.extractTemplateVariables(template.content?.toString()).each {
            fieldNames << it
        }
        fieldNames = fieldNames.unique()
        recipientData = createCommunicationRecipientData( template, fieldNames, true  )
    }

    void visitMobileNotification(CommunicationMobileNotificationTemplate template) {
        // Can this list be cached somewhere for similar processing on the same template but different user
        List<String> fieldNames = new ArrayList<String>()
        communicationTemplateMergeService.extractTemplateVariables(template.mobileHeadline?.toString()).each {
            fieldNames << it
        }
        communicationTemplateMergeService.extractTemplateVariables(template.headline?.toString()).each {
            fieldNames << it
        }
        communicationTemplateMergeService.extractTemplateVariables(template.messageDescription?.toString()).each {
            fieldNames << it
        }
        communicationTemplateMergeService.extractTemplateVariables(template.destinationLink?.toString()).each {
            fieldNames << it
        }
        communicationTemplateMergeService.extractTemplateVariables(template.destinationLabel?.toString()).each {
            fieldNames << it
        }
        fieldNames = fieldNames.unique()
        recipientData = createCommunicationRecipientData( template, fieldNames )
    }

    private CommunicationRecipientData createCommunicationRecipientData( CommunicationTemplate template, List<String> fieldNames, Boolean escapeFieldValue=false  ) {
        Map fieldNameValueMap = calculateFieldsForUser( fieldNames, escapeFieldValue )

        new CommunicationRecipientData(
            pidm: groupSendItem.recipientPidm,
            templateId: template.id,
            referenceId: groupSendItem.referenceId,
            ownerId: groupSendItem.communicationGroupSend.createdBy,
            fieldValues: fieldNameValueMap,
            organizationId: groupSendItem.communicationGroupSend.organizationId,
            communicationChannel: template.communicationChannel
        )

    }

    private Map calculateFieldsForUser( List<String> fieldNames, Boolean escapeFieldValue=false  ) {
        def originalMap = null
        try {
            originalMap = asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecuteAndSave( groupSendItem.communicationGroupSend.createdBy, groupSendItem.communicationGroupSend.mepCode)
            // This method will start a nested transaction (see REQUIRES_NEW annotation) and
            // consequently pick up a new db connection with the current oracle user name.
            return communicationFieldCalculationService.calculateFieldsByPidmWithNewTransaction(
                fieldNames,
                this.groupSendItem.communicationGroupSend.getParameterNameValueMap(),
                this.groupSendItem.recipientPidm,
                this.groupSendItem.communicationGroupSend.mepCode,
                false,
                escapeFieldValue
            )
        } finally {
            if (originalMap) {
                asynchronousBannerAuthenticationSpoofer.resetAuthAndFormContext( originalMap )
            }
        }
    }
}

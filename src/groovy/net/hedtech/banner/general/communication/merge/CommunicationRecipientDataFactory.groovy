/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.merge

import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendItem
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.template.CommunicationMobileNotificationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplateVisitor
import org.apache.log4j.Logger

/**
 * Creates a recipient data object.
 */
class CommunicationRecipientDataFactory implements CommunicationTemplateVisitor {
    def log = Logger.getLogger(this.getClass())

    def communicationTemplateMergeService
    def communicationFieldCalculationService
    def communicationRecipientDataService
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
        groupSendItem.communicationGroupSend.template.accept( this )
        recipientData = communicationRecipientDataService.create( recipientData )
    }

    void visitEmail(CommunicationEmailTemplate template) {
        // Can this list be cached somewhere for similar processing on the same template but different user
        List<String> fieldNames = communicationTemplateMergeService.extractTemplateVariables(template.toList?.toString())
        communicationTemplateMergeService.extractTemplateVariables(template.subject?.toString()).each {
            fieldNames << it
        }
        communicationTemplateMergeService.extractTemplateVariables(template.subject?.toString()).each {
            fieldNames << it
        }
        communicationTemplateMergeService.extractTemplateVariables(template.content?.toString()).each {
            fieldNames << it
        }
        fieldNames = fieldNames.unique()
        recipientData = createCommunicationRecipientData( template, fieldNames )
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

    private CommunicationRecipientData createCommunicationRecipientData( CommunicationTemplate template, List<String> fieldNames ) {
        def nameToValueMap = [:]
        fieldNames.each { fieldName ->
            CommunicationField communicationField = CommunicationField.fetchByName(fieldName)
            // Will ignore any not found communication fields (field may have been renamed or deleted, will skip for now.
            // Will come back to this to figure out desired behavior.
            if (communicationField) {
                String value = calculateFieldForUser( communicationField )
                CommunicationFieldValue communicationFieldValue = new CommunicationFieldValue(
                    value: value,
                    renderAsHtml: communicationField.renderAsHtml
                )
                nameToValueMap.put(communicationField.name, communicationFieldValue)
            }
        }

        new CommunicationRecipientData(
            pidm: groupSendItem.recipientPidm,
            templateId: groupSendItem.communicationGroupSend.template.id,
            referenceId: groupSendItem.referenceId,
            ownerId: groupSendItem.communicationGroupSend.createdBy,
            fieldValues: nameToValueMap,
            organization: groupSendItem.communicationGroupSend.organization,
            communicationChannel: groupSendItem.communicationGroupSend.template.communicationChannel
        )
    }

    private String calculateFieldForUser( CommunicationField communicationField ) {
        def originalMap = null
        try {
            originalMap = asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecuteAndSave( groupSendItem.communicationGroupSend.createdBy, groupSendItem.communicationGroupSend.mepCode)
            // This method will start a nested transaction (see REQUIRES_NEW annotation) and
            // consequently pick up a new db connection with the current oracle user name.
            return communicationFieldCalculationService.calculateFieldByPidmWithNewTransaction(
                communicationField.getRuleContent(),
                communicationField.returnsArrayArguments,
                communicationField.getFormatString(),
                this.groupSendItem.recipientPidm,
                this.groupSendItem.communicationGroupSend.mepCode
            )
        } finally {
            if (originalMap) {
                asynchronousBannerAuthenticationSpoofer.resetAuthAndFormContext( originalMap )
            }
        }
    }
}

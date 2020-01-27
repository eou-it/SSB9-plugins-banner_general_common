/*******************************************************************************
 Copyright 2015-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.job

import groovy.util.logging.Slf4j
import net.hedtech.banner.general.communication.email.CommunicationEmailMessage
import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.email.CommunicationSendEmailService
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.general.communication.letter.CommunicationGenerateLetterService
import net.hedtech.banner.general.communication.letter.CommunicationLetterMessage
import net.hedtech.banner.general.communication.letter.CommunicationLetterTemplate
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationMessage
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationTemplate
import net.hedtech.banner.general.communication.mobile.CommunicationSendMobileNotificationService
import net.hedtech.banner.general.communication.template.*
import net.hedtech.banner.general.communication.textmessage.CommunicationSendTextMessageItem
import net.hedtech.banner.general.communication.textmessage.CommunicationSendTextMessageService
import net.hedtech.banner.general.communication.textmessage.CommunicationTextMessage
import net.hedtech.banner.general.communication.textmessage.CommunicationTextMessageTemplate

/**
 * Communication Message Dispatcher evaluates a communication job's template and dispatches
 * work to the appropriate communication channel service.
 */
@Slf4j
class CommunicationMessageDispatcher implements CommunicationTemplateVisitor {

    CommunicationSendEmailService communicationSendEmailService
    CommunicationSendMobileNotificationService communicationSendMobileNotificationService
    CommunicationGenerateLetterService communicationGenerateLetterService
    CommunicationSendTextMessageService communicationSendTextMessageService
    private CommunicationMessage message
    private CommunicationRecipientData recipientData

    public void dispatch( CommunicationTemplate template, CommunicationRecipientData recipientData, CommunicationMessage message ) {
        assert( communicationSendEmailService )
        assert( communicationSendMobileNotificationService )
        assert( communicationGenerateLetterService )
        assert( communicationSendTextMessageService )

        assert( template )
        assert( recipientData )
        assert( message )

        this.message = message
        this.recipientData = recipientData
        template.accept( this )
    }

    @Override
    void visitEmail(CommunicationEmailTemplate template) {
        communicationSendEmailService.send( recipientData.organizationId, message as CommunicationEmailMessage, recipientData, recipientData.pidm )
    }

    @Override
    void visitLetter(CommunicationLetterTemplate template) {
        communicationGenerateLetterService.send( recipientData.organizationId, message as CommunicationLetterMessage, recipientData )
    }

    @Override
    void visitMobileNotification(CommunicationMobileNotificationTemplate template) {
        communicationSendMobileNotificationService.send( recipientData.organizationId, message as CommunicationMobileNotificationMessage, recipientData )
    }

    @Override
    void visitTextMessage(CommunicationTextMessageTemplate template) {
        communicationSendTextMessageService.send( message as CommunicationTextMessage, recipientData )
    }

}

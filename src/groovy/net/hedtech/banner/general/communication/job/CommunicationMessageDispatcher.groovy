/*******************************************************************************
 Copyright 2015-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.job

import net.hedtech.banner.general.communication.email.CommunicationEmailMessage
import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.email.CommunicationSendEmailService
import net.hedtech.banner.general.communication.letter.CommunicationGenerateLetterService
import net.hedtech.banner.general.communication.letter.CommunicationLetterMessage
import net.hedtech.banner.general.communication.letter.CommunicationLetterTemplate
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationMessage
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationTemplate
import net.hedtech.banner.general.communication.mobile.CommunicationSendMobileNotificationService
import net.hedtech.banner.general.communication.template.*
import org.apache.log4j.Logger

/**
 * Communication Message Dispatcher evaluates a communication job's template and dispatches
 * work to the appropriate communication channel service.
 */
class CommunicationMessageDispatcher implements CommunicationTemplateVisitor {

    private static final log = Logger.getLogger(CommunicationMessageDispatcher.class)

    CommunicationSendEmailService communicationSendEmailService
    CommunicationSendMobileNotificationService communicationSendMobileNotificationService
    CommunicationGenerateLetterService communicationGenerateLetterService

    private CommunicationMessage message
    private CommunicationRecipientData recipientData

    public void dispatch( CommunicationTemplate template, CommunicationRecipientData recipientData, CommunicationMessage message ) {
        assert( communicationSendEmailService )
        assert( communicationSendMobileNotificationService )
        assert( communicationGenerateLetterService )

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
}

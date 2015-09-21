/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.job

import net.hedtech.banner.general.communication.email.CommunicationEmailMessage
import net.hedtech.banner.general.communication.email.CommunicationSendEmailService
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationMessage
import net.hedtech.banner.general.communication.mobile.CommunicationSendMobileNotificationService
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.template.CommunicationMessage
import net.hedtech.banner.general.communication.template.CommunicationMobileNotificationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplateVisitor
import org.apache.log4j.Logger

/**
 * Communication Message Dispatcher evaluates a communication job's template and dispatches
 * work to the appropriate communication channel service.
 */
class CommunicationMessageDispatcher implements CommunicationTemplateVisitor {
    def log = Logger.getLogger(this.getClass())

    CommunicationSendEmailService communicationSendEmailService
    CommunicationSendMobileNotificationService communicationSendMobileNotificationService

    private CommunicationMessage message
    private CommunicationRecipientData recipientData

    public void dispatch( CommunicationTemplate template, CommunicationRecipientData recipientData, CommunicationMessage message ) {
        assert( communicationSendEmailService )
        assert( communicationSendMobileNotificationService )

        assert( template )
        assert( recipientData )
        assert( message )

        this.message = message
        this.recipientData = recipientData
        template.accept( this )
    }

    @Override
    void visitEmail(CommunicationEmailTemplate template) {
        communicationSendEmailService.sendEmail( recipientData.organization, message as CommunicationEmailMessage, recipientData, recipientData.pidm )
    }

    @Override
    void visitMobileNotification(CommunicationMobileNotificationTemplate template) {
        communicationSendMobileNotificationService.send( recipientData.organization, message as CommunicationMobileNotificationMessage, recipientData )
    }
}

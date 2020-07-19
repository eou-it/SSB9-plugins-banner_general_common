/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.item

import net.hedtech.banner.general.communication.email.CommunicationSendEmailItem
import net.hedtech.banner.general.communication.email.CommunicationSendEmailService
import net.hedtech.banner.general.communication.letter.CommunicationGenerateLetterService
import net.hedtech.banner.general.communication.mobile.CommunicationSendMobileNotificationService
import net.hedtech.banner.general.communication.textmessage.CommunicationSendTextMessageItem
import net.hedtech.banner.general.communication.textmessage.CommunicationSendTextMessageService

/**
 * Communication Message Dispatcher evaluates a communication job's template and dispatches
 * work to the appropriate communication channel service.
 */
class CommunicationSendDispatcher implements CommunicationSendItemVisitor {

    CommunicationSendEmailService communicationSendEmailService
    CommunicationSendMobileNotificationService communicationSendMobileNotificationService
    CommunicationGenerateLetterService communicationGenerateLetterService
    CommunicationSendTextMessageService communicationSendTextMessageService

    public void dispatch( CommunicationSendItem sendItem ) {
        assert( communicationSendEmailService )
        assert( communicationSendMobileNotificationService )
        assert( communicationGenerateLetterService )
        assert( communicationSendTextMessageService )

        assert( sendItem )
        sendItem.accept( this )
    }

    @Override
    void visitEmail(CommunicationSendEmailItem sendEmailItem) {
        communicationSendEmailService.send( sendEmailItem )
    }

//    @Override
//    void visitLetter(CommunicationLetterTemplate template) {
//        communicationGenerateLetterService.send( recipientData.organizationId, message as CommunicationLetterMessage, recipientData )
//    }
//
//    @Override
//    void visitMobileNotification(CommunicationMobileNotificationTemplate template) {
//        communicationSendMobileNotificationService.send( recipientData.organizationId, message as CommunicationMobileNotificationMessage, recipientData )
//    }
//
    @Override
    void visitTextMessage(CommunicationSendTextMessageItem textMessageItem) {
        //DO NOTHING - Text message is honored via the RESTFUL API and hence not need to be processed as of now
        return;
    }
}

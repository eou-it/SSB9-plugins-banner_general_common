/*********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.item

import net.hedtech.banner.general.communication.email.CommunicationSendEmailItem

/**
 * Communication Template Visitor allows subclasses of CommunicationTemplate a call
 * back mechanism akin to a visitor or double dispatch pattern often in the form of passing
 * a method object. This is useful when inheritance is NOT an option (e.g., implementing
 * a method incur an unwanted package dependency between a server and ui object) and the
 * number of subclasses is understood to be small. The visitor implementations
 * in turn allows the compiler to catch missing code which must be implemented.
 */
public interface CommunicationSendItemVisitor {

    void visitEmail(CommunicationSendEmailItem emailItem )

//    void visitMobileNotification( CommunicationMobileNotificationTemplate template )
//
//    void visitLetter( CommunicationLetterTemplate template )
//
//    void visitTextMessage(CommunicationTextMessageTemplate template )
}
/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.template

import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.letter.CommunicationLetterTemplate
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationTemplate
import net.hedtech.banner.general.communication.textmessage.CommunicationTextMessageTemplate

/**
 * Communication Template Visitor allows subclasses of CommunicationTemplate a call
 * back mechanism akin to a visitor or double dispatch pattern often in the form of passing
 * a method object. This is useful when inheritance is NOT an option (e.g., implementing
 * a method incur an unwanted package dependency between a server and ui object) and the
 * number of subclasses is understood to be small. The visitor implementations
 * in turn allows the compiler to catch missing code which must be implemented.
 */
public interface CommunicationTemplateVisitor {

    void visitEmail( CommunicationEmailTemplate template )

    void visitMobileNotification( CommunicationMobileNotificationTemplate template )

    void visitLetter( CommunicationLetterTemplate template )

//    void visitTextMessage(CommunicationTextMessageTemplate template )
}
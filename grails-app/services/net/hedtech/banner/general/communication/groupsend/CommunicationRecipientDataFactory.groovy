package net.hedtech.banner.general.communication.groupsend

import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.template.CommunicationMobileNotificationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplateVisitor

/**
 * Created by mbrzycki on 9/4/15.
 */
class CommunicationRecipientDataFactory implements CommunicationTemplateVisitor {

    public CommunicationRecipientData create( CommunicationTemplate template ) {
        template.accept( template )
    }

    void visitEmail(CommunicationEmailTemplate template) {

    }

    void visitMobileNotification(CommunicationMobileNotificationTemplate template) {

    }
}

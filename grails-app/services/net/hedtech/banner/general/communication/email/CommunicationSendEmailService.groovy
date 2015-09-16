/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.email

import net.hedtech.banner.general.communication.item.CommunicationEmailItem
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Email Service provides low level email send capability.
 */
class CommunicationSendEmailService {
    private Log log = LogFactory.getLog( this.getClass() )
    def communicationEmailItemService
    def communicationOrganizationService
    def sessionFactory
    def asynchronousBannerAuthenticationSpoofer


    /**
     * Sends an email message (single) based on the contents of EmailMessage passed.
     *
     * @param organization       the organization address config to use for obtaining credentials to connect to the
     *                           SMTP server, and to use to own comm items and interactions generated from the send
     * @param emailMessage       the email message contituents encapsulated in an EmailMessage object
     * @param recipientData      the recipient data this email message belongs to
     * @param pidm               the identifier for the constituent for whom the email is being sent
     */
    public void sendEmail( CommunicationOrganization organization, CommunicationEmailMessage emailMessage, CommunicationRecipientData recipientData, Long pidm  ) {
        log.debug( "sending email message" )

        if (organization?.theSenderMailboxAccount.encryptedPassword != null) {
            organization.theSenderMailboxAccount.clearTextPassword = communicationOrganizationService.decryptPassword( organization.theSenderMailboxAccount.encryptedPassword )
        }
        asynchronousBannerAuthenticationSpoofer.setMepProcessContext(sessionFactory.currentSession.connection(), recipientData.mepCode )
        CommunicationSendEmailMethod sendEmailMethod = new CommunicationSendEmailMethod( emailMessage, organization );
        sendEmailMethod.execute();

        try {
            trackEmailMessage( organization, emailMessage, recipientData, pidm )
        } catch (Throwable t) {
            log.error( t )
            throw t;
        } finally {
        }
    }


    private void trackEmailMessage( CommunicationOrganization organization, CommunicationEmailMessage emailMessage, CommunicationRecipientData recipientData, Long pidm  ) {
        log.debug( "tracking email message sent")
        CommunicationEmailItem emailItem = new CommunicationEmailItem()
        emailItem.setOrganizationId( organization.id )
        emailItem.setReferenceId( recipientData.getReferenceId() )
        emailItem.setToList(emailMessage.getToList().mailAddress.join(", "))
        emailItem.setFromList(organization?.theSenderMailboxAccount?.emailAddress)
        emailItem.setSender(organization?.theSenderMailboxAccount?.emailDisplayName ?: organization?.theSenderMailboxAccount?.emailAddress)
        emailItem.setReplyTo(organization?.theReplyToMailboxAccount?.emailAddress)
        emailItem.setSubject( emailMessage.getSubjectLine() )
        emailItem.setTemplateId(recipientData.templateId)
        emailItem.setContent(emailMessage.messageBody)
        emailItem.setRecipientPidm( pidm )
        emailItem.setCreatedBy( recipientData.ownerId )
        emailItem.setSentDate(emailMessage.dateSent)
        emailItem = communicationEmailItemService.create( emailItem )
        log.debug( "recorded email item sent with item id = ${emailItem.id}." )
    }

}

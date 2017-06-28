/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.email

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccountService
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.commons.lang3.exception.ExceptionUtils

/**
 * Email Service provides low level email send capability.
 */
class CommunicationSendEmailService {
    private Log log = LogFactory.getLog(this.getClass())
    def communicationEmailItemService
    public CommunicationMailboxAccountService communicationMailboxAccountService
    def sessionFactory
    def asynchronousBannerAuthenticationSpoofer

    /**
     * Sends an email message (single) based on the contents of EmailMessage passed.
     *
     * @param organization the organization address config to use for obtaining credentials to connect to the
     *                           SMTP server, and to use to own comm items and interactions generated from the send
     * @param emailMessage the email message contituents encapsulated in an EmailMessage object
     * @param recipientData the recipient data this email message belongs to
     * @param pidm the identifier for the constituent for whom the email is being sent
     */
    public void sendEmail(Long organizationId, CommunicationEmailMessage emailMessage, CommunicationRecipientData recipientData, Long pidm) {
        log.debug("sending email message")


        asynchronousBannerAuthenticationSpoofer.setMepProcessContext(sessionFactory.currentSession.connection(), recipientData.mepCode)
        CommunicationOrganization organization = CommunicationOrganization.fetchById(organizationId)

        if(!organization?.senderMailboxAccount)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("Sender mailbox account settings are not available for the chosen organization"), CommunicationErrorCode.UNKNOWN_ERROR.name())


        if (organization?.senderMailboxAccount.encryptedPassword != null)
            organization.senderMailboxAccount.clearTextPassword = communicationMailboxAccountService.decryptPassword( organization.senderMailboxAccount.encryptedPassword )



        CommunicationSendEmailMethod sendEmailMethod = new CommunicationSendEmailMethod(emailMessage, organization);
        sendEmailMethod.execute();

        try {
            trackEmailMessage(organization, emailMessage, recipientData, pidm)
        } catch (Throwable t) {
            log.error(t)
            throw t;
        } finally {
        }
    }

     void sendTestEmailSetup (Long organizationId, String sendTo) {
         if (sendTo == null || sendTo.length() == 0)
             throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, "communication.error.message.invalidReceiverEmail", CommunicationErrorCode.INVALID_RECEIVER_ADDRESS .name())

         try {
             CommunicationEmailAddress receiverAddress = new CommunicationEmailAddress(     mailAddress: sendTo    )
         } catch (Throwable e) {
             log.error(e)
             throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, "communication.error.message.invalidReceiverEmail", CommunicationErrorCode.UNKNOWN_ERROR_EMAIL.name())
         }

         try {
             CommunicationOrganization organization = CommunicationOrganization.fetchById(organizationId)

             if (!organization)
                 throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, (String) null, CommunicationErrorCode.ORGANIZATION_NOT_FOUND.name())
             if (!receiverAddress || !receiverAddress.mailAddress)
                 throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, "communication.error.message.invalidReceiverEmail", CommunicationErrorCode.INVALID_RECEIVER_ADDRESS.name())

             sendTestEmail(organization, receiverAddress)

        } catch (ApplicationException e) {
            log.error(e)
            if (e.type == 'UNKNOWN_ERROR')
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, "communication.error.message.unknownEmail", CommunicationErrorCode.UNKNOWN_ERROR_EMAIL.name())
            throw e
        } catch (Throwable e) {
            // catch unexpected exceptions
            log.error(e)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, "communication.error.message.unknownEmail", CommunicationErrorCode.UNKNOWN_ERROR_EMAIL.name())
        }
    }

    public void sendTestEmail(CommunicationOrganization organization, CommunicationEmailAddress receiverAddress) {
        checkValidOrganization(organization)
        checkValidEmail(receiverAddress)

        def EMAIL_MESSAGE = 'This is a test email message from the Banner Communication Management Application.'
        def EMAIL_SUBJECT = 'BCM test email'
        CommunicationEmailMessage emailMessage = new CommunicationEmailMessage()
        emailMessage.setSubjectLine(EMAIL_SUBJECT)
        emailMessage.setMessageBody(EMAIL_MESSAGE)
        emailMessage.setMessageBodyContentType("text/html; charset=UTF-8")
        emailMessage.setToList(([receiverAddress] as Set))

        if (organization?.senderMailboxAccount.encryptedPassword != null)
            organization.senderMailboxAccount.clearTextPassword = communicationMailboxAccountService.decryptPassword( organization.senderMailboxAccount.encryptedPassword )
        organization.senderMailboxAccount.clearTextPassword = 'incorrect pass'
        //incorrect password does not make auth fail?

        CommunicationSendEmailMethod sendEmailMethod = new CommunicationSendEmailMethod(emailMessage, organization)

        try {
            sendEmailMethod.execute()
        } catch (Throwable e) {
            log.error(e)
            // catch email exception to give more user friendly name
            if (e.type == 'INVALID_EMAIL_ADDRESS' || e.message == "RFC 822 address format violation.")
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("Invalid test email address."), CommunicationErrorCode.INVALID_EMAIL_ADDRESS.name())
            if (e.type == 'UNKNOWN_ERROR')
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("Unknown error. Email not sent successfully."), CommunicationErrorCode.UNKNOWN_ERROR.name())
            if (e.type.contains('EMAIL_SERVER')) {
                def wrappedException = e.wrappedException
                def cause = getCause(wrappedException)
                def errSplit = cause.message.split(':')
                if (errSplit[0]) {
                    if (errSplit[0].contains('certification path to'))
                        throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException(errSplit[0]), CommunicationErrorCode.CERTIFICATION_PATH_NOT_FOUND.name())
                    if (errSplit[0].contains('Failed to validate certificate'))
                        throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException(errSplit[0]), CommunicationErrorCode.CERTIFICATION_FAILED.name())
                    if (errSplit[0].contains('certification'))
                        throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException(errSplit[0]), CommunicationErrorCode.UNKNOWN_CERTIFICATION_ERROR.name())
                }
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("Could not connect to server : unknown problem."), CommunicationErrorCode.EMAIL_SERVER_CONNECTION_FAILED.name())
            }
            throw e
        } catch (Throwable e) {
            // catch any unexpected error
            log.error(e)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("Unknown error. Email not sent successfully."), CommunicationErrorCode.UNKNOWN_ERROR.name())
        }
    }

    Throwable getCause(Throwable e) {
        Throwable cause = null;
        Throwable result = e;

        while(null != (cause = result.getCause())  && (result != cause) ) {
            result = cause;
        }
        return result;
    }

    private static void checkValidOrganization(CommunicationOrganization organization) {
        if (organization == null)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("Organization not found."), CommunicationErrorCode.EMAIL_SERVER_AUTHENTICATION_FAILED.name())
        // if properties are null then it may default to use root properties.
        if (organization.sendEmailServerProperties != null) {
            if (organization.sendEmailServerProperties.host == null || organization.sendEmailServerProperties.host.length() == 0)
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("Organization host name invalid."), CommunicationErrorCode.EMAIL_SERVER_AUTHENTICATION_FAILED.name())
            if (organization.sendEmailServerProperties.port <= 0)
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("Organization port number is invalid."), CommunicationErrorCode.EMAIL_SERVER_AUTHENTICATION_FAILED.name())
            if (organization.sendEmailServerProperties.securityProtocol == null)
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("Organization security protocol not found."), CommunicationErrorCode.EMAIL_SERVER_AUTHENTICATION_FAILED.name())
            if (organization.sendEmailServerProperties.type == null)
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("Organization type not found."), CommunicationErrorCode.EMAIL_SERVER_AUTHENTICATION_FAILED.name())
        }
        if (organization.senderMailboxAccount == null)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("Sender mailbox not configured properly."), CommunicationErrorCode.INVALID_SENDER_MAILBOX.name())
        if (organization.senderMailboxAccount.type == null)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("Sender mailbox type not found."), CommunicationErrorCode.INVALID_SENDER_MAILBOX.name())
        if (organization.senderMailboxAccount.emailAddress == null)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("Sender email address invalid."), CommunicationErrorCode.EMPTY_SENDER_ADDRESS.name())
        if (organization.senderMailboxAccount.emailAddress.length() == 0)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("Sender email address invalid."), CommunicationErrorCode.EMPTY_SENDER_ADDRESS.name())
    }

    private static void checkValidEmail(CommunicationEmailAddress receiverAddress) {
        if (receiverAddress == null || receiverAddress.mailAddress == null || receiverAddress.mailAddress.length() == 0)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("Receiver address invalid."), CommunicationErrorCode.INVALID_RECEIVER_ADDRESS.name())
    }

    private void trackEmailMessage(CommunicationOrganization organization, CommunicationEmailMessage emailMessage, CommunicationRecipientData recipientData, Long pidm) {
        log.debug("tracking email message sent")
        CommunicationEmailItem emailItem = new CommunicationEmailItem()
        emailItem.setOrganizationId(organization.id)
        emailItem.setReferenceId(recipientData.getReferenceId())
        emailItem.setToList(emailMessage.getToList().mailAddress.join(", "))
        emailItem.setFromList(organization?.senderMailboxAccount?.emailAddress)
        emailItem.setSender(organization?.senderMailboxAccount?.emailDisplayName ?: organization?.senderMailboxAccount?.emailAddress)
        emailItem.setReplyTo(organization?.replyToMailboxAccount?.emailAddress)
        emailItem.setSubject(emailMessage.getSubjectLine())
        emailItem.setTemplateId(recipientData.templateId)
        emailItem.setContent(emailMessage.messageBody)
        emailItem.setRecipientPidm(pidm)
        emailItem.setCreatedBy(recipientData.ownerId)
        emailItem.setSentDate(emailMessage.dateSent)
        emailItem = communicationEmailItemService.create(emailItem)
        log.debug("recorded email item sent with item id = ${emailItem.id}.")
    }



}

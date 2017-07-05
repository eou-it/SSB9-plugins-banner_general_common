/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.email

import com.sun.mail.smtp.SMTPAddressFailedException
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccountService
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.commons.lang3.exception.ExceptionUtils
import sun.security.provider.certpath.SunCertPathBuilderException

import javax.mail.AuthenticationFailedException

/**
 * Email Service provides low level email send capability.
 */
class CommunicationSendEmailService {
    private Log log = LogFactory.getLog(this.getClass())
    def communicationEmailItemService
    def communicationMailboxAccountService
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
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.invalidSenderMailbox"), CommunicationErrorCode.INVALID_SENDER_MAILBOX.name())

        if (organization?.senderMailboxAccount?.encryptedPassword != null)
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

     void sendTestEmailSetup (Long organizationId, String sendTo, Map messageData) {
         if (sendTo == null || sendTo.length() == 0)
             throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.invalidReceiverEmail"), CommunicationErrorCode.INVALID_RECEIVER_ADDRESS .name())
         CommunicationEmailAddress receiverAddress

         try {
             receiverAddress = new CommunicationEmailAddress(     mailAddress: sendTo    )
         } catch (Throwable e) {
             log.error(e)
             throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.invalidReceiverEmail"), CommunicationErrorCode.INVALID_RECEIVER_ADDRESS.name())
         }

         try {
             CommunicationOrganization organization = CommunicationOrganization.fetchById(organizationId)

             if (!organization)
                 throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.organizationNotFound"), CommunicationErrorCode.ORGANIZATION_NOT_FOUND.name())
             if (!receiverAddress || !receiverAddress.mailAddress)
                 throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.invalidReceiverEmail"), CommunicationErrorCode.INVALID_RECEIVER_ADDRESS.name())

             sendTestEmail(organization, receiverAddress, messageData)

        } catch (ApplicationException e) {
            log.error(e)
            if (e.type == 'UNKNOWN_ERROR')
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.unknownEmail"), CommunicationErrorCode.UNKNOWN_ERROR_EMAIL.name())
            throw e
        } catch (Throwable e) {
            // catch unexpected exceptions
            log.error(e)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.unknownEmail"), CommunicationErrorCode.UNKNOWN_ERROR_EMAIL.name())
        }
    }

    public void sendTestEmail(CommunicationOrganization organization, CommunicationEmailAddress receiverAddress, Map messageData) {
        checkValidOrganization(organization)
        checkValidEmail(receiverAddress)

        def EMAIL_MESSAGE = messageData.body
        def EMAIL_SUBJECT = messageData.subject
        CommunicationEmailMessage emailMessage = new CommunicationEmailMessage()
        emailMessage.setSubjectLine(EMAIL_SUBJECT)
        emailMessage.setMessageBody(EMAIL_MESSAGE)
        emailMessage.setMessageBodyContentType("text/html; charset=UTF-8")
        emailMessage.setToList(([receiverAddress] as Set))

        if (organization?.senderMailboxAccount?.encryptedPassword != null)
            organization.senderMailboxAccount.clearTextPassword = communicationMailboxAccountService.decryptPassword( organization.senderMailboxAccount.encryptedPassword )

        CommunicationSendEmailMethod sendEmailMethod = new CommunicationSendEmailMethod(emailMessage, organization)

        try {
            sendEmailMethod.execute()
        } catch (ApplicationException e) {
            log.error(e)
            // catch email exception to give more user friendly name
            if (e.type == 'INVALID_EMAIL_ADDRESS' || e.message == "RFC 822 address format violation.")
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.invalidEmailGeneral"), CommunicationErrorCode.INVALID_EMAIL_ADDRESS.name())
            if (e.type == 'UNKNOWN_ERROR')
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.unknownEmail"), CommunicationErrorCode.UNKNOWN_ERROR_EMAIL.name())
//            if (e.type == 'EMAIL_SERVER_AUTHENTICATION_FAILED')
//                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.certification.authenticationFailedUserPass"), CommunicationErrorCode.EMAIL_SERVER_AUTHENTICATION_FAILED.name())
            if (e.type.contains('EMAIL_SERVER')) {
                def wrappedException = e.wrappedException
                def cause = getCause(wrappedException)
                def errSplit = cause.message.split(':')[0]?:""
                if (cause.getClass() == SunCertPathBuilderException.class || errSplit.contains('certification path to'))
                    throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.certification.notFoundOnPath"), CommunicationErrorCode.CERTIFICATION_PATH_NOT_FOUND.name())
                // TODO Possibly not in english. Look for better way to identify specific problems
                if (errSplit.contains('Failed to validate certificate'))
                    throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.certification.failed"), CommunicationErrorCode.CERTIFICATION_FAILED.name())
                if (errSplit.contains('certification'))
                    throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.certification.unknownError"), CommunicationErrorCode.UNKNOWN_CERTIFICATION_ERROR.name())

                if (cause.getClass() == AuthenticationFailedException.class || errSplit.contains('535') || errSplit.contains(' Authentication Failed'))
                    throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.certification.authenticationFailedUserPass"), CommunicationErrorCode.EMAIL_SERVER_USER_NOT_AUTHORIZED.name())
                if (cause.getClass() == SMTPAddressFailedException.class || errSplit.contains('550 5.1.1 User unknown'))
                    throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.certification.userNotAuthorized"), CommunicationErrorCode.EMAIL_SERVER_AUTHENTICATION_FAILED.name())
                if (errSplit.contains('uthentication'))
                    throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.certification.authenticationFailedUnknown"), CommunicationErrorCode.EMAIL_SERVER_AUTHENTICATION_FAILED_UNKNOWN.name())

                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.server.failed"), CommunicationErrorCode.EMAIL_SERVER_CONNECTION_FAILED.name())
            }
            throw e
        } catch (Throwable e) {
            // catch any unexpected error
            log.error(e)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.unknownEmail"), CommunicationErrorCode.UNKNOWN_ERROR_EMAIL.name())
        }
    }

    /*
     * Method to find root cause of exception and extract the message. Used to identify certification exceptions.
     */
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
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.organizationNotFound"), CommunicationErrorCode.ORGANIZATION_NOT_FOUND.name())
        // if properties are null then it may default to use root properties.
        if (organization.sendEmailServerProperties != null) {
            if (organization.sendEmailServerProperties.host == null || organization.sendEmailServerProperties.host.length() == 0)
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.server.hostNotFound"), CommunicationErrorCode.EMAIL_SERVER_HOST_NOT_FOUND.name())
            if (organization.sendEmailServerProperties.port <= 0)
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.server.portInvalid"), CommunicationErrorCode.EMAIL_SERVER_PORT_INVALID.name())
            if (organization.sendEmailServerProperties.securityProtocol == null)
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.server.securityProtocolInvalid"), CommunicationErrorCode.EMAIL_SERVER_SECURITY_PROTOCOL_INVALID.name())
            if (organization.sendEmailServerProperties.type == null)
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.server.typeInvalid"), CommunicationErrorCode.EMAIL_SERVER_TYPE_INVALID.name())
        }
        if (organization.senderMailboxAccount == null)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.senderMailbox.notFound"), CommunicationErrorCode.INVALID_SENDER_MAILBOX.name())
        if (organization.senderMailboxAccount.type == null)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.senderMailbox.typeNotFound"), CommunicationErrorCode.INVALID_SENDER_MAILBOX_TYPE.name())
        if (organization.senderMailboxAccount.emailAddress == null)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.senderMailbox.emptySenderEmail"), CommunicationErrorCode.EMPTY_SENDER_ADDRESS.name())
        if (organization.senderMailboxAccount.emailAddress.length() == 0)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.senderMailbox.emptySenderEmail"), CommunicationErrorCode.EMPTY_SENDER_ADDRESS.name())
    }

    private static void checkValidEmail(CommunicationEmailAddress receiverAddress) {
        if (receiverAddress == null || receiverAddress.mailAddress == null || receiverAddress.mailAddress.length() == 0)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendEmailService.class, new RuntimeException("communication.error.message.invalidReceiverEmail"), CommunicationErrorCode.INVALID_RECEIVER_ADDRESS.name())
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

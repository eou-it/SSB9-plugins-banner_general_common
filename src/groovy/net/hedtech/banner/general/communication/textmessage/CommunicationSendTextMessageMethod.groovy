/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.textmessage

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.email.CommunicationEmailAddress
import net.hedtech.banner.general.communication.email.CommunicationEmailAuthenticator
import net.hedtech.banner.general.communication.email.CommunicationEmailMessage
import net.hedtech.banner.general.communication.email.CommunicationEmailReceipt
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerConnectionSecurity
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerProperties
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.commons.mail.EmailException
import org.apache.commons.mail.HtmlEmail

import javax.mail.AuthenticationFailedException
import javax.mail.MessagingException
import javax.mail.internet.InternetAddress

/**
 * Performs the details of assembling and sending out a text message (SMS).
 */
class CommunicationSendTextMessageMethod {

    private Log log = LogFactory.getLog( this.getClass() )

    private CommunicationTextMessage textMessage;
    private CommunicationOrganization senderOrganization;
    private HtmlEmail email;
    private CommunicationEmailReceipt lastSend;

    CommunicationSendTextMessageMethod(CommunicationTextMessage textMessage, CommunicationOrganization senderOrganization ) {
        this.textMessage = textMessage;
        this.senderOrganization = senderOrganization;
    }

    public void execute() {
        CommunicationEmailReceipt emailReceipt = new CommunicationEmailReceipt();

        def senderAddress = new CommunicationEmailAddress(
                mailAddress: senderOrganization.senderMailboxAccount.emailAddress,
                displayName: senderOrganization.senderMailboxAccount.emailDisplayName
        )
//        textMessage.senders = [senderAddress] as Set
        minimumFieldsPresent( textMessage, false );

        //adding smtp 'from' for handling bounce back emails.
        emailReceipt.setFrom( senderOrganization.senderMailboxAccount.emailAddress );

        if (log.isDebugEnabled()) {
            log.debug( "Connecting to email server with account username = " + senderOrganization.senderMailboxAccount.getUserName() );
        }
//        optOutMessageId = uuidService.fetchOneGuid();

        try {
            //Add the OptOut Link in to the message being sent
            StringBuilder sb = new StringBuilder();
            if (textMessage.messageContent != null) {
                sb.append( textMessage.messageContent );
            }

            textMessage.setMessageContent( sb.toString() );
            email = createMessage( textMessage, true, emailReceipt )
            setConnectionProperties( email )
            email.getMailSession()?.setDebug(log.isDebugEnabled() || log.isTraceEnabled())

            email.send()
            this.lastSend = emailReceipt;
        } catch (EmailException e) {
            log.error( "EmailServer.SendEmailMethod.execute caught exception " + e, e );
            // throw custom system exception here MJB
            String fromList = InternetAddress.toString( email.getFromAddress() )
            String recipientList = email.getToAddresses().toListString()
            String replyToList = email.getReplyToAddresses().toListString()
            if(e.getCause() instanceof AuthenticationFailedException) {
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendTextMessageMethod.class, new RuntimeException("communication.error.message.certification.authenticationFailedUserPass"), CommunicationErrorCode.EMAIL_SERVER_AUTHENTICATION_FAILED.name())
            }
            else if(e.getCause() instanceof MessagingException) {
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendTextMessageMethod.class, new RuntimeException("communication.error.message.server.failed"), CommunicationErrorCode.EMAIL_SERVER_CONNECTION_FAILED.name())
            }
            else
            {
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendTextMessageMethod.class, e, CommunicationErrorCode.UNKNOWN_ERROR.name())
            }
        } catch (Throwable e) {
            log.error( "EmailServer.SendEmailMethod.execute caught exception " + e, e );
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendTextMessageMethod.class, e, CommunicationErrorCode.UNKNOWN_ERROR.name())
        }
    }

    public String getOptOutMessageId() {
        return optOutMessageId;
    }

    public CommunicationEmailReceipt getLastSend() {
        return lastSend;
    }

    /**
     * Create an HtmlEmail message based on the parameters passed.
     *
     * @param message       the email message to be sent
     * @param setRecipients true then the MimeMessage created has the recipients placeholders (to, cc, bcc)
     *                      filled from the place holders in the EmailMessage passed
     * @param emailReceipt  the CommunicationEmailReceipt object to set the email addresses
     * @return the created HtmlEmail message with the appropriate placeholders filled in
     */
    private HtmlEmail createMessage( final CommunicationTextMessage message, boolean setRecipients, CommunicationEmailReceipt emailReceipt ) {

        HtmlEmail htmlEmail = new HtmlEmail();
        htmlEmail.setCharset( "UTF-8" )
        htmlEmail.setFrom( "bindhu.prakash@university.edu" );

        // Set To
        emailReceipt.setTo(message.toList);
        htmlEmail.addTo(message.toList)

        String messageText = message.messageContent
        if (null == messageText || 0 == messageText.trim().length()) {
            messageText = new String();
        }
        htmlEmail.setHtmlMsg(messageText);
        htmlEmail.setSentDate(new Date());

        return htmlEmail;
    }

    /**
     * Sets the session properties for sending the email
     * @param email the HtmlEmail object to set the appropriate to
     */
    private void setConnectionProperties( HtmlEmail email ) {
        CommunicationEmailServerProperties sendEmailServerProperties

        if (senderOrganization?.sendEmailServerProperties != null)
            sendEmailServerProperties = senderOrganization?.sendEmailServerProperties
        else {
            sendEmailServerProperties = CommunicationOrganization.fetchRoot()?.sendEmailServerProperties
        }

        if (sendEmailServerProperties) {
            email.setHostName(sendEmailServerProperties.host);
            if (sendEmailServerProperties.securityProtocol == CommunicationEmailServerConnectionSecurity.None) {
                email.setSmtpPort(sendEmailServerProperties.port);
            } else if (sendEmailServerProperties.securityProtocol == CommunicationEmailServerConnectionSecurity.SSL) {
                email.setSSLOnConnect(true);
                email.setSslSmtpPort(sendEmailServerProperties.port.toString());
            } else if (sendEmailServerProperties.securityProtocol == CommunicationEmailServerConnectionSecurity.TLS) {
                email.setSmtpPort(sendEmailServerProperties.port);
                email.setStartTLSEnabled(true);
            } else {
                throw new RuntimeException( "Unsupported email server connection security. Security Protocol = ${senderOrganization.sendEmailServerProperties.securityProtocol}." )
            }
        }

        boolean shouldAuthenticate
        def smtpProperties = sendEmailServerProperties?.getSmtpPropertiesAsMap()
        if (smtpProperties && smtpProperties?.auth != null)
            shouldAuthenticate = smtpProperties.auth
        else
            shouldAuthenticate = true

        if (shouldAuthenticate) {
            CommunicationEmailAuthenticator auth = new CommunicationEmailAuthenticator( senderOrganization.senderMailboxAccount.userName, senderOrganization.senderMailboxAccount.clearTextPassword );
            email.setAuthenticator(auth);
        }
    }


    /**
     * Returns an array of Address objects from the passed emailAddresses.
     *
     * @param emailAddresses the set of EmailAddresses to be converted
     * @return Address[] converted EmailAddresses
     */
    private InternetAddress[] getAddressArray( Set<CommunicationEmailAddress> emailAddresses ) {
        if (null == emailAddresses || 0 == emailAddresses.size()) {
            return null;
        }

        ArrayList<InternetAddress> list = new ArrayList<InternetAddress>();
        for (CommunicationEmailAddress address : emailAddresses) {
            try {
                list.add( new InternetAddress( address.getMailAddress(), address.getDisplayName() ) );
            } catch (UnsupportedEncodingException uee) {

            }
        }

        InternetAddress[] retAddresses;
        retAddresses = list.toArray( new InternetAddress[0] );

        return retAddresses;
    }


    /**
     * Validates if the EmailMessage object passed has all the necessary/mandatory fields present or filled.
     *
     * @param message      the EmailMessage to check
     * @param ignoreToList true if the 'to' field should be ignored while validating
     * @throws ApplicationException thrown if the EmailMessage has some or all compulsary fields empty
     */
    private void minimumFieldsPresent( final CommunicationTextMessage message, boolean ignoreToList ) {
        ApplicationException exception = null;

        if (null == message) {
            exception = CommunicationExceptionFactory.createApplicationException(CommunicationSendTextMessageMethod.class, new RuntimeException("exception.communication.email.nullMessage"), CommunicationErrorCode.UNKNOWN_ERROR.name())
        } else if (false == ignoreToList && (null == message.getToList() || 0 == message.getToList().size())) {
            exception = CommunicationExceptionFactory.createApplicationException(CommunicationSendTextMessageMethod.class, new RuntimeException("exception.communication.email.recipientsEmpty"), CommunicationErrorCode.EMPTY_RECIPIENT_ADDRESS.name())
        }
        if (null != exception) {
            throw exception;
        }
    }

}
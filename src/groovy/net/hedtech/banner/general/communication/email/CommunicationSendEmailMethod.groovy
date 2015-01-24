/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.email

import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccount
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Created by mbrzycki on 1/6/15.
 */
class CommunicationSendEmailMethod {
    private Log log = LogFactory.getLog( this.getClass() )

    private CommunicationEmailMessage emailMessage;
    private CommunicationMailboxAccount sender;
    private MimeMessage mimeMessage;
    private String optOutMessageId;
    private CommunicationEmailReceipt lastSend;

    private CommunicationSendEmailMethod(CommunicationEmailMessage emailMessage, CommunicationMailboxAccount sender ) {
        this.emailMessage = emailMessage;
        this.sender = sender;
    }

    public void execute() {
        CommunicationEmailReceipt emailReceipt = new CommunicationEmailReceipt();
        def senderAddress = new CommunicationEmailAddress(mailAddress:sender.emailAddress,displayName:sender.emailDisplayName)
        emailMessage.senders = [senderAddress] as Set
        minimumFieldsPresent( emailMessage, false );
        Properties props = new Properties();
        //adding smtp 'from' for handling bounce back emails.

        emailReceipt.setFrom( sender.getEmailAddress());
        props.put( "mail.smtp.from", sender.getEmailAddress() );

        if (log.isDebugEnabled()) {
            log.debug( "Connecting to email server with account username = " + sender.getUserName() );
        }
        Session session = newSendSession(sender, props );
//        optOutMessageId = uuidService.fetchOneGuid();

        try {
            //Add the OptOut Link in to the message being sent
            StringBuilder sb = new StringBuilder();
            if (emailMessage.getMessageBody() != null) {
                sb.append( emailMessage.getMessageBody() );
            }
//            sb.append(
//                    EmailAddressUtilities.getOptOutText(
//                    getCommunicationConfiguration().getEmailService().getOptOutUrl(),
//                    getCommunicationConfiguration().getEmailService().getOptOutText(),
//                    this.optOutMessageId )
//            );


            emailMessage.setMessageBody( sb.toString() );

            mimeMessage = createMimeMessage( emailMessage, true, session, emailReceipt );

            if (log.isDebugEnabled()) {
                StringBuilder logMessage = new StringBuilder( "About to send email message:\n" );
                logMessage.append( "*** BEGIN MSG ***\n" );
                Properties sessionProperties = session.getProperties();
                for (Object key:sessionProperties.keySet()) {
                    logMessage.append( String.valueOf( key ) ).append( ": " ).append( String.valueOf( sessionProperties.get( key ) ) ).append( "\n" );
                }
                logMessage.append( "From: \n" );
                for( InternetAddress address:mimeMessage.getFrom() ) {
                    logMessage.append( "    " ).append( address.toString() ).append( "\n" );
                }
                logMessage.append( "Recipients: \n" );
                for( InternetAddress address:mimeMessage.getAllRecipients() ) {
                    logMessage.append( "    " ).append( address.toString() ).append( "\n" );
                }
                logMessage.append( "Reply-To: \n" );
                for( InternetAddress address:mimeMessage.getReplyTo() ) {
                    logMessage.append( "    " ).append( address.toString() ).append( "\n" );
                }
                logMessage.append( "Subject: " ).append( mimeMessage.getSubject() ).append( "\n" );
                logMessage.append( "Body: \n" );
                logMessage.append( String.valueOf( mimeMessage.getContent() ) );
                logMessage.append( "\n*** END MSG***" );
                log.debug( logMessage.toString() );
            }

            Transport.send( mimeMessage );
            emailMessage.setStatus( "SUCCESS" );
            this.lastSend = emailReceipt;
        } catch (MessagingException e) {
            log.error( "EmailServer.SendEmailMethod.execute caught exception " + e, e );
            // throw custom system exception here MJB
            String fromList = "";
            String recipientList = "";
            String replyToList = "";
            try {
                fromList = InternetAddress.toString( mimeMessage.getFrom() );
            } catch (MessagingException me) {
                log.error( "Error extracting fromList.", me );
            }
            try {
                recipientList = InternetAddress.toString( mimeMessage.getAllRecipients() );
            } catch (MessagingException me) {
                log.error( "Error extracting recipientList.", me );
            }
            try {
                replyToList = InternetAddress.toString( mimeMessage.getReplyTo() );
            } catch (MessagingException me) {
                log.error( "Error extracting replyToList.", me );
            }
            throw new CommunicationMessagingSystemException( e, fromList, recipientList, replyToList );
        } catch (Exception e) {
            log.error( "EmailServer.SendEmailMethod.execute caught exception " + e, e );
            throw new ApplicationException( CommunicationSendEmailMethod.class, "EmailServer.SendEmailMethod.execute", e );
        }
    }


    public Enumeration getHeaders() {
        try {
            return mimeMessage.getAllHeaders();
        } catch (MessagingException e) {
            log.error( "EmailServer.SendEmailMethod.getHeaders caught exception " + e, e );
            throw new ApplicationException( CommunicationSendEmailMethod.class, "EmailServer.SendEmailMethod.getHeaders", e );
        }
    }

    public String getOptOutMessageId() {
        return optOutMessageId;
    }

    public CommunicationEmailReceipt getLastSend() {
        return lastSend;
    }

    /**
     * Create a MimeMessage based on the parameters passed.
     *
     * @param message       the email message to be sent
     * @param setRecipients true then the MimeMessage created has the recipients placeholders (to, cc, bcc)
     *                      filled from the place holders in the EmailMessage passed
     * @param session       the session to be used to create the MimeMessage
     * @return the created MimeMessage with the appropiate placeholders filled in
     * @throws MessagingException if there is exception while creating the MimeMessage
     */
    private MimeMessage createMimeMessage( final CommunicationEmailMessage message, boolean setRecipients, Session session, CommunicationEmailReceipt emailReceipt )
            throws MessagingException {
        MimeMessage retMessage = new MimeMessage( session );

        retMessage.setFrom( new InternetAddress(message.senders?.mailAddress,message.senders?.displayName) );

        // Set Reply-To
        InternetAddress [] addresses = getAddressArray(message.getReplyTo());
        emailReceipt.setReplyTo( InternetAddress.toString( addresses ) );
        retMessage.setReplyTo( addresses );

        if (true == setRecipients) {
            // Set To:
            addresses = getAddressArray( message.getToList() );
            emailReceipt.setTo( InternetAddress.toString( addresses ) );
            retMessage.setRecipients( Message.RecipientType.TO, addresses );

            // Set cc:
            addresses = getAddressArray( message.getCcList() );
            if( addresses && addresses.length() > 0 ) {
                emailReceipt.setCc(InternetAddress.toString(addresses));
                retMessage.setRecipients(Message.RecipientType.CC, addresses);
            }

            // Set bcc:
            addresses = getAddressArray( message.getBccList() );
            if( addresses && addresses.length() > 0 ) {
                emailReceipt.setBcc(InternetAddress.toString(addresses));
                retMessage.setRecipients(Message.RecipientType.BCC, addresses);
            }
        }

        retMessage.setSubject( message.getSubjectLine() );

        String messageText = message.getMessageBody();
        if (null == messageText || 0 == messageText.trim().length()) {
            messageText = new String();
        }
        retMessage.setContent( messageText, message.getMessageBodyContentType() );

        retMessage.setSentDate( message.getDateSent() );
        retMessage.saveChanges();

        return retMessage;
    }


    /**
     * Returns a javamail session for sending email.
     * The properties used for the session will be taken from the EmailSender element in the configuration.
     * A set of properties to override or add to properties in the configuration may be passed in.
     *
     * @param sender mailbox account to use to get the send account information for authentication
     * @param overrides properties to add to override in the session
     * @return
     */
    private Session newSendSession( CommunicationMailboxAccount sender, Properties overrides ) {
        Properties emailServerProperties = Holders.config?.communication?.email?.sendProperties.toProperties('mail.smtp')
        log.debug "Mail server properties:" + emailServerProperties.toString()
        if (!emailServerProperties) emailServerProperties = new Properties()

        for (Object o : overrides.keySet()) {
            emailServerProperties.setProperty( (String) o, overrides.getProperty( (String) o ) );
        }

        CommunicationEmailAuthenticator auth = new CommunicationEmailAuthenticator( sender );
        return Session.getInstance( emailServerProperties, auth );
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
    private void minimumFieldsPresent( final CommunicationEmailMessage message, boolean ignoreToList ) {
        ApplicationException exception = null;

        if (null == message) {
            exception = new ApplicationException( CommunicationSendEmailMethod.class, "exception.communication.email.nullMessage" );
        } else if (null == message.getSenders()) {
            exception = new ApplicationException( CommunicationSendEmailMethod.class, "exception.communication.email.sendersEmpty" );
        } else if (false == ignoreToList && (null == message.getToList() || 0 == message.getToList().size())) {
            exception = new ApplicationException( CommunicationSendEmailMethod.class, "exception.communication.email.recipientsEmpty" );
        } else if (null == message.getSubjectLine() || 0 == message.getSubjectLine().trim().length()) {
            exception = new ApplicationException( CommunicationSendEmailMethod.class, "exception.communication.email.subjectEmpty" );
        }
        if (null != exception) {
            throw exception;
        }
    }

}

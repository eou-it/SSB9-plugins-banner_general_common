/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.mobile

import groovyx.net.http.HTTPBuilder
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.ExceptionFactory
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.email.CommunicationEmailAddress
import net.hedtech.banner.general.communication.email.CommunicationEmailAuthenticator
import net.hedtech.banner.general.communication.email.CommunicationEmailMessage
import net.hedtech.banner.general.communication.email.CommunicationEmailReceipt
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerConnectionSecurity
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerProperties
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccount
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory


/**
 * Performs the details of assembling and sending out an Ellucian Go mobile notification.
 */
class CommunicationSendMobileNotificationMethod {
    private Log log = LogFactory.getLog( this.getClass() )

    private String optOutMessageId;
    private CommunicationEmailReceipt lastSend;


    private boolean isEmpty( String s ) {
        return !s || s.length() == 0
    }

    public void send(CommunicationMobileNotificationMessage message, CommunicationOrganization senderOrganization) {
        log.trace( "Begin send mobile notification." )
        assert( message )
        assert( senderOrganization )
        if (senderOrganization.encryptedMobileApplicationKey) {
            assert senderOrganization.clearMobileApplicationKey
        }

        if (isEmpty( senderOrganization.mobileEndPointUrl )) {
            throw ExceptionFactory.createFriendlyApplicationException( CommunicationSendMobileNotificationMethod.class,
                CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_ENDPOINT_URL.name,
                "emptyMobileNotificationEndpointUrl",
                senderOrganization.name
            )
        }

        if (isEmpty( senderOrganization.mobileApplicationName )) {
            throw ExceptionFactory.createApplicationException( CommunicationSendMobileNotificationMethod.class,
                CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_APPLICATION_NAME.name,
                "emptyMobileNotificationApplicationName",
                senderOrganization.name
            )
        }

        if (isEmpty( senderOrganization.encryptedMobileApplicationKey )) {
            throw ExceptionFactory.createFriendlyApplicationException( CommunicationSendMobileNotificationMethod.class,
                    CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_APPLICATION_KEY.name,
                    "emptyMobileNotificationApplicationKey",
                    senderOrganization.name
            )
        }

        try {

        // Ex: 'https://mobiledev1.ellucian.com/'
        HTTPBuilder httpBuilder = new HTTPBuilder( senderOrganization.mobileEndPointUrl )
        httpBuilder.auth.basic senderOrganization.mobileApplicationName, senderOrganization.clearMobileApplicationKey

        } catch (Throwable t) {
            log.error( e );
            // throw custom system exception here MJB
//            String fromList = InternetAddress.toString( email.getFromAddress() )
//            String recipientList = email.getToAddresses().toListString()
//            String replyToList = email.getReplyToAddresses().toListString()
//            if(e.getCause() instanceof AuthenticationFailedException) {
//                 throw ExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, e, CommunicationErrorCode.EMAIL_SERVER_AUTHENTICATION_FAILED.name())
//            }
//            else if(e.getCause() instanceof MessagingException) {
//                throw ExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, e, CommunicationErrorCode.EMAIL_SERVER_CONNECTION_FAILED.name())
//            }
//            else
//            {
//                throw ExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, e, CommunicationErrorCode.UNKNOWN_ERROR.name())
//            }
        } catch (Exception t) {
//            log.error( "EmailServer.SendEmailMethod.execute caught exception " + e, e );
//            throw ExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, e, CommunicationErrorCode.UNKNOWN_ERROR.name())
        }
    }

//    /**
//     * Examines the sender organization and sets a reply to address (if present in the organization) on the current email message object.
//     * The mime message will later be constructed from the email message object.
//     */
//    private void assignReplyToAddress() {
//        if (senderOrganization?.replyToMailboxAccountSettings) {
//            List<CommunicationMailboxAccount> replyToList = senderOrganization?.replyToMailboxAccountSettings
//            if (replyToList.size() > 0) {
//                CommunicationMailboxAccount replyToMailboxAccount = replyToList.get(0)
//                def replyToAddress = new CommunicationEmailAddress(
//                        mailAddress: replyToMailboxAccount.emailAddress,
//                        displayName: replyToMailboxAccount.emailDisplayName
//                )
//                message.replyTo = [replyToAddress] as Set
//            }
//        }
//    }
//
//
//    public String getOptOutMessageId() {
//        return optOutMessageId;
//    }
//
//    public CommunicationEmailReceipt getLastSend() {
//        return lastSend;
//    }
//
//    /**
//     * Create an HtmlEmail message based on the parameters passed.
//     *
//     * @param message       the email message to be sent
//     * @param setRecipients true then the MimeMessage created has the recipients placeholders (to, cc, bcc)
//     *                      filled from the place holders in the EmailMessage passed
//     * @param emailReceipt  the CommunicationEmailReceipt object to set the email addresses
//     * @return the created HtmlEmail message with the appropriate placeholders filled in
//     */
//    private HtmlEmail createMessage( final CommunicationEmailMessage message, boolean setRecipients, CommunicationEmailReceipt emailReceipt ) {
//
//         HtmlEmail htmlEmail = new HtmlEmail();
//         htmlEmail.setFrom(message.senders?.mailAddress, message.senders?.displayName );
//         // Set Reply-To
//         InternetAddress [] addresses = getAddressArray(message.getReplyTo());
//         if( addresses && addresses.length >0) {
//             emailReceipt.setReplyTo(InternetAddress.toString(addresses));
//             htmlEmail.setReplyTo(addresses.toList())
//         }
//
//        if (true == setRecipients) {
//             // Set To:
//             addresses = getAddressArray( message.getToList() );
//             emailReceipt.setTo(InternetAddress.toString(addresses));
//             htmlEmail.setTo(addresses.toList());
//
//             // Set cc:
//             addresses = getAddressArray( message.getCcList() );
//             if( addresses && addresses.length > 0 ) {
//                 emailReceipt.setCc(InternetAddress.toString(addresses));
//                 htmlEmail.setCc(addresses?.toList())
//             }
//
//             // Set bcc:
//             addresses = getAddressArray( message.getBccList() );
//             if( addresses && addresses.length > 0 ) {
//                 emailReceipt.setBcc(InternetAddress.toString(addresses));
//                 htmlEmail.setBcc(addresses?.toList());
//             }
//         }
//
//         htmlEmail.setSubject(message.getSubjectLine())
//         String messageText = message.getMessageBody();
//         if (null == messageText || 0 == messageText.trim().length()) {
//             messageText = new String();
//         }
//         htmlEmail.setMsg(messageText);
//         htmlEmail.setSentDate(message.getDateSent());
//
//         return htmlEmail;
//    }
//
//    /**
//     * Sets the session properties for sending the email
//     * @param email the HtmlEmail object to set the appropriate to
//     */
//    private void setConnectionProperties( HtmlEmail email ) {
//        Properties emailServerProperties = new Properties()
//
//        CommunicationEmailServerProperties sendEmailServerProperties
//
//        if (senderOrganization?.sendEmailServerProperties != null && senderOrganization?.sendEmailServerProperties.size() > 0)
//               sendEmailServerProperties = senderOrganization?.sendEmailServerProperties?.get(0)
//        else {
//            sendEmailServerProperties = CommunicationOrganization.fetchRoot()?.sendEmailServerProperties?.get(0)
//        }
//
//        if (sendEmailServerProperties) {
//            email.setHostName(sendEmailServerProperties.host);
//            if (sendEmailServerProperties.securityProtocol == CommunicationEmailServerConnectionSecurity.None) {
//                email.setSmtpPort(sendEmailServerProperties.port);
//            } else if (sendEmailServerProperties.securityProtocol == CommunicationEmailServerConnectionSecurity.SSL) {
//                email.setSSLOnConnect(true);
//                email.setSslSmtpPort(sendEmailServerProperties.port.toString());
//            } else if (sendEmailServerProperties.securityProtocol == CommunicationEmailServerConnectionSecurity.TLS) {
//                email.setSmtpPort(sendEmailServerProperties.port);
//                email.setStartTLSEnabled(true);
//            } else {
//                throw new RuntimeException( "Unsupported email server connection security. Security Protocol = ${senderOrganization.theSendEmailServerProperties.securityProtocol}." )
//            }
//        }
//
//        log.debug "Mail server properties:" + emailServerProperties.toString()
//        CommunicationEmailAuthenticator auth = new CommunicationEmailAuthenticator( senderOrganization.theSenderMailboxAccount );
//        email.setAuthenticator(auth);
//    }
//
//
//    /**
//     * Returns an array of Address objects from the passed emailAddresses.
//     *
//     * @param emailAddresses the set of EmailAddresses to be converted
//     * @return Address[] converted EmailAddresses
//     */
//    private InternetAddress[] getAddressArray( Set<CommunicationEmailAddress> emailAddresses ) {
//        if (null == emailAddresses || 0 == emailAddresses.size()) {
//            return null;
//        }
//
//        ArrayList<InternetAddress> list = new ArrayList<InternetAddress>();
//        for (CommunicationEmailAddress address : emailAddresses) {
//            try {
//                list.add( new InternetAddress( address.getMailAddress(), address.getDisplayName() ) );
//            } catch (UnsupportedEncodingException uee) {
//
//            }
//        }
//
//        InternetAddress[] retAddresses;
//        retAddresses = list.toArray( new InternetAddress[0] );
//
//        return retAddresses;
//    }
//
//
//    /**
//     * Validates if the EmailMessage object passed has all the necessary/mandatory fields present or filled.
//     *
//     * @param message      the EmailMessage to check
//     * @param ignoreToList true if the 'to' field should be ignored while validating
//     * @throws ApplicationException thrown if the EmailMessage has some or all compulsary fields empty
//     */
//    private void minimumFieldsPresent( final CommunicationEmailMessage message, boolean ignoreToList ) {
//        ApplicationException exception = null;
//
//        if (null == message) {
//            exception = ExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, new RuntimeException("exception.communication.email.nullMessage"), CommunicationErrorCode.UNKNOWN_ERROR.name())
//        } else if (null == message.getSenders()) {
//            exception = ExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, new RuntimeException("exception.communication.email.sendersEmpty"), CommunicationErrorCode.EMPTY_SENDER_ADDRESS.name())
//        } else if (false == ignoreToList && (null == message.getToList() || 0 == message.getToList().size())) {
//            exception = ExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, new RuntimeException("exception.communication.email.recipientsEmpty"), CommunicationErrorCode.EMPTY_RECIPIENT_ADDRESS.name())
//        } else if (null == message.getSubjectLine() || 0 == message.getSubjectLine().trim().length()) {
//            exception = ExceptionFactory.createApplicationException(CommunicationSendMobileNotificationMethod.class, new RuntimeException("exception.communication.email.subjectEmpty"), CommunicationErrorCode.EMPTY_EMAIL_SUBJECT.name())
//        }
//        if (null != exception) {
//            throw exception;
//        }
//    }
//
}


/**
 def save(Notification notificationInstance) {
 if (notificationInstance == null) {
 notFound()
 return
 }

 if (notificationInstance.hasErrors()) {
 respond notificationInstance.errors, view:'create'
 return
 }

 notificationInstance.save flush:true

 def String csvList = new String(notificationInstance.csvFile)

 def http = new HTTPBuilder( 'https://mobiledev1.ellucian.com/' )
 http.auth.basic 'StudentSuccess', 'ss-key-value'
 http.request(POST, JSON) { req ->
 uri.path = '/banner-mobileserver/api/notification/notifications/'
 headers.Accept = 'application/json'

 def recipientsList = []
 def idTypeString = notificationInstance ? "loginId" : "sisId"

 if (notificationInstance.importList) {
 csvList.eachCsvLine {line ->
 line.each {
 def recipient = [idType : idTypeString, id : it]
 recipientsList << recipient
 }
 }

 } else {
 recipientsList = [
 [
 idType : 'all',
 id : null
 ]
 ]
 }

 def bodyMap = [
 mobileHeadline : notificationInstance.mobileHeadline,
 push : notificationInstance.push,
 sticky : notificationInstance.sticky
 ]

 bodyMap.put("recipients",recipientsList)

 if (notificationInstance?.headline) bodyMap.put("headline", notificationInstance?.headline)
 if (notificationInstance?.body) bodyMap.put("description", notificationInstance?.body)

 def expiresDateFormat = new SimpleDateFormat("YYYY-MM-DDhh:mm:ssZ")
 if (notificationInstance?.expires) bodyMap.put("expires", expiresDateFormat.format(notificationInstance?.expires))

 if (notificationInstance?.destinationLabel) bodyMap.put("destinationLabel", notificationInstance?.destinationLabel)
 if (notificationInstance?.destination) bodyMap.put("destination", notificationInstance?.destination)

 if (notificationInstance?.uuid) bodyMap.put("uuid", notificationInstance?.uuid)

 //println bodyMap
 body = bodyMap

 response.success = { resp, reader ->
 println "Got response: ${resp.statusLine}"
 println "Content-Type: ${resp.headers.'Content-Type'}"
 println reader.text
 println ((reader != null) && (reader.text != null))
 }

 }

 request.withFormat {
 form multipartForm {
 flash.message = message(code: 'default.created.message', args: [message(code: 'notificationInstance.label', default: 'Notification'), notificationInstance.id])
 redirect notificationInstance
 }
 '*' { respond notificationInstance, [status: CREATED] }
 }
 }
**/
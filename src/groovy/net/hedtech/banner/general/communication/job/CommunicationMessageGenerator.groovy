/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.job

import net.hedtech.banner.general.communication.email.CommunicationEmailAddress
import net.hedtech.banner.general.communication.email.CommunicationEmailMessage
import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.email.CommunicationMergedEmailTemplate
import net.hedtech.banner.general.communication.letter.CommunicationLetterMessage
import net.hedtech.banner.general.communication.letter.CommunicationLetterTemplate
import net.hedtech.banner.general.communication.letter.CommunicationMergedLetterTemplate
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.mobile.CommunicationMergedMobileNotificationTemplate
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationMessage
import net.hedtech.banner.general.communication.mobile.CommunicationMobileNotificationTemplate
import net.hedtech.banner.general.communication.template.*
import net.hedtech.banner.general.overall.ThirdPartyAccess
import org.apache.log4j.Logger

import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

/**
 * Communication Message Generator creates the mail merge holder object
 * that eventually gets passed to the communication channel for final delivery.
 */
class CommunicationMessageGenerator implements CommunicationTemplateVisitor {
    def log = Logger.getLogger(this.getClass())

    CommunicationTemplateMergeService communicationTemplateMergeService

    private CommunicationRecipientData recipientData

    CommunicationMessage message

    public CommunicationMessage generate( CommunicationTemplate template, CommunicationRecipientData recipientData ) {
        assert( communicationTemplateMergeService )

        assert( template )
        assert( recipientData )

        this.recipientData = recipientData
        message = null
        template.accept( this )
        message.setDateSent( new Date() )
        return message
    }

    @Override
    void visitEmail(CommunicationEmailTemplate template) {
        CommunicationMergedEmailTemplate mergedEmailTemplate = communicationTemplateMergeService.mergeEmailTemplate( template, recipientData )

        CommunicationEmailMessage emailMessage = new CommunicationEmailMessage();

        if (mergedEmailTemplate.toList && mergedEmailTemplate.toList.trim().length() > 0) {
            emailMessage.setToList( createAddresses( mergedEmailTemplate.toList.trim(), ";" ) );
        }
        emailMessage.setSubjectLine( mergedEmailTemplate.subject );
        emailMessage.setMessageBody( mergedEmailTemplate.content );
        emailMessage.setMessageBodyContentType( "text/html; charset=UTF-8" );

        message = emailMessage
    }

    @Override
    void visitLetter(CommunicationLetterTemplate template) {
        CommunicationMergedLetterTemplate mergedLetterTemplate = communicationTemplateMergeService.mergeLetterTemplate( template, recipientData )

        CommunicationLetterMessage letterMessage = new CommunicationLetterMessage()
        letterMessage.toAddress = mergedLetterTemplate.toAddress
        letterMessage.content = mergedLetterTemplate.content
        this.message = letterMessage
    }

    @Override
    void visitMobileNotification(CommunicationMobileNotificationTemplate template) {
        CommunicationMergedMobileNotificationTemplate mergedMobileNotificationTemplate = communicationTemplateMergeService.mergeMobileTemplate( template, recipientData )

        CommunicationMobileNotificationMessage mobileNotificationMessage = new CommunicationMobileNotificationMessage(
            mobileHeadline: mergedMobileNotificationTemplate.mobileHeadline,
            headline: mergedMobileNotificationTemplate.headline,
            messageDescription: mergedMobileNotificationTemplate.messageDescription,
            destinationLink: mergedMobileNotificationTemplate.destinationLink,
            destinationLabel: mergedMobileNotificationTemplate.destinationLabel,
            expirationPolicy: template.expirationPolicy,
            duration: template.duration,
            durationUnit: template.durationUnit,
            expirationDateTime: template.expirationDateTime,
            push: template.push,
            sticky: template.sticky,
            referenceId: recipientData.referenceId,
            externalUser: fetchExternalLoginIdByPidm( recipientData.pidm ),
        )

        message = mobileNotificationMessage
    }

    /**
     * Returns the login id that will be submitted to the mobile server.
     *
     * @param pidm the pidm of the recipient
     * @return the third party user name if it exists; null otherwise.
     */
    static String fetchExternalLoginIdByPidm( Long pidm ) {
        assert( pidm )
        ThirdPartyAccess thirdPartyAccess = ThirdPartyAccess.findByPidm( Integer.valueOf( pidm.intValue() ) )
        return thirdPartyAccess?.externalUser
    }

    /**
     * Creates a HashSet of EmailAddresses from a string containing email addresses separated by the separator passed.
     * @param listOfEmails string containing email addresses
     * @param separator the separator for the email addresses in the listOfEmails
     * @return HashSet&lt;EmailAddress&gt; unique set of EmailAddress objects created from the listOfEmails
     * @throws javax.mail.internet.AddressException thrown if the email address in listOfEmails is not conforming to 'RFC 822'
     */
    private HashSet<CommunicationEmailAddress> createAddresses( String listOfEmails, String separator ) throws AddressException {
        HashSet<CommunicationEmailAddress> emailAddresses = new HashSet<CommunicationEmailAddress>();
        String[] tempEA = listOfEmails.trim().split( separator );
        CommunicationEmailAddress emailAddress;

        for (String string : tempEA) {

            emailAddress = new CommunicationEmailAddress();

            try {
                // Try to parse the personal display name out of the address first.
                InternetAddress[] internetAddresses = InternetAddress.parse( string.trim(), true );
                if (internetAddresses.length == 1) {
                    InternetAddress internetAddress = internetAddresses[0];
                    emailAddress.setMailAddress( internetAddress.getAddress() );
                    emailAddress.setDisplayName( internetAddress.getPersonal() );
                } else {
                    if (log.isDebugEnabled()) log.debug( "Did not find exactly one internet address parsing: " + string );
                    emailAddress.setMailAddress( string.trim() );
                }
            } catch (AddressException e) {
                if (log.isDebugEnabled()) log.debug( "AddressException attempting to parse: " + string );
                emailAddress.setMailAddress( string.trim() );
            }

            emailAddresses.add( emailAddress );
        }

        return emailAddresses;
    }
}

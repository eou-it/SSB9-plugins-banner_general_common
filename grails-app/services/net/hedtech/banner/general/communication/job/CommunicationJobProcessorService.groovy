package net.hedtech.banner.general.communication.job

import net.hedtech.banner.general.communication.email.CommunicationEmailAddress
import net.hedtech.banner.general.communication.email.CommunicationEmailMessage
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.template.CommunicationMergedEmailTemplate
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.tools.ant.taskdefs.email.EmailAddress

import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

/**
 * CommunicationJobProcessorService is responsible for orchestrating the communication send in behalf of a communication job.
 */
class CommunicationJobProcessorService {
    private final Log log = LogFactory.getLog(this.getClass());
    def communicationJobService
    def communicationTemplateMergeService
    def communicationSendEmailService
    def communicationTemplateService
    def communicationOrganizationService

    public void performCommunicationJob( Long jobId ) {
        log.debug( "performed communication job with job id = ${jobId}." )

        CommunicationJob job = communicationJobService.get( jobId )
        List<CommunicationRecipientData> recipientDatas = CommunicationRecipientData.fetchByReferenceId( job.referenceId )
        CommunicationRecipientData recipientData = recipientDatas.size() ? recipientDatas[0] : null
        CommunicationEmailTemplate emailTemplate = communicationTemplateService.get( recipientData.templateId ) as CommunicationEmailTemplate
        CommunicationMergedEmailTemplate mergedEmailTemplate = communicationTemplateMergeService.mergeEmailTemplate( emailTemplate, recipientData )

        CommunicationEmailMessage emailMessage = createEmailMessage( mergedEmailTemplate )
        communicationSendEmailService.sendEmail( recipientData.organization, emailMessage, recipientData, recipientData.pidm )
    }

    private CommunicationEmailMessage createEmailMessage( CommunicationMergedEmailTemplate mergedEmailTemplate ) throws AddressException {
        CommunicationEmailMessage emailMessage = new CommunicationEmailMessage();

        // create a set of EmailAddress for fromList
//            if (getFromList() != null && getFromList().trim().length() > 0) {
//                emailMessage.setSenders( createAddresses( getFromList().trim(), ";" ) );
//            }

        // create a set of EmailAddress for toList
        if (mergedEmailTemplate.toList && mergedEmailTemplate.toList.trim().length() > 0) {
            emailMessage.setToList( createAddresses( mergedEmailTemplate.toList.trim(), ";" ) );
        }

//        // create a set of EmailAddress for ccList
//        if (getCcList() != null && getCcList().trim().length() > 0) {
//            emailMessage.setCcList( createAddresses( getCcList().trim(), ";" ) );
//        }
//
//        // create a set of EmailAddress for bccList
//        if (getBccList() != null && getBccList().trim().length() > 0) {
//            emailMessage.setBccList( createAddresses( getBccList().trim(), ";" ) );
//        }

        emailMessage.setSubjectLine( mergedEmailTemplate.subject );
        emailMessage.setMessageBody( mergedEmailTemplate.content );
        emailMessage.setMessageBodyContentType( "text/html; charset=UTF-8" );
        emailMessage.setDateSent( new Date() );

        return emailMessage;
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

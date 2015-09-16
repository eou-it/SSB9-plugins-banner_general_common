/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.mobile

import net.hedtech.banner.general.communication.email.CommunicationEmailMessage
import net.hedtech.banner.general.communication.email.CommunicationSendEmailMethod
import net.hedtech.banner.general.communication.item.CommunicationEmailItem
import net.hedtech.banner.general.communication.item.CommunicationMobileNotificationItem
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Provides a service for submitting a mobile notification.
 */
class CommunicationSendMobileNotificationService {
    private Log log = LogFactory.getLog( this.getClass() )
    def communicationMobileNotificationService
    def communicationOrganizationService
    def sessionFactory
    def asynchronousBannerAuthenticationSpoofer


    /**
     * Sends a mobile notification message (single) based on the contents of mobile notification message passed.
     *
     * @param organization the organization address config to use for obtaining configuration and authentication credentials
     * @param message the mobile notification message to send
     * @param recipientData a recipient data describing details of the target recipient
     */
    public void send( CommunicationOrganization organization, CommunicationMobileNotificationMessage message, CommunicationRecipientData recipientData ) {
        log.debug( "sending email message" )

        if (organization?.encryptedMobileApplicationKey) {
            organization.clearMobileApplicationKey = communicationOrganizationService.decryptPassword( organization.encryptedMobileApplicationKey )
        }
        asynchronousBannerAuthenticationSpoofer.setMepProcessContext(sessionFactory.currentSession.connection(), recipientData.mepCode )
        CommunicationSendEmailMethod sendEmailMethod = new CommunicationSendMobileNotificationMethod( message, organization );
        sendEmailMethod.execute();

        try {
            track( organization, message, recipientData )
        } catch (Throwable t) {
            log.error( t )
            throw t;
        } finally {
        }
    }


    private void track( CommunicationOrganization organization, CommunicationMobileNotificationMessage message, CommunicationRecipientData recipientData ) {
        log.debug( "tracking mobile notification message sent")
        CommunicationMobileNotificationItem item = new CommunicationMobileNotificationItem()
        // standard communication log entries
        item.setOrganizationId( organization.id )
        item.setReferenceId( recipientData.getReferenceId() )
        item.setRecipientPidm( recipientData.pidm )
        item.setCreatedBy( recipientData.ownerId )
        item.setSentDate( message.dateSent )
        item.setTemplateId(recipientData.templateId)

        // mobile notification personalized properties
        item.mobileHeadline = message.mobileHeadline
        item.headline = message.headline
        item.messageDescription = message.messageDescription
        item.destinationLink = message.destinationLink
        item.destinationLabel = message.destinationLabel

        // mobile notification other properties
        item.expirationPolicy = message.expirationPolicy
        item.elapsedTimeSeconds = message.elapsedTimeSeconds
        item.expirationDateTime = message.expirationDateTime

        item = communicationMobileNotificationService.create( item )
        log.debug( "recorded mobile notification item sent with item id = ${item.id}." )
    }

}

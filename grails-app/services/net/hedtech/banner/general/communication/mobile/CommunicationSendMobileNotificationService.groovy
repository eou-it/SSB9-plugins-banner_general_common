/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.mobile

import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Provides a service for submitting a mobile notification.
 */
class CommunicationSendMobileNotificationService {
    private Log log = LogFactory.getLog( this.getClass() )
    def communicationMobileNotificationItemService
    def communicationOrganizationService
    def sessionFactory
    def asynchronousBannerAuthenticationSpoofer
    def testOverride


    /**
     * Sends a mobile notification message (single) based on the contents of mobile notification message passed.
     *
     * @param organization the organization address config to use for obtaining configuration and authentication credentials
     * @param message the mobile notification message to send
     * @param recipientData a recipient data describing details of the target recipient
     */
    public void send( Long organizationId, CommunicationMobileNotificationMessage message, CommunicationRecipientData recipientData ) {
        log.debug( "sending mobile notification message" )

        asynchronousBannerAuthenticationSpoofer.setMepProcessContext(sessionFactory.currentSession.connection(), recipientData.mepCode )

        CommunicationOrganization senderOrganization = CommunicationOrganization.fetchById(organizationId)
        if (testOverride) {
            message.externalUser = testOverride.externalUser
        }

        CommunicationSendMobileNotificationMethod notificationMethod = new CommunicationSendMobileNotificationMethod( communicationOrganizationService: communicationOrganizationService );
        notificationMethod.execute( message, senderOrganization )

        try {
            track( senderOrganization, message, recipientData, notificationMethod.serverResponse )
        } catch (Throwable t) {
            log.error( t )
            throw t;
        } finally {
        }
    }


    private void track( CommunicationOrganization organization, CommunicationMobileNotificationMessage message, CommunicationRecipientData recipientData, String serverResponse ) {
        log.debug( "tracking mobile notification message sent")
        CommunicationMobileNotificationItem item = new CommunicationMobileNotificationItem()
        // standard communication log entries
        item.setOrganizationId( organization.id )
        item.setReferenceId( recipientData.getReferenceId() )
        item.setRecipientPidm( recipientData.pidm )
        item.setCreatedBy( recipientData.ownerId )
        item.setSentDate( message.dateSent )
        item.setTemplateId(recipientData.templateId)

        item.serverResponse = serverResponse

        item = communicationMobileNotificationItemService.create( item )
        log.debug( "recorded mobile notification item sent with item id = ${item.id}." )
    }

}

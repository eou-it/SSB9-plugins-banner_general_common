/*******************************************************************************
 Copyright 2015-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.mobile

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.general.communication.job.CommunicationMessageGenerator
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


    void sendTest(Long organizationId, Long pidm, Map messageData) {
        CommunicationOrganization senderOrganization = CommunicationOrganization.fetchById(organizationId)
        if (!senderOrganization)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.organizationNotFound"), CommunicationErrorCode.ORGANIZATION_NOT_FOUND .name())

        // check if child has no settings then look at root settings
        if (!senderOrganization.mobileEndPointUrl && !senderOrganization.mobileEndPointUrl && !senderOrganization.clearMobileApplicationKey && !senderOrganization.encryptedMobileApplicationKey) {
            CommunicationOrganization root = CommunicationOrganization.fetchRoot()
            if (!root)
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.organizationNotFound"), CommunicationErrorCode.ORGANIZATION_NOT_FOUND.name())
            senderOrganization.encryptedMobileApplicationKey = root.encryptedMobileApplicationKey
            senderOrganization.clearMobileApplicationKey = root.clearMobileApplicationKey
            senderOrganization.mobileEndPointUrl = root.mobileEndPointUrl
            senderOrganization.mobileApplicationName = root.mobileApplicationName
        }

        CommunicationRecipientData recipientData = createCommunicationRecipientData(pidm, organizationId)
        try {
            sendTestImpl(senderOrganization, recipientData, messageData)
        } catch (ApplicationException e) {
            log.error(e)
            // re-wrap unknown error with better message
            if (e.type == 'UNKNOWN_ERROR')
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.unknownMobile"), CommunicationErrorCode.UNKNOWN_ERROR_MOBILE.name())
            throw e
        } catch (Throwable e) {
            // catch any additional / unexpected exceptions
            log.error(e)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.unknownMobile"), CommunicationErrorCode.UNKNOWN_ERROR_MOBILE.name())
        }
    }

    private static CommunicationRecipientData createCommunicationRecipientData(Long pidm, Long organizationId) {

        return new CommunicationRecipientData(
                pidm: pidm,
                referenceId: UUID.randomUUID().toString(),
                ownerId: -1,
                fieldValues: null,
                organizationId: organizationId,
                communicationChannel: CommunicationChannel.MOBILE_NOTIFICATION,
        )
    }

    void sendTestImpl(CommunicationOrganization senderOrganization, CommunicationRecipientData recipientData, Map messageData) {
        log.debug( "sending mobile test notification message" )
        checkOrg(senderOrganization)
        checkRecipientData(recipientData)
        if (!testOverride)
            asynchronousBannerAuthenticationSpoofer.setMepProcessContext(sessionFactory.currentSession.connection(), recipientData.mepCode )

        CommunicationMobileNotificationMessage message = createTestMessage(recipientData, messageData)

        CommunicationSendMobileNotificationMethod notificationMethod = new CommunicationSendMobileNotificationMethod( communicationOrganizationService: communicationOrganizationService );

        // to override not having externalUser with no thirdparty set up
        if (testOverride)
            message.externalUser = testOverride.externalUser

        try {
            notificationMethod.execute( message, senderOrganization )
        } catch (ApplicationException e) {
            log.error(e)
            if (e.type == 'UNKNOWN_ERROR')
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.unknownMobile"), CommunicationErrorCode.UNKNOWN_ERROR_MOBILE.name())
            throw e
        } catch (Throwable t) {
            // check for unexpected exceptions
            log.error( t )
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.unknownMobile"), CommunicationErrorCode.UNKNOWN_ERROR_MOBILE.name())
        }
    }

    private static CommunicationMobileNotificationMessage createTestMessage (CommunicationRecipientData recipientData, Map messageData) {
        // create preset static message for mobile test notifications
        CommunicationMobileNotificationMessage mobileNotificationMessage = new CommunicationMobileNotificationMessage(
                mobileHeadline: messageData.mobileHeadline,
                headline: messageData.headline,
                messageDescription: messageData.description,
                destinationLink: null,
                destinationLabel: null,
                expirationPolicy: CommunicationMobileNotificationExpirationPolicy.DURATION,
                duration: 7,
                durationUnit: 'DAY',
                expirationDateTime: null,
                push: true,
                sticky: false,
                referenceId: recipientData.referenceId,
                externalUser: CommunicationMessageGenerator.fetchExternalLoginIdByPidm(recipientData.pidm),
        )
        return mobileNotificationMessage
    }

    private static void checkOrg (CommunicationOrganization org) {
        if (org == null)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.organizationNotFound"), CommunicationErrorCode.ORGANIZATION_NOT_FOUND.name())
        if (org.mobileEndPointUrl == null || org.mobileEndPointUrl.trim().length() == 0)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.mobileEndPointUrlNotFound"), CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_ENDPOINT_URL.name())
        if (org.encryptedMobileApplicationKey == null && org.clearMobileApplicationKey == null)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.mobileApplicationKeyNotFound"), CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_APPLICATION_KEY.name())
        if (org.mobileApplicationName == null)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.mobileApplicationNameNotFound"), CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_APPLICATION_NAME.name())
    }

    private static void checkRecipientData(CommunicationRecipientData recipientData) {
        if (recipientData == null)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.externalUserInvalid"), CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_EXTERNAL_USER.name())
        if (recipientData.pidm <= 0)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.externalUserInvalid"), CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_EXTERNAL_USER.name())
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

    // helper method to get external ID  for display on testMobileNotification modal
    def fetchExternalId (Long pidm) {
        return CommunicationMessageGenerator.fetchExternalLoginIdByPidm(pidm)
    }

}

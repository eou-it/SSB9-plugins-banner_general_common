/*******************************************************************************
 Copyright 2015-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.mobile

import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.general.communication.job.CommunicationMessageGenerator
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.system.LetterProcessLetter

/**
 * Provides a service for submitting a mobile notification.
 */
@Slf4j
@Transactional
class CommunicationSendMobileNotificationService {
    //private Log log = LogFactory.getLog( this.getClass() )
    def communicationMobileNotificationItemService
    def communicationOrganizationService
    def sessionFactory
    def asynchronousBannerAuthenticationSpoofer
    def testOverride
    def communicationInteractionCompositeService
    def communicationGurmailTrackingService

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
            log.error( t.message )
            throw t;
        } finally {
        }
    }


    void sendTest(Long organizationId, Long pidm, Map messageData) {
        CommunicationOrganization senderOrganization = CommunicationOrganization.fetchById(organizationId)
        if (!senderOrganization)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.organizationNotFound"), CommunicationErrorCode.ORGANIZATION_NOT_FOUND .name())

        CommunicationRecipientData recipientData = createCommunicationRecipientData(pidm, organizationId)
        try {
            sendTestImpl(senderOrganization, recipientData, messageData)
        } catch (ApplicationException e) {
            log.error(e.message)
            // re-wrap unknown error with better message
            if (e.type == 'UNKNOWN_ERROR')
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.unknownMobile"), CommunicationErrorCode.UNKNOWN_ERROR_MOBILE.name())
            throw e
        } catch (Throwable e) {
            // catch any additional / unexpected exceptions
            log.error(e.message)
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
            log.error(e.message)
            if (e.type == 'UNKNOWN_ERROR')
                throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.unknownMobile"), CommunicationErrorCode.UNKNOWN_ERROR_MOBILE.name())
            throw e
        } catch (Throwable t) {
            // check for unexpected exceptions
            log.error( t.message )
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
        CommunicationOrganization root = CommunicationOrganization.fetchRoot()
        if (org == null || root == null)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.organizationNotFound"), CommunicationErrorCode.ORGANIZATION_NOT_FOUND.name())
        if (root.mobileEndPointUrl == null || root.mobileEndPointUrl?.trim().length() == 0)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.mobileEndPointUrlNotFound"), CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_ENDPOINT_URL.name())
        if (root.encryptedMobileApplicationKey == null)
            throw CommunicationExceptionFactory.createApplicationException(CommunicationSendMobileNotificationService.class, new RuntimeException("communication.error.message.mobileApplicationKeyNotFound"), CommunicationErrorCode.EMPTY_MOBILE_NOTIFICATION_APPLICATION_KEY.name())
        if (root.mobileApplicationName == null)
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

        if(Holders?.config.communication.bacsEnabled || Holders?.config.communication.bannerMailTrackingEnabled) {
            communicationGurmailTrackingService.trackGURMAIL(recipientData, item, message)
        }

        log.debug( "recorded mobile notification item sent with item id = ${item.id}." )
    }

    // helper method to get external ID  for display on testMobileNotification modal
    def fetchExternalId (String bannerId) {
        def person = communicationInteractionCompositeService.getPersonOrNonPerson(bannerId)
        if (person.pidm == null || person.pidm <= 0) {
            throw new ApplicationException(CommunicationSendMobileNotificationService,"@@r1:idInvalid@@")
        }
        return CommunicationMessageGenerator.fetchExternalLoginIdByPidm(person.pidm)
    }

}

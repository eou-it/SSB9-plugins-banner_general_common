/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.letter

import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.system.LetterProcessLetter

/**
 * Provides a service for generating a letter.
 */
@Slf4j
@Transactional
class CommunicationGenerateLetterService {
    //private Log log = LogFactory.getLog( this.getClass() )
    def communicationLetterItemService
    def mailService
    def sessionFactory
    def asynchronousBannerAuthenticationSpoofer
    def testTemplate = false
    def communicationGurmailTrackingService

    /**
     * Sends a letter message (single) based on the contents of letter message passed.
     *
     * @param organization the organization address config to use for obtaining configuration and authentication credentials
     * @param message the letter message to send
     * @param recipientData a recipient data describing details of the target recipient
     */
    def send( Long organizationId, CommunicationLetterMessage message, CommunicationRecipientData recipientData ) {
        log.debug( "creating letter message" )

        asynchronousBannerAuthenticationSpoofer.setMepProcessContext(sessionFactory.currentSession.connection(), recipientData.mepCode )

        CommunicationOrganization senderOrganization = CommunicationOrganization.fetchById(organizationId)

        try {
            def response = track( senderOrganization, message, recipientData )
            if (testTemplate) {
                testTemplate = !testTemplate
                return response
            }
        } catch (Throwable t) {
            log.error( t.getMessage() )
            throw t;
        } finally {
        }
    }


    def track( CommunicationOrganization organization, CommunicationLetterMessage message, CommunicationRecipientData recipientData ) {
        log.debug( "tracking letter message sent")

        if (isEmpty(message.toAddress)) {
            throw CommunicationExceptionFactory.createFriendlyApplicationException(CommunicationGenerateLetterService.class,
                CommunicationErrorCode.EMPTY_LETTER_TO_ADDRESS,
                "emptyLetterToAddress"
            )
        }

        if (isEmpty(message.content)) {
            throw CommunicationExceptionFactory.createFriendlyApplicationException(CommunicationGenerateLetterService.class,
                CommunicationErrorCode.EMPTY_LETTER_CONTENT,
                "emptyLetterContent"
            )
        }

        CommunicationLetterItem item = new CommunicationLetterItem()
        // standard communication log entries
        item.setOrganizationId( organization.id )
        item.setReferenceId( recipientData.getReferenceId() )
        item.setRecipientPidm( recipientData.pidm )
        item.setCreatedBy( recipientData.ownerId )
        item.setSentDate( message.dateSent )
        item.setTemplateId(recipientData.templateId)

        // letter specific fields
        item.toAddress = message.toAddress
        item.content = message.content
        item.style = message.style

        item = communicationLetterItemService.create( item )

        if(Holders?.config.communication.bacsEnabled || Holders?.config.communication.bannerMailTrackingEnabled) {
            communicationGurmailTrackingService.trackGURMAIL(recipientData, item, message)
        }

        log.debug( "recorded letter item sent with item id = ${item.id}." )
        if (testTemplate)
            return item.id
    }



    private boolean isEmpty(String s) {
        return (s == null) || s.trim().length() == 0
    }

}

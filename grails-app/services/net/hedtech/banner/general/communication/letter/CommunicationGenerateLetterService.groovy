/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.letter

import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Provides a service for generating a letter.
 */
class CommunicationGenerateLetterService {
    private Log log = LogFactory.getLog( this.getClass() )
    def communicationLetterItemService
    def sessionFactory
    def asynchronousBannerAuthenticationSpoofer


    /**
     * Sends a letter message (single) based on the contents of letter message passed.
     *
     * @param organization the organization address config to use for obtaining configuration and authentication credentials
     * @param message the letter message to send
     * @param recipientData a recipient data describing details of the target recipient
     */
    public void send( Long organizationId, CommunicationLetterMessage message, CommunicationRecipientData recipientData ) {
        log.debug( "creating letter message" )

        asynchronousBannerAuthenticationSpoofer.setMepProcessContext(sessionFactory.currentSession.connection(), recipientData.mepCode )

        CommunicationOrganization senderOrganization = CommunicationOrganization.fetchById(organizationId)

        try {
            track( senderOrganization, message, recipientData )
        } catch (Throwable t) {
            log.error( t )
            throw t;
        } finally {
        }
    }


    private void track( CommunicationOrganization organization, CommunicationLetterMessage message, CommunicationRecipientData recipientData ) {
        log.debug( "tracking letter message sent")

        if (isEmpty(message.toAddress)) {
            throw CommunicationExceptionFactory.createFriendlyApplicationException(CommunicationGenerateLetterService.class,
                CommunicationErrorCode.EMPTY_LETTER_TO_ADDRESS.toString(),
                "emptyLetterToAddress"
            )
        }

        if (isEmpty(message.content)) {
            throw CommunicationExceptionFactory.createFriendlyApplicationException(CommunicationGenerateLetterService.class,
                CommunicationErrorCode.EMPTY_LETTER_CONTENT.toString(),
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

        item = communicationLetterItemService.create( item )
        log.debug( "recorded letter item sent with item id = ${item.id}." )
    }

    private boolean isEmpty(String s) {
        return !s || s.length() == 0
    }

}

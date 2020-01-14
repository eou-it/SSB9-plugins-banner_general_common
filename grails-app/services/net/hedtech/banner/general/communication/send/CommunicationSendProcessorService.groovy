/********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
package net.hedtech.banner.general.communication.send

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import net.hedtech.banner.general.communication.item.CommunicationSendDispatcher
import net.hedtech.banner.general.communication.item.CommunicationSendItem
import net.hedtech.banner.general.communication.job.CommunicationMessageDispatcher

/**
 * CommunicationSendProcessorService is responsible for orchestrating the communication send in behalf of a communication job.
 */
@Slf4j
@Transactional
class CommunicationSendProcessorService {
    def communicationSendEmailService
    def communicationSendMobileNotificationService
    def communicationGenerateLetterService
    def communicationSendTextMessageService

    public void performSendCommunication( Long sendItemId ) {
        log.debug( "performed communication send item with id = ${sendItemId}." )


        CommunicationSendItem sendItem = CommunicationSendItem.get( sendItemId )
        CommunicationSendDispatcher sendDispatcher = new CommunicationSendDispatcher(
                communicationSendEmailService: communicationSendEmailService,
                communicationSendMobileNotificationService: communicationSendMobileNotificationService,
                communicationGenerateLetterService: communicationGenerateLetterService,
                communicationSendTextMessageService: communicationSendTextMessageService
        )
        sendDispatcher.dispatch( sendItem )
    }

}

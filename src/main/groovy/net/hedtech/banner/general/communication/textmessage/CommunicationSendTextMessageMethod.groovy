/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.textmessage

import groovy.util.logging.Slf4j

/**
 * Performs the details of assembling and sending out a text message (SMS).
 */
@Slf4j
class CommunicationSendTextMessageMethod {

    private CommunicationTextMessage textMessage;

    CommunicationSendTextMessageMethod(CommunicationTextMessage textMessage ) {
        this.textMessage = textMessage;
    }

}
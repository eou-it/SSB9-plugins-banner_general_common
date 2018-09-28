/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.textmessage

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.template.CommunicationMessage

/**
 * Represents a Text Message entity with placeholders for attributes of a typical message
 * as sent from communication management.
 */
@SuppressWarnings("serial")
@EqualsAndHashCode
@ToString
class CommunicationTextMessage extends CommunicationMessage {

    String textMessage

    String footer

    String destinationLink

    String destinationLabel
}

/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.letter

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.email.CommunicationEmailAddress
import net.hedtech.banner.general.communication.template.CommunicationMessage

/**
 * Represents an Letter Message entity with placeholders for attributes of a typical letter message.
 */
@SuppressWarnings("serial")
@EqualsAndHashCode(includeFields = true)
@ToString
public class CommunicationLetterMessage extends CommunicationMessage {
    String toAddress
    String content
}

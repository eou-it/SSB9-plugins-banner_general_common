/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.mobile

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.email.CommunicationEmailAddress
import net.hedtech.banner.general.communication.template.CommunicationMessage

/**
 * Represents an Mobile Notification Message entity with placeholders for attributes of a typical message
 * as sent from communication management.
 */
@SuppressWarnings("serial")
@EqualsAndHashCode
@ToString
public class CommunicationMobileNotificationMessage implements CommunicationMessage {
    String guid
    String mobileHeadline
    String headline
    String description
    String destinationLink
    String destinationLabel
}

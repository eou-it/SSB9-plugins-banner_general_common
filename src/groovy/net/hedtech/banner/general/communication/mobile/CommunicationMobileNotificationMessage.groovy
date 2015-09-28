/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.mobile

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.email.CommunicationEmailAddress
import net.hedtech.banner.general.communication.template.CommunicationDurationUnit
import net.hedtech.banner.general.communication.template.CommunicationMessage
import net.hedtech.banner.general.communication.template.CommunicationMobileNotificationExpirationPolicy

/**
 * Represents an Mobile Notification Message entity with placeholders for attributes of a typical message
 * as sent from communication management.
 */
@SuppressWarnings("serial")
@EqualsAndHashCode
@ToString
public class CommunicationMobileNotificationMessage extends CommunicationMessage {
    String mobileHeadline
    String headline
    String messageDescription
    String destinationLink
    String destinationLabel
    CommunicationMobileNotificationExpirationPolicy expirationPolicy
    Long duration
    CommunicationDurationUnit durationUnit
    Date expirationDateTime
    boolean push
    boolean sticky
    String referenceId
    String externalUser
}

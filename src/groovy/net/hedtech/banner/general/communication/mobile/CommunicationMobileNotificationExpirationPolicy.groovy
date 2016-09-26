/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.mobile

/**
 * CommunicationMobileNotificationExpirationPolicy describes how the message expiration will be set.
 */
public enum CommunicationMobileNotificationExpirationPolicy implements Serializable {
    NO_EXPIRATION,
    DURATION,
    DATE_TIME;
}

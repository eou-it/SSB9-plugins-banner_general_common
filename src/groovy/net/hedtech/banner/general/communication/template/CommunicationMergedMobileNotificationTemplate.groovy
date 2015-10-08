package net.hedtech.banner.general.communication.template

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Represents the final merged template after all the recipient data is applied to the template.
 * This is the object that will be send to the email service.
 */
@ToString
@EqualsAndHashCode
class CommunicationMergedMobileNotificationTemplate implements Serializable {
    String mobileHeadline
    String headline
    String messageDescription
    String destinationLink
    String destinationLabel
}

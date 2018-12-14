/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.textmessage

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Represents the final merged template after all the recipient data is applied to the template.
 * This is the object that will be send to the SMS service.
 */
@ToString
@EqualsAndHashCode
class CommunicationMergedTextMessageTemplate {
    String toList
    String footer
    String message
    String destinationLink
    String destinationLabel
}

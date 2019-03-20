/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * A Communication Request is a request to send a single template based communication
 * to a specific pidm based recipient in behalf of a sender.
 *
 * Implementation note: the named object was favored over a map in order to better
 * describe the required attributes involved and follow the custom from BRM.
 */
@EqualsAndHashCode
@ToString
class CommunicationRequest implements Serializable {

    /** Optional correlation id for tracking the communication externally. **/
    public String referenceId
    /** The pidm of the recipient the communication is targeted towards. **/
    public long recipientPidm
    /** The id of the template to send. **/
    public templateId
    /** The id of the organization describing the sender details. **/
    public organizationId
    /** The Oracle user name of the person or entity submitting that submitted the request. **/
    public String initiatorUserId
}

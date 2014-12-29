/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * A group send communication request.
 *
 * The sender banner id and sender map code will be inferred by the caller.
 *
 * @author Michael Brzycki
 */
@EqualsAndHashCode
@ToString
class CommunicationGroupSendRequest implements Serializable {
    Long populationId
    Long templateId
    Long organizationId
}

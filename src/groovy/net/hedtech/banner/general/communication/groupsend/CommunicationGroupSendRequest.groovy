/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
@EqualsAndHashCode
@ToString
class CommunicationGroupSendRequest implements Serializable {
    String name
    Long populationId
    Long templateId
    Long organizationId
    String referenceId
    Calendar scheduledStartDate
    Boolean recalculateOnSend
}

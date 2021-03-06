/*******************************************************************************
 Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.parameter.CommunicationParameter
import net.hedtech.banner.general.communication.parameter.CommunicationParameterType

@EqualsAndHashCode
@ToString
class CommunicationGroupSendRequest implements Serializable {
    String name
    Long populationId
    Long templateId
    Long organizationId
    Long eventId
    String referenceId
    String cronExpression
    String cronTimezone
    Long recurrentMessageId
    Date scheduledStartDate
    Date endDate
    Long noOfOccurrences
    Boolean recalculateOnSend
    Map parameterNameValueMap = [:]
    Long communicationCodeId

    public void setParameter( CommunicationParameter parameter, Object value ) {
        setParameter( parameter.name, value, parameter.type )
    }

    public void setParameter( String name, Object value, CommunicationParameterType type ) {
        parameterNameValueMap.put( name, new CommunicationParameterValue( [value: value, type: type ] ) )
    }
}

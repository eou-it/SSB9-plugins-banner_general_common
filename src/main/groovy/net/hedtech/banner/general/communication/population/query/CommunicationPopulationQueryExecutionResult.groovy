/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population.query

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * A CommunicationPopulationQueryExecutionResult instance serves as a way
 * to pass detailed results back from the execution of a query.
 */
@EqualsAndHashCode
@ToString
class CommunicationPopulationQueryExecutionResult implements Serializable {
    Long selectionListId
    Long calculatedCount
    String calculatedBy
    String errorString
}

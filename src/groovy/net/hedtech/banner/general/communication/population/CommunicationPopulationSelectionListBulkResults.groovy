/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.ToString
import net.hedtech.banner.general.communication.CommunicationErrorCode

/**
 * Captures the results of a batch insert of banner ids to a communication selection list.
 */
@ToString
class CommunicationPopulationSelectionListBulkResults implements Serializable {
    CommunicationPopulation population
    int duplicateCount
    int ignoredCount
    int notExistCount
    int insertedCount
    List<String> bannerIdsNotFound
}
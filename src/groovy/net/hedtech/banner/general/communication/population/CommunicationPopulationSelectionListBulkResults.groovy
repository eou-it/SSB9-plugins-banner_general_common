/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

/**
 * Captures
 */
class CommunicationPopulationSelectionListBulkResults implements Serializable {
    CommunicationPopulation population
    List<CommunicationPopulationSelectionListEntryResult> entryResults = new ArrayList<CommunicationPopulationSelectionListEntryResult>()
}
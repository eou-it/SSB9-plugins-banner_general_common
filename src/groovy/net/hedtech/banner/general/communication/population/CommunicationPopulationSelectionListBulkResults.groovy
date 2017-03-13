/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import net.hedtech.banner.general.communication.CommunicationErrorCode

/**
 * Captures
 */
class CommunicationPopulationSelectionListBulkResults implements Serializable {
    CommunicationPopulation population
    int duplicateCount
    int ignoredCount
    int notExistCount
    int insertedCount
    String notFoundErrorCode = CommunicationErrorCode.BANNER_ID_NOT_FOUND
    List<String> bannerIdsNotFound
}
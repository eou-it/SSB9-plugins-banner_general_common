/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import net.hedtech.banner.general.communication.CommunicationErrorCode

/**
 * Captures
 */
class CommunicationPopulationSelectionListEntryResult implements Serializable {
    int index
    String bannerId
    boolean updated
    CommunicationErrorCode errorCode
    String errorText
}
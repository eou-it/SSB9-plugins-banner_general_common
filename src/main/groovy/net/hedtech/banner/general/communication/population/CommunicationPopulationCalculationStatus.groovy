/** *****************************************************************************
 Copyright 2014-2019 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.communication.population

public enum CommunicationPopulationCalculationStatus {

    SCHEDULED,
    PENDING_EXECUTION,
    ERROR,
    AVAILABLE;

    public Set<CommunicationPopulationCalculationStatus> set() {
        return EnumSet.range( CommunicationPopulationCalculationStatus.SCHEDULED, CommunicationPopulationCalculationStatus.AVAILABLE );
    }

}

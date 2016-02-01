/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.population

class CommunicationPopulationQueryParseResult {
    def status
    def message
    def cost
    def cardinality

    /**
     * Returns true if the status indicates a valid query statement.
     */
    public boolean isValid() {
        return "Y".equals( status )
    }
}

/** *****************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.communication.population.query

public enum CommunicationPopulationQueryExecutionStatus {

    SCHEDULED("S"),
    PENDING_EXECUTION("P"),
    ERROR("E"),
    AVAILABLE("A");

    private String executionStatusCode = null


    private CommunicationPopulationQueryExecutionStatus(String executionStatusCode) {
        this.executionStatusCode = executionStatusCode
    }


    public String getCode() {
        return executionStatusCode;
    }


    public static CommunicationPopulationQueryExecutionStatus getPopulationQueryExecutionStatus(String executionStatusCode) {

        if ("S".equalsIgnoreCase(executionStatusCode)) {
            return SCHEDULED
        } else if ("P".equalsIgnoreCase(executionStatusCode)) {
            return PENDING_EXECUTION
        } else if ("E".equalsIgnoreCase(executionStatusCode)) {
            return ERROR
        } else if ("A".equalsIgnoreCase(executionStatusCode)) {
            return AVAILABLE
        } else {
            throw new IllegalArgumentException("No PopulationQuery Execution Status for the code $executionStatusCode")
        }


    }


}

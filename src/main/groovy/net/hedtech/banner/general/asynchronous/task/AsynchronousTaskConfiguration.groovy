/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous.task;

public class AsynchronousTaskConfiguration {
    
    String jobType;
    int maxQueueSize;
    boolean continuousPolling;
    int pollingInterval;
    boolean disabled;
    boolean deleteSuccessfullyCompleted;
    int maxThreads;
    
    public AsynchronousTaskConfiguration() {
        super();
    }

    /**
     * Returns the jobType attribute value.
     * @return the jobType
     */
    public String getJobType() {
        return jobType;
    }

    /**
     * Sets the jobType attribute.
     * @param jobType the jobType to set
     */
    public void setJobType( String jobType ) {
        this.jobType = jobType;
    }

    /**
     * Returns the maxQueueSize attribute value.
     * @return the maxQueueSize
     */
    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    /**
     * Sets the maxQueueSize attribute.
     * @param maxQueueSize the maxQueueSize to set
     */
    public void setMaxQueueSize( int maxQueueSize ) {
        this.maxQueueSize = maxQueueSize;
    }

    /**
     * Returns the continuousPolling attribute value.
     * @return the continuousPolling
     */
    public boolean isContinuousPolling() {
        return continuousPolling;
    }

    /**
     * Sets the continuousPolling attribute.
     * @param continuousPolling the continuousPolling to set
     */
    public void setContinuousPolling( boolean continuousPolling ) {
        this.continuousPolling = continuousPolling;
    }

    /**
     * Returns the pollingInterval attribute value.
     * @return the pollingInterval
     */
    public int getPollingInterval() {
        return pollingInterval;
    }

    /**
     * Sets the pollingInterval attribute.
     * @param pollingInterval the pollingInterval to set
     */
    public void setPollingInterval( int pollingInterval ) {
        this.pollingInterval = pollingInterval;
    }

    /**
     * Returns the disabled attribute value.
     * @return the disabled
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Sets the disabled attribute.
     * @param disabled the disabled to set
     */
    public void setDisabled( boolean disabled ) {
        this.disabled = disabled;
    }

    /**
     * Returns the deleteSuccessfullyCompleted attribute value.
     * @return the deleteSuccessfullyCompleted
     */
    public boolean isDeleteSuccessfullyCompleted() {
        return deleteSuccessfullyCompleted;
    }

    /**
     * Sets the deleteSuccessfullyCompleted attribute.
     * @param deleteSuccessfullyCompleted the deleteSuccessfullyCompleted to set
     */
    public void setDeleteSuccessfullyCompleted( boolean deleteSuccessfullyCompleted ) {
        this.deleteSuccessfullyCompleted = deleteSuccessfullyCompleted;
    }

    /**
     * Returns the maxThreads attribute value.
     * @return the maxThreads
     */
    public int getMaxThreads() {
        return maxThreads;
    }

    /**
     * Sets the maxThreads attribute.
     * @param maxThreads the maxThreads to set
     */
    public void setMaxThreads( int maxThreads ) {
        this.maxThreads = maxThreads;
    }    
    
}
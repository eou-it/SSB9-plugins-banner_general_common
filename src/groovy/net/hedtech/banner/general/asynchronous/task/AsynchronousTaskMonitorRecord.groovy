/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous.task
/**
 * JobMonitorRecord
 *
 * @author charlie hardt
 */
public class AsynchronousTaskMonitorRecord {

    private final String agentID;
    private final long jobID;
    private long lastUpdate;


    public AsynchronousTaskMonitorRecord( String agentID, long jobID ) {
        this.agentID = agentID;
        this.jobID = jobID;
    }


    public AsynchronousTaskMonitorRecord( String agentID, long jobID, Date update ) {
        this( agentID, jobID );
        lastUpdate = update.getTime();
    }


    public String getAgentID() {
        return agentID;
    }


    public long getJobID() {
        return jobID;
    }


    public long getLastUpdate() {
        return lastUpdate;
    }


    @Override
    public boolean equals( Object o ) {
        if (!o.getClass().getName().equals( this.getClass().getName() )) {
            return false;
        }
        AsynchronousTaskMonitorRecord other = (AsynchronousTaskMonitorRecord) o;
        return other.agentID == agentID && other.jobID == jobID;
    }


    @Override
    public int hashCode() {
        return new Long( agentID ).intValue();
    }


    @Override
    public String toString() {
        return "Agent " + agentID + " is monitoring job #" + jobID;
    }


    public void setLastUpdate( long lastUpdate ) {
        this.lastUpdate = lastUpdate;
    }


}
/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous.task
/**
 * An interface for a job that will be asynchronously processed using a job
 * processing engine.
 *
 * @author charlie hardt
 */
public interface AsynchronousTask extends Serializable {

    /**
     * Returns the primary key for this job.
     * @return PrimaryKey the primary key for this job
     */
    public Long getPrimaryKey();


    /**
     * Returns the creation time of this job.
     * @return Date the creation time
     */
    public Date getCreationTime();

}

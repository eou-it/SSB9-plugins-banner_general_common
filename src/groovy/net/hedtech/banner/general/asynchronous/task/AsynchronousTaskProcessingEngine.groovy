/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous.task;

/**
 * An engine that polls for communication jobs and executes them.
 *
 * @author charlie hardt (very significantly based upon work of Shane Riddell)
 */
public interface AsynchronousTaskProcessingEngine {


    /**
     * Sets the job manager responsible for CRUD and processing of jobs.  Typically,
     * this will be invoked from a JobManager, which sets itself as the manager
     * upon an engine with which it is configured.
     * @param jobManager the job manager for which this processing engine is configured
     */
    public void setJobManager( AsynchronousTaskManager jobManager );


    /**
     * Starts the polling thread and thread pools if they aren't running.  Executes a no-op if they are running, or
     * if a shutdown is in progress.
     */
    public void startRunning();


    /**
     * Sets the manager to stop running.  The manager will not stop immediately,
     * but will wait for all managed threads to cleanly terminate first.
     * Blocks until all threads stop.
     */
    public void stopRunning();


    /**
     * Returns true if the manager's polling thread and thread pools are running.
     * Will be true while the manager is shutting down as well.  Once it returns false, this instance and all its
     * managed threads have cleanly terminated.
     */
    public boolean isRunning();


}

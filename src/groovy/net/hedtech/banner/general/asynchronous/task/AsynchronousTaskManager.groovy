/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous.task

import javax.annotation.security.DenyAll;

import net.hedtech.banner.exceptions.ApplicationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A manager of Jobs that should be executed asynchronously.  This manager performs
 * basic CRUD on jobs that will be executed asynchronously through a polling mechanism
 * based upon a JobProcessingEngine that is configured to support this manager.
 * The AsynchronousTaskManager is a generic
 * service that consumes jobs immediately, and has the potential for loss as the job
 * is solely in memory.
 *
 * @author charlie hardt (very significantly based upon work of Shane Riddell)
 */
@Transactional(readOnly = false, propagation = Propagation.REQUIRED ) //@ServiceBoundary
public interface AsynchronousTaskManager {


    /**
     * Returns the class type of task supported by this manager.
     * @return
     */
    @DenyAll
    public Class getJobType();


    /**
     * Creates a new task within the persistent store, such that it
     * will be asynchronously processed.
     * @param task the communication task to create
     * @return the newly created communication task
     */
    @DenyAll
    public AsynchronousTask create( AsynchronousTask task ) throws ApplicationException;

    /**
     * Deletes an existing task from the persistent store.
     * @param task the task to remove
     */
    @DenyAll
    public void delete( AsynchronousTask task ) throws ApplicationException;


    /**
     * Returns pending tasks, sorted by oldest first.
     * @param max maximum number of tasks to return.
     * @return List<T> the list of pending tasks
     */
    @DenyAll
    public List getPendingJobs( int max ) throws ApplicationException;


    /**
     * Returns tasks that have failed.
     * @return List<CommunicationJob> the failed tasks
     */
    @DenyAll
    public List getFailedJobs();

    /**
     * Acquires the specified task.
     * @return a boolean indicating whether the task could be acquired or not.  If true, the service was
     *         able to mark the task as acquired.  If false, the task was either
     *         already processed, or locked by another thread, and the call returned.
     */
    @DenyAll
    public boolean acquire( AsynchronousTask task ) throws ApplicationException;


    /**
     * Marks the specified task as complete.
     */
    @Transactional //(propagation=Propagation.REQUIRES_NEW, rollbackFor = { Throwable.class })
    @DenyAll    
    public void markComplete( AsynchronousTask task ) throws ApplicationException;


    /**
     * Processes the supplied task.
     */
    @DenyAll
    public void process( AsynchronousTask task ) throws ApplicationException;


    /**
     * Marks a task as having failed.
     * @param task    the task that failed
     * @param errorCode the error code representing the caue of the failure
     * @param cause the cause of the failure
     */
    @DenyAll
    public void markFailed( AsynchronousTask task, String errorCode, Throwable cause ) throws ApplicationException;



    /**
     * Sets the manager to fail processing a task with a simulated exception.
     * Used only for unit testing scenarios.
     * Should never be invoked in a production environment
     * @param cause the exception to throw when processing a task
     */
    @DenyAll
    public void setSimulatedFailureException( Exception cause );


    /**
     * Updates (touches) the supplied task monitor record within the persistent store
     * by updating it's lastUpdate time.  This is called by a monitor thread to
     * indicate that a thread is alive and working on this task.
     * @param monitorRecord the monitor record to update
     * @return JobMonitorRecord the updated monitor record containing the new lastUpdate time
     */
    @DenyAll
    public AsynchronousTaskMonitorRecord updateMonitorRecord( AsynchronousTaskMonitorRecord monitorRecord );

}

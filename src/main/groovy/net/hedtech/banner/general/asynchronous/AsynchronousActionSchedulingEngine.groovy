/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous;


/**
 * An interface for a scheduler service that executes jobs based upon
 * cron expressions.
 *
 * @author charlie hardt
 */
public interface AsynchronousActionSchedulingEngine {

    /**
     * Used as the Job Group for jobs that are used to execute actions.
     */
    public static final String ACTION_EXECUTION_JOB = "ACTION_EXECUTION_JOB";

    /**
     * Used as the schedule/trigger group for all triggers that are used to
     * execute a job immediately.
     */
    public static final String IMMEDIATE_EXECUTION_TRIGGER = "IMMEDIATE_EXECUTION_TRIGGER";

    /**
     * Schedules the action identified by the supplied URI for later execute
     * based upon the supplied cron expression.  Note that the action will be
     * retrieved from an action registry at the scheduled time of execution.
     * The URI could point to a different action at that time.  Also, it is
     * important that the action to be executed be consistent with the supplied
     * execution context.  The action will not be retrieved to perform this
     * validation until the scheduled execution time.  If the action and the
     * execution context are incompatible, an ApplicationException will be
     * thrown.
     * @param actionUri the URI of an action to execute
     * @param executionContext the job-specific execution context for the execution
     * @param cronExpression the cron expression
     * @return String a unique Job name (ID) that can be used to cancel execution
     * throws ApplicationException if the action cannot be executed
     */
    public String schedule( String actionUri, Map executionContext, String cronExpression )


    /**
     * Schedules the action identified by the supplied URI for later execute
     * based upon the supplied execution schedule.  Note that the action will be
     * retrieved from an action registry at the scheduled time of execution.
     * The URI could point to a different action at that time.  Also, it is
     * important that the action to be executed be consistent with the supplied
     * execution context.  The action will not be retrieved to perform this
     * validation until the scheduled execution time.  If the action and the
     * execution context are incompatible, an ApplicationException will be
     * thrown.
     * @param actionUri the URI of an action to execute
     * @param executionContext the job-specific execution context for the execution
     * @param executionSchedule the execution schedule
     * @return String a unique Job name (ID) that can be used to cancel execution
     * throws ApplicationException if the action cannot be executed
     */
    public String schedule( String actionUri, Map executionContext, AsynchronousActionExecutionSchedule schedule )


    /**
     * Unschedules and removes the job identified with the supplied jobName.
     * @param jobName the name of the job to remove
     * throws ApplicationException if the job cannot be found or be removed
     */
    public void unSchedule( String jobName )


    /**
     * Schedules the action identified by the action URI for immediate execution.
     * @param actionUri the URI of an action to execute
     * @param executionContext the job-specific execution context for the execution
     * @return String a unique Job name (ID) that can be used to cancel execution
     * throws ApplicationException if the action cannot be executed
     */
    public String scheduleNow( String actionUri, Map executionContext )


    /**
     * Returns true if the specified job exists and is scheduled.
     * @param jobName the name of the job
     * @return boolean true if the job exists and is scheduled, false otherwise
     */
    public boolean isScheduled( String jobName );

}

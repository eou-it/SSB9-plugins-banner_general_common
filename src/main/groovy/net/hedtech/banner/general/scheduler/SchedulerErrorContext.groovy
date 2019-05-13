/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.scheduler

/**
 * Scheduler Error Context contains the callback parameters
 * passed in from the scheduler needed for error handling cleanup.
 */
class SchedulerErrorContext implements Serializable {
    SchedulerJobContext jobContext
    Throwable cause
}

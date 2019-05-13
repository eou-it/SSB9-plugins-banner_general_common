/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.scheduler

/**
 * Scheduler Job Receipt contains tracking information for a job
 * that was submitted.
 */
class SchedulerJobReceipt implements Serializable {
    String groupId
    String jobId
}

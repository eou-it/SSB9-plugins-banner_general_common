/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.scheduler

/**
 * Stores a simple value for testing purposes.
 */
class SchedulerTestOperation {
    public static Date operationFiredDate
    public static Date operationFailedDate
    public static Date operationCompletedDate

    public static void reset() {
        operationFiredDate = null
        operationFailedDate = null
        operationCompletedDate = null
    }

    public static void markOperationFired() {
        operationFiredDate = new Date()
    }

    public static void markOperationCompleted() {
        operationCompletedDate = new Date()
    }

    public static void markOperationFailed() {
        operationFailedDate = new Date()
    }

    public static boolean didOperationFire() {
        return operationFiredDate != null
    }

    public static boolean didOperationFail() {
        return operationFailedDate != null
    }

    public static boolean didOperationComplete() {
        return operationCompletedDate != null
    }
}

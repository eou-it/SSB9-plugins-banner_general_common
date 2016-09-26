/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.scheduler

/**
 * A pointer to a service method
 */
class SchedulerServiceMethodHandle implements Serializable {
    String service
    String method

    public SchedulerServiceMethodHandle(String service, String method) {
        this.service = service
        this.method = method
    }

    public String toString() {
        return "${service}.${method}"
    }
}

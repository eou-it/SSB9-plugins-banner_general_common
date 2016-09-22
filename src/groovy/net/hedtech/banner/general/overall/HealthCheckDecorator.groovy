/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.overall

/**
 * Decorator the results of a health check
 */
class HealthCheckDecorator {

    //Constants for the Ethos health state
    private static String health_available = "available"
    private static String health_unavailable = "unavailable"

    def status

    /**
     * Create a decorator based on the passed in availabilty
     * @param isAvailable
     */
    HealthCheckDecorator( boolean isAvailable) {
       if (isAvailable){
           status = health_available
       }else{
           status = health_unavailable
       }
    }
}

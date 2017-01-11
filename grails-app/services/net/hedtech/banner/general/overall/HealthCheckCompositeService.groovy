/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.overall

/**
 * Returns the results of a system health check
 */
class HealthCheckCompositeService {


    /**
    * The list function returns an instance of the HealthCheckDecorator
    */
    def list(Map params) {

        //Currently we do not need to call a service to determine health,
        //but we will in a future enhancment
        def isAvailable = true

        return [new HealthCheckDecorator(isAvailable)]
    }

   /**
   * Returns the number of resources returned.
   * At this time, only one health check is returned, therefore this is hardcoded to 1.
   */
    def count() {
       return 1
    }
}

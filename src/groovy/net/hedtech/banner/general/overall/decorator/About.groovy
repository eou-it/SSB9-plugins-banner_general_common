/*********************************************************************************
 Copyright 2009-2013 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.overall.decorator

/**
 * Decorator for the about service
 */
class About {

    def applicationName
    def applicationVersion

    About( Map<String,Object> about) {

        this.applicationName = about.applicationName
        this.applicationVersion = about.applicationVersion
    }


}

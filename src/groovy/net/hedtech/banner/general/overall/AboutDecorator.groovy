/*********************************************************************************
 Copyright 2009-2013 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.overall

/**
 * Decorator for the about service
 */
class AboutDecorator {

    def applicationName
    def applicationVersion

    AboutDecorator( Map<String,Object> about) {

        this.applicationName = about.applicationName
        this.applicationVersion = about.applicationVersion
    }


}

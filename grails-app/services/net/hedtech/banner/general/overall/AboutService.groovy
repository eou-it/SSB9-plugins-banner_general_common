/*********************************************************************************
 Copyright 2009-2013 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.overall


/**
 * Information about the application
 */
class AboutService {
 
    def grailsApplication

    def list(Map params) {
        def appName = grailsApplication?.metadata?.getApplicationName()
        def appVersion = grailsApplication?.metadata?.getApplicationVersion()
        def aboutMap = [applicationName: appName, applicationVersion: appVersion]
        return [new AboutDecorator(aboutMap)]
    }

    def count() {
        return list()?.size()
    }

}

/*********************************************************************************
 Copyright 2009-2013 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.overall


/**
 * Information about the application, versions, plugins, and grails
 */
class AboutService {
 
    def grailsApplication

    def list(Map params) {
        def appName = grailsApplication?.metadata?.getApplicationName()
        def appVersion = grailsApplication?.metadata?.getApplicationVersion()
        def aboutMap = [applicationName: appName, applicationVersion: appVersion]
        return [new net.hedtech.banner.general.overall.decorator.About(aboutMap)]
    }

    def count() {
        return list()?.size()
    }

}

/*******************************************************************************
 Copyright 2011-2018 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package banner.general.common

import grails.plugins.*

class BannerGeneralCommonGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.3.2 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Banner General Common" // Headline display name of the plugin
    def author = "Ellucian"
    def authorEmail = ""
    def description = '''\
This plugin is BannerGeneralCommon.
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/banner-general-common"

    Closure doWithSpring() { {->
        // no-op
    }
    }

    void doWithDynamicMethods() {
        // no-op
    }

    void doWithApplicationContext() {
        // no-op
    }

    void onChange(Map<String, Object> event) {
        // no-op
    }

    void onConfigChange(Map<String, Object> event) {
        // no-op
    }

    void onShutdown(Map<String, Object> event) {
        // no-op
    }
}

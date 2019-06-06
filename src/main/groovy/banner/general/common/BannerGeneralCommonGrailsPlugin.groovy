/*******************************************************************************
 Copyright 2011-2018 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package banner.general.common

import grails.plugins.*
import grails.util.Holders
import net.hedtech.banner.general.asynchronous.AsynchronousBannerAuthenticationSpoofer
import net.hedtech.banner.general.asynchronous.task.AsynchronousTaskProcessingEngineImpl
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendMonitor

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
        asynchronousBannerAuthenticationSpoofer(AsynchronousBannerAuthenticationSpoofer) {
            dataSource = ref('dataSource')
        }

        // Manage the execution state of the group send as a whole
        // This object will scan the group send item records at regular intervals to determine
        // if the group send has completed.
        communicationGroupSendMonitor(CommunicationGroupSendMonitor) { bean ->
            bean.autowire = 'byName'
            bean.initMethod = 'init'
            asynchronousBannerAuthenticationSpoofer = ref('asynchronousBannerAuthenticationSpoofer')
            monitorIntervalInSeconds =  Holders.config.communication?.communicationGroupSendMonitor?.monitorIntervalInSeconds ?: 10
        }

        communicationGroupSendItemProcessingEngine (AsynchronousTaskProcessingEngineImpl) { bean ->
            bean.autowire = 'byName'
            bean.initMethod = 'init'
            jobManager = ref('communicationGroupSendItemTaskManagerService')
            asynchronousBannerAuthenticationSpoofer = ref('asynchronousBannerAuthenticationSpoofer')
            maxThreads = Holders.config.communication?.communicationGroupSendItemProcessingEngine?.maxThreads ?: 10
            maxQueueSize = Holders.config.communication?.communicationGroupSendItemProcessingEngine?.maxQueueSize ?: 5000
            continuousPolling = Holders.config.communication?.communicationGroupSendItemProcessingEngine?.continuousPolling ?: true
            enabled = Holders.config.communication?.communicationGroupSendItemProcessingEngine?.enabled ?: true
            pollingInterval = Holders.config.communication?.communicationGroupSendItemProcessingEngine?.pollingInterval ?: 2000
            deleteSuccessfullyCompleted = Holders.config.communication?.communicationGroupSendItemProcessingEngine?.deleteSuccessfullyCompleted ?: false
        }

        communicationJobProcessingEngine (AsynchronousTaskProcessingEngineImpl) { bean ->
            bean.autowire = 'byName'
            bean.initMethod = 'init'
            jobManager = ref('communicationJobTaskManagerService')
            asynchronousBannerAuthenticationSpoofer = ref('asynchronousBannerAuthenticationSpoofer')
            maxThreads = Holders.config.communication?.communicationJobProcessingEngine?.maxThreads ?: 10
            maxQueueSize = Holders.config.communication?.communicationJobProcessingEngine?.maxQueueSize ?: 5000
            continuousPolling = Holders.config.communication?.communicationJobProcessingEngine?.continuousPolling ?: true
            enabled = Holders.config.communication?.communicationJobProcessingEngine?.enabled ?: true
            pollingInterval = Holders.config.communication?.communicationJobProcessingEngine?.pollingInterval ?: 2000
            deleteSuccessfullyCompleted = Holders.config.communication?.communicationJobProcessingEngine?.deleteSuccessfullyCompleted ?: false
        }
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

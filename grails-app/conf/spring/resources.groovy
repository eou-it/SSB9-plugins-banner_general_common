import grails.util.Holders
import net.hedtech.banner.general.asynchronous.AsynchronousBannerAuthenticationSpoofer
import net.hedtech.banner.general.asynchronous.task.AsynchronousTaskProcessingEngineImpl
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendMonitor

/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

/**
 * Spring bean configuration using Groovy DSL, versus normal Spring XML.
 */
beans = {

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


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
    }

    // BRM also injected a group send item monitor record dao for auditing
    // the thread processing. Not implemented as to dubious need but we may
    // inject this in the future if we find it useful for debugging.

    communicationGroupSendItemProcessingEngine (AsynchronousTaskProcessingEngineImpl) { bean ->
        bean.autowire = 'byName'
        bean.initMethod = 'init'
        jobManager = ref('communicationGroupSendItemTaskManagerService')
        asynchronousBannerAuthenticationSpoofer = ref('asynchronousBannerAuthenticationSpoofer')
        maxThreads = Holders.config.communication?.groupSendEngine?.maxThreads ?: '1'
        maxQueueSize = Holders.config.communication?.groupSendEngine?.maxQueueSize ?: '5000'
        continuousPolling = Holders.config.communication?.groupSendEngine?.continuousPolling ?: 'true'
        pollingInterval = Holders.config.communication?.groupSendEngine?.pollingInterval ?: '2000'
        deleteSuccessfullyCompleted = Holders.config.communication?.groupSendEngine?.deleteSuccessfullyCompleted ?: 'false'
    }

    communicationJobProcessingEngine (AsynchronousTaskProcessingEngineImpl) { bean ->
        bean.autowire = 'byName'
        bean.initMethod = 'init'
        jobManager = ref('communicationJobTaskManagerService')
        asynchronousBannerAuthenticationSpoofer = ref('asynchronousBannerAuthenticationSpoofer')
        maxThreads = Holders.config.communication?.communicationJobEngine?.maxThreads ?: '1'
        maxQueueSize = Holders.config.communication?.communicationJobEngine?.maxQueueSize ?: '5000'
        continuousPolling = Holders.config.communication?.communicationJobEngine?.continuousPolling ?: 'true'
        pollingInterval = Holders.config.communication?.communicationJobEngine?.pollingInterval ?: '2000'
        deleteSuccessfullyCompleted = Holders.config.communication?.communicationJobEngine?.deleteSuccessfullyCompleted ?: 'false'
    }
    
}


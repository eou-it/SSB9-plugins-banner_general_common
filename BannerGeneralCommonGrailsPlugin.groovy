import net.hedtech.banner.general.asynchronous.task.AsynchronousTaskProcessingEngineImpl
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendMonitor
import net.hedtech.banner.general.communication.groupsend.automation.CommunicationGroupSendItemManagerImpl
import net.hedtech.banner.general.asynchronous.AsynchronousBannerAuthenticationSpoofer
import net.hedtech.banner.general.communication.groupsend.automation.CommunicationGroupSendItemProcessingEngineStartupPerformer

/*******************************************************************************
 Copyright 2011-2014 Ellucian Company L.P. and its affiliates.
****************************************************************************** */

/**
 * A Grails Plugin providing cross cutting concerns such as security and database access
 * for Banner web applications.
 * */
class BannerGeneralCommonGrailsPlugin {

    // Note: the groupId 'should' be used when deploying this plugin via the 'grails maven-deploy --repository=snapshots' command,
    // however it is not being picked up.  Consequently, a pom.xml file is added to the root directory with the correct groupId
    // and will be removed when the maven-publisher plugin correctly sets the groupId based on the following field.
    String groupId = "net.hedtech"

    // Note: Using '0.1-SNAPSHOT' (to put a timestamp on the artifact) is not used due to GRAILS-5624 see: http://jira.codehaus.org/browse/GRAILS-5624
    // Until this is resolved, Grails application's that use a SNAPSHOT plugin do not check for a newer plugin release, so that the
    // only way we'd be able to upgrade a project would be to clear the .grails and .ivy2 cache to force a fetch from our Nexus server.
    // Consequently, we'll use 'RELEASES' so that each project can explicitly identify the needed plugin version. Using RELEASES provides
    // more control on 'when' a grails app is updated to use a newer plugin version, and therefore 'could' allow delayed testing within those apps
    // independent of deploying a new plugin build to Nexus.
    //
    String version = "1.0.10"
//    String version = "0.1.0"

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.2.1 > *"

    // the other plugins this plugin depends on
//    def dependsOn = ['springSecurityCore': '1.2']

    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def author = "Ellucian"
    def authorEmail = "actionline@ellucian.com"
    def title = "BannerGeneralCommon Plugin"
    def description = '''This plugin is BannerGeneralCommon.'''//.stripMargin()  // TODO Enable this once we adopt Groovy 1.7.3

    def documentation = "http://sungardhe.com/development/horizon/plugins/general-common"


    def doWithWebDescriptor = { xml ->
        // no-op
    }


    def doWithSpring = {
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
        communicationGroupSendItemManager (CommunicationGroupSendItemManagerImpl) { bean ->
            bean.autowire = 'byName'
            bean.initMethod = 'init'
        }

        communicationGroupSendItemProcessingEngine (AsynchronousTaskProcessingEngineImpl) { bean ->
            bean.autowire = 'byName'
            bean.initMethod = 'init'
            jobManager = ref('communicationGroupSendItemManager')
            asynchronousBannerAuthenticationSpoofer = ref('asynchronousBannerAuthenticationSpoofer')
            maxThreads = '1'
            maxQueueSize = '5000'
            continuousPolling = 'true'
            pollingInterval = '2000'
            deleteSuccessfullyCompleted = 'false'
        }

//        communicationGroupSendItemProcessingEngineStartupPerformer (CommunicationGroupSendItemProcessingEngineStartupPerformer) {
//            jobProcessingEngine = ref('communicationGroupSendItemProcessingEngine')
//        }
    }


    def doWithDynamicMethods = {

    }

    // Register Hibernate event listeners.
    def doWithApplicationContext = {

    }


    def onChange = { event ->
        // no-op
    }


    def onConfigChange = { event ->
        // no-op
    }


}

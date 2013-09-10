import net.hedtech.banner.loginworkflow.UserAgreementFlow
import net.hedtech.banner.loginworkflow.SurveyFlow
import net.hedtech.banner.loginworkflow.SecurityQAFlow

/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
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

    def dependsOn = [ 'bannerCore': '2.5.0 > *']
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
        userAgreementFlow(UserAgreementFlow){ bean ->
            sessionFactory = ref(sessionFactory)

            registerFlowClass = [
                10: "userAgreementFlow"
            ]
        }

        securityQAFlow(SecurityQAFlow) {
            sessionFactory = ref(sessionFactory)
            registerFlowClass = [
                    30: "securityQAFlow"
            ]
        }

        surveyFlow(SurveyFlow) {
            sessionFactory = ref(sessionFactory)
            registerFlowClass = [
                    50: "surveyFlow"
            ]
        }
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

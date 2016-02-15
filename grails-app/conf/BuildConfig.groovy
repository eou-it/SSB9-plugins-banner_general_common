/** *******************************************************************************
 Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.plugin.location.'banner-core' = "../banner_core.git"
grails.plugin.location.'banner-codenarc' = "../banner_codenarc.git"
grails.plugin.location.'banner-general-validation-common' = "../banner_general_validation_common.git"
grails.plugin.location.'banner-general-person' = "../banner_general_person.git"
grails.plugin.location.'banner-seeddata-catalog' = "../banner_seeddata_catalog.git"
grails.plugin.location.'i18n-core' = "../i18n_core.git"

grails.project.dependency.resolver = "ivy" // or maven

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    repositories {
        if (System.properties['PROXY_SERVER_NAME']) {
            mavenRepo "${System.properties['PROXY_SERVER_NAME']}"
        } else {
            grailsPlugins()
            grailsHome()
            grailsCentral()
            mavenCentral()
            mavenRepo "http://repository.jboss.org/maven2/"
            mavenRepo "http://repository.codehaus.org"
        }
    }

    plugins {
        runtime  ":hibernate:3.6.10.10"
        compile ":tomcat:7.0.52.1"
        compile ':resources:1.2.7' // If the functional-test plugin is being used
        compile ":functional-test:2.0.0" // If the functional-test plugin is being used
        test ':code-coverage:2.0.3-3'
        compile ":rest:0.8"
        compile "org.grails.plugins:quartz:1.0.2"
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // runtime 'mysql:mysql-connector-java:5.1.5'
        compile 'org.antlr:ST4:4.0.8'
        compile "javax.mail:javax.mail-api:1.5.1"
        runtime "com.sun.mail:javax.mail:1.5.1"
        compile 'com.icegreen:greenmail:1.3'
        compile 'org.apache.commons:commons-email:1.3.1'
        compile 'org.quartz-scheduler:quartz:2.2.1'
    }


}

// CodeNarc rulesets
codenarc.ruleSetFiles="rulesets/banner.groovy"
codenarc.reportName="target/CodeNarcReport.html"
codenarc.propertiesFile="grails-app/conf/codenarc.properties"

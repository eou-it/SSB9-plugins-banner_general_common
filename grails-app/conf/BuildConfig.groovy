/** *******************************************************************************
 Copyright 2009-2017 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.plugin.location.'banner-general-person' = "../banner_general_person.git"

grails.project.dependency.resolver = "maven" // or ivy

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
        excludes "grails-docs"
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    repositories {
        if (System.properties['PROXY_SERVER_NAME']) {
            mavenRepo "${System.properties['PROXY_SERVER_NAME']}"
        } else {
            grailsCentral()
            mavenCentral()
            mavenRepo "http://repository.jboss.org/maven2/"
            mavenRepo "https://code.lds.org/nexus/content/groups/main-repo"
        }
    }

    plugins {
        compile "org.grails.plugins:quartz:1.0.2"
    }

    dependencies {
        build('org.grails:grails-docs:2.5.0') {
            excludes 'itext', 'core-renderer'
        }
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // runtime 'mysql:mysql-connector-java:5.1.5'
        compile 'org.xhtmlrenderer:flying-saucer-core:9.0.8'
        compile 'org.xhtmlrenderer:flying-saucer-pdf:9.0.8'
        compile 'com.lowagie:itext:2.1.7',{
            excludes 'bouncycastle:bcprov-jdk14:138', 'org.bouncycastle:bcprov-jdk14:1.38'
        }
        compile 'org.antlr:ST4:4.0.8'
        compile "javax.mail:javax.mail-api:1.5.5"
        runtime "com.sun.mail:javax.mail:1.5.5"
        compile 'com.icegreen:greenmail:1.5.0'
        compile 'org.apache.commons:commons-email:1.4'
        compile 'org.quartz-scheduler:quartz:2.2.1'
    }


}

// CodeNarc rulesets
codenarc.ruleSetFiles="rulesets/banner.groovy"
codenarc.reportName="target/CodeNarcReport.html"
codenarc.propertiesFile="grails-app/conf/codenarc.properties"

/** *****************************************************************************
  © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */


import com.sungardhe.banner.configuration.ApplicationConfigurationUtils as ConfigFinder
import grails.plugins.springsecurity.SecurityConfigType

// ******************************************************************************
//
//                       +++ EXTERNALIZED CONFIGURATION +++
//
// ******************************************************************************
//
// Config locations should be added to the map used below. They will be loaded based upon this search order:
// 1. Load the configuration file if its location was specified on the command line using -DmyEnvName=myConfigLocation
// 2. Load the configuration file if it exists within the user's .grails directory (i.e., convenient for developers)
// 3. Load the configuration file if its location was specified as a system environment variable
//
// Map [ environment variable or -D command line argument name : file path ]
grails.config.locations = [] // leave this initialized to an empty list, and add your locations
// in the APPLICATION CONFIGURATION section below.

def locationAdder = ConfigFinder.&addLocation.curry(grails.config.locations)

[bannerGrailsAppConfig: "${userHome}/.grails/banner_on_grails-local-config.groovy",
        customRepresentationConfig: "grails-app/conf/CustomRepresentationConfig.groovy",
].each { envName, defaultFileName -> locationAdder(envName, defaultFileName) }



grails.project.groupId = "com.sungardhe" // used when deploying to a maven repo

grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [html: ['text/html', 'application/xhtml+xml'],
        xml: ['text/xml', 'application/xml', 'application/vnd.sungardhe.student.v0.01+xml'],
        text: 'text/plain',
        js: 'text/javascript',
        rss: 'application/rss+xml',
        atom: 'application/atom+xml',
        css: 'text/css',
        csv: 'text/csv',
        all: '*/*',
        json: ['application/json', 'text/json'],
        form: 'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data'
]

// The default codec used to encode data with ${}
grails.views.default.codec = "html" // none, html, base64  **** Charlie note: Setting this to html will ensure html is escaped, to prevent XSS attack ****
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"

grails.converters.domain.include.version = true
grails.converters.json.date = "javascript"
grails.converters.json.pretty.print = true

banner {
    sso {
        authenticationProvider = 'default'
        authenticationAssertionAttribute = 'udcId'
    }
}

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = false

// enable GSP preprocessing: replace head -> g:captureHead, title -> g:captureTitle, meta -> g:captureMeta, body -> g:captureBody
grails.views.gsp.sitemesh.preprocess = true



// ******************************************************************************
//
//                       +++ LOGGER CONFIGURATION +++
//
// ******************************************************************************
// See http://grails.org/doc/1.1.x/guide/3.%20Configuration.html#3.1.2%20Logging
// for more information about log4j configuration.
log4j = {
    appenders {
        file name: 'file', file: 'target/logs/development.log'
    }
    root {
        off 'stdout', 'file'
        additivity = true
    }

    // Configure logging for other classes (e.g., in src/ or grails-app/utils/) here:
    // info 'com.sungardhe.banner.representations'
    // info 'com.sungardhe.banner.supplemental.SupplementalDataService'
    off  'com.sungardhe.banner.security'
    off  'com.sungardhe.banner.db'
    off  'com.sungardhe.banner.student'
    off  'com.sungardhe.banner.general.system.CollegeController'
    off  'com.sungardhe.banner.general.system.CollegeService'

    // Grails framework classes
    off  'org.codehaus.groovy.grails.web.servlet'        // controllers
    off  'org.codehaus.groovy.grails.web.pages'          // GSP
    off  'org.codehaus.groovy.grails.web.sitemesh'       // layouts
    off  'org.codehaus.groovy.grails.web.mapping.filter' // URL mapping
    off  'org.codehaus.groovy.grails.web.mapping'        // URL mapping
    off  'org.codehaus.groovy.grails.commons'            // core / classloading
    off  'org.codehaus.groovy.grails.plugins'            // plugins
    off  'org.codehaus.groovy.grails.orm.hibernate'      // hibernate integration
    off  'org.springframework'                           // Spring IoC
    off  'org.hibernate'                                 // hibernate ORM

    off  'grails.plugins.springsecurity'
    off  'org.springframework.security'

    // Grails provides a convenience for enabling logging within artefacts, using 'grails.app.XXX'.
    // Unfortunately, this configuration is not effective when 'mixing in' methods that perform logging.
    // Therefore, for controllers and services it is recommended that you enable logging using the controller
    // or service class name (see above 'class name' based configurations).  For example:
    //     all  'com.sungardhe.banner.testing.FooController' // turns on all logging for the FooController
    //
    off 'grails.app' // apply to all artefacts
    // off 'grails.app.<artefactType>.ClassName // where artefactType is in:
    //  bootstrap  - For bootstrap classes
    //  dataSource - For data sources
    //  tagLib     - For tag libraries
    //  service    // Not effective with mixins -- see comment above
    //  controller // Not effective with mixins -- see comment above
    //  domain     - For domain entities

    off 'com.sungardhe.banner.student.system'
}


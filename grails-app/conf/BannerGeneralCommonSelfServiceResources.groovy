
/** *****************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

modules = {

    'userAgreement' {
        dependsOn "bannerSelfService, i18n-core"
        defaultBundle environment == "development" ? false : "userAgreement"
        //defaultBundle false

        resource url: [plugin: 'banner-general-common', file: 'css/views/userAgreement/policy.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-general-common', file: 'js/views/userAgreement/policy.js']
    }

    'userAgreementRTL' {
        dependsOn "bannerSelfServiceRTL, i18n-core"
        defaultBundle environment == "development" ? false : "userAgreementRTL"
        //defaultBundle false

        resource url: [plugin: 'banner-general-common', file: 'css/views/userAgreement/policy-rtl.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-general-common', file: 'js/views/userAgreement/policy.js']
    }

    'securityQA' {
        dependsOn "bannerSelfService, i18n-core"
        defaultBundle environment == "development" ? false : "securityQA"
        resource url: [plugin: 'banner-general-common', file: 'css/views/securityQA/securityQA.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-general-common', file: 'js/views/securityQA/securityQA.js']
    }

    'securityQARTL' {
        dependsOn "bannerSelfServiceRTL, i18n-core"
        defaultBundle environment == "development" ? false : "securityQARTL"
        resource url: [plugin: 'banner-general-common', file: 'css/views/securityQA/securityQA-rtl.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-general-common', file: 'js/views/securityQA/securityQA.js']
    }

    'survey' {
        dependsOn "bannerSelfService, i18n-core"
        defaultBundle environment == "development" ? false : "survey"
        //defaultBundle false

        resource url: [plugin: 'banner-general-common', file: 'css/views/survey/survey.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-general-common', file: 'js/views/survey/survey.js']
    }

    'surveyRTL' {
        dependsOn "bannerSelfServiceRTL, i18n-core"
        defaultBundle environment == "development" ? false : "surveyRTL"
        //defaultBundle false

        resource url: [plugin: 'banner-general-common', file: 'css/views/survey/survey-rtl.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-general-common', file: 'js/views/survey/survey.js']
    }
}

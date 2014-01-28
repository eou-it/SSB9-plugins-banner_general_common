/** *****************************************************************************
 Copyright 2013-2014 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

import net.hedtech.banner.security.SelfServiceBannerAuthenticationProvider

modules = {
    if(SelfServiceBannerAuthenticationProvider.isSsbEnabled()) {

        'survey' {
            dependsOn "bannerSelfService, i18n-core"
            defaultBundle environment == "development" ? false : "survey"

            resource url: [plugin: 'banner-general-common', file: 'css/views/survey/survey.css'], attrs: [media: 'screen, projection']
            resource url: [plugin: 'banner-general-common', file: 'js/views/survey/survey.js']
        }

        'surveyRTL' {
            dependsOn "bannerSelfServiceRTL, i18n-core, survey"
            defaultBundle environment == "development" ? false : "surveyRTL"

            resource url: [plugin: 'banner-general-common', file: 'css/views/survey/survey-rtl.css'], attrs: [media: 'screen, projection']
            resource url: [plugin: 'banner-general-common', file: 'css/views/survey/survey-rtl-patch.css'], attrs: [media: 'screen, projection']
        }
    }
}

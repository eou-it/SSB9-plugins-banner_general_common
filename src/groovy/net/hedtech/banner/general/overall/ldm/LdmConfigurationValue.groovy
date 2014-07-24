/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.general.overall.IntegrationConfiguration


class LdmConfigurationValue {

    String settingName
    String value
    String translationValue

    def LdmConfigurationValue(IntegrationConfiguration rule) {
        this.settingName = rule.settingName
        this.value = rule.value
        this.translationValue = rule.translationValue
    }
}

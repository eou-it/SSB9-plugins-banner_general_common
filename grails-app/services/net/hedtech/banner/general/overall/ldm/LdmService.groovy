/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.general.overall.IntegrationConfiguration
import org.springframework.beans.factory.InitializingBean

class LdmService implements InitializingBean {

    List<IntegrationConfiguration> rules = []

    void afterPropertiesSet() {
        def ldmObject = this.class.simpleName.substring( 0, this.class.simpleName.indexOf( "CompositeService" ) ).toLowerCase() //Get the ldm name from the service name.
        log.debug "Getting LDM defaults for ${this.class.simpleName}"
        def rules = IntegrationConfiguration.findAllByProcessCodeAndSettingNameLike('LDM',ldmObject + '.%')
        this.rules = parseRules(rules)
    }

    private def parseRules(def rules) {
        rules.each { rule ->
            this.rules << new LdmConfigurationValue(rule)
        }
    }
}

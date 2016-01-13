/*********************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.system.ldm.v1

import net.hedtech.banner.general.system.ldm.IntegrationHubConfig

class EmsConfiguration {

    String id
    String description
    String logLevel
    Integer guidLifespan
    boolean useIntegrationHub

    IntegrationHubConfig integrationHubConfig
    AmqpServerConfig amqpServerConfig
    ErpEventConfig erpEventConfig
    MessageConfig messageInConfig
    MessageConfig messageOutConfig
    ErpApiConfig erpApiConfig

}

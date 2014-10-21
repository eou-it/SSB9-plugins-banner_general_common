/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.system.ldm.v1

class EmsConfiguration {
    String id
    String description
    AmqpServerConfig amqpServerConfig
    ErpEventConfig erpEventConfig
    MessageConfig messageOutConfig
    MessageConfig messageInConfig
    ErpApiConfig erpApiConfig
    String logLevel
    Integer guidLifespan

    EmsConfiguration(String id) {
        this.id = id
        this.amqpServerConfig = new AmqpServerConfig()
        this.erpEventConfig = new ErpEventConfig()
        this.messageOutConfig = new MessageConfig()
        this.messageInConfig = new MessageConfig()
        this.erpApiConfig = new ErpApiConfig()
        logLevel = null
        guidLifespan = 0
    }

}

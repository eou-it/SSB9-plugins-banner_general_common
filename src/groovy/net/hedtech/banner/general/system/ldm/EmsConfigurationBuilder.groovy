/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.system.ldm

import net.hedtech.banner.general.system.ldm.v1.*

/**
 *  Builder pattern is an object creation design pattern.  Builder allows different ways of object building i.e. different representations.
 *  EmsConfigurationBuilder constructs EmsConfiguration object.
 *  Builder removes what could be a complex building process from being the responsibility of the user of the EmsConfiguration object.
 *
 */
class EmsConfigurationBuilder {

    private static List<String> BEP_ROUTING_KEYS = ["HEDM.*.*"]
    private static List<String> MESSAGE_IN_ROUTING_KEYS = ['#']

    private String id
    String description
    String logLevel
    Integer guidLifespan
    boolean useIntegrationHub

    // integrationHubConfig
    String ihubApplicationID
    String ihubApplicationName
    String ihubApiKey
    String ihubTokenUrl
    String ihubPublishUrl
    String ihubSubscribeUrl
    String ihubMediaType

    // amqpServerConfig
    boolean amqpServerAutoRecovery
    Integer amqpServerHeartbeat
    boolean amqpServerSecure
    String amqpServerHost
    String amqpServerPort
    String amqpServerVirtualHost
    String amqpServerUsername
    String amqpServerPassword
    Integer amqpServerTimeout

    // erpEventConfig
    String bepExchangeName
    String bepQueueName
    List<String> bepRoutingKeys = BEP_ROUTING_KEYS

    // messageInConfig
    String messageInExchangeName
    String messageInQueueName
    List<String> messageInRoutingKeys = MESSAGE_IN_ROUTING_KEYS

    // messageOutConfig
    String messageOutExchangeName

    // erpApiConfig
    String erpName = "Banner"
    String apiUsername
    String apiPassword
    ErpApiConfig erpApiConfig


    EmsConfigurationBuilder(String id) {
        this.id = id
    }


    EmsConfiguration build() {
        EmsConfiguration emsConfiguration = new EmsConfiguration()
        emsConfiguration.id = this.id
        emsConfiguration.description = this.description
        emsConfiguration.logLevel = this.logLevel
        emsConfiguration.guidLifespan = this.guidLifespan
        emsConfiguration.useIntegrationHub = this.useIntegrationHub

        emsConfiguration.amqpServerConfig = createAmqpServerConfig()
        emsConfiguration.erpEventConfig = createErpEventConfig()
        if (this.useIntegrationHub) {
            emsConfiguration.integrationHubConfig = createIntegrationHubConfig()
        } else {
            emsConfiguration.messageInConfig = createMessageInConfig()
            emsConfiguration.messageOutConfig = createMessageOutConfig()
        }
        emsConfiguration.erpApiConfig = createErpApiConfig()
        return emsConfiguration
    }


    private IntegrationHubConfig createIntegrationHubConfig() {
        IntegrationHubConfig integrationHubConfig = new IntegrationHubConfig()
        integrationHubConfig.applicationID = this.ihubApplicationID
        integrationHubConfig.applicationName = this.ihubApplicationName
        integrationHubConfig.apiKey = this.ihubApiKey
        integrationHubConfig.tokenUrl = this.ihubTokenUrl
        integrationHubConfig.publishUrl = this.ihubPublishUrl
        integrationHubConfig.subscribeUrl = this.ihubSubscribeUrl
        integrationHubConfig.hubMediaType = this.ihubMediaType
        return integrationHubConfig
    }


    private AmqpServerConfig createAmqpServerConfig() {
        AmqpServerConfig amqpServerConfig = new AmqpServerConfig()
        amqpServerConfig.secure = this.amqpServerSecure
        amqpServerConfig.host = this.amqpServerHost
        amqpServerConfig.port = this.amqpServerPort
        amqpServerConfig.virtualHost = this.amqpServerVirtualHost
        amqpServerConfig.timeout = this.amqpServerTimeout
        amqpServerConfig.autoRecovery = this.amqpServerAutoRecovery
        amqpServerConfig.heartbeat = this.amqpServerHeartbeat
        amqpServerConfig.username = this.amqpServerUsername
        amqpServerConfig.password = this.amqpServerPassword
        return amqpServerConfig
    }


    private ErpEventConfig createErpEventConfig() {
        ErpEventConfig erpEventConfig = new ErpEventConfig()
        erpEventConfig.exchangeName = this.bepExchangeName
        erpEventConfig.queueName = this.bepQueueName
        erpEventConfig.routingKeys = this.bepRoutingKeys
        return erpEventConfig
    }


    private MessageConfig createMessageInConfig() {
        MessageConfig messageInConfig = new MessageConfig()
        messageInConfig.exchangeName = this.messageInExchangeName
        messageInConfig.queueName = this.messageInQueueName
        messageInConfig.routingKeys = this.messageInRoutingKeys
        return messageInConfig
    }


    private MessageConfig createMessageOutConfig() {
        MessageConfig messageOutConfig = new MessageConfig()
        messageOutConfig.exchangeName = this.messageOutExchangeName
        return messageOutConfig
    }


    private ErpApiConfig createErpApiConfig() {
        ErpApiConfig erpApiConfig = this.erpApiConfig
        erpApiConfig.erpname = this.erpName
        erpApiConfig.username = this.apiUsername
        erpApiConfig.password = this.apiPassword
        return erpApiConfig
    }

}

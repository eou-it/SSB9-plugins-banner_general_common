/*********************************************************************************
 Copyright 2015-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.system.ldm

import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.system.ldm.v1.AmqpServerConfig
import net.hedtech.banner.general.system.ldm.v1.ErpApiConfig
import net.hedtech.banner.general.system.ldm.v1.ErpEventConfig
import net.hedtech.banner.general.system.ldm.v1.MessageConfig

/**
 *  Builder pattern is an object creation design pattern.  Builder allows different ways of object building i.e. different representations.
 *  EmsConfigurationBuilder constructs EmsConfiguration object.
 *  Builder removes what could be a complex building process from being the responsibility of the user of the EmsConfiguration object.
 *
 */
class EmsConfigurationBuilder {

    private static List<String> BEP_ROUTING_KEYS = ["HEDM.#"]
    private static List<String> MESSAGE_IN_ROUTING_KEYS = ['#']

    private static final String BANNER_ELEVATE = "BANNER-ELEVATE"
    private static final String ETHOS_INTEGRATION = "ETHOS-INTEGRATION"

    EmsConfiguration emsConfiguration


    EmsConfigurationBuilder(String id, List<IntegrationConfiguration> settings, ErpApiConfig erpApiConfig) {
        if (id == BANNER_ELEVATE) {
            emsConfiguration = new EmsElevateConfiguration()

            emsConfiguration.messageInConfig = new MessageConfig()
            emsConfiguration.messageInConfig.routingKeys = MESSAGE_IN_ROUTING_KEYS

            emsConfiguration.messageOutConfig = new MessageConfig()
        } else if (id == ETHOS_INTEGRATION) {
            emsConfiguration = new EmsEthosConfiguration()
            emsConfiguration.integrationHubConfig = new IntegrationHubConfig()
        }

        emsConfiguration.id = id

        emsConfiguration.amqpServerConfig = new AmqpServerConfig()

        emsConfiguration.erpEventConfig = new ErpEventConfig()
        emsConfiguration.erpEventConfig.routingKeys = BEP_ROUTING_KEYS

        emsConfiguration.erpApiConfig = erpApiConfig
        emsConfiguration.erpApiConfig.erpname = "Banner"

        if (settings) {
            settings.each { setting ->
                switch (setting.settingName) {
                    case 'EMS.LOGLEVEL':
                        emsConfiguration.logLevel = setting.translationValue
                        break;
                    case 'EMS.GUID.LIFESPAN':
                        emsConfiguration.guidLifespan = setting.translationValue.toInteger()
                        break;
                // amqpServerConfig
                    case 'EMS.AMQP.AUTORECOVER':
                        emsConfiguration.amqpServerConfig.autoRecovery = setting.translationValue.toUpperCase() == "Y" ? true : false
                        break;
                    case 'EMS.AMQP.HEARTBEAT':
                        emsConfiguration.amqpServerConfig.heartbeat = setting.translationValue.toInteger()
                        break;
                    case 'EMS.AMQP.SECURE':
                        emsConfiguration.amqpServerConfig.secure = setting.translationValue.toUpperCase() == "Y" ? true : false
                        break;
                    case 'EMS.AMQP.HOST':
                        emsConfiguration.amqpServerConfig.host = setting.translationValue
                        break;
                    case 'EMS.AMQP.PORT':
                        emsConfiguration.amqpServerConfig.port = setting.translationValue
                        break;
                    case 'EMS.AMQP.VIRTUALHOST':
                        emsConfiguration.amqpServerConfig.virtualHost = setting.translationValue
                        break;
                    case 'EMS.AMQP.USERNAME':
                        emsConfiguration.amqpServerConfig.username = setting.translationValue
                        break;
                    case 'EMS.AMQP.PASSWORD':
                        emsConfiguration.amqpServerConfig.password = setting.translationValue
                        break;
                    case 'EMS.AMQP.TIMEOUT':
                        emsConfiguration.amqpServerConfig.timeout = setting.translationValue.toInteger()
                        break;
                // erpEventConfig
                    case 'EMS.EVENT.ERP.EXCHANGENAME':
                        emsConfiguration.erpEventConfig.exchangeName = setting.translationValue
                        break;
                    case 'EMS.EVENT.ERP.QUEUENAME':
                        emsConfiguration.erpEventConfig.queueName = setting.translationValue
                        break;
                // erpApiConfig
                    case 'EMS.API.USERNAME':
                        emsConfiguration.erpApiConfig.username = setting.translationValue
                        break;
                    case 'EMS.API.PASSWORD':
                        emsConfiguration.erpApiConfig.password = setting.translationValue
                        break;
                // messageInConfig
                    case 'EMS.MESSAGE.IN.EXCHANGENAME':
                        emsConfiguration.messageInConfig.exchangeName = setting.translationValue
                        break;
                    case 'EMS.MESSAGE.IN.QUEUENAME':
                        emsConfiguration.messageInConfig.queueName = setting.translationValue
                        break;
                // messageOutConfig
                    case 'EMS.MESSAGE.OUT.EXCHANGENAME':
                        emsConfiguration.messageOutConfig.exchangeName = setting.translationValue
                        break;
                // integrationHubConfig
                    case 'EMS.IHUB.USE':
                        emsConfiguration.useIntegrationHub = setting.translationValue.toUpperCase() == "Y" ? true : false
                        break;
                    case 'EMS.IHUB.APPLICATION.API.KEY':
                        emsConfiguration.integrationHubConfig.apiKey = setting.translationValue
                        break;
                    case 'EMS.IHUB.TOKEN.URL':
                        emsConfiguration.integrationHubConfig.tokenUrl = setting.translationValue
                        break;
                    case 'EMS.IHUB.PUBLISH.URL':
                        emsConfiguration.integrationHubConfig.publishUrl = setting.translationValue
                        break;
                    case 'EMS.IHUB.SUBSCRIBE.URL':
                        emsConfiguration.integrationHubConfig.subscribeUrl = setting.translationValue
                        break;
                    case 'EMS.IHUB.ERROR.URL':
                        emsConfiguration.integrationHubConfig.errorUrl = setting.translationValue
                        break;
                    case 'EMS.IHUB.MEDIATYPE':
                        emsConfiguration.integrationHubConfig.hubMediaType = setting.translationValue
                        break;
                    case 'EMS.IHUB.MEP.ENABLED':
                        emsConfiguration.integrationHubConfig.isMepEnvironment = setting.translationValue.toUpperCase() == "Y" ? true : false
                        break;
                    case 'EMS.IHUB.MEP.APIKEYS':
                        def vpdiKeyComb = [:]
                        vpdiKeyComb.put("vpdiCode", setting.value)
                        vpdiKeyComb.put("apiKey", setting.translationValue)
                        emsConfiguration.integrationHubConfig.mepApiKeyMappings << vpdiKeyComb
                        break;
                    default:
                        break;
                }
            }
        }

        if (id == ETHOS_INTEGRATION) {
            if (emsConfiguration.integrationHubConfig.isMepEnvironment) {
                // If 'isMepEnvironment' is true, 'mepApiKeyMappings' will be used by the adapter
                emsConfiguration.integrationHubConfig.apiKey = null
            } else {
                // If 'isMepEnvironment' is false, 'apiKey' will be used by the adapter
                emsConfiguration.integrationHubConfig.mepApiKeyMappings = null
            }
            if (emsConfiguration.useIntegrationHub == false) {
                emsConfiguration.integrationHubConfig = null
            }
        }
    }

}

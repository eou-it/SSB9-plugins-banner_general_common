/*********************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.system.ldm

import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.system.ldm.v1.EmsConfiguration
import net.hedtech.banner.general.system.ldm.v1.ErpApiConfig

/**
 * RESTful APIs for EMS
 */
class EmsConfigurationCompositeService {

    /**
     * GET /api/ems/<id>
     *
     * @param id
     * @return
     */
    EmsConfiguration get(String id) {
        String processCode = "EMS-" + id.toUpperCase()
        String springBeanName = "erpApiConfig-" + id.toUpperCase()
        log.debug "Process code '$processCode' and spring bean name '$springBeanName'"
        List<IntegrationConfiguration> settings = IntegrationConfiguration.findAllByProcessCode(processCode)
        ErpApiConfig erpApiConfig = Holders.applicationContext.getBean(springBeanName)
        if (settings) {
            EmsConfigurationBuilder emsConfigBuilder = new EmsConfigurationBuilder(id)
            emsConfigBuilder.erpApiConfig = erpApiConfig
            settings.each { setting ->
                switch (setting.settingName) {
                    case 'EMS.LOGLEVEL':
                        emsConfigBuilder.logLevel = setting.translationValue
                        break;
                    case 'EMS.GUID.LIFESPAN':
                        emsConfigBuilder.guidLifespan = setting.translationValue.toInteger()
                        break;
                    case 'EMS.IHUB.USE':
                        emsConfigBuilder.useIntegrationHub = setting.translationValue.toUpperCase() == "Y" ? true : false
                        break;
                // integrationHubConfig
                    case 'EMS.IHUB.APPLICATION.ID':
                        emsConfigBuilder.ihubApplicationID = setting.translationValue
                        break;
                    case 'EMS.IHUB.APPLICATION.NAME':
                        emsConfigBuilder.ihubApplicationName = setting.translationValue
                        break;
                    case 'EMS.IHUB.APPLICATION.API.KEY':
                        emsConfigBuilder.ihubApiKey = setting.translationValue
                        break;
                    case 'EMS.IHUB.TOKEN.URL':
                        emsConfigBuilder.ihubTokenUrl = setting.translationValue
                        break;
                    case 'EMS.IHUB.PUBLISH.URL':
                        emsConfigBuilder.ihubPublishUrl = setting.translationValue
                        break;
                    case 'EMS.IHUB.SUBSCRIBE.URL':
                        emsConfigBuilder.ihubSubscribeUrl = setting.translationValue
                        break;
                    case 'EMS.IHUB.MEDIATYPE':
                        emsConfigBuilder.ihubMediaType = setting.translationValue
                        break;
                // amqpServerConfig
                    case 'EMS.AMQP.AUTORECOVER':
                        emsConfigBuilder.amqpServerAutoRecovery = setting.translationValue.toUpperCase() == "Y" ? true : false
                        break;
                    case 'EMS.AMQP.HEARTBEAT':
                        emsConfigBuilder.amqpServerHeartbeat = setting.translationValue.toInteger()
                        break;
                    case 'EMS.AMQP.SECURE':
                        emsConfigBuilder.amqpServerSecure = setting.translationValue.toUpperCase() == "Y" ? true : false
                        break;
                    case 'EMS.AMQP.HOST':
                        emsConfigBuilder.amqpServerHost = setting.translationValue
                        break;
                    case 'EMS.AMQP.PORT':
                        emsConfigBuilder.amqpServerPort = setting.translationValue
                        break;
                    case 'EMS.AMQP.VIRTUALHOST':
                        emsConfigBuilder.amqpServerVirtualHost = setting.translationValue
                        break;
                    case 'EMS.AMQP.USERNAME':
                        emsConfigBuilder.amqpServerUsername = setting.translationValue
                        break;
                    case 'EMS.AMQP.PASSWORD':
                        emsConfigBuilder.amqpServerPassword = setting.translationValue
                        break;
                    case 'EMS.AMQP.TIMEOUT':
                        emsConfigBuilder.amqpServerTimeout = setting.translationValue.toInteger()
                        break;
                // erpEventConfig
                    case 'EMS.EVENT.ERP.EXCHANGENAME':
                        emsConfigBuilder.bepExchangeName = setting.translationValue
                        break;
                    case 'EMS.EVENT.ERP.QUEUENAME':
                        emsConfigBuilder.bepQueueName = setting.translationValue
                        break;
                // messageInConfig
                    case 'EMS.MESSAGE.IN.EXCHANGENAME':
                        emsConfigBuilder.messageInExchangeName = setting.translationValue
                        break;
                    case 'EMS.MESSAGE.IN.QUEUENAME':
                        emsConfigBuilder.messageInQueueName = setting.translationValue
                        break;
                // messageOutConfig
                    case 'EMS.MESSAGE.OUT.EXCHANGENAME':
                        emsConfigBuilder.messageOutExchangeName = setting.translationValue
                        break;
                // erpApiConfig
                    case 'EMS.API.USERNAME':
                        emsConfigBuilder.apiUsername = setting.translationValue
                        break;
                    case 'EMS.API.PASSWORD':
                        emsConfigBuilder.apiPassword = setting.translationValue
                        break;
                    default:
                        break;
                }
            }
            return emsConfigBuilder.build()
        } else {
            log.error "GORICCR settings not found under process code '$processCode'"
            throw new ApplicationException("EmsConfiguration", new NotFoundException())
        }
    }

}

/*********************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.system.ldm

import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.system.ldm.v1.EmsConfiguration

/**
 * RESTful APIs for EMS
 */
class EmsConfigurationCompositeService {

    public static List<String> ROUTING_KEYS = ["HEDM.*.*"]

    /**
     * GET /api/ems/<id>
     *
     * @param id
     * @return
     */
    EmsConfiguration get(String id) {
        List<IntegrationConfiguration> settings = IntegrationConfiguration.findAllByProcessCode("EMS-" + id.toUpperCase())
        if (settings.size()) {
            EmsConfiguration configuration = new EmsConfiguration(id)
            configuration.erpApiConfig = Holders.applicationContext.getBean("erpApiConfig-" + id.toUpperCase())
            configuration.erpApiConfig.erpname = "Banner"
            settings.each { setting ->
                switch (setting.settingName) {
                    case 'EMS.AMQP.SECURE':
                        configuration.amqpServerConfig.secure = setting.translationValue.toUpperCase() == "Y" ? true : false
                        break;
                    case 'EMS.AMQP.HOST':
                        configuration.amqpServerConfig.host = setting.translationValue
                        break;
                    case 'EMS.AMQP.PORT':
                        configuration.amqpServerConfig.port = setting.translationValue
                        break;
                    case 'EMS.AMQP.VIRTUALHOST':
                        configuration.amqpServerConfig.virtualHost = setting.translationValue
                        break;
                    case 'EMS.AMQP.TIMEOUT':
                        configuration.amqpServerConfig.timeout = setting.translationValue.toInteger()
                        break;
                    case 'EMS.AMQP.AUTORECOVER':
                        configuration.amqpServerConfig.autoRecovery = setting.translationValue.toUpperCase() == "Y" ? true : false
                        break;
                    case 'EMS.AMQP.HEARTBEAT':
                        configuration.amqpServerConfig.heartbeat = setting.translationValue.toInteger()
                        break;
                    case 'EMS.AMQP.USERNAME':
                        configuration.amqpServerConfig.username = setting.translationValue
                        break;
                    case 'EMS.AMQP.PASSWORD':
                        configuration.amqpServerConfig.password = setting.translationValue
                        break;
                    case 'EMS.EVENT.ERP.EXCHANGENAME':
                        configuration.erpEventConfig.exchangeName = setting.translationValue
                        configuration.erpEventConfig.routingKeys = ROUTING_KEYS
                        break;
                    case 'EMS.EVENT.ERP.QUEUENAME':
                        configuration.erpEventConfig.queueName = setting.translationValue
                        break;
                    case 'EMS.MESSAGE.OUT.EXCHANGENAME':
                        configuration.messageOutConfig.exchangeName = setting.translationValue
                        break;
                    case 'EMS.MESSAGE.IN.EXCHANGENAME':
                        configuration.messageInConfig.exchangeName = setting.translationValue
                        configuration.messageInConfig.routingKeys = ['#']
                        break;
                    case 'EMS.MESSAGE.IN.QUEUENAME':
                        configuration.messageInConfig.queueName = setting.translationValue
                        break;
                    case 'EMS.API.USERNAME':
                        configuration.erpApiConfig.username = setting.translationValue
                        break;
                    case 'EMS.API.PASSWORD':
                        configuration.erpApiConfig.password = setting.translationValue
                        break;
                    case 'EMS.LOGLEVEL':
                        configuration.logLevel = setting.translationValue
                        break;
                    case 'EMS.GUID.LIFESPAN':
                        configuration.guidLifespan = setting.translationValue.toInteger()
                        break;
                    case 'EMS.IHUB.USE':
                        // Use Ellucian Integration Hub
                        break;
                    case 'EMS.IHUB.APPLICATION.ID':
                        // ID of the application from the hub admin UI
                        break;
                    case 'EMS.IHUB.APPLICATION.API.KEY':
                        // API Key of the application from the hub admin UI
                        break;
                    case 'EMS.IHUB.TOKEN.URL':
                        // URL for the hub token service
                        break;
                    case 'EMS.IHUB.PUBLISH.URL':
                        // URL for the publish service
                        break;
                    case 'EMS.IHUB.MESSAGEQUEUE.URL':
                        // URL for the hub message queue service
                        break;
                    case 'EMS.IHUB.MEDIATYPE':
                        // the media type for messages getting published and retrieved
                        break;
                    default:
                        break;
                }
            }
            configuration
        } else {
            throw new ApplicationException("EmsConfiguration", new NotFoundException())
        }
    }

}

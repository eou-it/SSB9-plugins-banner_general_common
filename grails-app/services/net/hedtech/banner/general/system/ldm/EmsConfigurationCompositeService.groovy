package net.hedtech.banner.general.system.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.system.ldm.v1.EmsConfiguration

/**
 * Created by rshishehbor on 10/15/14.
 */
class EmsConfigurationCompositeService {

    public static String ROUTING_KEYS = ["CDM.*.*"]

    EmsConfiguration show(String id) {
        List<IntegrationConfiguration> settings = IntegrationConfiguration.findAllByProcessCode("EMS-" + id.toUpperCase())
        if(settings.size()) {
            EmsConfiguration configuration = new EmsConfiguration(id)
            configuration.erpApiConfig.erpname = "Banner"
            settings.each { setting ->
                switch (setting.settingName) {
                    case 'EMS.AMQP.SECURE':
                        configuration.amqpServerConfig.secure = setting.value == "Y" ? true : false
                        break;
                    case 'EMS.AMQP.HOST':
                        configuration.amqpServerConfig.host = setting.value
                        break;
                    case 'EMS.AMQP.PORT':
                        configuration.amqpServerConfig.port = setting.value
                        break;
                    case 'EMS.AMQP.VIRTUALHOST':
                        configuration.amqpServerConfig.virtualHost = setting.value
                        break;
                    case 'EMS.AMQP.TIMEOUT':
                        configuration.amqpServerConfig.timeout = setting.value.toInteger()
                        break;
                    case 'EMS.AMQP.AUTORECOVER':
                        configuration.amqpServerConfig.autoRecovery = setting.value == "Y" ? true : false
                        break;
                    case 'EMS.AMQP.HEARTBEAT':
                        configuration.amqpServerConfig.heartbeat = setting.value.toInteger()
                        break;
                    case 'EMS.AMQP.USERNAME':
                        configuration.amqpServerConfig.username = setting.value
                        break;
                    case 'EMS.AMQP.PASSWORD':
                        configuration.amqpServerConfig.password = setting.value
                        break;
                    case 'EMS.EVENT.ERP.EXCHANGENAME':
                        configuration.erpEventConfig.exchangeName = setting.value
                        configuration.erpEventConfig.routingKeys = ROUTING_KEYS
                        break;
                    case 'EMS.EVENT.ERP.QUEUENAME':
                        configuration.erpEventConfig.queueName = setting.value
                        break;
                    case 'EMS.MESSAGE.OUT.EXCHANGENAME':
                        configuration.messageOutConfig.exchangeName = setting.value
                        break;
                    case 'EMS.MESSAGE.IN.EXCHANGENAME':
                        configuration.messageInConfig.exchangeName = setting.value
                        configuration.messageInConfig.routingKeys = ['#']
                        break;
                    case 'EMS.MESSAGE.IN.QUEUENAME':
                        configuration.messageInConfig.queueName = setting.value
                        break;
                    case 'EMS.API.USERNAME':
                        configuration.erpApiConfig.username = setting.value
                        break;
                    case 'EMS.API.PASSWORD':
                        configuration.erpApiConfig.password = setting.value
                        break;
                    case 'EMS.LOGLEVEL':
                        configuration.logLevel = setting.value
                        break;
                    case 'EMS.GUID.LIFESPAN':
                        configuration.guidLifespan = setting.value.toInteger()
                        break;
                    default:
                        break;

                }
            }
            configuration
        }
        else {
            throw new ApplicationException("EmsConfiguration", new NotFoundException(EmsConfiguration.simpleName, id))
        }

    }
}

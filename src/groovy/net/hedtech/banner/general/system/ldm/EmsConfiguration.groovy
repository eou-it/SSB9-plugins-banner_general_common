/*********************************************************************************
 Copyright 2014-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.system.ldm

import net.hedtech.banner.general.system.ldm.v1.AmqpServerConfig
import net.hedtech.banner.general.system.ldm.v1.ErpApiConfig
import net.hedtech.banner.general.system.ldm.v1.ErpEventConfig

abstract class EmsConfiguration {

    String id
    String description
    String logLevel
    Integer guidLifespan

    AmqpServerConfig amqpServerConfig
    ErpEventConfig erpEventConfig
    ErpApiConfig erpApiConfig

}

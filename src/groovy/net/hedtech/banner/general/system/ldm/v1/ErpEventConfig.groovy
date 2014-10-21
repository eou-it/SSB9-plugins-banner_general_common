/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.system.ldm.v1

class ErpEventConfig {
    List<String> routingKeys
    String exchangeName
    String queueName

    ErpEventConfig () {
        routingKeys = []
        exchangeName = null
        queueName = null
    }
}

/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.system.ldm.v1

class MessageConfig {
    List<String> routingKeys
    String exchangeName
    String queueName

    MessageConfig() {
        routingKeys = null
        exchangeName = null
        queueName = null
    }
}

package net.hedtech.banner.general.system.ldm.v1

/**
 * Created by rshishehbor on 10/15/14.
 */
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

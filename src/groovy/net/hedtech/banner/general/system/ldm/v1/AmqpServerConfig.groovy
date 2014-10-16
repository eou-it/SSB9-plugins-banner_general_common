package net.hedtech.banner.general.system.ldm.v1

/**
 * Created by rshishehbor on 10/15/14.
 */
class AmqpServerConfig {
    Boolean secure
    String host
    String port
    String virtualHost
    Integer timeout
    Boolean autoRecovery
    Integer heartbeat
    String username
    String password

    AmqpServerConfig () {
        secure = null
        host = null
        port = null
        virtualHost = null
        timeout = null
        autoRecovery = null
        heartbeat = null
        username = null
        password = null
    }
}

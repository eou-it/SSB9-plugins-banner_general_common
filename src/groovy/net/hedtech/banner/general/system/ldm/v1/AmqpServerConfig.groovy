/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.system.ldm.v1

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

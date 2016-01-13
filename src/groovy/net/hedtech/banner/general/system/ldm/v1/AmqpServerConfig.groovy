/*********************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.system.ldm.v1

class AmqpServerConfig {

    boolean secure
    String host
    String port
    String virtualHost
    Integer timeout
    boolean autoRecovery
    Integer heartbeat
    String username
    String password

}

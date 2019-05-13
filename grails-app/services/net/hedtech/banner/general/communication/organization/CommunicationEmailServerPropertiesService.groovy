/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.organization

import grails.gorm.transactions.Transactional
import net.hedtech.banner.service.ServiceBase

@Transactional
class CommunicationEmailServerPropertiesService extends ServiceBase {

    def preCreate(domainModelOrMap) {
        CommunicationEmailServerProperties emailServerProperties = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationEmailServerProperties
        if (!emailServerProperties.securityProtocol) {
            emailServerProperties.securityProtocol = CommunicationEmailServerConnectionSecurity.None
        }
    }

}

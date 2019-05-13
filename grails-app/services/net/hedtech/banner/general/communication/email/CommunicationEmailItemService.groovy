/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.email

import grails.gorm.transactions.Transactional
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.service.ServiceBase

@Transactional
class CommunicationEmailItemService extends ServiceBase {

    def preCreate( domainModelOrMap ) {
        CommunicationEmailItem emailItem = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationEmailItem
        emailItem.createDate = new Date()
        emailItem.communicationChannel = CommunicationChannel.EMAIL
    }

}

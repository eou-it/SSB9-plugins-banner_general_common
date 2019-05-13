/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.textmessage

import grails.gorm.transactions.Transactional
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.service.ServiceBase

@Transactional
class CommunicationTextMessageItemService extends ServiceBase {

    def preCreate( domainModelOrMap ) {
        CommunicationTextMessageItem item = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationTextMessageItem
        item.createDate = new Date()
        item.communicationChannel = CommunicationChannel.TEXT_MESSAGE
    }

}

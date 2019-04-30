/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/

package net.hedtech.banner.general.communication.mobile

import grails.gorm.transactions.Transactional
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.service.ServiceBase

@Transactional
class CommunicationMobileNotificationItemService extends ServiceBase {

    def preCreate( domainModelOrMap ) {
        CommunicationMobileNotificationItem item = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationMobileNotificationItem
        item.createDate = new Date()
        item.communicationChannel = CommunicationChannel.MOBILE_NOTIFICATION
    }

}

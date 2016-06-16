/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.letter

import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.general.communication.email.CommunicationEmailItem
import net.hedtech.banner.service.ServiceBase

class CommunicationLetterItemService extends ServiceBase {

    def preCreate( domainModelOrMap ) {
        CommunicationEmailItem emailItem = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationEmailItem
        emailItem.createDate = new Date()
        emailItem.communicationChannel = CommunicationChannel.EMAIL
    }

}

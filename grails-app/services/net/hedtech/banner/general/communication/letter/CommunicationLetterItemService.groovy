/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.letter

import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.service.ServiceBase

class CommunicationLetterItemService extends ServiceBase {

    def preCreate( domainModelOrMap ) {
        CommunicationLetterItem letterItem = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationLetterItem
        letterItem.createDate = new Date()
        letterItem.communicationChannel = CommunicationChannel.EMAIL
    }

}

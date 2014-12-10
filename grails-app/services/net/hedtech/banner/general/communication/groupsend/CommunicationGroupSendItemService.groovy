/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.groupsend

import net.hedtech.banner.service.ServiceBase
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Manages group send items.
 */
class CommunicationGroupSendItemService extends ServiceBase {

    def preCreate( domainModelOrMap ) {
        CommunicationGroupSendItem groupSendItem = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationGroupSendItem
        if (groupSendItem.getCreationDateTime() == null) {
            groupSendItem.setCreationDateTime( new Date() )
        };
    }

}

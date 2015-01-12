/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.merge

import grails.gorm.DetachedCriteria
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendItem
import net.hedtech.banner.service.ServiceBase

/**
 * Manages recipient data.
 */
class CommunicationRecipientDataService extends ServiceBase {

    public void deleteAll() {
        def criteria = new DetachedCriteria(CommunicationGroupSendItem).build {
            ne 'id', 0L
        }
        int total = criteria.deleteAll()
    }
}

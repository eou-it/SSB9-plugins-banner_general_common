/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.job

import grails.gorm.DetachedCriteria
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSend
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendItem
import net.hedtech.banner.service.ServiceBase

/**
 * Creates communication job
 */
class CommunicationJobService extends ServiceBase {

    public void deleteAll() {
        def criteria = new DetachedCriteria(CommunicationJob).build {
            ne 'id', 0L
        }
        int total = criteria.deleteAll()
    }
}

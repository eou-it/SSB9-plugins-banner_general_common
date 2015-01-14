/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.job

import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSend
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendItem
import net.hedtech.banner.service.ServiceBase

/**
 *  DAO service interface for communication group send item objects.
 */
class CommunicationJobService extends ServiceBase {

    public List fetchPending( Integer max ) {
        return CommunicationJob.fetchPending( max )
    }
}

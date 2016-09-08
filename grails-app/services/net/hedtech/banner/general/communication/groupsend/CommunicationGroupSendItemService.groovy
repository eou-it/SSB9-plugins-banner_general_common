/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import groovy.sql.Sql
import net.hedtech.banner.service.ServiceBase

/**
 * DAO service interface for communication group send item objects.
 */
class CommunicationGroupSendItemService extends ServiceBase {

    def preCreate( domainModelOrMap ) {
        CommunicationGroupSendItem groupSendItem = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationGroupSendItem
        if (groupSendItem.getCreationDateTime() == null) {
            groupSendItem.setCreationDateTime( new Date() )
        };
    }

    public def fetchRunningGroupSendItemCount( Long groupSendId ) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def count = 0
        try {
            sql.eachRow("select count(*) as totalCount from GCRGSIM where GCRGSIM_GROUP_SEND_ID = ? and GCRGSIM_CURRENT_STATE = 'Ready'", [groupSendId]) { row ->
                count = row.totalCount
            }
        } finally {
            sql?.close()
        }
        return count
    }

}

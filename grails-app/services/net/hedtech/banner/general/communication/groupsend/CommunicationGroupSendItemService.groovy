/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import net.hedtech.banner.service.ServiceBase

/**
 * DAO service interface for communication group send item objects.
 */
@Transactional
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
        def readyvar = 'Ready'
        try {
            sql.eachRow("select count(*) as totalCount from GCRGSIM where GCRGSIM_GROUP_SEND_ID = ? and GCRGSIM_CURRENT_STATE = ?", [groupSendId, readyvar]) { row ->
                count = row.totalCount
            }
        } finally {
            sql?.close()
        }
        return count
    }

}

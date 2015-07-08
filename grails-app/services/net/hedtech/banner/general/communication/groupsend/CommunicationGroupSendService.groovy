/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.groupsend

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.ExceptionFactory
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.service.ServiceBase
import org.springframework.security.core.context.SecurityContextHolder

import java.sql.Connection
import java.sql.SQLException

/**
 * Manages group send instances.
 */
class CommunicationGroupSendService extends ServiceBase {

    def preCreate( domainModelOrMap ) {
        CommunicationGroupSend groupSend = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationGroupSend
        if (groupSend.getCreatedBy() == null) {
            groupSend.setCreatedBy( SecurityContextHolder?.context?.authentication?.principal?.getOracleUserName() )
        };
        if (groupSend.getCreationDateTime() == null) {
            groupSend.setCreationDateTime( new Date() )
        };
        if (groupSend.getName() == null) {
            groupSend.setName(CommunicationTemplate.get(groupSend.template.id).getName())
        }
        groupSend.setDeleted( false );
    }

    public List findRunning() {
        return CommunicationGroupSend.findRunning()
    }

}

/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.groupsend

import grails.gorm.transactions.Transactional
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.service.ServiceBase
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Manages group send instances.
 */
@Transactional
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
            groupSend.setName(CommunicationTemplate.get(groupSend.templateId).getName())
        }
        groupSend.setDeleted( false );
    }

    public List findRunning() {
        return CommunicationGroupSend.findRunning()
    }

}

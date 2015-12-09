/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.groupsend

import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.service.ServiceBase
import org.springframework.security.core.context.SecurityContextHolder

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
            groupSend.setName(CommunicationTemplate.get(groupSend.templateId).getName())
        }
        if(groupSend.getScheduledStartDate() != null)
        {
            //Validation to make sure date is not in the past
        }
        groupSend.setDeleted( false );
    }

    public List findRunning() {
        return CommunicationGroupSend.findRunning()
    }

}

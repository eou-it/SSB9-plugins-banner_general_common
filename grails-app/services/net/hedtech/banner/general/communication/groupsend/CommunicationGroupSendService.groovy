/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.groupsend

import net.hedtech.banner.service.ServiceBase
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional

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
        groupSend.setDeleted( false );
    }

    @Transactional
    public List findRunning() {
        return CommunicationGroupSend.findRunning()
    }

    public CommunicationGroupSend stopGroupSend( Long groupSendId ) {
        if (log.isDebugEnabled()) log.debug( "Stopping group send with id = " + groupSendId + "." )

        CommunicationGroupSend groupSend = get( groupSendId )

        if (groupSend.currentExecutionState.isTerminal()) {
            if (log.isWarnEnabled()) log.warn( "Group send with id = " + groupSendId + " has already concluded." )
            return groupSend
        }

        groupSend.currentExecutionState = CommunicationGroupSendExecutionState.Stopped

        // Note: Scheduled is not enabled for CR1, but this is how we would do the check:
//        if (groupSend.getScheduledStartJobID() != null && !groupSend.isStarted()) {
//            try {
//                jobSubmissionService.unSchedule( groupSend.getScheduledStartJobID() );
//            } catch (ApplicationException e) {
//                if (log.isErrorEnabled()) log.error( "Error trying to clean up scheduled group send start job while stopping group send with pk = " + groupSend.getPrimaryKey().getKeyValue() + ".", e );
//            }
//        }

        return update( groupSend )
    }

}

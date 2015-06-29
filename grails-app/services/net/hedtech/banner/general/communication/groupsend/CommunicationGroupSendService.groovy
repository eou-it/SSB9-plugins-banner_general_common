/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.groupsend

import grails.gorm.DetachedCriteria
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.ApplicationExceptionFactory
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
            groupSend.setName(CommunicationTemplate.get(groupSend.template.id).getName())
        }
        groupSend.setDeleted( false );
    }

    def preUpdate( domainModelOrMap ) {
        CommunicationGroupSend groupSend = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationGroupSend

        // Make sure we are not transitioning from a group send that already completed, erred, or was previously stopped.
        CommunicationGroupSend originalGroupSend = get( groupSend.id )
        if (originalGroupSend.currentExecutionState.isTerminal()) {
            log.error( "Group send with id = ${groupSend.id} has already concluded with execution state ${originalGroupSend.currentExecutionState.toString()}." )
//            throw ApplicationExceptionFactory.createApplicationException( CommunicationGroupSendService.class, "cannotStopConcludedGroupSend" )
            throw ApplicationExceptionFactory.createApplicationException( CommunicationGroupSendService.class, "cannotStopConcludedGroupSend" )
        }
    }

    public List findRunning() {
        return CommunicationGroupSend.findRunning()
    }

    public CommunicationGroupSend stopGroupSend( Long groupSendId ) {
        if (log.isDebugEnabled()) log.debug( "Stopping group send with id = ${groupSendId}." )

        CommunicationGroupSend groupSend = get( groupSendId )

        // Note: Scheduled is not enabled for CR1, but this is how we would do the check:
//        if (groupSend.getScheduledStartJobID() != null && !groupSend.isStarted()) {
//            try {
//                jobSubmissionService.unSchedule( groupSend.getScheduledStartJobID() );
//            } catch (ApplicationException e) {
//                if (log.isErrorEnabled()) log.error( "Error trying to clean up scheduled group send start job while stopping group send with pk = " + groupSend.getId() + ".", e );
//            }
//        }

        groupSend.currentExecutionState = CommunicationGroupSendExecutionState.Stopped
        groupSend.stopDate = new Date()

        return update( groupSend )
    }

    public CommunicationGroupSend completeGroupSend( Long groupSendId ) {
        if (log.isDebugEnabled()) log.debug( "Completing group send with id = " + groupSendId + "." )

        CommunicationGroupSend groupSend = get( groupSendId )

        groupSend.currentExecutionState = CommunicationGroupSendExecutionState.Complete
        groupSend.stopDate = new Date()
        //TODO: Figure out why ServiceBase.update is not working with this domain.
        return groupSend.save() //update( groupSend )
    }

}

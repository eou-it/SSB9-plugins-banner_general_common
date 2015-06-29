/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.groupsend

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.ApplicationExceptionFactory
import net.hedtech.banner.general.communication.field.CommunicationFieldCalculationService
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

    public CommunicationGroupSend stopGroupSend( Long groupSendId ) {
        if (log.isDebugEnabled()) log.debug( "Stopping group send with id = ${groupSendId}." )

        CommunicationGroupSend groupSend = get( groupSendId )

        if (groupSend.currentExecutionState.isTerminal()) {
            log.error( "Group send with id = ${groupSend.id} has already concluded with execution state ${groupSend.currentExecutionState.toString()}." )
            throw ApplicationExceptionFactory.createApplicationException( CommunicationGroupSendService.class, "cannotStopConcludedGroupSend" )
        }

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

        groupSend = update( groupSend )

        // fetch any communication jobs for this group send and marked as stopped
        stopPendingCommunicationJobs( groupSend.id )

        return groupSend
    }

    public CommunicationGroupSend completeGroupSend( Long groupSendId ) {
        if (log.isDebugEnabled()) log.debug( "Completing group send with id = " + groupSendId + "." )

        CommunicationGroupSend groupSend = get( groupSendId )

        groupSend.currentExecutionState = CommunicationGroupSendExecutionState.Complete
        groupSend.stopDate = new Date()
        //TODO: Figure out why ServiceBase.update is not working with this domain.
        return groupSend.save() //update( groupSend )
    }

    private void stopPendingCommunicationJobs( Long groupSendId ) {
        def Sql sql
        try {
            Connection connection = (Connection) sessionFactory.getCurrentSession().connection()
            sql = new Sql( (Connection) sessionFactory.getCurrentSession().connection() )
            int rowsUpdated = sql.executeUpdate( "update GCBCJOB set GCBCJOB_STATUS='STOPPED', GCBCJOB_ACTIVITY_DATE = SYSDATE where " +
                    "GCBCJOB_STATUS in ('PENDING', 'DISPATCHED') and GCBCJOB_REFERENCE_ID in " +
                    "(select GCRGSIM_REFERENCE_ID from GCRGSIM where GCRGSIM_GROUP_SEND_ID = ${groupSendId} and GCRGSIM_CURRENT_STATE = 'Complete')" )
        } catch (SQLException e) {
            throw new ApplicationException( CommunicationGroupSendService, e.message )
        } catch (Exception e) {
            throw new ApplicationException( CommunicationGroupSendService, e.message )
        } finally {
            sql?.close()
        }
    }

}

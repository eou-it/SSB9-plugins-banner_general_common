/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.organization.CommunicationOrganizationService
import net.hedtech.banner.general.communication.population.CommunicationPopulation
import net.hedtech.banner.general.communication.population.CommunicationPopulationCompositeService
import net.hedtech.banner.general.communication.population.CommunicationPopulationVersion
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionListService
import net.hedtech.banner.general.communication.template.CommunicationTemplateService
import net.hedtech.banner.general.scheduler.SchedulerJobService
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional

import java.sql.Connection
import java.sql.SQLException

/**
 * Communication Group Send Composite Service is responsible for initiating and processing group send communications.
 * Controllers and other client code should generally work through this service for interacting with group send
 * behavior and objects.
 */
@Transactional
class CommunicationGroupSendCompositeService {

    def log = Logger.getLogger( this.getClass() )
    CommunicationGroupSendService communicationGroupSendService
    CommunicationTemplateService communicationTemplateService
    CommunicationPopulationSelectionListService communicationPopulationSelectionListService
    CommunicationOrganizationService communicationOrganizationService
    CommunicationPopulationCompositeService communicationPopulationCompositeService
    SchedulerJobService schedulerJobService
    def sessionFactory
    def dataSource


    /**
     * Initiate the sending of a communication to a set of prospect recipients
     * @param request the communication to initiate
     */
    public CommunicationGroupSend sendAsynchronousGroupCommunication( CommunicationGroupSendRequest request ) {
        if (log.isDebugEnabled()) log.debug( "Method sendAsynchronousGroupCommunication reached." );
        if (!request) throw new IllegalArgumentException( "request may not be null!" )

        String jobName = request.getName();
        if(!jobName || jobName.isEmpty()) {
            throw CommunicationExceptionFactory.createNotFoundException( CommunicationGroupSendCompositeService, "@@r1:jobNameInvalid@@" )
        }

        String bannerUser = SecurityContextHolder.context.authentication.principal.getOracleUserName()

        CommunicationGroupSend groupSend = new CommunicationGroupSend();
        groupSend.templateId = request.getTemplateId()
        groupSend.populationId = request.getPopulationId()
        groupSend.organizationId = request.getOrganizationId()
        groupSend.name = jobName
        groupSend.scheduledStartDate = request.scheduledStartDate
        groupSend.recalculateOnSend = request.getRecalculateOnSend()
        groupSend.jobId = request.referenceId

        if(!groupSend.recalculateOnSend) {
            CommunicationPopulationVersion populationVersion = CommunicationPopulationVersion.findLatestByPopulationIdAndCreatedBy( groupSend.getPopulationId(), bannerUser )
            groupSend.populationVersionId = populationVersion.id

        }
        groupSend = communicationGroupSendService.create( groupSend )

        String mepCode = groupSend.mepCode
        if (request.scheduledStartDate) {
            groupSend = scheduleGroupSend(request, bannerUser, mepCode, groupSend)
        } else {
            groupSend = scheduleGroupSendImmediately( groupSend, bannerUser )
        }

        return groupSend
    }

    /**
     * Deletes a group send and it's dependent objects. The group send must not bre running otherwise an
     * application exception will be thrown.
     *
     * @param groupSendId the long id of the group send
     */
    public void deleteGroupSend( Long groupSendId ) {
        if (log.isDebugEnabled()) {
            log.debug( "deleteGroupSend for id = ${groupSendId}." )
        }

        CommunicationGroupSend groupSend = (CommunicationGroupSend) communicationGroupSendService.get( groupSendId )
        if (!groupSend) {
            throw CommunicationExceptionFactory.createNotFoundException( groupSendId, CommunicationGroupSend.class )
        }

        if (groupSend.currentExecutionState.running) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationGroupSendCompositeService.class, "cannotDeleteRunningGroupSend" )
        }

        //if group send is scheduled
        String bannerUser = SecurityContextHolder.context.authentication.principal.getOracleUserName()
        if(groupSend.currentExecutionState == CommunicationGroupSendExecutionState.Scheduled) {
            def result = schedulerJobService.deleteScheduledJob(groupSend.jobId, "communicationGroupSendCompositeService", "generateGroupSendItems")
        }
        else {
            //if Group send is not scheduled then remove job and recipient data
            deleteCommunicationJobsByGroupSendId(groupSendId)
            deleteRecipientDataByGroupSendId(groupSendId)
        }

        communicationGroupSendService.delete( groupSendId )
    }

    /**
     * Stops a group send. The group send must be running otherwise an application exception will be thrown.
     * @param groupSendId the long id of the group send
     * @return the updated (stopped) group send
     */
    public CommunicationGroupSend stopGroupSend( Long groupSendId ) {
        if (log.isDebugEnabled()) log.debug( "Stopping group send with id = ${groupSendId}." )

        CommunicationGroupSend groupSend = communicationGroupSendService.get( groupSendId )

        if (groupSend.currentExecutionState.isTerminal()) {
            log.error( "Group send with id = ${groupSend.id} has already concluded with execution state ${groupSend.currentExecutionState.toString()}." )
            throw CommunicationExceptionFactory.createApplicationException( CommunicationGroupSendService.class, "cannotStopConcludedGroupSend" )
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

        groupSend = saveGroupSend( groupSend )

        // fetch any communication jobs for this group send and marked as stopped
        stopPendingCommunicationJobs( groupSend.id )

        return groupSend
    }

    /**
     * Marks a group send as complete.
     * @param groupSendId the id of the group send.
     * @return the updated group send
     */
    public CommunicationGroupSend completeGroupSend( Long groupSendId ) {
        if (log.isDebugEnabled()) log.debug( "Completing group send with id = " + groupSendId + "." )

        CommunicationGroupSend aGroupSend = communicationGroupSendService.get( groupSendId )
        aGroupSend.currentExecutionState = CommunicationGroupSendExecutionState.Complete
        aGroupSend.stopDate = new Date()
        return saveGroupSend( aGroupSend )
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // Scheduling service callback job methods
    //////////////////////////////////////////////////////////////////////////////////////

    /**
     * This method is called by the scheduler to regenerate a population list specifically for the group send
     * and change the state of the group send to next state.
     */
    public CommunicationGroupSend calculatePopulationForGroupSend( Map parameters ) {
        Long groupSendId = parameters.get( "groupSendId" ) as Long
        assert( groupSendId )
        if (log.isDebugEnabled()) {
            log.debug( "Calling calculatePopulationForGroupSend for groupSendId = ${groupSendId}.")
        }

        CommunicationGroupSend groupSend = CommunicationGroupSend.get( groupSendId )
        if (!groupSend) {
            throw new ApplicationException("groupSend", new NotFoundException())
        }

        try {
            CommunicationPopulation population = CommunicationPopulation.fetchById( groupSend.getPopulationId() )
            if (!population) {
                throw new ApplicationException("population", new NotFoundException())
            }

            if (!groupSend.populationVersionId) {
                groupSend.currentExecutionState = CommunicationGroupSendExecutionState.Calculating

                //Calculate the population version
                CommunicationPopulationVersion populationVersion = communicationPopulationCompositeService.calculatePopulationForGroupSend(population, groupSend.createdBy)
                groupSend.populationVersionId = populationVersion.id
                groupSend = communicationGroupSendService.update(groupSend)
                // double check this is the correct user
            }
            groupSend = generateGroupSendItemsImpl( groupSend )
        } catch(Throwable t) {
            log.error(t.getMessage())
            groupSend.currentExecutionState = CommunicationGroupSendExecutionState.Error
            groupSend.errorCode = CommunicationErrorCode.UNKNOWN_ERROR
            groupSend.errorText = t.getMessage()
            groupSend = communicationGroupSendService.update(groupSend)
        }
        return groupSend
    }

    /**
     * This method is called by the scheduler to create the group send items and move the state of
     * the group send to processing.
     */
    public CommunicationGroupSend generateGroupSendItems( Map parameters ) {
        Long groupSendId = parameters.get( "groupSendId" ) as Long
        assert( groupSendId )

        if (log.isDebugEnabled()) {
            log.debug( "Calling generateGroupSendItems for groupSendId = ${groupSendId}.")
        }
        CommunicationGroupSend groupSend = CommunicationGroupSend.get(groupSendId)
        if (!groupSend) {
            throw new ApplicationException("groupSend", new NotFoundException())
        }

        try {
            groupSend = generateGroupSendItemsImpl( groupSend )
        } catch(Throwable t) {
            log.error(t.getMessage())
            groupSend.currentExecutionState = CommunicationGroupSendExecutionState.Error
            groupSend.errorCode = CommunicationErrorCode.UNKNOWN_ERROR
            groupSend.errorText = t.getMessage()
            groupSend = communicationGroupSendService.update(groupSend)
        }
        return groupSend
    }

    private CommunicationGroupSend scheduleGroupSendImmediately( CommunicationGroupSend groupSend, String bannerUser, String scheduledJobId = UUID.randomUUID().toString() ) {
        if (!groupSend.populationVersionId) {
            CommunicationPopulationVersion populationVersion = CommunicationPopulationVersion.findLatestByPopulationIdAndCreatedBy( groupSend.getPopulationId(), bannerUser )
            if (populationVersion) {
                groupSend.populationVersionId = populationVersion.id
            } else {
                // TODO: create exception in messages.properties and confirm if we would rather calculate on the fly for this scenario.
                throw CommunicationExceptionFactory.createApplicationException(CommunicationGroupSendService.class, "populationNotCalculatedForUser" )
            }
        }

        assert( groupSend.populationVersionId )

        schedulerJobService.scheduleNowServiceMethod(
                scheduledJobId,
                bannerUser,
                groupSend.mepCode,
                "communicationGroupSendCompositeService",
                "generateGroupSendItems",
                ["groupSendId": groupSend.id]
        )
        groupSend.currentExecutionState = CommunicationGroupSendExecutionState.Processing
        groupSend = communicationGroupSendService.update(groupSend)
        return groupSend
    }


    private CommunicationGroupSend scheduleGroupSend(CommunicationGroupSendRequest request, String bannerUser, String mepCode, CommunicationGroupSend groupSend) {
        Date now = new Date(System.currentTimeMillis())
        if (now.after(request.scheduledStartDate)) {
            throw CommunicationExceptionFactory.createApplicationException(CommunicationGroupSendService.class, "invalidScheduledDate")
        }
        if(request.recalculateOnSend) {
            schedulerJobService.scheduleServiceMethod(request.scheduledStartDate, request.referenceId, bannerUser, mepCode, "communicationGroupSendCompositeService", "calculatePopulationForGroupSend", ["groupSendId": groupSend.id])
        }
        else
        {
            schedulerJobService.scheduleServiceMethod(request.scheduledStartDate, request.referenceId, bannerUser, mepCode, "communicationGroupSendCompositeService", "generateGroupSendItems", ["groupSendId": groupSend.id])
        }
        groupSend.currentExecutionState = CommunicationGroupSendExecutionState.Scheduled
        groupSend = communicationGroupSendService.update(groupSend)
        groupSend
    }

    private CommunicationGroupSend generateGroupSendItemsImpl( CommunicationGroupSend groupSend ) {
        // We'll created the group send items synchronously for now until we have support for scheduling.
        // The individual group send items will still be processed asynchronously via the framework.
        createGroupSendItems(groupSend)
        groupSend.currentExecutionState = CommunicationGroupSendExecutionState.Processing
        groupSend = communicationGroupSendService.update(groupSend)
        groupSend
    }

    private CommunicationGroupSend saveGroupSend( CommunicationGroupSend groupSend ) {
        //TODO: Figure out why ServiceBase.update is not working with this domain.
        return groupSend.save( flush:true ) //update( groupSend )
    }

    /**
     * Removes all communication job records referenced by a group send id.
     *
     * @param groupSendId the long id of the group send.
     */
    private void deleteCommunicationJobsByGroupSendId( Long groupSendId ) {
        if (log.isDebugEnabled()) {
            log.debug( "Attempting to delete all communication jobs referenced by group send id = ${groupSendId}.")
        }
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            int rows = sql.executeUpdate("DELETE FROM gcbcjob a WHERE EXISTS (SELECT b.gcrgsim_surrogate_id FROM gcrgsim b, gcbgsnd c WHERE a.gcbcjob_reference_id = b.gcrgsim_reference_id AND b.gcrgsim_group_send_id = c.gcbgsnd_surrogate_id AND c.gcbgsnd_surrogate_id = ?)",
                    [ groupSendId ] )
            if (log.isDebugEnabled()) {
                log.debug( "Deleting ${rows} communication jobs referenced by group send id = ${groupSendId}.")
            }
        } catch (Exception e) {
            log.error( e )
            throw e
        } finally {
            sql?.close()
        }
    }


    /**
     * Removes all recipient data records referenced by a group send id.
     *
     * @param groupSendId the long id of the group send.
     */
    private void deleteRecipientDataByGroupSendId( Long groupSendId ) {
        if (log.isDebugEnabled()) {
            log.debug( "Attempting to delete all recipient data referenced by group send id = ${groupSendId}.")
        }
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            int rows = sql.executeUpdate("DELETE FROM gcbrdat a WHERE EXISTS (SELECT b.gcrgsim_surrogate_id FROM gcrgsim b, gcbgsnd c WHERE a.gcbrdat_reference_id = b.gcrgsim_reference_id AND b.gcrgsim_group_send_id = c.gcbgsnd_surrogate_id AND c.gcbgsnd_surrogate_id = ?)",
                    [ groupSendId ] )
            if (log.isDebugEnabled()) {
                log.debug( "Deleting ${rows} recipient data referenced by group send id = ${groupSendId}.")
            }
        } catch (Exception e) {
            log.error( e )
            throw e
        } finally {
            sql?.close()
        }
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
            throw CommunicationExceptionFactory.createApplicationException( CommunicationGroupSendService, e )
        } catch (Exception e) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationGroupSendService, e )
        } finally {
            sql?.close()
        }
    }

    private void createGroupSendItems( CommunicationGroupSend groupSend ) {
        if (log.isDebugEnabled()) log.debug( "Generating group send item records for group send with id = " + groupSend?.id );
        def sql
        try {
            def ctx = ServletContextHolder.servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)
            def sessionFactory = ctx.sessionFactory
            def session = sessionFactory.currentSession
            sql = new Sql(session.connection())

            sql.execute(
            [
                state:CommunicationGroupSendItemExecutionState.Ready.toString(),
                group_send_key:groupSend.id
            ],
            """
            INSERT INTO gcrgsim (gcrgsim_group_send_id, gcrgsim_pidm, gcrgsim_creationdatetime
                                            ,gcrgsim_current_state, gcrgsim_reference_id, gcrgsim_user_id, gcrgsim_activity_date, gcrgsim_started_date)
                           SELECT gcbgsnd_surrogate_id
                                 ,gcrlent_pidm
                                 ,SYSDATE
                                 , :state
                                 , SYS_GUID()
                                 ,gcbgsnd_user_id
                                 ,SYSDATE
                                 ,SYSDATE
                             FROM gcrslis, gcrlent, gcbgsnd, gcrpvid
                            WHERE gcbgsnd_popversion_id = gcrpvid_popv_id
                                and gcrslis_surrogate_id = gcrpvid_slis_id
                                  AND gcrlent_slis_id = gcrslis_surrogate_id
                                  AND gcbgsnd_surrogate_id = :group_send_key
            """ )

            if (log.isDebugEnabled()) log.debug( "Created " + sql.updateCount + " group send item records for group send with id = " + groupSend.id )
        } catch (SQLException ae) {
            log.debug "SqlException in INSERT INTO gcrgsim ${ae}"
            log.debug ae.stackTrace
            throw ae
        } catch (Exception ae) {
            log.debug "Exception in INSERT INTO gcrgsim ${ae}"
            log.debug ae.stackTrace
            throw ae
        } finally {
            sql?.close()
        }

    }

}
/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.recurrence

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSend
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendCompositeService
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendExecutionState
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendRequest
import net.hedtech.banner.general.communication.parameter.CommunicationParameterType
import net.hedtech.banner.general.communication.template.CommunicationTemplateParameterView
import net.hedtech.banner.general.scheduler.SchedulerErrorContext
import net.hedtech.banner.general.scheduler.SchedulerJobContext
import net.hedtech.banner.general.scheduler.SchedulerJobReceipt
import net.hedtech.banner.general.scheduler.SchedulerJobService
import org.apache.commons.lang.NotImplementedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional

import java.text.SimpleDateFormat

/**
 * Communication Recurrent Message Composite Service is responsible for initiating and processing recurrent communications.
 * Controllers and other client code should generally work through this service for interacting with recurrent message
 * behavior and objects.
 */
@Transactional
class CommunicationRecurrentMessageCompositeService {

    CommunicationRecurrentMessageService communicationRecurrentMessageService
    CommunicationGroupSendCompositeService communicationGroupSendCompositeService
    SchedulerJobService schedulerJobService
    def sessionFactory
    def dataSource

    /**
     * Initiate the sending of a communication to a set of prospect recipients
     * @param request the communication to initiate
     */
    public CommunicationRecurrentMessage sendRecurrentMessageCommunication(CommunicationGroupSendRequest request ) {
        if (log.isDebugEnabled()) log.debug( "Method sendRecurrentMessageCommunication reached." );
        if (!request) throw new IllegalArgumentException( "request may not be null!" )

        String jobName = request.getName();
        if(!jobName || jobName.isEmpty()) {
            throw CommunicationExceptionFactory.createNotFoundException( CommunicationRecurrentMessageCompositeService, "@@r1:jobNameInvalid@@" )
        }

        CommunicationRecurrentMessage recurrentMessage = new CommunicationRecurrentMessage();
        recurrentMessage.templateId = request.getTemplateId()
        recurrentMessage.populationId = request.getPopulationId()
        recurrentMessage.organizationId = request.getOrganizationId()
        recurrentMessage.eventId = request.eventId
        recurrentMessage.name = jobName
        recurrentMessage.description = jobName
        recurrentMessage.cronExpression = request.cronExpression
        recurrentMessage.cronTimezone = request.cronTimezone
        recurrentMessage.startDate = request.scheduledStartDate
        recurrentMessage.endDate = request.endDate
        recurrentMessage.noOfOccurrences = request.noOfOccurrences
        recurrentMessage.recalculateOnSend = request.getRecalculateOnSend()
        recurrentMessage.jobId = request.referenceId
        recurrentMessage.totalCount = 0;
        recurrentMessage.successCount = 0;
        recurrentMessage.failureCount = 0;
        String bannerUser = SecurityContextHolder.context.authentication.principal.getOracleUserName()

        recurrentMessage.setParameterNameValueMap( request.getParameterNameValueMap() )
        validateTemplateAndParameters( recurrentMessage )

        recurrentMessage = (CommunicationRecurrentMessage) communicationRecurrentMessageService.create( recurrentMessage )
        recurrentMessage = scheduleRecurrentMessage( recurrentMessage, bannerUser )

        return recurrentMessage
    }


    private CommunicationRecurrentMessage scheduleRecurrentMessage( CommunicationRecurrentMessage recurrentMessage, String bannerUser ) {

        Date now = new Date()
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
        Date today = sdf.parse(sdf.format(now))

        Date scheduledDate = sdf.parse(sdf.format(recurrentMessage.startDate))

        if (today.compareTo(scheduledDate) != 0 ) {
            if(now.after(recurrentMessage.startDate)) {
                throw CommunicationExceptionFactory.createApplicationException(CommunicationRecurrentMessageService.class, "invalidScheduleStartDate")
            }
        }
        if (recurrentMessage.endDate) {
            if (now.after(recurrentMessage.endDate)) {
                throw CommunicationExceptionFactory.createApplicationException(CommunicationRecurrentMessageService.class, "invalidScheduleEndDate")
            }
        }

        SchedulerJobContext jobContext = new SchedulerJobContext( recurrentMessage.jobId )
                .setBannerUser( bannerUser )
                .setMepCode( recurrentMessage.mepCode )
                .setCronSchedule( recurrentMessage.cronExpression )
                .setCronScheduleTimezone(recurrentMessage.cronTimezone)
                .setScheduledStartDate(recurrentMessage.startDate)
                .setEndDate(recurrentMessage.endDate)
                .setParameter( "recurrentMessageId", recurrentMessage.id )

        jobContext.setJobHandle( "communicationRecurrentMessageCompositeService", "generateGroupSendFired" )
                .setErrorHandle( "communicationRecurrentMessageCompositeService", "generateGroupSendFailed" )

        SchedulerJobReceipt jobReceipt = schedulerJobService.scheduleCronServiceMethod( jobContext )
        recurrentMessage.jobId = jobReceipt.jobId
        recurrentMessage.groupId = jobReceipt.groupId
        recurrentMessage = (CommunicationRecurrentMessage) communicationRecurrentMessageService.update( recurrentMessage )
        return recurrentMessage
    }

    private void reScheduleRecurrentMessage( CommunicationRecurrentMessage recurrentMessage, String bannerUser ) {

        Date now = new Date()
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
        Date today = sdf.parse(sdf.format(now))

        Date scheduledDate = sdf.parse(sdf.format(recurrentMessage.startDate))

        if (today.compareTo(scheduledDate) != 0 && recurrentMessage.currentExecutionState.equals(CommunicationGroupSendExecutionState.New)) {
            if(now.after(recurrentMessage.startDate)) {
                throw CommunicationExceptionFactory.createApplicationException(CommunicationRecurrentMessageService.class, "invalidScheduleStartDate")
            }
        }
        if (recurrentMessage.endDate) {
            if (now.after(recurrentMessage.endDate)) {
                throw CommunicationExceptionFactory.createApplicationException(CommunicationRecurrentMessageService.class, "invalidScheduleEndDate")
            }
        }

        SchedulerJobContext jobContext = new SchedulerJobContext( recurrentMessage.jobId )
                .setBannerUser( bannerUser )
                .setMepCode( recurrentMessage.mepCode )
                .setCronSchedule( recurrentMessage.cronExpression )
                .setCronScheduleTimezone(recurrentMessage.cronTimezone)
                .setScheduledStartDate(recurrentMessage.startDate)
                .setEndDate(recurrentMessage.endDate)
                .setParameter( "recurrentMessageId", recurrentMessage.id )

        jobContext.setJobHandle( "communicationRecurrentMessageCompositeService", "generateGroupSendFired" )
                .setErrorHandle( "communicationRecurrentMessageCompositeService", "generateGroupSendFailed" )

        SchedulerJobReceipt jobReceipt = schedulerJobService.reScheduleCronServiceMethod( jobContext )
    }

    public CommunicationRecurrentMessage generateGroupSendFired( SchedulerJobContext jobContext ) {
        return generateGroupSend( jobContext.parameters )
    }

    public CommunicationRecurrentMessage generateGroupSendFailed(SchedulerErrorContext errorContext ) {
        return scheduledGroupSendCallbackFailed( errorContext )
    }

    /**
     * This method is called by the scheduler to create the group send items and move the state of
     * the group send to processing.
     */
    private CommunicationRecurrentMessage generateGroupSend( Map parameters ) {
        Long recurrentMessageId = parameters.get( "recurrentMessageId" ) as Long
        assert( recurrentMessageId )

        if (log.isDebugEnabled()) {
            log.debug( "Calling generateGroupSend for recurrentMessageId = ${recurrentMessageId}.")
        }
        CommunicationRecurrentMessage recurrentMessage = CommunicationRecurrentMessage.get(recurrentMessageId)
        if (!recurrentMessage) {
            throw new ApplicationException("recurrentMessage", new NotFoundException())
        }

        if(!recurrentMessage.currentExecutionState.isTerminalWithoutErrors()) {
            try {
                recurrentMessage = generateGroupSendImpl(recurrentMessage)
            } catch (Throwable t) {
                log.error(t.getMessage())
                recurrentMessage.setCurrentExecutionState(CommunicationGroupSendExecutionState.Error)
                recurrentMessage.errorCode = CommunicationErrorCode.UNKNOWN_ERROR
                recurrentMessage.errorText = t.getMessage()
                recurrentMessage.failureCount = recurrentMessage.failureCount + 1;
                recurrentMessage.totalCount = recurrentMessage.totalCount + 1;
                recurrentMessage = (CommunicationRecurrentMessage) communicationRecurrentMessageService.update(recurrentMessage)
            }
        }
        return recurrentMessage
    }

    private CommunicationRecurrentMessage generateGroupSendImpl( CommunicationRecurrentMessage recurrentMessage ) {
        // The individual group sends will still be processed asynchronously via the framework.
        CommunicationGroupSendRequest request = new CommunicationGroupSendRequest()
        request.referenceId = UUID.randomUUID().toString()
        request.name = recurrentMessage.name
        request.populationId = recurrentMessage.populationId
        request.templateId = recurrentMessage.templateId
        request.organizationId = recurrentMessage.organizationId
        request.eventId = recurrentMessage.eventId
        request.cronExpression = recurrentMessage.cronExpression
        request.recalculateOnSend = recurrentMessage.recalculateOnSend
        request.parameterNameValueMap = recurrentMessage.parameterNameValueMap
        request.recurrentMessageId = recurrentMessage.id

        try {
            CommunicationGroupSend groupSend = communicationGroupSendCompositeService.sendAsynchronousGroupCommunication(request)

//      Get the recurrent message again as the job delete trigger would update the recurrent message object
            if (!recurrentMessage.currentExecutionState.isTerminal()) {
                recurrentMessage.setCurrentExecutionState(CommunicationGroupSendExecutionState.Scheduled)
            }
            recurrentMessage.successCount = recurrentMessage.successCount + 1;
            recurrentMessage.totalCount = recurrentMessage.totalCount + 1;

            recurrentMessage = (CommunicationRecurrentMessage) communicationRecurrentMessageService.update(recurrentMessage)

            //if no of occurences <= total count, delete the cron_trigger entry
            if (recurrentMessage.noOfOccurrences && recurrentMessage.noOfOccurrences <= recurrentMessage.totalCount) {
                if (recurrentMessage.jobId != null) {
                    schedulerJobService.deleteScheduledJob(recurrentMessage.jobId, recurrentMessage.groupId)
                }
            }
        }catch(Throwable t) {
            log.error("Error occurred when creating group send from recurrent message " + t.printStackTrace())
            throw t;
        }
        return recurrentMessage
    }

    private CommunicationRecurrentMessage scheduledGroupSendCallbackFailed( SchedulerErrorContext errorContext ) {
        Long recurrentMessageId = errorContext.jobContext.getParameter("recurrentMessageId") as Long
        if (log.isDebugEnabled()) {
            log.debug("${errorContext.jobContext.errorHandle} called for recurrentMessageId = ${recurrentMessageId} with message = ${errorContext?.cause?.message}")
        }

        CommunicationRecurrentMessage recurrentMessage = CommunicationRecurrentMessage.get(recurrentMessageId)
        if (!recurrentMessage) {
            throw new ApplicationException("recurrentMessage", new NotFoundException())
        }

        recurrentMessage.setCurrentExecutionState(CommunicationGroupSendExecutionState.Error)

        if (errorContext.cause) {
            recurrentMessage.errorCode = CommunicationErrorCode.UNKNOWN_ERROR
            recurrentMessage.errorText = errorContext.cause.message
        } else {
            recurrentMessage.errorCode = CommunicationErrorCode.UNKNOWN_ERROR
        }
        recurrentMessage.failureCount = recurrentMessage.failureCount + 1;
        recurrentMessage.totalCount = recurrentMessage.totalCount + 1;
        recurrentMessage = (CommunicationRecurrentMessage) communicationRecurrentMessageService.update(recurrentMessage)

        //if no of occurences <= total count, delete the cron_trigger entry
        if (recurrentMessage.noOfOccurrences && recurrentMessage.noOfOccurrences <= recurrentMessage.totalCount) {
            if (recurrentMessage.jobId != null) {
                schedulerJobService.deleteScheduledJob(recurrentMessage.jobId, recurrentMessage.groupId)
            }
        }

        return recurrentMessage
    }

    /**
     * Stops a recurrent message. The recurrent message must be running otherwise an application exception will be thrown.
     * @param recurrentMessageId the long id of the recurrent message
     * @return the updated (stopped) recurrent message
     */
    public CommunicationRecurrentMessage stopRecurrentMessage( Long recurrentMessageId ) {
        if (log.isDebugEnabled()) log.debug( "Stopping recurrent message with id = ${recurrentMessageId}." )

        CommunicationRecurrentMessage recurrentMessage = (CommunicationRecurrentMessage) communicationRecurrentMessageService.get( recurrentMessageId )

        if (recurrentMessage.currentExecutionState.isTerminal()) {
            log.warn( "Recurrent message with id = ${recurrentMessage.id} has already concluded with execution state ${recurrentMessage.currentExecutionState.toString()}." )
            throw CommunicationExceptionFactory.createApplicationException( CommunicationRecurrentMessageService.class, "cannotStopConcludedRecurrentMessage" )
        }

        if (recurrentMessage.jobId != null) {
            this.schedulerJobService.deleteScheduledJob( recurrentMessage.jobId, recurrentMessage.groupId )
        }

        //Refresh the recuurent message as the delete scheduled job runs the DB trigger to update the recurrent message status to Complete
        recurrentMessage.refresh()
        recurrentMessage.stoppedDate = new Date()
        recurrentMessage.currentExecutionState = CommunicationGroupSendExecutionState.Stopped
        recurrentMessage = communicationRecurrentMessageService.update(recurrentMessage)

        return recurrentMessage
    }

    /**
     * Deletes a recurrent message and it's dependent objects. The recurrent messages must not be running otherwise an
     * application exception will be thrown.
     *
     * @param recurrentMessageId the long id of the recurrent message
     */
    public void deleteRecurrentMessage( Long recurrentMessageId ) {
        if (log.isDebugEnabled()) {
            log.debug( "deleteRecurrentMessage for id = ${recurrentMessageId}." )
        }

        CommunicationRecurrentMessage recurrentMessage = CommunicationRecurrentMessage.get(recurrentMessageId)
        if (!recurrentMessage) {
            throw CommunicationExceptionFactory.createNotFoundException( recurrentMessageId, CommunicationRecurrentMessage.class )
        }

        if (!recurrentMessage.currentExecutionState.equals(CommunicationGroupSendExecutionState.New) && !recurrentMessage.currentExecutionState.terminal) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationRecurrentMessageCompositeService.class, "cannotDeleteRunningRecurrentMessage" )
        }

        if(recurrentMessage.jobId != null) {
            schedulerJobService.deleteScheduledJob(recurrentMessage.jobId, recurrentMessage.groupId)
        }

        //delete all group sends belonging to this recurrent message. For each group send, then remove job and recipient data
        List<CommunicationGroupSend> groupSends = CommunicationGroupSend.findByRecurrentMessageId(recurrentMessageId)
        for(CommunicationGroupSend groupSend : groupSends) {
            communicationGroupSendCompositeService.deleteGroupSend(groupSend.id)
        }

        //Refresh the recuurent message as the delete scheduled job runs the DB trigger to update the recurrent message status to Complete
        recurrentMessage.refresh()
        communicationRecurrentMessageService.delete( recurrentMessage )
    }

    public CommunicationRecurrentMessage updateRecurrentMessageCommunication(CommunicationRecurrentMessage recurrentMessage ) {
        if (log.isDebugEnabled()) log.debug( "Method updateRecurrentMessageCommunication reached." );

        boolean rescheduleNeeded = false;
        CommunicationRecurrentMessage oldRecurrentMessage = CommunicationRecurrentMessage.get(recurrentMessage.id)
        if(recurrentMessage.cronExpression != null && !oldRecurrentMessage.cronExpression.equalsIgnoreCase(recurrentMessage.cronExpression)) {
            rescheduleNeeded = true;
        }

        Date now = new Date()
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
        Date today = sdf.parse(sdf.format(now))

        Date scheduledDate = sdf.parse(sdf.format(recurrentMessage.startDate))
        Date oldScheduledDate = sdf.parse(sdf.format(oldRecurrentMessage.startDate))

        if(today.compareTo(scheduledDate)== 0) {
            if(today.compareTo(oldScheduledDate) == 0) {
                //no change to start date as it the scheduled date is for today
                recurrentMessage.startDate = oldRecurrentMessage.startDate
            } else {
                recurrentMessage.startDate = now
                rescheduleNeeded = true;
            }
        }

        if(recurrentMessage.startDate != null && oldRecurrentMessage.startDate.compareTo(recurrentMessage.startDate) != 0
            && recurrentMessage.currentExecutionState.equals(CommunicationGroupSendExecutionState.New)) {
            rescheduleNeeded = true;
        }

        if(recurrentMessage.endDate != null && !recurrentMessage.currentExecutionState.isTerminal()) {
            if(oldRecurrentMessage.endDate != null) {
                //there was an update to existing end date, so compare them
                if(oldRecurrentMessage.endDate.compareTo(recurrentMessage.endDate) != 0) {
                    rescheduleNeeded = true;
                }
            }  else {
                //new end date added
                rescheduleNeeded = true;
            }
        }

        recurrentMessage.createdBy = oldRecurrentMessage.createdBy
        recurrentMessage.creationDateTime = oldRecurrentMessage.creationDateTime
        recurrentMessage.parameterNameValueMap = oldRecurrentMessage.parameterNameValueMap
        recurrentMessage.jobId = oldRecurrentMessage.jobId
        recurrentMessage.groupId = oldRecurrentMessage.groupId
        recurrentMessage = (CommunicationRecurrentMessage) communicationRecurrentMessageService.update( recurrentMessage )

        if(rescheduleNeeded) {
            String bannerUser = SecurityContextHolder.context.authentication.principal.getOracleUserName()

            reScheduleRecurrentMessage(recurrentMessage, bannerUser)
        }


        return recurrentMessage
    }

    private void validateTemplateAndParameters(CommunicationRecurrentMessage recurrentMessage) {
        if (recurrentMessage.templateId == null) {
            throw CommunicationExceptionFactory.createApplicationException(CommunicationRecurrentMessageCompositeService, "templateIsRequired")
        }
        List templateParameterList = CommunicationTemplateParameterView.findAllByTemplateId(recurrentMessage.templateId)
        if (templateParameterList != null) {
            templateParameterList.each { CommunicationTemplateParameterView templateParameter ->
                Object value = recurrentMessage.getParameterNameValueMap().get(templateParameter.parameterName)?.value
                if (value == null) {
                    throw CommunicationExceptionFactory.createApplicationException(CommunicationRecurrentMessageCompositeService, "missingParameterValue", templateParameter.parameterName)
                }
                if (templateParameter.parameterType == CommunicationParameterType.TEXT) {
                    if (!(value instanceof String)) {
                        throw CommunicationExceptionFactory.createApplicationException(CommunicationRecurrentMessageCompositeService, "invalidParameterValue", templateParameter.parameterName)
                    }
                    String stringValue = (String) value
                    if (stringValue.length() == 0) {
                        throw CommunicationExceptionFactory.createApplicationException(CommunicationRecurrentMessageCompositeService, "missingParameterValue", templateParameter.parameterName)
                    }
                } else if (templateParameter.parameterType == CommunicationParameterType.NUMBER) {
                    if (!(value instanceof Number)) {
                        throw CommunicationExceptionFactory.createApplicationException(CommunicationRecurrentMessageCompositeService, "invalidParameterValue", templateParameter.parameterName)
                    }
                } else if (templateParameter.parameterType == CommunicationParameterType.DATE) {
                    if (!(value instanceof Date)) {
                        throw CommunicationExceptionFactory.createApplicationException(CommunicationRecurrentMessageCompositeService, "invalidParameterValue", templateParameter.parameterName)
                    }
                } else {
                    throw new NotImplementedException("Unhandled template parameter type when validating send parameter values.")
                }
            }
        }
    }
}

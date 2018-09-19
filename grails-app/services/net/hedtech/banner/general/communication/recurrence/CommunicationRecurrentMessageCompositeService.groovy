package net.hedtech.banner.general.communication.recurrence

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSend
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendCompositeService
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendExecutionState
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendRequest
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendService
import net.hedtech.banner.general.communication.recurrence.CommunicationRecurrentMessage
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendRequest
import net.hedtech.banner.general.communication.parameter.CommunicationParameterType
import net.hedtech.banner.general.communication.population.CommunicationPopulation
import net.hedtech.banner.general.communication.population.CommunicationPopulationQueryAssociation
import net.hedtech.banner.general.communication.template.CommunicationTemplateParameterView
import net.hedtech.banner.general.scheduler.SchedulerErrorContext
import net.hedtech.banner.general.scheduler.SchedulerJobContext
import net.hedtech.banner.general.scheduler.SchedulerJobReceipt
import net.hedtech.banner.general.scheduler.SchedulerJobService
import org.apache.commons.lang.NotImplementedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional

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
        if (log.isDebugEnabled()) log.debug( "Method sendAsynchronousGroupCommunication reached." );
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
        SchedulerJobContext jobContext = new SchedulerJobContext( recurrentMessage.jobId )
                .setBannerUser( bannerUser )
                .setMepCode( recurrentMessage.mepCode )
                .setCronSchedule( recurrentMessage.cronExpression )
                .setScheduledStartDate(recurrentMessage.startDate)
                .setEndDate(recurrentMessage.endDate)
                .setParameter( "recurrentMessageId", recurrentMessage.id )

        jobContext.setJobHandle( "communicationRecurrentMessageCompositeService", "generateGroupSendFired" )
                .setErrorHandle( "communicationRecurrentMessageCompositeService", "generateGroupSendFailed" )

        SchedulerJobReceipt jobReceipt = schedulerJobService.scheduleCronServiceMethod( jobContext )
//        groupSend.markScheduled( jobReceipt.jobId, jobReceipt.groupId )
        recurrentMessage.jobId = jobReceipt.jobId
        recurrentMessage.groupId = jobReceipt.groupId
        recurrentMessage = (CommunicationRecurrentMessage) communicationRecurrentMessageService.update( recurrentMessage )
        return recurrentMessage
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
        request.scheduledStartDate = recurrentMessage.startDate
        request.recalculateOnSend = recurrentMessage.recalculateOnSend
        request.parameterNameValueMap = recurrentMessage.parameterNameValueMap
        request.recurrentMessageId = recurrentMessage.id

        CommunicationGroupSend groupSend = communicationGroupSendCompositeService.sendAsynchronousGroupCommunication(request)

//      Get the recurrent message again as the job delete trigger would update the recurrent message object
        if(!recurrentMessage.currentExecutionState.isTerminal()) {
            recurrentMessage.setCurrentExecutionState(CommunicationGroupSendExecutionState.Scheduled)
        }
        recurrentMessage.successCount = recurrentMessage.successCount + 1;
        recurrentMessage.totalCount = recurrentMessage.totalCount + 1;

        recurrentMessage = (CommunicationRecurrentMessage) communicationRecurrentMessageService.update(recurrentMessage)

        //if no of occurences <= total count, delete the cron_trigger entry
        if(recurrentMessage.noOfOccurrences <= recurrentMessage.totalCount) {
            if( recurrentMessage.jobId != null ) {
                schedulerJobService.deleteScheduledJob( recurrentMessage.jobId, recurrentMessage.groupId )
            }
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

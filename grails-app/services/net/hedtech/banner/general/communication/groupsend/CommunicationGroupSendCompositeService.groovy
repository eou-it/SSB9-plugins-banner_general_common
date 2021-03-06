/*******************************************************************************
 Copyright 2014-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import grails.web.context.ServletContextHolder
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.asynchronous.AsynchronousBannerAuthenticationSpoofer
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.event.CommunicationEventMappingView
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.organization.CommunicationOrganizationService
import net.hedtech.banner.general.communication.parameter.CommunicationParameterType
import net.hedtech.banner.general.communication.population.*
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionListService
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplateParameterView
import net.hedtech.banner.general.communication.template.CommunicationTemplateService
import net.hedtech.banner.general.scheduler.SchedulerErrorContext
import net.hedtech.banner.general.scheduler.SchedulerJobContext
import net.hedtech.banner.general.scheduler.SchedulerJobReceipt
import net.hedtech.banner.general.scheduler.SchedulerJobService
import org.apache.commons.lang.NotImplementedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Propagation
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.springframework.web.context.request.RequestContextHolder

import java.sql.Connection
import java.sql.SQLException

/**
 * Communication Group Send Composite Service is responsible for initiating and processing group send communications.
 * Controllers and other client code should generally work through this service for interacting with group send
 * behavior and objects.
 */
@Slf4j
@Transactional
class CommunicationGroupSendCompositeService {

    //private static final log = Logger.getLogger(CommunicationGroupSendCompositeService.class)
    CommunicationGroupSendService communicationGroupSendService
    CommunicationTemplateService communicationTemplateService
    CommunicationPopulationSelectionListService communicationPopulationSelectionListService
    CommunicationOrganizationService communicationOrganizationService
    CommunicationPopulationCompositeService communicationPopulationCompositeService
    AsynchronousBannerAuthenticationSpoofer asynchronousBannerAuthenticationSpoofer
    SchedulerJobService schedulerJobService
    def sessionFactory
    def dataSource


    /**
     * Initiate the sending of a communication to a set of prospect recipients
     * @param request the communication to initiate
     */
    public CommunicationGroupSend sendAsynchronousGroupCommunication( CommunicationGroupSendRequest request, actualBannerUser=null, actualMepCode=null ) {
        if (log.isDebugEnabled()) log.debug( "Method sendAsynchronousGroupCommunication reached." );
        if (!request) throw new IllegalArgumentException( "request may not be null!" )

        String jobName = request.getName();
        if(!jobName || jobName.isEmpty()) {
            throw CommunicationExceptionFactory.createNotFoundException( CommunicationGroupSendCompositeService, "@@r1:jobNameInvalid@@" )
        }

        CommunicationGroupSend groupSend = new CommunicationGroupSend();
        groupSend.templateId = request.getTemplateId()
        groupSend.populationId = request.getPopulationId()

        // do lookup for population version
        groupSend.organizationId = request.getOrganizationId()
        groupSend.eventId = request.eventId
        groupSend.name = jobName
        groupSend.scheduledStartDate = request.scheduledStartDate
        groupSend.recalculateOnSend = request.getRecalculateOnSend()
        groupSend.jobId = request.referenceId
        groupSend.recurrentMessageId = request.recurrentMessageId
        groupSend.communicationCodeId = request.communicationCodeId
        String bannerUser = (actualBannerUser != null)? actualBannerUser : SecurityContextHolder.context.authentication.principal.getOracleUserName()
        groupSend.createdBy = bannerUser
        if (actualMepCode != null) {
            groupSend.mepCode = actualMepCode
        } else if (RequestContextHolder?.getRequestAttributes()?.request?.session) {
            def session = RequestContextHolder.currentRequestAttributes()?.request?.session
            def mepCode = session?.getAttribute("mep")
            if (mepCode != null) {
                groupSend.setMepCode(mepCode)
            }
        }

        groupSend.setParameterNameValueMap( request.getParameterNameValueMap() )
        validateTemplateAndParameters( groupSend )

        CommunicationPopulation population = communicationPopulationCompositeService.fetchPopulation( groupSend.populationId )
        //shared population should be treated as not having a query, because you don't need to recalculate the same
        boolean hasQuery = CommunicationPopulationQueryAssociation.countByPopulation( population ) > 0
//                                        recalculate false || no scheduled date
        boolean useCurrentReplica

        //if group send came from recurrent message, it becomes a send now with possibility of having a re-calculate
        if(groupSend.recurrentMessageId) {
            useCurrentReplica = !groupSend.recalculateOnSend
        } else {
            //assign population calculation if there is no recalculate for one-time schedule or if it is a send now
            useCurrentReplica  = (!groupSend.recalculateOnSend || !request.scheduledStartDate)
        }

        if (hasQuery && useCurrentReplica) {
            // this will need to be updated once we allow queries to be added to existing manual only populations
            assignPopulationVersion( groupSend )
            if(population.personal) {
                assignPopulationCalculation(groupSend, bannerUser)
            } else {
                //Always use the calculation of the created user if it is a shared population
                assignPopulationCalculation(groupSend, population.createdBy)
            }
        } else if (groupSend.recalculateOnSend) { // scheduled with future replica of population
            groupSend.populationVersionId = null
            groupSend.populationCalculationId = null
        } else { // sending now or scheduled with replica of current population
            assert (useCurrentReplica == true)
            assignPopulationVersion( groupSend )
            if (hasQuery) {
                assignPopulationCalculation( groupSend, bannerUser )
            }
        }

        groupSend = (CommunicationGroupSend) communicationGroupSendService.create( groupSend )

        //For a recurrent scheduled message
        if(request.recurrentMessageId) {
            //recalculate on send
            if( request.recalculateOnSend) {
                groupSend = scheduleGroupSendImmediatelyForRecalculate(groupSend, bannerUser)
            } else {
                groupSend = scheduleGroupSendImmediately( groupSend, bannerUser )
            }
        } else if (request.scheduledStartDate) {
            groupSend = scheduleGroupSend( groupSend, bannerUser )
        } else {
            if (hasQuery) {
                assert( groupSend.populationCalculationId != null )
            }
            groupSend = scheduleGroupSendImmediately( groupSend, bannerUser )
        }

        return groupSend
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor = Throwable.class )
    public CommunicationGroupSend createMessageAndPopulationForGroupSend(String eventCode, List<String> bannerIDs, Map parameterNameValuesMap) {

        if(!eventCode || eventCode.isEmpty()) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationGroupSendCompositeService, "eventCodeNotSpecified" )
        }

        CommunicationEventMappingView eventMappingView = CommunicationEventMappingView.fetchByName(eventCode)
        if(!eventMappingView) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationGroupSendCompositeService, "eventMappingDoesNotExist:${eventCode}" )
        }

        //Make a unique name for group send and population by adding timeinmillis to event code
        String uniqueName = eventCode + "_" +System.currentTimeMillis()

        if(eventMappingView.templateId == null) {
            throw CommunicationExceptionFactory.createApplicationException(CommunicationGroupSendCompositeService, "templateIsRequired")
        } else if(!eventMappingView.publishInd) {
            throw CommunicationExceptionFactory.createApplicationException(CommunicationGroupSendCompositeService, "templateNotPublished")
        } else if(!eventMappingView.templateActiveInd) {
            throw CommunicationExceptionFactory.createApplicationException(CommunicationGroupSendCompositeService, "templateNotActive")
        }

        if (!eventMappingView.organizationId) {
            throw CommunicationExceptionFactory.createApplicationException(CommunicationGroupSendCompositeService, "organizationIsRequired")
        } else if(!eventMappingView.organizationAvailableIndicator) {
            throw CommunicationExceptionFactory.createApplicationException(CommunicationGroupSendCompositeService, "organizationNotAvailable")
        }

        CommunicationOrganization organization = CommunicationOrganization.get(eventMappingView.organizationId)
        CommunicationOrganization rootOrganization = CommunicationOrganization.fetchRoot()
        if ((eventMappingView.communicationChannel == CommunicationChannel.EMAIL) &&
                !((organization?.senderMailboxAccount && organization?.replyToMailboxAccount) &&
                        (organization?.sendEmailServerProperties || rootOrganization?.sendEmailServerProperties))) {
            throw CommunicationExceptionFactory.createApplicationException(CommunicationGroupSendCompositeService, "organizationEmailServerSettingsNotAvailable")
        }

        if(bannerIDs == null || bannerIDs.isEmpty()) {
            throw CommunicationExceptionFactory.createApplicationException(CommunicationGroupSendCompositeService, "PIDM(s)IsRequired")
        }

        CommunicationFolder templateFolder = CommunicationFolder.get(eventMappingView.templateFolderId)
        CommunicationPopulation population = communicationPopulationCompositeService.createPopulation(templateFolder, uniqueName, "", true)
        CommunicationPopulationSelectionListBulkResults results = communicationPopulationCompositeService.addPersonsToIncludeList(population, bannerIDs, false)

        CommunicationGroupSendRequest groupSendRequest = new CommunicationGroupSendRequest()
        groupSendRequest.name = uniqueName
        groupSendRequest.populationId = population.id
        groupSendRequest.templateId = eventMappingView.templateId
        groupSendRequest.organizationId = organization.id
        groupSendRequest.eventId = eventMappingView.surrogateId
        groupSendRequest.referenceId = UUID.randomUUID().toString()
        groupSendRequest.recalculateOnSend = false
        groupSendRequest.parameterNameValueMap = parameterNameValuesMap

        CommunicationGroupSend groupSend = sendAsynchronousGroupCommunication(groupSendRequest)
        return groupSend
    }

    private static void assignPopulationCalculation(CommunicationGroupSend groupSend, String bannerUser) {
        CommunicationPopulationCalculation calculation = CommunicationPopulationCalculation.findLatestByPopulationIdAndCalculatedBy(groupSend.getPopulationId(), bannerUser)
        if (!calculation || !calculation.status.equals(CommunicationPopulationCalculationStatus.AVAILABLE)) {
            throw CommunicationExceptionFactory.createApplicationException(CommunicationGroupSendCompositeService.class, "populationNotCalculatedForUser")
        }
        groupSend.populationCalculationId = calculation.id
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

        if (!groupSend.currentExecutionState.pending && !groupSend.currentExecutionState.terminal) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationGroupSendCompositeService.class, "cannotDeleteRunningGroupSend" )
        }

        // Grab population calculation if only used for this group send
        CommunicationPopulationCalculation calculation = null
        boolean recalculateOnSend = groupSend.recalculateOnSend
        if (groupSend.populationCalculationId != null) {
            calculation = CommunicationPopulationCalculation.get( groupSend.populationCalculationId )
        }

        //if group send is scheduled
        if(groupSend.jobId != null) {
            schedulerJobService.deleteScheduledJob( groupSend.jobId, groupSend.groupId )
        } else {
            //if Group send is not scheduled then remove job and recipient data
            deleteCommunicationJobsByGroupSendId(groupSendId)
            deleteRecipientDataByGroupSendId(groupSendId)
        }
        communicationGroupSendService.delete( groupSendId )

        // Garbage collect the population calculation
        if (calculation != null) {
            if (recalculateOnSend) {
                communicationPopulationCompositeService.deletePopulationCalculation( groupSend.populationCalculationId )
            } else {
                CommunicationPopulationCalculation latestCalculation =
                        CommunicationPopulationCalculation.findLatestByPopulationVersionIdAndCalculatedBy( calculation.populationVersion.id, calculation.createdBy )
                if (calculation.id != latestCalculation.id) {
                    communicationPopulationCompositeService.deletePopulationCalculation( latestCalculation )
                }
            }
        }
    }

    /**
     * Stops a group send. The group send must be running otherwise an application exception will be thrown.
     * @param groupSendId the long id of the group send
     * @return the updated (stopped) group send
     */
    public CommunicationGroupSend stopGroupSend( Long groupSendId ) {
        if (log.isDebugEnabled()) log.debug( "Stopping group send with id = ${groupSendId}." )

        CommunicationGroupSend groupSend = (CommunicationGroupSend) communicationGroupSendService.get( groupSendId )

        if (groupSend.currentExecutionState.isTerminal()) {
            log.warn( "Group send with id = ${groupSend.id} has already concluded with execution state ${groupSend.currentExecutionState.toString()}." )
            throw CommunicationExceptionFactory.createApplicationException( CommunicationGroupSendService.class, "cannotStopConcludedGroupSend" )
        }

        groupSend.markStopped()
        groupSend = saveGroupSend( groupSend )

        if (groupSend.jobId != null) {
            this.schedulerJobService.deleteScheduledJob( groupSend.jobId, groupSend.groupId )
        }

        // fetch any communication jobs for this group send and marked as stopped
        stopPendingCommunicationJobs( groupSend.id )
        stopPendingGroupSendItems( groupSend.id )

        return groupSend
    }

    /**
     * Marks a group send as complete.
     * @param groupSendId the id of the group send.
     * @return the updated group send
     */
    public CommunicationGroupSend completeGroupSend( Long groupSendId ) {
        if (log.isDebugEnabled()) log.debug( "Completing group send with id = " + groupSendId + "." )

        CommunicationGroupSend aGroupSend = (CommunicationGroupSend) communicationGroupSendService.get( groupSendId )
        aGroupSend.updateCumulativeStatus(CommunicationGroupSendExecutionState.Processing)
        aGroupSend.markComplete()
        return saveGroupSend( aGroupSend )
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // Scheduling service callback job methods
    //////////////////////////////////////////////////////////////////////////////////////

    public CommunicationGroupSend calculatePopulationVersionForGroupSendFired( SchedulerJobContext jobContext ) {
        calculatePopulationVersionForGroupSend( jobContext.parameters )
    }

    public CommunicationGroupSend calculatePopulationVersionForGroupSendFailed( SchedulerErrorContext errorContext ) {
        return scheduledGroupSendCallbackFailed( errorContext )
    }

    public CommunicationGroupSend generateGroupSendItemsFired( SchedulerJobContext jobContext ) {
        return generateGroupSendItems( jobContext.parameters )
    }

    public CommunicationGroupSend generateGroupSendItemsFailed( SchedulerErrorContext errorContext ) {
        return scheduledGroupSendCallbackFailed( errorContext )
    }

    private CommunicationPopulationVersion assignPopulationVersion( CommunicationGroupSend groupSend ) {
        CommunicationPopulation population = communicationPopulationCompositeService.fetchPopulation( groupSend.populationId )
        CommunicationPopulationVersion populationVersion
        if (population.changesPending) {
            populationVersion = communicationPopulationCompositeService.createPopulationVersion( population )
            population.changesPending = false
            communicationPopulationCompositeService.updatePopulation(population)
            // Todo: Should we delete population versions no longer in use by any group sends aside from the one we just created
            // We would need to remove all the associated objects.
        } else {
            populationVersion = CommunicationPopulationVersion.findLatestByPopulationId( groupSend.populationId )
        }
        assert populationVersion.id
        groupSend.populationVersionId = populationVersion.id
        return populationVersion
    }

    private CommunicationGroupSend scheduledGroupSendCallbackFailed( SchedulerErrorContext errorContext ) {
        Long groupSendId = errorContext.jobContext.getParameter("groupSendId") as Long
        if (log.isDebugEnabled()) {
            log.debug("${errorContext.jobContext.errorHandle} called for groupSendId = ${groupSendId} with message = ${errorContext?.cause?.message}")
        }

        asynchronousBannerAuthenticationSpoofer.setMepContext(sessionFactory.getCurrentSession().connection(),errorContext.jobContext?.parameters?.get("mepCode"))
        CommunicationGroupSend groupSend = CommunicationGroupSend.get( groupSendId )
        if (!groupSend) {
            throw new ApplicationException("groupSend", new NotFoundException())
        }

        groupSend.setCurrentExecutionState(CommunicationGroupSendExecutionState.Error)
        groupSend.setCumulativeExecutionState(CommunicationGroupSendExecutionState.Error)

        if (errorContext.cause) {
            groupSend.errorCode = CommunicationErrorCode.UNKNOWN_ERROR
            groupSend.errorText = errorContext.cause.message
        } else {
            groupSend.errorCode = CommunicationErrorCode.UNKNOWN_ERROR
        }
        groupSend = (CommunicationGroupSend) communicationGroupSendService.update(groupSend)
        return groupSend
    }

    /**
     * This method is called by the scheduler to regenerate a population list specifically for the group send
     * and change the state of the group send to next state.
     */
    private CommunicationGroupSend calculatePopulationVersionForGroupSend( Map parameters ) {
        Long groupSendId = parameters.get( "groupSendId" ) as Long
        assert( groupSendId )
        if (log.isDebugEnabled()) {
            log.debug( "Calling calculatePopulationVersionForGroupSend for groupSendId = ${groupSendId}.")
        }

        asynchronousBannerAuthenticationSpoofer.setMepContext(sessionFactory.getCurrentSession().connection(),parameters.get("mepCode"))
        CommunicationGroupSend groupSend = CommunicationGroupSend.get( groupSendId )
        if (!groupSend) {
            throw new ApplicationException("groupSend", new NotFoundException())
        }

        if(!groupSend.currentExecutionState.isTerminal()) {
            try {
                //Check if organization is active
                def orgTemplateValid = checkOrgTemplateValid(groupSend.organizationId, groupSend.templateId)
                if(orgTemplateValid?.orgValid != 'Y') {
                    String message = "Inactive organization selected for job"
                    log.error(message)
                    groupSend.markError( CommunicationErrorCode.INACTIVE_ORGANIZATION, message )
                    groupSend = (CommunicationGroupSend) communicationGroupSendService.update(groupSend)
                } else if(orgTemplateValid?.templateValid != 'Y') {
                    String message = "Inactive template selected for job"
                    log.error(message)
                    groupSend.markError( CommunicationErrorCode.INACTIVE_TEMPLATE, message )
                    groupSend = (CommunicationGroupSend) communicationGroupSendService.update(groupSend)
                } else {
                    boolean shouldUpdateGroupSend = false
                    CommunicationPopulationVersion populationVersion
                    if (!groupSend.populationVersionId) {
                        populationVersion = assignPopulationVersion(groupSend)
                        shouldUpdateGroupSend = true
                    } else {
                        populationVersion = CommunicationPopulationVersion.get(groupSend.populationVersionId)
                    }

                    if (!populationVersion) {
                        throw new ApplicationException("populationVersion", new NotFoundException())
                    }

                    boolean hasQuery = (CommunicationPopulationVersionQueryAssociation.countByPopulationVersion(populationVersion) > 0)
                    boolean populationCalculationSuccessful = true
                    CommunicationPopulationCalculation calculation
                    if (!groupSend.populationCalculationId && hasQuery) {
                        groupSend.currentExecutionState = CommunicationGroupSendExecutionState.Calculating
                        groupSend.cumulativeExecutionState = CommunicationGroupSendExecutionState.Calculating

                        //Add spoofer to change the user to created user instead of commmgr
                        asynchronousBannerAuthenticationSpoofer.authenticateAndSetFormContextForExecuteAndSave(groupSend.createdBy, parameters.get("mepCode"))
                        calculation = communicationPopulationCompositeService.calculatePopulationVersionForGroupSend(populationVersion)
                        groupSend.populationCalculationId = calculation.id
                        if (calculation.errorCode || calculation.errorText) {
                            populationCalculationSuccessful = false
                            groupSend.markError(calculation.errorCode, calculation.errorText)
                        } else if(calculation.calculatedCount == 0) {
                            //Mark current execution status as Complete so the group send cumulative status gets marked as completed by the monitor when there are no population records
                            Date now = new Date()
                            groupSend.setCurrentExecutionState(CommunicationGroupSendExecutionState.Complete )
                            groupSend.startedDate = now
                            groupSend.stopDate = now
                        }
                        shouldUpdateGroupSend = true
                    }
                    if (shouldUpdateGroupSend) {
                        groupSend = (CommunicationGroupSend) communicationGroupSendService.update(groupSend)
                    }

                    boolean inculdeListExists = populationVersion.includeList
                    if (populationCalculationSuccessful || inculdeListExists) {
                        boolean calculationHasProfiles = calculation?.calculatedCount > 0;
                        if (calculationHasProfiles || inculdeListExists) {
                            groupSend = generateGroupSendItemsImpl(groupSend)
                        }
                    }
                }
            } catch (Throwable t) {
                log.error( t.getMessage() )
                groupSend.refresh()
                groupSend.markError( CommunicationErrorCode.UNKNOWN_ERROR, t.getMessage() )
                groupSend = (CommunicationGroupSend) communicationGroupSendService.update(groupSend)
            }
        }
        return groupSend
    }

    /**
     * This method is called by the scheduler to create the group send items and move the state of
     * the group send to processing.
     */
    private CommunicationGroupSend generateGroupSendItems( Map parameters ) {
        Long groupSendId = parameters.get( "groupSendId" ) as Long
        assert( groupSendId )

        if (log.isDebugEnabled()) {
            log.debug( "Calling generateGroupSendItems for groupSendId = ${groupSendId}.")
        }
        asynchronousBannerAuthenticationSpoofer.setMepContext(sessionFactory.getCurrentSession().connection(),parameters.get("mepCode"))
        CommunicationGroupSend groupSend = CommunicationGroupSend.get(groupSendId)
        if (!groupSend) {
            throw new ApplicationException("groupSend", new NotFoundException())
        }

        if(!groupSend.currentExecutionState.isTerminal()) {
            try {
                //Check if organization is active
                def orgTemplateValid = checkOrgTemplateValid(groupSend.organizationId, groupSend.templateId)
                if(orgTemplateValid?.orgValid != 'Y') {
                    String message = "Inactive organization selected for job"
                    log.error(message)
                    groupSend.markError( CommunicationErrorCode.INACTIVE_ORGANIZATION, message )
                    groupSend = (CommunicationGroupSend) communicationGroupSendService.update(groupSend)
                } else if(orgTemplateValid?.templateValid != 'Y') {
                    String message = "Inactive template selected for job"
                    log.error(message)
                    groupSend.markError( CommunicationErrorCode.INACTIVE_TEMPLATE, message )
                    groupSend = (CommunicationGroupSend) communicationGroupSendService.update(groupSend)
                } else {
                    groupSend = generateGroupSendItemsImpl(groupSend)
                }
            } catch (Throwable t) {
                log.error(t.getMessage())
                groupSend.markError( CommunicationErrorCode.UNKNOWN_ERROR, t.getMessage() )
                groupSend = (CommunicationGroupSend) communicationGroupSendService.update(groupSend)
            }
        }
        return groupSend
    }

    private CommunicationGroupSend scheduleGroupSendImmediately( CommunicationGroupSend groupSend, String bannerUser ) {
        SchedulerJobContext jobContext = new SchedulerJobContext( groupSend.jobId != null ? groupSend.jobId : UUID.randomUUID().toString() )
                .setBannerUser( bannerUser )
                .setMepCode( groupSend.mepCode )
                .setJobHandle( "communicationGroupSendCompositeService", "generateGroupSendItemsFired" )
                .setErrorHandle( "communicationGroupSendCompositeService", "generateGroupSendItemsFailed" )
                .setParameter( "groupSendId", groupSend.id )

        SchedulerJobReceipt jobReceipt = schedulerJobService.scheduleNowServiceMethod( jobContext )
        groupSend.markQueued( jobReceipt.jobId, jobReceipt.groupId )
        groupSend = (CommunicationGroupSend) communicationGroupSendService.update(groupSend)
        return groupSend
    }


    private CommunicationGroupSend scheduleGroupSend( CommunicationGroupSend groupSend, String bannerUser ) {
        Date now = new Date(System.currentTimeMillis())
        if (now.after(groupSend.scheduledStartDate)) {
            throw CommunicationExceptionFactory.createApplicationException(CommunicationGroupSendService.class, "invalidScheduledDate")
        }

        SchedulerJobContext jobContext = new SchedulerJobContext( groupSend.jobId )
                .setBannerUser( bannerUser )
                .setMepCode( groupSend.mepCode )
                .setScheduledStartDate( groupSend.scheduledStartDate )
                .setParameter( "groupSendId", groupSend.id )

        if(groupSend.recalculateOnSend) {
            jobContext.setJobHandle( "communicationGroupSendCompositeService", "calculatePopulationVersionForGroupSendFired" )
                    .setErrorHandle( "communicationGroupSendCompositeService", "calculatePopulationVersionForGroupSendFailed" )
        } else {
            jobContext.setJobHandle( "communicationGroupSendCompositeService", "generateGroupSendItemsFired" )
                    .setErrorHandle( "communicationGroupSendCompositeService", "generateGroupSendItemsFailed" )
        }

        SchedulerJobReceipt jobReceipt = schedulerJobService.scheduleServiceMethod( jobContext )
        groupSend.markScheduled( jobReceipt.jobId, jobReceipt.groupId )
        groupSend = (CommunicationGroupSend) communicationGroupSendService.update( groupSend )
        return groupSend
    }

    //To be used when performing a recalculate on send which is part of a recurrent message
    private CommunicationGroupSend scheduleGroupSendImmediatelyForRecalculate( CommunicationGroupSend groupSend, String bannerUser ) {
        SchedulerJobContext jobContext = new SchedulerJobContext( groupSend.jobId != null ? groupSend.jobId : UUID.randomUUID().toString() )
                .setBannerUser( bannerUser )
                .setMepCode( groupSend.mepCode )
                .setJobHandle( "communicationGroupSendCompositeService", "calculatePopulationVersionForGroupSendFired" )
                .setErrorHandle( "communicationGroupSendCompositeService", "calculatePopulationVersionForGroupSendFailed" )
                .setParameter( "groupSendId", groupSend.id )

        SchedulerJobReceipt jobReceipt = schedulerJobService.scheduleNowServiceMethod( jobContext )
        groupSend.markQueued( jobReceipt.jobId, jobReceipt.groupId )
        groupSend = (CommunicationGroupSend) communicationGroupSendService.update(groupSend)
        return groupSend
    }


    private CommunicationGroupSend generateGroupSendItemsImpl( CommunicationGroupSend groupSend ) {
        // We'll created the group send items synchronously for now until we have support for scheduling.
        // The individual group send items will still be processed asynchronously via the framework.
        createGroupSendItems(groupSend)
        //Refresh the group send if other threads have already updated the same
        groupSend.refresh()
        groupSend.markProcessing()

        groupSend = (CommunicationGroupSend) communicationGroupSendService.update(groupSend)
        return groupSend
    }

    private CommunicationGroupSend saveGroupSend( CommunicationGroupSend groupSend ) {
        //TODO: Figure out why ServiceBase.update is not working with this domain.
        try {
            return groupSend.save(flush: true) //update( groupSend )
        } catch(Throwable t) {
            log.error( t.message )
        }
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
            log.error( e.message )
            throw e
        } finally {
//            sql?.close()
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
            int rows = sql.executeUpdate( "DELETE FROM gcbrdat a WHERE EXISTS (SELECT b.gcrgsim_surrogate_id FROM gcrgsim b, gcbgsnd c WHERE a.gcbrdat_reference_id = b.gcrgsim_reference_id AND b.gcrgsim_group_send_id = c.gcbgsnd_surrogate_id AND c.gcbgsnd_surrogate_id = ?)",
                    [ groupSendId ] )
            if (log.isDebugEnabled()) {
                log.debug( "Deleting ${rows} recipient data referenced by group send id = ${groupSendId}.")
            }
        } catch (Exception e) {
            log.error( e.message )
            throw e
        } finally {
//            sql?.close()
        }
    }


    private void stopPendingCommunicationJobs( Long groupSendId ) {
        def Sql sql
        try {
            Connection connection = (Connection) sessionFactory.getCurrentSession().connection()
            sql = new Sql( (Connection) sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update GCBCJOB set GCBCJOB_STATUS='STOPPED', GCBCJOB_ACTIVITY_DATE = SYSDATE where " +
                    "GCBCJOB_STATUS in ('PENDING', 'DISPATCHED') and GCBCJOB_REFERENCE_ID in " +
                    "(select GCRGSIM_REFERENCE_ID from GCRGSIM where GCRGSIM_GROUP_SEND_ID = ${groupSendId} and GCRGSIM_CURRENT_STATE = 'Complete')" )
        } catch (SQLException e) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationGroupSendService, e )
        } catch (Exception e) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationGroupSendService, e )
        } finally {
//            sql?.close()
        }
    }

    private void stopPendingGroupSendItems( Long groupSendId ) {
        def Sql sql
        try {
            Connection connection = (Connection) sessionFactory.getCurrentSession().connection()
            sql = new Sql( (Connection) sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update GCRGSIM set GCRGSIM_CURRENT_STATE='Stopped', GCRGSIM_ACTIVITY_DATE = SYSDATE, GCRGSIM_STOP_DATE = SYSDATE where " +
                    "GCRGSIM_CURRENT_STATE in ('Ready') and GCRGSIM_GROUP_SEND_ID = ${groupSendId}" )
        } catch (SQLException e) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationGroupSendService, e )
        } catch (Exception e) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationGroupSendService, e )
        } finally {
//            sql?.close()
        }
    }


    private void createGroupSendItems( CommunicationGroupSend groupSend ) {
        if (log.isDebugEnabled()) log.debug( "Generating group send item records for group send with id = " + groupSend?.id );
        def sql
        try {
            def ctx = Holders.grailsApplication.getMainContext()
            def sessionFactory = ctx.sessionFactory
            def session = sessionFactory.currentSession
            sql = new Sql(session.connection())

            sql.execute(
                    [
                            state:CommunicationGroupSendItemExecutionState.Ready.toString(),
                            group_send_key:groupSend.id,
                            current_time:new Date().toTimestamp()
                    ],
                    """
            INSERT INTO gcrgsim (gcrgsim_group_send_id, gcrgsim_pidm, gcrgsim_creationdatetime
                                            ,gcrgsim_current_state, gcrgsim_reference_id, gcrgsim_user_id, gcrgsim_activity_date, gcrgsim_started_date)
                    select
                        gcbgsnd_surrogate_id,
                        gcrlent_pidm,
                        :current_time,
                        :state,
                        sys_guid(),
                        gcbgsnd_user_id,
                        :current_time,
                        :current_time
                    from (
                        select gcrlent_pidm, gcbgsnd_surrogate_id, gcbgsnd_user_id
                            from gcrslis, gcrlent, gcbgsnd, gcrpopc
                            where
                            gcbgsnd_surrogate_id = :group_send_key
                            and gcrpopc_surrogate_id = gcbgsnd_popcalc_id
                            and gcrslis_surrogate_id = gcrpopc_slis_id
                            and gcrlent_slis_id = gcrslis_surrogate_id
                        union
                        select gcrlent_pidm, gcbgsnd_surrogate_id, gcbgsnd_user_id
                            from gcrslis, gcrlent, gcbgsnd, gcrpopv
                            where
                            gcbgsnd_surrogate_id = :group_send_key
                            and gcrpopv_surrogate_id = gcbgsnd_popversion_id
                            and gcrslis_surrogate_id = gcrpopv_include_list_id
                            and gcrlent_slis_id = gcrslis_surrogate_id
                    )
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
//            sql?.close()
        }

        if(groupSend.communicationCodeId) {
            try {
                def ctx = Holders.grailsApplication.getMainContext()
                def sessionFactory = ctx.sessionFactory
                def session = sessionFactory.currentSession
                sql = new Sql(session.connection())

                sql.execute(
                        [
                                group_send_key: groupSend.id,
                                current_time:new Date().toTimestamp()
                        ],
                        """
            INSERT INTO gcrlmap (gcrlmap_reference_id, gcrlmap_letr_id, gcrlmap_user_id, gcrlmap_activity_date)
                    select
                        gcrgsim_reference_id,
                        gcbgsnd_letr_id,
                        gcbgsnd_user_id,
                        :current_time
                    from gcbgsnd, gcrgsim where gcbgsnd_surrogate_id = :group_send_key and gcbgsnd_surrogate_id = gcrgsim_group_send_id
            """)

                if (log.isDebugEnabled()) log.debug("Created " + sql.updateCount + " gcrlmap records for group send with id = " + groupSend.id)
            } catch (SQLException ae) {
                log.debug "SqlException in INSERT INTO gcrlmap ${ae}"
                log.debug ae.stackTrace
                throw ae
            } catch (Exception ae) {
                log.debug "Exception in INSERT INTO gcrlmap ${ae}"
                log.debug ae.stackTrace
                throw ae
            } finally {
//            sql?.close()
            }
        }

    }

    private void validateTemplateAndParameters(CommunicationGroupSend groupSend) {
        if (groupSend.templateId == null) {
            throw CommunicationExceptionFactory.createApplicationException(CommunicationGroupSendCompositeService, "templateIsRequired")
        }
        List templateParameterList = CommunicationTemplateParameterView.findAllByTemplateId(groupSend.templateId)
        if (templateParameterList != null) {
            templateParameterList.each { CommunicationTemplateParameterView templateParameter ->
                Object value = groupSend.getParameterNameValueMap().get(templateParameter.parameterName)?.value
                if (value == null) {
                    throw CommunicationExceptionFactory.createApplicationException(CommunicationGroupSendCompositeService, "missingParameterValue", templateParameter.parameterName)
                }
                if (templateParameter.parameterType == CommunicationParameterType.TEXT) {
                    if (!(value instanceof String)) {
                        throw CommunicationExceptionFactory.createApplicationException(CommunicationGroupSendCompositeService, "invalidParameterValue", templateParameter.parameterName)
                    }
                    String stringValue = (String) value
                    if (stringValue.length() == 0) {
                        throw CommunicationExceptionFactory.createApplicationException(CommunicationGroupSendCompositeService, "missingParameterValue", templateParameter.parameterName)
                    }
                } else if (templateParameter.parameterType == CommunicationParameterType.NUMBER) {
                    if (!(value instanceof Number)) {
                        throw CommunicationExceptionFactory.createApplicationException(CommunicationGroupSendCompositeService, "invalidParameterValue", templateParameter.parameterName)
                    }
                } else if (templateParameter.parameterType == CommunicationParameterType.DATE) {
                    if (!(value instanceof Date)) {
                        throw CommunicationExceptionFactory.createApplicationException(CommunicationGroupSendCompositeService, "invalidParameterValue", templateParameter.parameterName)
                    }
                } else {
                    throw new NotImplementedException("Unhandled template parameter type when validating send parameter values.")
                }
            }
        }
    }


    private  checkOrgTemplateValid(orgId, templateId) {

        def orgSqlString =
                """
                     SELECT gcroran_available_ind FROM gcroran
                     WHERE gcroran_surrogate_id = ?
                """
        def templateSqlString =
                """
                    SELECT
                      CASE
                          WHEN gcbtmpl_publish = 'Y' AND SYSDATE between NVL(trunc(GCBTMPL_VALID_FROM),SYSDATE) and NVL(trunc(GCBTMPL_VALID_TO), SYSDATE)
                          THEN 'Y'
                          ELSE 'N'
                          END AS active
                    FROM gcbtmpl
                    WHERE gcbtmpl_surrogate_id = ?
                """
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def orgValid = 'N'
        def templateValid = 'N'
        try {
            sql.eachRow(orgSqlString, [orgId]) { row ->
                orgValid = row.gcroran_available_ind;
            }
            sql.eachRow(templateSqlString, [templateId]) { row ->
                templateValid = row.active
            }
            return ['orgValid':orgValid, 'templateValid':templateValid]
        }
        catch (Exception ae) {
            log.error("Exception when trying to check for org and template validity${ae} ")
            println "CAUGHT EXCEPTION AT ORG CHECK: "+ae.getMessage()
            throw ae
        }
        finally {

        }
    }

}

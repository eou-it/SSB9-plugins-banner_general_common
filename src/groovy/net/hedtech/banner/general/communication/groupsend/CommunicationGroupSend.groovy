/*********************************************************************************
 Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.groupsend

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.DateUtility
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.parameter.CommunicationParameterType
import net.hedtech.banner.service.DatabaseModifiesState
import org.apache.commons.lang.NotImplementedException
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

import javax.persistence.*

/**
 * CommunicationJob
 *
 */
@Entity
@Table(name = "GCBGSND")
@EqualsAndHashCode
@ToString
@DatabaseModifiesState
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationGroupSend.findRunning",
                query = """ FROM CommunicationGroupSend gs
                    WHERE gs.currentExecutionState = :new_ or
                          gs.currentExecutionState = :processing_ or
                          gs.currentExecutionState = :scheduled_ or
                          gs.currentExecutionState = :queued_ or
                          gs.currentExecutionState = :calculating_"""
        ),
        @NamedQuery(name = "CommunicationGroupSend.findWithRunningCumulativeStatus",
                query = """ FROM CommunicationGroupSend gs
                    WHERE gs.cumulativeExecutionState = :new_ or
                          gs.cumulativeExecutionState = :processing_ or
                          gs.cumulativeExecutionState = :scheduled_ or
                          gs.cumulativeExecutionState = :queued_ or
                          gs.cumulativeExecutionState = :calculating_
                    AND   gs.currentExecutionState = :complete_"""
        ),
        @NamedQuery(name = "CommunicationGroupSend.findByRecurrentMessageId",
                query = """ FROM CommunicationGroupSend gs
                    WHERE gs.recurrentMessageId =   :recurrentMessageId """
        ),
        @NamedQuery(name = "CommunicationGroupSend.fetchCompleted",
                query = """ FROM CommunicationGroupSend gs
                WHERE gs.currentExecutionState = :complete_ """
        ),
        @NamedQuery(name = "CommunicationGroupSend.fetchCompletedByRecurrentMessageId",
                query = """ FROM CommunicationGroupSend gs
                WHERE gs.currentExecutionState = :complete_ 
                      and gs.recurrentMessageId =   :recurrentMessageId 
                ORDER BY gs.id DESC"""
        )
])
class CommunicationGroupSend implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCBGSND_SURROGATE_ID")
    @SequenceGenerator(name = "gcbgsnd_SEQ_GEN", allocationSize = 1, sequenceName = "gcbgsnd_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gcbgsnd_SEQ_GEN")
    Long id

    @Column(name = "GCBGSND_NAME")
    String name

    /**
     *  Optimistic lock token.
     */
    @Version
    @Column(name = "GCBGSND_VERSION")
    Long version

    /** The oracle user name of the person that submitted the group send. **/
    @Column(name = "GCBGSND_CREATOR_ID")
    String createdBy

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCBGSND_USER_ID")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "GCBGSND_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *  Source system that created or updated the data.
     */
    @Column(name = "GCBGSND_DATA_ORIGIN")
    String dataOrigin

    @Column(name = "GCBGSND_VPDI_CODE")
    String mepCode

    @Column(name = "GCBGSND_ORGANIZATION_ID")
    Long organizationId

    @Column(name = "GCBGSND_POPLIST_ID")
    Long populationId;

    @Column(name = "GCBGSND_POPVERSION_ID")
    Long populationVersionId;

    @Column(name = "GCBGSND_POPCALC_ID")
    Long populationCalculationId;

    @Column(name = "GCBGSND_TEMPLATE_ID")
    Long templateId;

    @Column(name = "GCBGSND_EVENT_ID")
    Long eventId;

    @Column(name = "GCBGSND_STARTED_DATE", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    Date startedDate;

    @Column(name = "GCBGSND_CREATIONDATETIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date creationDateTime;

    @Column(name = "gcbgsnd_CURRENT_STATE", nullable = false)
    @Enumerated(EnumType.STRING)
    CommunicationGroupSendExecutionState currentExecutionState = CommunicationGroupSendExecutionState.New;

    @Column(name = "GCBGSND_CUMULATIVE_STATE", nullable = true)
    @Enumerated(EnumType.STRING)
    CommunicationGroupSendExecutionState cumulativeExecutionState = CommunicationGroupSendExecutionState.New;

    @Column(name = "gcbgsnd_STOP_DATE", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    Date stopDate;

    @Column(name = "gcbgsnd_DELETED", nullable = false)
    @Type(type = "yes_no")
    boolean deleted = false;

    @Column(name = "GCBGSND_SCHEDULEDDATETIME", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    Date scheduledStartDate

    @Type(type = "yes_no")
    @Column(name = "GCBGSND_RECALC_ON_SEND")
    Boolean recalculateOnSend

    /**
     * Error Code: The error code for the error scenario that failed the Communication Job
     */
    @Column(name = "GCBGSND_ERROR_CODE")
    @Enumerated(EnumType.STRING)
    CommunicationErrorCode errorCode

    @Lob
    @Column(name = "GCBGSND_ERROR_TEXT")
    String errorText

    /**
     * Job ID : job id of a quartz scheduled task for this group send
     */
    @Column(name = "GCBGSND_JOB_ID")
    String jobId

    /**
     * Group ID : group id of the quartz job and/or trigger for a group send scheduled task
     */
    @Column(name = "GCBGSND_GROUP_ID")
    String groupId

    @Column(name = "GCBGSND_CREC_ID")
    Long recurrentMessageId

    /**
     * Parameter Values : the values entered by the user for the parameters in a chosen template for the given group send
     */
    @Lob
    @Column(name = "GCBGSND_PARAMETER_VALUES")
    String parameterValues

    @Transient
    private Map parameterNameValueMap


    static constraints = {
        mepCode(nullable: true)
        name(nullable: false)
        populationId(nullable: false)
        populationVersionId(nullable: true)
        populationCalculationId(nullable: true)
        organizationId(nullable: false)
        templateId(nullable: false)
        eventId(nullable:true)
        createdBy(nullable: false, maxSize: 30)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        startedDate(nullable: true)
        stopDate(nullable: true)
        creationDateTime(nullable: false)
        currentExecutionState(nullable: false)
        scheduledStartDate(nullable:true)
        recalculateOnSend(nullable:false)
        errorText(nullable:true)
        errorCode(nullable:true)
        jobId(nullable:true)
        groupId(nullable:true)
        recurrentMessageId(nullable:true)
        parameterValues(nullable:true)
    }

    public Map getParameterNameValueMap() {
        if (parameterNameValueMap == null) {
            if (parameterValues == null || parameterValues.trim().length() == 0 ) {
                parameterNameValueMap = [:]
            } else {
                JsonSlurper jsonSlurper = new JsonSlurper()
                try {
                    Map rawNameValueMap = jsonSlurper.parseText( parameterValues )
                    if (rawNameValueMap != null) {
                        rawNameValueMap.keySet().each { String name ->
                            Map valueType = rawNameValueMap.get( name )
                            CommunicationParameterType type = CommunicationParameterType.valueOf( (String) valueType.type )
                            if (type == CommunicationParameterType.DATE) {
                                Date d = DateUtility.parseDateString( valueType.value, 'yyyy-MM-dd' )
                                rawNameValueMap.put( name, new CommunicationParameterValue( [ value: d, type: type ] ) )
                            } else if (type == CommunicationParameterType.NUMBER) {
                                Number number = null
                                try {
                                    number = Long.parseLong( valueType.value )
                                } catch (NumberFormatException e) {
                                    number = Double.parseDouble( valueType.value )
                                }
                                if (number != null) {
                                    rawNameValueMap.put( name, new CommunicationParameterValue( [ value: number, type: type ] ) )
                                }
                            } else if (type == CommunicationParameterType.TEXT) {
                                rawNameValueMap.put( name, new CommunicationParameterValue( [ value: valueType.value, type: type ] ) )
                            } else {
                                throw new NotImplementedException( "Unhandled parameter type" )
                            }
                        }
                    }
                    parameterNameValueMap = rawNameValueMap
                } catch (groovy.json.JsonException e) {
                    throw CommunicationExceptionFactory.createApplicationException( CommunicationGroupSend.class, "badSyntax" )
                }
            }
        }

        return parameterNameValueMap
    }

    public void setParameterNameValueMap( Map nameParameterValueMap ) {
        assert nameParameterValueMap != null
        Map rawNameValueMap = [:]
        if (nameParameterValueMap != null) {
            nameParameterValueMap.keySet().each { String name ->
                CommunicationParameterValue parameterValue = nameParameterValueMap.get( name )
                String valueAsString
                if (parameterValue.type == CommunicationParameterType.DATE) {
                    valueAsString = DateUtility.getDateString( parameterValue.value, 'yyyy-MM-dd' )
                } else if (parameterValue.type == CommunicationParameterType.NUMBER) {
                    valueAsString = parameterValue.value.toString()
                } else if (parameterValue.type == CommunicationParameterType.TEXT) {
                    valueAsString = parameterValue.value
                } else {
                    throw new NotImplementedException( "Not handled parameter type" )
                }
                rawNameValueMap.put( name, [ value: valueAsString, type: parameterValue.type.toString() ] )
            }
        }
        parameterValues = JsonOutput.toJson( rawNameValueMap )
        parameterNameValueMap = null
    }

    public void markScheduled( String jobId, String groupId ) {
        assert jobId != null
        assert groupId != null
        assignGroupSendExecutionState( CommunicationGroupSendExecutionState.Scheduled, jobId, groupId )
        this.cumulativeExecutionState = CommunicationGroupSendExecutionState.Scheduled
    }

    public void markQueued( String jobId, String groupId ) {
        assert jobId != null
        assert groupId != null
        assignGroupSendExecutionState( CommunicationGroupSendExecutionState.Queued, jobId, groupId )
        this.cumulativeExecutionState = CommunicationGroupSendExecutionState.Queued
    }

    public void markStopped( Date stopDate = new Date() ) {
        assignGroupSendExecutionState( CommunicationGroupSendExecutionState.Stopped )
        this.cumulativeExecutionState = CommunicationGroupSendExecutionState.Stopped
        this.stopDate = stopDate
    }

    public void markComplete( Date stopDate = new Date() ) {
        assignGroupSendExecutionState( CommunicationGroupSendExecutionState.Complete )
        //We do not update the cumulative status at this point, as we need monitor thread to make sure all items and jobs are completed as well
        this.stopDate = stopDate
    }

    public void markProcessing() {
        assignGroupSendExecutionState( CommunicationGroupSendExecutionState.Processing )
        this.cumulativeExecutionState = CommunicationGroupSendExecutionState.Processing
        if (this.startedDate == null) {
            this.startedDate = new Date()
        }
    }

    public void markError( CommunicationErrorCode errorCode, String errorText ) {
        assignGroupSendExecutionState( CommunicationGroupSendExecutionState.Error )
        this.cumulativeExecutionState = CommunicationGroupSendExecutionState.Error
        this.errorCode = errorCode
        this.errorText = errorText
        this.stopDate = stopDate
    }

    public void updateCumulativeStatus( CommunicationGroupSendExecutionState executionState ) {
        if(this.cumulativeExecutionState != CommunicationGroupSendExecutionState.Error) {
            this.cumulativeExecutionState = executionState
        }
    }

    private void assignGroupSendExecutionState( CommunicationGroupSendExecutionState executionState, String jobId = null, String groupId = null ) {
        this.currentExecutionState = executionState
        this.jobId = jobId
        this.groupId = groupId
    }

    public static List findRunning( Integer max = Integer.MAX_VALUE ) {
        def query
        CommunicationGroupSend.withSession { session ->
            query = session.getNamedQuery('CommunicationGroupSend.findRunning')
                    .setParameter('new_', CommunicationGroupSendExecutionState.New)
                    .setParameter('processing_', CommunicationGroupSendExecutionState.Processing)
                    .setParameter('scheduled_', CommunicationGroupSendExecutionState.Scheduled)
                    .setParameter('queued_', CommunicationGroupSendExecutionState.Queued)
                    .setParameter('calculating_', CommunicationGroupSendExecutionState.Calculating)
                    .setFirstResult( 0 )
                    .setMaxResults( max )
                    .list()
        }
        return query
    }

    public static List findWithRunningCumulativeStatus( Integer max = Integer.MAX_VALUE ) {
        def query
        CommunicationGroupSend.withSession { session ->
            query = session.getNamedQuery('CommunicationGroupSend.findWithRunningCumulativeStatus')
                    .setParameter('new_', CommunicationGroupSendExecutionState.New)
                    .setParameter('processing_', CommunicationGroupSendExecutionState.Processing)
                    .setParameter('scheduled_', CommunicationGroupSendExecutionState.Scheduled)
                    .setParameter('queued_', CommunicationGroupSendExecutionState.Queued)
                    .setParameter('calculating_', CommunicationGroupSendExecutionState.Calculating)
                    .setParameter('complete_', CommunicationGroupSendExecutionState.Complete)
                    .setFirstResult( 0 )
                    .setMaxResults( max )
                    .list()
        }
        return query
    }

    public static List fetchCompleted() {
        def results
        CommunicationGroupSendItem.withSession { session ->
            results = session.getNamedQuery('CommunicationGroupSend.fetchCompleted')
                    .setParameter('complete_', CommunicationGroupSendExecutionState.Complete)
                    .list()
        }
        return results
    }

    public static CommunicationGroupSend fetchCompletedByRecurrentMessageId( Long recurrentMessageId ) {
        def results
        CommunicationGroupSendItem.withSession { session ->
            results = session.getNamedQuery('CommunicationGroupSend.fetchCompletedByRecurrentMessageId')
                    .setParameter('complete_', CommunicationGroupSendExecutionState.Complete)
                    .setParameter('recurrentMessageId', recurrentMessageId)
                    .list()
        }
        return results[0]
    }

    public static int findCountByPopulationCalculationId( Long populationCalculationId) {
        return CommunicationGroupSend.createCriteria().list {
            projections {
                count()
            }
            eq( 'populationCalculationId', populationCalculationId )
        }[0]
    }

    public static List findByRecurrentMessageId( Long recurrentMessageId ) {
        def query
        CommunicationGroupSend.withSession { session ->
            query = session.getNamedQuery('CommunicationGroupSend.findByRecurrentMessageId')
                    .setParameter('recurrentMessageId', recurrentMessageId)
                    .list()
        }
        return query
    }

    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def descdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'desc'

        def queryCriteria = CommunicationGroupSend.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("name", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.groupSendName))
            ilike("createdBy", filterData?.params?.createdBy)
            order((descdir ? Order.desc(pagingAndSortParams?.sortColumn) : Order.asc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }
}


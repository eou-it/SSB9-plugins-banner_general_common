/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.groupsend

import grails.converters.JSON
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.DateUtility
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.parameter.CommunicationParameterType
import net.hedtech.banner.service.DatabaseModifiesState
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
        @NamedQuery(name = "CommunicationGroupSend.fetchCompleted",
                query = """ FROM CommunicationGroupSend gs
                WHERE gs.currentExecutionState = :complete_ """
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

    @Column(name = "GCBGSND_STARTED_DATE", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    Date startedDate;

    @Column(name = "GCBGSND_CREATIONDATETIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date creationDateTime;

    @Column(name = "gcbgsnd_CURRENT_STATE", nullable = false)
    @Enumerated(EnumType.STRING)
    CommunicationGroupSendExecutionState currentExecutionState = CommunicationGroupSendExecutionState.New;

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

    /**
     * Parameter Values : the values entered by the user for the parameters in a chosen template for the given group send
     */
    @Lob
    @Column(name = "GCBGSND_PARAMETER_VALUES")
    String parameterValues

    @Transient
    Map parameterValueMap

    static constraints = {
        mepCode(nullable: true)
        name(nullable: false)
        populationId(nullable: false)
        populationVersionId(nullable: true)
        populationCalculationId(nullable: true)
        organizationId(nullable: false)
        templateId(nullable: false)
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
        parameterValues(nullable:true)
    }

    public Map getParameterValueMap()
    {
        if(parameterValueMap == null && (parameterValues!=null && !parameterValues.isEmpty()))
        {
            parameterValueMap = new HashMap<String,Object>()
            List parameterValuesList = JSON.parse(parameterValues)
            for(Object parameterValue : parameterValuesList)
            {
                if(parameterValue.type == CommunicationParameterType.DATE.name()) {
                    Date temp = DateUtility.parseDateString(parameterValue.answer);
                    parameterValue.answer = temp;
                }
                parameterValueMap.put(parameterValue.name, parameterValue)
            }
        }
        return parameterValueMap
    }

    public void markScheduled( String jobId, String groupId ) {
        assert jobId != null
        assert groupId != null
        assignGroupSendExecutionState( CommunicationGroupSendExecutionState.Scheduled, jobId, groupId )
    }

    public void markQueued( String jobId, String groupId ) {
        assert jobId != null
        assert groupId != null
        assignGroupSendExecutionState( CommunicationGroupSendExecutionState.Queued, jobId, groupId )
    }

    public void markStopped( Date stopDate = new Date() ) {
        assignGroupSendExecutionState( CommunicationGroupSendExecutionState.Stopped )
        this.stopDate = stopDate
    }

    public void markComplete( Date stopDate = new Date() ) {
        assignGroupSendExecutionState( CommunicationGroupSendExecutionState.Complete )
        this.stopDate = stopDate
    }

    public void markProcessing() {
        assignGroupSendExecutionState( CommunicationGroupSendExecutionState.Processing )
        if (this.startedDate == null) {
            this.startedDate = new Date()
        }
    }

    public void markError( CommunicationErrorCode errorCode, String errorText ) {
        assignGroupSendExecutionState( CommunicationGroupSendExecutionState.Error )
        this.errorCode = errorCode
        this.errorText = errorText
        this.stopDate = stopDate
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

    public static List fetchCompleted() {
        def results
        CommunicationGroupSendItem.withSession { session ->
            results = session.getNamedQuery('CommunicationGroupSend.fetchCompleted')
                    .setParameter('complete_', CommunicationGroupSendExecutionState.Complete)
                    .list()
        }
        return results
    }

    public static int findCountByPopulationCalculationId( Long populationCalculationId) {
        return CommunicationGroupSend.createCriteria().list {
            projections {
                count()
            }
            eq( 'populationCalculationId', populationCalculationId )
        }[0]
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


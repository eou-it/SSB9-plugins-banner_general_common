package net.hedtech.banner.general.communication.recurrence

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendExecutionState
import net.hedtech.banner.general.communication.item.CommunicationChannel
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

import javax.persistence.*

@Entity
@Table(name = "GVQ_GCBCREC")
@EqualsAndHashCode
@ToString
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationRecurrentMessageView.fetchById",
                query = """ FROM CommunicationRecurrentMessageView a
                            WHERE  a.id = :recurrentMessageId
                        """)
])
class CommunicationRecurrentMessageView {

/**
 * KEY: Generated unique key.
 */
    @Id
    @Column(name = "CREC_ID")
    Long id

    @Column(name = "JOB_NAME")
    String name

    @Column(name = "JOB_DESCRIPTION")
    String description
    /**
     *  Optimistic lock token.
     */
    @Version
    @Column(name = "VERSION")
    Long version

    /** The oracle user name of the person that submitted the group send. **/
    @Column(name = "CREATOR_ID")
    String createdBy

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "USER_ID")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *  Source system that created or updated the data.
     */
    @Column(name = "DATA_ORIGIN")
    String dataOrigin

    @Column(name = "ORGANIZATION_ID")
    Long organizationId

    @Column(name = "ORGANIZATION_NAME")
    String organizationName

    @Column(name = "VPDI_CODE")
    String mepCode

    @Column(name = "POPLULATION_ID")
    Long populationId;

    @Column(name = "POPULATION_NAME")
    String populationName

    @Column(name = "TEMPLATE_ID")
    Long templateId;

    @Column(name = "TEMPLATE_NAME")
    String templateName

    @Column(name = "COMM_CHANNEL")
    @Enumerated(value = EnumType.STRING)
    CommunicationChannel communicationChannel

    @Column(name = "EVENT_ID")
    Long eventId;

    @Column(name = "EVENT_NAME")
    String eventName

    @Column(name = "START_DATE", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    Date startDate;

    @Column(name = "CREATIONDATETIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date creationDateTime;

    @Column(name = "CURRENT_STATE", nullable = false)
    @Enumerated(EnumType.STRING)
    CommunicationGroupSendExecutionState currentExecutionState = CommunicationGroupSendExecutionState.New;

    @Column(name = "STOPPED_DATE", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    Date stoppedDate;

    @Type(type = "yes_no")
    @Column(name = "RECALC_ON_SEND")
    Boolean recalculateOnSend

    /**
     * Error Code: The error code for the error scenario that failed
     */
    @Column(name = "ERROR_CODE")
    @Enumerated(EnumType.STRING)
    CommunicationErrorCode errorCode

    @Lob
    @Column(name = "ERROR_TEXT")
    String errorText

    /**
     * Job ID : job id of a quartz scheduled task for this group send
     */
    @Column(name = "JOB_ID")
    String jobId

    /**
     * Group ID : group id of the quartz job and/or trigger for a group send scheduled task
     */
    @Column(name = "GROUP_ID")
    String groupId

    /**
     * Parameter Values : the values entered by the user for the parameters in a chosen template for the given group send
     */
    @Lob
    @Column(name = "PARAMETER_VALUES")
    String parameterValues

    @Column(name = "CRON_STRING")
    String cronExpression

    @Column(name = "END_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date endDate

    @Column(name = "NUM_OCCURRENCES")
    Long noOfOccurrences;

    @Column(name = "TOTAL_COUNT")
    Long totalCount;

    @Column(name = "SUCCESS_COUNT")
    Long successCount;

    @Column(name = "FAILURE_COUNT")
    Long failureCount;

    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def descdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'desc'

        def queryCriteria = CommunicationRecurrentMessageView.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("name", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name))
            ilike("createdBy", filterData?.params?.createdBy)
            order((descdir ? Order.desc(pagingAndSortParams?.sortColumn) : Order.asc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }

    public static CommunicationRecurrentMessageView fetchById(Long recurrentMessageId) {

        def query =
                CommunicationRecurrentMessageView.withSession { session ->
                    session.getNamedQuery('CommunicationRecurrentMessageView.fetchById')
                            .setLong('recurrentMessageId', recurrentMessageId)
                            .list()[0]

                }
        return query
    }
}


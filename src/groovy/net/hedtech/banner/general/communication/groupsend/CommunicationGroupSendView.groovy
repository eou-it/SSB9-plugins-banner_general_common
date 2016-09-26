/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.groupsend

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.item.CommunicationChannel
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

import javax.persistence.*

/**
 * CommunicationJob
 *
 */
@Entity
@Table(name = "GVQ_GCBGSND")
@EqualsAndHashCode
@ToString
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationGroupSendView.fetchById",
                query = """ FROM CommunicationGroupSendView a
                            WHERE  a.id = :groupSendId
                        """)
])
class CommunicationGroupSendView implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "group_send_id")
    Long id

    /**
     *  Optimistic lock token.
     */
    @Version
    @Column(name = "VERSION")
    Long version

    /** The oracle user name of the person that submitted the group send. **/
    @Column(name = "CREATOR")
    String createdBy

    @Column(name = "group_send_creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    Date creationDateTime;

    @Column(name = "group_send_scheduled_date")
    @Temporal(TemporalType.TIMESTAMP)
    Date scheduledStartDate;

    @Column(name = "group_send_started_date")
    @Temporal(TemporalType.TIMESTAMP)
    Date groupSendStartedDate;

    @Column(name = "group_send_end_date")
    @Temporal(TemporalType.TIMESTAMP)
    Date groupSendEndDate;

    @Column(name = "group_send_name")
    String groupSendName;

    @Column(name = "template_name")
    String templateName;

    @Column(name = "comm_channel")
    @Enumerated(value = EnumType.STRING)
    CommunicationChannel communicationChannel

    @Column(name = "template_id")
    String templateId;

    @Column(name = "population_name")
    String populationName;

    @Column(name = "population_id")
    Long populationId

    @Column(name = "population_version_id")
    Long populationVersionId

    @Column(name = "population_calculation_id")
    Long populationCalculationId

    @Column(name = "organization_name")
    String organizationName;

    @Column(name = "population_calculation_date")
    @Temporal(TemporalType.TIMESTAMP)
    Date populationCalculatedDate

    @Column(name = "population_count")
    Long populationCount

    @Column(name = "group_send_current_state")
    String currentExecutionState

    @Type(type="yes_no")
    @Column(name = "errors_exist")
    boolean errors_exist

    @Column(name = "group_items_processing")
    Long groupItemsProcessingCount

    @Column(name = "group_items_failed")
    Long groupItemsFailedCount

    @Column(name = "group_items_stopped")
    Long groupItemsStoppedCount

    @Column(name = "group_items_processed")
    Long groupItemsProcessedCount

    @Column(name = "communication_jobs_processing")
    Long jobsProcessingCount

    @Column(name = "communication_jobs_failed")
    Long jobsFailedCount

    @Column(name = "communication_jobs_stopped")
    Long jobsStoppedCount

    @Column(name = "communication_jobs_processed")
    Long jobsProcessedCount

    @Column(name = "communication_item_count")
    Long communicationItemCount

    @Column(name = "as_of_date")
    @Temporal(TemporalType.TIMESTAMP)
    Date asOfDate

    @Type(type="yes_no")
    @Column(name = "recalc_on_send")
    boolean recalculateOnSend

    public static CommunicationGroupSendView fetchById(Long groupSendId) {

        def query =
                CommunicationGroupSendView.withSession { session ->
                    session.getNamedQuery('CommunicationGroupSendView.fetchById')
                            .setLong('groupSendId', groupSendId)
                            .list()[0]

                }
        return query
    }

//    Commented out for performance reason in calling a very complex view. Instead of paging through this view directly,
//    the controller fetches from the main entity GroupSend and then does a CommunicationGroupSendView#fetchById on each
//    found object for the current page it is returning back.
//
//    public static def findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {
//
//        def descdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'desc'
//
//        def queryCriteria = CommunicationGroupSendView.createCriteria()
//        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
//            ilike("groupSendName", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.groupSendName))
//            ilike("createdBy", filterData?.params?.createdBy)
//            order((descdir ? Order.desc(pagingAndSortParams?.sortColumn) : Order.asc(pagingAndSortParams?.sortColumn)).ignoreCase())
//        }
//        return results
//    }
}


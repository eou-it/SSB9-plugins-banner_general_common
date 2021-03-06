/*********************************************************************************
 Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.groupsend

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.item.CommunicationChannel
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

import javax.persistence.*

/**
 * CommunicationJob
 *
 */
@Entity
@Table(name = "GVQ_JOBS_LIST")
@EqualsAndHashCode
@ToString
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationGroupSendListView.fetchById",
                query = """ FROM CommunicationGroupSendListView a
                            WHERE  a.id = :groupSendId
                        """)
])
class CommunicationGroupSendListView implements Serializable {

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

    @Column(name = "group_send_date")
    @Temporal(TemporalType.TIMESTAMP)
    Date groupSendDate

    @Column(name = "group_send_name")
    String groupSendName;

    @Column(name = "template_name")
    String templateName;

    @Column(name = "comm_channel")
    @Enumerated(value = EnumType.STRING)
    CommunicationChannel communicationChannel

    @Column(name = "communication_code")
    String communicationCode

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

    @Column(name = "group_send_current_state")
    String currentExecutionState

    @Column(name = "group_send_cumulative_state")
    String cumulativeExecutionState

    @Type(type="yes_no")
    @Column(name = "errors_exist")
    boolean errors_exist

    @Column(name = "as_of_date")
    @Temporal(TemporalType.TIMESTAMP)
    Date asOfDate

    @Type(type="yes_no")
    @Column(name = "recalc_on_send")
    boolean recalculateOnSend

    /**
     * Error Code: The error code for the error scenario that failed
     */
    @Column(name = "error_code")
    @Enumerated(EnumType.STRING)
    CommunicationErrorCode errorCode

    @Lob
    @Column(name = "error_text")
    String errorText

    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def descdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'desc'
        def sortColumn = pagingAndSortParams?.sortColumn

        def queryCriteria = CommunicationGroupSendListView.createCriteria()
        def results
        if(sortColumn.equalsIgnoreCase("groupSendDate")) {
            results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
                ilike("groupSendName", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.groupSendName))
                ilike("createdBy", filterData?.params?.createdBy)
                order((descdir ? Order.desc(sortColumn) : Order.asc(sortColumn)).ignoreCase())
            }
        } else if (sortColumn.equalsIgnoreCase("currentExecutionState")){
            results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
                ilike("groupSendName", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.groupSendName))
                ilike("createdBy", filterData?.params?.createdBy)
                order((descdir ? Order.desc("errors_exist") : Order.asc("errors_exist")).ignoreCase())
                order((descdir ? Order.desc(sortColumn) : Order.asc(sortColumn)).ignoreCase())
                order(Order.desc("groupSendDate").ignoreCase())
            }
        } else {
            results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
                ilike("groupSendName", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.groupSendName))
                ilike("createdBy", filterData?.params?.createdBy)
                order((descdir ? Order.desc(sortColumn) : Order.asc(sortColumn)).ignoreCase())
                order(Order.desc("groupSendDate").ignoreCase())
            }
        }
        return results
    }

    public static CommunicationGroupSendListView fetchById(Long groupSendId) {

        def query =
                CommunicationGroupSendListView.withSession { session ->
                    session.getNamedQuery('CommunicationGroupSendListView.fetchById')
                            .setLong('groupSendId', groupSendId)
                            .list()[0]

                }
        return query
    }
}


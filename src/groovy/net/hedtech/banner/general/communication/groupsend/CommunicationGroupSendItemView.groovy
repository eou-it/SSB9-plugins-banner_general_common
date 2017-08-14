/*********************************************************************************
 Copyright 2014-2017 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.groupsend

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.CommunicationErrorCode
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

import javax.persistence.*

/**
 * A communication group send item.
 */
@Entity
@Table(name = "GVQ_GCRGSIM")
@EqualsAndHashCode
@ToString
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationGroupSendItemView.fetchByGroupSendId",
                query = """ FROM CommunicationGroupSendItemView a
                            WHERE  a.groupSendId = :groupSendId
                            ORDER BY a.lastModified desc
                        """)
])
class CommunicationGroupSendItemView implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "group_item_id")
    Long id

    @Column(name = "group_send_id")
    Long groupSendId

    /**
     *  Optimistic lock token.
     */
    @Version
    @Column(name = "VERSION")
    Long version

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "LAST_MODIFIED_BY")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "LAST_MODIFIED")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    @Column(name="ITEM_STATUS")
    String currentExecutionState;

    /**
     * The error code
     */
    @Column(name = "error_code")
    CommunicationErrorCode errorCode

    @Column(name="START_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date startedDate;

    @Column(name="STOP_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date stopDate;

    /** Correlation ID linking the communication request to the recipient data to the communication job to the final communication item. **/
    @Column(name = "REFERENCE_ID")
    String referenceId
    /**
     * This field defines the identification number used to access person on-line.
     */
    @Column(name = "BANNER_ID")
    String bannerId

    /**
     * This field defines the last name of person.
     */
    @Column(name = "LAST_NAME")
    String lastName

    /**
     * This field entities the first name of person.
     */
    @Column(name = "FIRST_NAME")
    String firstName

    /**
     * This field entities the middle name of person.
     */
    @Column(name = "MIDDLE_NAME")
    String middleName

    /**
     * This field entifies the middle name of person.
     */
    @Column(name = "SURNAME_PREFIX")
    String surnamePrefix

    /**
     * This field identifies if a person record is confidential
     *
     */
    @Type(type = "yes_no")
    @Column(name = "CONFIDENTIAL_IND")
    Boolean confidential

    /**
     * This field indicates if a person is deceased.
     */
    @Type(type = "yes_no")
    @Column(name = "DECEASED_IND")
    Boolean deceased

    public static List<CommunicationGroupSendItemView> fetchByGroupSendId(Long groupSendId) {

        def query =
                CommunicationGroupSendItemView.withSession { session ->
                    session.getNamedQuery('CommunicationGroupSendItemView.fetchByGroupSendId')
                            .setLong('groupSendId', groupSendId)
                            .list()

                }
        return query
    }


    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def ascdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'asc'
        def searchName = CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name)
        def searchStatus = filterData?.params?.status

        def queryCriteria = CommunicationGroupSendItemView.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            eq ("groupSendId", filterData?.params?.jobId)
            ilike ("currentExecutionState", searchStatus)
            or {
                ilike("lastName", searchName)
                ilike("firstName", searchName)
                ilike("bannerId", searchName)
            }
            order((ascdir ? Order.asc(pagingAndSortParams?.sortColumn) : Order.desc(pagingAndSortParams?.sortColumn)))
            order(Order.asc("currentExecutionState"))

        }
        return results
    }

}

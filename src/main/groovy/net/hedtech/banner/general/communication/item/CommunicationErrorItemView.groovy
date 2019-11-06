package net.hedtech.banner.general.communication.item

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.CommunicationErrorCode
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

import javax.persistence.*

/**
 * Communication Error Item View. Denotes a communication that had to be sent to a recipient and an error encountered in the process.
 */
@Entity
@EqualsAndHashCode
@ToString
@Table(name = "gvq_error_log")
class CommunicationErrorItemView implements Serializable {

    /**
     * Surrogate Id of the corresponding group send record
     */
    @Column(name = "GCBGSND_SURROGATE_ID")
    Long groupSendSurrogateId

    /**
     * Surrogate Id of the corresponding group send record
     */
    @Id
    @Column(name = "REFERENCE_ID")
    String referenceId

    /**
     * Username of the person who initiated the communication.
     */
    @Column(name = "INITIATED_BY")
    String name

    /**
     * The date the communication item was created in the system.
     */
    @Column(name = "CREATE_DATE_TIME")
    Date createDate

    /**
     * The Banner ID of the recipient
     */
    @Column(name = "banner_id")
    String bannerId

    /**
     * The pidm of the recipient
     */
    @Column(name = "pidm")
    Long pidm

    /**
     * The first name of the recipient
     */
    @Column(name = "first_name")
    String firstName

    /**
     * The last name of the recipient
     */
    @Column(name = "last_name")
    String lastName

    /**
     * The middle name of the recipient
     */
    @Column(name = "middle_name")
    String middleName

    /**
     * The surname prefix of the recipient
     */
    @Column(name = "surname_prefix")
    String surnamePrefix

    /**
     * This field identifies if a person record is confidential
     *
     */
    @Type(type = "yes_no")
    @Column(name = "confidential_ind")
    Boolean confidential

    /**
     * This field indicates if a person is deceased.
     */
    @Type(type = "yes_no")
    @Column(name = "deceased_ind")
    Boolean deceased

    /**
     * The date the communication item was errored out.
     */
    @Column(name = "ACTIVITY_DATE")
    Date errorDate

    /**
     * The name of the template send in the communication
     */
    @Column(name = "template_name")
    String templateName

    /**
     * The name of the organization on whose behalf the communication was sent
     */
    @Column(name = "organization_name")
    String organizationName

    /**
     * Email, Letter, etc
     */
    @Column(name = "communication_channel")
    @Enumerated(value = EnumType.STRING)
    CommunicationChannel communicationChannel

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "VERSION")
    Long version

    /**
     * The name of the communication job
     */
    @Column(name = "job_name")
    String jobName

    /**
     * The error code
     */
    @Column(name = "error_code")
    @Enumerated(value = EnumType.STRING)
    CommunicationErrorCode errorCode

    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def descdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'desc'

        def queryCriteria = CommunicationErrorItemView.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("bannerId", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.bannerId))
            order((descdir ? Order.desc(pagingAndSortParams?.sortColumn) : Order.asc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }

    public static findByNameWithPagingAndSortParams(String bannerId, Date fromDate, Date toDate, String subSearchString, pagingAndSortParams) {

        def ascdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'asc'

        def queryCriteria = CommunicationErrorItemView.createCriteria()
        def results
        if (subSearchString && subSearchString.trim().length() > 0) {
            if (!subSearchString.contains( '%' )) {
                subSearchString = '%' + subSearchString + '%'
            }

            results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
                eq("bannerId", bannerId)
                between(errorDate, fromDate, toDate)
                or {
                    ilike( "firstName", subSearchString )
                    ilike( "middleName", subSearchString )
                    ilike( "lastName", subSearchString )
                    ilike( "organizationName", subSearchString )
                    ilike( "name", subSearchString )
                    ilike( "templateName", subSearchString )
                    ilike( "jobName", subSearchString )
                }
                order((ascdir ? Order.asc(pagingAndSortParams?.sortColumn) : Order.desc(pagingAndSortParams?.sortColumn)).ignoreCase())
            }
        } else {
            Date today = new Date()
            results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
                eq("bannerId", bannerId)
                //fetch for last 7 days
                between(errorDate, today-7, today)
                order((ascdir ? Order.asc(pagingAndSortParams?.sortColumn) : Order.desc(pagingAndSortParams?.sortColumn)).ignoreCase())
            }
        }

        return results
    }
}

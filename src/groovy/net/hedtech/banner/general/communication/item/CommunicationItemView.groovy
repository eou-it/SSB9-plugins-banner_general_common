/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.item

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version
/**
 * Communication Item View. Denotes a communication that has been sent to a recipient.
 */
@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GVQ_GCRCITM")
class CommunicationItemView implements Serializable {

    /**
     * SURROGATE ID: Immutable unique key.
     */
    @Id
    @Column(name = "SURROGATE_ID")
    Long id

    /**
     * Username of the person who initiated the communication.
     */
    @Column(name = "initiated_by")
    String name

    /**
     * The date the communication item was created in the system.
     */
    @Column(name = "create_date")
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
     * The date the communication item was actually sent.
     */
    @Column(name = "sent_date")
    Date sentDate

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
    CommunicationChannel communicationChannel

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "VERSION")
    Long version

    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def descdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'desc'

        def queryCriteria = CommunicationItemView.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("bannerId", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.bannerId))
            order((descdir ? Order.desc(pagingAndSortParams?.sortColumn) : Order.asc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }

}

/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.organization

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

import javax.persistence.*

/**
 * Organization is the internal department or group that owns the communication template, and holds the return
 * address information for emails
 *
 */
@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GVQ_GCRORAN_LIST")
class CommunicationOrganizationListView implements Serializable {
    /**
     * Generated unique key.
     */
    @Id
    @Column(name = "GCRORAN_SURROGATE_ID")
    Long id

    /**
     * Name of the organization.
     */
    @Column(name = "GCRORAN_NAME")
    String name

    /**
     * Description of the organization.
     */
    @Column(name = "GCRORAN_DESCRIPTION")
    String description


    @Type(type = "yes_no")
    @Column(name = "GCRORAN_IS_ROOT")
    Boolean isRoot = false


    @Type(type = "yes_no")
    @Column(name = "GCRORAN_AVAILABLE_IND")
    Boolean isAvailable = false
    /**
     * Optimistic lock token.
     */
    @Version
    @Column(name = "GCRORAN_VERSION")
    Long version

    static constraints = {
        name(nullable: false, maxSize: 1020)
        description(nullable: true)
        isRoot(nullable: true)
        isAvailable(nullable:true)
    }

    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def ascdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'asc'

        def queryCriteria = CommunicationOrganizationListView.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("name", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name))
            eq("isRoot",false)
            order((ascdir ? Order.asc(pagingAndSortParams?.sortColumn) : Order.desc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }
}

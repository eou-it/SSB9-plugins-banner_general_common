/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.organization

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.*

/**
 * Organization is the internal department or group that owns the communication template, and holds the return
 * address information for emails
 *
 */
@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GVQ_GCRORAN")
class CommunicationOrganizationView implements Serializable {
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


    @Type(type = "yes_no")
    @Column(name = "GCRORAN_IS_ROOT")
    Boolean isRoot = false


    /**
     * Optimistic lock token.
     */
    @Version
    @Column(name = "GCRORAN_VERSION")
    Long version

    static constraints = {
        name(nullable: false, maxSize: 1020)
        isRoot(nullable: true)
    }

}

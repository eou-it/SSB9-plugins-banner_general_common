/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.*

/**
 * This table stores a mapping  between PIDM and Sourced ID.
 */

@Entity
@Table(name = "GOBSRID")
@ToString(includeNames = true, includeFields = true)
@EqualsAndHashCode(includeFields = true)
class PidmAndSourcedIdMapping implements Serializable {

    /**
     * Surrogate ID for GOBSRID
     */
    @Id
    @Column(name = "GOBSRID_SURROGATE_ID")
    Long id

    /**
     * Optimistic lock token for GOBSRID
     */
    @Version
    @Column(name = "GOBSRID_VERSION")
    Long version

    /**
     * Sourced ID
     */
    @Column(name = "GOBSRID_SOURCED_ID")
    String sourcedId

    /**
     * PIDM: Internal identification number of the person.
     */
    @Column(name = "GOBSRID_PIDM")
    Integer pidm

    /**
     * ACTIVITY DATE: Date on which the record was created or last updated.
     */
    @Column(name = "GOBSRID_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * User ID: User ID of the user who created or last updated the record.
     */
    @Column(name = "GOBSRID_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the row.
     */
    @Column(name = "GOBSRID_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        pidm(nullable: false, min: -99999999, max: 99999999)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['sourcedId']

}

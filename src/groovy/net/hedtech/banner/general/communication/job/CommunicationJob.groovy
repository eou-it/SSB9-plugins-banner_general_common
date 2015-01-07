/*******************************************************************************
Copyright 2014 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.general.communication.job

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.*

/**
 * Communication Log. Record of individual communication item final status. entity.
 */
@Entity
@ToString
@EqualsAndHashCode
@Table(name = "GCBCJOB")

class CommunicationJob implements Serializable {

    /**
     * SURROGATE ID: Generated unique numeric identifier for this entity.
     */
    @Id
    @Column(name = "GCBCJOB_SURROGATE_ID")
    @SequenceGenerator(name = "GCBCJOB_SEQ_GEN", allocationSize = 1, sequenceName = "GCBCJOB_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCBCJOB_SEQ_GEN")
    Long id


    /**
     * SEND REFERENCE ID: The reference id of the group send item that initiated this communication.
     */
    @Column(name = "GCBCJOB_REFERENCE_ID")
    String referenceId

    /**
     * STATUS: The final disposition of the communication send operation. SENT, ERROR.
     */
    @Column(name = "GCBCJOB_STATUS")
    String status

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "GCBCJOB_VERSION")
    Integer version

    /**
     * ACTIVITY DATE: Date that record was created or last updated.
     */
    @Column(name = "GCBCJOB_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCBCJOB_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCBCJOB_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        referenceId(nullable: false, maxSize: 255)
        status(nullable: false, maxSize: 30)
    }


    // Read Only fields that should be protected against update
    public static readonlyProperties = [ 'id' ]

}

/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GCRPVJB")
class CommunicationPopulationVersionJobAssociation implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCRPVJB_SURROGATE_ID")
    @SequenceGenerator(name = "GCRPVJB_SEQ_GEN", allocationSize = 1, sequenceName = "GCRPVJB_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRPVJB_SEQ_GEN")
    Long id

    /**
     * Foreign key reference to the population version.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRPVJB_POPV_ID", referencedColumnName = "GCRPOPV_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRPVJB_INV_GCRPOPV")
    CommunicationPopulationVersion populationVersion

    /**
     * JOB ID : UUID of the quartz job for the population version calculation
     */
    @Column(name = "GCRPVJB_QZJB_ID")
    String jobId

    /**
     * ACTIVITY_DATE: Most current date record was created or changed.
     */
    @Column(name = "GCRPVJB_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER_ID: Oracle User ID of the person who last inserted or last updated the data.
     */
    @Column(name = "GCRPVJB_USER_ID")
    String lastModifiedBy

    /**
     * VERSION: Optimistic lock token
     */
    @Version
    @Column(name = "GCRPVJB_VERSION")
    Long version

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCRPVJB_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        populationVersion(nullable: false)
        jobId(nullable: false)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }
}

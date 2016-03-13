/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersion
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionList

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
@Table(name = "GCRPVID")
class CommunicationPopulationVersionQueryAssociation implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCRPVID_SURROGATE_ID")
    @SequenceGenerator(name = "GCRPVID_SEQ_GEN", allocationSize = 1, sequenceName = "GCRPVID_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRPVID_SEQ_GEN")
    Long id

    /**
     * Foreign key reference to the population version.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRPVID_POPV_ID", referencedColumnName = "GCRPOPV_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRPVID_INV_GCRPOPV")
    CommunicationPopulationVersion populationVersion

    /**
     * Foreign key reference to the population query version.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRPVID_QRYV_ID", referencedColumnName = "GCRQRYV_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRPVID_INV_GCRQRYV")
    CommunicationPopulationQueryVersion populationQueryVersion

    /**
     * Foreign key reference to the population selection list.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRPVID_SLIS_ID", referencedColumnName = "GCRSLIS_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRPVID_INV_GCRSLIS")
    CommunicationPopulationSelectionList selectionList

    /**
     * The error code if calculation results in an error
     */
    @Column(name = "GCRPVID_ERROR_CODE")
    String errorCode

    /**
     * The error text or stacktrace if calculation results in an error
     */
    @Column(name = "GCRPVID_ERROR_TEXT")
    String errorText

    /**
     * ACTIVITY_DATE: Most current date record was created or changed.
     */
    @Column(name = "GCRPVID_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER_ID: Oracle User ID of the person who last inserted or last updated the data.
     */
    @Column(name = "GCRPVID_USER_ID")
    String lastModifiedBy

    /**
     * VERSION: Optimistic lock token
     */
    @Version
    @Column(name = "GCRPVID_VERSION")
    Long version

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCRPVID_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        populationVersion(nullable: false)
        populationQueryVersion(nullable: false)
        selectionList(nullable: true)
        errorCode(nullable: true)
        errorText(nullable: true)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }
}

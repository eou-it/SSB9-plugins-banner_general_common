/*******************************************************************************
 Copyright 2013-2018 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import net.hedtech.banner.general.system.Document
import net.hedtech.banner.general.system.VisaSource
import net.hedtech.banner.general.system.VisaType

import javax.persistence.*

/**
 * Document Information Table
 */
@NamedQueries(value = [
@NamedQuery(
        name = "DocumentInformation.fetchByPidmAndSequenceNumber",
        query = """
           FROM DocumentInformation a
          WHERE a.pidm = :pidm
            AND a.sequenceNumber = :sequenceNumber"""),

@NamedQuery(
        name = "DocumentInformation.fetchDuplicateRecord",
        query = """
           FROM DocumentInformation a
          WHERE a.pidm = :pidm
            AND a.sequenceNumber = :sequenceNumber
            AND a.document.code = :documentCode
            AND a.visaType.code = :visaTypeCode
            AND a.id <> :id"""),
])

@Entity
@Table(name = "GORDOCM")
@EqualsAndHashCode(includeFields = true)
class DocumentInformation implements Serializable {

    /**
     * Surrogate ID for GORDOCM
     */
    @Id
    @Column(name = "GORDOCM_SURROGATE_ID")
    @SequenceGenerator(name = "GORDOCM_SEQ_GEN", allocationSize = 1, sequenceName = "GORDOCM_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GORDOCM_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GORDOCM
     */
    @Version
    @Column(name = "GORDOCM_VERSION")
    Long version

    /**
     * PERSONAL ID NUMBER: This field indicates Personal Identification Number.
     */
    @Column(name = "GORDOCM_PIDM")
    Integer pidm

    /**
     * SEQUENCE NUMBER: This field indicates a Record Sequence Number.
     */
    @Column(name = "GORDOCM_SEQ_NO")
    Integer sequenceNumber

    /**
     * VISA NUMBER: This field indicates a unique identification number assigned to a Visa.
     */
    @Column(name = "GORDOCM_VISA_NUMBER")
    String visaNumber

    /**
     * DOCUMENT DISPOSITION: This field indicates the status of the Document.
     */
    @Column(name = "GORDOCM_DISPOSITION")
    String disposition

    /**
     * REQUEST DATE: This field indicates the date the Document was requested.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "GORDOCM_REQUEST_DATE")
    Date requestDate

    /**
     * RECEIVED DATE: This field indicates the date the Document was received.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "GORDOCM_RECEIVED_DATE")
    Date receivedDate

    /**
     * ACTIVITY DATE: The date that the information for the row was inserted or updated in the GORDOCM table.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "GORDOCM_ACTIVITY_DATE")
    Date lastModified

    /**
     * USER IDENTIFICATION: The unique identification of the user who changed the record.
     */
    @Column(name = "GORDOCM_USER_ID")
    String lastModifiedBy

    /**
     * Data origin column for GORDOCM
     */
    @Column(name = "GORDOCM_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FKV_GORDOCM_INV_GTVDOCM_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GORDOCM_DOCM_CODE", referencedColumnName = "GTVDOCM_CODE")
    ])
    Document document

    /**
     * Foreign Key : FKV_GORDOCM_INV_GTVSRCE_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GORDOCM_SRCE_CODE", referencedColumnName = "GTVSRCE_CODE")
    ])
    VisaSource visaSource

    /**
     * Foreign Key : FKV_GORDOCM_INV_STVVTYP_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GORDOCM_VTYP_CODE", referencedColumnName = "STVVTYP_CODE")
    ])
    VisaType visaType


    public String toString() {
        """DocumentInformation[
					id=$id, 
					version=$version, 
					pidm=$pidm, 
					sequenceNumber=$sequenceNumber, 
					visaNumber=$visaNumber, 
					disposition=$disposition, 
					requestDate=$requestDate, 
					receivedDate=$receivedDate, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin, 
					document=$document, 
					visaSource=$visaSource, 
					visaType=$visaType]"""
    }


    static constraints = {
        pidm(nullable: false, min: -99999999, max: 99999999)
        sequenceNumber(nullable: false, min: -999, max: 999)
        visaNumber(nullable: true, maxSize: 18)
        disposition(nullable: false, maxSize: 1, inList: ["P", "R", "A"])
        requestDate(nullable: true)
        receivedDate(nullable: true)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        document(nullable: false)
        visaSource(nullable: true)
        visaType(nullable: false)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['pidm', 'sequenceNumber', 'document', 'visaType']


    static def fetchByPidmAndSequenceNumber(int pidm, int sequenceNumber) {
        DocumentInformation.withSession { session ->
            List documentInformations = session.getNamedQuery('DocumentInformation.fetchByPidmAndSequenceNumber')
                    .setInteger('pidm', pidm)
                    .setInteger('sequenceNumber', sequenceNumber)
                    .list()
            return documentInformations
        }
    }


    static def fetchDuplicateRecord(map) {
        DocumentInformation.withSession { session ->
            def id = map?.id
            if (id == null) id = -1
            List documentInformations = session.getNamedQuery('DocumentInformation.fetchDuplicateRecord')
                    .setInteger('pidm', map?.pidm)
                    .setInteger('sequenceNumber', map?.sequenceNumber)
                    .setString('documentCode', map?.document?.code)
                    .setString('visaTypeCode', map?.visaType?.code)
                    .setLong('id', id)
                    .list()
            return documentInformations
        }
    }
}

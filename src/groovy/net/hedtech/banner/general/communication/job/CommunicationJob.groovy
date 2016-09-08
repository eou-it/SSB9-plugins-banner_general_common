/*******************************************************************************
Copyright 2014 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.general.communication.job

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.asynchronous.task.AsynchronousTask
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.service.DatabaseModifiesState

import javax.persistence.*

/**
 * Communication Log. Record of individual communication item final status. entity.
 */
@Entity
@ToString
@EqualsAndHashCode
@Table(name = "GCBCJOB")
@DatabaseModifiesState
@NamedQueries(value = [
    @NamedQuery( name = "CommunicationJob.fetchPending",
        query = """ FROM CommunicationJob job
                    WHERE job.status = :status_
                    ORDER BY job.id ASC """
    ),
    @NamedQuery( name = "CommunicationJob.fetchByStatusAndReferenceId",
            query = """ FROM CommunicationJob job
        WHERE job.status = :status_
        ORDER BY job.id ASC """
    )
])
class CommunicationJob implements AsynchronousTask {

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
    @Column(name = "GCBCJOB_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private CommunicationJobStatus status = CommunicationJobStatus.PENDING

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
     * DATA ORIGIN: Source s dystem that created or updated the data.
     */
    @Column(name = "GCBCJOB_DATA_ORIGIN")
    String dataOrigin

    @Column(name="GCBCJOB_CREATION_DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date creationDateTime;

    @Column(name="GCBCJOB_ERROR_TEXT")
    @Lob
    String errorText;

    /**
     * Error Code: The error code for the error scenario that failed the Communication Job
     */
    @Column(name = "GCBCJOB_ERROR_CODE")
    @Enumerated(EnumType.STRING)
    CommunicationErrorCode errorCode

    static constraints = {
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        referenceId(nullable: false, maxSize: 255)
        status(nullable: false, maxSize: 30)
        errorText(nullable: true)
        errorCode(nullable: true)
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = [ 'id' ]


    CommunicationJobStatus getStatus() {
        return status
    }

    void setStatus(CommunicationJobStatus status) {
        this.status = status
    }

    public static List fetchPending( Integer max = Integer.MAX_VALUE ) {
        def results
        CommunicationJob.withSession { session ->
            results = session.getNamedQuery( 'CommunicationJob.fetchByStatusAndReferenceId' )
                    .setParameter( 'status_', CommunicationJobStatus.PENDING )
                    .setFirstResult( 0 )
                    .setMaxResults( max )
                    .list()
        }
        return results
    }

    public static List fetchCompleted() {
        def results
        CommunicationJob.withSession { session ->
            results = session.getNamedQuery( 'CommunicationJob.fetchByStatusAndReferenceId' )
                .setParameter( 'status_', CommunicationJobStatus.COMPLETED )
                .setFirstResult( 0 )
                .setMaxResults( Integer.MAX_VALUE )
                .list()
        }
        return results
    }

}

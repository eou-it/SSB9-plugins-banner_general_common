/*********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.item

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.asynchronous.task.AsynchronousTask
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.job.CommunicationJobStatus

import javax.persistence.*

/**
 * CommunicationSendItem.
 * Defines the common attributes required for communication items.
 */
@Entity
@Table(name = "GCRSITM")
@Inheritance(strategy = InheritanceType.JOINED)
@EqualsAndHashCode
@ToString
@NamedQueries(value = [
        @NamedQuery( name = "CommunicationSendItem.fetchPending",
                query = """ FROM CommunicationSendItem sendItem
                    WHERE sendItem.status = :status_
                    ORDER BY sendItem.id ASC """
        ),
        @NamedQuery( name = "CommunicationSendItem.fetchPendingTextMessages",
                query = """ FROM CommunicationSendItem sendItem
                    WHERE sendItem.status = :status_
                    AND sendItem.communicationChannel = 'TEXT_MESSAGE'
                    ORDER BY sendItem.id ASC """
        )
])
public abstract class CommunicationSendItem implements AsynchronousTask, Serializable {
    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCRSITM_SURROGATE_ID")
    @SequenceGenerator(name = "GCRSITM_SEQ_GEN", allocationSize = 1, sequenceName = "GCRSITM_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRSITM_SEQ_GEN")
    Long id

    /**
     *  The communication type of this item : EMAIL, LETTER, etc
     */
    @Column(name = "GCRSITM_COMM_CHANNEL")
    @Enumerated(value = EnumType.STRING)
    CommunicationChannel communicationChannel

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCRSITM_CREATOR_ID")
    String createdBy

    /**
     *  Date record is created
     */
    @Column(name = "GCRSITM_CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date creationDateTime

    /**
     * Date from which the item was sent.
     */
    @Column(name = "GCRSITM_SENT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date sentDate

    /**
     * STATUS: The final disposition of the communication send operation. SENT, ERROR.
     */
    @Column(name = "GCRSITM_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    CommunicationJobStatus status = CommunicationJobStatus.PENDING

    @Column(name="GCRSITM_ERROR_TEXT")
    @Lob
    String errorText;

    /**
     * Error Code: The error code for the error scenario that failed the Communication send item
     */
    @Column(name = "GCRSITM_ERROR_CODE")
    @Enumerated(EnumType.STRING)
    CommunicationErrorCode errorCode

    /** The target of the send item
     *
     */
//    @Column(name = "GCRSITM_RECIPIENT_PIDM")
//    Long recipientPidm;

//    /**
//     * The template associated with the template item
//     */
//    @Column(name = "GCRSITM_TEMPLATE_ID")
//    Long templateId

    /**
     * The organization id
     */
    @Column(name = "GCRSITM_ORGANIZATION_ID")
    Long organizationId

    /**
     * The source
     */
    @Column(name="GCRSITM_SOURCE")
    String source

    /**
     * The reference id
     */
    @Column(name = "GCRSITM_REFERENCE_ID")
    String referenceId

    /**
     *  Optimistic lock token.
     */
    @Version
    @Column(name = "GCRSITM_VERSION")
    Long version

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCRSITM_USER_ID")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "GCRSITM_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *  Source system that created or updated the data.
     */
    @Column(name = "GCRSITM_DATA_ORIGIN")
    String dataOrigin

    @Column(name = "GCRSITM_VPDI_CODE")
    String mepCode

    static constraints = {

        communicationChannel(nullable: true)
//        recipientPidm(nullable: true)
        sentDate(nullable: true)
//        templateId(nullable: true)
        source(nullable:false)
        referenceId(nullable: false)
        organizationId(nullable: true)
        createdBy(nullable: false)
        creationDateTime(nullable: false)
        errorCode(nullable: true)
        errorText(nullable: true)
        lastModified(nullable: false)
        lastModifiedBy(nullable: false, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        mepCode(nullable: true)
    }

    /**
     * Calls the visitor method that pertains to type of this object. An email template will call the
     * visitEmail method on the visitor. Useful when inheritance is not an option or when the code needs
     * to traverse a collection of objects.
     *
     * @param visitor an object implementing the template visitor interface and typically a method object itself
     * @return
     */
    public abstract CommunicationSendItemVisitor accept( CommunicationSendItemVisitor visitor )

    public static List fetchPending( Integer max = Integer.MAX_VALUE ) {
        def results
        CommunicationSendItem.withSession { session ->
            results = session.getNamedQuery( 'CommunicationSendItem.fetchPending' )
                    .setParameter( 'status_', CommunicationJobStatus.PENDING )
                    .setFirstResult( 0 )
                    .setMaxResults( max )
                    .list()
        }
        return results
    }

    public static List fetchPendingTextMessages( Integer max = Integer.MAX_VALUE ) {
        def results
        CommunicationSendItem.withSession { session ->
            results = session.getNamedQuery( 'CommunicationSendItem.fetchPendingTextMessages' )
                    .setParameter( 'status_', CommunicationJobStatus.HOLD )
                    .setFirstResult( 0 )
                    .setMaxResults( max )
                    .list()
        }
        return results
    }
}

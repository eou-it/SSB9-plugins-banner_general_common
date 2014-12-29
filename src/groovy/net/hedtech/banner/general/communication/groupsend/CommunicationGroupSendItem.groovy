/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.groupsend

import org.hibernate.annotations.Type

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

/**
 * A communication job item is an order to send a message to an individual recipient.
 */
@Entity
@Table(name = "GCRGSIM")
@NamedQueries(value = [
    @NamedQuery( name = "CommunicationGroupSendItem.fetchByGroupSend",
        query = """ FROM CommunicationGroupSendItem gsi
                    WHERE gsi.communicationGroupSend = :groupSend """
    )
])
class CommunicationGroupSendItem implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCRGSIM_SURROGATE_ID")
    @SequenceGenerator(name = "GCRGSIM_SEQ_GEN", allocationSize = 1, sequenceName = "GCRGSIM_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRGSIM_SEQ_GEN")
    Long Id

    /**
     *  Optimistic lock token.
     */
    @Version
    @Column(name = "GCRGSIM_VERSION")
    Long version

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCRGSIM_USER_ID")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "GCRGSIM_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *  Source system that created or updated the data.
     */
    @Column(name = "GCRGSIM_DATA_ORIGIN")
    String dataOrigin

    /**
     * Parent communication job
     */
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn(name="GCRGSIM_GCBGSND_ID", referencedColumnName="GCBGSND_SURROGATE_ID", nullable=false )
    CommunicationGroupSend communicationGroupSend;

    /** The target of the send item */
    @Column(name="GCRGSIM_PIDM")
    Long recipientPidm;

    @Column(name="GCRGSIM_CURRENT_STATE")
    @Enumerated(EnumType.STRING)
    CommunicationGroupSendItemExecutionState currentExecutionState;

    @Column(name="GCRGSIM_ERROR_TEXT", nullable=true)
    @Lob
    String errorText;

    @Column(name="GCRGSIM_STARTED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date startedDate;

    @Column(name="GCRGSIM_STOP_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date stopDate;

    /** Correlation ID linking the communication request to the recipient data to the communication job to the final communication item. **/
    @Column(name = "GCRGSIM_REFERENCE_ID")
    String referenceId

    @Column(name="GCRGSIM_CREATIONDATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    Date creationDateTime;

    static constraints = {
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    public static List fetchByGroupSend( CommunicationGroupSend groupSend ) {
        def results
        CommunicationGroupSend.withSession { session ->
            results = session.getNamedQuery( 'CommunicationGroupSendItem.fetchByGroupSend' )
                .setParameter( 'groupSend', groupSend )
                .list()
        }
        return results
    }

}

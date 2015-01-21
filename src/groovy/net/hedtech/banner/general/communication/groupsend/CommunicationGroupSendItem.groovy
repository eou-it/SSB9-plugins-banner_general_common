/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.groupsend

import net.hedtech.banner.general.asynchronous.task.AsynchronousTask
import net.hedtech.banner.service.DatabaseModifiesState
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
@DatabaseModifiesState
@NamedQueries(value = [
    @NamedQuery( name = "CommunicationGroupSendItem.fetchByGroupSend",
        query = """ FROM CommunicationGroupSendItem gsi
                    WHERE gsi.communicationGroupSend = :groupSend """
    ),
    @NamedQuery(name = "CommunicationGroupSendItem.fetchByExecutionState",
            query = """ FROM CommunicationGroupSendItem gsi
                     WHERE gsi.currentExecutionState = :executionState
                     ORDER by gsi.creationDateTime asc"""
    ),
    @NamedQuery(name = "CommunicationGroupSendItem.fetchByCompleteExecutionStateAndGroupSend",
            query = """ FROM CommunicationGroupSendItem gsi
                     WHERE gsi.communicationGroupSend = :groupSend
                     and gsi.currentExecutionState = :executionState
                     ORDER by gsi.creationDateTime asc"""
    )
])
class CommunicationGroupSendItem implements AsynchronousTask {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCRGSIM_SURROGATE_ID")
    @SequenceGenerator(name = "GCRGSIM_SEQ_GEN", allocationSize = 1, sequenceName = "GCRGSIM_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRGSIM_SEQ_GEN")
    Long id

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
    @JoinColumn(name="GCRGSIM_GROUP_SEND_ID", referencedColumnName="GCBGSND_SURROGATE_ID")
    CommunicationGroupSend communicationGroupSend;

    /** The target of the send item */
    @Column(name="GCRGSIM_PIDM")
    Long recipientPidm;

    @Column(name="GCRGSIM_CURRENT_STATE")
    @Enumerated(EnumType.STRING)
    CommunicationGroupSendItemExecutionState currentExecutionState;

    @Column(name="GCRGSIM_ERROR_TEXT")
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
        stopDate(nullable:true)
        errorText(nullable:true)
    }

    public static List fetchByGroupSend( CommunicationGroupSend groupSend ) {
        def results
        CommunicationGroupSendItem.withSession { session ->
            results = session.getNamedQuery( 'CommunicationGroupSendItem.fetchByGroupSend' )
                .setParameter( 'groupSend', groupSend )
                .list()
        }
        return results
    }


    public static List fetchByCompleteExecutionStateAndGroupSend( CommunicationGroupSend groupSend, Integer max = Integer.MAX_VALUE ) {
        def results
        CommunicationGroupSendItem.withSession { session ->
            results = session.getNamedQuery( 'CommunicationGroupSendItem.fetchByCompleteExecutionStateAndGroupSend' )
                .setParameter( 'groupSend', groupSend )
                .setParameter( 'executionState', CommunicationGroupSendItemExecutionState.Complete )
                .setFirstResult( 0 )
                .setMaxResults( max )
                .list()
        }
        return results
    }

    public static List fetchByReadyExecutionState( Integer max = Integer.MAX_VALUE ) {
        def results
        CommunicationGroupSendItem.withSession { session ->
            results = session.getNamedQuery( 'CommunicationGroupSendItem.fetchByExecutionState' )
                .setParameter( 'executionState', CommunicationGroupSendItemExecutionState.Ready )
                .setFirstResult( 0 )
                .setMaxResults( max )
                .list()
        }
        return results
    }

    public static List fetchByCompleteExecutionState( Integer max = Integer.MAX_VALUE ) {
        def results
        CommunicationGroupSendItem.withSession { session ->
            results = session.getNamedQuery( 'CommunicationGroupSendItem.fetchByExecutionState' )
                .setParameter( 'executionState', CommunicationGroupSendItemExecutionState.Complete )
                .setFirstResult( 0 )
                .setMaxResults( max )
                .list()
        }
        return results
    }


}
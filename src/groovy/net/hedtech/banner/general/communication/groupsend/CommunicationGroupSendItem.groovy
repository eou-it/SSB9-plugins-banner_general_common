/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.groupsend

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
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

/**
 * A communication job item is an order to send a message to an individual recipient.
 */
@Entity
@Table(name = "GCRCJIT")
class CommunicationGroupSendItem {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCRCJIT_SURROGATE_ID")
    @SequenceGenerator(name = "GCRCJIT_SEQ_GEN", allocationSize = 1, sequenceName = "GCRCJIT_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRCJIT_SEQ_GEN")
    Long Id

    /**
     *  Optimistic lock token.
     */
    @Version
    @Column(name = "GCRCJIT_VERSION")
    Long version

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCRCJIT_USER_ID")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "GCRCJIT_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *  Source system that created or updated the data.
     */
    @Column(name = "GCRCJIT_DATA_ORIGIN")
    String dataOrigin

    /**
     * Parent communication job
     */
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn(name="GCRCJIT_GCBCJOB_ID", referencedColumnName="GCBCJOB_SURROGATE_ID", nullable=false )
    CommunicationJob communicationJob;

    /** The target of the send item */
    @Column(name="GCRCJIT_PIDM")
    Long recipientPidm;

    @Column(name="GCRCJIT_CURRENT_STATE")
    @Enumerated(EnumType.STRING)
    CommunicationGroupSendItemExecutionState currentExecutionState;

    @Column(name="GCRCJIT_ERROR_TEXT", nullable=true)
    @Lob
    String errorText;

    @Column(name="GCRCJIT_STARTED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date startedDate;

    @Column(name="GCRCJIT_STOP_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date stopDate;

    static constraints = {
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

}

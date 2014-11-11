/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.item

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.*

/**
 * CommunicationItem.
 * Defines the common attributes required for communication items.
 */
@Entity
@Table(name = "GCRCITM")
@Inheritance(strategy = InheritanceType.JOINED)
@EqualsAndHashCode
@ToString

public abstract class CommunicationItem implements Serializable {
    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCRCITM_SURROGATE_ID")
    @SequenceGenerator(name = "GCRCITM_SEQ_GEN", allocationSize = 1, sequenceName = "GCRCITM_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRCITM_SEQ_GEN")
    Long id

    /**
     *  The communication type of this item : EMAIL, LETTER, etc
     */
    @Column(name = "GCRCITM_COMM_CHANNEL")
    String communicationChannel

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCRCITM_CREATOR_ID")
    String createdBy

    /**
     *  Date record is created
     */
    @Column(name = "GCRCITM_CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date createDate

    /**
     * Date from which the item was sent.
     */
    @Column(name = "GCRCITM_SENT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date sentDate

    /** The target of the send item
     *
     */
    @Column(name = "GCRCITM_RECIPIENT_PIDM")
    Long recipientPidm;

    /**
     * The template associated with the template item
     */
    @Column(name = "GCRCITM_GCBTMPL_ID")
    Long templateId

    /**
     * The organization id
     */
    @Column(name = "GCRCITM_GCRORAN_ID")
    Long organizationId

    /**
     *  Optimistic lock token.
     */
    @Version
    @Column(name = "GCRCITM_VERSION")
    Long version

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCRCITM_USER_ID")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "GCRCITM_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *  Source system that created or updated the data.
     */
    @Column(name = "GCRCITM_DATA_ORIGIN")
    String dataOrigin

    static constraints = {

        communicationChannel(nullable: true)
        recipientPidm(nullable: true)
        sentDate(nullable: true)
        templateId(nullable: true)
        organizationId(nullable: true)
        createdBy(nullable: true)
        createDate(nullable: true)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }


}

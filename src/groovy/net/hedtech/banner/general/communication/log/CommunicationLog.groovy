
/*******************************************************************************
Copyright 2014 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.general.communication.log

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.*

/**
 * Communication Log. Record of individual communication item final status. entity.
 */
@Entity
@ToString
@EqualsAndHashCode
@Table(name = "GCBCLOG")

class CommunicationLog implements Serializable {

    /**
     * SURROGATE ID: Generated unique numeric identifier for this entity.
     */
    @Id
    @Column(name = "GCBCLOG_SURROGATE_ID")
    @SequenceGenerator(name = "GCBCLOG_SEQ_GEN", allocationSize = 1, sequenceName = "GCBCLOG_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCBCLOG_SEQ_GEN")
    Long id

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "GCBCLOG_VERSION")
    Integer version

    /**
     * ACTIVITY DATE: Date that record was created or last updated.
     */
    @Column(name = "GCBCLOG_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCBCLOG_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCBCLOG_DATA_ORIGIN")
    String dataOrigin

    /**
     * COMMUNICATION ITEM ID: The id of the communication item that was processed.
     */
    @Column(name = "GCBCLOG_COMMUNICATION_ITEM_ID")
    Long communicationItemId

    /**
     * COMMUNICATION CHANNEL: THe type of communication that was sent. EMAIL, LETTER etc.
     */
    @Column(name = "GCBCLOG_COMM_CHANNEL")
    String commChannel

    /**
     * CREATOR: The user id of the person who submitted the request to send this item.
     */
    @Column(name = "GCBCLOG_CREATOR_ID")
    String creatorId

    /**
     * ERROR TEXT: The complete error stack trace recorded when a send error is encountered.
     */
    @Column(name = "GCBCLOG_ERROR_TEXT")
    String errorText

    /**
     * ORGANIZATION NAME: The name of the organization on whose behalf this communication was sent.
     */
    @Column(name = "GCBCLOG_ORGANIZATION_NAME")
    String organizationName

    /**
     * PIDM: The pidm of the person recieving the communication.
     */
    @Column(name = "GCBCLOG_PIDM")
    Long pidm

    /**
     * SEND ITEM REFERENCE ID: The id of the group send item that initiated this communication.
     */
    @Column(name = "GCBCLOG_SEND_ITEM_REFERENCE_ID")
    String sendItemReferenceId

    /**
     * STATUS: The final disposition of the communication send operation. SENT, ERROR.
     */
    @Column(name = "GCBCLOG_STATUS")
    String status

    /**
     * TEMPLATE NAME: The name of the communication template that was sent.
     */
    @Column(name = "GCBCLOG_TEMPLATE_NAME")
    String templateName


    static constraints = {
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        communicationItemId(nullable: true)
        commChannel(nullable: false, maxSize: 30)
        creatorId(nullable: false, maxSize: 30)
        errorText(nullable: true)
        organizationName(nullable: true, maxSize: 1020)
        pidm(nullable: false)
        sendItemReferenceId(nullable: true, maxSize: 255)
        status(nullable: false, maxSize: 30)
        templateName(nullable: true, maxSize: 2048)
    }


    // Read Only fields that should be protected against update
    public static readonlyProperties = [ 'id' ]

}

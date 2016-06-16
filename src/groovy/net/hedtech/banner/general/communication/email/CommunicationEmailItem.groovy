/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.email

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.item.CommunicationItem

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.PrimaryKeyJoinColumn
import javax.persistence.Table

/**
 * CommunicationItem.
 * Defines the attributes for an email item
 */
@Entity
@Table(name = "GCREITM")
@PrimaryKeyJoinColumn(name = "GCREITM_SURROGATE_ID")
@EqualsAndHashCode
@ToString
class CommunicationEmailItem extends CommunicationItem implements Serializable {

    /**
     * The BCC (blind carbon copy) attribute of an email message with placeholders in raw format.
     */
    @Column(name = "GCREITM_BCCLIST")
    String bccList

    /**
     * The CC (carbon copy) attribute of an email message with placeholders in raw format.
     */
    @Column(name = "GCREITM_CCLIST")
    String ccList

    /**
     * The message body of the email with placeholders in raw format.
     */
    @Column(name = "GCREITM_CONTENT")
    String content

    /**
     * The FROM attribute of an email message with placeholders in raw format.
     */
    @Column(name = "GCREITM_FROMLIST")
    String fromList

    /**
     * The SUBJECT attribute of an email message with placeholders in raw format.
     */
    @Column(name = "GCREITM_SUBJECT")
    String subject

    /**
     * The TO attribute of an email message with placeholders in raw format.
     */
    @Column(name = "GCREITM_TOLIST")
    String toList

    /**
     * The sender of the email
     */
    @Column(name = "GCREITM_SENDER")
    String sender

    /**
     * The reply to in this email
     */
    @Column(name = "GCREITM_REPLYTO")
    String replyTo


    static constraints = {
        bccList(nullable: true, maxSize: 1020)
        ccList(nullable: true, maxSize: 1020)
        content(nullable: true)
        fromList(nullable: true, maxSize: 1020)
        subject(nullable: true, maxSize: 1020)
        toList(nullable: true, maxSize: 1020)
        sender(nullable: true)
        replyTo(nullable: true)
    }
}

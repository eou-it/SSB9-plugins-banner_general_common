/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.template

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.item.CommunicationChannel

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.PrimaryKeyJoinColumn
import javax.persistence.Table

@Entity
@Table(name = "GCBEMTL")
@PrimaryKeyJoinColumn(name = "GCBEMTL_SURROGATE_ID")
@EqualsAndHashCode
@ToString
class CommunicationEmailTemplate extends CommunicationTemplate implements Serializable {


    /**
     * The BCC (blind carbon copy) attribute of an email message with placeholders in raw format.
     */
    @Column(name = "GCBEMTL_BCCLIST")
    String bccList

    /**
     * The CC (carbon copy) attribute of an email message with placeholders in raw format.
     */
    @Column(name = "GCBEMTL_CCLIST")
    String ccList

    /**
     * The message body of the email with placeholders in raw format.
     */
    @Column(name = "GCBEMTL_CONTENT")
    String content

    /**
     * The FROM attribute of an email message with placeholders in raw format.
     */
    @Column(name = "GCBEMTL_FROMLIST")
    String fromList

    /**
     * The SUBJECT attribute of an email message with placeholders in raw format.
     */
    @Column(name = "GCBEMTL_SUBJECT")
    String subject

    /**
     * The TO attribute of an email message with placeholders in raw format.
     */
    @Column(name = "GCBEMTL_TOLIST")
    String toList

    static constraints = {
        bccList(nullable: true, maxSize: 1020)
        ccList(nullable: true, maxSize: 1020)
        content(nullable: true)
        fromList(nullable: true, maxSize: 1020)
        subject(nullable: true, maxSize: 1020)
        toList(nullable: true, maxSize: 1020)

    }

    @Override
    CommunicationChannel getCommunicationChannel() {
        return CommunicationChannel.EMAIL
    }

    @Override
    final CommunicationTemplateVisitor accept(CommunicationTemplateVisitor visitor) {
        visitor.visitEmail( this )
    }
}

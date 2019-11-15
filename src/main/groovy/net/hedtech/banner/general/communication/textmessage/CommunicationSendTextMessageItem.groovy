/*********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.textmessage

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.item.CommunicationSendItem
import net.hedtech.banner.general.communication.item.CommunicationSendItemVisitor

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.PrimaryKeyJoinColumn
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType

/**
 * CommunicationSendTextMessageItem.
 * Defines the attributes for a text message item
 */
@Entity
@Table(name = "GCRSTTM")
@PrimaryKeyJoinColumn(name = "GCRSTTM_SURROGATE_ID")
@EqualsAndHashCode
@ToString
class CommunicationSendTextMessageItem extends CommunicationSendItem implements Serializable {

    /**
     * The list of Banner IDs to whom the text message communication needs to be send.
     */
    @Column(name = "GCRSTTM_TOLIST")
    String toList

    /**
     * The message body of the text message with placeholders in raw format.
     */
    @Column(name = "GCRSTTM_CONTENT")
    String content

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCRSTTM_USER_ID")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "GCRSTTM_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *  Source system that created or updated the data.
     */
    @Column(name = "GCRSTTM_DATA_ORIGIN")
    String dataOrigin

    @Column(name = "GCRSTTM_VPDI_CODE")
    String mepCode

    static constraints = {
        toList(nullable: false, maxSize: 1020)
        content(nullable: false)
//        lastModified(nullable:false)
//        lastModifiedBy(nullable:false)
        dataOrigin(nullable:true)
        mepCode(nullable:true)
    }

    @Override
    final CommunicationSendItemVisitor accept(CommunicationSendItemVisitor visitor) {
        visitor.visitTextMessage( this )
    }
}

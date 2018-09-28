/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.textmessage

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.item.CommunicationItem

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.PrimaryKeyJoinColumn
import javax.persistence.Table

/**
 * Communication log entry for a personalized text message send to a specific recipient.
 */
@Entity
@Table(name = "GCRTITM")
@PrimaryKeyJoinColumn(name = "GCRTITM_SURROGATE_ID")
@EqualsAndHashCode
@ToString
class CommunicationTextMessageItem extends CommunicationItem implements Serializable {

    @Column(name = "GCRTITM_RESPONSE", nullable = true)
    String serverResponse

    static constraints = {
        serverResponse(nullable: true)
    }
}

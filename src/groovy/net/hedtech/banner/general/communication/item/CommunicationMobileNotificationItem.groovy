/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.item

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.PrimaryKeyJoinColumn
import javax.persistence.Table

/**
 * Communication log entry for a personalized mobile notification send to a specific recipient.
 */
@Entity
@Table(name = "GCRMITM")
@PrimaryKeyJoinColumn(name = "GCRMITM_SURROGATE_ID")
@EqualsAndHashCode
@ToString
class CommunicationMobileNotificationItem extends CommunicationItem implements Serializable {

    @Column(name = "GCRMITM_RESPONSE", nullable = true)
    String serverResponse

    static constraints = {
        serverResponse(nullable: true)
    }
}

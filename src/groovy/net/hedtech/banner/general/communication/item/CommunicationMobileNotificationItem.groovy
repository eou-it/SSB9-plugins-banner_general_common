/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.item

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.template.CommunicationMobileNotificationExpirationPolicy

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
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

    @Column(name = "GCRMITM_MOBILE_HEADLINE", nullable = false)
    String mobileHeadline

    @Column(name = "GCRMITM_HEADLINE")
    String headline

    @Column(name = "GCRMITM_DESCRIPTION")
    String messageDescription

    @Column(name = "GCRMITM_DESTINATION_LINK")
    String destinationLink

    @Column(name = "GCRMITM_DESTINATION_LABEL")
    String destinationLabel

    static constraints = {
        mobileHeadline(nullable: false, maxSize: 160)
        headline(nullable: true, maxSize: 255)
        messageDescription(nullable: true, maxSize: 4000)
        destinationLink(nullable: true, maxSize: 2048)
        destinationLabel(nullable: true, maxSize: 255)
    }
}

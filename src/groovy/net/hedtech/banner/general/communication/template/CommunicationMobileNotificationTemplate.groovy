/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.template

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.item.CommunicationChannel
import org.hibernate.annotations.Type

import javax.persistence.*

@Entity
@Table(name = "GCBMNTL")
@PrimaryKeyJoinColumn(name = "GCBMNTL_SURROGATE_ID")
@EqualsAndHashCode
@ToString
class CommunicationMobileNotificationTemplate extends CommunicationTemplate implements Serializable {

    @Column(name = "GCBMNTL_MOBILE_HEADLINE", nullable = false)
    String mobileHeadline

    @Column(name = "GCBMNTL_HEADLINE", nullable = true)
    String headline

    @Column(name = "GCBMNTL_DESCRIPTION", nullable = true)
    String description

    @Column(name = "GCBMNTL_DESTINATION_LINK", nullable = true)
    String destinationLink

    @Column(name = "GCBMNTL_DESTINATION_LABEL", nullable = true)
    String destinationLabel

    @Column(name = "GCBMNTL_EXPIRATION_POLICY", nullable = false)
    @Enumerated(EnumType.STRING)
    CommunicationMobileNotificationExpirationPolicy expirationPolicy = CommunicationMobileNotificationExpirationPolicy.NO_EXPIRATION

    @Column(name = "GCBMNTL_ELAPSED_TIME_SECS", nullable = true)
    Long elapsedTimeSeconds

    @Column(name = "GCBMNTL_EXPIRATION_DATE_TIME", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    Date expirationDateTime

    @Column(name = "GCBMNTL_PUSH", nullable = false)
    @Type(type="yes_no")
    boolean push = false

    @Column(name = "GCBMNTL_STICKY", nullable = false)
    @Type(type="yes_no")
    boolean sticky = false

    static constraints = {
        mobileHeadline(nullable: true)
        headline(nullable: true)
        description(nullable: true)
        destinationLink(nullable: true)
        destinationLabel(nullable: true)
        expirationPolicy(nullable: false)
        elapsedTimeSeconds(nullable: true)
        expirationDateTime(nullable: true)
        push(nullable: false)
        sticky(nullable: false)
    }

    @Override
    CommunicationChannel getCommunicationChannel() {
        return CommunicationChannel.MOBILE_NOTIFICATION
    }

    @Override
    final CommunicationTemplateVisitor accept(CommunicationTemplateVisitor visitor) {
        visitor.visitMobileNotification( this )
    }
}

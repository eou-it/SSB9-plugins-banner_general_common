/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.mobile

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.general.communication.template.CommunicationDurationUnit
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplateVisitor
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
    String messageDescription

    @Column(name = "GCBMNTL_DESTINATION_LINK", nullable = true)
    String destinationLink

    @Column(name = "GCBMNTL_DESTINATION_LABEL", nullable = true)
    String destinationLabel

    @Column(name = "GCBMNTL_EXPIRATION_POLICY", nullable = false)
    @Enumerated(EnumType.STRING)
    CommunicationMobileNotificationExpirationPolicy expirationPolicy = CommunicationMobileNotificationExpirationPolicy.NO_EXPIRATION

    @Column(name = "GCBMNTL_DURATION", nullable = true)
    Integer duration

    @Column(name = "GCBMNTL_DURATION_UNIT", nullable = true)
    @Enumerated(EnumType.STRING)
    CommunicationDurationUnit durationUnit = CommunicationDurationUnit.DAY

    @Column(name = "GCBMNTL_EXPIRATION_DATE_TIME", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    Date expirationDateTime

    @Column(name = "GCBMNTL_PUSH", nullable = false)
    @Type(type="yes_no")
    boolean push = false

    @Column(name = "GCBMNTL_STICKY", nullable = false)
    @Type(type="yes_no")
    boolean sticky = false

    @Transient
    Long parentVersion

    public CommunicationMobileNotificationTemplate() {
        super( CommunicationChannel.MOBILE_NOTIFICATION )
    }

    static constraints = {
        mobileHeadline(nullable: true)
        headline(nullable: true)
        messageDescription(nullable: true)
        destinationLink(nullable: true)
        destinationLabel(nullable: true)
        expirationPolicy(nullable: false)
        duration(nullable: true, min: 0)
        durationUnit(nullable: false)
        expirationDateTime(nullable: true)
        push(nullable: false)
        sticky(nullable: false)
    }

    Long getParentVersion() {
        return super.getVersion()
    }

    @Override
    final CommunicationTemplateVisitor accept(CommunicationTemplateVisitor visitor) {
        visitor.visitMobileNotification( this )
    }
}

/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.template

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.*

@Entity
@Table(name = "GCBMNTL")
@PrimaryKeyJoinColumn(name = "GCBMNTL_SURROGATE_ID")
@EqualsAndHashCode
@ToString
class CommunicationMobileNotificationTemplate extends CommunicationTemplate implements Serializable {

    @Column(name = "GCBMNTL_MOBILE_HEADLINE", nullable = false)
    String mobileHeadline // 250 max

    @Column(name = "GCBMNTL_HEADLINE", nullable = true)
    String headline // 250 max

    @Column(name = "GCBMNTL_BODY", nullable = true)
    String body // 2500 max

    @Column(name = "GCBMNTL_DESTINATION_LINK", nullable = true)
    String destinationLink // 250 max

    @Column(name = "GCBMNTL_DESTINATION_LABEL", nullable = true)
    String destinationLabel // 250 max

    @Column(name = "GCBMNTL_EXPIRATION_POLICY", nullable = false)
    String expirationPolicy // enum NO_EXPIRATION, ELAPSED_TIME, DATE_TIME // varchar(64)

    @Column(name = "GCBMNTL_ELAPSED_TIME_SECS", nullable = true)
    Long elapsedTimeSeconds

    @Column(name = "GCBMNTL_EXPIRATION_DATE_TIME", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    Date expirationDateTime

    @Column(name = "GCBMNTL_PUSH", nullable = false)
    @Type(type="yes_no")
    boolean push

    @Column(name = "GCBMNTL_STICKY", nullable = false)
    @Type(type="yes_no")
    boolean sticky

    static constraints = {
        mobileHeadline(nullable: false, maxSize: 250)
        headline(nullable: true, maxSize: 250)
        body(nullable: true, maxSize: 2500)
        destinationLink(nullable: true, maxSize: 250)
        destinationLabel(nullable: true, maxSize: 250)
        expirationPolicy(nullable: true)
        elapsedTimeSeconds(nullable: true)
        expirationDateTime(nullable: true)
        push(nullable: false)
        sticky(nullable: false)
    }
}

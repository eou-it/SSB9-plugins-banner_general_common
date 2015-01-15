package net.hedtech.banner.general.communication.organization

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/*******************************************************************************
Copyright 2014 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

import javax.persistence.*

/**
 * Email server protocol properties. Defines the connection information for the email server. entity.
 */
@Entity
@ToString
@EqualsAndHashCode
@Table(name = "GCBSPRP")
// @NamedQueries(value = [
// @NamedQuery(name = "CommunicationEmailServerProperties.fetchByxxxxx",
//             query = """ FROM CommunicationEmailServerProperties a WHERE xxxxx """)
// ])
class CommunicationEmailServerProperties implements Serializable {

    /**
     * SURROGATE ID: Generated unique numeric identifier for this entity.
     */
    @Id
    @Column(name = "GCBSPRP_SURROGATE_ID")
    @SequenceGenerator(name = "GCBSPRP_SEQ_GEN", allocationSize = 1, sequenceName = "GCBSPRP_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCBSPRP_SEQ_GEN")
    Long id

    /**
     * SMTP HOST: The host name for the SMTP mail server to send email to
     */
    @Column(name = "GCBSPRP_SMTP_HOST")
    String smtpHost

    /**
     * SMTP PORT: The port number on the host send email to
     */
    @Column(name = "GCBSPRP_SMTP_PORT")
    String smtpPort

    /**
     * SMTP HOST: The SMTP protocal to use. none, ssl, etc.
     */
    @Column(name = "GCBSPRP_SECURITY_PROTOCOL")
    String securityProtocol

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "GCBSPRP_VERSION")
    Integer version

    /**
     * ACTIVITY DATE: Date that record was created or last updated.
     */
    @Column(name = "GCBSPRP_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCBSPRP_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCBSPRP_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        securityProtocol(nullable: true, maxSize: 2000)
        smtpHost(nullable: true, maxSize: 2000)
        smtpPort(nullable: true, maxSize: 2000)
    }


    // Read Only fields that should be protected against update
    public static readonlyProperties = [ 'id' ]

}

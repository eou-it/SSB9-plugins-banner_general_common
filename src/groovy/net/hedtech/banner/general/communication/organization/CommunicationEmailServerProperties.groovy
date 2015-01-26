package net.hedtech.banner.general.communication.organization

import groovy.transform.EqualsAndHashCode

import javax.persistence.*

/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
/**
 * Email server protocol properties. Defines the connection information for the email server. entity.
 */
@Entity

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
     * Type: The type of email server properties, Send,Receive
     */
    @Column(name = "GCBSPRP_TYPE")
    @Enumerated(value = EnumType.STRING)
    CommunicationEmailServerPropertiesType type
    /**
     * HOST: The host name for the mail server to send email to
     */
    @Column(name = "GCBSPRP_SMTP_HOST")
    String host

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "GCBSPRP_ORGANIZATION_ID")
    CommunicationOrganization organization

    /**
     * PORT: The port number on the host send email to
     */
    @Column(name = "GCBSPRP_SMTP_PORT")
    int port

    /**
     * SMTP HOST: The SMTP protocal to use. none, ssl, etc.
     */
    @Column(name = "GCBSPRP_SECURITY_PROTOCOL")
    @Enumerated(value = EnumType.STRING)
    CommunicationEmailServerConnectionSecurity securityProtocol = CommunicationEmailServerConnectionSecurity.None

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
        lastModified( nullable: true )
        lastModifiedBy( nullable: true, maxSize: 30 )
        dataOrigin( nullable: true, maxSize: 30 )
        securityProtocol( nullable: false, maxSize: 2000 )
        host( nullable: false, maxSize: 2000 )
        port( nullable: false )
        type( nullable: false )
        organization( nullable: false )
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']

    /*
     Cannot use the @ToString annotation because it include an Organization reference and causes an infinite loop
     */


    @Override
    public String toString() {
        return "CommunicationEmailServerProperties{" +
                "id=" + id +
                ", type=" + type +
                ", smtpHost='" + host + '\'' +
                ", organization=" + organization.id + "**"+organization.name +
                ", smtpPort=" + port +
                ", securityProtocol='" + securityProtocol + '\'' +
                ", version=" + version +
                ", lastModified=" + lastModified +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", dataOrigin='" + dataOrigin + '\'' +
                '}';
    }
}

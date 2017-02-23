package net.hedtech.banner.general.communication.organization

import groovy.json.JsonSlurper
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.*

/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
/**
 * Email server protocol properties. Defines the connection information for the email server. entity.
 */
@Entity
@EqualsAndHashCode
@ToString
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
     * HOST: The host name for the mail server
     */
    @Column(name = "GCBSPRP_HOST")
    String host

    /**
     * PORT: The port number on the host
     */
    @Column(name = "GCBSPRP_PORT")
    int port

    /**
     * SMTP HOST: The SMTP protocal to use. none, ssl, etc.
     */
    @Column(name = "GCBSPRP_SECURITY_PROTOCOL")
    @Enumerated(value = EnumType.STRING)
    CommunicationEmailServerConnectionSecurity securityProtocol = CommunicationEmailServerConnectionSecurity.None

    /**
     * The set of all applicable smtp properties relevant to this server.
     */
    @Lob
    @Column(name = "GCBSPRP_SMTP_PROPERTIES")
    String smtpProperties

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
        smtpProperties(nullable:true)
        port( nullable: false )
        type( nullable: false )
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']

    public Map getSmtpPropertiesAsMap() {
        def jsonSlurper = new JsonSlurper()
        return (smtpProperties ? jsonSlurper.parseText(this.smtpProperties) : null)
    }

    public void setSmtpProperties(Map smtpProp) {
        def mapToString = {
            it.collect { /"$it.key":$it.value/ } join " "
        }
        this.smtpProperties = smtpProp ? "{"+mapToString(smtpProp)+"}" : null
    }
}

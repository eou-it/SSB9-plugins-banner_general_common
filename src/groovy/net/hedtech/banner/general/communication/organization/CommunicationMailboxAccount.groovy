/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.organization

import groovy.transform.EqualsAndHashCode

import javax.persistence.*

/**
 * Communication Mailbox Account Table. A mailbox account is a named object that contains the email address and the credentials of an account on the email system that BCM is linked to for sending and receving email messages. entity.
 */
@Entity
@EqualsAndHashCode
@Table(name = "GCRMBAC")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationMailboxAccount.fetchByOrganizationId",
                query = """ FROM CommunicationMailboxAccount a WHERE organization.id = :organizationId """)
])
class CommunicationMailboxAccount implements Serializable {

    /**
     * SURROGATE ID: Immutable unique key.
     */
    @Id
    @Column(name = "GCRMBAC_SURROGATE_ID")
    @SequenceGenerator(name = "GCRMBAC_SEQ_GEN", allocationSize = 1, sequenceName = "GCRMBAC_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRMBAC_SEQ_GEN")
    Long id

    /**
     * EMAILADDRESS: Email address of the mailbox account.
     */
    @Column(name = "GCRMBAC_EMAIL_ADDRESS")
    String emailAddress

    @ManyToOne
    @JoinColumn(name = "GCRMBAC_ORGANIZATION_ID")
    CommunicationOrganization organization

    /**
     * Clear text password
     */
    @Transient
    String clearTextPassword

    /**
     * PASSWORD: Encrypted password of this mailbox account.
     */
    @Column(name = "GCRMBAC_ENCRYPTED_PASSWORD")
    String encryptedPassword

    /**
     * TYPE: Type of mailbox account. Valid values are Sender and ReplyTo. MailboxAccount instances must be of a specific type; the type tells the monitoring system how to treat email received by the account. (For example, parse as bounce-backs, or parse as normal replies.)
     */
    @Column(name = "GCRMBAC_TYPE")
    @Enumerated(value = EnumType.STRING)
    CommunicationMailboxAccountType type

    /**
     * The user friendly name of the return email address.
     */
    @Column(name = "GCRMBAC_EMAIL_DISPLAY_NAME")
    String emailDisplayName

    /**
     * USERNAME: Username used to logon to the mail server.
     */
    @Column(name = "GCRMBAC_USERNAME")
    String userName

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "GCRMBAC_VERSION")
    Long version

    /**
     * ACTIVITY DATE: Date that record was created or last updated.
     */
    @Column(name = "GCRMBAC_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCRMBAC_USER_ID")
    String lastModifiedBy


 /**

     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCRMBAC_DATA_ORIGIN")
    String dataOrigin



    static constraints = {
        lastModified( nullable: true )
        lastModifiedBy( nullable: true, maxSize: 30 )
        dataOrigin( nullable: true, maxSize: 30 )
        emailAddress( nullable: false, maxSize: 1020 )
        organization( nullable: false, maxSize: 1020 )
        encryptedPassword( nullable: false )
        type( nullable: false, maxSize: 200 )
        userName( nullable: false, maxSize: 1020 )
        emailDisplayName( nullable: true )

        clearTextPassword( nullable: true )
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']

    public static List<CommunicationMailboxAccount> fetchByOrganizationId( Long organizationId ) {
        def mailboxAccountList
        CommunicationMailboxAccount.withSession { session ->
            mailboxAccountList = session.getNamedQuery( 'CommunicationMailboxAccount.fetchByOrganizationId' ).setLong( 'organizationId', organizationId ).list()
        }
        return mailboxAccountList
    }

    /*
    Cannot use the @ToString annotation because it include an Organization reference and causes an infinite loop
    */
    @Override
    public String toString() {
        return "CommunicationMailboxAccount{" +
                "id=" + id +
                ", emailAddress='" + emailAddress + '\'' +
                ", organization=" + organization.id +
                ", clearTextPassword='" + clearTextPassword + '\'' +
                ", encryptedPassword='" + encryptedPassword + '\'' +
                ", type=" + type +
                ", emailDisplayName='" + emailDisplayName + '\'' +
                ", userName='" + userName + '\'' +
                ", version=" + version +
                ", lastModified=" + lastModified +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", dataOrigin='" + dataOrigin + '\'' +
                '}';
    }
}

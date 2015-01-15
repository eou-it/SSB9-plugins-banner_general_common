/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.organization

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.*
import java.sql.Blob

/**
 * Communication Mailbox Account Table. A mailbox account is a named object that contains the email address and the credentials of an account on the email system that BCM is linked to for sending and receving email messages. entity.
 */
@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GCRMBAC")
// @NamedQueries(value = [
// @NamedQuery(name = "CommunicationMailboxAccount.fetchByxxxxx",
//             query = """ FROM CommunicationMailboxAccount a WHERE xxxxx """)
// ])
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

    /**
     * NAME: Name of the mailbox account.
     */
    @Column(name = "GCRMBAC_NAME")
    String name

    /**
     * PASSWORD: Encrypted password of this mailbox account.
     */
    @Column(name = "GCRMBAC_PASSWORD")
    Blob password

    /**
     * TYPE: Type of mailbox account. Valid values are Sender and ReplyTo. MailboxAccount instances must be of a specific type; the type tells the monitoring system how to treat email received by the account. (For example, parse as bounce-backs, or parse as normal replies.)
     */
    @Column(name = "GCRMBAC_TYPE")
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
        name( nullable: false, maxSize: 1020 )
        password( nullable: true )
        type( nullable: false, maxSize: 200 )
        userName( nullable: false, maxSize: 1020 )
        emailDisplayName( nullable: true )
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']

}

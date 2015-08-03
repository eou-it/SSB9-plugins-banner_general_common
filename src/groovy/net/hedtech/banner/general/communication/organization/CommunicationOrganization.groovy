/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.organization

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hibernate.annotations.Type
import org.hibernate.annotations.Where

import javax.persistence.*

/**
 * Organization is the internal department or group that owns the communication template, and holds the return
 * address information for emails
 *
 */
@Entity
@Table(name = "GCRORAN")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationOrganization.fetchById",
                query = """ FROM CommunicationOrganization a
                    WHERE a.id = :id"""),
        @NamedQuery(name = "CommunicationOrganization.fetchByName",
                query = """ FROM CommunicationOrganization a
                    WHERE a.name = :name"""),
        @NamedQuery(name = "CommunicationOrganization.fetchActive",
                query = """ FROM CommunicationOrganization a
                    WHERE a.isActive = true"""),
        @NamedQuery(name = "CommunicationOrganization.fetchRoot",
                query = """ FROM CommunicationOrganization a
                    WHERE a.isRoot = true"""),
])
class CommunicationOrganization implements Serializable {
    /**
     * Generated unique key.
     */
    @Id
    @Column(name = "GCRORAN_SURROGATE_ID")
    @SequenceGenerator(name = "GCRORAN_SEQ_GEN", allocationSize = 1, sequenceName = "GCRORAN_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRORAN_SEQ_GEN")
    Long id

    /**
     * Name of the organization.
     */
    @Column(name = "GCRORAN_NAME")
    String name

    /**
     * Indicates if the organization is the root organization (1=Yes or 0=No). Only one organization can be identified as the root organization.
     */
    @Type(type = "yes_no")
    @Column(name = "GCRORAN_IS_ROOT")
    Boolean isRoot = false

    @Type(type = "yes_no")
    @Column(name = "GCRORAN_ACTIVE_IND")
    Boolean isActive = false

    /**
     * Foreign key reference to the parent Organization (REL_ORGANIZATION) of which this organization
     is associated. Organizations are hierarchical in nature, thus any child organization is able to return
     * its parent. Hierarchies are created by associating a parent to a child, not the other way around, therefore child
     * organizations are read-only.
     */
    @Column(name = "GCRORAN_PARENT_ID")
    Long parent

    /**
     * Description of the organization.
     */
    @Column(name = "GCRORAN_DESCRIPTION")
    String description

    @Column(name = "GCRORAN_DATE_FORMAT")
    String dateFormat

    @Column(name = "GCRORAN_DAYOFWEEK_FORMAT")
    String dayOfWeekFormat

    @Column(name = "GCRORAN_TIMEOFDAY_FORMAT")
    String timeOfDayFormat

    /**
     * USER ID: The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCRORAN_USER_ID")
    String lastModifiedBy

    /**
     * Date that record was created or last updated.
     */
    @Column(name = "GCRORAN_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * Optimistic lock token.
     */
    @Version
    @Column(name = "GCRORAN_VERSION")
    Long version

    /**
     * Source system that created or updated the data.
     */
    @Column(name = "GCRORAN_DATA_ORIGIN")
    String dataOrigin

    /**
     * The send email server configuration properties
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
    @Where(clause = " GCBSPRP_TYPE = 'Send'")
    List<CommunicationEmailServerProperties> sendEmailServerProperties

    /**
     * The send email server configuration properties
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
    @Where(clause = "GCBSPRP_TYPE = 'Receive'")
    List<CommunicationEmailServerProperties> receiveEmailServerProperties

    /**
     * The sender email mailbox properties
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
    @Where(clause = "GCRMBAC_TYPE = 'Sender'")
    List<CommunicationMailboxAccount> senderMailboxAccountSettings

    /**
     * The replyTo email mailbox properties
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
    @Where(clause = "GCRMBAC_TYPE = 'ReplyTo'")
    List<CommunicationMailboxAccount> replyToMailboxAccountSettings

    // Individual accessors. For now qualified with a 'the' in order to avoid conflicts with
    // the hibernate collection counterparts above. We can only ever have one of each of these.
    // Will try to remap the above or at least rename them so we can have the desired method access.
    // These methods will also populate the organization as it makes sense.

    public CommunicationEmailServerProperties getTheSendEmailServerProperties() {
        CommunicationEmailServerProperties settings = null
        if (sendEmailServerProperties && sendEmailServerProperties.size() > 0) {
            settings = sendEmailServerProperties.get( 0 )
        }
        return settings
    }

    public void setTheSendEmailServerProperties( CommunicationEmailServerProperties settings ) {
        if (settings) {
            sendEmailServerProperties = [ settings ]
            settings.organization = this
            settings.type = CommunicationEmailServerPropertiesType.Send
        } else {
            sendEmailServerProperties = null
        }
    }

    public CommunicationEmailServerProperties getTheReceiveEmailServerProperties() {
        CommunicationEmailServerProperties settings = null
        if (receiveEmailServerProperties && receiveEmailServerProperties.size() > 0) {
            settings = receiveEmailServerProperties.get( 0 )
        }
        return settings
    }

    public void setTheReceiveEmailServerProperties( CommunicationEmailServerProperties settings ) {
        if (settings) {
            receiveEmailServerProperties = [ settings ]
            settings.organization = this
            settings.type = CommunicationEmailServerPropertiesType.Receive
        } else {
            receiveEmailServerProperties = null
        }
    }

    public CommunicationMailboxAccount getTheSenderMailboxAccount() {
        CommunicationMailboxAccount settings = null
        if (senderMailboxAccountSettings && senderMailboxAccountSettings.size() > 0) {
            settings = senderMailboxAccountSettings.get( 0 )
        }
        return settings
    }

    public void setTheSenderMailboxAccount( CommunicationMailboxAccount settings ) {
        if (settings) {
            senderMailboxAccountSettings = [ settings ]
            settings.organization = this
            settings.type = CommunicationMailboxAccountType.Sender
        } else {
            senderMailboxAccountSettings = null
        }
    }

    public CommunicationMailboxAccount getTheReplyToMailboxAccount() {
        CommunicationMailboxAccount settings = null
        if (replyToMailboxAccountSettings && replyToMailboxAccountSettings.size() > 0) {
            settings = replyToMailboxAccountSettings.get( 0 )
        }
        return settings
    }

    public void setTheReplyToMailboxAccount( CommunicationMailboxAccount settings ) {
        if (settings) {
            replyToMailboxAccountSettings = [ settings ]
            settings.organization = this
            settings.type = CommunicationMailboxAccountType.ReplyTo
        } else {
            replyToMailboxAccountSettings = null
        }
    }


    static constraints = {
        name( nullable: false, maxSize: 1020 )
        description( nullable: true, maxSize: 2000 )
        parent( nullable: true )
        isRoot( nullable: true )
        isActive( nullable: true )
        dateFormat( nullable: true )
        dayOfWeekFormat( nullable: true )
        timeOfDayFormat( nullable: true )
        lastModified( nullable: true )
        lastModifiedBy( nullable: true, maxSize: 30 )
        dataOrigin( nullable: true, maxSize: 30 )
        receiveEmailServerProperties( nullable: true )
        sendEmailServerProperties( nullable: true )
        replyToMailboxAccountSettings( nullable: true )
        senderMailboxAccountSettings( nullable: true )

    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof CommunicationOrganization)) return false

        CommunicationOrganization that = (CommunicationOrganization) o

        if (dataOrigin != that.dataOrigin) return false
        if (dateFormat != that.dateFormat) return false
        if (dayOfWeekFormat != that.dayOfWeekFormat) return false
        if (description != that.description) return false
        if (id != that.id) return false
        if (isRoot != that.isRoot) return false
        if (isActive != that.isActive) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (name != that.name) return false
        if (parent != that.parent) return false
        if (!receiveEmailServerProperties.equals(that.receiveEmailServerProperties)) return false
        if (!replyToMailboxAccountSettings.equals(replyToMailboxAccountSettings)) return false
        if (!sendEmailServerProperties.equals(sendEmailServerProperties)) return false
        if (!senderMailboxAccountSettings.equals(senderMailboxAccountSettings)) return false
        if (timeOfDayFormat != that.timeOfDayFormat) return false
        if (version != that.version) return false

        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (name != null ? name.hashCode() : 0)
        result = 31 * result + (isRoot != null ? isRoot.hashCode() : 0)
        result = 31 * result + (isActive != null ? isActive.hashCode() : 0)
        result = 31 * result + (parent != null ? parent.hashCode() : 0)
        result = 31 * result + (description != null ? description.hashCode() : 0)
        result = 31 * result + (dateFormat != null ? dateFormat.hashCode() : 0)
        result = 31 * result + (dayOfWeekFormat != null ? dayOfWeekFormat.hashCode() : 0)
        result = 31 * result + (timeOfDayFormat != null ? timeOfDayFormat.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        return result
    }


    public static CommunicationOrganization fetchById( Long id ) {

        def query
        CommunicationOrganization.withSession { session ->
            query = session.getNamedQuery( 'CommunicationOrganization.fetchById' )
                    .setLong( 'id', id ).list()[0]

        }
        return query
    }


    public static CommunicationOrganization fetchByName( String name ) {
        def query
        CommunicationOrganization.withSession { session ->
            query = session.getNamedQuery( 'CommunicationOrganization.fetchByName' ).setString( 'name', name ).list()[0]
        }
        return query
    }

    public static List<CommunicationOrganization> fetchActive() {
        def query
        CommunicationOrganization.withSession { session ->
            query = session.getNamedQuery( 'CommunicationOrganization.fetchActive' ).list()
        }
        return query
    }

    public static CommunicationOrganization fetchRoot() {
        def query
        CommunicationOrganization.withSession { session ->
            query = session.getNamedQuery( 'CommunicationOrganization.fetchRoot' ).list()[0]
        }
        return query
    }

    @Override
    public String toString() {
        return "CommunicationOrganization{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isRoot=" + isRoot +
                ", isActive=" + isActive +
                ", parent=" + parent +
                ", description='" + description + '\'' +
                ", dateFormat='" + dateFormat + '\'' +
                ", dayOfWeekFormat='" + dayOfWeekFormat + '\'' +
                ", timeOfDayFormat='" + timeOfDayFormat + '\'' +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", lastModified=" + lastModified +
                ", version=" + version +
                ", dataOrigin='" + dataOrigin + '\'' +
                ", sendEmailServerProperties=" + sendEmailServerProperties +
                ", receiveEmailServerProperties=" + receiveEmailServerProperties +
                ", senderMailboxAccountSettings=" + senderMailboxAccountSettings +
                ", replyToMailboxAccountSettings=" + replyToMailboxAccountSettings +
                '}';
    }
}

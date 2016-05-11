/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.organization

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import org.hibernate.annotations.Type
import org.hibernate.annotations.Where
import org.hibernate.criterion.Order

import javax.persistence.*

/**
 * Organization is the internal department or group that owns the communication template, and holds the return
 * address information for emails
 *
 */
@Entity
@Table(name = "GCRORAN")
@EqualsAndHashCode
@ToString
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationOrganization.fetchById",
                query = """ FROM CommunicationOrganization a
                    WHERE a.id = :id"""),
        @NamedQuery(name = "CommunicationOrganization.fetchByName",
                query = """ FROM CommunicationOrganization a
                    WHERE a.name = :name"""),
        @NamedQuery(name = "CommunicationOrganization.fetchAvailable",
                query = """ FROM CommunicationOrganization a
                    WHERE a.isAvailable = true"""),
        @NamedQuery(name = "CommunicationOrganization.fetchRoot",
                query = """ FROM CommunicationOrganization a
                    WHERE a.parent is null"""),
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


    @Type(type = "yes_no")
    @Column(name = "GCRORAN_AVAILABLE_IND")
    Boolean isAvailable = false

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

    @Column(name = "GCRORAN_NOTFN_ENDPOINT_URL")
    String mobileEndPointUrl


    @Column(name = "GCRORAN_NOTFN_APPL_NAME")
    String mobileApplicationName


    @Column(name = "GCRORAN_NOTFN_APPL_KEY")
    String encryptedMobileApplicationKey

    /**
     * Clear text password
     */
    @Transient
    String clearMobileApplicationKey

    /**
     * The send email server configuration properties
     */
    @OneToOne
    @JoinColumn(name = "GCRORAN_SEND_EMAILPROP_ID", referencedColumnName = "GCBSPRP_SURROGATE_ID")
    CommunicationEmailServerProperties sendEmailServerProperties

    /**
     * The send email server configuration properties
     */
    @OneToOne
    @JoinColumn(name = "GCRORAN_RECEIVE_EMAILPROP_ID", referencedColumnName = "GCBSPRP_SURROGATE_ID")
    CommunicationEmailServerProperties receiveEmailServerProperties

    /**
     * The sender email mailbox properties
     */
    @OneToOne
    @JoinColumn(name = "GCRORAN_SEND_MAILBOX_ID", referencedColumnName = "GCRMBAC_SURROGATE_ID")
    CommunicationMailboxAccount senderMailboxAccount

    /**
     * The replyTo email mailbox properties
     */
    @OneToOne
    @JoinColumn(name = "GCRORAN_REPLY_MAILBOX_ID", referencedColumnName = "GCRMBAC_SURROGATE_ID")
    CommunicationMailboxAccount replyToMailboxAccount

    static constraints = {
        name(nullable: false, maxSize: 1020)
        description(nullable: true, maxSize: 2000)
        parent(nullable: true)
        isAvailable(nullable: true)
        dateFormat(nullable: true)
        dayOfWeekFormat(nullable: true)
        timeOfDayFormat(nullable: true)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        receiveEmailServerProperties(nullable: true)
        sendEmailServerProperties(nullable: true)
        replyToMailboxAccount(nullable: true)
        senderMailboxAccount(nullable: true)
        encryptedMobileApplicationKey(nullable: true)
        mobileApplicationName(nullable:true)
        mobileEndPointUrl(nullable:true)

    }

    public static CommunicationOrganization fetchById(Long id) {

        def query
        CommunicationOrganization.withSession { session ->
            query = session.getNamedQuery('CommunicationOrganization.fetchById')
                    .setLong('id', id).list()[0]

        }
        return query
    }

    public static CommunicationOrganization fetchByName(String name) {
        def query
        CommunicationOrganization.withSession { session ->
            query = session.getNamedQuery('CommunicationOrganization.fetchByName').setString('name', name).list()[0]
        }
        return query
    }

    public static List<CommunicationOrganization> fetchAvailable() {
        def query
        CommunicationOrganization.withSession { session ->
            query = session.getNamedQuery('CommunicationOrganization.fetchAvailable').list()
        }
        return query
    }

    public static CommunicationOrganization fetchRoot() {
        def query
        CommunicationOrganization.withSession { session ->
            query = session.getNamedQuery('CommunicationOrganization.fetchRoot').list()[0]
        }
        return query
    }

    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def ascdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'asc'

        def queryCriteria = CommunicationOrganization.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("name", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name))
            isNotNull("parent")
            order((ascdir ? Order.asc(pagingAndSortParams?.sortColumn) : Order.desc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }

}

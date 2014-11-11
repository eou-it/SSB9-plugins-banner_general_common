/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.organization

import groovy.transform.EqualsAndHashCode

import javax.persistence.*

/**
 * Organization is the internal department or group that owns the communication template, and holds the return
 * address information for emails
 *
 */
@Entity
@Table(name = "GCRORAN")
@EqualsAndHashCode
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationOrganization.fetchById",
                query = """ FROM CommunicationOrganization a
                    WHERE a.id = :id"""),
        @NamedQuery(name = "CommunicationOrganization.fetchByName",
                query = """ FROM CommunicationOrganization a
                    WHERE a.name = :name""")
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
    @Column(name = "GCRORAN_IS_ROOT")
    Boolean isRoot

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


    static constraints = {
        name(nullable: false, maxSize: 1020)
        description(nullable: true, maxSize: 2000)
        parent(nullable: true)
        isRoot(nullable: true)
        dateFormat(nullable: true)
        dayOfWeekFormat(nullable: true)
        timeOfDayFormat(nullable: true)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }


    @Override
    public String toString() {
        return "Organization{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isRoot=" + isRoot +
                ", parent=" + parent +
                ", description='" + description + '\'' +
                ", dateFormat='" + dateFormat + '\'' +
                ", dayOfWeekFormat='" + dayOfWeekFormat + '\'' +
                ", timeOfDayFormat='" + timeOfDayFormat + '\'' +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", lastModified=" + lastModified +
                ", version=" + version +
                ", dataOrigin='" + dataOrigin + '\'' +
                '}';
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
}

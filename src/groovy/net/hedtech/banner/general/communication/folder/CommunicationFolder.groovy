/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.folder

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import org.hibernate.criterion.Order

import javax.persistence.*

/**
 * Oraganizing object for communication templates
 *
 */
@Entity
@Table(name = "GCRFLDR")
@EqualsAndHashCode
@ToString
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationFolder.fetchById",
                query = """ FROM CommunicationFolder a
                    WHERE a.id = :id"""),
        @NamedQuery(name = "CommunicationFolder.fetchByName",
                query = """ FROM CommunicationFolder a
                    WHERE upper(a.name) = upper(:name)"""),
        @NamedQuery(name = "CommunicationFolder.existsAnotherSameNameFolder",
                query = """ FROM CommunicationFolder a
                    WHERE upper(a.name) = upper(:folderName)
                    AND   a.id <> :id"""),
        @NamedQuery(name = "CommunicationFolder.fetchFoldersWithActiveTemplates",
                query = """ FROM CommunicationFolder a
                    WHERE exists (select b.id from CommunicationEmailTemplate b
                                  where b.folder.id = a.id
                                  AND b.published = 'Y'
                                  AND SYSDATE between NVL(b.validFrom,SYSDATE) and NVL(b.validTo, SYSDATE)
                                  AND (b.personal = 'N' or lower(b.createdBy) = lower(:currentuser)))"""),
        @NamedQuery(name = "CommunicationFolder.fetchFoldersWithPublishedDatafields",
                query = """ FROM CommunicationFolder a
                    WHERE exists (select b.id from CommunicationField b
                                  where b.folder.id = a.id
                                  AND lower(b.status) = 'production'
                                  )""")
])
class CommunicationFolder implements Serializable {
    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCRFLDR_SURROGATE_ID")
    @SequenceGenerator(name = "GCRFLDR_SEQ_GEN", allocationSize = 1, sequenceName = "GCRFLDR_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRFLDR_SEQ_GEN")
    Long id

    /**
     * Description of the folder.
     */
    @Column(name = "GCRFLDR_DESCRIPTION")
    String description

    /**
     * Indicates if the folder was created for internal use (1=Yes or 0=No). Internal use folders are
     * created through the seeded data set and should not be deleted or modified in any way.
     */
    @Column(name = "GCRFLDR_INTERNAL")
    Boolean internal = false

    /**
     * Name of the folder.
     */
    @Column(name = "GCRFLDR_NAME")
    String name

    /**
     *  Optimistic lock token.
     */
    @Version
    @Column(name = "GCRFLDR_VERSION")
    Long version

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCRFLDR_USER_ID")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "GCRFLDR_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *  Source system that created or updated the data.
     */
    @Column(name = "GCRFLDR_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        name(nullable: false, maxSize: 1020)
        description(nullable: true, maxSize: 4000)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        internal(nullable: false)
    }

    public static CommunicationFolder fetchById(Long id) {

        def query
        CommunicationFolder.withSession { session ->
            query = session.getNamedQuery('CommunicationFolder.fetchById')
                    .setLong('id', id).list()[0]

        }
        return query
    }


    public static CommunicationFolder fetchByName(String name) {
        def query
        CommunicationFolder.withSession { session ->
            query = session.getNamedQuery('CommunicationFolder.fetchByName').setString('name', name).list()[0]
        }
        return query
    }

    public static Boolean existsAnotherSameNameFolder(Long folderId, String name ) {

        def query
        CommunicationFolder.withSession { session ->
            query = session.getNamedQuery('CommunicationFolder.existsAnotherSameNameFolder')
                    .setString('folderName', name)
                    .setLong('id', folderId)
                    .list()[0]
        }

        return (query != null)
    }

    public static List<CommunicationFolder> fetchFoldersWithActiveTemplates() {

        def folderList
        CommunicationFolder.withSession { session ->
            folderList = session.getNamedQuery('CommunicationFolder.fetchFoldersWithActiveTemplates')
                    .setString('currentuser', CommunicationCommonUtility.getUserOracleUserName())
                    .list()
        }
        return folderList
    }

    public static List<CommunicationFolder> fetchFoldersWithPublishedDatafields() {

        def folderList
        CommunicationFolder.withSession { session ->
            folderList = session.getNamedQuery('CommunicationFolder.fetchFoldersWithPublishedDatafields')
                    .list()
        }
        return folderList
    }

    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def descdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'desc'

        def queryCriteria = CommunicationFolder.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("name", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name))
            order((descdir ? Order.desc(pagingAndSortParams?.sortColumn) : Order.asc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }
}
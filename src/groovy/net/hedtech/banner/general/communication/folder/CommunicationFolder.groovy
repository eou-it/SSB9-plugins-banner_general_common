/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.folder

import groovy.transform.EqualsAndHashCode
import net.hedtech.banner.query.DynamicFinder

import javax.persistence.*

/**
 * Oraganizing object for communication templates
 *
 */
@Entity
@Table(name = "GCFOLDR")
@EqualsAndHashCode
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationFolder.fetchById",
                query = """ FROM CommunicationFolder a
                    WHERE a.id = :id"""),
        @NamedQuery(name = "CommunicationFolder.fetchByName",
                query = """ FROM CommunicationFolder a
                    WHERE a.name = :name"""),
        @NamedQuery(name = "CommunicationFolder.findByNameWildcard",
                query = """ FROM CommunicationFolder a
                    WHERE a.name like :folderName""")
])
class CommunicationFolder implements Serializable {
    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCFOLDR_SURROGATE_ID")
    @SequenceGenerator(name = "GCFOLDR_SEQ_GEN", allocationSize = 1, sequenceName = "GCFOLDR_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCFOLDR_SEQ_GEN")
    Long Id

    /**
     * Description of the folder.
     */
    @Column(name = "GCFOLDR_DESCRIPTION")
    String description

    /**
     * Indicates if the folder was created for internal use (1=Yes or 0=No). Internal use folders are
     * created through the seeded data set and should not be deleted or modified in any way.
     */
    @Column(name = "GCFOLDR_INTERNAL")
    Boolean internal = false

    /**
     * Name of the folder.
     */
    @Column(name = "GCFOLDR_NAME")
    String name

    /**
     *  Optimistic lock token.
     */
    @Version
    @Column(name = "GCFOLDR_VERSION")
    Long version

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCFOLDR_USER_ID")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "GCFOLDR_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *  Source system that created or updated the data.
     */
    @Column(name = "GCFOLDR_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        name(nullable: false, maxSize: 1020)
        description(nullable: true, maxSize: 4000)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        internal(nullable: false)
    }


    @Override
    public String toString() {
        return "Folder{" +
                "Id=" + Id +
                ", description='" + description + '\'' +
                ", internal=" + internal +
                ", name='" + name + '\'' +
                ", version=" + version +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", lastModified=" + lastModified +
                ", dataOrigin='" + dataOrigin + '\'' +
                '}';
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


    public static List findByNameWildcard(String folderName) {
        def query
        CommunicationFolder.withSession { session ->
            query = session.getNamedQuery('CommunicationFolder.findByNameWildcard')
                    .setString('folderName', folderName + '%').list()

        }
        return query
    }

    public static String getQuery(Map filterData) {
        def query =
                """ FROM CommunicationFolder a
                """

        def predicateArray = []

        if (filterData?.params?.containsKey('name')) {
            predicateArray.push("""(upper(a.name) like upper(:name))""")
        }

        if (predicateArray.size() > 0) {
            query = query + """ WHERE """ + predicateArray.join(""" AND """)
        }
        return query
    }


    public static findByFilterPagingParams(filterData, pagingAndSortParams) {
        return (new DynamicFinder(CommunicationFolder.class, getQuery(filterData), "a")).find(filterData,
                pagingAndSortParams)
    }


    public static countByFilterParams(filterData) {
        return new DynamicFinder(CommunicationFolder.class, getQuery(filterData), "a").count(filterData)
    }


}

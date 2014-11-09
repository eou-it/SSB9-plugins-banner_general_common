/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import org.hibernate.annotations.Type

import javax.persistence.*

/**
 * BannerPopulation PopulationQuery Base Table entity.
 */
@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GCBQURY")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationPopulationQuery.findAll",
                query = """ FROM CommunicationPopulationQuery a
                        order by a.name"""),
        @NamedQuery(name = "CommunicationPopulationQuery.findAllByFolderName",
                query = """ FROM CommunicationPopulationQuery a
                    WHERE a.folder.name = :folderName"""),
        @NamedQuery(name = "CommunicationPopulationQuery.fetchByQueryNameAndFolderName",
                query = """ FROM CommunicationPopulationQuery a
                    WHERE a.folder.name = :folderName
                      AND upper(a.name) = upper(:queryName)"""),
        @NamedQuery(name = "CommunicationPopulationQuery.existsAnotherNameFolder",
                query = """ FROM CommunicationPopulationQuery a
                    WHERE a.folder.name = :folderName
                    AND   upper(a.name) = upper(:queryName)
                    AND   a.id <> :id"""),
        @NamedQuery(name = "CommunicationPopulationQuery.fetchById",
                query = """ FROM CommunicationPopulationQuery a
                    WHERE a.id = :id"""),
        @NamedQuery(name = "CommunicationPopulationQuery.findAllByQueryName",
                query = """ FROM CommunicationPopulationQuery a
                    WHERE a.name like :queryName""")
])


class CommunicationPopulationQuery implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCBQURY_SURROGATE_ID")
    @SequenceGenerator(name = "GCBQURY_SEQ_GEN", allocationSize = 1, sequenceName = "GCBQURY_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCBQURY_SEQ_GEN")
    Long id

    /**
     * NAME: Descriptive name of the population query.
     */
    @Column(name = "GCBQURY_NAME")
    String name

    /**
     * DESCRIPTION: Long description.
     */
    @Column(name = "GCBQURY_DESCRIPTION")
    String description

    /**
     * Foreign key reference to the Folder under which this template is organized.
     */

    @ManyToOne(optional = false)
    @JoinColumn(name = "GCBQURY_FOLDER_ID", referencedColumnName = "GCFOLDR_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCBQURY_INV_GCFOLDR_KEY")
    CommunicationFolder folder

    /**
     * CREATE_DATE: The date the record was created.
     */
    @Column(name = "GCBQURY_CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date createDate

    /**
     * CREATOR_ID: The Oracle username of the user who created the record.
     */
    @Column(name = "GCBQURY_CREATOR_ID")
    String createdBy

    /**
     * VERSION: Optimistic lock token
     */
    @Version
    @Column(name = "GCBQURY_VERSION")
    Long version

    /**
     * ACTIVITY_DATE: Most current date record was created or changed.
     */
    @Column(name = "GCBQURY_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER_ID: Oracle User ID of the person who last inserted or last updated the data.
     */
    @Column(name = "GCBQURY_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCBQURY_DATA_ORIGIN")
    String dataOrigin

    /**
     * QUERY_STRING: The text of the statement that will be executed.
     */
    @Lob
    @Column(name = "GCBQURY_QUERY_STRING")
    String sqlString

    /**
     * VALID_IND: Indicator showing if the SQL statement is syntactically valid(Y or N).
     */
    @Type(type = "yes_no")
    @Column(name = "GCBQURY_VALID_IND")
    Boolean valid


    /* ----------------------------------------------------------------------*/


    static constraints = {
        folder(nullable: false, maxSize: 30)
        createDate(nullable: false)
        createdBy(nullable: false, maxSize: 30)
        description(nullable: true, maxSize: 2000)
        name(nullable: false, maxSize: 30)
        sqlString(nullable: true)
        valid(nullable: false)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)

    }


    public static List findAll() {

        def queries = []
        CommunicationPopulationQuery.withSession { session ->
            queries = session.getNamedQuery('CommunicationPopulationQuery.findAll').list()
        }
        return queries
    }


    public static CommunicationPopulationQuery fetchByQueryNameAndFolderName(String queryName, String folderName) {

        def query
        CommunicationPopulationQuery.withSession { session ->
            query = session.getNamedQuery('CommunicationPopulationQuery.fetchByQueryNameAndFolderName').setString('folderName', folderName).setString('queryName', queryName).list()[0]
        }
        return query
    }


    public static List findAllByQueryName(String queryName) {

        def query
        CommunicationPopulationQuery.withSession { session ->
            query = session.getNamedQuery('CommunicationPopulationQuery.findAllByQueryName').setString('queryName', queryName + "%").list()
        }
        return query
    }


    public static List findAllByFolderName(String folderName) {

        def queries = []
        CommunicationPopulationQuery.withSession { session ->
            queries = session.getNamedQuery('CommunicationPopulationQuery.findAllByFolderName').setString('folderName', folderName).list()
        }
        return queries
    }


    public static Boolean existsAnotherNameFolder(Long queryId, String queryName, String folderName) {

        def query
        CommunicationPopulationQuery.withSession { session ->
            query = session.getNamedQuery('CommunicationPopulationQuery.existsAnotherNameFolder')
                    .setString('folderName', folderName).setString('queryName', queryName).setLong('id', queryId).list()[0]

        }
        return (query != null)
    }


    public static CommunicationPopulationQuery fetchById(Long id) {

        def query
        CommunicationPopulationQuery.withSession { session ->
            query = session.getNamedQuery('CommunicationPopulationQuery.fetchById')
                    .setLong('id', id).list()[0]

        }
        return query
    }


    @Override
    public String toString() {
        return "PopulationQuery{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", folder=" + folder +
                ", createDate=" + createDate +
                ", createdBy='" + createdBy + '\'' +
                ", version=" + version +
                ", lastModified=" + lastModified +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", dataOrigin='" + dataOrigin + '\'' +
                ", sqlString='" + sqlString + '\'' +
                ", valid=" + valid +
                '}';
    }

}

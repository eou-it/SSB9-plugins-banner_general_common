/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.EqualsAndHashCode
import net.hedtech.banner.query.DynamicFinder
import org.hibernate.annotations.Type

import javax.persistence.*

/**
 * BannerPopulation PopulationQueryView Base Table entity.
 */
@Entity
@EqualsAndHashCode
@Table(name = "GVQ_GCBQURY")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationPopulationQueryView.findAll",
                query = """ FROM CommunicationPopulationQueryView a
                            ORDER by a.name"""),
        @NamedQuery(name = "CommunicationPopulationQueryView.fetchById",
                query = """ FROM CommunicationPopulationQueryView a
                            WHERE  a.id = :id
                        """)
])
class CommunicationPopulationQueryView implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "SURROGATE_ID")
    Long id

    /**
     * NAME: Descriptive name of the population query.
     */
    @Column(name = "NAME")
    String name

    /**
     * DESCRIPTION: Long description.
     */
    @Column(name = "DESCRIPTION")
    String description

    /**
     * FOLDER: The folder containing this object.
     */
    @Column(name = "FOLDER_ID")
    String folderId

    /**
     * FOLDER: The folder containing this object.
     */
    @Column(name = "FOLDER_NAME")
    String folderName

    /**
     * CREATE_DATE: The date the record was created.
     */
    @Column(name = "CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date createDate

    /**
     * CREATOR_ID: The Oracle username of the user who created the record.
     */
    @Column(name = "CREATOR_ID")
    String createdBy

    /**
     * LOCKED_IND: Indicator showing if the population query may be modified or not.
     */
    @Type(type = "yes_no")
    @Column(name = "LOCKED_IND")
    Boolean locked

    /**
     * VALID_IND: Indicator showing if the SQL statement is syntactically valid(Y or N).
     */
    @Type(type = "yes_no")
    @Column(name = "VALID_IND")
    Boolean valid

    /**
     * PUBLISHED_IND: Indicator showing if the SQL statement is available to be executed.
     */
    @Type(type = "yes_no")
    @Column(name = "PUBLISHED_IND")
    Boolean published

    /**
     * VERSION: Optimistic lock token
     */
    @Version
    @Column(name = "VERSION")
    Long version


    static constraints = {
        folderName(nullable: false)
        createDate(nullable: false)
        createdBy(nullable: false, maxSize: 30)
        description(nullable: true, maxSize: 2000)
        locked(nullable: false)
        name(nullable: false, maxSize: 30)
        valid(nullable: false)
        published(nullable: false)
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']


    public static CommunicationPopulationQueryView fetchById(Long id) {

        def query =
                CommunicationPopulationQueryView.withSession { session ->
                    session.getNamedQuery('CommunicationPopulationQueryView.fetchById')
                            .setLong('id', id)
                            .list()[0]

                }
        return query
    }


    public static List findAll() {

        def queries = []
        CommunicationPopulationQueryView.withSession { session ->
            queries = session.getNamedQuery('CommunicationPopulationQueryView.findAll')
                    .list()
        }
        return queries
    }


    public static String getQuery(Map filterData) {
        def query =
                """ FROM CommunicationPopulationQueryView a  """

        if (filterData?.params?.containsKey('folderName')) {
            query = query + """WHERE (a.folderName = :folderName) """
        }
        if (filterData?.params?.containsKey('folderName') && filterData?.params?.containsKey('name')) {
            query = query + """  AND (upper(a.name) like upper(:name)+'%')"""
        } else if (filterData?.params?.containsKey('name')) {
            query = query + """  WHERE (upper(a.name) like upper(:name)+'%')"""
        }
        return query
    }


    public static findByFilterPagingParams(filterData, pagingAndSortParams) {
        def finder = new DynamicFinder(CommunicationPopulationQueryView.class, getQuery(filterData), "a")
        return finder.find(filterData, pagingAndSortParams)
    }


    public static countByFilterParams(filterData) {
        return new DynamicFinder(CommunicationPopulationQueryView.class, getQuery(filterData), "a").count(filterData)
    }


    @Override
    public String toString() {
        return "PopulationQueryView{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", folderId='" + folderId + '\'' +
                ", folderName='" + folderName + '\'' +
                ", createDate=" + createDate +
                ", createdBy='" + createdBy + '\'' +
                ", locked=" + locked +
                ", valid=" + valid +
                ", published=" + published +
                ", version=" + version +
                '}';
    }

}

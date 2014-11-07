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
     * VALID_IND: Indicator showing if the SQL statement is syntactically valid(Y or N).
     */
    @Type(type = "yes_no")
    @Column(name = "VALID_IND")
    Boolean valid

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
        name(nullable: false, maxSize: 30)
        valid(nullable: false)
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

        def predicateArray = []

        if (filterData?.params?.containsKey('folderName')) {
            predicateArray.push(""" (a.folderName = :folderName)""")
        }
        if (filterData?.params?.containsKey('name')) {
            predicateArray.push("""(upper(a.name) like upper(:name))""")
        }

        if (predicateArray.size() > 0) {
            query = query + """ WHERE """ + predicateArray.join(""" AND """)
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
                ", valid=" + valid +
                ", version=" + version +
                '}';
    }

}

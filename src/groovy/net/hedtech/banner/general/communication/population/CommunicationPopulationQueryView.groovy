/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.EqualsAndHashCode
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.query.DynamicFinder
import oracle.net.ns.Communication
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

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

    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams){

        def descdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'desc'

        def queryCriteria = CommunicationPopulationQueryView.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("name", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name))
            order((descdir ? Order.desc(pagingAndSortParams?.sortColumn) : Order.asc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
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

/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

import javax.persistence.*

/**
 * BannerPopulation PopulationQueryView Base Table entity.
 */
@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GVQ_GCBQURY")
@NamedQueries(value = [
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
    Boolean changesPending

    /**
     * VERSION: Optimistic lock token
     */
    @Version
    @Column(name = "VERSION")
    Long version

    /**
     * Query version surrogate id.
     */
    @Column(name = "VERSION_SURROGATE_ID")
    Long versionId

    /**
     * PUBLISHED_DATE: The date the latest query version was published.
     */
    @Column(name = "PUBLISHED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date publishedDate

    /**
     * PUBLISHED_SQL: The sql string of the latest published query version.
     */
    @Column(name = "PUBLISHED_SQL")
    String publishedSql

    static constraints = {
        folderName(nullable: false)
        createDate(nullable: false)
        createdBy(nullable: false, maxSize: 30)
        description(nullable: true, maxSize: 2000)
        name(nullable: false, maxSize: 30)
        changesPending(nullable: false)
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id','versionId']


    public static CommunicationPopulationQueryView fetchById(Long id) {

        def query =
                CommunicationPopulationQueryView.withSession { session ->
                    session.getNamedQuery('CommunicationPopulationQueryView.fetchById')
                            .setLong('id', id)
                            .list()[0]

                }
        return query
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

}

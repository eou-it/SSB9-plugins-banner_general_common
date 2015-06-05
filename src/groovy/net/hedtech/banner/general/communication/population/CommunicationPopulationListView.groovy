/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import org.hibernate.criterion.Order

import javax.persistence.*

/**
 * BannerPopulation Selection List definitions view
 */
@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GVQ_GCRSLIS")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationPopulationListView.fetchAllByQueryId",
                query = """ FROM CommunicationPopulationListView a
                    WHERE  a.populationQueryId = :populationQueryId
                     """),
        @NamedQuery(name = "CommunicationPopulationListView.fetchAllByQueryIdUserId",
                query = """ FROM CommunicationPopulationListView a
                    WHERE  a.populationQueryId = :populationQueryId
                    AND    upper(a.lastCalculatedBy) = upper(:userid) """)
])
class CommunicationPopulationListView implements Serializable {

    /**
     * Generated unique numeric identifier for this entity.
     */
    @Id
    @Column(name = "GCRSLIS_SURROGATE_ID")
    Long id

    @Column(name = "GCRSLIS_NAME")
    String name

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "GCRSLIS_VERSION")
    Long version

    /**
     *
     */
    @Column(name = "GCRSLIS_QUERY_ID")
    Long populationQueryId

    /**
     * ID of user who created the Selection List
     */
    @Column(name = "GCRSLIS_LAST_CALC_BY")
    String lastCalculatedBy

    /**
     * Record creation date
     */
    @Column(name = "GCRSLIS_LAST_CALC_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastCalculatedTime

    /**
     * Last calculated count
     */
    @Column(name = "GCRSLIS_LAST_CALC_COUNT")
    Long lastCalculatedCount

    /**
     * This field is the status of the calculation.
     */
    @Column(name = "GCRSLIS_STATUS")
    String calculationStatus

    /**
     * This field defines the nameof the query
     */
    @Column(name = "GCBQURY_NAME")
    String queryName

    /**
     * This field defines the application
     */
    @Column(name = "GCBQURY_FOLDER_NAME")
    String queryFolder

    /**
     * This field defines the description of the query
     */
    @Column(name = "GCBQURY_DESCRIPTION")
    String queryDescription

    /**
     * This field defines the creator of the query
     */
    @Column(name = "GCBQURY_CREATOR_ID")
    String queryCreator

    /**
     * the last time the query itself was updated
     */
    @Column(name = "GCBQURY_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date queryLastUpdated


    static constraints = {
        name(nullable: false)
        populationQueryId(nullable: false)
        lastCalculatedBy(nullable: true, maxSize: 30)
        lastCalculatedTime(nullable: true)
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']


    public static List<CommunicationPopulationListView> fetchAllByQueryId(Long populationQueryId) {

        def CommunicationPopulationListView[] populationListViews
        populationListViews = CommunicationPopulationListView.withSession { session ->
            session.getNamedQuery('CommunicationPopulationListView.fetchAllByQueryId')
                    .setLong('populationQueryId', populationQueryId)
                    .list()
        }
        return populationListViews
    }


    public static CommunicationPopulationListView fetchAllByQueryIdUserId(Long populationQueryId, String userid) {

        def CommunicationPopulationListView populationListView
        populationListView = CommunicationPopulationListView.withSession { session ->
            session.getNamedQuery('CommunicationPopulationListView.fetchAllByQueryIdUserId')
                    .setLong('populationQueryId', populationQueryId)
                    .setString('userid', userid)
                    .list()[0]
        }
        return populationListView
    }


    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def ascdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'asc'

        def queryCriteria = CommunicationPopulationListView.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("queryName", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.queryName))
            order((ascdir ? Order.asc(pagingAndSortParams?.sortColumn) : Order.desc(pagingAndSortParams?.sortColumn)))
        }
        return results
    }
}

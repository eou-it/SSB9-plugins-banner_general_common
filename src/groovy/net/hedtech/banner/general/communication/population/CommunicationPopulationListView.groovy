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
@Table(name = "GVQ_GCBPOPL_DETAIL")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationPopulationListView.fetchAllByQueryId",
                query = """ FROM CommunicationPopulationListView a
                    WHERE  a.populationQueryId = :populationQueryId
                     """),
        @NamedQuery(name = "CommunicationPopulationListView.fetchAllByQueryIdUserId",
                query = """ FROM CommunicationPopulationListView a
                    WHERE  a.populationQueryId = :populationQueryId
                    AND    upper(a.createdBy) = upper(:userid) """),
        @NamedQuery(name = "CommunicationPopulationListView.fetchLatestByPopulationIdAndUserId",
                query = """ FROM CommunicationPopulationListView a
                    WHERE  a.id = :populationId
                    AND    upper(a.createdBy) = upper(:userid)
                    ORDER BY a.lastCalculatedTime desc """),
        @NamedQuery(name = "CommunicationPopulationListView.fetchByPopulationId",
                query = """ FROM CommunicationPopulationListView a
                    WHERE  a.id = :populationId """),
        @NamedQuery(name = "CommunicationPopulationListView.fetchBySelectionListIdAndManual",
                query = """ select distinct a FROM CommunicationPopulationListView a
            WHERE  a.populationSelectionListId IN ( :selectionListId, :includeListId)
            """)
])
class CommunicationPopulationListView implements Serializable {
    /**
     * Generated unique numeric identifier for this entity.
     */
    @Id
    @Column(name = "POPULATION_ID")
    Long id

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "GCBPOPL_VERSION")
    Long version

    @Column(name = "POPULATION_NAME")
    String name

    @Column(name = "POPULATION_DESCRIPTION")
    String description

    @Column(name = "POPULATION_FOLDER_NAME")
    String populationFolderName

    @Column(name = "POPULATION_VERSION_ID")
    Long populationVersionId

    @Column(name = "POPULATION_CALCULATION_ID")
    Long populationCalculationId

    /**
     * Record creation date
     */
    @Column(name = "CALCULATED_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastCalculatedTime

    /**
     * ID of user who created the Selection List
     */
    @Column(name = "CALCULATED_BY")
    String calculatedBy

    /**
     * ID of user who created the Selection List
     */
    @Column(name = "CREATOR_ID")
    String createdBy

    /**
     * Record creation date
     */
    @Column(name = "CREATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date createdDate

    /**
     * Last calculated count
     */
    @Column(name = "CALCULATED_COUNT")
    Long lastCalculatedCount

    /**
     * This field is the status of the calculation.
     */
    @Column(name = "CALCULATION_STATUS")
    String calculationStatus

    /**
     *
     */
    @Column(name = "POPULATION_SELECTION_LIST_ID")
    Long populationSelectionListId

    /**
     *
     */
    @Column(name = "POPULATION_INCLUDE_LIST_ID")
    Long includeListId

    /**
     *
     */
    @Column(name = "POPULATION_EXCLUDE_LIST_ID")
    Long excludeListId

    /**
     *
     */
    @Column(name = "QUERY_ID")
    Long populationQueryId

    /**
     * This field defines the name of the query
     */
    @Column(name = "QUERY_NAME")
    String queryName

    /**
     * This field defines the application
     */
    @Column(name = "QUERY_FOLDER_NAME")
    String queryFolder

    /**
     * This field defines the description of the query
     */
    @Column(name = "QUERY_DESCRIPTION")
    String queryDescription

    /**
     * This field defines the creator of the query
     */
    @Column(name = "QUERY_CREATOR_ID")
    String queryCreator

    /**
     * the last time the query itself was updated
     */
    @Column(name = "QUERY_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date queryLastUpdated

    /**
     *
     */
    @Column(name = "QUERY_VERSION_ID")
    Long populationQueryVersionId

    /**
     * This field defines the name of the query version
     */
    @Column(name = "QUERY_VERSION_NAME")
    @Temporal(TemporalType.TIMESTAMP)
    Date queryVersionName

    /**
     * Indicates what advanced option user selected for regenerating population
     */
    @Column(name = "QUERY_VERSION_SELECTED")
    String useRecentOrCurrent


    static constraints = {
        name(nullable: false)
        populationQueryId(nullable: false)
        calculatedBy(nullable: true, maxSize: 30)
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

    public static CommunicationPopulationListView fetchLatestByPopulation( CommunicationPopulation population ) {
        return fetchLatestByPopulationIdAndUserId( population.id, population.createdBy )
    }

    public static CommunicationPopulationListView fetchLatestByPopulationIdAndUserId(Long populationId, String userid) {

        def CommunicationPopulationListView populationListView
        populationListView = CommunicationPopulationListView.withSession { session ->
            session.getNamedQuery('CommunicationPopulationListView.fetchLatestByPopulationIdAndUserId')
                    .setLong('populationId', populationId)
                    .setString('userid', userid)
                    .list()[0]
        }
        return populationListView
    }

    public static CommunicationPopulationListView fetchByPopulationId(Long populationId) {

        def CommunicationPopulationListView populationListView
        populationListView = CommunicationPopulationListView.withSession { session ->
            session.getNamedQuery('CommunicationPopulationListView.fetchByPopulationId')
                    .setLong('populationId', populationId)
                    .list()[0]
        }
        return populationListView
    }

    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def isAscending = pagingAndSortParams?.sortDirection?.toLowerCase() == 'asc'

        def queryCriteria = CommunicationPopulationListView.createCriteria()
        def results

        String name = filterData?.params?.populationName ?: ""
        if (name.equals( "%" ) || name.equals( "*" )) {
            results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
                ilike("createdBy", filterData?.params?.createdBy)
                order((isAscending ? Order.asc(pagingAndSortParams?.sortColumn) : Order.desc(pagingAndSortParams?.sortColumn)))
            }
        } else {
            String scrubbedName = CommunicationCommonUtility.getScrubbedInput( name )
            results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
                ilike("name", scrubbedName)
                ilike("createdBy", filterData?.params?.createdBy)
                order((isAscending ? Order.asc(pagingAndSortParams?.sortColumn) : Order.desc(pagingAndSortParams?.sortColumn)))
            }
        }

        return results
    }

    public static findAllForSendByPagination(filterData, pagingAndSortParams) {

        def queryCriteria = CommunicationPopulationListView.createCriteria()
        def searchName = CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name)
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            eq("createdBy", CommunicationCommonUtility.getUserOracleUserName().toUpperCase())
            gt("lastCalculatedCount",0L)
            and {
                or {
                    ilike("name", searchName)
                    ilike("populationFolderName", searchName)
                }
            }
        }
        return results
    }

    public static List<CommunicationPopulationListView> fetchBySelectionListIdAndManual(Long selectionListId, Long includeListId) {

        def populationListViews

        populationListViews = CommunicationPopulationListView.withSession { session ->
            session.getNamedQuery('CommunicationPopulationListView.fetchBySelectionListIdAndManual')
                    .setLong('selectionListId', selectionListId)
                    .setLong('includeListId', includeListId)
                    .list()
        }
        return populationListViews
    }
}

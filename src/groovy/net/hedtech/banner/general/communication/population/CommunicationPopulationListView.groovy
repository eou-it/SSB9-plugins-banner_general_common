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
@Table(name = "GVQ_GCBPOPL")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationPopulationListView.fetchAllByQueryId",
                query = """ FROM CommunicationPopulationListView a
                    WHERE  a.populationQueryId = :populationQueryId
                     """),
        @NamedQuery(name = "CommunicationPopulationListView.fetchAllByQueryIdUserId",
                query = """ FROM CommunicationPopulationListView a
                    WHERE  a.populationQueryId = :populationQueryId
                    AND    upper(a.lastCalculatedBy) = upper(:userid) """),
        @NamedQuery(name = "CommunicationPopulationListView.fetchAllByPopulationIdUserId",
                query = """ FROM CommunicationPopulationListView a
                    WHERE  a.id = :populationId
                    AND    upper(a.lastCalculatedBy) = upper(:userid) """)
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
    String lastCalculatedBy

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

    public static CommunicationPopulationListView fetchAllByPopulationIdUserId(Long populationId, String userid) {

        def CommunicationPopulationListView populationListView
        populationListView = CommunicationPopulationListView.withSession { session ->
            session.getNamedQuery('CommunicationPopulationListView.fetchAllByPopulationIdUserId')
                    .setLong('populationId', populationId)
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
            ilike("lastCalculatedBy", filterData?.params?.lastCalculatedBy)
            order((ascdir ? Order.asc(pagingAndSortParams?.sortColumn) : Order.desc(pagingAndSortParams?.sortColumn)))
        }
        return results
    }

    public static findAllForSendByPagination(filterData, pagingAndSortParams) {

        def queryCriteria = CommunicationPopulationListView.createCriteria()
        def searchName = CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name)
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            eq("lastCalculatedBy",CommunicationCommonUtility.getUserOracleUserName().toLowerCase(), [ignoreCase: true])
            gt("lastCalculatedCount",0L)
            and {
                or {
                    ilike("queryName", searchName)
                    ilike("queryFolder", searchName)
                }
            }
        }
        return results
    }
}

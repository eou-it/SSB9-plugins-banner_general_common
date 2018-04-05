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
 * BannerPopulation Selection List definitions view
 */
@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GVQ_GCRLENT")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationPopulationProfileView.findAllBySelectionListId",
                query = """ FROM CommunicationPopulationProfileView a
                    WHERE  a.selectionListId = :selectionListId
                    """)
])
class CommunicationPopulationProfileView implements Serializable {

    /**
     * Generated unique numeric identifier for this entity.
     */
    @Id
    @Column(name = "SURROGATE_ID")
    Long id

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "VERSION")
    Long version

    /**
     *
     */
    @Column(name = "SLIS_ID")
    Long selectionListId

    /**
     * Internal identification number of the person.
     */
    @Column(name = "PIDM")
    Long pidm

    /**
     * This field defines the identification number used to access person on-line.
     */
    @Column(name = "BANNER_ID")
    String bannerId

    /**
     * This field defines the last name of person.
     */
    @Column(name = "LAST_NAME")
    String lastName

    /**
     * This field entities the first name of person.
     */
    @Column(name = "FIRST_NAME")
    String firstName

    /**
     * This field entities the middle name of person.
     */
    @Column(name = "MIDDLE_NAME")
    String middleName

    /**
     * This field entifies the middle name of person.
     */
    @Column(name = "SURNAME_PREFIX")
    String surnamePrefix

    /**
     * This field identifies if a person record is confidential
     *
     */
    @Type(type = "yes_no")
    @Column(name = "CONFIDENTIAL_IND")
    Boolean confidential

    /**
     * This field indicates if a person is deceased.
     */
    @Type(type = "yes_no")
    @Column(name = "DECEASED_IND")
    Boolean deceased


    static constraints = {
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']


    public static List<CommunicationPopulationProfileView> findAllBySelectionListId(Long selectionListId) {
        assert( selectionListId != null )
        def populationProfileViews

        populationProfileViews = CommunicationPopulationProfileView.withSession { session ->
            session.getNamedQuery('CommunicationPopulationProfileView.findAllBySelectionListId')
                    .setLong('selectionListId', selectionListId)
                    .list()
        }
        return populationProfileViews
    }

    public static Map findDistinctProfilesByNameWithPagingAndSortParams(filter, pagingAndSortParams) {
        return [list: CommunicationPopulationProfileView.findAllDistinctProfilesByNameWithPagingAndSortParams(filter, pagingAndSortParams)
                , totalCount: CommunicationPopulationProfileView.findCountDistinctProfilesByName(filter)]
    }

    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams){

        def ascdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'asc'
        def searchName = CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name)

        def queryCriteria = CommunicationPopulationProfileView.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
             'in' ("selectionListId", [filterData?.params?.selectionListId, filterData?.params?.includeListId])
             or {
                 ilike("lastName", searchName)
                 ilike("firstName", searchName)
                 ilike("bannerId", searchName)
             }
            order((ascdir ? Order.asc(pagingAndSortParams?.sortColumn) : Order.desc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }

    public static int findTotalCountByPopulation( CommunicationPopulationListView populationListView ) {
        Map filterData = [
            params: [
                selectionListId: populationListView.populationSelectionListId ?: 0L, // guaranteed to not satisfy 0 if null
                includeListId: populationListView.includeListId ?: 0L, // guaranteed to not satisfy 0 if null
                "name": '%'
            ]
        ]
        int count = findCountDistinctProfilesByName( filterData )
        return count
    }

    public static findCountDistinctProfilesByName( Map filterData ) {
        if(!filterData)
            return 0;

        def searchName = CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name)
        def queryCriteria = CommunicationPopulationProfileView.createCriteria()
        def results = queryCriteria.get() {
            'in' ("selectionListId", [filterData?.params?.selectionListId, filterData?.params?.includeListId])
            or {
                ilike("lastName", searchName)
                ilike("firstName", searchName)
                ilike("bannerId", searchName)
            }
            projections {
                countDistinct("pidm")
            }
        }
        return results
    }

    public static findAllDistinctProfilesByNameWithPagingAndSortParams(filterData, pagingAndSortParams){

        def ascdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'asc'
        def searchName = CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name)

        def queryCriteria = CommunicationPopulationProfileView.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            'in' ("selectionListId", [filterData?.params?.selectionListId, filterData?.params?.includeListId])
            or {
                ilike("lastName", searchName)
                ilike("firstName", searchName)
                ilike("bannerId", searchName)
            }
            projections {
                distinct(["pidm","bannerId","lastName","firstName","middleName","surnamePrefix","confidential","deceased"])
            }
            order((ascdir ? Order.asc(pagingAndSortParams?.sortColumn) : Order.desc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }
}

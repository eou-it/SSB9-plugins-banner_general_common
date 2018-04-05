/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population.selectionextract

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import org.hibernate.criterion.Order

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GVQ_POPSEL_EXTR_VIEW")
class CommunicationPopulationSelectionExtractView {

    @Id
    @Column(name = "POPSEL_EXTR_ID")
    String id

    @Column(name = "POPSEL_EXTR_APPLICATION")
    String application

    @Column(name = "POPSEL_EXTR_APPLICATION_DESC")
    String applicationDesc

    @Column(name = "POPSEL_EXTR_SELECTION")
    String selection

    @Column(name = "POPSEL_EXTR_SELECTION_DESC")
    String selectionDesc

    @Column(name = "POPSEL_EXTR_CREATOR_ID")
    String creatorId

    @Column(name = "POPSEL_EXTR_USER_ID")
    String userId

    @Column(name = "POPSEL_EXTR_COUNT")
    Long count

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']

    public static findByApplicationWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def ascdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'asc'
        def application = filterData?.params?.application
        def selection = CommunicationCommonUtility.getScrubbedInput(filterData?.params?.selection)

        def queryCriteria = CommunicationPopulationSelectionExtractView.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("application", application)
            ilike("selection",selection)
            order((ascdir ? Order.asc(pagingAndSortParams?.sortColumn) : Order.desc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }

    //Used for Application Lookup.
    public static Map findDistinctApplicationWithPagingAndSortParams(filter, pagingAndSortParams) {
        return [list: CommunicationPopulationSelectionExtractView.findAllDistinctApplicationWithPagingAndSortParams(filter, pagingAndSortParams)
                , totalCount: CommunicationPopulationSelectionExtractView.findCountDistinctApplication(filter)]
    }

    public static findCountDistinctApplication(filterData) {

        if(!filterData)
            return 0;

        def application = CommunicationCommonUtility.getScrubbedInput(filterData?.params?.application)

        def queryCriteria = CommunicationPopulationSelectionExtractView.createCriteria()
        def results = queryCriteria.get() {
            ilike("application", application)
            projections {
                countDistinct("application")
            }
        }
        return results
    }

    public static findAllDistinctApplicationWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def ascdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'asc'
        def application = CommunicationCommonUtility.getScrubbedInput(filterData?.params?.application)

        def queryCriteria = CommunicationPopulationSelectionExtractView.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("application", application)
            projections {
                distinct("application")
                property("applicationDesc")
            }
            order((ascdir ? Order.asc(pagingAndSortParams?.sortColumn) : Order.desc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }
}

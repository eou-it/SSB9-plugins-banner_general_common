/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.EqualsAndHashCode
import net.hedtech.banner.query.DynamicFinder

import javax.persistence.*

/**
 * BannerPopulation Selection List definitions view
 */
@Entity
@EqualsAndHashCode
@Table(name = "GVQ_GCRLENT")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationPopulationProfileView.findAllByPopulationId",
                query = """ FROM CommunicationPopulationProfileView a
                    WHERE  a.populationId = :populationId
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
    @Column(name = "QUERY_ID")
    Long populationQueryId

    /**
     *
     */
    @Column(name = "SLIS_ID")
    Long populationId

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
     * This field entifies the first name of person.
     */
    @Column(name = "FIRST_NAME")
    String firstName


    public String toString() {
        return "PopulationProfileView{" +
                "id=" + id +
                ", version=" + version +
                ", populationQueryId=" + populationQueryId +
                ", populationId=" + populationId +
                ", pidm=" + pidm +
                ", bannerId='" + bannerId + '\'' +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                '}';
    }


    static constraints = {
        populationQueryId(nullable: false)
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']


    public static List<CommunicationPopulationProfileView> findAllByPopulationId(Long populationId) {

        def CommunicationPopulationProfileView[] populationProfileViews

        populationProfileViews = CommunicationPopulationProfileView.withSession { session ->
            session.getNamedQuery('CommunicationPopulationProfileView.findAllByPopulationId')
                    .setLong('populationId', populationId)
                    .list()
        }
        return populationProfileViews
    }


    public static String getQuery(Map filterData) {
        def query =
                """  FROM CommunicationPopulationProfileView a
                     WHERE  a.populationId = :populationId
                """

        if (filterData?.params?.containsKey('bannerId')) {
            query = query + """AND (a.bannerId = :bannerId) """
        }

        if (filterData?.params?.containsKey('lastName')) {
            query = query + """AND upper(a.lastName) like upper(:lastName) """
        }

        return query
    }


    public static findByFilterPagingParams(filterData, pagingAndSortParams) {
        return (new DynamicFinder(CommunicationPopulationProfileView.class, getQuery(filterData), "a")).find(filterData,
                pagingAndSortParams)
    }


    public static countByFilterParams(filterData) {
        return (new DynamicFinder(CommunicationPopulationProfileView.class, getQuery(filterData), "a")).count(filterData)
    }

}

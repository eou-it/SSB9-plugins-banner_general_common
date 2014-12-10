/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.EqualsAndHashCode
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.query.DynamicFinder
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

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

    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams){

        def ascdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'asc'
        def searchName = CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name)

        def queryCriteria = CommunicationPopulationProfileView.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
             eq ("populationId", filterData?.params?.populationId)
             or {
                 ilike("lastName", searchName)
                 ilike("firstName", searchName)
                 ilike("bannerId", searchName)
             }
            order((ascdir ? Order.asc(pagingAndSortParams?.sortColumn) : Order.desc(pagingAndSortParams?.sortColumn)))
        }
        return results
    }


    @Override
    public String toString() {
        return "CommunicationPopulationProfileView{" +
                "id=" + id +
                ", version=" + version +
                ", populationQueryId=" + populationQueryId +
                ", populationId=" + populationId +
                ", pidm=" + pidm +
                ", bannerId='" + bannerId + '\'' +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", surnamePrefix='" + surnamePrefix + '\'' +
                ", confidential=" + confidential +
                ", deceased=" + deceased +
                '}';
    }
}

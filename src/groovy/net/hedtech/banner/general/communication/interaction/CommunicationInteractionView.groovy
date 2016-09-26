/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.interaction

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.item.CommunicationChannel
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GVQ_GCVINTR")
class CommunicationInteractionView implements Serializable {

    @Id
    @Column(name = "RECORD_ID")
    String recordId

    @Column(name = "SURROGATE_ID")
    Long surrogateId

    @Column(name = "INTERACTEE_PIDM")
    Long interacteePidm

    @Column(name = "INTERACTION_DATE")
    Date interactionDate

    @Column(name = "BANNER_ID")
    String bannerId

    @Column(name = "FIRST_NAME")
    String firstName

    @Column(name = "LAST_NAME")
    String lastName

    @Column(name = "MIDDLE_NAME")
    String middleName

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

    @Column(name = "CHANNEL")
    CommunicationChannel channel

    @Column(name = "TEMPLATE_NAME")
    String templateName

    @Column(name = "FOLDER_NAME")
    String folderName

    @Column(name = "SUBJECT")
    String subject

    @Column(name = "ORGANIZATION_NAME")
    String organizationName

    @Column(name = "CREATOR_ID")
    String creatorId

    static constraints = {
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']


    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def ascdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'asc'
        def searchName = filterData?.params?.name

        def queryCriteria = CommunicationInteractionView.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
                eq("bannerId", searchName)
            order((ascdir ? Order.asc(pagingAndSortParams?.sortColumn) : Order.desc(pagingAndSortParams?.sortColumn)))
        }
        return results
    }

    public static Map findDistinctConstituentsByNameWithPagingAndSortParams(filter, pagingAndSortParams) {
        return [list: CommunicationInteractionView.findAllDistinctConstituentsByNameWithPagingAndSortParams(filter, pagingAndSortParams)
                , totalCount: CommunicationInteractionView.findCountDistinctConstituentsByName(filter)]
    }

    public static findCountDistinctConstituentsByName(filterData) {
        if(!filterData)
            return 0;

        def searchName = CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name)
        def queryCriteria = CommunicationInteractionView.createCriteria()
        def results = queryCriteria.get() {
            or {
                ilike("lastName", searchName)
                ilike("firstName", searchName)
                ilike("bannerId", searchName)
            }
            projections {
                countDistinct("interacteePidm")
            }
        }
        return results
    }

    public static findAllDistinctConstituentsByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def ascdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'asc'
        def searchName = CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name)

        def queryCriteria = CommunicationInteractionView.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            or {
                ilike("lastName", searchName)
                ilike("firstName", searchName)
                ilike("bannerId", searchName)
            }
            projections {
                distinct(["interacteePidm","lastName","firstName","bannerId","middleName","surnamePrefix","confidential","deceased"])
            }
        }
        return results
    }

    public static findByConstituentNameOrBannerId(String nameOrBannerId) {

        def searchName = CommunicationCommonUtility.getScrubbedInput(nameOrBannerId)

        def queryCriteria = CommunicationInteractionView.createCriteria()
        def results = queryCriteria.list() {
            or {
                ilike("lastName", searchName)
                ilike("firstName", searchName)
                ilike("bannerId", searchName)
            }
            order(Order.desc("interactionDate"))
        }
        return results
    }
}

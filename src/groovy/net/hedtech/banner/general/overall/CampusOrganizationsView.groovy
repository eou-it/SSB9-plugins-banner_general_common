/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.query.DynamicFinder

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.Table

/**
 * Campus Organizations View
 */
@Entity
@Table(name = "SVQ_CAMP_ORG_GUID")
@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includeFields = true)
@NamedQueries(value = [
        @NamedQuery(name = "CampusOrganizationsView.fetchByGuid",
                query = """FROM CampusOrganizationsView a
                    where a.id = :guid""")
])

class CampusOrganizationsView implements Serializable {

    /**
     * GUID of an Campus Organizations
     */
    @Id
    @Column(name = "CAMP_ORG_GUID")
    String id

    /**
     * Description of Campus Organizations
     */
    @Column(name = "CAMP_ORG_DESC")
    String campusOrgDesc

    /**
     * Type (GUID) of Campus Organizations
     */
    @Column(name = "CAMP_ORG_TYPE_GUID")
    String campusOrgTypeGuid

    /**
     * Code of Campus Organizations
     */
    @Column(name = "CAMP_ORG_CODE")
    String campusOrgCode


    public static CampusOrganizationsView fetchByGuid(String guid) {
        CampusOrganizationsView campusOrganizationsView
        CampusOrganizationsView.withSession {
            session ->
                campusOrganizationsView = session.getNamedQuery('CampusOrganizationsView.fetchByGuid').setString('guid', guid).uniqueResult()
        }
        return campusOrganizationsView
    }


    def static countAll(filterData) {
        return finderByAll().count(filterData)
    }


    def static fetchSearch(filterData, pagingAndSortParams) {
        return finderByAll().find(filterData, pagingAndSortParams)

    }


    def private static finderByAll = {
        def query = "from CampusOrganizationsView a where 1 = 1"
        return new DynamicFinder(CampusOrganizationsView.class, query, "a")
    }

}

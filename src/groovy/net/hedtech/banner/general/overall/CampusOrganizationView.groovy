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
                query = """FROM CampusOrganizationView a
                    where a.id = :guid"""),
        @NamedQuery(name = "CampusOrganizationsView.fetchByCode",
                query = """FROM CampusOrganizationView a
                    where a.campusOrgCode = :code""")
])

class CampusOrganizationView implements Serializable {

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

}

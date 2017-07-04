/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.system

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import javax.persistence.*

/**
 * EducationalInstitution View
 */
@Entity
@Table(name = "SVQ_EDU_INST")
@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includeFields = true)

class EducationalInstitutionView implements Serializable{

    /**
     * GUID of an Institution
     */
    @Id
    @Column(name="EDU_INST_GUID")
    String id

    /**
     * Title of an Institution
     */
    @Column(name="EDU_INST_TITLE")
    String title

    /**
     * Type of an Institution
     */
    @Column(name="EDU_INST_TYPE")
    String type

    /**
     * Home Institution of an Institution
     */
    @Column(name="EDU_INST_HOME_INST")
    String homeInstitution

    /**
     * Address Guid of an Institution
     */
    @Column(name="EDU_INST_ADDR_GUID")
    String addressGuid

    /**
     * Source of an Institution
     */
    @Column(name="EDU_INST_SOURCE")
    String sourceTable

    /**
     * Version of SVQ_EDU_INST
     */
    @Version
    @Column(name = "EDU_INST_VERSION",  nullable = false, precision = 19)
    Long version
}

/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * Addresses Geographic Areas View
 */
@Entity
@Table(name = "SVQ_GEOGRAPHIC_AREAS_GUID")
@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includeFields = true)

class AddressGeographicAreasView {

    /**
     * GUID of an Address Geographic Areas
     */
    @Id
    @Column(name="GEOGRAPHIC_AREA_GUID")
    String id

    /**
     * PIDM or Code of person or non-person
     */
    @Column(name="PIDM_OR_CODE")
    String pidmOrCode

    /**
     * Address type of SPRADDR row the Region/Division is defined for.
     */
    @Column(name="ATYP_CODE")
    String atypCode

    /**
     * Sequence number of SPRADDR row the Region/Division is defined for.
     */
    @Column(name="ADDR_SEQNO")
    String addressSequenceNumber

    /**
     * Source details of the Geographic Areas.
     * Like, source is from SPRADDR or SOBSBGI
     */
    @Column(name="GEOGRAPHIC_AREA_SOURCE")
    String geographicAreasSource

}

/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import javax.persistence.*

/**
 * StudentChargeView
 */
@Entity
@Table(name = "SVQ_ADDRESS")
@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includeFields = true)

class AddressView implements Serializable{

    /**
     * GUID of an Address
     */
    @Id
    @Column(name="ADDRESS_GUID")
    String id

    /**
     * Street Line1 of an Address
     */
    @Column(name="ADDRESS_STREET_LINE1")
    String addressLine1

    /**
     * Street Line2 of an Address
     */
    @Column(name="ADDRESS_STREET_LINE2")
    String addressLine2

    /**
     * Street Line3 of an Address
     */
    @Column(name="ADDRESS_STREET_LINE3")
    String addressLine3

    /**
     * Street Line4 of an Address
     */
    @Column(name="ADDRESS_STREET_LINE4")
    String addressLine4

    /**
     * Country code of an Address
     */
    @Column(name="ADDRESS_COUNTRY")
    String countryCode

    /**
     * Source of an Address
     */
    @Column(name="ADDRESS_SOURCE")
    String sourceTable

    /**
     * Version of SVQ_ADDRESS
     */
    @Version
    @Column(name = "ADDRESS_VERSION",  nullable = false, precision = 19)
    Long version
}

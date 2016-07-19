/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import javax.persistence.*

/**
 * Addresses View
 */
@Entity
@Table(name = "SVQ_ADDR_GUID_DTLS")
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
     * Country Title of an Address
     */
    @Column(name="ADDRESS_COUNTRY_TITLE")
    String countryTitle

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
     * Country Region Code of an Address
     */
    @Column(name="ADDRESS_CNTRY_REGION_CODE")
    String countryRegionCode

    /**
     * Country Region Title of an Address
     */
    @Column(name="ADDRESS_CNTRY_REGION_TITLE")
    String countryRegionTitle

    /**
     * Country Sub Region Code of an Address
     */
    @Column(name="ADDRESS_CNTRY_SUBREGN_CODE")
    String countrySubRegionCode

    /**
     * Country Sub Region Title of an Address
     */
    @Column(name="ADDRESS_CNTRY_SUBREGN_TITLE")
    String countrySubRegionTitle

    /**
     * Country Locality Title of an Address
     */
    @Column(name="ADDRESS_CNTRY_LOCALITY")
    String countryLocality

    /**
     * Country Postal Code Title of an Address
     */
    @Column(name="ADDRESS_CNTRY_POSTAL_CODE")
    String countryPostalCode

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

    /**
     * PIDM of person or non-person
     */
    @Column(name="ADDRESS_PIDM_OR_CODE")
    String pidmOrCode

    /**
     * ATYP Code
     */
    @Column(name="ADDRESS_ATYP_CODE")
    String atypCode

    /**
     * Sequence number
     */
    @Column(name="ADDRESS_SEQNO")
    String sequenceNumber
}

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
@NamedQueries(value = [
        @NamedQuery(name = "AddressView.fetchByGuid",
                query = """FROM AddressView a
                    where a.id = :guid""")
])

class AddressView implements Serializable {

    /**
     * GUID of an Address
     */
    @Id
    @Column(name = "ADDRESS_GUID")
    String id

    /**
     * Country Title of an Address
     */
    @Column(name = "ADDRESS_COUNTRY_TITLE")
    String countryTitle

    /**
     * Street Line1 of an Address
     */
    @Column(name = "ADDRESS_STREET_LINE1")
    String addressLine1

    /**
     * Street Line2 of an Address
     */
    @Column(name = "ADDRESS_STREET_LINE2")
    String addressLine2

    /**
     * Street Line3 of an Address
     */
    @Column(name = "ADDRESS_STREET_LINE3")
    String addressLine3

    /**
     * Street Line4 of an Address
     */
    @Column(name = "ADDRESS_STREET_LINE4")
    String addressLine4

    /**
     * Country code of an Address
     */
    @Column(name = "ADDRESS_COUNTRY")
    String countryCode

    /**
     * Country Region Code of an Address
     */
    @Column(name = "ADDRESS_CNTRY_REGION_CODE")
    String countryRegionCode

    /**
     * Country Region Title of an Address
     */
    @Column(name = "ADDRESS_CNTRY_REGION_TITLE")
    String countryRegionTitle

    /**
     * Country Sub Region Code of an Address
     */
    @Column(name = "ADDRESS_CNTRY_SUBREGN_CODE")
    String countrySubRegionCode

    /**
     * Country Sub Region Title of an Address
     */
    @Column(name = "ADDRESS_CNTRY_SUBREGN_TITLE")
    String countrySubRegionTitle

    /**
     * Country Locality Title of an Address
     */
    @Column(name = "ADDRESS_CNTRY_LOCALITY")
    String countryLocality

    /**
     * Country Postal Code Title of an Address
     */
    @Column(name = "ADDRESS_CNTRY_POSTAL_CODE")
    String countryPostalCode

    /**
     * State Code of Address
     */
    @Column(name = "ADDRESS_STAT_CODE")
    String stateCode

    /**
     * Address County code
     */
    @Column(name = "ADDRESS_CNTY_CODE")
    String countyCode

    /**
     * Source of an Address
     */
    @Column(name = "ADDRESS_SOURCE")
    String sourceTable

    /**
     * Version of SVQ_ADDRESS
     */
    @Version
    @Column(name = "ADDRESS_VERSION", nullable = false, precision = 19)
    Long version

    /**
     * PIDM of person or non-person
     */
    @Column(name = "ADDRESS_PIDM_OR_CODE")
    String pidmOrCode

    /**
     * ATYP Code
     */
    @Column(name = "ADDRESS_ATYP_CODE")
    String addressTypeCode

    /**
     * Sequence number
     */
    @Column(name = "ADDRESS_SEQNO")
    String sequenceNumber

    /**
     * Address delivery point
     */
    @Column(name = "ADDRESS_DELIVERY_POINT")
    String deliveryPoint

    /**
     * Address carrier route
     */
    @Column(name = "ADDRESS_CARRIER_ROUTE")
    String carrierRoute

    /**
     * Address correction digit
     */
    @Column(name = "ADDRESS_CORRECTION_DIGIT")
    String correctionDigit

    /**
     * Address type GUID
     */
    @Column(name = "ADDRESS_ATYP_CODE_GUID")
    String addressTypeGuid

    /**
     * Surrogate ID for SPRTELE
     */
    @Column(name = "TELEPHONE_SURROGATE_ID")
    Long telephoneId

}

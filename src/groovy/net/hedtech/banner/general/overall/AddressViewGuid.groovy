/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import javax.persistence.*

/**
 * Addresses View Guid
 */
@Entity
@Table(name = "SVQ_ADDR_GUID_DTLS_GUID")
@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includeFields = true)
class AddressViewGuid implements Serializable {

    /**
     * GUID of an Address
     */
    @Id
    @Column(name = "ADDRESS_GUID")
    String id

}

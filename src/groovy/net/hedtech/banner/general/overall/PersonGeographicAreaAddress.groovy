/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.system.AddressSource
import net.hedtech.banner.general.system.AddressType
import net.hedtech.banner.general.system.County
import net.hedtech.banner.general.system.GeographicDivision
import net.hedtech.banner.general.system.GeographicRegion
import net.hedtech.banner.general.system.Nation
import net.hedtech.banner.general.system.State


import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

/**
 * Person Geographic Area Address
 */
@Entity
@Table(name = "GORPGEO")
@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includeFields = true)
@NamedQueries(value = [
@NamedQuery(name = "PersonGeographicAreaAddress.fetchActivePersonGeographicAreaAddress",
        query = """FROM PersonGeographicAreaAddress a
          WHERE a.pidm = :pidm AND a.statusIndicator is null""")
])
class PersonGeographicAreaAddress {

    /**
     * Surrogate ID for GORPGEO
     */
    @Id
    @Column(name = "GORPGEO_SURROGATE_ID")
    @SequenceGenerator(name = "GORPGEO_SEQ_GEN", allocationSize = 1, sequenceName = "GORPGEO_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GORPGEO_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GORPGEO
     */
    @Version
    @Column(name = "GORPGEO_VERSION")
    Long version

    /**
     * Internal identification number of person.
     */
    @Column(name = "GORPGEO_PIDM")
    Integer pidm

    /**
     * This field assigns an internal sequence number to each address type associated with person.
     * This field does not display on screen.
     */
    @Column(name = "GORPGEO_ADDR_SEQNO")
    Integer sequenceNumber

    /**
     * A value of S indicates the geographic region/division was created from the BANNER process GORPGEO.pc and with rules defined on SORGEOR.
     * A value of U indicates the value was manually entered on the form GOAPGEO.
     */
    @Column(name = "GORPGEO_SOURCE_IND")
    String sourceIndicator

    /**
     * This field maintains the effective start date of address associated with person.
     */
    @Column(name = "GORPGEO_FROM_DATE")
    @Temporal(TemporalType.DATE)
    Date fromDate

    /**
     * This field maintains the effective end date of address associated with person.
     */
    @Column(name = "GORPGEO_TO_DATE")
    @Temporal(TemporalType.DATE)
    Date toDate

    /**
     * This field identifies if address information is inactive. Valid value is I - Inactive.
     */
    @Column(name = "GORPGEO_STATUS_IND")
    String statusIndicator

    /**
     * This field defines the most current date a record is added or changed.
     */
    @Column(name = "GORPGEO_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * Last modified by column for SPRADDR
     */
    @Column(name = "GORPGEO_USER_ID")
    String lastModifiedBy

    /**
     * DATA SOURCE: Source system that generated the data
     */
    @Column(name = "GORPGEO_DATA_ORIGIN")
    String dataOrigin


    /**
     * The Id for the User who create or update the record.
     */
    @Column(name = "GORPGEO_USERID")
    String userData

    /**
     * Address Type of Geographic Area person Address
     */
    @ManyToOne
    @JoinColumns([
            @JoinColumn(name = "GORPGEO_ATYP_CODE", referencedColumnName = "STVATYP_CODE")
    ])
    AddressType addressType

    /**
     * Foreign Key : FK_GORPGEO_INV_STVGEOR_CODE
     */
    @ManyToOne
    @JoinColumns([
            @JoinColumn(name = "GORPGEO_GEOR_CODE", referencedColumnName = "STVGEOR_CODE")
    ])
    GeographicRegion region

    /**
     * Foreign Key : FK_GORPGEO_INV_STVGEOD_CODE
     */
    @ManyToOne
    @JoinColumns([
            @JoinColumn(name = "GORPGEO_GEOD_CODE", referencedColumnName = "STVGEOD_CODE")
    ])
    GeographicDivision division


    static constraints = {
        pidm(nullable: false, min: -99999999, max: 99999999)
        sequenceNumber(nullable: true, min: -99, max: 99)
        fromDate(nullable: true)
        toDate(nullable: true)
        statusIndicator(nullable: true, maxSize: 1, inList: ["I"])
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        addressType(nullable: false)
        sourceIndicator(nullable: true)
        region(nullable: false)
        division(nullable: false)
    }

}

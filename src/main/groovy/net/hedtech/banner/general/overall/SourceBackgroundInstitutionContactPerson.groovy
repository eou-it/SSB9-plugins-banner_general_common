/*********************************************************************************
  Copyright 2010-2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode

import net.hedtech.banner.general.system.PersonType
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.query.DynamicFinder

import javax.persistence.*

/**
 * Source/Background Institution Contact Person Repeating Table
 */
@Entity
@Table(name = "SORBCNT")
@EqualsAndHashCode(includeFields = true)
class SourceBackgroundInstitutionContactPerson implements Serializable {

    /**
     * Surrogate ID for SORBCNT
     */
    @Id
    @Column(name = "SORBCNT_SURROGATE_ID")
    @SequenceGenerator(name = "SORBCNT_SEQ_GEN", allocationSize = 1, sequenceName = "SORBCNT_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORBCNT_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SORBCNT
     */
    @Version
    @Column(name = "SORBCNT_VERSION")
    Long version

    /**
     * This field identifies the name of the contact at the source.
     */
    @Column(name = "SORBCNT_NAME")
    String personName

    /**
     * This field identifies the area code associated with the telephone number of     the contact person at the source.
     */
    @Column(name = "SORBCNT_PHONE_AREA")
    String phoneArea

    /**
     * This field identifies the telephone number of the contact person at the         source.
     */
    @Column(name = "SORBCNT_PHONE_NUMBER")
    String phoneNumber

    /**
     * This field identifies the extension associated with the telephone number of     the contact person at the source.
     */
    @Column(name = "SORBCNT_PHONE_EXT")
    String phoneExtension

    /**
     * COUNTRY CODE: Telephone code that designates the region and country.
     */
    @Column(name = "SORBCNT_CTRY_CODE_PHONE")
    String countryPhone

    /**
     * This field identifies the most current date record was created or changed.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SORBCNT_ACTIVITY_DATE")
    Date lastModified

    /**
     * Last modified by column for SORBCNT
     */
    @Column(name = "SORBCNT_USER_ID")
    String lastModifiedBy

    /**
     * Data origin column for SORBCNT
     */
    @Column(name = "SORBCNT_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FK1_SORBCNT_INV_STVSBGI_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORBCNT_SBGI_CODE", referencedColumnName = "STVSBGI_CODE")
    ])
    SourceAndBackgroundInstitution sourceAndBackgroundInstitution

    /**
     * Foreign Key : FK1_SORBCNT_INV_STVPTYP_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORBCNT_PTYP_CODE", referencedColumnName = "STVPTYP_CODE")
    ])
    PersonType personType


    public String toString() {
        """SourceBackgroundInstitutionContactPerson[
					id=$id, 
					version=$version, 
					personName=$personName,
					phoneArea=$phoneArea, 
					phoneNumber=$phoneNumber, 
					phoneExtension=$phoneExtension, 
					countryPhone=$countryPhone, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin, 
					sourceAndBackgroundInstitution=$sourceAndBackgroundInstitution, 
					personType=$personType]"""
    }


    static constraints = {
        personName(nullable: false, maxSize: 230,
                validator: { field ->
                    if (field.trim().isEmpty()) {
                        return "default.null.message"
                    }
                }
        )
        phoneArea(nullable: true, maxSize: 6)
        phoneNumber(nullable: true, maxSize: 12)
        phoneExtension(nullable: true, maxSize: 10)
        countryPhone(nullable: true, maxSize: 4)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        sourceAndBackgroundInstitution(nullable: false)
        personType(nullable: true)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['sourceAndBackgroundInstitution']


    def static countAll(filterData) {
        finderByAll().count(filterData)
    }


    def static fetchSearch(filterData, pagingAndSortParams) {
        finderByAll().find(filterData, pagingAndSortParams)
    }


    def private static finderByAll = {
        def query = """ FROM SourceBackgroundInstitutionContactPerson a
	                   WHERE a.sourceAndBackgroundInstitution.code = :sourceAndBackgroundInstitutionCode
	            	"""
        return new DynamicFinder(SourceBackgroundInstitutionContactPerson.class, query, "a")
    }
}

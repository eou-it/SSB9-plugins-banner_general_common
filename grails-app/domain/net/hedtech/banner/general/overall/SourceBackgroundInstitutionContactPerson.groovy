/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.PersonType
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.query.DynamicFinder

import javax.persistence.*

/**
 * Source/Background Institution Contact Person Repeating Table
 */
@Entity
@Table(name = "SORBCNT")
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
    String name

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
					name=$name, 
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


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof SourceBackgroundInstitutionContactPerson)) return false
        SourceBackgroundInstitutionContactPerson that = (SourceBackgroundInstitutionContactPerson) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (name != that.name) return false
        if (phoneArea != that.phoneArea) return false
        if (phoneNumber != that.phoneNumber) return false
        if (phoneExtension != that.phoneExtension) return false
        if (countryPhone != that.countryPhone) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (sourceAndBackgroundInstitution != that.sourceAndBackgroundInstitution) return false
        if (personType != that.personType) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (name != null ? name.hashCode() : 0)
        result = 31 * result + (phoneArea != null ? phoneArea.hashCode() : 0)
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0)
        result = 31 * result + (phoneExtension != null ? phoneExtension.hashCode() : 0)
        result = 31 * result + (countryPhone != null ? countryPhone.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (sourceAndBackgroundInstitution != null ? sourceAndBackgroundInstitution.hashCode() : 0)
        result = 31 * result + (personType != null ? personType.hashCode() : 0)
        return result
    }


    static constraints = {
        name(nullable: false, maxSize: 230)
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
    public static readonlyProperties = ['name', 'sourceAndBackgroundInstitution']


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

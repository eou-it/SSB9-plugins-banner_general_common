/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.Degree
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.query.DynamicFinder

import javax.persistence.*

/**
 * Source/Background Institution Degrees Offered Repeating Table
 */
@Entity
@Table(name = "SORBDEG")
class SourceBackgroundInstitutionDegreesOffered implements Serializable {

    /**
     * Surrogate ID for SORBDEG
     */
    @Id
    @Column(name = "SORBDEG_SURROGATE_ID")
    @SequenceGenerator(name = "SORBDEG_SEQ_GEN", allocationSize = 1, sequenceName = "SORBDEG_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORBDEG_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SORBDEG
     */
    @Version
    @Column(name = "SORBDEG_VERSION")
    Long version

    /**
     * This field identifies the year the degrees offered by source are valid.
     */
    @Column(name = "SORBDEG_DEMO_YEAR")
    Integer demographicYear

    /**
     * This field identifies the most current date record was created or changed.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SORBDEG_ACTIVITY_DATE")
    Date lastModified

    /**
     * Last modified by column for SORBDEG
     */
    @Column(name = "SORBDEG_USER_ID")
    String lastModifiedBy

    /**
     * Data origin column for SORBDEG
     */
    @Column(name = "SORBDEG_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FK1_SORBDEG_INV_STVSBGI_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORBDEG_SBGI_CODE", referencedColumnName = "STVSBGI_CODE")
    ])
    SourceAndBackgroundInstitution sourceAndBackgroundInstitution

    /**
     * Foreign Key : FK1_SORBDEG_INV_STVDEGC_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORBDEG_DEGC_CODE", referencedColumnName = "STVDEGC_CODE")
    ])
    Degree degree


    public String toString() {
        """SourceBackgroundInstitutionDegreesOffered[
					id=$id, 
					version=$version, 
					demographicYear=$demographicYear, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin, 
					sourceAndBackgroundInstitution=$sourceAndBackgroundInstitution, 
					degree=$degree]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof SourceBackgroundInstitutionDegreesOffered)) return false
        SourceBackgroundInstitutionDegreesOffered that = (SourceBackgroundInstitutionDegreesOffered) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (demographicYear != that.demographicYear) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (sourceAndBackgroundInstitution != that.sourceAndBackgroundInstitution) return false
        if (degree != that.degree) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (demographicYear != null ? demographicYear.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (sourceAndBackgroundInstitution != null ? sourceAndBackgroundInstitution.hashCode() : 0)
        result = 31 * result + (degree != null ? degree.hashCode() : 0)
        return result
    }


    static constraints = {
        demographicYear(nullable: false, min: -9999, max: 9999)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        sourceAndBackgroundInstitution(nullable: false)
        degree(nullable: false)
    }

    //No Updates are allowed
    //Read Only fields that should be protected against update
    public static readonlyProperties = ['demographicYear', 'sourceAndBackgroundInstitution', 'degree']


    def static countAll(filterData) {
        finderByAll().count(filterData)
    }


    def static fetchSearch(filterData, pagingAndSortParams) {
        finderByAll().find(filterData, pagingAndSortParams)
    }


    def private static finderByAll = {
        def query = """ FROM SourceBackgroundInstitutionDegreesOffered a
	                   WHERE a.sourceAndBackgroundInstitution.code = :sourceAndBackgroundInstitutionCode
	            	"""
        return new DynamicFinder(SourceBackgroundInstitutionDegreesOffered.class, query, "a")
    }
}

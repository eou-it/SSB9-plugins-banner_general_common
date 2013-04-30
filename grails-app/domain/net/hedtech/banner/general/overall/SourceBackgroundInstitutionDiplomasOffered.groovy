/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.DiplomaType
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.query.DynamicFinder

import javax.persistence.*

/**
 * Source/Background Institution Diplomas Offered Repeating Table
 */
@Entity
@Table(name = "SORBDPL")
class SourceBackgroundInstitutionDiplomasOffered implements Serializable {

    /**
     * Surrogate ID for SORBDPL
     */
    @Id
    @Column(name = "SORBDPL_SURROGATE_ID")
    @SequenceGenerator(name = "SORBDPL_SEQ_GEN", allocationSize = 1, sequenceName = "SORBDPL_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORBDPL_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SORBDPL
     */
    @Version
    @Column(name = "SORBDPL_VERSION")
    Long version

    /**
     * This field identifies the year diplomas offered by source are valid.
     */
    @Column(name = "SORBDPL_DEMO_YEAR")
    Integer demographicYear

    /**
     * This field identifies the most current date record was created or changed.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SORBDPL_ACTIVITY_DATE")
    Date lastModified

    /**
     * Last modified by column for SORBDPL
     */
    @Column(name = "SORBDPL_USER_ID")
    String lastModifiedBy

    /**
     * Data origin column for SORBDPL
     */
    @Column(name = "SORBDPL_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FK1_SORBDPL_INV_STVSBGI_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORBDPL_SBGI_CODE", referencedColumnName = "STVSBGI_CODE")
    ])
    SourceAndBackgroundInstitution sourceAndBackgroundInstitution

    /**
     * Foreign Key : FK1_SORBDPL_INV_STVDPLM_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORBDPL_DPLM_CODE", referencedColumnName = "STVDPLM_CODE")
    ])
    DiplomaType diplomaType


    public String toString() {
        """SourceBackgroundInstitutionDiplomasOffered[
					id=$id, 
					version=$version, 
					demographicYear=$demographicYear, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin, 
					sourceAndBackgroundInstitution=$sourceAndBackgroundInstitution, 
					diplomaType=$diplomaType]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof SourceBackgroundInstitutionDiplomasOffered)) return false
        SourceBackgroundInstitutionDiplomasOffered that = (SourceBackgroundInstitutionDiplomasOffered) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (demographicYear != that.demographicYear) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (sourceAndBackgroundInstitution != that.sourceAndBackgroundInstitution) return false
        if (diplomaType != that.diplomaType) return false
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
        result = 31 * result + (diplomaType != null ? diplomaType.hashCode() : 0)
        return result
    }


    static constraints = {
        demographicYear(nullable: false, min: -9999, max: 9999)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        sourceAndBackgroundInstitution(nullable: false)
        diplomaType(nullable: false)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['demographicYear', 'sourceAndBackgroundInstitution', 'diplomaType']


    def static countAll(filterData) {
        finderByAll().count(filterData)
    }


    def static fetchSearch(filterData, pagingAndSortParams) {
        finderByAll().find(filterData, pagingAndSortParams)
    }


    def private static finderByAll = {
        def query = """ FROM SourceBackgroundInstitutionDiplomasOffered a
	                   WHERE a.sourceAndBackgroundInstitution.code = :sourceAndBackgroundInstitutionCode
	            	"""
        return new DynamicFinder(SourceBackgroundInstitutionDiplomasOffered.class, query, "a")
    }
}

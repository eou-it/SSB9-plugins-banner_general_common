/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.Ethnicity
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.query.DynamicFinder

import javax.persistence.*

/**
 * Source/Background Institution Ethnic Make-up Repeating Table
 */
@Entity
@Table(name = "SORBETH")
class SourceBackgroundInstitutionEthnicMakeUp implements Serializable {

    /**
     * Surrogate ID for SORBETH
     */
    @Id
    @Column(name = "SORBETH_SURROGATE_ID")
    @SequenceGenerator(name = "SORBETH_SEQ_GEN", allocationSize = 1, sequenceName = "SORBETH_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORBETH_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SORBETH
     */
    @Version
    @Column(name = "SORBETH_VERSION")
    Long version

    /**
     * This field identifies the year ethnic make-up of source is valid.
     */
    @Column(name = "SORBETH_DEMO_YEAR")
    Integer demographicYear

    /**
     * Percent of enrollment for ethnic code.
     */
    @Column(name = "SORBETH_ETHN_PERCENT")
    Integer ethnicPercent

    /**
     * This field identifies the most current date record was created or changed.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SORBETH_ACTIVITY_DATE")
    Date lastModified

    /**
     * Last modified by column for SORBETH
     */
    @Column(name = "SORBETH_USER_ID")
    String lastModifiedBy

    /**
     * Data origin column for SORBETH
     */
    @Column(name = "SORBETH_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FK1_SORBETH_INV_STVSBGI_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORBETH_SBGI_CODE", referencedColumnName = "STVSBGI_CODE")
    ])
    SourceAndBackgroundInstitution sourceAndBackgroundInstitution

    /**
     * Foreign Key : FK1_SORBETH_INV_STVETHN_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORBETH_ETHN_CODE", referencedColumnName = "STVETHN_CODE")
    ])
    Ethnicity ethnicity


    public String toString() {
        """SourceBackgroundInstitutionEthnicMakeUp[
					id=$id, 
					version=$version, 
					demographicYear=$demographicYear, 
					ethnicPercent=$ethnicPercent, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin, 
					sourceAndBackgroundInstitution=$sourceAndBackgroundInstitution, 
					ethnicity=$ethnicity]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof SourceBackgroundInstitutionEthnicMakeUp)) return false
        SourceBackgroundInstitutionEthnicMakeUp that = (SourceBackgroundInstitutionEthnicMakeUp) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (demographicYear != that.demographicYear) return false
        if (ethnicPercent != that.ethnicPercent) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (sourceAndBackgroundInstitution != that.sourceAndBackgroundInstitution) return false
        if (ethnicity != that.ethnicity) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (demographicYear != null ? demographicYear.hashCode() : 0)
        result = 31 * result + (ethnicPercent != null ? ethnicPercent.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (sourceAndBackgroundInstitution != null ? sourceAndBackgroundInstitution.hashCode() : 0)
        result = 31 * result + (ethnicity != null ? ethnicity.hashCode() : 0)
        return result
    }


    static constraints = {
        demographicYear(nullable: false, min: -9999, max: 9999)
        ethnicPercent(nullable: true, min: -999, max: 999)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        sourceAndBackgroundInstitution(nullable: false)
        ethnicity(nullable: false)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['demographicYear', 'sourceAndBackgroundInstitution', 'ethnicity']


    def static countAll(filterData) {
        finderByAll().count(filterData)
    }


    def static fetchSearch(filterData, pagingAndSortParams) {
        finderByAll().find(filterData, pagingAndSortParams)
    }


    def private static finderByAll = {
        def query = """ FROM SourceBackgroundInstitutionEthnicMakeUp a
	                   WHERE a.sourceAndBackgroundInstitution.code = :sourceAndBackgroundInstitutionCode
	            	"""
        return new DynamicFinder(SourceBackgroundInstitutionEthnicMakeUp.class, query, "a")
    }
}

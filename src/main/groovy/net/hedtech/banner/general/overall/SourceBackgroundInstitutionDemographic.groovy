/*********************************************************************************
  Copyright 2010-2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode

import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.query.DynamicFinder

import javax.persistence.*

/**
 * Source/Background Institution Demographics Repeating Table
 */
@Entity
@Table(name = "SORBDMO")
@EqualsAndHashCode(includeFields = true)
class SourceBackgroundInstitutionDemographic implements Serializable {

    /**
     * Surrogate ID for SORBDMO
     */
    @Id
    @Column(name = "SORBDMO_SURROGATE_ID")
    @SequenceGenerator(name = "SORBDMO_SEQ_GEN", allocationSize = 1, sequenceName = "SORBDMO_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORBDMO_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SORBDMO
     */
    @Version
    @Column(name = "SORBDMO_VERSION")
    Long version

    /**
     * This field identifies the year demographic information being viewed or created  is valid.
     */
    @Column(name = "SORBDMO_DEMO_YEAR")
    Integer demographicYear

    /**
     * This field identifies the total enrollment at source for the year specified.
     */
    @Column(name = "SORBDMO_ENROLLMENT")
    Integer enrollment

    /**
     * This field identifies the number of seniors enrolled at source for the year     specified.
     */
    @Column(name = "SORBDMO_NO_OF_SENIORS")
    Integer numberOfSeniors

    /**
     * This field identifies mean family income of students enrolled at source for     the year specified.
     */
    @Column(name = "SORBDMO_MEAN_FAMILY_INCOME")
    Integer meanFamilyIncome

    /**
     * This field identifies percent of student enrolled at source who intend to       pursue a college education.
     */
    @Column(name = "SORBDMO_PERC_COLLEGE_BOUND")
    Integer percentCollegeBound

    /**
     * This field identifies the most current date record was created or changed.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SORBDMO_ACTIVITY_DATE")
    Date lastModified

    /**
     * Last modified by column for SORBDMO
     */
    @Column(name = "SORBDMO_USER_ID")
    String lastModifiedBy

    /**
     * Data origin column for SORBDMO
     */
    @Column(name = "SORBDMO_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FK1_SORBDMO_INV_STVSBGI_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORBDMO_SBGI_CODE", referencedColumnName = "STVSBGI_CODE")
    ])
    SourceAndBackgroundInstitution sourceAndBackgroundInstitution


    public String toString() {
        """SourceBackgroundInstitutionDemographic[
					id=$id, 
					version=$version, 
					demographicYear=$demographicYear, 
					enrollment=$enrollment, 
					numberOfSeniors=$numberOfSeniors, 
					meanFamilyIncome=$meanFamilyIncome, 
					percentCollegeBound=$percentCollegeBound, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin, 
					sourceAndBackgroundInstitution=$sourceAndBackgroundInstitution]"""
    }


    static constraints = {
        demographicYear(nullable: false, min: -9999, max: 9999)
        enrollment(nullable: true, min: -99999, max: 99999)
        numberOfSeniors(nullable: true, min: -9999, max: 9999)
        meanFamilyIncome(nullable: true, min: -99999, max: 99999)
        percentCollegeBound(nullable: true, min: -999, max: 999)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        sourceAndBackgroundInstitution(nullable: false)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['demographicYear', 'sourceAndBackgroundInstitution']


    def static countAll(filterData) {
        finderByAll().count(filterData)
    }


    def static fetchSearch(filterData, pagingAndSortParams) {
        finderByAll().find(filterData, pagingAndSortParams)
    }


    def private static finderByAll = {
        def query = """ FROM SourceBackgroundInstitutionDemographic a
	                   WHERE a.sourceAndBackgroundInstitution.code = :sourceAndBackgroundInstitutionCode
	            	"""
        return new DynamicFinder(SourceBackgroundInstitutionDemographic.class, query, "a")
    }
}

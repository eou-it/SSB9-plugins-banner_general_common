/*********************************************************************************
  Copyright 2010-2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode

import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.general.system.TestScore
import net.hedtech.banner.query.DynamicFinder

import javax.persistence.*

/**
 * Source/Backround Institution Test Score Repeating Table
 */
@Entity
@Table(name = "SORBTST")
@EqualsAndHashCode(includeFields = true)
class SourceBackgroundInstitutionTestScore implements Serializable {

    /**
     * Surrogate ID for SORBTST
     */
    @Id
    @Column(name = "SORBTST_SURROGATE_ID")
    @SequenceGenerator(name = "SORBTST_SEQ_GEN", allocationSize = 1, sequenceName = "SORBTST_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORBTST_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SORBTST
     */
    @Version
    @Column(name = "SORBTST_VERSION")
    Long version

    /**
     * This field identifies the year mean test scores for students enrolled at        source are valid.
     */
    @Column(name = "SORBTST_DEMO_YEAR")
    Integer demographicYear

    /**
     * This field identifies the mean test score for the type of test specified by     source.
     */
    @Column(name = "SORBTST_TEST_SCORE")
    String meanTestScore

    /**
     * This field identifies the most current date record was created or changed.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SORBTST_ACTIVITY_DATE")
    Date lastModified

    /**
     * Last modified by column for SORBTST
     */
    @Column(name = "SORBTST_USER_ID")
    String lastModifiedBy

    /**
     * Data origin column for SORBTST
     */
    @Column(name = "SORBTST_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FK1_SORBTST_INV_STVSBGI_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORBTST_SBGI_CODE", referencedColumnName = "STVSBGI_CODE")
    ])
    SourceAndBackgroundInstitution sourceAndBackgroundInstitution

    /**
     * Foreign Key : FK1_SORBTST_INV_STVTESC_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORBTST_TESC_CODE", referencedColumnName = "STVTESC_CODE")
    ])
    TestScore testScore


    public String toString() {
        """SourceBackgroundInstitutionTestScore[
					id=$id,
					version=$version,
					demographicYear=$demographicYear,
					meanTestScore=$meanTestScore,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					sourceAndBackgroundInstitution=$sourceAndBackgroundInstitution,
					testScore=$testScore]"""
    }


    static constraints = {
        demographicYear(nullable: false, min: -9999, max: 9999)
        meanTestScore(nullable: false, maxSize: 15)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        sourceAndBackgroundInstitution(nullable: false)
        testScore(nullable: false)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['demographicYear', 'sourceAndBackgroundInstitution', 'testScore']


    def static countAll(filterData) {
        finderByAll().count(filterData)
    }


    def static fetchSearch(filterData, pagingAndSortParams) {
        finderByAll().find(filterData, pagingAndSortParams)
    }


    def private static finderByAll = {
        def query = """ FROM SourceBackgroundInstitutionTestScore a
	                   WHERE a.sourceAndBackgroundInstitution.code = :sourceAndBackgroundInstitutionCode
	            	"""
        return new DynamicFinder(SourceBackgroundInstitutionTestScore.class, query, "a")
    }
}

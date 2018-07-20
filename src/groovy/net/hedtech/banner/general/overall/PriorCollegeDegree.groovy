/*********************************************************************************
  Copyright 2010-2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import net.hedtech.banner.general.system.College
import net.hedtech.banner.general.system.Degree
import net.hedtech.banner.general.system.InstitutionalHonor
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.query.DynamicFinder

import javax.persistence.*

/**
 * Prior College Degree Table
 */
@NamedQueries(value = [
@NamedQuery(
        name = "PriorCollegeDegree.fetchNextDegreeSequenceNumber",
        query = """SELECT NVL(MAX(a.degreeSequenceNumber),0) + 1
                     FROM PriorCollegeDegree a
                    WHERE a.pidm = :pidm
                      AND a.sourceAndBackgroundInstitution.code = :sourceAndBackgroundInstitutionCode""")
,
@NamedQuery(
        name = "PriorCollegeDegree.fetchByPidmAndSourceAndBackgroundInstitution",
        query = """  FROM PriorCollegeDegree a
                    WHERE a.pidm = :pidm
                      AND a.sourceAndBackgroundInstitution.code = :sourceAndBackgroundInstitutionCode"""),

@NamedQuery(
        name = "PriorCollegeDegree.fetchByPidm",
        query = """  FROM PriorCollegeDegree a
                    WHERE a.pidm = :pidm"""),
@NamedQuery(
        name = "PriorCollegeDegree.fetchByPidmList",
        query = """  FROM PriorCollegeDegree a
                    WHERE a.pidm IN :pidm""")
])

@Entity
@Table(name = "SV_SORDEGR")
@EqualsAndHashCode(includeFields = true)
class PriorCollegeDegree implements Serializable {

    /**
     * Surrogate ID for SORDEGR
     */
    @Id
    @Column(name = "SORDEGR_SURROGATE_ID")
    @SequenceGenerator(name = "SORDEGR_SEQ_GEN", allocationSize = 1, sequenceName = "SORDEGR_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORDEGR_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SORDEGR
     */
    @Version
    @Column(name = "SORDEGR_VERSION")
    Long version

    /**
     * DEGREE SEQUENCE NUMBER: A unique sequence number assigned to the prior college degree
     */
    @Column(name = "SORDEGR_DEGR_SEQ_NO")
    Integer degreeSequenceNumber

    /**
     * ATTEND TO:  The last date of attendance at the prior college
     */
    @Column(name = "SORDEGR_ATTEND_FROM")
    @Temporal(TemporalType.DATE)
    Date attendenceFrom

    /**
     * ATTEND FROM: The first date of attendance at the prior college
     */
    @Column(name = "SORDEGR_ATTEND_TO")
    @Temporal(TemporalType.DATE)
    Date attendenceTo

    /**
     * HOURS TRANSFERRED: The total number of hours transferred from the prior college.  This field is informational and does not update the transfer GPA in Academic History
     */
    @Column(name = "SORDEGR_HOURS_TRANSFERRED")
    Double hoursTransferred

    /**
     * GPA TRANSFERRED: The transfer GPA.  This is informational and does not update the transfer GPA in Academic History
     */
    @Column(name = "SORDEGR_GPA_TRANSFERRED")
    BigDecimal gpaTransferred

    /**
     * DEGREE DATE: Prior college degree date
     */
    @Column(name = "SORDEGR_DEGC_DATE")
    @Temporal(TemporalType.DATE)
    Date degreeDate

    /**
     * DEGREE YEAR: Prior college degree year
     */
    @Column(name = "SORDEGR_DEGC_YEAR")
    String degreeYear

    /**
     * TERM DEGREE: Terminal degree indicator
     */
    @Column(name = "SORDEGR_TERM_DEGREE")
    String termDegree

    /**
     * PRIMARY INDICATOR: Primary School Indicator
     */
    @Column(name = "SORDEGR_PRIMARY_IND")
    String primaryIndicator

    /**
     * ACTIVITY DATE: Most current date record was created or changed
     */
    @Column(name = "SORDEGR_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: The Oracle ID of the user who changed the record
     */
    @Column(name = "SORDEGR_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the row
     */
    @Column(name = "SORDEGR_DATA_ORIGIN")
    String dataOrigin

    /**
     * PIDM: Internal identification number of the student
     */
    @Column(name = "SORDEGR_PIDM")
    Integer pidm

    /**
     * Foreign Key : FK1_SORDEGR_INV_STVSBGI_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORDEGR_SBGI_CODE", referencedColumnName = "STVSBGI_CODE")
    ])
    SourceAndBackgroundInstitution sourceAndBackgroundInstitution

    /**
     * Foreign Key : FK1_SORDEGR_INV_STVDEGC_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORDEGR_DEGC_CODE", referencedColumnName = "STVDEGC_CODE")
    ])
    Degree degree

    /**
     * Foreign Key : FK1_SORDEGR_INV_STVCOLL_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORDEGR_COLL_CODE", referencedColumnName = "STVCOLL_CODE")
    ])
    College college

    /**
     * Foreign Key : FK1_SORDEGR_INV_STVHONR_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORDEGR_HONR_CODE", referencedColumnName = "STVHONR_CODE")
    ])
    InstitutionalHonor institutionalHonor

    /**
     * Foreign Key : FKV_SORDEGR_INV_STVEGOL_CODE;
     * Not enforced.  This was removed so this plugin is not dependent on student validation
     */
    @Column(name = "SORDEGR_EGOL_CODE")
    String educationGoal

    @Transient
    Integer tempDegreeSeqNo = 0


    public String toString() {
        """PriorCollegeDegree[
					id=$id,
					version=$version,
                    pidm=$pidm,
					degreeSequenceNumber=$degreeSequenceNumber,
					attendenceFrom=$attendenceFrom,
					attendenceTo=$attendenceTo,
					hoursTransferred=$hoursTransferred,
					gpaTransferred=$gpaTransferred,
					degreeDate=$degreeDate,
					degreeYear=$degreeYear,
					termDegree=$termDegree,
					primaryIndicator=$primaryIndicator,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					sourceAndBackgroundInstitution=$sourceAndBackgroundInstitution,
					degree=$degree,
					college=$college,
					institutionalHonor=$institutionalHonor,
					educationGoal=$educationGoal]"""
    }


    static constraints = {
        pidm(nullable: false, min: -99999999, max: 99999999)
        degreeSequenceNumber(nullable: false, min: -99, max: 99)
        attendenceFrom(nullable: true)
        attendenceTo(nullable: true)
        hoursTransferred(nullable: true, scale: 3, min: 0.0D, max: 99999999.999D)
        gpaTransferred(nullable: true, scale: 9, min: 0.0G, max: 99999999999999.999999999G)
        degreeDate(nullable: true)
        degreeYear(nullable: true, maxSize: 4)
        termDegree(nullable: true, maxSize: 1)
        primaryIndicator(nullable: true, maxSize: 1, inList: ["Y", "N"])
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        sourceAndBackgroundInstitution(nullable: false)
        degree(nullable: true)
        college(nullable: true)
        institutionalHonor(nullable: true)
        educationGoal(nullable: true, maxSize: 2)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['pidm', 'sourceAndBackgroundInstitution']


    static def fetchNextDegreeSequenceNumber(Integer pidm, String sourceAndBackgroundInstitutionCode) {
        def nextSequenceNumber = PriorCollegeDegree.withSession { session ->
            session.getNamedQuery('PriorCollegeDegree.fetchNextDegreeSequenceNumber')
                    .setInteger('pidm', pidm)
                    .setString('sourceAndBackgroundInstitutionCode', sourceAndBackgroundInstitutionCode)
                    .list()
        }
        return nextSequenceNumber[0]
    }


    static def fetchNextDegreeSequenceNumber(Integer pidm, SourceAndBackgroundInstitution sourceAndBackgroundInstitution) {
        return fetchNextDegreeSequenceNumber(pidm, sourceAndBackgroundInstitution.code)
    }


    static def fetchByPidmAndSourceAndBackgroundInstitution(Integer pidm, String sourceAndBackgroundInstitutionCode) {
        def priorCollegeDegrees = PriorCollegeDegree.withSession { session ->
            session.getNamedQuery('PriorCollegeDegree.fetchByPidmAndSourceAndBackgroundInstitution')
                    .setInteger('pidm', pidm)
                    .setString('sourceAndBackgroundInstitutionCode', sourceAndBackgroundInstitutionCode)
                    .list()
        }
        return priorCollegeDegrees
    }


    static def fetchByPidmAndSourceAndBackgroundInstitution(Integer pidm, SourceAndBackgroundInstitution sourceAndBackgroundInstitution) {
        return fetchByPidmAndSourceAndBackgroundInstitution(pidm, sourceAndBackgroundInstitution.code)
    }


    static List<PriorCollegeDegree> fetchByPidm(Integer pidm) {
        def priorCollegeDegree = PriorCollegeDegree.withSession { session ->
            session.getNamedQuery('PriorCollegeDegree.fetchByPidm').setInteger('pidm', pidm).list()
        }

        return priorCollegeDegree
    }


    static List<PriorCollegeDegree> fetchByPidmList(List pidm) {
        def priorCollegeDegree = PriorCollegeDegree.withSession { session ->
            session.getNamedQuery('PriorCollegeDegree.fetchByPidmList').setParameterList('pidm', pidm).list()
        }

        return priorCollegeDegree
    }


    def static countAll(filterData) {
        finderByAll().count(filterData)
    }


    def static fetchSearch(filterData, pagingAndSortParams) {
        finderByAll().find(filterData, pagingAndSortParams)
    }


    def private static finderByAll = {
        def query = """ FROM PriorCollegeDegree a
	                   WHERE a.pidm = :pidm
	                     AND a.sourceAndBackgroundInstitution.code = :sourceAndBackgroundInstitutionCode
	            	"""
        return new DynamicFinder(PriorCollegeDegree.class, query, "a")
    }
}

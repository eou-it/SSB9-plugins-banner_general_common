/*********************************************************************************
  Copyright 2010-2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import net.hedtech.banner.general.system.Degree
import net.hedtech.banner.general.system.MajorMinorConcentration
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution

import javax.persistence.*

/**
 * Prior college major repeating table
 */
@NamedQueries(value = [
@NamedQuery(
        name = "PriorCollegeMajor.fetchByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree",
        query = """  FROM PriorCollegeMajor a
                    WHERE a.pidm = :pidm
                      AND a.sourceAndBackgroundInstitution.code = :sourceAndBackgroundInstitutionCode
                      AND a.degreeSequenceNumber = :degreeSequenceNumber
                      AND a.degree.code = :degreeCode"""),
        @NamedQuery(
                name = "PriorCollegeMajor.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree",
                query = """  FROM PriorCollegeMajor a
                    WHERE a.pidm in (:pidmList)
                      AND a.sourceAndBackgroundInstitution.code in (:sourceAndBackgroundInstitutionCodeList)
                      AND a.degreeSequenceNumber in (:degreeSequenceNumberList)
                      AND a.degree.code in (:degreeCodeList)""")
])

@Entity
@Table(name = "SV_SORMAJR")
@EqualsAndHashCode(includeFields = true)
class PriorCollegeMajor implements Serializable {

    /**
     * Surrogate ID for SORMAJR
     */
    @Id
    @Column(name = "SORMAJR_SURROGATE_ID")
    @SequenceGenerator(name = "SORMAJR_SEQ_GEN", allocationSize = 1, sequenceName = "SORMAJR_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORMAJR_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SORMAJR
     */
    @Version
    @Column(name = "SORMAJR_VERSION")
    Long version

    /**
     * PIDM: The pidm of the person
     */
    @Column(name = "SORMAJR_PIDM")
    Integer pidm

    /**
     * DEGREE SEQUENCE NUMBER: Prior college degree seq no
     */
    @Column(name = "SORMAJR_DEGR_SEQ_NO")
    Integer degreeSequenceNumber

    /**
     * ACTIVITY DATE: Most current date record was created or changed
     */
    @Column(name = "SORMAJR_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: The Oracle user ID of the user who changed the record
     */
    @Column(name = "SORMAJR_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the row
     */
    @Column(name = "SORMAJR_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FK1_SORMAJR_INV_STVSBGI_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORMAJR_SBGI_CODE", referencedColumnName = "STVSBGI_CODE")
    ])
    SourceAndBackgroundInstitution sourceAndBackgroundInstitution

    /**
     * Foreign Key : FK1_SORMAJR_INV_STVDEGC_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORMAJR_DEGC_CODE", referencedColumnName = "STVDEGC_CODE")
    ])
    Degree degree

    /**
     * Foreign Key : FK1_SORMAJR_INV_STVMAJR_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORMAJR_MAJR_CODE_MAJOR", referencedColumnName = "STVMAJR_CODE")
    ])
    MajorMinorConcentration majorMinorConcentrationMajor


    public String toString() {
        """PriorCollegeMajor[
					id=$id,
					version=$version,
					pidm=$pidm,
					degreeSequenceNumber=$degreeSequenceNumber,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					sourceAndBackgroundInstitution=$sourceAndBackgroundInstitution,
					degree=$degree,
					majorMinorConcentrationMajor=$majorMinorConcentrationMajor]"""
    }


    static constraints = {
        pidm(nullable: false, min: -99999999, max: 99999999)
        degreeSequenceNumber(nullable: false, min: -99, max: 99)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        sourceAndBackgroundInstitution(nullable: false)
        degree(nullable: true)
        majorMinorConcentrationMajor(nullable: false)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['pidm', 'sourceAndBackgroundInstitution']


    static def fetchByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(
            Integer pidm,
            String sourceAndBackgroundInstitutionCode,
            Integer degreeSequenceNumber,
            String degreeCode) {

        def priorCollegeMajors = PriorCollegeMajor.withSession { session ->
            session.getNamedQuery('PriorCollegeMajor.fetchByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree')
                    .setInteger('pidm', pidm)
                    .setString('sourceAndBackgroundInstitutionCode', sourceAndBackgroundInstitutionCode)
                    .setInteger('degreeSequenceNumber', degreeSequenceNumber)
                    .setString('degreeCode', degreeCode)
                    .list()
        }
        return priorCollegeMajors
    }


    static def fetchByPidmAndSourceAndBackgroundInstitution(Integer pidm, SourceAndBackgroundInstitution sourceAndBackgroundInstitution) {
        return fetchByPidmAndSourceAndBackgroundInstitution(pidm, sourceAndBackgroundInstitution.code)
    }

}

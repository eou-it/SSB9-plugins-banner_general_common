/*********************************************************************************
  Copyright 2010-2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.Degree
import net.hedtech.banner.general.system.MajorMinorConcentration
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution

import javax.persistence.*

/**
 * Prior college concentration area repeating table
 */
@NamedQueries(value = [
@NamedQuery(
        name = "PriorCollegeConcentrationArea.fetchByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree",
        query = """  FROM PriorCollegeConcentrationArea a
                    WHERE a.pidm = :pidm
                      AND a.sourceAndBackgroundInstitution.code = :sourceAndBackgroundInstitutionCode
                      AND a.degreeSequenceNumber = :degreeSequenceNumber
                      AND a.degree.code = :degreeCode"""),
        @NamedQuery(
                name = "PriorCollegeConcentrationArea.fetchAllByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree",
                query = """  FROM PriorCollegeConcentrationArea a
                    WHERE a.pidm in (:pidmList)
                      AND a.sourceAndBackgroundInstitution.code in (:sourceAndBackgroundInstitutionCodeList)
                      AND a.degreeSequenceNumber in (:degreeSequenceNumberList)
                      AND a.degree.code in (:degreeCodeList)""")
])

@Entity
@Table(name = "SV_SORCONC")
class PriorCollegeConcentrationArea implements Serializable {

    /**
     * Surrogate ID for SORCONC
     */
    @Id
    @Column(name = "SORCONC_SURROGATE_ID")
    @SequenceGenerator(name = "SORCONC_SEQ_GEN", allocationSize = 1, sequenceName = "SORCONC_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORCONC_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SORCONC
     */
    @Version
    @Column(name = "SORCONC_VERSION")
    Long version

    /**
     * PIDM: The pidm of the person
     */
    @Column(name = "SORCONC_PIDM")
    Integer pidm

    /**
     * DEGREE SEQUENCE NUMBER: Prior college degree seq no
     */
    @Column(name = "SORCONC_DEGR_SEQ_NO")
    Integer degreeSequenceNumber

    /**
     * ACTIVITY DATE: Most current date record was created or changed
     */
    @Column(name = "SORCONC_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: The Oracle user ID of the user who changed the record
     */
    @Column(name = "SORCONC_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the row
     */
    @Column(name = "SORCONC_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FK1_SORCONC_INV_STVSBGI_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORCONC_SBGI_CODE", referencedColumnName = "STVSBGI_CODE")
    ])
    SourceAndBackgroundInstitution sourceAndBackgroundInstitution

    /**
     * Foreign Key : FK1_SORCONC_INV_STVDEGC_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORCONC_DEGC_CODE", referencedColumnName = "STVDEGC_CODE")
    ])
    Degree degree

    /**
     * Foreign Key : FK1_SORCONC_INV_STVMAJR_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORCONC_MAJR_CODE_CONC", referencedColumnName = "STVMAJR_CODE")
    ])
    MajorMinorConcentration concentration


    public String toString() {
        """PriorCollegeConcentrationArea[
					id=$id,
					version=$version,
					pidm=$pidm,
					degreeSequenceNumber=$degreeSequenceNumber,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					sourceAndBackgroundInstitution=$sourceAndBackgroundInstitution,
					degree=$degree,
					concentration=$concentration]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof PriorCollegeConcentrationArea)) return false
        PriorCollegeConcentrationArea that = (PriorCollegeConcentrationArea) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (pidm != that.pidm) return false
        if (degreeSequenceNumber != that.degreeSequenceNumber) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (sourceAndBackgroundInstitution != that.sourceAndBackgroundInstitution) return false
        if (degree != that.degree) return false
        if (concentration != that.concentration) return false
//        if (beforeUpdate != that.beforeUpdate) return false;
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (pidm != null ? pidm.hashCode() : 0)
        result = 31 * result + (degreeSequenceNumber != null ? degreeSequenceNumber.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (sourceAndBackgroundInstitution != null ? sourceAndBackgroundInstitution.hashCode() : 0)
        result = 31 * result + (degree != null ? degree.hashCode() : 0)
        result = 31 * result + (concentration != null ? concentration.hashCode() : 0)
//        result = 31 * result + (beforeUpdate != null ? beforeUpdate.hashCode() : 0);
        return result
    }


    static constraints = {
        pidm(nullable: false, min: -99999999, max: 99999999)
        degreeSequenceNumber(nullable: false, min: -99, max: 99)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        sourceAndBackgroundInstitution(nullable: false)
        degree(nullable: true)
        concentration(nullable: false)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['pidm', 'sourceAndBackgroundInstitution']


    static def fetchByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(
            Integer pidm,
            String sourceAndBackgroundInstitutionCode,
            Integer degreeSequenceNumber,
            String degreeCode) {

        def priorCollegeConcentrationAreas = PriorCollegeConcentrationArea.withSession { session ->
            session.getNamedQuery('PriorCollegeConcentrationArea.fetchByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree')
                    .setInteger('pidm', pidm)
                    .setString('sourceAndBackgroundInstitutionCode', sourceAndBackgroundInstitutionCode)
                    .setInteger('degreeSequenceNumber', degreeSequenceNumber)
                    .setString('degreeCode', degreeCode)
                    .list()
        }
        return priorCollegeConcentrationAreas
    }

}

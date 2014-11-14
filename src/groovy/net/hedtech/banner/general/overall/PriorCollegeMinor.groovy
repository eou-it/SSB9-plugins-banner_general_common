/*********************************************************************************
  Copyright 2010-2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.Degree
import net.hedtech.banner.general.system.MajorMinorConcentration
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution

import javax.persistence.*

/**
 * Prior college minor repeating table
 */
@NamedQueries(value = [
@NamedQuery(
        name = "PriorCollegeMinor.fetchByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree",
        query = """  FROM PriorCollegeMinor a
                    WHERE a.pidm = :pidm
                      AND a.sourceAndBackgroundInstitution.code = :sourceAndBackgroundInstitutionCode
                      AND a.degreeSequenceNumber = :degreeSequenceNumber
                      AND a.degree.code = :degreeCode""")
])

@Entity
@Table(name = "SV_SORMINR")
class PriorCollegeMinor implements Serializable {

    /**
     * Surrogate ID for SORMINR
     */
    @Id
    @Column(name = "SORMINR_SURROGATE_ID")
    @SequenceGenerator(name = "SORMINR_SEQ_GEN", allocationSize = 1, sequenceName = "SORMINR_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORMINR_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SORMINR
     */
    @Version
    @Column(name = "SORMINR_VERSION")
    Long version

    /**
     * PIDM: The pidm of the person
     */
    @Column(name = "SORMINR_PIDM")
    Integer pidm

    /**
     * DEGREE SEQUENCE NUMBER: Prior college degree seq no
     */
    @Column(name = "SORMINR_DEGR_SEQ_NO")
    Integer degreeSequenceNumber

    /**
     * ACTIVITY DATE: Most current date record was created or changed
     */
    @Column(name = "SORMINR_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: The Oracle user ID of the user who changed the record
     */
    @Column(name = "SORMINR_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the row
     */
    @Column(name = "SORMINR_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FK1_SORMINR_INV_STVSBGI_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORMINR_SBGI_CODE", referencedColumnName = "STVSBGI_CODE")
    ])
    SourceAndBackgroundInstitution sourceAndBackgroundInstitution

    /**
     * Foreign Key : FK1_SORMINR_INV_STVDEGC_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORMINR_DEGC_CODE", referencedColumnName = "STVDEGC_CODE")
    ])
    Degree degree

    /**
     * Foreign Key : FK1_SORMINR_INV_STVMAJR_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORMINR_MAJR_CODE_MINOR", referencedColumnName = "STVMAJR_CODE")
    ])
    MajorMinorConcentration majorMinorConcentrationMinor


    public String toString() {
        """PriorCollegeMinor[
					id=$id,
					version=$version,
					pidm=$pidm,
					degreeSequenceNumber=$degreeSequenceNumber,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					sourceAndBackgroundInstitution=$sourceAndBackgroundInstitution,
					degree=$degree,
					majorMinorConcentrationMinor=$majorMinorConcentrationMinor]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof PriorCollegeMinor)) return false
        PriorCollegeMinor that = (PriorCollegeMinor) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (pidm != that.pidm) return false
        if (degreeSequenceNumber != that.degreeSequenceNumber) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (sourceAndBackgroundInstitution != that.sourceAndBackgroundInstitution) return false
        if (degree != that.degree) return false
        if (majorMinorConcentrationMinor != that.majorMinorConcentrationMinor) return false
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
        result = 31 * result + (majorMinorConcentrationMinor != null ? majorMinorConcentrationMinor.hashCode() : 0)
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
        majorMinorConcentrationMinor(nullable: false)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['pidm', 'sourceAndBackgroundInstitution']


    static def fetchByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree(
            Integer pidm,
            String sourceAndBackgroundInstitutionCode,
            Integer degreeSequenceNumber,
            String degreeCode) {

        def priorCollegeMinors = PriorCollegeMinor.withSession { session ->
            session.getNamedQuery('PriorCollegeMinor.fetchByPidmAndSourceAndBackgroundInstitutionAndDegreeSequenceNumberAndDegree')
                    .setInteger('pidm', pidm)
                    .setString('sourceAndBackgroundInstitutionCode', sourceAndBackgroundInstitutionCode)
                    .setInteger('degreeSequenceNumber', degreeSequenceNumber)
                    .setString('degreeCode', degreeCode)
                    .list()
        }
        return priorCollegeMinors
    }

}

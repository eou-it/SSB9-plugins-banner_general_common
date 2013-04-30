/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.SourceAndBackgroundInstitution

import javax.persistence.*

/**
 * Source/Background Institution Academic Repeating Table
 */
@Entity
@Table(name = "SORBACD")
class SourceBackgroundInstitutionAcademic implements Serializable {

    /**
     * Surrogate ID for SORBACD
     */
    @Id
    @Column(name = "SORBACD_SURROGATE_ID")
    @SequenceGenerator(name = "SORBACD_SEQ_GEN", allocationSize = 1, sequenceName = "SORBACD_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORBACD_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SORBACD
     */
    @Version
    @Column(name = "SORBACD_VERSION")
    Long version

    /**
     * This field identifies the year associated with source demographic               information.
     */
    @Column(name = "SORBACD_DEMO_YEAR")
    Integer demographicYear

    /**
     * This field identifies whether the source is a state-approved institution.
     */
    @Column(name = "SORBACD_STATE_APPROV_IND")
    String stateApprovIndicator

    /**
     * This field identifies the type of calendar under which the source operates.
     */
    @Column(name = "SORBACD_CALENDAR_TYPE")
    String calendarType

    /**
     * This field identifies the type of accredition source holds.
     */
    @Column(name = "SORBACD_ACCREDITATION_TYPE")
    String accreditationType

    /**
     * This field identifies the value at your institution for each credit earned at   the source.
     */
    @Column(name = "SORBACD_CREDIT_TRANS_VALUE")
    Double creditTransactionValue

    /**
     * This field identifies the most current date record was created or changed.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SORBACD_ACTIVITY_DATE")
    Date lastModified

    /**
     * Last modified by column for SORBACD
     */
    @Column(name = "SORBACD_USER_ID")
    String lastModifiedBy

    /**
     * Data origin column for SORBACD
     */
    @Column(name = "SORBACD_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FK1_SORBACD_INV_STVSBGI_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORBACD_SBGI_CODE", referencedColumnName = "STVSBGI_CODE")
    ])
    SourceAndBackgroundInstitution sourceAndBackgroundInstitution


    public String toString() {
        """SourceBackgroundInstitutionAcademic[
					id=$id, 
					version=$version, 
					demographicYear=$demographicYear, 
					stateApprovIndicator=$stateApprovIndicator, 
					calendarType=$calendarType, 
					accreditationType=$accreditationType, 
					creditTransactionValue=$creditTransactionValue, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin, 
					sourceAndBackgroundInstitution=$sourceAndBackgroundInstitution]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof SourceBackgroundInstitutionAcademic)) return false
        SourceBackgroundInstitutionAcademic that = (SourceBackgroundInstitutionAcademic) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (demographicYear != that.demographicYear) return false
        if (stateApprovIndicator != that.stateApprovIndicator) return false
        if (calendarType != that.calendarType) return false
        if (accreditationType != that.accreditationType) return false
        if (creditTransactionValue != that.creditTransactionValue) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (sourceAndBackgroundInstitution != that.sourceAndBackgroundInstitution) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (demographicYear != null ? demographicYear.hashCode() : 0)
        result = 31 * result + (stateApprovIndicator != null ? stateApprovIndicator.hashCode() : 0)
        result = 31 * result + (calendarType != null ? calendarType.hashCode() : 0)
        result = 31 * result + (accreditationType != null ? accreditationType.hashCode() : 0)
        result = 31 * result + (creditTransactionValue != null ? creditTransactionValue.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (sourceAndBackgroundInstitution != null ? sourceAndBackgroundInstitution.hashCode() : 0)
        return result
    }


    static constraints = {
        demographicYear(nullable: false, min: -9999, max: 9999)
        stateApprovIndicator(nullable: true, maxSize: 1, inList: ["Y"])
        calendarType(nullable: true, maxSize: 10)
        accreditationType(nullable: true, maxSize: 15)
        creditTransactionValue(nullable: true, scale: 1, min: -9.9D, max: 9.9D)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        sourceAndBackgroundInstitution(nullable: false)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['demographicYear', 'sourceAndBackgroundInstitution']
}

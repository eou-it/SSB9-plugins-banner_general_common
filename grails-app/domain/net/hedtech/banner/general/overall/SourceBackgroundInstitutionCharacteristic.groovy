/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.BackgroundInstitutionCharacteristic
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution

import javax.persistence.*

/**
 * Source/Background Institution Characteristics Repeating Table
 */
@Entity
@Table(name = "SORBCHR")
class SourceBackgroundInstitutionCharacteristic implements Serializable {

    /**
     * Surrogate ID for SORBCHR
     */
    @Id
    @Column(name = "SORBCHR_SURROGATE_ID")
    @SequenceGenerator(name = "SORBCHR_SEQ_GEN", allocationSize = 1, sequenceName = "SORBCHR_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORBCHR_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SORBCHR
     */
    @Version
    @Column(name = "SORBCHR_VERSION")
    Long version

    /**
     * This field identifies the calender year from the Key Block of SOABGIY.
     */
    @Column(name = "SORBCHR_DEMO_YEAR")
    Integer demographicYear

    /**
     * This field identifies the last date the SORBCHR record was updated.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SORBCHR_ACTIVITY_DATE")
    Date lastModified

    /**
     * Last modified by column for SORBCHR
     */
    @Column(name = "SORBCHR_USER_ID")
    String lastModifiedBy

    /**
     * Data origin column for SORBCHR
     */
    @Column(name = "SORBCHR_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FK1_SORBCHR_INV_STVSBGI_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORBCHR_SBGI_CODE", referencedColumnName = "STVSBGI_CODE")
    ])
    SourceAndBackgroundInstitution sourceAndBackgroundInstitution

    /**
     * Foreign Key : FK1_SORBCHR_INV_STVBCHR_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SORBCHR_BCHR_CODE", referencedColumnName = "STVBCHR_CODE")
    ])
    BackgroundInstitutionCharacteristic backgroundInstitutionCharacteristic


    public String toString() {
        """SourceBackgroundInstitutionCharacteristic[
					id=$id, 
					version=$version, 
					demographicYear=$demographicYear, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin, 
					sourceAndBackgroundInstitution=$sourceAndBackgroundInstitution, 
					backgroundInstitutionCharacteristic=$backgroundInstitutionCharacteristic]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof SourceBackgroundInstitutionCharacteristic)) return false
        SourceBackgroundInstitutionCharacteristic that = (SourceBackgroundInstitutionCharacteristic) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (demographicYear != that.demographicYear) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (sourceAndBackgroundInstitution != that.sourceAndBackgroundInstitution) return false
        if (backgroundInstitutionCharacteristic != that.backgroundInstitutionCharacteristic) return false
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
        result = 31 * result + (backgroundInstitutionCharacteristic != null ? backgroundInstitutionCharacteristic.hashCode() : 0)
        return result
    }


    static constraints = {
        demographicYear(nullable: false, min: -9999, max: 9999)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        sourceAndBackgroundInstitution(nullable: false)
        backgroundInstitutionCharacteristic(nullable: false)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['demographicYear', 'sourceAndBackgroundInstitution', 'backgroundInstitutionCharacteristic']
}

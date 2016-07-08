/*********************************************************************************
  Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
/*******************************************************************************
 Copyright 2013-2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.County
import net.hedtech.banner.general.system.Nation
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.general.system.State

import javax.persistence.*

/**
 * Source/Background Institution Base Table
 */
@NamedQueries(value = [
@NamedQuery(
name = "SourceBackgroundInstitutionBase.fetchBySourceAndBackgroundInstitution",
query = """  FROM SourceBackgroundInstitutionBase a
                    WHERE a.sourceAndBackgroundInstitution.code = :sourceAndBackgroundInstitutionCode""")
])

@Entity
@Table(name = "SOBSBGI")
class SourceBackgroundInstitutionBase implements Serializable {

    /**
     * Surrogate ID for SOBSBGI
     */
    @Id
    @Column(name = "SOBSBGI_SURROGATE_ID")
    @SequenceGenerator(name = "SOBSBGI_SEQ_GEN", allocationSize = 1, sequenceName = "SOBSBGI_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SOBSBGI_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SOBSBGI
     */
    @Version
    @Column(name = "SOBSBGI_VERSION")
    Long version

    /**
     * This field identifies the first line of the source institutions address.
     */
    @Column(name = "SOBSBGI_STREET_LINE1")
    String streetLine1

    /**
     * This field identifies the second line of the source institutions address.
     */
    @Column(name = "SOBSBGI_STREET_LINE2")
    String streetLine2

    /**
     * This field identifies the third line of the source institutions address.
     */
    @Column(name = "SOBSBGI_STREET_LINE3")
    String streetLine3

    /**
     * This field identifies the city of the source institutions address.
     */
    @Column(name = "SOBSBGI_CITY")
    String city

    /**
     * This field identifies the zip code of the source institutions address.
     */
    @Column(name = "SOBSBGI_ZIP")
    String zip

    /**
     * HOUSE NUMBER: Building or lot number on a street or in an area.
     */
    @Column(name = "SOBSBGI_HOUSE_NUMBER")
    String houseNumber

    /**
     * STREET LINE 4: This field identifies the fourth line of the source institutions address.
     */
    @Column(name = "SOBSBGI_STREET_LINE4")
    String streetLine4

    /**
     * This field identifies the most current date record was created or changed.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SOBSBGI_ACTIVITY_DATE")
    Date lastModified

    /**
     * Last modified by column for SOBSBGI
     */
    @Column(name = "SOBSBGI_USER_ID")
    String lastModifiedBy

    /**
     * Data origin column for SOBSBGI
     */
    @Column(name = "SOBSBGI_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FK1_SOBSBGI_INV_STVSBGI_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SOBSBGI_SBGI_CODE", referencedColumnName = "STVSBGI_CODE")
    ])
    SourceAndBackgroundInstitution sourceAndBackgroundInstitution

    /**
     * Foreign Key : FK1_SOBSBGI_INV_STVSTAT_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SOBSBGI_STAT_CODE", referencedColumnName = "STVSTAT_CODE")
    ])
    State state

    /**
     * Foreign Key : FK1_SOBSBGI_INV_STVCNTY_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SOBSBGI_CNTY_CODE", referencedColumnName = "STVCNTY_CODE")
    ])
    County county

    /**
     * Foreign Key : FK1_SOBSBGI_INV_STVNATN_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SOBSBGI_NATN_CODE", referencedColumnName = "STVNATN_CODE")
    ])
    Nation nation

    /**
     * Global Unique Identifier for SOBSBGI
     */
    @Column(name= "SOBSBGI_GUID")
    String addressGuid



    public String toString() {
        """SourceBackgroundInstitutionBase[
					id=$id, 
					version=$version, 
					streetLine1=$streetLine1, 
					streetLine2=$streetLine2, 
					streetLine3=$streetLine3, 
					city=$city, 
					zip=$zip, 
					houseNumber=$houseNumber, 
					streetLine4=$streetLine4, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin, 
					sourceAndBackgroundInstitution=$sourceAndBackgroundInstitution, 
					state=$state, 
					county=$county,
					nation=$nation,
                    addressGuid=$addressGuid]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof SourceBackgroundInstitutionBase)) return false
        SourceBackgroundInstitutionBase that = (SourceBackgroundInstitutionBase) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (streetLine1 != that.streetLine1) return false
        if (streetLine2 != that.streetLine2) return false
        if (streetLine3 != that.streetLine3) return false
        if (city != that.city) return false
        if (zip != that.zip) return false
        if (houseNumber != that.houseNumber) return false
        if (streetLine4 != that.streetLine4) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (sourceAndBackgroundInstitution != that.sourceAndBackgroundInstitution) return false
        if (state != that.state) return false
        if (county != that.county) return false
        if (nation != that.nation) return false
        if (addressGuid != that.addressGuid) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (streetLine1 != null ? streetLine1.hashCode() : 0)
        result = 31 * result + (streetLine2 != null ? streetLine2.hashCode() : 0)
        result = 31 * result + (streetLine3 != null ? streetLine3.hashCode() : 0)
        result = 31 * result + (city != null ? city.hashCode() : 0)
        result = 31 * result + (zip != null ? zip.hashCode() : 0)
        result = 31 * result + (houseNumber != null ? houseNumber.hashCode() : 0)
        result = 31 * result + (streetLine4 != null ? streetLine4.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (sourceAndBackgroundInstitution != null ? sourceAndBackgroundInstitution.hashCode() : 0)
        result = 31 * result + (state != null ? state.hashCode() : 0)
        result = 31 * result + (county != null ? county.hashCode() : 0)
        result = 31 * result + (nation != null ? nation.hashCode() : 0)
        result = 31 * result + (addressGuid != null ? addressGuid.hashCode() : 0)
        return result
    }


    static constraints = {
        streetLine1(nullable: true, maxSize: 75)
        streetLine2(nullable: true, maxSize: 75)
        streetLine3(nullable: true, maxSize: 75)
        city(nullable: false, maxSize: 50,
                validator: { field ->
                    if (field.trim().isEmpty()) {
                        return "default.null.message"
                    }
                }
        )
        zip(nullable: true, maxSize: 30)
        houseNumber(nullable: true, maxSize: 10)
        streetLine4(nullable: true, maxSize: 75)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        sourceAndBackgroundInstitution(nullable: false)
        state(nullable: true)
        county(nullable: true)
        nation(nullable: true)
        addressGuid(nullable: true, maxSize: 36)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['sourceAndBackgroundInstitution']


    static def fetchBySourceAndBackgroundInstitution(String sourceAndBackgroundInstitutionCode) {
        def list = SourceBackgroundInstitutionBase.withSession { session ->
            session.getNamedQuery('SourceBackgroundInstitutionBase.fetchBySourceAndBackgroundInstitution')
                    .setString('sourceAndBackgroundInstitutionCode', sourceAndBackgroundInstitutionCode)
                    .list()
        }

        if (list.size > 0)
            return list[0]
        else
            return null
    }


    static def fetchBySourceAndBackgroundInstitution(SourceAndBackgroundInstitution sourceAndBackgroundInstitution) {
        fetchBySourceAndBackgroundInstitution(sourceAndBackgroundInstitution.code)
    }
}

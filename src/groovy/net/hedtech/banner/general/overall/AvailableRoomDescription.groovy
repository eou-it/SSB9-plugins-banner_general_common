/*********************************************************************************
 Copyright 2011-2012 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.overall

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

/**
 * AvailableRoomDescription model, represents the view
 * SVQ_SLBRDEF, for fetching the Available rooms
 */
@Entity
@Table(name = "SVQ_SLBRDEF")
class AvailableRoomDescription {

    /**
     * Surrogate ID for SLBRDEF
     */
    @Id
    @Column(name = "SLBRDEF_SURROGATE_ID", precision = 19)
    Long id

    /**
     * Optimistic lock token for SLBRDEF
     */
    @Version
    @Column(name = "SLBRDEF_VERSION", nullable = false, precision = 19)
    Long version

    /**
     * Date invitee information was last created or modified.
     */
    @Column(name = "SLBRDEF_ACTIVITY_DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * Last modified by column for SLBRDEF
     */
    @Column(name = "SLBRDEF_USER_ID", length = 30, nullable = true)
    String lastModifiedBy

    /**
     * Data origin column for SLBRDEF
     */
    @Column(name = "SLBRDEF_DATA_ORIGIN", length = 30, nullable = true)
    String dataOrigin

    /**
     * This field identifies the room number associated with the room
     */
    @Column(name = "SLBRDEF_ROOM_NUMBER", nullable = false, unique = true, length = 10)
    String roomNumber

    /**
     * Term code effective date.
     */
    @Column(name = "SLBRDEF_TERM_CODE_EFF", nullable = false, unique = true, length = 6)
    String termEffective

    /**
     * This field identifies the description associated with the room
     */
    @Column(name = "SLBRDEF_DESC", length = 50)
    String description
    /**
     * This field defines the capacity of the room
     */
    @Column(name = "SLBRDEF_CAPACITY", nullable = false, precision = 5)
    Integer capacity

    /**
     * This field identifies the gender associated with the room
     */
    @Column(name = "SLBRDEF_SEX", length = 1)
    String sex

    /**
     * This field defines the room type of the building, is it a Dorm, Class, or Other Room
     */
    @Column(name = "SLBRDEF_ROOM_TYPE", nullable = false, length = 1)
    String roomType

    /**
     * building code the room belongs to.
     */
    @Column(name = "SLBBLDG_BLDG_CODE")
    String buildingCode

    /**
     *  campus the building belongs to.
     */
    @Column(name = "SLBBLDG_CAMP_CODE")
    String campusCode

    /**
     * site the building belongs to .
     */
    @Column(name = "SLBBLDG_SITE_CODE")
    String siteCode

    public String toString() {
        """AvailableRoomDescription[
					id=$id,
					version=$version,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					roomNumber=$roomNumber,
					termEffective=$termEffective
					description=$description,
					capacity=$capacity,
					sex=$sex,
					roomType=$roomType,
					buildingCode=$buildingCode,
					campusCode=$campusCode,
					siteCode=$siteCode]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof AvailableRoomDescription)) return false
        AvailableRoomDescription that = (AvailableRoomDescription) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (roomNumber != that.roomNumber) return false
        if (termEffective != that.termEffective) return false
        if (description != that.description) return false
        if (capacity != that.capacity) return false
        if (sex != that.sex) return false
        if (roomType != that.roomType) return false
        if (buildingCode != that.buildingCode) return false
        if (campusCode != that.campusCode) return false
        if (siteCode != that.siteCode) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (roomNumber != null ? roomNumber.hashCode() : 0)
        result = 31 * result + (termEffective != null ? termEffective.hashCode() : 0)
        result = 31 * result + (description != null ? description.hashCode() : 0)
        result = 31 * result + (capacity != null ? capacity.hashCode() : 0)
        result = 31 * result + (sex != null ? sex.hashCode() : 0)
        result = 31 * result + (roomType != null ? roomType.hashCode() : 0)
        result = 31 * result + (buildingCode != null ? buildingCode.hashCode() : 0)
        result = 31 * result + (campusCode != null ? campusCode.hashCode() : 0)
        result = 31 * result + (siteCode != null ? siteCode.hashCode() : 0)
        return result
    }
}
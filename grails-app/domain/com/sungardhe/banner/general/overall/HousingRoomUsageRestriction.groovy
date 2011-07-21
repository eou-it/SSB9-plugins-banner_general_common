/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
/**
 Banner Automator Version: 1.21
 Generated: Fri Jul 01 19:13:07 IST 2011
 */
package com.sungardhe.banner.general.overall

import com.sungardhe.banner.general.system.Building
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Version

/**
 * Room Usage Restriction Table
 */
/*PROTECTED REGION ID(housingroomusagerestriction_namedqueries) ENABLED START*/
//TODO: NamedQueries that needs to be ported:
/**
 * Where clause on this entity present in forms:
 * Form Name: SLARDEF
 *  where slrruse_bldg_code = :slbrdef.slbrdef_bldg_code and slrruse_room_number = :slbrdef.slbrdef_room_number

 * Order by clause on this entity present in forms:
 * Form Name: SLARDEF
 *  slrruse_start_date

 */
/*PROTECTED REGION END*/
@Entity
@Table(name = "SLRRUSE")
class HousingRoomUsageRestriction implements Serializable {

    /**
     * Surrogate ID for SLRRUSE
     */
    @Id
    @Column(name = "SLRRUSE_SURROGATE_ID")
    @SequenceGenerator(name = "SLRRUSE_SEQ_GEN", allocationSize = 1, sequenceName = "SLRRUSE_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SLRRUSE_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SLRRUSE
     */
    @Version
    @Column(name = "SLRRUSE_VERSION", nullable = false, precision = 19)
    Long version

    /**
     * This field identifies the room number of the room usage restriction
     */
    @Column(name = "SLRRUSE_ROOM_NUMBER", nullable = false, length = 10)
    String roomNumber

    /**
     * This field identifies the starting date of the room usage restriction
     */
    @Column(name = "SLRRUSE_START_DATE", nullable = false)
    Date startDate

    /**
     * This field identifies the ending date of the room usage restriction
     */
    @Column(name = "SLRRUSE_END_DATE")
    Date endDate

    /**
     * Begin time for room usage. Used by 3rd party scheduling tool.
     */
    @Column(name = "SLRRUSE_BEGIN_TIME", length = 4)
    String beginTime

    /**
     * End time for room usage. Used by 3rd party scheduling tools.
     */
    @Column(name = "SLRRUSE_END_TIME", length = 4)
    String endTime

    /**
     * Field defining to room usage on Sunday.  Used is U  Unused is NULL.
     */
    @Column(name = "SLRRUSE_SUN_DAY", length = 1)
    String sunday

    /**
     * Field defining to room usage on Monday.  Used is M  Unused is NULL.
     */
    @Column(name = "SLRRUSE_MON_DAY", length = 1)
    String monday

    /**
     * Field defining to room usage on Tuesday.  Used is T  Unused is NULL.
     */
    @Column(name = "SLRRUSE_TUE_DAY", length = 1)
    String tuesday

    /**
     * Field defining to room usage on Wednesday.  Used is W  Unused is NULL.
     */
    @Column(name = "SLRRUSE_WED_DAY", length = 1)
    String wednesday

    /**
     * Field defining to room usage on Thursday.  Used is R  Unused is NULL.
     */
    @Column(name = "SLRRUSE_THU_DAY", length = 1)
    String thursday

    /**
     * Field defining to room usage on Friday.  Used is F  Unused is NULL.
     */
    @Column(name = "SLRRUSE_FRI_DAY", length = 1)
    String friday

    /**
     * Field defining to room usage on Saturday.  Used is S  Unused is NULL.
     */
    @Column(name = "SLRRUSE_SAT_DAY", length = 1)
    String saturday

    /**
     * This field identifies the date the record was created or last updated
     */
    @Column(name = "SLRRUSE_ACTIVITY_DATE")
    Date lastModified

    /**
     * Last modified by column for SLRRUSE
     */
    @Column(name = "SLRRUSE_USER_ID", length = 30)
    String lastModifiedBy

    /**
     * Data origin column for SLRRUSE
     */
    @Column(name = "SLRRUSE_DATA_ORIGIN", length = 30)
    String dataOrigin

    /**
     * Foreign Key : FKV_SLRRUSE_INV_STVBLDG_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SLRRUSE_BLDG_CODE", referencedColumnName = "STVBLDG_CODE")
    ])
    Building building


    public String toString() {
        """HousingRoomUsageRestriction[
					id=$id,
					version=$version,
					roomNumber=$roomNumber,
					startDate=$startDate,
					endDate=$endDate,
					beginTime=$beginTime,
					endTime=$endTime,
					sunday=$sunday,
					monday=$monday,
					tuesday=$tuesday,
					wednesday=$wednesday,
					thursday=$thursday,
					friday=$friday,
					saturday=$saturday,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					building=$building]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof HousingRoomUsageRestriction)) return false
        HousingRoomUsageRestriction that = (HousingRoomUsageRestriction) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (roomNumber != that.roomNumber) return false
        if (startDate != that.startDate) return false
        if (endDate != that.endDate) return false
        if (beginTime != that.beginTime) return false
        if (endTime != that.endTime) return false
        if (sunday != that.sunday) return false
        if (monday != that.monday) return false
        if (tuesday != that.tuesday) return false
        if (wednesday != that.wednesday) return false
        if (thursday != that.thursday) return false
        if (friday != that.friday) return false
        if (saturday != that.saturday) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (building != that.building) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (roomNumber != null ? roomNumber.hashCode() : 0)
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0)
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0)
        result = 31 * result + (beginTime != null ? beginTime.hashCode() : 0)
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0)
        result = 31 * result + (sunday != null ? sunday.hashCode() : 0)
        result = 31 * result + (monday != null ? monday.hashCode() : 0)
        result = 31 * result + (tuesday != null ? tuesday.hashCode() : 0)
        result = 31 * result + (wednesday != null ? wednesday.hashCode() : 0)
        result = 31 * result + (thursday != null ? thursday.hashCode() : 0)
        result = 31 * result + (friday != null ? friday.hashCode() : 0)
        result = 31 * result + (saturday != null ? saturday.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (building != null ? building.hashCode() : 0)
        return result
    }


    static constraints = {
        roomNumber(nullable: false, maxSize: 10)
        startDate(nullable: false)
        endDate(nullable: true)
        beginTime(nullable: true, maxSize: 4)
        endTime(nullable: true, maxSize: 4)
        sunday(nullable: true, maxSize: 1)
        monday(nullable: true, maxSize: 1)
        tuesday(nullable: true, maxSize: 1)
        wednesday(nullable: true, maxSize: 1)
        thursday(nullable: true, maxSize: 1)
        friday(nullable: true, maxSize: 1)
        saturday(nullable: true, maxSize: 1)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        building(nullable: false)
        /**
         * Please put all the custom constraints in this protected section to protect the code
         * from being overwritten on re-generation
         */
        /*PROTECTED REGION ID(housingroomusagerestriction_custom_constraints) ENABLED START*/

        /*PROTECTED REGION END*/
    }

    /**
     * Please put all the custom/transient attributes with @Transient annotations in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(housingroomusagerestriction_custom_attributes) ENABLED START*/

    /*PROTECTED REGION END*/

    /**
     * Please put all the custom methods/code in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(housingroomusagerestriction_custom_methods) ENABLED START*/

    /*PROTECTED REGION END*/
}

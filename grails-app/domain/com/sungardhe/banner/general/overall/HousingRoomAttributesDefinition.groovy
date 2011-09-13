/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard, Banner and Luminis are either 
 registered trademarks or trademarks of SunGard Higher Education in the U.S.A. 
 and/or other regions and/or countries.
 **********************************************************************************/
/**
 Banner Automator Version: 1.21
 Generated: Fri Jul 01 19:13:07 IST 2011
 */
package com.sungardhe.banner.general.overall

import com.sungardhe.banner.general.system.Building
import com.sungardhe.banner.general.system.BuildingAndRoomAttribute
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import org.hibernate.annotations.Type
import javax.persistence.Version
import javax.persistence.Temporal
import javax.persistence.TemporalType

/**
 * Room Attributes Definition Table
 */
@Entity
@Table(name = "SLRRDEF")
@NamedQueries(value = [
@NamedQuery(name = "HousingRoomAttributesDefinition.fetchByBuildingRoomNumberAndTermEffective",
query = """FROM HousingRoomAttributesDefinition hrad WHERE
                 hrad.building.code = :buildingCode and
                  hrad.roomNumber = :roomNumber and hrad.termEffective = :termEffective""")
])
class HousingRoomAttributesDefinition implements Serializable {

    /**
     * Surrogate ID for SLRRDEF
     */
    @Id
    @Column(name = "SLRRDEF_SURROGATE_ID")
    @SequenceGenerator(name = "SLRRDEF_SEQ_GEN", allocationSize = 1, sequenceName = "SLRRDEF_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SLRRDEF_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SLRRDEF
     */
    @Version
    @Column(name = "SLRRDEF_VERSION", nullable = false, precision = 19)
    Long version

    /**
     * This field identifies the room number of the attributes
     */
    @Column(name = "SLRRDEF_ROOM_NUMBER", nullable = false, unique = true, length = 10)
    String roomNumber

    /**
     * Term code effective date.
     */
    @Column(name = "SLRRDEF_TERM_CODE_EFF", nullable = false, unique = true, length = 6)
    String termEffective

    /**
     * This field determines whether the attribute entered for the room requires a matching attribute for the people scheduled in the room
     */
    @Column(name = "SLRRDEF_MUST_MATCH")
    String mustMatch

    /**
     * This field identifies the date the record was created or last updated
     */
    @Column(name = "SLRRDEF_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * Last modified by column for SLRRDEF
     */
    @Column(name = "SLRRDEF_USER_ID", length = 30)
    String lastModifiedBy

    /**
     * Data origin column for SLRRDEF
     */
    @Column(name = "SLRRDEF_DATA_ORIGIN", length = 30)
    String dataOrigin

    /**
     * Foreign Key : FKV_SLRRDEF_INV_STVBLDG_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SLRRDEF_BLDG_CODE", referencedColumnName = "STVBLDG_CODE")
    ])
    Building building

    /**
     * Foreign Key : FK1_SLRRDEF_INV_STVRDEF_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SLRRDEF_RDEF_CODE", referencedColumnName = "STVRDEF_CODE")
    ])
    BuildingAndRoomAttribute buildingAndRoomAttribute


    public String toString() {
        """HousingRoomAttributesDefinition[
					id=$id,
					version=$version,
					roomNumber=$roomNumber,
					termEffective=$termEffective,
					mustMatch=$mustMatch,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					building=$building,
					buildingAndRoomAttribute=$buildingAndRoomAttribute]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof HousingRoomAttributesDefinition)) return false
        HousingRoomAttributesDefinition that = (HousingRoomAttributesDefinition) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (roomNumber != that.roomNumber) return false
        if (termEffective != that.termEffective) return false
        if (mustMatch != that.mustMatch) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (building != that.building) return false
        if (buildingAndRoomAttribute != that.buildingAndRoomAttribute) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (roomNumber != null ? roomNumber.hashCode() : 0)
        result = 31 * result + (termEffective != null ? termEffective.hashCode() : 0)
        result = 31 * result + (mustMatch != null ? mustMatch.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (building != null ? building.hashCode() : 0)
        result = 31 * result + (buildingAndRoomAttribute != null ? buildingAndRoomAttribute.hashCode() : 0)
        return result
    }


    static constraints = {
        roomNumber(nullable: false, maxSize: 10)
        termEffective(nullable: false, maxSize: 6)
        mustMatch(nullable: true, maxSize: 1)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        building(nullable: false)
        buildingAndRoomAttribute(nullable: false)
        /**
         * Please put all the custom constraints in this protected section to protect the code
         * from being overwritten on re-generation
         */
        /*PROTECTED REGION ID(housingroomattributesdefinition_custom_constraints) ENABLED START*/

        /*PROTECTED REGION END*/
    }

    /*PROTECTED REGION ID(housingroomattributesdefinition_readonly_properties) ENABLED START*/
    //Read Only fields that should be protected against update
    public static readonlyProperties = ['roomNumber', 'termEffective', 'building', 'buildingAndRoomAttribute']
    /*PROTECTED REGION END*/
    /**
     * Please put all the custom/transient attributes with @Transient annotations in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(housingroomattributesdefinition_custom_attributes) ENABLED START*/

    /*PROTECTED REGION END*/

    /**
     * Please put all the custom methods/code in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(housingroomattributesdefinition_custom_methods) ENABLED START*/


    public static List fetchByBuildingRoomNumberAndTermEffective(String buildingCode, String roomNumber, String termEffective) {
        def lst = HousingRoomAttributesDefinition.withSession { session ->
            session.getNamedQuery('HousingRoomAttributesDefinition.fetchByBuildingRoomNumberAndTermEffective').setString('buildingCode', buildingCode).setString('roomNumber', roomNumber).setString('termEffective', termEffective).list()
        }
        return lst
    }
    /*PROTECTED REGION END*/
}

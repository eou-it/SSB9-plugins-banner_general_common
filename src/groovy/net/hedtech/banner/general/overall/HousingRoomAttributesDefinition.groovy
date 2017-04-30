/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.Building
import net.hedtech.banner.general.system.BuildingAndRoomAttribute

import javax.persistence.*

/**
 * Room Attributes Definition Table
 */
@Entity
@Table(name = "SLRRDEF")
@NamedQueries(value = [
        @NamedQuery(name = "HousingRoomAttributesDefinition.fetchByBuildingRoomNumberAndTermEffective",
                query = """FROM HousingRoomAttributesDefinition hrad WHERE
                 hrad.building.code = :buildingCode and
                  hrad.roomNumber = :roomNumber and hrad.termEffective = :termEffective"""),
        @NamedQuery(name = "HousingRoomAttributesDefinition.fetchByBuildingCodeList",
                query = """FROM HousingRoomAttributesDefinition hrad WHERE
                 hrad.building.code in :buildingCodeList """),
        @NamedQuery(name = "HousingRoomAttributesDefinition.fetchAllByBuildingRoomNumberAndTermEffective",
                query = """FROM HousingRoomAttributesDefinition a WHERE
                 a.building.code in :buildingCodes and
                  a.roomNumber in :roomNumbers and a.termEffective in :termEffectives""")

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
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['roomNumber', 'termEffective', 'building', 'buildingAndRoomAttribute']


    public
    static List fetchByBuildingRoomNumberAndTermEffective(String buildingCode, String roomNumber, String termEffective) {
        def lst = HousingRoomAttributesDefinition.withSession { session ->
            session.getNamedQuery('HousingRoomAttributesDefinition.fetchByBuildingRoomNumberAndTermEffective').setString('buildingCode', buildingCode).setString('roomNumber', roomNumber).setString('termEffective', termEffective).list()
        }
        return lst
    }


}

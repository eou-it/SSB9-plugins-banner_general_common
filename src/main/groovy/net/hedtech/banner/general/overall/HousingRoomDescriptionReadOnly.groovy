/*********************************************************************************
 Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import net.hedtech.banner.general.system.Term

import javax.persistence.*

@Entity
@Table(name = "SVQ_SLBRDEF_SLBBLDG")
@EqualsAndHashCode(includeFields = true)
@NamedQueries(value = [
        @NamedQuery(name = "HousingRoomDescriptionReadOnly.fetchByGuid",
                query = """FROM  HousingRoomDescriptionReadOnly a WHERE a.roomGUID = :guid""")


])
class HousingRoomDescriptionReadOnly {

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
    @ManyToOne
    @JoinColumns([
            @JoinColumn(name = "SLBRDEF_TERM_CODE_EFF", referencedColumnName = "STVTERM_CODE", nullable = false)
    ])
    Term termEffective

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

    @Column(name = "SLBRDEF_RMST_CODE")
    String roomStatusCode

    @Column(name = "STVRMST_INACT_IND")
    String roomStatusInactiveIndicator

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

    @ManyToOne
    @JoinColumns([
            @JoinColumn(name = "TERM_TO", referencedColumnName = "STVTERM_CODE", nullable = true)
    ])
    Term termTo

    @Column(name = "ROOM_GUID")
    String roomGUID

    @Column(name = "BUILDING_GUID")
    String buildingGUID

    @Column(name = "SITE_GUID")
    String siteGUID

    public String toString() {
        """HousingRoomDescriptionReadOnly[
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
                    roomStatusCode=$roomStatusCode,
                    roomStatusInactiveIndicator=$roomStatusInactiveIndicator,
					buildingCode=$buildingCode,
					campusCode=$campusCode,
					siteCode=$siteCode,
                    roomGUID=$roomGUID,
                    buildingGUID=$buildingGUID,
                    siteGUID=$siteGUID]"""
    }

}

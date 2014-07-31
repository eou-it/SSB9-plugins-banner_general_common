/*********************************************************************************
 Copyright 2010-2013 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall
import net.hedtech.banner.general.system.*
import net.hedtech.banner.query.DynamicFinder
import net.hedtech.banner.service.DatabaseModifiesState

import javax.persistence.*
/**
 * Room Description Table.
 */
@Entity
@Table(name = "SV_SLBRDEF")
@NamedQueries(value = [
@NamedQuery(name = "HousingRoomDescription.fetchAllByBuilding",
        query = """FROM  HousingRoomDescription a
           WHERE  (a.roomNumber like :filter
	  	           OR UPPER(a.description) like :filter)
           and  a.building like  :building
           and a.roomType = 'C' and a.roomStatus.inactiveIndicator is null
           and a.termEffective =  ( select max(b.termEffective)
                   from HousingRoomDescription b
                     where  b.building =  a.building
                     and b.roomNumber = a.roomNumber
                     and b.termEffective <= :termEffective )
                     order by  a.roomNumber """),
@NamedQuery(name = "HousingRoomDescription.fetchValidateRoomAndBuilding",
        query = """FROM  HousingRoomDescription a
           WHERE a.building = :building
           and a.termEffective = ( select max(b.termEffective)
                   from HousingRoomDescription b
                      where  b.building =  :building
                     and b.roomNumber = :roomNumber
                     and b.termEffective <= :termEffective )
           and a.roomNumber = :roomNumber """),
@NamedQuery(name = "HousingRoomDescription.fetchValidateSomeRoomAndBuilding",
        query = """FROM  HousingRoomDescription a
           WHERE a.building like :building
           and a.roomNumber = :roomNumber
           and a.termEffective = ( select max(b.termEffective)
                   from HousingRoomDescription b
                      where  b.building =  a.building
                     and b.roomNumber = a.roomNumber
                     and b.termEffective <= :termEffective )
            """),
@NamedQuery(name = "HousingRoomDescription.fetchTermTo",
        query = """select NVL(MIN(termEffective),'999999') FROM  HousingRoomDescription a
           WHERE a.building.code = :buildingCode and a.roomNumber = :roomNumber  and a.termEffective > :termEffective """)

])
@DatabaseModifiesState
class HousingRoomDescription implements Serializable {

    /**
     * Surrogate ID for SLBRDEF
     */
    @Id
    @Column(name = "SLBRDEF_SURROGATE_ID")
    @SequenceGenerator(name = "SLBRDEF_SEQ_GEN", allocationSize = 1, sequenceName = "SLBRDEF_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SLBRDEF_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for SLBRDEF
     */
    @Version
    @Column(name = "SLBRDEF_VERSION", nullable = false, precision = 19)
    Long version

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
     * This field defines the maximum capacity of the room
     */
    @Column(name = "SLBRDEF_MAXIMUM_CAPACITY", precision = 5)
    Integer maximumCapacity

    /**
     * This field defines the utility rate associated with the room, this field is inf ormational only
     */
    @Column(name = "SLBRDEF_UTILITY_RATE", precision = 7, scale = 2)
    Double utilityRate

    /**
     * This field defines the time period the utility rate is associated with the room
     */
    @Column(name = "SLBRDEF_UTILITY_RATE_PERIOD", length = 2)
    String utilityRatePeriod

    /**
     * This field identifies the area code of the room
     */
    @Column(name = "SLBRDEF_PHONE_AREA", length = 6)
    String phoneArea

    /**
     * This field identifies the phone number of the room
     */
    @Column(name = "SLBRDEF_PHONE_NUMBER", length = 12)
    String phoneNumber

    /**
     * This field identifies the phone extension number of the room
     */
    @Column(name = "SLBRDEF_PHONE_EXTENSION", length = 10)
    String phoneExtension

    /**
     * This field identifies the building category associated with the room
     */
    @Column(name = "SLBRDEF_BCAT_CODE", length = 4)
    String benefitCategory

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
     * This field defines the priority of the room, it is used by the scheduler to det ermine which rooms are used first
     */
    @Column(name = "SLBRDEF_PRIORITY", length = 8)
    String priority

    /**
     * This field identifies the number of the key to the room
     */
    @Column(name = "SLBRDEF_KEY_NUMBER", length = 5)
    String keyNumber

    /**
     * This field shows the width, in feet, of the the room
     */
    @Column(name = "SLBRDEF_WIDTH", precision = 6, scale = 2)
    Double width

    /**
     * This field shows the length, in feet, of the the room
     */
    @Column(name = "SLBRDEF_LENGTH", precision = 6, scale = 2)
    Double length

    /**
     * This field shows the area, in square feet, of the room
     */
    // Note: If width and length are set, the GB_ROOMDEFINITION procedures will persist a calculated value versus one set here
    @Column(name = "SLBRDEF_AREA", precision = 10, scale = 2)
    Double area

    /**
     * COUNTRY CODE: Telephone code that designates the region and country.
     */
    @Column(name = "SLBRDEF_CTRY_CODE_PHONE", length = 4)
    String countryPhone

    /**
     * This field identifies the date the record was created or last updated
     */
    @Column(name = "SLBRDEF_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * Last modified by column for SLBRDEF
     */
    @Column(name = "SLBRDEF_USER_ID", length = 30)
    String lastModifiedBy

    /**
     * Data origin column for SLBRDEF
     */
    @Column(name = "SLBRDEF_DATA_ORIGIN", length = 30)
    String dataOrigin

    /**
     * Foreign Key : FKV_SLBRDEF_INV_STVDEPT_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SLBRDEF_DEPT_CODE", referencedColumnName = "STVDEPT_CODE")
    ])
    Department department

    /**
     * Foreign Key : FKV_SLBRDEF_INV_GTVPARS_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SLBRDEF_PARS_CODE", referencedColumnName = "GTVPARS_CODE")
    ])
    Partition partition

    /**
     * Foreign Key : FKV_SLBRDEF_INV_STVBLDG_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SLBRDEF_BLDG_CODE", referencedColumnName = "STVBLDG_CODE")
    ])
    Building building

    /**
     * Foreign Key : FK1_SLBRDEF_INV_STVRMST_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SLBRDEF_RMST_CODE", referencedColumnName = "STVRMST_CODE")
    ])
    RoomStatus roomStatus

    /**
     * Foreign Key : FKV_SLBRDEF_INV_STVRRCD_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SLBRDEF_RRCD_CODE", referencedColumnName = "STVRRCD_CODE")
    ])
    RoomRate roomRate

    /**
     * Foreign Key : FKV_SLBRDEF_INV_STVPRCD_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SLBRDEF_PRCD_CODE", referencedColumnName = "STVPRCD_CODE")
    ])
    PhoneRate phoneRate

    /**
     * Foreign Key : FKV_SLBRDEF_INV_STVCOLL_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "SLBRDEF_COLL_CODE", referencedColumnName = "STVCOLL_CODE")
    ])
    College college


    public String toString() {
        """HousingRoomDescription[
					id=$id,
					version=$version,
					roomNumber=$roomNumber,
					termEffective=$termEffective,
					description=$description,
					capacity=$capacity,
					maximumCapacity=$maximumCapacity,
					utilityRate=$utilityRate,
					utilityRatePeriod=$utilityRatePeriod,
					phoneArea=$phoneArea,
					phoneNumber=$phoneNumber,
					phoneExtension=$phoneExtension,
					benefitCategory=$benefitCategory,
					sex=$sex,
					roomType=$roomType,
					priority=$priority,
					keyNumber=$keyNumber,
					width=$width,
					length=$length,
					area=$area,
					countryPhone=$countryPhone,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					department=$department,
					partition=$partition,
					building=$building,
					roomStatus=$roomStatus,
					roomRate=$roomRate,
					phoneRate=$phoneRate,
					college=$college]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof HousingRoomDescription)) return false
        HousingRoomDescription that = (HousingRoomDescription) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (roomNumber != that.roomNumber) return false
        if (termEffective != that.termEffective) return false
        if (description != that.description) return false
        if (capacity != that.capacity) return false
        if (maximumCapacity != that.maximumCapacity) return false
        if (utilityRate != that.utilityRate) return false
        if (utilityRatePeriod != that.utilityRatePeriod) return false
        if (phoneArea != that.phoneArea) return false
        if (phoneNumber != that.phoneNumber) return false
        if (phoneExtension != that.phoneExtension) return false
        if (benefitCategory != that.benefitCategory) return false
        if (sex != that.sex) return false
        if (roomType != that.roomType) return false
        if (priority != that.priority) return false
        if (keyNumber != that.keyNumber) return false
        if (width != that.width) return false
        if (length != that.length) return false
        if (area != that.area) return false
        if (countryPhone != that.countryPhone) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (department != that.department) return false
        if (partition != that.partition) return false
        if (building != that.building) return false
        if (roomStatus != that.roomStatus) return false
        if (roomRate != that.roomRate) return false
        if (phoneRate != that.phoneRate) return false
        if (college != that.college) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (roomNumber != null ? roomNumber.hashCode() : 0)
        result = 31 * result + (termEffective != null ? termEffective.hashCode() : 0)
        result = 31 * result + (description != null ? description.hashCode() : 0)
        result = 31 * result + (capacity != null ? capacity.hashCode() : 0)
        result = 31 * result + (maximumCapacity != null ? maximumCapacity.hashCode() : 0)
        result = 31 * result + (utilityRate != null ? utilityRate.hashCode() : 0)
        result = 31 * result + (utilityRatePeriod != null ? utilityRatePeriod.hashCode() : 0)
        result = 31 * result + (phoneArea != null ? phoneArea.hashCode() : 0)
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0)
        result = 31 * result + (phoneExtension != null ? phoneExtension.hashCode() : 0)
        result = 31 * result + (benefitCategory != null ? benefitCategory.hashCode() : 0)
        result = 31 * result + (sex != null ? sex.hashCode() : 0)
        result = 31 * result + (roomType != null ? roomType.hashCode() : 0)
        result = 31 * result + (priority != null ? priority.hashCode() : 0)
        result = 31 * result + (keyNumber != null ? keyNumber.hashCode() : 0)
        result = 31 * result + (width != null ? width.hashCode() : 0)
        result = 31 * result + (length != null ? length.hashCode() : 0)
        result = 31 * result + (area != null ? area.hashCode() : 0)
        result = 31 * result + (countryPhone != null ? countryPhone.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (department != null ? department.hashCode() : 0)
        result = 31 * result + (partition != null ? partition.hashCode() : 0)
        result = 31 * result + (building != null ? building.hashCode() : 0)
        result = 31 * result + (roomStatus != null ? roomStatus.hashCode() : 0)
        result = 31 * result + (roomRate != null ? roomRate.hashCode() : 0)
        result = 31 * result + (phoneRate != null ? phoneRate.hashCode() : 0)
        result = 31 * result + (college != null ? college.hashCode() : 0)
        return result
    }


    static constraints = {
        roomNumber(nullable: false, maxSize: 10)
        termEffective(nullable: false, maxSize: 6)
        description(nullable: true, maxSize: 50)
        capacity(nullable: false, min: -99999, max: 99999)
        maximumCapacity(nullable: true, min: -99999, max: 99999)
        utilityRate(nullable: true, min: -99999.99D, max: 99999.99D)
        utilityRatePeriod(nullable: true, maxSize: 2)
        phoneArea(nullable: true, maxSize: 6)
        phoneNumber(nullable: true, maxSize: 12)
        phoneExtension(nullable: true, maxSize: 10)
        benefitCategory(nullable: true, maxSize: 4)
        sex(nullable: true, maxSize: 1)
        roomType(nullable: false, maxSize: 1)
        priority(nullable: true, maxSize: 8)
        keyNumber(nullable: true, maxSize: 5)
        width(nullable: true, min: -9999.99D, max: 9999.99D)
        length(nullable: true, min: -9999.99D, max: 9999.99D)
        area(nullable: true, min: -99999999.99D, max: 99999999.99D)
        countryPhone(nullable: true, maxSize: 4)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        department(nullable: true)
        partition(nullable: true)
        building(nullable: false)
        roomStatus(nullable: true)
        roomRate(nullable: true)
        phoneRate(nullable: true)
        college(nullable: true)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['roomNumber', 'termEffective', 'building']


    public static String fetchTermToOfRoom(String buildingCode, String roomNumber, String termEffective) {
        HousingRoomDescription.withSession { session ->
            session.getNamedQuery('HousingRoomDescription.fetchTermTo').
                    setString('buildingCode', buildingCode).
                    setString('roomNumber', roomNumber).
                    setString('termEffective', termEffective).list()[0]
        }
    }


    public static Object fetchBySomeHousingRoomDescriptionRoom() {
        def returnObj = [list: HousingRoomDescription.list().sort { it.roomNumber }]
        return returnObj
    }


    public static Object fetchBySomeHousingRoomDescriptionRoom(Map params) {
        def buildingCode
        if (params?.building?.code)
            buildingCode = "%" + params?.building?.code + "%"
        else
            buildingCode = "%"
        def rooms = HousingRoomDescription.withSession { session ->
            session.getNamedQuery('HousingRoomDescription.fetchAllByBuilding').setString('building', buildingCode).setString('termEffective', params?.termEffective).setString('filter', "%").list()
        }
        return [list: rooms]
    }


    public static Object fetchBySomeHousingRoomDescriptionRoom(String filter) {
        def rooms = HousingRoomDescription.withSession { session ->
            session.getNamedQuery('HousingRoomDescription.fetchAllByBuilding').setString('building', "%").setString('filter', "%${filter?.toUpperCase()}%").list()
        }
        return [list: rooms]
    }


    public static Object fetchBySomeHousingRoomDescriptionRoom(String filter, Map params) {
        def buildingCode
        if (params?.building?.code)
            buildingCode = "%" + params?.building?.code + "%"
        else
            buildingCode = "%"
        def rooms = HousingRoomDescription.withSession { session ->
            session.getNamedQuery('HousingRoomDescription.fetchAllByBuilding').setString('building', buildingCode).setString('termEffective', params?.termEffective).setString('filter', "%${filter?.toUpperCase()}%").list()
        }
        return [list: rooms]
    }


    public static Object fetchValidRoomAndBuilding(String roomNumber, Map params) {
        def room = HousingRoomDescription.withSession { session ->
            session.getNamedQuery('HousingRoomDescription.fetchValidateRoomAndBuilding').setString('building', params?.building?.code).setString('termEffective', params?.termEffective).setString('roomNumber', roomNumber).list()
        }

        return room[0]
    }


    public static Object fetchValidSomeRoomAndBuilding(String roomNumber, Map params) {
        def buildingCode
        if (params?.building?.code)
            buildingCode = "%" + params?.building?.code + "%"
        else
            buildingCode = "%"

        def room = HousingRoomDescription.withSession { session ->
            session.getNamedQuery('HousingRoomDescription.fetchValidateSomeRoomAndBuilding').setString('building', buildingCode).setString('termEffective', params?.termEffective).setString('roomNumber', roomNumber).list()
        }

        return room[0]
    }

    // Used for simple room look up without dependency on building
    public static Object fetchByRoomNumberOrDescription(String filter) {
        def roomList
        if (filter) {
            roomList = HousingRoomDescription.findAllByRoomNumberIlikeOrDescriptionIlike("%" + filter + "%", "%" + filter + "%", [sort: "roomNumber", order: "asc"])
        } else {
            roomList = HousingRoomDescription.findAll([sort: "roomNumber", order: "asc"])
        }
        return [list: roomList]
    }


    static List<HousingRoomDescription> fetchAllActiveClassrooms(Map filterData, Map paginationAndSortParams){
        def query = """FROM  HousingRoomDescription a
                                       WHERE a.roomType = 'C'
                                       AND NVL(a.roomStatus.inactiveIndicator,'N') != 'Y'"""
        return new DynamicFinder(HousingRoomDescription.class, query, 'a').find(filterData?:[params:[:]], paginationAndSortParams)
    }

}

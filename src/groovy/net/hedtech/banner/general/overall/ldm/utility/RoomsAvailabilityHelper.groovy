package net.hedtech.banner.general.overall.ldm.utility

import net.hedtech.banner.general.overall.AvailableRoomDescription
import net.hedtech.banner.general.system.Term
import net.hedtech.banner.query.DynamicFinder

/**
 * Helper class for Rooms Availability, which is the utility class responsible
 * for building the SQL Query AND Runtime parameters AND uses DynamicFinder
 * to find the results
 */
class RoomsAvailabilityHelper {

    static List fetchSearchAvailableRoom(Map filterData, Map pagingAndSortParams) {
        parseInputParameters(filterData)
        def query = """FROM AvailableRoomDescription a
                            WHERE """ + fetchConditionalClauseForAvailableRoomSearch("a")
        new DynamicFinder(AvailableRoomDescription.class, query, "a").find(filterData, pagingAndSortParams)
    }

    static Long countAllAvailableRoom(Map filterData){
        parseInputParameters(filterData)
        def query = """FROM AvailableRoomDescription a
                            WHERE """ + fetchConditionalClauseForAvailableRoomSearch("a")
        new DynamicFinder(AvailableRoomDescription.class, query, "a").count(filterData)
    }


    static boolean checkExistsAvailableRoomByRoomAndBuilding(Map filterData) {
        parseInputParameters(filterData)
        def query = """FROM AvailableRoomDescription a
                            WHERE ROWNUM = 1
                            AND a.buildingCode = :buildingCode
                            AND a.roomNumber = :roomNumber AND """ + fetchConditionalClauseForAvailableRoomSearch("a")
        new DynamicFinder(AvailableRoomDescription.class, query, "a").find(filterData, [:])
    }


    private static void parseInputParameters(Map filterData) {
        filterData.params.termCode = Term.fetchMaxTermWithStartDateLessThanGivenDate( filterData.params.startDate)?.code
        //if roomType is set to A or it is null then all roomTypes except dormitory has to be selected.
        if (!filterData.params.roomType) {
            filterData.params.roomType = "%"
        }
    }


    private static String fetchConditionalClauseForAvailableRoomSearch(String tableIdentifier) {
        return """ ${tableIdentifier}.roomType like :roomType
                            AND ${tableIdentifier}.capacity >= NVL(:capacity, 0)
                            AND ${tableIdentifier}.termEffective = (SELECT MAX(hrd1.termEffective)
                                                                        FROM AvailableRoomDescription hrd1
                                                                        WHERE ${tableIdentifier}.buildingCode = hrd1.buildingCode
                                                                        AND ${tableIdentifier}.roomNumber = hrd1.roomNumber
                                                                        AND hrd1.termEffective <= :termCode )
                            AND NOT EXISTS( FROM SectionMeetingTime b
                                                                WHERE b.building.code IS NOT NULL
                                                                AND b.room IS NOT NULL
                                                                AND b.building.code = ${tableIdentifier}.buildingCode
                                                                AND b.room = ${tableIdentifier}.roomNumber
                                                                AND(
                                                                        (b.beginTime BETWEEN :beginTime AND :endTime
                                                                        OR b.endTime BETWEEN :beginTime AND :endTime)
                                                                        OR(:beginTime BETWEEN b.beginTime AND b.endTime))
                                                                AND(
                                                                        (b.startDate BETWEEN :startDate AND :endDate
                                                                        OR b.endDate BETWEEN :startDate AND :endDate)
                                                                        OR(:startDate BETWEEN b.startDate AND b.endDate) )
                                                                AND(b.monday = nvl(:monday, '#')
                                                                        OR b.tuesday = nvl(:tuesday, '#')
                                                                        OR b.wednesday = nvl(:wednesday, '#')
                                                                        OR b.thursday = nvl(:thursday, '#')
                                                                        OR b.friday = nvl(:friday, '#')
                                                                        OR b.saturday = nvl(:saturday, '#')
                                                                        OR b.sunday = nvl(:sunday, '#')) )
                            AND NOT EXISTS(FROM HousingRoomUsageRestriction c
                                                                WHERE c.building.code = ${tableIdentifier}.buildingCode
                                                                AND c.roomNumber = ${tableIdentifier}.roomNumber
                                                                AND(
                                                                        c.startDate BETWEEN :startDate AND :endDate
                                                                        OR(c.endDate IS NOT NULL AND(c.endDate BETWEEN :startDate AND :endDate
                                                                                OR c.startDate <= :startDate AND c.endDate >= :endDate))
                                                                        OR(c.endDate IS NULL AND c.startDate <= :endDate)))"""
    }
}

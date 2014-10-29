package net.hedtech.banner.general.overall.ldm.utility

import net.hedtech.banner.general.overall.HousingRoomDescriptionReadOnly
import net.hedtech.banner.general.system.DayOfWeek
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
        def query = """FROM HousingRoomDescriptionReadOnly a
                            WHERE """ + fetchConditionalClauseForAvailableRoomSearch("a")
        new DynamicFinder(HousingRoomDescriptionReadOnly.class, query, "a").find(filterData, pagingAndSortParams)
    }

    static Long countAllAvailableRoom(Map filterData){
        parseInputParameters(filterData)
        def query = """FROM HousingRoomDescriptionReadOnly a
                            WHERE """ + fetchConditionalClauseForAvailableRoomSearch("a")
        new DynamicFinder(HousingRoomDescriptionReadOnly.class, query, "a").count(filterData)
    }


    static boolean checkExistsAvailableRoomByRoomAndBuilding(Map filterData) {
        parseInputParameters(filterData)
        def query = """FROM HousingRoomDescriptionReadOnly a
                            WHERE ROWNUM = 1
                            AND a.buildingCode = :buildingCode
                            AND a.roomNumber = :roomNumber AND """ + fetchConditionalClauseForAvailableRoomSearch("a")
        new DynamicFinder(HousingRoomDescriptionReadOnly.class, query, "a").find(filterData, [:])
    }


    private static void parseInputParameters(Map filterData) {
        filterData.params.termCode = Term.fetchMaxTermWithStartDateLessThanGivenDate( filterData.params.startDate)?.code
        if (!filterData.params.roomType) {
            filterData.params.roomType = "%"
        }
        DayOfWeek.list().description.each{day->
            if(!filterData.params?."${day.toLowerCase()}"){
                filterData.params?."${day.toLowerCase()}"= '#'
            }
        }
    }


    private static String fetchConditionalClauseForAvailableRoomSearch(String tableIdentifier) {
        return """${tableIdentifier}.roomType like :roomType
                            AND ${tableIdentifier}.capacity >= NVL(:capacity, 0)
                            AND ${tableIdentifier}.termEffective = (SELECT MAX(hrd1.termEffective)
                                                                        FROM HousingRoomDescriptionReadOnly hrd1
                                                                        WHERE ${tableIdentifier}.buildingCode = hrd1.buildingCode
                                                                        AND ${tableIdentifier}.roomNumber = hrd1.roomNumber
                                                                        AND hrd1.termEffective <= :termCode )
                            AND NOT EXISTS ( FROM SectionMeetingTime b
                                                                WHERE b.building.code IS NOT NULL
                                                                AND b.room IS NOT NULL
                                                                AND b.building.code = ${tableIdentifier}.buildingCode
                                                                AND b.room = ${tableIdentifier}.roomNumber
                                                                AND (
                                                                        b.beginTime BETWEEN :beginTime AND :endTime
                                                                        OR b.endTime BETWEEN :beginTime AND :endTime
                                                                        OR :beginTime BETWEEN b.beginTime AND b.endTime)
                                                                AND (
                                                                        b.startDate BETWEEN :startDate AND :endDate
                                                                        OR b.endDate BETWEEN :startDate AND :endDate
                                                                        OR :startDate BETWEEN b.startDate AND b.endDate)
                                                                AND (b.monday = :monday
                                                                        OR b.tuesday = :tuesday
                                                                        OR b.wednesday = :wednesday
                                                                        OR b.thursday = :thursday
                                                                        OR b.friday = :friday
                                                                        OR b.saturday = :saturday
                                                                        OR b.sunday = :sunday) )
                            AND NOT EXISTS (FROM HousingRoomUsageRestriction c
                                                                WHERE c.building.code = ${tableIdentifier}.buildingCode
                                                                AND c.roomNumber = ${tableIdentifier}.roomNumber
                                                                AND (
                                                                        c.startDate BETWEEN :startDate AND :endDate
                                                                        OR (c.endDate IS NOT NULL AND (c.endDate BETWEEN :startDate AND :endDate
                                                                                OR c.startDate <= :startDate AND c.endDate >= :endDate))
                                                                        OR(c.endDate IS NULL AND c.startDate <= :endDate)))
                            AND NOT EXISTS ( FROM HousingRoomAttributesDefinition d
                                                            WHERE d.building.code = ${tableIdentifier}.buildingCode
                                                            AND  d.roomNumber = ${tableIdentifier}.roomNumber
                                                            AND  d.termEffective = ${tableIdentifier}.termEffective
                                                            AND d.mustMatch = 'Y')"""
    }
}

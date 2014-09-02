package net.hedtech.banner.general.overall.ldm.utility

import net.hedtech.banner.general.overall.AvailableRoomDescription
import net.hedtech.banner.general.system.Term
import net.hedtech.banner.query.DynamicFinder

/**
 * Helper class for Rooms Availability, which is the utility class responsible
 * for building the SQL Query and Runtime parameters and uses DynamicFinder
 * to find the results
 */
class RoomsAvailabilityHelper {

    def static fetchAvailableRoomSearch( filterData, pagingAndSortParams ) {
        parseInputParameters( filterData )
        finderForAvailableRoomSearchByAll( filterData ).find( filterData, pagingAndSortParams )
    }



    /**
     * Parse the input parameters and set % for optional parameters.
     */
    private static void parseInputParameters( filterData ) {
        //Compute the termCode and add to the params.
        def beginDate = filterData.params.beginDate
        def termCode = Term.fetchMaxTermWithStartDateLessThanGivenDate( beginDate )?.code
        filterData.params.termCode = termCode
        //These fields are not mandatory in advance search. So if null then they have to be set to default value.
        if (!filterData.params.sunday) {
            filterData.params.sunday = "#"
        }
        if (!filterData.params.monday) {
            filterData.params.monday = "#"
        }
        if (!filterData.params.tuesday) {
            filterData.params.tuesday = "#"
        }
        if (!filterData.params.wednesday) {
            filterData.params.wednesday = "#"
        }
        if (!filterData.params.thursday) {
            filterData.params.thursday = "#"
        }
        if (!filterData.params.friday) {
            filterData.params.friday = "#"
        }
        if (!filterData.params.saturday) {
            filterData.params.saturday = "#"
        }
        //if roomType is set to A or it is null then all roomTypes except dormintory has to be selectec.
        if (!filterData.params.roomType) {
            filterData.params.classroom = "C"
            filterData.params.other = "O"
        }
    }
    def private static finderForAvailableRoomSearchByAll = {filterData ->
        def query = """FROM  AvailableRoomDescription ehrd where """
        if (filterData.params.roomType) {
            query += " ehrd.roomType in (:roomType) "
        } else {
            query += " ehrd.roomType in (:classroom, :other) "
        }
        query += """ and ehrd.termEffective = (select max(hrd1.termEffective) from  AvailableRoomDescription hrd1 where ehrd.buildingCode= hrd1.buildingCode
         and ehrd.roomNumber = hrd1.roomNumber and hrd1.termEffective <= :termCode)
         and not exists ( from SectionMeetingTime smt
                          where ((smt.beginTime between :beginTime and :endTime or smt.endTime between :beginTime and :endTime) or (:beginTime between smt.beginTime and smt.endTime))
                            and ((smt.startDate between :beginDate and :endDate or smt.endDate between :beginDate and :endDate) or (:beginDate between smt.startDate and smt.endDate))
                            and smt.building.code = ehrd.buildingCode and smt.room = ehrd.roomNumber
                            and smt.building.code is not null and smt.room is not null
                            and (smt.monday = :monday or smt.tuesday = :tuesday or smt.wednesday = :wednesday or smt.thursday = :thursday or smt.friday = :friday or smt.saturday = :saturday or smt.sunday = :sunday))
         and not exists ( from HousingRoomUsageRestriction hrur where hrur.building.code = ehrd.buildingCode
                            and  hrur.roomNumber = ehrd.roomNumber
                            AND (
                                hrur.startDate BETWEEN :beginDate AND :endDate
                                OR  (hrur.endDate is not null AND hrur.endDate BETWEEN :beginDate AND :endDate)
                                OR  (hrur.endDate is not null AND hrur.startDate <= :beginDate AND hrur.endDate >= :endDate)
                                OR  (hrur.endDate is null AND hrur.startDate <= :endDate)
                            ))"""
        //This is needed because it is an "And" condition .
        if (filterData.params.roomAttribute1 || filterData.params.roomAttribute2 || filterData.params.roomAttribute3 || filterData.params.roomAttribute4 || filterData.params.roomAttribute5 || filterData.params.roomAttribute6) {
            if (filterData.params.roomAttribute1) {
                query += """  and exists ( from HousingRoomAttributesDefinition hrad1 where hrad1.building.code = ehrd.buildingCode
                                              and  hrad1.roomNumber = ehrd.roomNumber
                                              and  hrad1.termEffective = ehrd.termEffective and hrad1.buildingAndRoomAttribute.code = :roomAttribute1) """
            }
            if (filterData.params.roomAttribute2) {
                query += """  and exists ( from HousingRoomAttributesDefinition hrad2 where hrad2.building.code = ehrd.buildingCode
                                              and  hrad2.roomNumber = ehrd.roomNumber
                                              and  hrad2.termEffective = ehrd.termEffective and hrad2.buildingAndRoomAttribute.code = :roomAttribute2) """
            }
            if (filterData.params.roomAttribute3) {
                query += """  and exists ( from HousingRoomAttributesDefinition hrad3 where hrad3.building.code = ehrd.buildingCode
                                              and  hrad3.roomNumber = ehrd.roomNumber
                                              and  hrad3.termEffective = ehrd.termEffective and hrad3.buildingAndRoomAttribute.code = :roomAttribute3) """
            }
            if (filterData.params.roomAttribute4) {
                query += """  and exists ( from HousingRoomAttributesDefinition hrad4 where hrad4.building.code = ehrd.buildingCode
                                              and  hrad4.roomNumber = ehrd.roomNumber
                                              and  hrad4.termEffective = ehrd.termEffective and hrad4.buildingAndRoomAttribute.code = :roomAttribute4) """
            }
            if (filterData.params.roomAttribute5) {
                query += """  and exists ( from HousingRoomAttributesDefinition hrad5 where hrad5.building.code = ehrd.buildingCode
                                              and  hrad5.roomNumber = ehrd.roomNumber
                                              and  hrad5.termEffective = ehrd.termEffective and hrad5.buildingAndRoomAttribute.code = :roomAttribute5) """
            }
            if (filterData.params.roomAttribute6) {
                query += """  and exists ( from HousingRoomAttributesDefinition hrad6 where hrad6.building.code = ehrd.buildingCode
                                              and  hrad6.roomNumber = ehrd.roomNumber
                                              and  hrad6.termEffective = ehrd.termEffective and hrad6.buildingAndRoomAttribute.code = :roomAttribute6) """
            }
            String str = createInClause( filterData.params )
            query += """ and not exists ( from HousingRoomAttributesDefinition hrad7 where hrad7.building.code = ehrd.buildingCode
                                              and  hrad7.roomNumber = ehrd.roomNumber
                                              and  hrad7.termEffective = ehrd.termEffective and hrad7.mustMatch = 'Y'
                                              and  hrad7.buildingAndRoomAttribute.code not in ("""
            query += str + "))"
        } else {
            query += """ and not exists ( from HousingRoomAttributesDefinition hrad7 where hrad7.building.code = ehrd.buildingCode
                                              and  hrad7.roomNumber = ehrd.roomNumber
                                              and  hrad7.termEffective = ehrd.termEffective and hrad7.mustMatch = 'Y')"""
        }
        if (filterData.params.buildingCode) {
            query += """ and ehrd.buildingCode = :buildingCode"""
        }
        if (filterData.params.capacity) {
            query += """ and ehrd.capacity >= :capacity"""
        } else {
            query += """ and ehrd.capacity >= 0"""
        }
        if (filterData.params.campusCode) {
            query += """ and ehrd.campusCode = :campusCode"""
        }
        if (filterData.params.siteCode) {
            query += """ and ehrd.siteCode = :siteCode"""
        }
        return new DynamicFinder( AvailableRoomDescription.class, query, "ehrd" )
    }


}

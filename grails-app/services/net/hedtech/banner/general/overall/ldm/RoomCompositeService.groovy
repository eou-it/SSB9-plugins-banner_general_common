/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.HousingRoomDescription
import net.hedtech.banner.general.overall.ldm.v1.Building
import net.hedtech.banner.general.overall.ldm.v1.Occupancy
import net.hedtech.banner.general.overall.ldm.v1.Room
import net.hedtech.banner.general.system.ldm.v1.Metadata
import net.hedtech.banner.query.QueryBuilder
import net.hedtech.banner.query.operators.Operators
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional

@Transactional
class RoomCompositeService {

    def buildingCompositeService

    private List roomLayoutTypeMapping = [
            [ldmValue: 'Classroom', bannerValue: 'C'],
            [ldmValue: 'Dorm', bannerValue: 'D'],
            [ldmValue: 'Other', bannerValue: 'O']
    ]


    List<Room> list(Map params) {
        List rooms = []
        RestfulApiValidationUtility.correctMaxAndOffset(params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        def filters = QueryBuilder.createFilters(params)
        def allowedSearchFields = ['roomLayoutType']
        def allowedOperators = [Operators.EQUALS]
        RestfulApiValidationUtility.validateCriteria(filters, allowedSearchFields, allowedOperators)
        def filterMap = QueryBuilder.getFilterData(params)
        def filterData = [params: [roomType: '%']]
        if (filterMap.params.containsKey('roomLayoutType')) {
            filterData.params = [roomType: fetchBannerRoomTypeForLdmRoomLayoutType(filterMap.params?.roomLayoutType)]
        }
        List<HousingRoomDescription> housingRoomDescriptions = HousingRoomDescription.fetchAllActiveRoomsByRoomType(filterData, filterMap.pagingAndSortParams)
        housingRoomDescriptions.each { housingRoomDescription ->
            List occupancies = [new Occupancy(fetchLdmRoomLayoutTypeForBannerRoomType(housingRoomDescription.roomType), housingRoomDescription.capacity)]
            Building building = buildingCompositeService.fetchByBuildingCode(housingRoomDescription.building.code)
            rooms << new Room(housingRoomDescription, building, occupancies, GlobalUniqueIdentifier.findByLdmNameAndDomainId(Room.LDM_NAME, housingRoomDescription.id).guid, new Metadata(housingRoomDescription.dataOrigin))
        }
        return rooms
    }


    Long count(Map params) {
        def filters = QueryBuilder.createFilters(params)
        def allowedSearchFields = ['roomLayoutType']
        def allowedOperators = [Operators.EQUALS]
        RestfulApiValidationUtility.validateCriteria(filters, allowedSearchFields, allowedOperators)
        def filterMap = QueryBuilder.getFilterData(params)
        def filterData = [params: [roomType: '%']]
        if (filterMap.params.containsKey('roomLayoutType')) {
            filterData.params = [roomType: fetchBannerRoomTypeForLdmRoomLayoutType(filterMap.params?.roomLayoutType)]
        }
        return HousingRoomDescription.countAllActiveRoomsByRoomType(filterData)
    }


    @Transactional(readOnly = true)
    Room get(String guid) {
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndGuid(Room.LDM_NAME, guid)
        if (!globalUniqueIdentifier) {
            throw new ApplicationException(GlobalUniqueIdentifierService.API, new NotFoundException(id: Room.class.simpleName))
        }

        def filterData = [params: [roomType: '%',  id: globalUniqueIdentifier.domainId], criteria: [[key: 'id', binding: 'id', operator: Operators.EQUALS]]]
        HousingRoomDescription housingRoomDescription = HousingRoomDescription.fetchAllActiveRoomsByRoomType(filterData, [:])[0]
        if (!housingRoomDescription) {
            throw new ApplicationException(GlobalUniqueIdentifierService.API, new NotFoundException(id: Room.class.simpleName))
        }
        Building building = buildingCompositeService.fetchByBuildingCode(housingRoomDescription.building.code)
        List occupancies = [new Occupancy(fetchLdmRoomLayoutTypeForBannerRoomType(housingRoomDescription.roomType), housingRoomDescription.capacity)]
        return new Room(housingRoomDescription, building, occupancies, globalUniqueIdentifier.guid, new Metadata(housingRoomDescription.dataOrigin))
    }


    private String fetchBannerRoomTypeForLdmRoomLayoutType(String ldmRoomLayoutType) {
        return roomLayoutTypeMapping.find { it.ldmValue.toUpperCase() == ldmRoomLayoutType?.toUpperCase() }?.bannerValue
    }


    private String fetchLdmRoomLayoutTypeForBannerRoomType(String bannerRoomType) {
        return roomLayoutTypeMapping.find { it.bannerValue.toUpperCase() == bannerRoomType?.toUpperCase() }?.ldmValue
    }
}

/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.HousingRoomDescription
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.ldm.v1.Building
import net.hedtech.banner.general.overall.ldm.v1.Occupancy
import net.hedtech.banner.general.overall.ldm.v1.Room
import net.hedtech.banner.general.system.ldm.v1.Metadata
import net.hedtech.banner.query.QueryBuilder
import net.hedtech.banner.query.operators.Operators
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional

@Transactional
class RoomCompositeService extends LdmService {

    def buildingCompositeService


    List<Room> list(Map params) {
        List rooms = []
        RestfulApiValidationUtility.correctMaxAndOffset(params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        Map filterParams = prepareParams(params)
        List<HousingRoomDescription> housingRoomDescriptions = HousingRoomDescription.fetchAllActiveRoomsByRoomType(filterParams.filterData, filterParams.pagingAndSortParams)
        housingRoomDescriptions.each { housingRoomDescription ->
            List occupancies = [new Occupancy(fetchLdmRoomLayoutTypeForBannerRoomType(housingRoomDescription.roomType), housingRoomDescription.capacity)]
            Building building = buildingCompositeService.fetchByBuildingCode(housingRoomDescription.building.code)
            rooms << new Room(housingRoomDescription, building, occupancies, GlobalUniqueIdentifier.findByLdmNameAndDomainId(Room.LDM_NAME, housingRoomDescription.id).guid, new Metadata(housingRoomDescription.dataOrigin))
        }
        return rooms
    }


    Long count(Map params) {
        Map filterParams = prepareParams(params)
        return HousingRoomDescription.countAllActiveRoomsByRoomType(filterParams.filterData)
    }


    private Map prepareParams(Map params) {
        validateSearchCriteria(params)
        def filterMap = QueryBuilder.getFilterData(params)
        def filterData = [params: [roomType: '%']]
        if (filterMap.params.containsKey('roomLayoutType')) {
            filterData.params = [roomType: fetchBannerRoomTypeForLdmRoomLayoutType(filterMap.params?.roomLayoutType)]
        }
        return [filterData: filterData, pagingAndSortParams: filterMap.pagingAndSortParams]
    }


    private void validateSearchCriteria(Map params) {
        def filters = QueryBuilder.createFilters(params)
        def allowedSearchFields = ['roomLayoutType']
        def allowedOperators = [Operators.EQUALS]
        RestfulApiValidationUtility.validateCriteria(filters, allowedSearchFields, allowedOperators)
    }


    @Transactional(readOnly = true)
    Room get(String guid) {
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndGuid(Room.LDM_NAME, guid)
        if (!globalUniqueIdentifier) {
            throw new ApplicationException(GlobalUniqueIdentifierService.API, new NotFoundException(id: Room.class.simpleName))
        }

        def filterData = [params: [roomType: '%', id: globalUniqueIdentifier.domainId], criteria: [[key: 'id', binding: 'id', operator: Operators.EQUALS]]]
        HousingRoomDescription housingRoomDescription = HousingRoomDescription.fetchAllActiveRoomsByRoomType(filterData, [:])[0]
        if (!housingRoomDescription) {
            throw new ApplicationException(GlobalUniqueIdentifierService.API, new NotFoundException(id: Room.class.simpleName))
        }
        Building building = buildingCompositeService.fetchByBuildingCode(housingRoomDescription.building.code)
        List occupancies = [new Occupancy(fetchLdmRoomLayoutTypeForBannerRoomType(housingRoomDescription.roomType), housingRoomDescription.capacity)]
        return new Room(housingRoomDescription, building, occupancies, globalUniqueIdentifier.guid, new Metadata(housingRoomDescription.dataOrigin))
    }


    private String fetchBannerRoomTypeForLdmRoomLayoutType(String ldmRoomLayoutType) {
        String roomType
        if (ldmRoomLayoutType) {
            List<IntegrationConfiguration> roomLayoutTypes = rules.grep {
                it.settingName?.equals('room.occupancy.roomLayoutType')
            }
            roomType = roomLayoutTypes.find { it.value == ldmRoomLayoutType }?.translationValue
        }
        return roomType
    }


    private String fetchLdmRoomLayoutTypeForBannerRoomType(String bannerRoomType) {
        String roomLayoutType
        if (bannerRoomType) {
            List<IntegrationConfiguration> roomLayoutTypes = rules.grep {
                it.settingName?.equals('room.occupancy.roomLayoutType')
            }
            roomLayoutType = roomLayoutTypes.find { it.translationValue == bannerRoomType }?.value
        }
        return roomLayoutType
    }
}

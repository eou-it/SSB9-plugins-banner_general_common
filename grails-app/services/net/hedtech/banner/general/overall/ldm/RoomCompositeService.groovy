/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.HousingRoomDescription
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.ldm.utility.RoomsAvailabilityHelper
import net.hedtech.banner.general.overall.ldm.v1.AvailableRoom
import net.hedtech.banner.general.overall.ldm.v1.BuildingDetail
import net.hedtech.banner.general.overall.ldm.v1.Occupancy
import net.hedtech.banner.general.overall.ldm.v1.Room
import net.hedtech.banner.general.system.ldm.v1.Metadata
import net.hedtech.banner.query.QueryBuilder
import net.hedtech.banner.query.operators.Operators
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional
import java.text.DateFormat
import java.text.SimpleDateFormat

@Transactional
class RoomCompositeService extends LdmService {

    def buildingCompositeService

	 /**
     * Responsible for returning the list of Rooms in case of API request or Returns List
     * of AvailableRoom for QAPI request, In case of QAPI Request will be sent as part of
     * POST method, which is exposed as POST Restfull Webservice having the endpoints
     * API End Point  /api/rooms
     * QAPI End point /qapi/rooms
     * param params Request parameter
     * @return List<Room>
     */
    List<Room> list(Map params) {
	
	 //Handles QAPI Request
        if(RestfulApiValidationUtility.isQApiRequest(params)){
            List rooms = []
            RestfulApiValidationUtility.correctMaxAndOffset(params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
            Map filterParams = prepareSearchParams(params)
            List<HousingRoomDescription> housingRoomDescriptions = RoomsAvailabilityHelper.fetchAvailableRoomSearch(filterParams.filterData, filterParams.pagingAndSortParams)
            housingRoomDescriptions.each { availableRoomDescription ->
                List occupancies = [new Occupancy(fetchLdmRoomLayoutTypeForBannerRoomType(availableRoomDescription.roomType), availableRoomDescription.capacity)]
                BuildingDetail building = buildingCompositeService.fetchByBuildingCode(availableRoomDescription.buildingCode)
                rooms << new AvailableRoom(availableRoomDescription, building, occupancies, GlobalUniqueIdentifier.findByLdmNameAndDomainId(Room.LDM_NAME, availableRoomDescription.id).guid,new Metadata(availableRoomDescription.dataOrigin))
            }
            return rooms
        }
        // Handles API request
        else{
			List rooms = []
			RestfulApiValidationUtility.correctMaxAndOffset(params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
			Map filterParams = prepareParams(params)
			List<HousingRoomDescription> housingRoomDescriptions = HousingRoomDescription.fetchAllActiveRoomsByRoomType(filterParams.filterData, filterParams.pagingAndSortParams)
			housingRoomDescriptions.each { housingRoomDescription ->
				List occupancies = [new Occupancy(fetchLdmRoomLayoutTypeForBannerRoomType(housingRoomDescription.roomType), housingRoomDescription.capacity)]
				BuildingDetail building = buildingCompositeService.fetchByBuildingCode(housingRoomDescription.building.code)
				rooms << new Room(housingRoomDescription, building, occupancies, GlobalUniqueIdentifier.findByLdmNameAndDomainId(Room.LDM_NAME, housingRoomDescription.id).guid, new Metadata(housingRoomDescription.dataOrigin))
			}
			return rooms
		}
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
            filterData.params = [roomType: fetchBannerRoomTypeForLdmRoomLayoutType(filterMap.params?.roomLayoutType?.trim())]
        }
        return [filterData: filterData, pagingAndSortParams: filterMap.pagingAndSortParams]
    }
	
	private Map prepareSearchParams(Map params) {
        validateSearchCriteria(params)
        def filterMap = QueryBuilder.getFilterData(params)
        def type = params.occupancies.roomLayoutType[0] == "Classroom" ? 'C':'%'

        List<String> days =['Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday']
        def daysList = params.recurrences.byDay[0]
        Map inputData =[:]
        days.each { day ->
            if(daysList.contains(day)){
                inputData.put(day.toLowerCase(), 'S')


            }else{
                inputData.put(day.toLowerCase(), '')
            }

        }
        inputData.put('roomType',type )

        //Extract beginDate
        DateFormat df = new SimpleDateFormat("yyyy-mm-dd");
        java.util.Date beginDate = df.parse(params.startDate);
        inputData.put('beginDate',new java.sql.Date(beginDate.getTime()))

        //Extract endDate
        java.util.Date endDate = df.parse(params.endDate);
        inputData.put('endDate',new java.sql.Date(endDate.getTime()))

        inputData.put('beginTime',params.startTime)
        inputData.put('endTime',params.endTime)
        def filterData = [params: inputData,criteria:[]]
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
        BuildingDetail building = buildingCompositeService.fetchByBuildingCode(housingRoomDescription.building.code)
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

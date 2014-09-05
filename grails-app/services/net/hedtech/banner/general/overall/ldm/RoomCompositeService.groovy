/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.AvailableRoomDescription
import net.hedtech.banner.general.overall.HousingRoomDescription
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.ldm.utility.RoomsAvailabilityHelper
import net.hedtech.banner.general.overall.ldm.v1.AvailableRoom
import net.hedtech.banner.general.overall.ldm.v1.BuildingDetail
import net.hedtech.banner.general.overall.ldm.v1.Occupancy
import net.hedtech.banner.general.overall.ldm.v1.Room
import net.hedtech.banner.general.system.DayOfWeek
import net.hedtech.banner.general.system.ldm.v1.Metadata
import net.hedtech.banner.query.QueryBuilder
import net.hedtech.banner.query.operators.Operators
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional

@Transactional
class RoomCompositeService extends LdmService {

    private static final String DATE_FORMAT = 'yyyy-MM-dd'

    def buildingCompositeService


    List<Room> list(Map params) {
        if (RestfulApiValidationUtility.isQApiRequest(params)) {
            checkIfRoomAvailable(params)
            List rooms = []
            RestfulApiValidationUtility.correctMaxAndOffset(params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
            validateParams(params)
            Map filterParams = prepareSearchParams(params)
            List<AvailableRoomDescription> availableRoomDescriptions = RoomsAvailabilityHelper.fetchSearchAvailableRoom(filterParams.filterData, filterParams.pagingAndSortParams)
            availableRoomDescriptions.each { availableRoomDescription ->
                List occupancies = [new Occupancy(fetchLdmRoomLayoutTypeForBannerRoomType(availableRoomDescription.roomType), availableRoomDescription.capacity)]
                BuildingDetail building = buildingCompositeService.fetchByBuildingCode(availableRoomDescription.buildingCode)
                rooms << new AvailableRoom(availableRoomDescription, building, occupancies, GlobalUniqueIdentifier.findByLdmNameAndDomainId(Room.LDM_NAME, availableRoomDescription.id).guid, new Metadata(availableRoomDescription.dataOrigin))
            }
            return rooms
        } else {
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
        if (RestfulApiValidationUtility.isQApiRequest(params)) {
            Map filterParams = prepareSearchParams(params)
            RoomsAvailabilityHelper.countAllAvailableRoom(filterParams.filterData)
        } else {
            Map filterParams = prepareParams(params)
            return HousingRoomDescription.countAllActiveRoomsByRoomType(filterParams.filterData)
        }
    }


    private void validateParams(Map params) {
        if (!params.startDate) {
            throw new ApplicationException(RoomCompositeService, "@@r1:missing.startDate:BusinessLogicValidationException@@")
        }
        if (!params.endDate) {
            throw new ApplicationException(RoomCompositeService, "@@r1:missing.endDate:BusinessLogicValidationException@@")
        }
        def pattern = /[0-2][0-3][0-5][0-9]/
        if (!params.startTime) {
            throw new ApplicationException(RoomCompositeService, "@@r1:missing.startTime:BusinessLogicValidationException@@")
        }
        if( !(params.startTime ==~ pattern)) {
            throw new ApplicationException(RoomCompositeService, "@@r1:invalid.startTime:BusinessLogicValidationException@@")
        }
        if (!params.endTime) {
            throw new ApplicationException(RoomCompositeService, "@@r1:missing.endTime:BusinessLogicValidationException@@")
        }
        if (!(params.endTime ==~ pattern)) {
            throw new ApplicationException(RoomCompositeService, "@@r1:invalid.endTime:BusinessLogicValidationException@@")
        }
        if (params.recurrence) {
            if (!params.recurrence?.byDay) {
                throw new ApplicationException(RoomCompositeService, "@@r1:invalid.recurrence.byDay:BusinessLogicValidationException@@")
            }
        } else {
            throw new ApplicationException(RoomCompositeService, "@@r1:invalid.recurrence:BusinessLogicValidationException@@")
        }
        if (params.occupancies) {
            if (!params.occupancies[0]?.roomLayoutType) {
                throw new ApplicationException(RoomCompositeService, "@@r1:invalid.roomLayoutType:BusinessLogicValidationException@@")
            }
            if (!params.occupancies && params.occupancies[0]?.maxOccupancy) {
                throw new ApplicationException(RoomCompositeService, "@@r1:invalid.maxOccupancy:BusinessLogicValidationException@@")
            }
        } else {
            throw new ApplicationException(RoomCompositeService, "@@r1:invalid.occupancies:BusinessLogicValidationException@@")
        }
        Date startDate = Date.parse(DATE_FORMAT, params.startDate?.trim())
        Date endDate = Date.parse(DATE_FORMAT, params.endDate?.trim())

        if (startDate > endDate) {
            throw new ApplicationException(RoomCompositeService, "@@r1:startDate.laterThanEndDate:BusinessLogicValidationException@@")
        }

        Integer startTimeAsInteger = Integer.valueOf(params.startTime)
        Integer endTimeAsInteger = Integer.valueOf(params.endTime)
        if (startTimeAsInteger >= endTimeAsInteger) {
            throw new ApplicationException(RoomCompositeService, "@@r1:startTime.laterThanEndTime:BusinessLogicValidationException@@")
        }
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
        Map inputData = [:]

        inputData.put('startDate', Date.parse(DATE_FORMAT, params.startDate?.trim()))
        inputData.put('endDate', Date.parse(DATE_FORMAT, params.endDate?.trim()))

        inputData.put('beginTime', params.startTime)
        inputData.put('endTime', params.endTime)

        List<DayOfWeek> days = DayOfWeek.list([sort: 'number', order: 'asc'])
        def daysList = params.recurrence.byDay
        days.each { day ->
            if (daysList.contains(day.description)) {
                inputData.put(day.description.toLowerCase(), 'S')
            } else {
                inputData.put(day.description.toLowerCase(), '')
            }
        }
        if (params.occupancies) {
            inputData.put('roomType', fetchBannerRoomTypeForLdmRoomLayoutType(params.occupancies[0]?.roomLayoutType))
            inputData.put('capacity', params.occupancies[0]?.maxOccupancy)
        } else {
            inputData.put('roomType', '%')
            inputData.put('capacity', null)
        }

        def filterData = [params: inputData, criteria: []]
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


    boolean checkIfRoomAvailable(Map params){
        boolean roomAvailable = false
        validateParams(params)
        if(params.roomNumber && params.building?.code) {
            Map filterParams = prepareSearchParams(params)
            filterParams.filterData.params << [roomNumber: params.roomNumber.toString(), buildingCode: params.building?.code]
            roomAvailable = RoomsAvailabilityHelper.checkExistsAvailableRoomByRoomAndBuilding(filterParams.filterData)
        }
        return roomAvailable
    }

    private String fetchBannerRoomTypeForLdmRoomLayoutType(String ldmRoomLayoutType) {
        String roomType = null
        if (ldmRoomLayoutType) {
            def rule = findAllByProcessCodeAndSettingNameAndValue('LDM','room.occupancy.roomLayoutType', ldmRoomLayoutType)
            roomType = rule?.translationValue
        }
        if (!roomType) {
            throw new ApplicationException(RoomCompositeService, "@@r1:invalid.roomLayoutType:BusinessLogicValidationException@@")
        }
        return roomType
    }


    private String fetchLdmRoomLayoutTypeForBannerRoomType(String bannerRoomType) {
        String roomLayoutType = null
        if (bannerRoomType) {
            def rule = fetchAllByProcessCodeAndSettingNameAndTranslationValue('LDM','room.occupancy.roomLayoutType', bannerRoomType)
            roomLayoutType = rule?.value
        }
        return roomLayoutType
    }
}

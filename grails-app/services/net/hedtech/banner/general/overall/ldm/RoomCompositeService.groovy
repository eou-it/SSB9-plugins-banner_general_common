/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.HousingRoomDescription
import net.hedtech.banner.general.overall.HousingRoomDescriptionReadOnly
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.ldm.utility.RoomsAvailabilityHelper
import net.hedtech.banner.general.overall.ldm.v1.AvailableRoom
import net.hedtech.banner.general.overall.ldm.v1.BuildingDetail
import net.hedtech.banner.general.overall.ldm.v1.Occupancy
import net.hedtech.banner.general.system.Building
import net.hedtech.banner.general.system.DayOfWeek
import net.hedtech.banner.general.system.ldm.SiteDetailCompositeService
import net.hedtech.banner.general.system.ldm.v1.Metadata
import net.hedtech.banner.general.system.ldm.v1.SiteDetail
import net.hedtech.banner.query.DynamicFinder
import net.hedtech.banner.query.QueryBuilder
import net.hedtech.banner.query.operators.Operators
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional

@Transactional
class RoomCompositeService extends LdmService {

    private static final String HOUR_FORMAT = '([0-1][0-9]|2[0-3])'
    private static final String MINUTE_FORMAT = '[0-5][0-9]'
    private static final String SECOND_FORMAT = '[0-5][0-9]'
    private static final String LDM_NAME = 'rooms'
    private static final String PROCESS_CODE = "LDM"
    private static final String SETTING_ROOM_LAYOUT_TYPE = "ROOM.OCCUPANCY.ROOMLAYOUTTYPE"
    public static final String CONTENT_TYPE_ROOM_AVAILABILITY_V2 = "application/vnd.hedtech.integration.room-availability.v2+json"
    private static final String VERSION_V2 = "v2"
    def buildingCompositeService


    List<AvailableRoom> list(Map params) {
        def entities
        RestfulApiValidationUtility.correctMaxAndOffset(params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        List allowedSortFields = ['number', 'title']
        RestfulApiValidationUtility.validateSortField(params.sort?.trim(), allowedSortFields)
        RestfulApiValidationUtility.validateSortOrder(params.order?.trim())
        params.sort = fetchBannerDomainPropertyForLdmField(params.sort?.trim())
        if (RestfulApiValidationUtility.isQApiRequest(params)) {
            // POST /qapi/rooms (Search for available rooms)
            validateParams(params)
            Map filterParams = prepareSearchParams(params)
            def listOfObjectArrays = RoomsAvailabilityHelper.fetchSearchAvailableRoom(filterParams.filterData, filterParams.pagingAndSortParams,CONTENT_TYPE_ROOM_AVAILABILITY_V2)
            entities = []
            listOfObjectArrays?.each {
                entities << it[0]
            }
        } else {
            // GET /api/rooms?filter[0][field]=roomLayoutType&filter[0][operator]=equals&filter[0][value]=Classroom
            Map filterData = prepareParams(params)
            def roomTypes
            if (filterData.params.containsKey('roomLayoutType')) {
                roomTypes = [fetchBannerRoomTypeForLdmRoomLayoutType(filterData.params?.roomLayoutType?.trim())]
            } else {
                roomTypes = getHEDMRoomTypes()
            }
            entities = fetchAllActiveRoomsByRoomTypes(roomTypes, filterData.pagingAndSortParams)
        }
        return getAvailableRooms(entities)
    }


    Long count(Map params) {
        if (RestfulApiValidationUtility.isQApiRequest(params)) {
            Map filterParams = prepareSearchParams(params)
            RoomsAvailabilityHelper.fetchSearchAvailableRoom(filterParams.filterData, null, true)
        } else {
            Map filterData = prepareParams(params)
            def roomTypes
            if (filterData.params.containsKey('roomLayoutType')) {
                roomTypes = [fetchBannerRoomTypeForLdmRoomLayoutType(filterData.params?.roomLayoutType?.trim())]
            } else {
                roomTypes = getHEDMRoomTypes()
            }
            return fetchAllActiveRoomsByRoomTypes(roomTypes, null, true)
        }
    }


    private void validateParams(Map params) {
        validateRequiredFields(params)
        validateBeginAndEndDates(params)
        validateTimeFormat(params.startTime?.trim(), 'startTime')
        validateTimeFormat(params.endTime?.trim(), 'endTime')
        validateBeginAndEndTimes(params)
        validateRecurrence(params)
        validateOccupancies(params)
    }


    private void validateRequiredFields(Map params) {
        if (!params.startDate?.trim()) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("missing.startDate", []))
        }
        if (!params.endDate?.trim()) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("missing.endDate", []))
        }
        if (!params.startTime?.trim()) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("missing.startTime", []))
        }
        if (!params.endTime?.trim()) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("missing.endTime", []))
        }
    }


    private void validateBeginAndEndDates(Map params) {
        Date startDate = convertString2Date(params.startDate?.trim())
        Date endDate = convertString2Date(params.endDate?.trim())

        if (startDate > endDate) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("startDate.laterThanEndDate", []))
        }
    }


    private void validateTimeFormat(String timeString, String fieldName) {
        String timeFormat = getTimeFormat().toLowerCase().replace('hh', HOUR_FORMAT).replace('mm', MINUTE_FORMAT).replace('ss', SECOND_FORMAT)
        if (timeString && (timeString.length() != getTimeFormat().length() || !(timeString ==~ /$timeFormat/))) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("invalid.timeFormat", []))
        }
    }


    private void validateBeginAndEndTimes(Map params) {
        Integer startTimeAsInteger = Integer.valueOf(getTimeInHHmmFormat(params.startTime?.trim()))
        Integer endTimeAsInteger = Integer.valueOf(getTimeInHHmmFormat(params.endTime?.trim()))
        if (startTimeAsInteger >= endTimeAsInteger) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("startTime.laterThanEndTime", []))
        }
    }


    private void validateRecurrence(Map params) {
        if (!params.recurrence) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("missing.recurrence", []))
        }
        validateByDays(params)
    }


    private void validateByDays(Map params) {
        if (!params.recurrence?.byDay) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("missing.recurrence.byDay", []))
        }
        List<DayOfWeek> days = DayOfWeek.list()
        if (days.description.intersect(params.recurrence?.byDay).isEmpty() || params.recurrence?.byDay?.size() > 7) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("invalid.recurrence.byDay", []))
        }
        List validDays = []
        Date startDate = convertString2Date(params.startDate?.trim())
        Date endDate = convertString2Date(params.endDate?.trim())
        int noOfDays = (int) use(groovy.time.TimeCategory) {
            def duration = endDate - startDate
            duration.days
        }
        noOfDays += 1
        if (noOfDays == 1) {
            validDays.add(days.find { it.number == startDate.getDay().toString() }?.description)
        } else if (noOfDays <= 7) {
            for (int i = 0; i < noOfDays; i++) {
                Date calculatedDate = startDate + i
                validDays.add(days.find { it.number == calculatedDate.getDay().toString() }?.description)
            }
        } else if (noOfDays > 7) {
            validDays = days.description
        }
        List invalidDays = params.recurrence?.byDay - validDays
        if (invalidDays) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("invalid.recurrence.byDay", []))
        }
    }


    private void validateOccupancies(Map params) {
        if (!params.occupancies) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("missing.occupancies", []))
        }
        if (!params.occupancies[0]?.roomLayoutType) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("missing.roomLayoutType", []))
        }
        if (!params.occupancies[0]?.maxOccupancy) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("missing.maxOccupancy", []))
        }
        try {
            Integer.valueOf(params.occupancies[0]?.maxOccupancy)
        } catch (NumberFormatException nfe) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("invalid.maxOccupancy", []))
        }
    }


    private Map prepareParams(Map params) {
        validateSearchCriteria(params)
        return QueryBuilder.getFilterData(params)
    }


    private Map prepareSearchParams(Map params) {
        def filterMap = QueryBuilder.getFilterData(params)
        Map inputData = [:]

        inputData.put('startDate', convertString2Date(params.startDate?.trim()))
        inputData.put('endDate', convertString2Date(params.endDate?.trim()))

        inputData.put('beginTime', getTimeInHHmmFormat(params.startTime?.trim()))
        inputData.put('endTime', getTimeInHHmmFormat(params.endTime?.trim()))

        def daysList = params.recurrence.byDay
        DayOfWeek.list().each { DayOfWeek day ->
            inputData.put(day.description.toLowerCase(), daysList.contains(day.description) ? day.code : null)
        }

        def roomTypes
        if (params.occupancies) {
            roomTypes = [fetchBannerRoomTypeForLdmRoomLayoutType(params.occupancies[0]?.roomLayoutType)]
        } else {
            roomTypes = getHEDMRoomTypes()
        }
        // TODO: Not sure why IN operator of DynamicFinder requires list in this format
        def roomTypeObjects = []
        roomTypes.each {
            roomTypeObjects << [data: it]
        }
        inputData.put('roomTypes', roomTypeObjects)

        if (params.occupancies) {
            inputData.put('capacity', params.occupancies[0]?.maxOccupancy)
        } else {
            inputData.put('capacity', null)
        }

        def contentType = LdmService.getRequestRepresentation()
        if(contentType == CONTENT_TYPE_ROOM_AVAILABILITY_V2) {
            if(params.containsKey('building')){
                if (params.building.guid) {
                    GlobalUniqueIdentifier buildingGUID = GlobalUniqueIdentifier.fetchByLdmNameAndGuid(BuildingCompositeService.LDM_NAME, params.building.guid)
                    if(buildingGUID){
                        inputData.put('buildingCode', buildingGUID.domainKey)
                    } else {
                        throw new ApplicationException("building", new NotFoundException())
                    }
                } else {
                    inputData.put('buildingCode', null)
                }
            }

            if(params.containsKey('site')){
                if (params.site.guid) {
                    GlobalUniqueIdentifier siteGUID = GlobalUniqueIdentifier.fetchByLdmNameAndGuid(SiteDetailCompositeService.LDM_NAME, params.site.guid)
                    if(siteGUID){
                        inputData.put('siteCode', siteGUID.domainKey)
                    } else {
                        throw new ApplicationException("site", new NotFoundException())
                    }
                } else {
                    inputData.put('siteCode', null)
                }
            }

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
    AvailableRoom get(String guid) {
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndGuid(AvailableRoom.LDM_NAME, guid)
        if (!globalUniqueIdentifier)
            throw new ApplicationException("room", new NotFoundException())
        HousingRoomDescriptionReadOnly housingRoomDescription = HousingRoomDescriptionReadOnly.get(globalUniqueIdentifier.domainId)
        if (!housingRoomDescription)
            throw new ApplicationException("room", new NotFoundException())
        BuildingDetail building = new BuildingDetail(GlobalUniqueIdentifier.findByLdmNameAndDomainKey(BuildingCompositeService.LDM_NAME, housingRoomDescription.buildingCode)?.guid)
        List occupancies = [new Occupancy(fetchLdmRoomLayoutTypeForBannerRoomType(housingRoomDescription.roomType), housingRoomDescription.capacity)]
        if(VERSION_V2.equals(getRequestedVersion())){
            SiteDetail site = new SiteDetail(GlobalUniqueIdentifier.findByLdmNameAndDomainKey(SiteDetailCompositeService.LDM_NAME, housingRoomDescription.campusCode)?.guid)
            return new AvailableRoomV2(housingRoomDescription, building, site, occupancies, globalUniqueIdentifier.guid, new Metadata(housingRoomDescription.dataOrigin))
        } else {
            return new AvailableRoom(housingRoomDescription, building, occupancies, globalUniqueIdentifier.guid, new Metadata(housingRoomDescription.dataOrigin))
        }

    }


    boolean checkIfRoomAvailable(Map params) {
        boolean roomAvailable = false
        validateParams(params)
        if (params.roomNumber?.trim() && params.building?.trim()) {
            Map filterParams = prepareSearchParams(params)
            filterParams.filterData.params << [roomNumber: params.roomNumber.toString()?.trim(), buildingCode: params.building?.trim()]
            roomAvailable = RoomsAvailabilityHelper.checkExistsAvailableRoomByRoomAndBuilding(filterParams.filterData)
        }
        return roomAvailable
    }


    private String fetchBannerRoomTypeForLdmRoomLayoutType(String ldmRoomLayoutType) {
        String roomType = null
        if (ldmRoomLayoutType) {
            IntegrationConfiguration integrationConfiguration = fetchAllByProcessCodeAndSettingNameAndTranslationValue(PROCESS_CODE, SETTING_ROOM_LAYOUT_TYPE, ldmRoomLayoutType)
            roomType = integrationConfiguration?.value
        }
        if (!roomType) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("missing.roomLayoutType", []))
        }
        return roomType
    }


    private String fetchLdmRoomLayoutTypeForBannerRoomType(String bannerRoomType) {
        String roomLayoutType = null
        if (bannerRoomType) {
            IntegrationConfiguration integrationConfiguration = findAllByProcessCodeAndSettingNameAndValue(PROCESS_CODE, SETTING_ROOM_LAYOUT_TYPE, bannerRoomType)
            roomLayoutType = integrationConfiguration?.translationValue
        }
        return roomLayoutType
    }


    private def fetchAllActiveRoomsByRoomTypes(def roomTypes, def pagingAndSortParams, boolean count = false) {
        log.trace "fetchAllActiveRoomsByRoomTypes:Begin"
        def result

        def params = [:]
        def criteria = []

        params.put("inactiveIndicator", "Y")
        //criteria.add([key: "inactiveIndicator", binding: "roomStatusInactiveIndicator", operator: Operators.NOT_EQUALS_IGNORE_CASE])

        // TODO: Not sure why IN operator of DynamicFinder requires list in this format
        def roomTypeObjects = []
        roomTypes.each {
            roomTypeObjects << [data: it]
        }
        params.put("roomTypes", roomTypeObjects)
        //criteria.add([key: "roomTypes", binding: "roomType", operator: Operators.IN])

        params.put("endDate", new Date())

        def query = """
                       from HousingRoomDescriptionReadOnly a
                       where lower(nvl(a.roomStatusInactiveIndicator,'N')) != lower(:inactiveIndicator)
                       and a.roomType in :roomTypes
                       and a.termEffective.code = (select min(b.termEffective.code)
                                                   from HousingRoomDescriptionReadOnly b left join b.termTo termB
                                                   where b.buildingCode = a.buildingCode
                                                   and b.roomNumber = a.roomNumber
                                                   and lower(nvl(b.roomStatusInactiveIndicator,'N')) != lower(:inactiveIndicator)
                                                   and (b.termTo is null OR termB.startDate > :endDate))
                    """
        DynamicFinder dynamicFinder = new DynamicFinder(HousingRoomDescriptionReadOnly.class, query, "a")
        if (count) {
            result = dynamicFinder.count([params: params, criteria: criteria])
            log.debug "Count query on SVQ_SLBRDEF_SLBBLDG returned $result"
        } else {
            result = dynamicFinder.find([params: params, criteria: criteria], pagingAndSortParams)
            log.debug "Query on SVQ_SLBRDEF_SLBBLDG returned ${result?.size()} rows"
        }

        log.trace "fetchAllActiveRoomsByRoomTypes:End"
        return result
    }

    /**
     * Get all Higher Education Data Model (HEDM) room types.
     *
     * @return
     */
    private def getHEDMRoomTypes() {
        def list = IntegrationConfiguration.findAllByProcessCodeAndSettingName(PROCESS_CODE, SETTING_ROOM_LAYOUT_TYPE)
        def intConfs = list?.findAll {
            ["Banquet", "Booth", "Classroom", "Empty", "Theater"].contains(it.translationValue)
        }
        def map = [:]
        intConfs?.each {
            if (!map.containsKey(it.translationValue)) {
                map.put(it.translationValue, it.value)
            }
        }
        if (map.size() == 0) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException('goriccr.not.found.message', [SETTING_ROOM_LAYOUT_TYPE]))
        }
        return map.values()
    }


    private def getAvailableRooms(def listHousingRoomDescriptionReadOnly) {
        def availableRooms = []
        listHousingRoomDescriptionReadOnly?.each { HousingRoomDescriptionReadOnly housingRoomDescription ->
            List occupancies = [new Occupancy(fetchLdmRoomLayoutTypeForBannerRoomType(housingRoomDescription.roomType), housingRoomDescription.capacity)]
            String buildingGuid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey(BuildingCompositeService.LDM_NAME, housingRoomDescription.buildingCode)?.guid
            String roomGuid = GlobalUniqueIdentifier.findByLdmNameAndDomainId(AvailableRoom.LDM_NAME, housingRoomDescription.id).guid
            BuildingDetail building = buildingGuid ? new BuildingDetail(buildingGuid) : null
            if(VERSION_V2.equals(getRequestedVersion())){
                String siteGuid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey(SiteDetailCompositeService.LDM_NAME, housingRoomDescription.campusCode)?.guid
                SiteDetail site = siteGuid ? new SiteDetail( siteGuid ) : null
                availableRooms << new AvailableRoom(housingRoomDescription, building, site, occupancies, roomGuid, new Metadata(housingRoomDescription.dataOrigin))
            } else{
                availableRooms << new AvailableRoom(housingRoomDescription, building, occupancies, roomGuid, new Metadata(housingRoomDescription.dataOrigin))
            }
        }
        return availableRooms
    }


    public AvailableRoom fetchByRoomBuildingAndTerm(String roomNumber, Building building, String termEffective) {
        AvailableRoom room
        if (roomNumber && building && termEffective) {
            Map params = [building: building, termEffective: termEffective]
            HousingRoomDescription housingRoomDescription = HousingRoomDescription.fetchValidRoomAndBuilding(roomNumber, params)
            room = get(GlobalUniqueIdentifier.findByLdmNameAndDomainId(LDM_NAME, housingRoomDescription.id)?.guid?.toLowerCase())
        }
        return room
    }


    private String getRequestedVersion() {
        String representationVersion = LdmService.getResponseRepresentationVersion()
        if (representationVersion == null) {
            // Assume latest (current) version
            representationVersion = VERSION_V2
        }
        return representationVersion
    }
}

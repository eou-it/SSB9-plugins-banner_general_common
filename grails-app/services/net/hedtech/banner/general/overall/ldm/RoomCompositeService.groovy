/*********************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.HousingRoomDescription
import net.hedtech.banner.general.overall.HousingRoomDescriptionReadOnly
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.ldm.utility.AvailableRoomHelper
import net.hedtech.banner.general.overall.ldm.v1.AvailableRoom
import net.hedtech.banner.general.overall.ldm.v1.BuildingDetail
import net.hedtech.banner.general.overall.ldm.v1.Occupancy
import net.hedtech.banner.general.overall.ldm.v2.Room
import net.hedtech.banner.general.overall.ldm.v4.RoomV4
import net.hedtech.banner.general.overall.ldm.v4.WeekDays
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
    private static final String PROCESS_CODE = "HEDM"
    private static final String SETTING_ROOM_LAYOUT_TYPE = "ROOM.OCCUPANCY.ROOMLAYOUTTYPE"
    private static final String SETTING_ROOM_LAYOUT_TYPE_V4= "ROOM.ROOMTYPE"
    private static final List<String> VERSIONS = ["v1", "v2","v4"]
    private static final String ROOM_LAYOUT_TYPE_SEMINAR = 'seminar'
    private static final String ROOM_LAYOUT_TYPE_CLASSROOM_V4 = 'classroom'
    private static final String FILTER_TYPE_ROOM_LAYOUT = 'roomLayoutType'
    private static final String FILTER_TYPE_TYPE = 'type'


    def roomTypeCompositeService


    List<AvailableRoom> list(Map params) {
        log.debug( "Start of List()" )
        def entities
        RestfulApiValidationUtility.correctMaxAndOffset(params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        List allowedSortFields = ['number', 'title']
        RestfulApiValidationUtility.validateSortField(params.sort?.trim(), allowedSortFields)
        RestfulApiValidationUtility.validateSortOrder(params.order?.trim())
        params.sort = fetchBannerDomainPropertyForLdmField(params.sort?.trim())
        if (RestfulApiValidationUtility.isQApiRequest(params)) {
            if("v4".equals( LdmService.getAcceptVersion(VERSIONS))){
                prepareQapiV4Request(params)
            }
            // POST /qapi/rooms (Search for available rooms)
            validateParams(params)
            Map filterParams = prepareSearchParams(params)
            log.debug( "Start of AvailableRoomHelper.fetchSearchAvailableRoom()" )
            def listOfObjectArrays = AvailableRoomHelper.fetchSearchAvailableRoom(filterParams.filterData, filterParams.pagingAndSortParams)
            log.debug( "End of AvailableRoomHelper.fetchSearchAvailableRoom()" )
            entities = []
            listOfObjectArrays?.each {
                entities << it[0]
            }
        } else {
            //v1- GET /api/rooms?filter[0][field]=roomLayoutType&filter[0][operator]=equals&filter[0][value]=Classroom
            //v4- GET /api/rooms?filter[0][field]=type&filter[0][operator]=equals&filter[0][value]=classroom
            Map filterData = prepareParams(params)
            def roomTypes
            String filterType = getFilterType(filterData)
            if (filterData.params.containsKey(filterType)) {
                roomTypes = [fetchBannerRoomTypeForLdmRoomLayoutType(filterData.params?.get(filterType)?.trim())]
            } else {
                roomTypes = getHEDMRoomTypes()
            }
            entities = fetchAllActiveRoomsByRoomTypes(roomTypes, params,filterData.pagingAndSortParams)
        }
        if(entities.size()==0 && params.containsKey('building.id')){
            throw new ApplicationException('rooms.building',new NotFoundException())
        }
        return getAvailableRooms(entities)
    }


    Long count(Map params) {
        if (RestfulApiValidationUtility.isQApiRequest(params)) {
            Map filterParams = prepareSearchParams(params)
            AvailableRoomHelper.fetchSearchAvailableRoom(filterParams.filterData, null, true)
        } else {
            Map filterData = prepareParams(params)
            def roomTypes
            String filterType = getFilterType(filterData)
            if (filterData.params.containsKey(filterData)) {
                roomTypes = [fetchBannerRoomTypeForLdmRoomLayoutType(filterData.params?.get(filterType)?.trim())]
            } else {
                roomTypes = getHEDMRoomTypes()
            }
            return fetchAllActiveRoomsByRoomTypes(roomTypes,params, null, true)
        }
    }


    private void validateParams(Map params) {
        validateRequiredFields(params)
        validateBeginAndEndDates(params)
        validateTimeFormat(params.startTime?.trim())
        validateTimeFormat(params.endTime?.trim())
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


    private void validateTimeFormat(String timeString) {
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
        if (!params.occupancies[0]?.maxOccupancy) {
            throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("missing.maxOccupancy", []))
        }
        if ("v4".equals(LdmService.getAcceptVersion(VERSIONS))) {
            if (!params.roomTypes[0]?.type) {
                throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("missing.roomLayoutType", []))
            }
        }
        else {
            if (!params.occupancies[0]?.roomLayoutType) {
                throw new ApplicationException(RoomCompositeService, new BusinessLogicValidationException("missing.roomLayoutType", []))
            }
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
        String contentTypeVersion = getContentTypeVersion(VERSIONS)
        String filterType = "v4".equals(LdmService.getAcceptVersion(VERSIONS))? params.roomTypes[0]?.type :params.occupancies[0]?.roomLayoutType
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
            roomTypes = [fetchBannerRoomTypeForLdmRoomLayoutType(filterType)]
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

        if (!"v1".equals(contentTypeVersion)) {
            if (params.containsKey('building')) {
                String buildingGuid = "v4".equals(contentTypeVersion)? params.building?.id?.trim()?.toLowerCase() : params.building?.trim()?.toLowerCase()
                if (buildingGuid) {
                    GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndGuid(BuildingCompositeService.LDM_NAME, buildingGuid)
                    if (globalUniqueIdentifier) {
                        inputData.put('buildingCode', globalUniqueIdentifier.domainKey)
                    } else {
                        throw new ApplicationException("building", new BusinessLogicValidationException("not.found.message", null))
                    }
                } else {
                    inputData.put('buildingCode', null)
                }
            }
            if (params.containsKey('site')) {
                String siteGuid = "v4".equals(contentTypeVersion)?  params.site?.id?.trim()?.toLowerCase() : params.site?.trim()?.toLowerCase()
                if (siteGuid) {
                    GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndGuid(SiteDetailCompositeService.LDM_NAME, siteGuid)
                    if (globalUniqueIdentifier) {
                        inputData.put('siteCode', globalUniqueIdentifier.domainKey)
                    } else {
                        throw new ApplicationException("site", new BusinessLogicValidationException("not.found.message", null))
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
        def allowedSearchFields = ['roomLayoutType','type']
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
        switch (LdmService.getAcceptVersion(VERSIONS)){
            case "v1" :
                return new AvailableRoom(housingRoomDescription, building, occupancies, globalUniqueIdentifier.guid, new Metadata(housingRoomDescription.dataOrigin))
            case "v2" :
                SiteDetail site = new SiteDetail(GlobalUniqueIdentifier.findByLdmNameAndDomainKey(SiteDetailCompositeService.LDM_NAME, housingRoomDescription.campusCode)?.guid)
                return new Room(housingRoomDescription, building, site, occupancies, globalUniqueIdentifier.guid, new Metadata(housingRoomDescription.dataOrigin))
            case "v4" :
                def roomTypes = [:]
                findAllByProcessCodeAndSettingName(PROCESS_CODE, getSettingNameForRoom_type(LdmService.getAcceptVersion(VERSIONS)))?.each { it ->
                    roomTypes.put( it?.value, it?.translationValue )
                }
                String occupanciesType = ROOM_LAYOUT_TYPE_CLASSROOM_V4.equals(roomTypes.get(housingRoomDescription?.roomType)) ? ROOM_LAYOUT_TYPE_SEMINAR : ''
                occupancies = [new Occupancy(occupanciesType, housingRoomDescription?.capacity)]
                SiteDetail site = housingRoomDescription?.siteGUID ? new SiteDetail(housingRoomDescription?.siteGUID) : null
                return new RoomV4(housingRoomDescription, building, site, occupancies, housingRoomDescription?.roomGUID,roomTypeCompositeService.list([:]).get(0))
        }
    }


    boolean checkIfRoomAvailable(Map params) {
        boolean roomAvailable = false
        validateParams(params)
        if (params.roomNumber?.trim() && params.building?.trim()) {
            Map filterParams = prepareSearchParams(RestfulApiValidationUtility.cloneMapExcludingKeys(params, ["roomNumber", "building"]))
            filterParams.filterData.params << [roomNumber: params.roomNumber.toString()?.trim(), buildingCode: params.building?.trim()]
            roomAvailable = AvailableRoomHelper.checkExistsAvailableRoomByRoomAndBuilding(filterParams.filterData)
        }
        return roomAvailable
    }


    private String fetchBannerRoomTypeForLdmRoomLayoutType(String ldmRoomLayoutType) {
        String roomType = null
        //fetching the Goricccr setting name for the room layout type
        String roomTypeSettinName = getSettingNameForRoom_type(LdmService.getAcceptVersion(VERSIONS))
        if (ldmRoomLayoutType) {
            IntegrationConfiguration integrationConfiguration = fetchAllByProcessCodeAndSettingNameAndTranslationValue(PROCESS_CODE, roomTypeSettinName, ldmRoomLayoutType)
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


    private def fetchAllActiveRoomsByRoomTypes(def roomTypes, def queryParams,def pagingAndSortParams, boolean count = false) {
        log.trace "fetchAllActiveRoomsByRoomTypes:Begin"
        def result

        def params = [:]
        def criteria = []

        params.put("inactiveIndicator", "Y")
        //criteria.add([key: "inactiveIndicator", binding: "roomStatusInactiveIndicator", operator: Operators.NOT_EQUALS_IGNORE_CASE])

        //Adding the criteria for data links for building
        if(queryParams?.containsKey('building.id')){
            criteria.add([key: 'buildingId', binding: 'buildingGUID', operator: Operators.EQUALS_IGNORE_CASE])
            params.put('buildingId',queryParams.get('building.id'))
        }

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
        String roomTypeSettinName = getSettingNameForRoom_type(LdmService.getAcceptVersion(VERSIONS))
        def list = IntegrationConfiguration.findAllByProcessCodeAndSettingName(PROCESS_CODE, roomTypeSettinName)
        def intConfs = list?.findAll {
            ["Banquet", "Booth", "Classroom", "Empty", "Theater","classroom"].contains(it.translationValue)
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
        log.debug( "Start of getAvailableRooms()" )
        def availableRooms = []
        Map roomType = [:]
        String roomTypeSettinName = getSettingNameForRoom_type(LdmService.getAcceptVersion(VERSIONS))
        findAllByProcessCodeAndSettingName(PROCESS_CODE, roomTypeSettinName)?.each { it ->
            roomType.put( it.value, it.translationValue )
        }
        listHousingRoomDescriptionReadOnly?.each { HousingRoomDescriptionReadOnly housingRoomDescription ->
            log.debug( "Start of Occupancy()" )
            List occupancies = [new Occupancy(roomType.get( housingRoomDescription.roomType ), housingRoomDescription.capacity)]
            log.debug( "End of Occupancy()" )
            BuildingDetail building = housingRoomDescription.buildingGUID ? new BuildingDetail(housingRoomDescription.buildingGUID) : null
            switch (LdmService.getAcceptVersion(VERSIONS)){
                case "v1" :
                    availableRooms << new AvailableRoom(housingRoomDescription, building, occupancies, housingRoomDescription.roomGUID, new Metadata(housingRoomDescription.dataOrigin))
                    break
                case "v2" :
                    SiteDetail site = housingRoomDescription.siteGUID ? new SiteDetail(housingRoomDescription.siteGUID) : null
                    availableRooms << new Room(housingRoomDescription, building, site, occupancies, housingRoomDescription.roomGUID, new Metadata(housingRoomDescription.dataOrigin))
                    break
                case "v4" :
                    String occupanciesType = ROOM_LAYOUT_TYPE_CLASSROOM_V4.equals(roomType.get( housingRoomDescription?.roomType )) ? ROOM_LAYOUT_TYPE_SEMINAR : ''
                    occupancies = [new Occupancy(occupanciesType, housingRoomDescription?.capacity)]
                    SiteDetail site = housingRoomDescription?.siteGUID ? new SiteDetail(housingRoomDescription?.siteGUID) : null
                    availableRooms << new RoomV4(housingRoomDescription, building, site, occupancies, housingRoomDescription?.roomGUID,roomTypeCompositeService.list([:]).get(0))
                    break
            }
        }
        log.debug( "End of getAvailableRooms()" )
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

    private String getFilterType(Map filterData){
        String filterType = null
        if(filterData.params.containsKey(FILTER_TYPE_ROOM_LAYOUT)){
            filterType = FILTER_TYPE_ROOM_LAYOUT
        }
        else if(filterData.params.containsKey(FILTER_TYPE_TYPE)){
            filterType = FILTER_TYPE_TYPE
        }
        return filterType
    }

    private String getSettingNameForRoom_type(String version){
        return ("v4".equals(version)? SETTING_ROOM_LAYOUT_TYPE_V4 :SETTING_ROOM_LAYOUT_TYPE)

    }

    private Map prepareQapiV4Request(Map request) {
        if (request?.recurrence?.timePeriod) {
            if (request?.recurrence?.timePeriod?.startOn) {
                if (request?.recurrence?.timePeriod?.startOn?.contains('T')) {
                    def datetime = request?.recurrence?.timePeriod?.startOn?.split('T')
                    request.put("startDate", datetime[0])
                    if (datetime[1].length() > 8) {
                        request.put("startTime", datetime[1]?.substring(0, 8))
                    }
                }
            }

            if(request?.recurrence?.timePeriod?.endOn){
                if(request?.recurrence?.timePeriod?.endOn?.contains('T')){
                    def datetime = request?.recurrence?.timePeriod?.endOn?.split('T')
                    request.put("endDate",datetime[0])
                    if(datetime[1].length()>8){
                        request.put("endTime",datetime[1].substring(0,8))
                    }
                }
            }
        }
        request?.recurrence?.remove("timePeriod")

        if(request?.recurrence?.repeatRule?.daysOfWeek){
            def weekList=[]
            request?.recurrence?.repeatRule?.daysOfWeek?.each{
                weekList.add(WeekDays.("${it}").value)
            }
            request.recurrence?.byDay = weekList
        }
        request?.recurrence?.remove("repeatRule")
        return request
    }

}

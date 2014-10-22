/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm
import net.hedtech.banner.exceptions.BusinessLogicValidationException


import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.AvailableRoomDescription
import net.hedtech.banner.general.overall.HousingRoomDescription
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.ldm.utility.RoomsAvailabilityHelper
import net.hedtech.banner.general.overall.ldm.v1.AvailableRoom
import net.hedtech.banner.general.overall.ldm.v1.BuildingDetail
import net.hedtech.banner.general.overall.ldm.v1.Occupancy
import net.hedtech.banner.general.system.Building
import net.hedtech.banner.general.system.DayOfWeek
import net.hedtech.banner.general.system.ldm.v1.Metadata
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


    List<AvailableRoom> list( Map params ) {
        if (RestfulApiValidationUtility.isQApiRequest( params )) {
            List rooms = []
            RestfulApiValidationUtility.correctMaxAndOffset( params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT )
            List allowedSortFields = ['number', 'title']
            RestfulApiValidationUtility.validateSortField( params.sort?.trim(), allowedSortFields )
            RestfulApiValidationUtility.validateSortOrder( params.order?.trim() )
            params.sort = fetchBannerDomainPropertyForLdmField( params.sort?.trim() )
            validateParams( params )
            Map filterParams = prepareSearchParams( params )
            List<AvailableRoomDescription> availableRoomDescriptions = RoomsAvailabilityHelper.fetchSearchAvailableRoom( filterParams.filterData, filterParams.pagingAndSortParams )
            availableRoomDescriptions.each {availableRoomDescription ->
                List occupancies = [new Occupancy( fetchLdmRoomLayoutTypeForBannerRoomType( availableRoomDescription.roomType ), availableRoomDescription.capacity )]
                String buildingGuid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey( BuildingCompositeService.LDM_NAME, availableRoomDescription.buildingCode )?.guid
                String roomGuid = GlobalUniqueIdentifier.findByLdmNameAndDomainId( AvailableRoom.LDM_NAME, availableRoomDescription.id )?.guid
                BuildingDetail building = buildingGuid ? new BuildingDetail( buildingGuid ) : null
                rooms << new AvailableRoom( availableRoomDescription, building, occupancies, roomGuid, new Metadata( availableRoomDescription.dataOrigin ) )
            }
            return rooms
        } else {
            List rooms = []
            RestfulApiValidationUtility.correctMaxAndOffset( params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT )
            List allowedSortFields = ['number', 'title']
            RestfulApiValidationUtility.validateSortField( params.sort?.trim(), allowedSortFields )
            RestfulApiValidationUtility.validateSortOrder( params.order?.trim() )
            params.sort = fetchBannerDomainPropertyForLdmField( params.sort?.trim() )
            Map filterParams = prepareParams( params )
            AvailableRoomDescription.fetchAllActiveRoomsByRoomType( filterParams.filterData, filterParams.pagingAndSortParams ).each { AvailableRoomDescription housingRoomDescription ->
                List occupancies = [new Occupancy( fetchLdmRoomLayoutTypeForBannerRoomType( housingRoomDescription.roomType ), housingRoomDescription.capacity )]
                String buildingGuid = GlobalUniqueIdentifier.findByLdmNameAndDomainKey( BuildingCompositeService.LDM_NAME, housingRoomDescription.buildingCode )?.guid
                String roomGuid = GlobalUniqueIdentifier.findByLdmNameAndDomainId( AvailableRoom.LDM_NAME, housingRoomDescription.id ).guid
                BuildingDetail building = buildingGuid ? new BuildingDetail( buildingGuid ) : null
                rooms << new AvailableRoom( housingRoomDescription, building, occupancies, roomGuid, new Metadata( housingRoomDescription.dataOrigin ) )
            }
            return rooms
        }
    }


    Long count( Map params ) {
        if (RestfulApiValidationUtility.isQApiRequest( params )) {
            Map filterParams = prepareSearchParams( params )
            RoomsAvailabilityHelper.countAllAvailableRoom( filterParams.filterData )
        } else {
            Map filterParams = prepareParams( params )
            return AvailableRoomDescription.countAllActiveRoomsByRoomType( filterParams.filterData )
        }
    }


    private void validateParams( Map params ) {
        validateRequiredFields( params )
        validateBeginAndEndDates( params )
        validateTimeFormat( params.startTime?.trim(), 'startTime' )
        validateTimeFormat( params.endTime?.trim(), 'endTime' )
        validateBeginAndEndTimes( params )
        validateRecurrence( params )
        validateOccupancies( params )
    }


    private void validateRequiredFields( Map params ) {
        if (!params.startDate?.trim()) {
            throw new ApplicationException( RoomCompositeService, new BusinessLogicValidationException("missing.startDate",[]) )
        }
        if (!params.endDate?.trim()) {
            throw new ApplicationException( RoomCompositeService, new BusinessLogicValidationException("missing.endDate",[]) )
        }
        if (!params.startTime?.trim()) {
            throw new ApplicationException( RoomCompositeService, new BusinessLogicValidationException("missing.startTime",[]) )
        }
        if (!params.endTime?.trim()) {
            throw new ApplicationException( RoomCompositeService, new BusinessLogicValidationException("missing.endTime",[]) )
        }
    }


    private void validateBeginAndEndDates( Map params ) {
        Date startDate = convertString2Date( params.startDate?.trim() )
        Date endDate = convertString2Date( params.endDate?.trim() )

        if (startDate > endDate) {
            throw new ApplicationException( RoomCompositeService, new BusinessLogicValidationException("startDate.laterThanEndDate",[]) )
        }
    }


    private void validateTimeFormat( String timeString, String fieldName ) {
        String timeFormat = getTimeFormat().toLowerCase().replace( 'hh', HOUR_FORMAT ).replace( 'mm', MINUTE_FORMAT ).replace( 'ss', SECOND_FORMAT )
        if (timeString && (timeString.length() != getTimeFormat().length() || !(timeString ==~ /$timeFormat/))) {
            throw new ApplicationException( RoomCompositeService, new BusinessLogicValidationException("invalid.timeFormat",[]) )
        }
    }


    private void validateBeginAndEndTimes( Map params ) {
        Integer startTimeAsInteger = Integer.valueOf( getTimeInHHmmFormat( params.startTime?.trim() ) )
        Integer endTimeAsInteger = Integer.valueOf( getTimeInHHmmFormat( params.endTime?.trim() ) )
        if (startTimeAsInteger >= endTimeAsInteger) {
            throw new ApplicationException( RoomCompositeService, new BusinessLogicValidationException("startTime.laterThanEndTime",[]) )
        }
    }


    private void validateRecurrence( Map params ) {
        if (!params.recurrence) {
            throw new ApplicationException( RoomCompositeService, new BusinessLogicValidationException("missing.recurrence",[]) )
        }
        validateByDays( params )
    }


    private void validateByDays( Map params ) {
        if (!params.recurrence?.byDay) {
            throw new ApplicationException( RoomCompositeService, new BusinessLogicValidationException("missing.recurrence.byDay",[]) )
        }
        List<DayOfWeek> days = DayOfWeek.list()
        if (days.description.intersect( params.recurrence?.byDay ).isEmpty() || params.recurrence?.byDay?.size() > 7) {
            throw new ApplicationException( RoomCompositeService, new BusinessLogicValidationException("invalid.recurrence.byDay",[]) )
        }
        List validDays = []
        Date startDate = convertString2Date( params.startDate?.trim() )
        Date endDate = convertString2Date( params.endDate?.trim() )
        int noOfDays = (int) use( groovy.time.TimeCategory ) {
            def duration = endDate - startDate
            duration.days
        }
        noOfDays += 1
        if (noOfDays == 1) {
            validDays.add( days.find {it.number == startDate.getDay().toString()}?.description )
        } else if (noOfDays <= 7) {
            for (int i = 0; i < noOfDays; i++) {
                Date calculatedDate = startDate + i
                validDays.add( days.find {it.number == calculatedDate.getDay().toString()}?.description )
            }
        } else if (noOfDays > 7) {
            validDays = days.description
        }
        List invalidDays = params.recurrence?.byDay - validDays
        if (invalidDays) {
            throw new ApplicationException( RoomCompositeService, new BusinessLogicValidationException("invalid.recurrence.byDay",[]) )
        }
    }


    private void validateOccupancies( Map params ) {
        if (!params.occupancies) {
            throw new ApplicationException( RoomCompositeService, new BusinessLogicValidationException("missing.occupancies",[]) )
        }
        if (!params.occupancies[0]?.roomLayoutType) {
            throw new ApplicationException( RoomCompositeService, new BusinessLogicValidationException("missing.roomLayoutType",[]) )
        }
        if (!params.occupancies[0]?.maxOccupancy) {
            throw new ApplicationException( RoomCompositeService, new BusinessLogicValidationException("missing.maxOccupancy",[]) )
        }
        try {
            Integer.valueOf( params.occupancies[0]?.maxOccupancy )
        } catch (NumberFormatException nfe) {
            throw new ApplicationException( RoomCompositeService, new BusinessLogicValidationException("invalid.maxOccupancy",[]) )
        }
    }


    private Map prepareParams( Map params ) {
        validateSearchCriteria( params )
        def filterMap = QueryBuilder.getFilterData( params )
        def filterData = [params: [roomType: '%']]
        if (filterMap.params.containsKey( 'roomLayoutType' )) {
            filterData.params = [roomType: fetchBannerRoomTypeForLdmRoomLayoutType( filterMap.params?.roomLayoutType?.trim() )]
        }
        return [filterData: filterData, pagingAndSortParams: filterMap.pagingAndSortParams]
    }


    private Map prepareSearchParams( Map params ) {
        validateSearchCriteria( params )
        def filterMap = QueryBuilder.getFilterData( params )
        Map inputData = [:]

        inputData.put( 'startDate', convertString2Date( params.startDate?.trim() ) )
        inputData.put( 'endDate', convertString2Date( params.endDate?.trim() ) )

        inputData.put( 'beginTime', getTimeInHHmmFormat( params.startTime?.trim() ) )
        inputData.put( 'endTime', getTimeInHHmmFormat( params.endTime?.trim() ) )

        def daysList = params.recurrence.byDay
        DayOfWeek.list().each {DayOfWeek day ->
            inputData.put( day.description.toLowerCase(), daysList.contains( day.description ) ? day.code : null )
        }
        if (params.occupancies) {
            inputData.put( 'roomType', fetchBannerRoomTypeForLdmRoomLayoutType( params.occupancies[0]?.roomLayoutType ) )
            inputData.put( 'capacity', params.occupancies[0]?.maxOccupancy )
        } else {
            inputData.put( 'roomType', '%' )
            inputData.put( 'capacity', null )
        }

        def filterData = [params: inputData, criteria: []]
        return [filterData: filterData, pagingAndSortParams: filterMap.pagingAndSortParams]
    }


    private void validateSearchCriteria( Map params ) {
        def filters = QueryBuilder.createFilters( params )
        def allowedSearchFields = ['roomLayoutType']
        def allowedOperators = [Operators.EQUALS]
        RestfulApiValidationUtility.validateCriteria( filters, allowedSearchFields, allowedOperators )
    }


    @Transactional(readOnly = true)
    AvailableRoom get( String guid ) {
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndGuid( AvailableRoom.LDM_NAME, guid )
        if (!globalUniqueIdentifier)
            throw new ApplicationException( GlobalUniqueIdentifierService.API, new NotFoundException( id: "Room" ) )
        AvailableRoomDescription housingRoomDescription = AvailableRoomDescription.get( globalUniqueIdentifier.domainId )
        if (!housingRoomDescription)
            throw new ApplicationException( GlobalUniqueIdentifierService.API, new NotFoundException( id: "Room" ) )
        BuildingDetail building = new BuildingDetail(GlobalUniqueIdentifier.findByLdmNameAndDomainKey(BuildingCompositeService.LDM_NAME, housingRoomDescription.buildingCode )?.guid)
        List occupancies = [new Occupancy( fetchLdmRoomLayoutTypeForBannerRoomType( housingRoomDescription.roomType ), housingRoomDescription.capacity )]
        return new AvailableRoom( housingRoomDescription, building, occupancies, globalUniqueIdentifier.guid, new Metadata( housingRoomDescription.dataOrigin ) )
    }


    boolean checkIfRoomAvailable( Map params ) {
        boolean roomAvailable = false
        validateParams( params )
        if (params.roomNumber?.trim() && params.building?.trim()) {
            Map filterParams = prepareSearchParams( params )
            filterParams.filterData.params << [roomNumber: params.roomNumber.toString()?.trim(), buildingCode: params.building?.trim()]
            roomAvailable = RoomsAvailabilityHelper.checkExistsAvailableRoomByRoomAndBuilding( filterParams.filterData )
        }
        return roomAvailable
    }


    private String fetchBannerRoomTypeForLdmRoomLayoutType( String ldmRoomLayoutType ) {
        String roomType = null
        if (ldmRoomLayoutType) {
            IntegrationConfiguration integrationConfiguration = fetchAllByProcessCodeAndSettingNameAndTranslationValue( 'LDM', 'ROOM.OCCUPANCY.ROOMLAYOUTTYPE', ldmRoomLayoutType )
            roomType = integrationConfiguration?.value
        }
        if (!roomType) {
            throw new ApplicationException( RoomCompositeService, new BusinessLogicValidationException("missing.roomLayoutType",[]) )
        }
        return roomType
    }


    public String fetchLdmRoomLayoutTypeForBannerRoomType( String bannerRoomType ) {
        String roomLayoutType = null
        if (bannerRoomType) {
            IntegrationConfiguration integrationConfiguration = findAllByProcessCodeAndSettingNameAndValue( 'LDM', 'ROOM.OCCUPANCY.ROOMLAYOUTTYPE', bannerRoomType )
            roomLayoutType = integrationConfiguration?.translationValue
        }
        return roomLayoutType
    }


    public AvailableRoom fetchByRoomBuildingAndTerm( String roomNumber, Building building, String termEffective ) {
        AvailableRoom room
        if(roomNumber && building && termEffective) {
            Map params = [building: building, termEffective: termEffective]
            HousingRoomDescription housingRoomDescription = HousingRoomDescription.fetchValidRoomAndBuilding( roomNumber, params )
            room = get( GlobalUniqueIdentifier.findByLdmNameAndDomainId( LDM_NAME, housingRoomDescription.id )?.guid?.toLowerCase() )
        }
        return room
    }
}

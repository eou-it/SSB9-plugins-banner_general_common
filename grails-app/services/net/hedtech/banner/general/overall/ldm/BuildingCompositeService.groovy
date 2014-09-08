/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm

import grails.util.GrailsNameUtils
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.HousingLocationBuildingDescription
import net.hedtech.banner.general.overall.HousingRoomDescription
import net.hedtech.banner.general.overall.ldm.v1.BuildingDetail
import net.hedtech.banner.general.overall.ldm.v1.Occupancy
import net.hedtech.banner.general.overall.ldm.v1.Room
import net.hedtech.banner.general.system.Building
import net.hedtech.banner.general.system.Campus
import net.hedtech.banner.general.system.ldm.v1.Metadata
import net.hedtech.banner.general.system.ldm.v1.SiteDetail
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
class BuildingCompositeService {


    private static final String LDM_NAME = 'buildings'
    def housingLocationBuildingDescriptionService
    def siteDetailCompositeService

    private static final String ROOM_LAYOUT_TYPE_CLASSROOM = 'Classroom'

    private HashMap ldmFieldToBannerDomainPropertyMap = [
            abbreviation: 'building.code',
            title       : 'building.description'
    ]


    List<BuildingDetail> list( Map params ) {
        List buildings = []
        List allowedSortFields = ['abbreviation', 'title']
        RestfulApiValidationUtility.correctMaxAndOffset( params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT )
        RestfulApiValidationUtility.validateSortField(params.sort, allowedSortFields)
        RestfulApiValidationUtility.validateSortOrder(params.order)
        params.sort = ldmFieldToBannerDomainPropertyMap[params.sort]

        List<HousingLocationBuildingDescription> housingLocationBuildingDescriptions = housingLocationBuildingDescriptionService.list( params ) as List
        housingLocationBuildingDescriptions.each {housingLocationBuildingDescription ->
            SiteDetail siteDetail = siteDetailCompositeService.fetchByCampusCode( housingLocationBuildingDescription?.campus?.code )
            List<Room> rooms = getRooms(housingLocationBuildingDescription?.building)
            buildings << new BuildingDetail( housingLocationBuildingDescription, siteDetail, GlobalUniqueIdentifier.findByLdmNameAndDomainId( LDM_NAME, housingLocationBuildingDescription.id )?.guid.toLowerCase(), rooms, new Metadata(housingLocationBuildingDescription.dataOrigin))
        }
        return buildings
    }


    Long count() {
        return housingLocationBuildingDescriptionService.count()
    }


    BuildingDetail get( String guid ) {
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndGuid( LDM_NAME, guid )
        if (!globalUniqueIdentifier) {
            throw new ApplicationException( GlobalUniqueIdentifierService.API, new NotFoundException( id: GrailsNameUtils.getNaturalName( Building.class.simpleName ) ) )
        }

        HousingLocationBuildingDescription housingLocationBuildingDescription = housingLocationBuildingDescriptionService.get( globalUniqueIdentifier.domainId )
        if (!housingLocationBuildingDescription) {
            throw new ApplicationException( GlobalUniqueIdentifierService.API, new NotFoundException( id: GrailsNameUtils.getNaturalName( Building.class.simpleName ) ) )
        }

        SiteDetail siteDetail = siteDetailCompositeService.fetchByCampusCode( housingLocationBuildingDescription?.campus?.code )
        List<Room> rooms = getRooms(housingLocationBuildingDescription?.building)
        return new BuildingDetail( housingLocationBuildingDescription, siteDetail, globalUniqueIdentifier.guid.toLowerCase(), rooms, new Metadata(housingLocationBuildingDescription.dataOrigin))
    }


    BuildingDetail fetchByBuildingId( Long domainId ) {
        if (null == domainId) {
            return null
        }

        HousingLocationBuildingDescription housingLocationBuildingDescription = HousingLocationBuildingDescription.get( domainId )
        if (!housingLocationBuildingDescription) {
            return null
        }

        SiteDetail siteDetail = siteDetailCompositeService.fetchByCampusCode( housingLocationBuildingDescription?.campus?.code )
        List<Room> rooms = getRooms(housingLocationBuildingDescription?.building)
        return new BuildingDetail( housingLocationBuildingDescription, siteDetail, GlobalUniqueIdentifier.findByLdmNameAndDomainId( LDM_NAME, housingLocationBuildingDescription.id )?.guid.toLowerCase(), rooms, new Metadata(housingLocationBuildingDescription.dataOrigin))
    }


    BuildingDetail fetchByBuildingCode( String buildingCode ) {
        if (null == buildingCode) {
            return null
        }
        HousingLocationBuildingDescription housingLocationBuildingDescription = HousingLocationBuildingDescription.findByBuilding(
                net.hedtech.banner.general.system.Building.findByCode( buildingCode ) )
        if (!housingLocationBuildingDescription) {
            return null
        }

        SiteDetail siteDetail = siteDetailCompositeService.fetchByCampusCode( housingLocationBuildingDescription?.campus?.code )
        List<Room> rooms = getRooms(housingLocationBuildingDescription?.building)
        return new BuildingDetail( housingLocationBuildingDescription, siteDetail, GlobalUniqueIdentifier.findByLdmNameAndDomainId( LDM_NAME, housingLocationBuildingDescription.id )?.guid.toLowerCase(), rooms, new Metadata(housingLocationBuildingDescription.dataOrigin))
    }


    List<BuildingDetail> fetchByCampusCode( String campusCode ) {
        List buildings = []
        def siteDetail
        def rooms

        if (null == campusCode) {
            return null
        }

        Campus campus = net.hedtech.banner.general.system.Campus.findByCode( campusCode )
        List<HousingLocationBuildingDescription> housingLocationBuildingDescriptions = HousingLocationBuildingDescription.findAllByCampus(campus)

        housingLocationBuildingDescriptions.each {housingLocationBuildingDescription ->
            buildings << new BuildingDetail( housingLocationBuildingDescription, siteDetail, GlobalUniqueIdentifier.findByLdmNameAndDomainId( LDM_NAME, housingLocationBuildingDescription.id )?.guid.toLowerCase(), rooms, new Metadata(housingLocationBuildingDescription.dataOrigin))
        }
        return buildings
    }


    private List<Room> getRooms(def building){
        List rooms = []
        net.hedtech.banner.general.overall.ldm.v1.BuildingDetail buildingDetail
        if (null == building) {
            return null
        }

        List<HousingRoomDescription> housingRoomDescriptions = HousingRoomDescription.findAllByBuilding(building)
        housingRoomDescriptions.each {housingRoomDescription ->
            List occupancies = [new Occupancy(ROOM_LAYOUT_TYPE_CLASSROOM, housingRoomDescription.capacity)]
            rooms << new Room(housingRoomDescription, buildingDetail, occupancies, GlobalUniqueIdentifier.findByLdmNameAndDomainId(Room.LDM_NAME, housingRoomDescription.id).guid.toLowerCase(), new Metadata(housingRoomDescription.dataOrigin))
        }
        return rooms
    }
}

/*********************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.HousingLocationBuildingDescription
import net.hedtech.banner.general.overall.HousingRoomDescriptionReadOnly
import net.hedtech.banner.general.overall.ldm.v1.AvailableRoom
import net.hedtech.banner.general.overall.ldm.v1.BuildingDetail
import net.hedtech.banner.general.system.Campus
import net.hedtech.banner.general.system.ldm.SiteDetailCompositeService
import net.hedtech.banner.general.system.ldm.v1.Metadata
import net.hedtech.banner.general.system.ldm.v1.SiteDetail
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
class BuildingCompositeService {


    public static final String LDM_NAME = 'buildings'
    def housingLocationBuildingDescriptionService
    def siteDetailCompositeService
    private static final List<String> VERSIONS = ["v1", "v2","v3","v4"]
    private HashMap ldmFieldToBannerDomainPropertyMap = [
            abbreviation: 'building.code',
            title       : 'building.description',
            code        : 'building.code'
    ]

    List<BuildingDetail> list( Map params ) {
        List buildings = []
        List allowedSortFields = ("v4".equals(LdmService.getAcceptVersion(VERSIONS))? ['code', 'title']:['abbreviation', 'title'])
        RestfulApiValidationUtility.correctMaxAndOffset( params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT )
        RestfulApiValidationUtility.validateSortField(params.sort, allowedSortFields)
        RestfulApiValidationUtility.validateSortOrder(params.order)
        params.sort = ldmFieldToBannerDomainPropertyMap[params.sort]
        List<HousingLocationBuildingDescription> housingLocationBuildingDescriptions=fetchBuildingDetails(params)

        housingLocationBuildingDescriptions.each {housingLocationBuildingDescription ->
            SiteDetail siteDetail = new SiteDetail(GlobalUniqueIdentifier.findByLdmNameAndDomainKey( SiteDetailCompositeService.LDM_NAME,housingLocationBuildingDescription?.campus?.code)?.guid )
            List<AvailableRoom> rooms = getRooms(housingLocationBuildingDescription?.building)
            buildings << new BuildingDetail( housingLocationBuildingDescription, siteDetail, GlobalUniqueIdentifier.findByLdmNameAndDomainId( LDM_NAME, housingLocationBuildingDescription.id )?.guid.toLowerCase(), rooms, new Metadata(housingLocationBuildingDescription.dataOrigin))
        }
        return buildings
    }

    private def fetchBuildingDetails(Map params,boolean count=false) {
        List<HousingLocationBuildingDescription> housingLocationBuildingDescriptions
        SiteDetail detail = null
        if(params.get('site.id')){
            detail = siteDetailCompositeService.get(params.get('site.id'))
        }
        if(count){
            if(params.get('site.id')){
                return HousingLocationBuildingDescription.fetchAllByCampuses([detail?.code]).size()
        }
            return housingLocationBuildingDescriptionService.count()
        }
        if (params.get('site.id')) {
            housingLocationBuildingDescriptions = HousingLocationBuildingDescription.fetchAllByCampuses([detail?.code]) as List
        } else {
            housingLocationBuildingDescriptions = housingLocationBuildingDescriptionService.list(params) as List
        }
        housingLocationBuildingDescriptions
    }


    Long count(params) {
        return fetchBuildingDetails(params,true)
    }


    BuildingDetail get( String guid ) {
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndGuid( LDM_NAME, guid )
        if (!globalUniqueIdentifier) {
            throw new ApplicationException("building", new NotFoundException())
        }

        HousingLocationBuildingDescription housingLocationBuildingDescription = housingLocationBuildingDescriptionService.get( globalUniqueIdentifier.domainId )
        if (!housingLocationBuildingDescription) {
            throw new ApplicationException("building", new NotFoundException())
        }

        SiteDetail siteDetail = siteDetailCompositeService.fetchByCampusCode( housingLocationBuildingDescription?.campus?.code )
        List<AvailableRoom> rooms = getRooms(housingLocationBuildingDescription?.building)
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
        List<AvailableRoom> rooms = getRooms(housingLocationBuildingDescription?.building)
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
        List<AvailableRoom> rooms = getRooms(housingLocationBuildingDescription?.building)
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


    private List<AvailableRoom> getRooms(def building){
        List rooms = []
        if (null == building) {
            return null
        }

        List<HousingRoomDescriptionReadOnly> housingRoomDescriptions = HousingRoomDescriptionReadOnly.findAllByBuildingCode(building)
        housingRoomDescriptions.each {housingRoomDescription ->
            rooms << new AvailableRoom(GlobalUniqueIdentifier.findByLdmNameAndDomainId(AvailableRoom.LDM_NAME, housingRoomDescription.id).guid.toLowerCase())
        }
        return rooms
    }
}

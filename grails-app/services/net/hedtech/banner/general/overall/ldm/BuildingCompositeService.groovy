/*********************************************************************************
 Copyright 2014-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralCommonConstants
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

/**
 * <p> REST End point for Building Service.</p>
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
class BuildingCompositeService extends  LdmService{


    public static final String LDM_NAME = 'buildings'
    def housingLocationBuildingDescriptionService
    def siteDetailCompositeService
    private static final List<String> VERSIONS = [GeneralCommonConstants.VERSION_V1,GeneralCommonConstants.VERSION_V4]
    private static final HashMap ldmFieldToBannerDomainPropertyMap = [
            abbreviation: 'building.code',
            title       : 'building.description',
            code        : 'building.code'
    ]
    private static final String SITE_FILTER_NAME = 'site.id'
    private static final String BUILDING_SURROGATE_ID = 'building.id'

    /**
     * GET /api/buildings
     * @param params
     * @return List
     */
    List<BuildingDetail> list( Map params ) {
        List buildings = []
        List allowedSortFields = (GeneralCommonConstants.VERSION_V4.equals(LdmService.getAcceptVersion(VERSIONS))? [GeneralCommonConstants.CODE, GeneralCommonConstants.TITLE]:[GeneralCommonConstants.ABBREVIATION, GeneralCommonConstants.TITLE])
        RestfulApiValidationUtility.correctMaxAndOffset( params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT )

        if (params.containsKey("sort")) {
            RestfulApiValidationUtility.validateSortField(params.sort, allowedSortFields)
            params.sort = ldmFieldToBannerDomainPropertyMap[params.sort]
        }

        if (params.containsKey("order")) {
            RestfulApiValidationUtility.validateSortOrder(params.order)
        } else {
            params.put('order', "asc")
        }

        if (!params.sort) {
            params.sort = BUILDING_SURROGATE_ID
        }
        fetchBuildingDetails(params).each {housingLocationBuildingDescription ->
            SiteDetail siteDetail = new SiteDetail(globalUniqueIdentifierService.fetchByDomainKeyAndLdmName(housingLocationBuildingDescription.campus?.code, SiteDetailCompositeService.LDM_NAME)?.guid )
            List<AvailableRoom> rooms = getRooms(housingLocationBuildingDescription.building)
            buildings << new BuildingDetail( housingLocationBuildingDescription, siteDetail, globalUniqueIdentifierService.fetchByLdmNameAndDomainId( LDM_NAME, housingLocationBuildingDescription.id )?.guid?.toLowerCase(), rooms, new Metadata(housingLocationBuildingDescription.dataOrigin))
        }
        return buildings
    }
    /**
     *
     * @param params
     * @return
     */
    private def fetchBuildingDetails(Map params) {
        if (GeneralCommonConstants.VERSION_V4.equalsIgnoreCase(getAcceptVersion(VERSIONS)) && params.containsKey(SITE_FILTER_NAME)) {
            SiteDetail detail = siteDetailCompositeService.get(params.get(SITE_FILTER_NAME))
            return housingLocationBuildingDescriptionService.fetchAllByCampuses([detail?.code]) as List
        } else {
            return housingLocationBuildingDescriptionService.list(params) as List
        }

    }

    /**
     * GET /api/buildings
     * @param params
     * @return count
     */
    Long count(params) {
        if (GeneralCommonConstants.VERSION_V4.equalsIgnoreCase(getAcceptVersion(VERSIONS)) && params.containsKey(SITE_FILTER_NAME)) {
            SiteDetail detail = siteDetailCompositeService.get(params.get(SITE_FILTER_NAME))
            return housingLocationBuildingDescriptionService.countAllByCampuses([detail?.code])
        } else {
            return housingLocationBuildingDescriptionService.count()
        }

    }

    /**
     * GET /api/buildings/<guid>
     * @param params
     * @return BuildingDetail
     */
    BuildingDetail get( String guid ) {
        GlobalUniqueIdentifier globalUniqueIdentifier = globalUniqueIdentifierService.fetchByLdmNameAndGuid( LDM_NAME, guid )
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

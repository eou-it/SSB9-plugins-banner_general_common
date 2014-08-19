/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm

import grails.util.GrailsNameUtils
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.HousingLocationBuildingDescription
import net.hedtech.banner.general.overall.ldm.v1.Building
import net.hedtech.banner.general.system.ldm.v1.SiteDetail
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
class BuildingCompositeService {

    private static final String LDM_NAME = 'buildings'
    def housingLocationBuildingDescriptionService
    def siteDetailCompositeService

	
    List<Building> list( Map params ) {
        List buildings = []
        List allowedSortFields = ['abbreviation', 'title']
		private HashMap ldmFieldToBannerDomainPropertyMap = [
            abbreviation: 'building.code',
            title       : 'building.description'
		]

        RestfulApiValidationUtility.correctMaxAndOffset( params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT )
        RestfulApiValidationUtility.validateSortField(params.sort, allowedSortFields)
        RestfulApiValidationUtility.validateSortOrder(params.order)
        params.sort = ldmFieldToBannerDomainPropertyMap[params.sort]

        List<HousingLocationBuildingDescription> housingLocationBuildingDescriptions = housingLocationBuildingDescriptionService.list( params ) as List
        housingLocationBuildingDescriptions.each {housingLocationBuildingDescription ->
            SiteDetail siteDetail = siteDetailCompositeService.fetchByCampusCode( housingLocationBuildingDescription?.campus?.code )
            buildings << new Building( housingLocationBuildingDescription, siteDetail, GlobalUniqueIdentifier.findByLdmNameAndDomainId( LDM_NAME, housingLocationBuildingDescription.id )?.guid )
        }
        return buildings
    }


    Long count() {
        return housingLocationBuildingDescriptionService.count()
    }


    Building get( String guid ) {
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndGuid( LDM_NAME, guid )
        if (!globalUniqueIdentifier) {
            throw new ApplicationException( GlobalUniqueIdentifierService.API, new NotFoundException( id: GrailsNameUtils.getNaturalName( Building.class.simpleName ) ) )
        }

        HousingLocationBuildingDescription housingLocationBuildingDescription = housingLocationBuildingDescriptionService.get( globalUniqueIdentifier.domainId )
        if (!housingLocationBuildingDescription) {
            throw new ApplicationException( GlobalUniqueIdentifierService.API, new NotFoundException( id: GrailsNameUtils.getNaturalName( Building.class.simpleName ) ) )
        }

        SiteDetail siteDetail = siteDetailCompositeService.fetchByCampusCode( housingLocationBuildingDescription?.campus?.code )

        return new Building( housingLocationBuildingDescription, siteDetail, globalUniqueIdentifier.guid )
    }


    Building fetchByBuildingId( Long domainId ) {
        if (null == domainId) {
            return null
        }

        HousingLocationBuildingDescription housingLocationBuildingDescription = HousingLocationBuildingDescription.get( domainId )
        if (!housingLocationBuildingDescription) {
            return null
        }

        SiteDetail siteDetail = siteDetailCompositeService.fetchByCampusCode( housingLocationBuildingDescription?.campus?.code )
        return new Building( housingLocationBuildingDescription, siteDetail, GlobalUniqueIdentifier.findByLdmNameAndDomainId( LDM_NAME, domainId )?.guid )
    }


    Building fetchByBuildingCode( String buildingCode ) {
        if (null == buildingCode) {
            return null
        }
        HousingLocationBuildingDescription housingLocationBuildingDescription = HousingLocationBuildingDescription.findByBuilding(
                net.hedtech.banner.general.system.Building.findByCode( buildingCode ) )
        if (!housingLocationBuildingDescription) {
            return null
        }

        SiteDetail siteDetail = siteDetailCompositeService.fetchByCampusCode( housingLocationBuildingDescription?.campus?.code )
        return new Building( housingLocationBuildingDescription, siteDetail, GlobalUniqueIdentifier.findByLdmNameAndDomainId( LDM_NAME, housingLocationBuildingDescription.id )?.guid )
    }


}

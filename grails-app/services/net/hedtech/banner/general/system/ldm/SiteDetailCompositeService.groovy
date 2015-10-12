/** *******************************************************************************
 Copyright 2014-2015 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.system.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.system.Campus
import net.hedtech.banner.general.system.ldm.v1.Metadata
import net.hedtech.banner.general.system.ldm.v1.SiteDetail
import net.hedtech.banner.query.DynamicFinder
import net.hedtech.banner.query.QueryBuilder
import net.hedtech.banner.query.operators.Operators
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * RESTful APIs for Site LDM (/base/domain/site/v1/site.json-schema)
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
class SiteDetailCompositeService {

    public static final String LDM_NAME = "campuses"
    private static final String CODE ="code"
    private static final String DESCRIPTION ="description"
    private static final String QUERY = """from Campus a"""
    private static final String ENTITY ="a"
    private static final List<String> VERSIONS = ["v1","v4"]


    def campusService
    def buildingCompositeService

    /**
     * GET /api/sites/<guid>
     *
     * @param guid
     * @return
     */
    @Transactional(readOnly = true)
    SiteDetail get(String guid) {
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndGuid(LDM_NAME, guid)
        if (!globalUniqueIdentifier) {
            throw new ApplicationException("site", new NotFoundException())
        }

        Campus campus = Campus.get(globalUniqueIdentifier.domainId)
        if(!campus?.getDescription() && LdmService.getAcceptVersion(VERSIONS).equalsIgnoreCase('v4')){
            campus.setDescription(campus?.getCode())
        }
        if (!campus) {
            throw new ApplicationException("site", new NotFoundException())
        }
        def buildings = buildingCompositeService.fetchByCampusCode(campus.code)
        return new SiteDetail(guid, campus, buildings, new Metadata(campus.dataOrigin));
    }

    /**
     * GET /api/sites
     *
     * @param map
     * @return
     */
    def list(Map map) {
        def sites = []
        def buildings = []

        RestfulApiValidationUtility.correctMaxAndOffset(map, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)

        RestfulApiValidationUtility.correctMaxAndOffset(map, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        List allowedSortFields = ("v4".equals(LdmService.getAcceptVersion(VERSIONS))? ['code', 'title']:['abbreviation', 'title'])
        RestfulApiValidationUtility.validateSortField(map.sort, allowedSortFields)
        RestfulApiValidationUtility.validateSortOrder(map.order)
        map.sort = LdmService.fetchBannerDomainPropertyForLdmField(map.sort)
        def filters = QueryBuilder.createFilters(map)
        def allowedSearchFields = [CODE, DESCRIPTION]
        def allowedOperators = [Operators.EQUALS, Operators.EQUALS_IGNORE_CASE, Operators.CONTAINS, Operators.STARTS_WITH]
        RestfulApiValidationUtility.validateCriteria(filters, allowedSearchFields, allowedOperators)
        RestfulApiValidationUtility.validateSortField(map.sort,allowedSearchFields)
        def filterMap = QueryBuilder.getFilterData(map)
        DynamicFinder dynamicFinder = new DynamicFinder(Campus.class, QUERY, ENTITY)
        List<Campus> campuses = dynamicFinder.find([params: filterMap.params, criteria: filterMap.criteria], filterMap.pagingAndSortParams)

        campuses.each { campus ->
            buildings = buildingCompositeService.fetchByCampusCode(campus.code)
            if(!campus?.getDescription() && LdmService.getAcceptVersion(VERSIONS).equalsIgnoreCase('v4')){
                campus.setDescription(campus?.getCode())
            }
            sites << new SiteDetail(GlobalUniqueIdentifier.findByLdmNameAndDomainId(LDM_NAME, campus.id).guid, campus, buildings, new Metadata(campus.dataOrigin))
         }

        return sites
    }

    /**
     * GET /api/organizations
     *
     * @return
     */
    Long count() {
        return campusService.count()
    }


    SiteDetail fetchByCampusId(Long domainId) {
        if (null == domainId) {
            return null
        }
        Campus campus = campusService.get(domainId)
        if (!campus) {
            return null
        }
        def buildings = buildingCompositeService.fetchByCampusCode(campus.code)
        return new SiteDetail(GlobalUniqueIdentifier.findByLdmNameAndDomainId(LDM_NAME, domainId).guid, campus, buildings, new Metadata(campus.dataOrigin))
    }


    SiteDetail fetchByCampusCode(String campusCode) {
        if (!campusCode) {
            return null
        }
        Campus campus = campusService.fetchByCode(campusCode)
        if (!campus) {
            return null
        }
        def buildings = buildingCompositeService.fetchByCampusCode(campus.code)
        return new SiteDetail(GlobalUniqueIdentifier.findByLdmNameAndDomainId(LDM_NAME, campus.id)?.guid, campus, buildings, new Metadata(campus.dataOrigin))
    }

}

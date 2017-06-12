/** *******************************************************************************
 Copyright 2014-2017 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.system.ldm
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.system.Campus
import net.hedtech.banner.general.system.ldm.v1.Metadata
import net.hedtech.banner.general.system.ldm.v1.SiteDetail
import net.hedtech.banner.general.system.ldm.v4.SiteDetailV4
import net.hedtech.banner.query.QueryBuilder
import net.hedtech.banner.query.operators.Operators
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.hibernate.Hibernate
import org.hibernate.criterion.Projections
import org.hibernate.type.Type
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
    private static final List<String> VERSIONS = [GeneralValidationCommonConstants.VERSION_V1, GeneralValidationCommonConstants.VERSION_V6]


    def campusService
    def buildingCompositeService
    def globalUniqueIdentifierService

    /**
     * GET /api/sites/<guid>
     *
     * @param guid
     * @return
     */
    @Transactional(readOnly = true)
    SiteDetail get(String guid) {
        GlobalUniqueIdentifier globalUniqueIdentifier = globalUniqueIdentifierService.fetchByLdmNameAndGuid(LDM_NAME, guid)
        if (!globalUniqueIdentifier) {
            throw new ApplicationException("site", new NotFoundException())
        }

        Campus campus = Campus.get(globalUniqueIdentifier.domainId)
        if(!campus?.getDescription() && LdmService.getAcceptVersion(VERSIONS).equalsIgnoreCase(GeneralValidationCommonConstants.VERSION_V6)){
            campus.setDescription(campus?.getCode())
        }
        if (!campus) {
            throw new ApplicationException("site", new NotFoundException())
        }
        def buildings = buildingCompositeService.fetchByCampusCode(campus.code)
        def siteDetail = LdmService.getAcceptVersion(VERSIONS).equalsIgnoreCase(GeneralValidationCommonConstants.VERSION_V6) ?
                new SiteDetailV4(globalUniqueIdentifier.fetchByLdmNameAndDomainId(LDM_NAME, campus.id).guid, campus, buildings, new Metadata(campus.dataOrigin)) :
                new SiteDetail(GlobalUniqueIdentifier.findByLdmNameAndDomainId(LDM_NAME, campus.id).guid, campus, buildings, new Metadata(campus.dataOrigin))
        return siteDetail
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
        List allowedSortFields = (GeneralValidationCommonConstants.VERSION_V6.equals(LdmService.getAcceptVersion(VERSIONS))? ['code', 'title']:['abbreviation', 'title'])
        RestfulApiValidationUtility.validateSortField(map.sort, allowedSortFields)
        map.sort = LdmService.fetchBannerDomainPropertyForLdmField(map.sort)

        if (map.containsKey("order")) {
            RestfulApiValidationUtility.validateSortOrder(map.order?.trim())
        } else {
            map.put('order', "asc")
        }

        map.order = map.order?map?.order:'ASC'
        def filters = QueryBuilder.createFilters(map)
        def allowedSearchFields = [CODE, DESCRIPTION]
        def allowedOperators = [Operators.EQUALS, Operators.EQUALS_IGNORE_CASE, Operators.CONTAINS, Operators.STARTS_WITH]
        RestfulApiValidationUtility.validateCriteria(filters, allowedSearchFields, allowedOperators)
        RestfulApiValidationUtility.validateSortField(map.sort,allowedSearchFields)
        def filterMap = QueryBuilder.getFilterData(map)
        def criteria = Campus.createCriteria()
        filterMap?.pagingAndSortParams?.offset = filterMap?.pagingAndSortParams?.offset?filterMap?.pagingAndSortParams?.offset:0
        def details = criteria.list(max: filterMap?.pagingAndSortParams?.max, offset: filterMap?.pagingAndSortParams?.offset) {
            projections{
                property('code')
                addProjectionToList(Projections.sqlProjection("nvl(STVCAMP_DESC,STVCAMP_CODE) as description", ['description'] as String[], [Hibernate.STRING] as Type[]), 'description')
            }
            if(map?.sort) {
                if (map?.sort?.equalsIgnoreCase('description') && (GeneralValidationCommonConstants.VERSION_V6.equalsIgnoreCase(LdmService.getAcceptVersion(VERSIONS)))) {
                    order('description', map?.order)
                    order('id', map?.order)
                } else {
                    order(map?.sort, map?.order)
                    order('id', map?.order)
                }
            } else {
                order('id', map?.order)
            }
        }

        details.each { detail ->
            Campus campus = Campus.findByCode(detail[0])
            buildings = buildingCompositeService.fetchByCampusCode(campus.code)
            def siteDetail = LdmService.getAcceptVersion(VERSIONS).equalsIgnoreCase(GeneralValidationCommonConstants.VERSION_V6) ?
                    new SiteDetailV4(globalUniqueIdentifierService.fetchByLdmNameAndDomainId(LDM_NAME, campus.id).guid, campus, buildings, new Metadata(campus.dataOrigin)) :
                    new SiteDetail(GlobalUniqueIdentifier.findByLdmNameAndDomainId(LDM_NAME, campus.id).guid, campus, buildings, new Metadata(campus.dataOrigin))
            sites << siteDetail
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
        return new SiteDetail(globalUniqueIdentifierService.fetchByLdmNameAndDomainId(LDM_NAME, domainId).guid, campus, buildings, new Metadata(campus.dataOrigin))
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
        return new SiteDetail(globalUniqueIdentifierService.fetchByLdmNameAndDomainId(LDM_NAME, campus.id)?.guid, campus, buildings, new Metadata(campus.dataOrigin))
    }

}

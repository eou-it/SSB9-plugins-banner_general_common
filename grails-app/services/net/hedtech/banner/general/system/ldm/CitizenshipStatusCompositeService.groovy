/** *******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.system.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.system.CitizenType
import net.hedtech.banner.general.system.ldm.v4.CitizenshipStatus
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * RESTful APIs for HeDM citizenship-statuses
 */
@Transactional
class CitizenshipStatusCompositeService extends LdmService {

    private static final List<String> VERSIONS = [GeneralValidationCommonConstants.VERSION_V6]

    def citizenTypeService

    /**
     * GET /api/citizenship-statuses
     *
     * @param params Request parameters
     * @return
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    List<CitizenshipStatus> list(Map params) {
        log.debug "list:Begin:$params"
        String acceptVersion = getAcceptVersion(VERSIONS)
        List<CitizenshipStatus> citizenshipStatuses = []
        setPagingParams(params)
        setSortingParams(params)

        String sortField = params.sort?.trim()
        String sortOrder = params.order?.trim()
        int max = params.max?.trim()?.toInteger() ?: 0
        int offset = params.offset?.trim()?.toInteger() ?: 0

        Map mapForSearch = [:]
        List<CitizenType> citizenTypes = citizenTypeService.fetchAllByCriteria(mapForSearch, sortField, sortOrder, max, offset) as List
        citizenTypes?.each { citizenType ->
            citizenshipStatuses << getDecorator(citizenType)
        }
        log.debug "list:End:${citizenshipStatuses?.size()}"
        return citizenshipStatuses
    }


    protected void setPagingParams(Map requestParams) {
        RestfulApiValidationUtility.correctMaxAndOffset(requestParams, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
    }


    protected void setSortingParams(Map requestParams) {
        if (requestParams.containsKey("sort")) {
            RestfulApiValidationUtility.validateSortField(requestParams.sort, [GeneralValidationCommonConstants.CODE, GeneralValidationCommonConstants.TITLE])
            requestParams.sort = fetchBannerDomainPropertyForLdmField(requestParams.sort)
        }

        if (requestParams.containsKey("order")) {
            RestfulApiValidationUtility.validateSortOrder(requestParams.order)
        } else {
            requestParams.put('order', "asc")
        }
    }

    /**
     * GET /api/citizenship-statuses
     *
     * The count method must return the total number of instances of the resource.
     * It is used in conjunction with the list method when returning a list of resources.
     * RestfulApiController will make call to "count" only if the "list" execution happens without any exception.
     *
     * @return
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Long count() {
        return citizenTypeService.count()
    }

    /**
     * GET /api/citizenship-statuses/<guid>
     *
     * @param guid
     * @return
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    CitizenshipStatus get(String guid) {
        log.debug "get:Begin:$guid"
        String acceptVersion = getAcceptVersion(VERSIONS)
        GlobalUniqueIdentifier globalUniqueIdentifier = globalUniqueIdentifierService.fetchByLdmNameAndGuid(GeneralValidationCommonConstants.CITIZENSHIP_STATUSES_LDM_NAME, guid)
        if (!globalUniqueIdentifier) {
            throw new ApplicationException(GeneralValidationCommonConstants.CITIZENSHIP_STATUS, new NotFoundException())
        }

        CitizenType citizenType = citizenTypeService.get(globalUniqueIdentifier.domainId)
        if (!citizenType) {
            throw new ApplicationException(GeneralValidationCommonConstants.CITIZENSHIP_STATUS, new NotFoundException())
        }
        log.debug("guid: ${globalUniqueIdentifier.guid}")
        return getDecorator(citizenType, globalUniqueIdentifier.guid)
    }


    private CitizenshipStatus getDecorator(CitizenType citizenType, String guid = null) {
        if (!guid) {
            guid = globalUniqueIdentifierService.fetchByLdmNameAndDomainId(GeneralValidationCommonConstants.CITIZENSHIP_STATUSES_LDM_NAME, citizenType.id)?.guid
        }

        return new CitizenshipStatus(citizenType, guid, getCitizenshipStatusCategory(citizenType.citizenIndicator ?: null))
    }


    protected String getCitizenshipStatusCategory(Boolean citizenIndicator) {
        CitizenshipStatusCategory citizenshipStatusCategory = CitizenshipStatusCategory.getByCitizenIndicator(citizenIndicator)
        return citizenshipStatusCategory.versionToEnumMap[GeneralValidationCommonConstants.VERSION_V6]
    }

}

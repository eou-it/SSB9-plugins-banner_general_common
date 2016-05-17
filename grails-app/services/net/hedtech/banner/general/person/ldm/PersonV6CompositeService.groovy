/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.system.ldm.v6.PersonV6
import net.hedtech.banner.query.DynamicFinder
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Transactional
class PersonV6CompositeService extends LdmService {

    static final int DEFAULT_PAGE_SIZE = 500
    static final int MAX_PAGE_SIZE = 500

    UserRoleCompositeService userRoleCompositeService

    def list(Map params){
        log.trace "list v6:Begin"
        List allowedSortFields = ["firstName", "lastName"]
        if (params.sort) {
            RestfulApiValidationUtility.validateSortField(params.sort, allowedSortFields)
        } else {
            params.put('sort', allowedSortFields[1])
        }
        if (params.order) {
            RestfulApiValidationUtility.validateSortOrder(params.order)
        } else {
            params.put('order', "asc")
        }
        RestfulApiValidationUtility.correctMaxAndOffset(params, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE)

        List<PersonIdentificationNameCurrent> personCurrentEntities

        if (params.role) {
            String role = params.role?.trim()?.toLowerCase()
            if (role == "instructor" || role == "student") {
                log.debug "fetchAllByRole $params"
                def returnVal = userRoleCompositeService.fetchAllByRole(params)
                log.debug "fetchAllByRole returned ${returnVal?.count} pidms containing ${returnVal?.pidms}"
                personCurrentEntities = fetchPersonCurrentByPIDMs(returnVal?.pidms, params.sort, params.order)
            } else {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("role.supported.v6", []))
            }
        } else {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("role.required", []))
        }

        def pidmToGuidMap = fetchPersonGuids(personCurrentEntities)

        return createDecorators(personCurrentEntities, pidmToGuidMap)
    }

    private List<PersonV6> createDecorators(List<PersonIdentificationNameCurrent> entities, def pidmToGuidMap) {
        List<PersonV6> decorators = []
        if(entities) {
            PersonV6 decorator
            entities?.each {
                decorator = new PersonV6()
                decorator.guid = pidmToGuidMap.get(it.pidm)
                decorators.add(decorator)
            }
        }
        return decorators
    }

    private List<PersonIdentificationNameCurrent> fetchPersonCurrentByPIDMs(List<Integer> pidms, String sortField, String sortOrder) {
        log.trace "fetchPersonCurrentByPIDMs : $pidms : $sortField: $sortOrder"
        List<PersonIdentificationNameCurrent> entities = null
        if (pidms) {
            def objectsOfPidms = []
            pidms.each {
                objectsOfPidms << [data: it]
            }
            Map paramsMap = [pidms: objectsOfPidms]
            String query = """from PersonIdentificationNameCurrent a
                                       where a.pidm in (:pidms)
                                       order by a.$sortField $sortOrder, a.bannerId $sortOrder
                                    """
            DynamicFinder dynamicFinder = new DynamicFinder(PersonIdentificationNameCurrent.class, query, "a")
            log.debug "$query"
            entities = dynamicFinder.find([params: paramsMap, criteria: []], [:])
            log.debug "Query returned ${entities?.size()} records"
        }
        return entities
    }

    /**
     * GET /api/persons
     *
     * @param params Request parameters
     * @return
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def count(Map params) {
        log.debug "count: Begin: Request parameters ${params}"
        def total = 0
        total = userRoleCompositeService.fetchAllByRole(params)?.count?.longValue()
        log.debug "count: End: $total"
        return total
    }

    private def fetchPersonGuids(List<PersonIdentificationNameCurrent> personCurrentEntities) {
    def pidmToGuidMap = [:]
        List<Long> surrogateIds = personCurrentEntities?.collect {
            it.id
        }
        List<GlobalUniqueIdentifier> globalUniqueIdentifiers = GlobalUniqueIdentifier.fetchByLdmNameAndDomainSurrogateIds(GeneralCommonConstants.PERSONS_LDM_NAME, surrogateIds)

        globalUniqueIdentifiers?.each {
            pidmToGuidMap.put(it.domainKey.toInteger(), it.guid)
        }

        return pidmToGuidMap
    }
}

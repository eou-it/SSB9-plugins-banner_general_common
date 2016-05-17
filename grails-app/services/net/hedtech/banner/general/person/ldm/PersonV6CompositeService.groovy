/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.person.PersonBasicPersonBase
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.system.ldm.v6.NameV6
import net.hedtech.banner.general.system.ldm.v6.PersonV6
import net.hedtech.banner.query.DynamicFinder
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Transactional
class PersonV6CompositeService extends LdmService {

    static final String LDM_NAME = 'persons'
    static final int DEFAULT_PAGE_SIZE = 500
    static final int MAX_PAGE_SIZE = 500

    UserRoleCompositeService userRoleCompositeService

    /**
     * GET /api/persons
     *
     * @param params Request parameters
     * @return
     */
    @Transactional(readOnly = true)
    def list(Map params) {
        log.trace "list v6:Begin"

        RestfulApiValidationUtility.correctMaxAndOffset(params, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE)

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

        List<Integer> pidms
        int totalCount = 0

        if (params.role) {
            String role = params.role?.trim()?.toLowerCase()
            if (role == "instructor" || role == "student") {
                log.debug "Searching for persons with role $role ...."
                def returnVal = userRoleCompositeService.fetchAllByRole(params)
                pidms = returnVal?.pidms
                totalCount = returnVal?.count
                log.debug "${totalCount} persons found with role $role."
            } else {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("role.supported.v6", []))
            }
        } else {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("role.required", []))
        }

        injectPropertyIntoParams(params, "count", totalCount)

        List<PersonIdentificationNameCurrent> personCurrentEntities = fetchPersonCurrentByPIDMs(pidms, params.sort, params.order)
        def pidmToGuidMap = fetchPersonGuids(personCurrentEntities)
        def pidmToPersonBaseMap = fetchPersonBaseByPIDMs(pidms)

        return createDecorators(personCurrentEntities, pidmToGuidMap, pidmToPersonBaseMap)
    }

    /**
     * GET /api/persons
     *
     * @param params Request parameters
     * @return
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def count(Map params) {
        log.trace "count v6: Begin: Request parameters ${params}"
        return getInjectedPropertyFromParams(params, "count")
    }

    /**
     * GET /api/persons/<guid>
     *
     * @param guid
     * @return
     */
    @Transactional(readOnly = true)
    def get(String guid) {

    }

    /**
     * POST /api/persons
     *
     * @param content Request body
     */
    def create(Map content) {

    }

    /**
     * PUT /api/persons/<guid>
     *
     * @param content Request body
     */
    def update(Map content) {

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


    private def fetchPersonBaseByPIDMs(List<Integer> pidms) {
        def pidmToPersonBaseMap = [:]
        List<PersonBasicPersonBase> entities = PersonBasicPersonBase.fetchByPidmList(pidms)
        entities?.each {
            pidmToPersonBaseMap.put(it.pidm, it)
        }
        return pidmToPersonBaseMap
    }


    private List<PersonV6> createDecorators(List<PersonIdentificationNameCurrent> entities,
                                            def pidmToGuidMap, def pidmToPersonBaseMap) {
        List<PersonV6> decorators = []
        if (entities) {
            PersonV6 decorator
            PersonBasicPersonBase personBase
            NameV6 nameV6
            entities?.each {
                decorator = new PersonV6()
                // GUID
                decorator.guid = pidmToGuidMap.get(it.pidm)
                // Privacy Status
                personBase = pidmToPersonBaseMap.get(it.pidm)
                if (personBase.confidIndicator == "Y") {
                    decorator.privacyStatus = ["privacyCategory": "restricted"]
                } else {
                    decorator.privacyStatus = ["privacyCategory": "unrestricted"]
                }
                // Names
                nameV6 = new NameV6()
                nameV6.type = ["category": "personal"]
                nameV6.firstName = it.firstName
                nameV6.lastName = it.lastName
                decorator.names << nameV6
                decorators.add(decorator)
            }
        }
        return decorators
    }


    private void injectPropertyIntoParams(Map params, String propName, def propVal) {
        def injectedProps = [:]
        if (params.containsKey("persons-injected") && params.get("persons-injected") instanceof Map) {
            injectedProps = params.get("persons-injected")
        } else {
            params.put("persons-injected", injectedProps)
        }
        injectedProps.putAt(propName, propVal)
    }


    private def getInjectedPropertyFromParams(Map params, String propName) {
        def propVal
        def injectedProps = params.get("persons-injected")
        if (injectedProps instanceof Map && injectedProps.containsKey(propName)) {
            propVal = injectedProps.get(propName)
        }
        return propVal
    }

}

/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.lettergeneration.ldm.PersonFilterCompositeService
import net.hedtech.banner.general.overall.VisaInformation
import net.hedtech.banner.general.overall.VisaInformationService
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.person.PersonBasicPersonBase
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.system.CitizenType
import net.hedtech.banner.general.system.ldm.CitizenshipStatusCompositeService
import net.hedtech.banner.general.system.ldm.ReligionCompositeService
import net.hedtech.banner.general.system.ldm.VisaTypeCompositeService
import net.hedtech.banner.general.system.ldm.v6.*
import net.hedtech.banner.general.utility.DateConvertHelperService
import net.hedtech.banner.query.DynamicFinder
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import java.sql.Timestamp

/**
 * RESTful APIs for Ellucian Ethos Data Model "persons" V6.
 */
@Transactional
class PersonV6CompositeService extends LdmService {

    static final int DEFAULT_PAGE_SIZE = 500
    static final int MAX_PAGE_SIZE = 500
    private static final String ROLE_FACULTY = "faculty"
    private static final String ROLE_STUDENT = "student"

    UserRoleCompositeService userRoleCompositeService
    PersonFilterCompositeService personFilterCompositeService
    CitizenshipStatusCompositeService citizenshipStatusCompositeService
    VisaInformationService visaInformationService
    VisaTypeCompositeService visaTypeCompositeService
    ReligionCompositeService religionCompositeService

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
            RestfulApiValidationUtility.validateSortField(params.sort.trim(), allowedSortFields)
        } else {
            params.put('sort', allowedSortFields[1])
        }
        if (params.order) {
            RestfulApiValidationUtility.validateSortOrder(params.order.trim())
        } else {
            params.put('order', "asc")
        }

        String sortField = params.sort.trim()
        String sortOrder = params.order.trim()
        int max = params.max.trim().toInteger()
        int offset = params.offset?.trim()?.toInteger() ?: 0

        List<Integer> pidms
        int totalCount = 0

        if (RestfulApiValidationUtility.isQApiRequest(params)) {

        } else {
            if (params.containsKey("personFilter") && params.containsKey("role")) {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("UnsupportedFilterCombination", []))
            }

            if (params.containsKey("personFilter")) {
                String guidOrDomainKey = params.get("personFilter")
                def returnMap = personFilterCompositeService.fetchPidmsOfPopulationExtract(guidOrDomainKey, sortField, sortOrder, max, offset)
                pidms = returnMap?.pidms
                totalCount = returnMap?.totalCount
                log.debug "${totalCount} persons in population extract ${guidOrDomainKey}."
            } else {
                if (params.role) {
                    String role = params.role?.trim()?.toLowerCase()
                    log.debug "Fetching persons with role $role ...."
                    def returnMap
                    if (role == "instructor") {
                        returnMap = userRoleCompositeService.fetchFaculties(sortField, sortOrder, max, offset)
                    } else if (role == "student") {
                        returnMap = userRoleCompositeService.fetchStudents(sortField, sortOrder, max, offset)
                    } else {
                        throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("role.supported.v6", []))
                    }
                    pidms = returnMap?.pidms
                    totalCount = returnMap?.totalCount
                    log.debug "${totalCount} persons found with role $role."
                } else {
                    throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("role.required", []))
                }
            }
        }

        injectPropertyIntoParams(params, "count", totalCount)

        List<PersonIdentificationNameCurrent> personCurrentEntities = fetchPersonCurrentByPIDMs(pidms, sortField, sortOrder)

        return createDecorators(personCurrentEntities)
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
        PersonIdentificationNameCurrent personIdentificationNameCurrent = getPersonIdentificationNameCurrentByGUID(guid)
        return createDecorators([personIdentificationNameCurrent])[0]
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


    PersonIdentificationNameCurrent getPersonIdentificationNameCurrentByGUID(String guid) {
        GlobalUniqueIdentifier entity = GlobalUniqueIdentifier.fetchByLdmNameAndGuid(GeneralCommonConstants.PERSONS_LDM_NAME, guid)
        if (!entity) {
            throw new ApplicationException("Person", new NotFoundException())
        }
        PersonIdentificationNameCurrent personIdentificationNameCurrent =
                PersonIdentificationNameCurrent.fetchByPidm(entity.domainKey?.toInteger())
        return personIdentificationNameCurrent
    }


    private List<PersonIdentificationNameCurrent> fetchPersonCurrentByPIDMs(List<Integer> pidms, String sortField, String sortOrder) {
        log.trace "fetchPersonCurrentByPIDMs : $pidms : $sortField: $sortOrder"
        List<PersonIdentificationNameCurrent> entities
        if (pidms) {
            def objectsOfPidms = []
            pidms.each {
                objectsOfPidms << [data: it]
            }
            Map paramsMap = [pidms: objectsOfPidms]
            String query = """ from PersonIdentificationNameCurrent a
                               where a.pidm in (:pidms)
                               and a.entityIndicator = 'P'
                               order by a.$sortField $sortOrder, a.bannerId $sortOrder """
            DynamicFinder dynamicFinder = new DynamicFinder(PersonIdentificationNameCurrent.class, query, "a")
            log.debug "$query"
            entities = dynamicFinder.find([params: paramsMap, criteria: []], [:])
            log.debug "Query returned ${entities?.size()} records"
        }
        return entities
    }


    private def createDecorators(List<PersonIdentificationNameCurrent> entities) {
        def decorators = []
        if (entities) {
            List<Long> personSurrogateIds = entities?.collect {
                it.id
            }
            List<Integer> pidms = entities?.collect {
                it.pidm
            }

            def dataMap = [:]
            fetchPersonsGuidDataAndPutInMap(personSurrogateIds, dataMap)
            fetchPersonsBiographicalDataAndPutInMap(pidms, dataMap)
            fetchPersonsVisaDataAndPutInMap(pidms, dataMap)
            fetchPersonsRoleDataAndPutInMap(pidms, dataMap)

            entities?.each {
                def dataMapForPerson = [:]
                dataMapForPerson << ["personGuid": dataMap.pidmToGuidMap.get(it.pidm)]
                PersonBasicPersonBase personBase = dataMap.pidmToPersonBaseMap.get(it.pidm)
                if (personBase) {
                    dataMapForPerson << ["personBase": personBase]
                    if (personBase.citizenType) {
                        dataMapForPerson << ["citizenTypeGuid": dataMap.ctCodeToGuidMap.get(personBase.citizenType.code)]
                    }
                    if (personBase.religion) {
                        dataMapForPerson << ["religionGuid": dataMap.relCodeToGuidMap.get(personBase.religion.code)]
                    }
                }
                VisaInformation visaInfo = dataMap.pidmToVisaInfoMap.get(it.pidm)
                if (visaInfo) {
                    dataMapForPerson << ["visaInformation": visaInfo]
                    dataMapForPerson << ["visaTypeGuid": dataMap.vtCodeToGuidMap.get(visaInfo.visaType.code)]
                }
                def personRoles = []
                if (dataMap.pidmToFacultyRoleMap.containsKey(it.pidm)) {
                    personRoles << dataMap.pidmToFacultyRoleMap.get(it.pidm)
                }
                if (dataMap.pidmToStudentRoleMap.containsKey(it.pidm)) {
                    personRoles << dataMap.pidmToStudentRoleMap.get(it.pidm)
                }
                dataMapForPerson << ["personRoles": personRoles]
                decorators.add(createPersonV6(it, dataMapForPerson))
            }
        }
        return decorators
    }


    private PersonV6 createPersonV6(PersonIdentificationNameCurrent personCurrent, def dataMapForPerson) {
        PersonV6 decorator
        if (personCurrent) {
            decorator = new PersonV6()
            // GUID
            decorator.guid = dataMapForPerson["personGuid"]
            PersonBasicPersonBase personBase = dataMapForPerson["personBase"]
            if (personBase) {
                // privacyStatus
                if (personBase.confidIndicator == "Y") {
                    decorator.privacyStatus = ["privacyCategory": "restricted"]
                } else {
                    decorator.privacyStatus = ["privacyCategory": "unrestricted"]
                }
                // citizenshipStatus
                if (personBase.citizenType) {
                    decorator.citizenshipStatus = createCitizenshipStatusV6(personBase.citizenType, dataMapForPerson["citizenTypeGuid"])
                }
                // religion
                if (personBase.religion) {
                    decorator.religion = ["id": dataMapForPerson["religionGuid"]]
                }
            }
            // Names
            decorator.names = []
            NameV6 nameV6 = new NameV6()
            nameV6.type = ["category": "personal"]
            nameV6.firstName = personCurrent.firstName
            nameV6.lastName = personCurrent.lastName
            decorator.names << nameV6
            // visaStatus
            VisaInformation visaInfo = dataMapForPerson["visaInformation"]
            if (visaInfo) {
                decorator.visaStatus = createVisaStatusV6(visaInfo, dataMapForPerson["visaTypeGuid"])
            }
            // Roles
            def personRoles = dataMapForPerson["personRoles"]
            if (personRoles) {
                decorator.roles = []
                personRoles.each {
                    decorator.roles << createRoleV6(it.role, it.startDate, it.endDate)
                }
            }
        }
        return decorator
    }


    private void fetchPersonsGuidDataAndPutInMap(List<Long> personSurrogateIds, Map dataMap) {
        // Get GUIDs for persons
        def pidmToGuidMap = fetchPersonGuids(personSurrogateIds)
        // Put in Map
        dataMap.put("pidmToGuidMap", pidmToGuidMap)
    }


    private void fetchPersonsBiographicalDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        // Get SPBPERS records for persons
        def pidmToPersonBaseMap = fetchPersonBaseByPIDMs(pidms)
        // Get GUIDs for CitizenTypes
        List<String> citizenTypeCodes = pidmToPersonBaseMap?.values()?.findResults {
            it.citizenType?.code
        }.unique()
        Map<String, String> ctCodeToGuidMap = [:]
        if (citizenTypeCodes) {
            log.debug "Getting GUIDs for CitizenType codes $citizenTypeCodes..."
            ctCodeToGuidMap = citizenshipStatusCompositeService.fetchGUIDs(citizenTypeCodes)
            log.debug "Got ${ctCodeToGuidMap?.size() ?: 0} GUIDs for given CitizenType codes"
        }
        // Get GUIDs for regligion codes
        List<String> religionCodes = pidmToPersonBaseMap?.values()?.findResults {
            it.religion?.code
        }.unique()
        Map<String, String> relCodeToGuidMap = [:]
        if (religionCodes) {
            log.debug "Getting GUIDs for religion codes $religionCodes..."
            relCodeToGuidMap = religionCompositeService.fetchGUIDs(religionCodes)
            log.debug "Got ${relCodeToGuidMap?.size() ?: 0} GUIDs for given religion codes"
        }
        // Put in Map
        dataMap.put("pidmToPersonBaseMap", pidmToPersonBaseMap)
        dataMap.put("ctCodeToGuidMap", ctCodeToGuidMap)
        dataMap.put("relCodeToGuidMap", relCodeToGuidMap)
    }


    private void fetchPersonsVisaDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        // Get GORVISA records for persons
        def pidmToVisaInfoMap = fetchVisaInformationByPIDMs(pidms)
        // Get GUIDs for visa types
        List<String> visaTypeCodes = pidmToVisaInfoMap?.values()?.findResults {
            it.visaType?.code
        }.unique()
        Map<String, String> vtCodeToGuidMap = [:]
        if (visaTypeCodes) {
            log.debug "Getting GUIDs for VisaType codes $visaTypeCodes..."
            vtCodeToGuidMap = visaTypeCompositeService.fetchGUIDs(visaTypeCodes)
            log.debug "Got ${vtCodeToGuidMap?.size() ?: 0} GUIDs for given VisaType codes"
        }
        // Put in Map
        dataMap.put("pidmToVisaInfoMap", pidmToVisaInfoMap)
        dataMap.put("vtCodeToGuidMap", vtCodeToGuidMap)
    }


    private void fetchPersonsRoleDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        // Faculty role
        def pidmToFacultyRoleMap = [:]
        List<Object[]> facList = userRoleCompositeService.fetchFacultiesByPIDMs(pidms)
        facList?.each {
            BigDecimal bdPidm = it[0]
            Timestamp startDate = it[1]
            Timestamp endDate = it[2]
            pidmToFacultyRoleMap.put(bdPidm.toInteger(), [role: ROLE_FACULTY, startDate: startDate, endDate: endDate])
        }
        // Student role
        def pidmToStudentRoleMap = [:]
        List<BigDecimal> studList = userRoleCompositeService.fetchStudentsByPIDMs(pidms)
        studList?.each {
            pidmToStudentRoleMap.put(it.toInteger(), [role: ROLE_STUDENT])
        }
        // Put in Map
        dataMap.put("pidmToFacultyRoleMap", pidmToFacultyRoleMap)
        dataMap.put("pidmToStudentRoleMap", pidmToStudentRoleMap)
    }


    private def fetchPersonGuids(List<Long> personSurrogateIds) {
        def pidmToGuidMap = [:]
        if (personSurrogateIds) {
            log.debug "Getting GORGUID records for ${personSurrogateIds?.size()} Surrogate Ids..."
            List<GlobalUniqueIdentifier> entities = GlobalUniqueIdentifier.fetchByLdmNameAndDomainSurrogateIds(GeneralCommonConstants.PERSONS_LDM_NAME, personSurrogateIds)
            log.debug "Got ${entities?.size()} GORGUID records"
            entities?.each {
                pidmToGuidMap.put(it.domainKey.toInteger(), it.guid)
            }
        }
        return pidmToGuidMap
    }


    private def fetchPersonBaseByPIDMs(List<Integer> pidms) {
        def pidmToPersonBaseMap = [:]
        if (pidms) {
            log.debug "Getting SPBPERS records for ${pidms?.size()} PIDMs..."
            List<PersonBasicPersonBase> entities = PersonBasicPersonBase.fetchByPidmList(pidms)
            log.debug "Got ${entities?.size()} SPBPERS records"
            entities?.each {
                pidmToPersonBaseMap.put(it.pidm, it)
            }
        }
        return pidmToPersonBaseMap
    }


    private def fetchVisaInformationByPIDMs(List<Integer> pidms) {
        def pidmToVisaInfoMap = [:]
        if (pidms) {
            log.debug "Getting GORVISA records for ${pidms?.size()} PIDMs..."
            List<VisaInformation> entities = visaInformationService.fetchAllWithMaxSeqNumByPidmInList(pidms)
            log.debug "Got ${entities?.size()} GORVISA records"
            entities?.each {
                pidmToVisaInfoMap.put(it.pidm, it)
            }
        }
        return pidmToVisaInfoMap
    }


    private CitizenshipStatusV6 createCitizenshipStatusV6(CitizenType citizenType, String citizenTypeGuid) {
        CitizenshipStatusV6 decorator
        if (citizenType) {
            decorator = new CitizenshipStatusV6()
            decorator.category = citizenshipStatusCompositeService.getCitizenshipStatusCategory(citizenType.citizenIndicator)
            if (citizenTypeGuid) {
                decorator.detail = ["id": citizenTypeGuid]
            }
        }
        return decorator
    }


    private VisaStatusV6 createVisaStatusV6(VisaInformation visaInfo, String visaTypeGuid) {
        VisaStatusV6 decorator
        if (visaInfo) {
            decorator = new VisaStatusV6()
            decorator.category = visaTypeCompositeService.getVisaTypeCategory(visaInfo.visaType?.nonResIndicator)
            if (visaTypeGuid) {
                decorator.detail = ["id": visaTypeGuid]
            }
            decorator.status = getVisaStatus(visaInfo)
            if (visaInfo.visaIssueDate) {
                decorator.startOn = DateConvertHelperService.convertDateIntoUTCFormat(visaInfo.visaIssueDate)
            }
            if (visaInfo.visaExpireDate) {
                decorator.endOn = DateConvertHelperService.convertDateIntoUTCFormat(visaInfo.visaExpireDate)
            }
        }
        return decorator
    }

    /**
     * There is no status field on visa information in Banner.
     * If the current date is greater than GORVISA_VISA_EXPIRE_DATE then the status should be set to 'expired'
     * otherwise, the status should be set to 'current'
     *
     * @param visaInfo VisaInformation
     * @return
     */
    private String getVisaStatus(VisaInformation visaInfo) {
        String status = "current"
        if (visaInfo && visaInfo.visaExpireDate) {
            String currentDate = new Date().format("yyyyMMdd")
            String visaExpireDate = visaInfo.visaExpireDate.format("yyyyMMdd")
            if (currentDate > visaExpireDate) {
                status = "expired"
            }
        }
        return status
    }


    private RoleV6 createRoleV6(String role, Timestamp startDate, Timestamp endDate) {
        RoleV6 decorator
        if (role) {
            decorator = new RoleV6()
            if (role == ROLE_FACULTY) {
                decorator.role = RoleNameV6.INSTRUCTOR.value
            } else if (role == ROLE_STUDENT) {
                decorator.role = RoleNameV6.STUDENT.value
            }
            if (startDate) {
                decorator.startOn = DateConvertHelperService.convertDateIntoUTCFormat(startDate)
            }
            if (endDate) {
                decorator.endOn = DateConvertHelperService.convertDateIntoUTCFormat(endDate)
            }
        }
        return decorator
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

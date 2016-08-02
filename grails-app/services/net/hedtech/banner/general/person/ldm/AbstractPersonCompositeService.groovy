/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.commonmatching.CommonMatchingCompositeService
import net.hedtech.banner.general.lettergeneration.ldm.PersonFilterCompositeService
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.person.*
import net.hedtech.banner.general.system.ldm.EmailTypeCompositeService
import net.hedtech.banner.general.system.ldm.EthnicityCompositeService
import net.hedtech.banner.general.system.ldm.PersonNameTypeCompositeService
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import java.sql.Timestamp

abstract class AbstractPersonCompositeService extends LdmService {

    private static final int MAX_DEFAULT = 500
    private static final int MAX_UPPER_LIMIT = 500

    private static final ThreadLocal<Map> threadLocal =
            new ThreadLocal<Map>() {
                @Override
                protected Map initialValue() {
                    return [:]
                }
            }

    UserRoleCompositeService userRoleCompositeService
    PersonFilterCompositeService personFilterCompositeService
    CommonMatchingCompositeService commonMatchingCompositeService
    PersonIdentificationNameCurrentService personIdentificationNameCurrentService
    PersonAdvancedSearchViewService personAdvancedSearchViewService
    EmailTypeCompositeService emailTypeCompositeService
    EthnicityCompositeService ethnicityCompositeService
    PersonNameTypeCompositeService personNameTypeCompositeService
    PersonIdentificationNameAlternateService personIdentificationNameAlternateService


    abstract protected String getPopSelGuidOrDomainKey(final Map requestParams)


    abstract protected def prepareCommonMatchingRequest(final Map content)


    abstract protected Map processListApiRequest(final Map requestParams)


    abstract protected List<RoleName> getRolesRequired()


    abstract protected void fetchDataAndPutInMap_VersonSpecific(List<Integer> pidms, Map dataMap)


    abstract protected void fetchPersonsBiographicalDataAndPutInMap_VersionSpecific(List<Integer> pidms, Map dataMap)


    abstract protected def getBannerNameTypeToHedmNameTypeMap()


    abstract protected void fetchPersonsAlternateNameDataAndPutInMap_VersionSpecific(List<Integer> pidms, Map dataMap)


    abstract
    protected void prepareDataMapForSinglePerson_VersionSpecific(PersonIdentificationNameCurrent personIdentificationNameCurrent,
                                                                 final Map dataMap, Map dataMapForPerson)


    abstract protected def createPersonDataModel(PersonIdentificationNameCurrent personIdentificationNameCurrent,
                                                 final Map dataMapForPerson)

    /**
     * POST /qapi/persons
     *
     * Query-with-POST
     * URL mapping with prefix qapi can be used to allow the use of a POST for querying a resource.
     *
     * @param requestParams
     * @return
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    protected def listQApi(Map requestParams) {
        setPagingParams(requestParams)

        setSortingParams(requestParams)

        Map requestProcessingResult = processQueryWithPostRequest(requestParams)

        injectTotalCountIntoParams(requestParams, requestProcessingResult)

        return requestProcessingResult
    }

    /**
     * GET /api/persons
     *
     * @param requestParams
     * @return
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    protected def listApi(Map requestParams) {
        setPagingParams(requestParams)

        setSortingParams(requestParams)

        Map requestProcessingResult = processListApiRequest(requestParams)

        injectTotalCountIntoParams(requestParams, requestProcessingResult)

        return createPersonDataModels(requestParams, requestProcessingResult)
    }

    /**
     * GET /api/persons
     * or
     * POST /qapi/persons
     *
     * The count method must return the total number of instances of the resource.
     * It is used in conjunction with the list method when returning a list of resources.
     * RestfulApiController will make call to "count" only if the "list" execution happens without any exception.
     *
     * @param requestParams
     * @return
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    protected def count(Map requestParams) {
        return getInjectedPropertyFromParams(requestParams, "totalCount")
    }

    /**
     * GET /api/persons/<guid>
     *
     * @param guid
     * @return
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    protected def get(String guid) {
        def row = personIdentificationNameCurrentService.fetchByGuid(guid)
        if (!row) {
            throw new ApplicationException("Person", new NotFoundException())
        }
        return createPersonDataModels([row.personIdentificationNameCurrent], getPidmToGuidMap([row]))[0]
    }


    protected def createPersonDataModels(final Map requestParams, final Map requestProcessingResult) {
        List<PersonIdentificationNameCurrent> personCurrentEntities = []
        def pidmToGuidMap = [:]
        if (requestProcessingResult.containsKey("pidms")) {
            List<Integer> pidms = requestProcessingResult.get("pidms")
            def rows = personIdentificationNameCurrentService.fetchAllWithGuidByPidmInList(pidms, requestParams.sort?.trim(), requestParams.order?.trim())
            personCurrentEntities = rows?.collect { it.personIdentificationNameCurrent }
            pidmToGuidMap = getPidmToGuidMap(rows)
        }
        return createPersonDataModels(personCurrentEntities, pidmToGuidMap)
    }


    protected def createPersonDataModels(List<PersonIdentificationNameCurrent> entities, def pidmToGuidMap) {
        def decorators = []
        if (entities) {
            List<Integer> pidms = entities?.collect {
                it.pidm
            }

            def dataMap = [:]
            dataMap.put("pidmToGuidMap", pidmToGuidMap)
            fetchDataAndPutInMap(pidms, dataMap)

            entities?.each {
                def dataMapForPerson = prepareDataMapForSinglePerson(it, dataMap)
                decorators << createPersonDataModel(it, dataMapForPerson)
            }
        }
        return decorators
    }


    private Map processQueryWithPostRequest(final Map requestParams) {
        Map requestProcessingResult
        String contentType = getRequestRepresentation()
        log.debug "Content-Type: ${contentType}"
        if (contentType?.contains('person-filter')) {
            log.debug "PopSel"
            requestProcessingResult = getPidmsOfPopulationExtract(requestParams)
        } else if (contentType?.contains("duplicate-check")) {
            log.debug("Duplicate Check")
            requestProcessingResult = searchForMatchingPersons(requestParams)
        }
        return requestProcessingResult
    }


    protected getPidmsOfPopulationExtract(final Map requestParams) {
        String guidOrDomainKey = getPopSelGuidOrDomainKey(requestParams)
        return personFilterCompositeService.fetchPidmsOfPopulationExtract(guidOrDomainKey, requestParams.sort?.trim(), requestParams.order?.trim(), requestParams.max?.trim()?.toInteger() ?: 0, requestParams.offset?.trim()?.toInteger() ?: 0)
    }


    private def searchForMatchingPersons(final Map content) {
        def cmRequest = prepareCommonMatchingRequest(content)

        if (cmRequest?.dateOfBirth) {
            Date dob = cmRequest.remove("dateOfBirth")
            cmRequest << [birthDay: dob?.format("dd")]
            cmRequest << [birthMonth: dob?.format("MM")]
            cmRequest << [birthYear: dob?.format("yyyy")]
        }

        cmRequest << [max: content?.max]
        cmRequest << [offset: content?.offset]
        cmRequest << [sort: content?.sort]
        cmRequest << [order: content?.order]

        def personEmails = cmRequest.remove("personEmails")

        // Common Matching Source Code
        IntegrationConfiguration intConf = IntegrationConfiguration.fetchByProcessCodeAndSettingName("HEDM", "PERSON.MATCHRULE")
        cmRequest << [source: intConf?.value]

        // Call Common Matching Service
        commonMatchingCompositeService.commonMatchingCleanup()

        def cmResponse
        if (personEmails) {
            // Try with each email until you get match
            for (def temp : personEmails) {
                cmRequest << [emailType: temp.emailType]
                cmRequest << [email: temp.email]
                log.debug "Common matching request : ${cmRequest}"
                cmResponse = commonMatchingCompositeService.commonMatching(cmRequest)
                if (cmResponse?.personList) break
            }
        } else {
            // Try without email
            log.debug "Common matching request : ${cmRequest}"
            cmResponse = commonMatchingCompositeService.commonMatching(cmRequest)
        }
        log.debug "Common matching returned ${cmResponse?.personList?.size()} records"
        List<Integer> pidms = cmResponse?.personList?.collect { it.pidm }
        def totalCount = cmResponse?.count

        return [pidms: pidms, totalCount: totalCount]
    }


    private void fetchDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        fetchPersonsBiographicalDataAndPutInMap(pidms, dataMap)
        fetchPersonsAlternateNameDataAndPutInMap(pidms, dataMap)

        fetchDataAndPutInMap_VersonSpecific(pidms, dataMap)
    }


    private def prepareDataMapForSinglePerson(PersonIdentificationNameCurrent personIdentificationNameCurrent,
                                              final Map dataMap) {
        Map dataMapForPerson = [:]

        dataMapForPerson << ["personGuid": dataMap.pidmToGuidMap.get(personIdentificationNameCurrent.pidm)]

        // Biographical
        PersonBasicPersonBase personBase = dataMap.pidmToPersonBaseMap.get(personIdentificationNameCurrent.pidm)
        if (personBase) {
            dataMapForPerson << ["personBase": personBase]

            if (personBase.ethnic) {
                dataMapForPerson << ["usEthnicCodeGuid": dataMap.usEthnicCodeToGuidMap.get(personBase.ethnic)]
            }
        }

        // names
        List<PersonIdentificationNameAlternate> personAlternateNames = dataMap.pidmToAlternateNamesMap.get(personIdentificationNameCurrent.pidm)
        if (personAlternateNames) {
            dataMapForPerson << ["bannerNameTypeToHedmNameTypeMap": dataMap.bannerNameTypeToHedmNameTypeMap]
            dataMapForPerson << ["personAlternateNames": personAlternateNames]
        }

        prepareDataMapForSinglePerson_VersionSpecific(personIdentificationNameCurrent, dataMap, dataMapForPerson)

        return dataMapForPerson
    }


    private void fetchPersonsBiographicalDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        // Get SPBPERS records for persons
        def pidmToPersonBaseMap = fetchPersonBaseByPIDMs(pidms)

        // Get GUIDs for US ethnic codes (SPBPERS_ETHN_CDE)
        Map<String, String> usEthnicCodeToGuidMap = ethnicityCompositeService.fetchGUIDsForUnitedStatesEthnicCodes()

        // Put in Map
        dataMap.put("pidmToPersonBaseMap", pidmToPersonBaseMap)
        dataMap.put("usEthnicCodeToGuidMap", usEthnicCodeToGuidMap)

        fetchPersonsBiographicalDataAndPutInMap_VersionSpecific(pidms, dataMap)
    }


    private void fetchPersonsAlternateNameDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        def bannerNameTypeToHedmNameTypeMap = getBannerNameTypeToHedmNameTypeMap()
        log.debug "Banner NameType to HEDM NameType mapping = ${bannerNameTypeToHedmNameTypeMap}"

        List<PersonIdentificationNameAlternate> entities = personIdentificationNameAlternateService.fetchAllMostRecentlyCreated(pidms, bannerNameTypeToHedmNameTypeMap.keySet().toList())
        log.debug "Got ${entities?.size() ?: 0} SV_SPRIDEN_ALT records"
        Map pidmToAlternateNamesMap = [:]
        entities.each {
            List<PersonIdentificationNameAlternate> personAlternateNames = []
            if (pidmToAlternateNamesMap.containsKey(it.pidm)) {
                personAlternateNames = pidmToAlternateNamesMap.get(it.pidm)
            } else {
                pidmToAlternateNamesMap.put(it.pidm, personAlternateNames)
            }
            personAlternateNames.add(it)
        }

        // Put in Map
        dataMap.put("bannerNameTypeToHedmNameTypeMap", bannerNameTypeToHedmNameTypeMap)
        dataMap.put("pidmToAlternateNamesMap", pidmToAlternateNamesMap)

        fetchPersonsAlternateNameDataAndPutInMap_VersionSpecific(pidms, dataMap)
    }


    protected void fetchPersonsRoleDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        def pidmToRolesMap = [:]

        List<RoleName> roleNames = getRolesRequired()
        if (roleNames) {
            def pidmToStudentRoleMap = [:]
            def pidmToFacultyRoleMap = [:]
            def pidmToAdvisorRoleMap = [:]
            def pidmToAlumniRoleMap = [:]
            def pidmToEmployeeRoleMap = [:]
            def pidmToProspectiveStudentRoleMap = [:]
            def pidmToVendorRoleMap = [:]

            for (RoleName roleName : roleNames) {
                switch (roleName) {
                    case RoleName.STUDENT:
                        if (isThreadLocalContainsStudentPidms()) {
                            // Saving query time
                            pidmToStudentRoleMap = getPidmToStudentRoleMapUsingThreadLocal()
                        } else {
                            pidmToStudentRoleMap = getPidmToStudentRoleMap(pidms)
                        }
                        break
                    case RoleName.INSTRUCTOR:
                        pidmToFacultyRoleMap = getPidmToFacultyRoleMap(pidms)
                        break
                    case RoleName.ADVISOR:
                        pidmToAdvisorRoleMap = getPidmToAdvisorRoleMap(pidms)
                        break
                    case RoleName.ALUMNI:
                        pidmToAlumniRoleMap = getPidmToAlumniRoleMap(pidms)
                        break
                    case RoleName.EMPLOYEE:
                        pidmToEmployeeRoleMap = getPidmToEmployeeRoleMap(pidms)
                        break
                    case RoleName.PROSPECTIVE_STUDENT:
                        pidmToProspectiveStudentRoleMap = getPidmToProspectiveStudentRoleMap(pidms)
                        break
                    case RoleName.VENDOR:
                        pidmToVendorRoleMap = getPidmToVendorRoleMap(pidms)
                        break
                    default:
                        break
                }
            }

            pidms.each {
                def personRoles = []
                pidmToRolesMap.put(it, personRoles)
                if (pidmToStudentRoleMap.containsKey(it)) {
                    personRoles << pidmToStudentRoleMap.get(it)
                }
                if (pidmToFacultyRoleMap.containsKey(it)) {
                    personRoles << pidmToFacultyRoleMap.get(it)
                }
                if (pidmToAdvisorRoleMap.containsKey(it)) {
                    personRoles << pidmToAdvisorRoleMap.get(it)
                }
                if (pidmToAlumniRoleMap.containsKey(it)) {
                    personRoles << pidmToAlumniRoleMap.get(it)
                }
                if (pidmToEmployeeRoleMap.containsKey(it)) {
                    personRoles << pidmToEmployeeRoleMap.get(it)
                }
                if (pidmToProspectiveStudentRoleMap.containsKey(it)) {
                    personRoles << pidmToProspectiveStudentRoleMap.get(it)
                }
                if (pidmToVendorRoleMap.containsKey(it)) {
                    personRoles << pidmToVendorRoleMap.get(it)
                }
            }
        }

        // Put in Map
        dataMap.put("pidmToRolesMap", pidmToRolesMap)
    }


    protected def getPidmToStudentRoleMap(List<Integer> pidms) {
        def pidmToStudentRoleMap = [:]
        List<BigDecimal> rows = userRoleCompositeService.fetchStudentsByPIDMs(pidms)
        rows?.each {
            pidmToStudentRoleMap.put(it.toInteger(), [role: RoleName.STUDENT])
        }
        return pidmToStudentRoleMap
    }


    private def getPidmToStudentRoleMapUsingThreadLocal() {
        List<Integer> pidms = getStudentPidmsFromThreadLocal()
        def pidmToStudentRoleMap = [:]
        pidms?.each {
            pidmToStudentRoleMap.put(it, [role: RoleName.STUDENT])
        }
        return pidmToStudentRoleMap
    }


    protected def getPidmToFacultyRoleMap(List<Integer> pidms) {
        def pidmToFacultyRoleMap = [:]
        List<Object[]> rows = userRoleCompositeService.fetchFacultiesByPIDMs(pidms)
        rows?.each {
            BigDecimal bdPidm = it[0]
            Timestamp startDate = it[1]
            Timestamp endDate = it[2]
            pidmToFacultyRoleMap.put(bdPidm.toInteger(), [role: RoleName.INSTRUCTOR, startDate: startDate, endDate: endDate])
        }
        return pidmToFacultyRoleMap
    }


    protected def getPidmToEmployeeRoleMap(List<Integer> pidms) {
        def pidmToEmployeeRoleMap = [:]
        List<Object[]> rows = userRoleCompositeService.fetchEmployeesByPIDMs(pidms)
        rows?.each {
            BigDecimal bdPidm = it[0]
            Timestamp startDate = it[1]
            Timestamp endDate = it[2]
            pidmToEmployeeRoleMap.put(bdPidm.toInteger(), [role: RoleName.EMPLOYEE, startDate: startDate, endDate: endDate])
        }
        return pidmToEmployeeRoleMap
    }


    protected def getPidmToAlumniRoleMap(List<Integer> pidms) {
        def pidmToAlumniRoleMap = [:]
        List<Object[]> alumniList = userRoleCompositeService.fetchAlumnisByPIDMs(pidms)
        alumniList?.each {
            BigDecimal bdPidm = it[0]
            Timestamp startDate = it[1]
            pidmToAlumniRoleMap.put(bdPidm.toInteger(), [role: RoleName.ALUMNI, startDate: startDate])
        }
        return pidmToAlumniRoleMap
    }


    protected def getPidmToVendorRoleMap(List<Integer> pidms) {
        def pidmToVendorRoleMap = [:]
        List<Object[]> rows = userRoleCompositeService.fetchVendorsByPIDMs(pidms)
        rows?.each {
            BigDecimal bdPidm = it[0]
            Timestamp startDate = it[1]
            Timestamp endDate = it[2]
            pidmToVendorRoleMap.put(bdPidm.toInteger(), [role: RoleName.VENDOR, startDate: startDate, endDate: endDate])
        }
        return pidmToVendorRoleMap
    }


    protected def getPidmToProspectiveStudentRoleMap(List<Integer> pidms) {
        def pidmToProspectiveStudentRoleMap = [:]
        List<Object[]> rows = userRoleCompositeService.fetchProspectiveStudentByPIDMs(pidms)
        rows?.each {
            BigDecimal bdPidm = it[0]
            Timestamp startDate = it[1]
            Timestamp endDate = it[2]
            pidmToProspectiveStudentRoleMap.put(bdPidm.toInteger(), [role: RoleName.PROSPECTIVE_STUDENT, startDate: startDate, endDate: endDate])
        }
        return pidmToProspectiveStudentRoleMap
    }


    protected def getPidmToAdvisorRoleMap(List<Integer> pidms) {
        def pidmToAdvisorRoleMap = [:]
        List<Object[]> rows = userRoleCompositeService.fetchAdvisorByPIDMs(pidms)
        rows?.each {
            BigDecimal bdPidm = it[0]
            Timestamp startDate = it[1]
            Timestamp endDate = it[2]
            pidmToAdvisorRoleMap.put(bdPidm.toInteger(), [role: RoleName.ADVISOR, startDate: startDate, endDate: endDate])
        }
        return pidmToAdvisorRoleMap
    }


    private def getPidmToGuidMap(def rows) {
        Map<Integer, String> pidmToGuidMap = [:]
        rows?.each {
            pidmToGuidMap.put(it.personIdentificationNameCurrent.pidm, it.globalUniqueIdentifier.guid)
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


    protected void setPagingParams(Map requestParams) {
        RestfulApiValidationUtility.correctMaxAndOffset(requestParams, MAX_DEFAULT, MAX_UPPER_LIMIT)

        if (!requestParams.containsKey("offset")) {
            requestParams.put("offset", "0")
        }
    }


    protected void setSortingParams(Map requestParams) {
        def allowedSortFields = ["firstName", "lastName"]

        if (requestParams.containsKey("sort")) {
            RestfulApiValidationUtility.validateSortField(requestParams.sort, allowedSortFields)
        } else {
            requestParams.put('sort', allowedSortFields[1])
        }

        if (requestParams.containsKey("order")) {
            RestfulApiValidationUtility.validateSortOrder(requestParams.order)
        } else {
            requestParams.put('order', "asc")
        }
    }


    private void injectTotalCountIntoParams(Map requestParams, Map requestProcessingResult) {
        if (requestProcessingResult.containsKey("totalCount")) {
            log.debug("X-Total-Count = ${requestProcessingResult.totalCount}")
            injectPropertyIntoParams(requestParams, "totalCount", requestProcessingResult.totalCount)
        }
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


    protected void setStudentPidmsInThreadLocal(List<Integer> pidms) {
        Map map = threadLocal.get()
        map.put("studentPidms", pidms)
        threadLocal.set(map)
    }


    private def getStudentPidmsFromThreadLocal() {
        Map map = threadLocal.get()
        return map.get("studentPidms")
    }


    private boolean isThreadLocalContainsStudentPidms() {
        return threadLocal.get().containsKey("studentPidms")
    }

}

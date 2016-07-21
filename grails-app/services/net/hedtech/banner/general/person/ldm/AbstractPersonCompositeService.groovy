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
import net.hedtech.banner.general.person.PersonAdvancedSearchViewService
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.person.PersonIdentificationNameCurrentService
import net.hedtech.banner.general.system.ldm.EmailTypeCompositeService
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import java.sql.Timestamp

abstract class AbstractPersonCompositeService extends LdmService {

    UserRoleCompositeService userRoleCompositeService
    PersonFilterCompositeService personFilterCompositeService
    CommonMatchingCompositeService commonMatchingCompositeService
    PersonIdentificationNameCurrentService personIdentificationNameCurrentService
    PersonAdvancedSearchViewService personAdvancedSearchViewService
    EmailTypeCompositeService emailTypeCompositeService


    abstract protected String getPopSelGuidOrDomainKey(final Map requestParams)


    abstract protected def prepareCommonMatchingRequest(final Map content)


    abstract protected Map processListApiRequest(final Map requestParams)


    abstract protected List<RoleName> getRolesRequired()


    abstract protected def createDecorators(List<PersonIdentificationNameCurrent> entities, def pidmToGuidMap)

    /**
     * GET /qapi/persons
     *
     * @param requestParams
     * @return
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def listQApi(Map requestParams) {
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

        if (requestProcessingResult.containsKey("totalCount")) {
            log.debug("Total records ${requestProcessingResult.totalCount}")
            injectPropertyIntoParams(requestParams, "totalCount", requestProcessingResult.totalCount)
        }

        return requestProcessingResult
    }

    /**
     * GET /api/persons
     *
     * @param requestParams
     * @return
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def listApi(Map requestParams) {
        Map requestProcessingResult = processListApiRequest(requestParams)

        if (requestProcessingResult.containsKey("totalCount")) {
            log.debug("Total records ${requestProcessingResult.totalCount}")
            injectPropertyIntoParams(requestParams, "totalCount", requestProcessingResult.totalCount)
        }

        return createDecorators(requestParams, requestProcessingResult)
    }

    /**
     * GET /api/persons
     * or
     * GET /qapi/persons
     *
     * The count method must return the total number of instances of the resource.
     * It is used in conjunction with the list method when returning a list of resources.
     * RestfulApiController will make call to "count" only if the "list" execution happens without any exception.
     *
     * @param requestParams
     * @return
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def count(Map requestParams) {
        return getInjectedPropertyFromParams(requestParams, "totalCount")
    }

    /**
     * GET /api/persons/<guid>
     *
     * @param guid
     * @return
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def get(String guid) {
        def row = personIdentificationNameCurrentService.fetchByGuid(guid)
        if (!row) {
            throw new ApplicationException("Person", new NotFoundException())
        }
        return createDecorators([row.personIdentificationNameCurrent], getPidmToGuidMap([row]))[0]
    }


    def createDecorators(final Map requestParams, final Map requestProcessingResult) {
        List<PersonIdentificationNameCurrent> personCurrentEntities = []
        def pidmToGuidMap = [:]
        if (requestProcessingResult.containsKey("pidms")) {
            List<Integer> pidms = requestProcessingResult.get("pidms")
            def rows = personIdentificationNameCurrentService.fetchAllWithGuidByPidmInList(pidms, requestParams.sort?.trim(), requestParams.order?.trim())
            personCurrentEntities = rows?.collect { it.personIdentificationNameCurrent }
            pidmToGuidMap = getPidmToGuidMap(rows)
        }
        return createDecorators(personCurrentEntities, pidmToGuidMap)
    }


    protected getPidmsOfPopulationExtract(final Map requestParams) {
        String guidOrDomainKey = getPopSelGuidOrDomainKey(requestParams)
        return personFilterCompositeService.fetchPidmsOfPopulationExtract(guidOrDomainKey, requestParams.sort?.trim(), requestParams.order?.trim(), requestParams.max?.trim()?.toInteger() ?: 0, requestParams.offset?.trim()?.toInteger() ?: 0)
    }


    protected def searchForMatchingPersons(final Map content) {
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
                        pidmToStudentRoleMap = getPidmToStudentRoleMap(pidms)
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

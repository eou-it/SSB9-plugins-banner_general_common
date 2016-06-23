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
import net.hedtech.banner.general.overall.ldm.v6.VisaStatusV6
import net.hedtech.banner.general.person.*
import net.hedtech.banner.general.person.ldm.v6.NameAlternateV6
import net.hedtech.banner.general.person.ldm.v6.NameV6
import net.hedtech.banner.general.person.ldm.v6.PersonV6
import net.hedtech.banner.general.person.ldm.v6.RoleV6
import net.hedtech.banner.general.system.CitizenType
import net.hedtech.banner.general.system.NameTypeService
import net.hedtech.banner.general.system.ldm.*
import net.hedtech.banner.general.system.ldm.v4.EmailTypeDetails
import net.hedtech.banner.general.system.ldm.v6.CitizenshipStatusV6
import net.hedtech.banner.general.system.ldm.v6.EmailV6
import net.hedtech.banner.general.system.ldm.v6.RaceV6
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

    UserRoleCompositeService userRoleCompositeService
    PersonFilterCompositeService personFilterCompositeService
    CitizenshipStatusCompositeService citizenshipStatusCompositeService
    VisaInformationService visaInformationService
    VisaTypeCompositeService visaTypeCompositeService
    ReligionCompositeService religionCompositeService
    PersonCredentialCompositeService personCredentialCompositeService
    PersonEmailService personEmailService
    EmailTypeCompositeService emailTypeCompositeService
    PersonRaceService personRaceService
    RaceCompositeService raceCompositeService
    EthnicityCompositeService ethnicityCompositeService
    NameTypeService nameTypeService
    PersonNameTypeCompositeService personNameTypeCompositeService
    PersonIdentificationNameAlternateService personIdentificationNameAlternateService

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
                    if (role == RoleName.INSTRUCTOR.v6) {
                        returnMap = userRoleCompositeService.fetchFaculties(sortField, sortOrder, max, offset)
                    } else if (role == RoleName.STUDENT.v6) {
                        returnMap = userRoleCompositeService.fetchStudents(sortField, sortOrder, max, offset)
                    } else if (role == RoleName.EMPLOYEE.v6) {
                        returnMap = userRoleCompositeService.fetchEmployees(sortField, sortOrder, max, offset)
                    } else if (role == RoleName.ALUMNI.v6) {
                        returnMap = userRoleCompositeService.fetchAlumnis(sortField, sortOrder, max, offset)
                    } else if (role == RoleName.VENDOR.v6) {
                        returnMap = userRoleCompositeService.fetchVendors(sortField, sortOrder, max, offset)
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
            fetchPersonsAlternateNameDataAndPutInMap(pidms, dataMap)
            fetchPersonsVisaDataAndPutInMap(pidms, dataMap)
            fetchPersonsRoleDataAndPutInMap(pidms, dataMap)
            personCredentialCompositeService.fetchPersonsCredentialDataAndPutInMap(pidms, dataMap)
            fetchPersonsEmailDataAndPutInMap(pidms, dataMap)
            fetchPersonsRaceDataAndPutInMap(pidms, dataMap)

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
                    if (personBase.ethnic) {
                        dataMapForPerson << ["usEthnicCodeGuid": dataMap.usEthnicCodeToGuidMap.get(personBase.ethnic)]
                    }
                }

                // names
                List<PersonIdentificationNameAlternate> personAlternateNames = dataMap.pidmToAlternateNamesMap.get(it.pidm)
                if (personAlternateNames) {
                    dataMapForPerson << ["bannerNameTypeToHedmNameTypeMap": dataMap.bannerNameTypeToHedmNameTypeMap]
                    dataMapForPerson << ["nameTypeCodeToGuidMap": dataMap.nameTypeCodeToGuidMap]
                    dataMapForPerson << ["personAlternateNames": personAlternateNames]
                }

                // visaStatus
                VisaInformation visaInfo = dataMap.pidmToVisaInfoMap.get(it.pidm)
                if (visaInfo) {
                    dataMapForPerson << ["visaInformation": visaInfo]
                    dataMapForPerson << ["visaTypeGuid": dataMap.vtCodeToGuidMap.get(visaInfo.visaType.code)]
                }

                // roles
                def personRoles = []
                if (dataMap.pidmToRolesMap.containsKey(it.pidm)) {
                    personRoles = dataMap.pidmToRolesMap.get(it.pidm)
                }
                dataMapForPerson << ["personRoles": personRoles]

                // credentials
                def personCredentials = []
                if (dataMap.pidmToCredentialsMap.containsKey(it.pidm)) {
                    personCredentials = dataMap.pidmToCredentialsMap.get(it.pidm)
                }
                personCredentials << [type: CredentialType.BANNER_ID, value: it.bannerId]
                dataMapForPerson << ["personCredentials": personCredentials]

                // emails
                List<PersonEmail> personEmailList = dataMap.pidmToEmailInfoMap.get(it.pidm)
                if (personEmailList) {
                    dataMapForPerson << ["emailInfo": personEmailList]
                    dataMapForPerson << ["emailTypeInfo": dataMap.etCodeToEmailTypeMap]
                }

                // races
                List<PersonRace> personRaces = dataMap.pidmToRacesMap.get(it.pidm)
                if (personRaces) {
                    dataMapForPerson << ["personRaces": personRaces]
                    dataMapForPerson << ["raceCodeToGuidMap": dataMap.raceCodeToGuidMap]
                }

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
                // ethnicity
                if (personBase.ethnic) {
                    String usEthnicCodeGuid = dataMapForPerson["usEthnicCodeGuid"]
                    decorator.ethnicity = ethnicityCompositeService.createEthnicityV6(usEthnicCodeGuid, null, personBase.ethnic)
                }
            }
            // Names
            decorator.names = []
            NameV6 nameV6 = createNameV6(personCurrent, personBase)
            decorator.names << nameV6
            def bannerNameTypeToHedmNameTypeMap = dataMapForPerson["bannerNameTypeToHedmNameTypeMap"]
            def nameTypeCodeToGuidMap = dataMapForPerson["nameTypeCodeToGuidMap"]
            List<PersonIdentificationNameAlternate> personAlternateNames = dataMapForPerson["personAlternateNames"]
            personAlternateNames?.each {
                decorator.names << createNameAlternateV6(it, bannerNameTypeToHedmNameTypeMap.get(it.nameType.code), nameTypeCodeToGuidMap.get(it.nameType.code))
            }
            // SPBPERS_LEGAL_NAME is the primary source
            NameAlternateV6 legalNameAlternateV6 = createLegalNameAlternateV6(personBase?.legalName)
            if (legalNameAlternateV6) {
                // Remove other alternate names of "legal" type
                decorator.names.removeAll {
                    it.type.category == NameTypeCategory.LEGAL.v6
                }
                // Add SPBPERS_LEGAL_NAME as "legal"
                decorator.names << legalNameAlternateV6
            }
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
            // Credentials
            def personCredentials = dataMapForPerson["personCredentials"]
            decorator.credentials = personCredentialCompositeService.createCredentialDecorators(personCredentials)
            // Emails
            List<PersonEmail> personEmailList = dataMapForPerson["emailInfo"]
            if (personEmailList) {
                Map emailTypeDetailsMap = dataMapForPerson["emailTypeInfo"]
                decorator.emails = []
                personEmailList.each {
                    decorator.emails << createEmailV6(it, emailTypeDetailsMap.get(it.emailType.code))
                }
            }
            // Races
            List<PersonRace> personRaces = dataMapForPerson["personRaces"]
            Map<String, String> raceCodeToGuidMap = dataMapForPerson["raceCodeToGuidMap"]
            if (personRaces) {
                decorator.races = []
                personRaces.each {
                    decorator.races << new RaceV6(raceCodeToGuidMap.get(it.race), raceCompositeService.getLdmRace(it.race))
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
        // Get GUIDs for US ethnic codes (SPBPERS_ETHN_CDE)
        Map<String, String> usEthnicCodeToGuidMap = ethnicityCompositeService.fetchGUIDsForUnitedStatesEthnicCodes()
        // Put in Map
        dataMap.put("pidmToPersonBaseMap", pidmToPersonBaseMap)
        dataMap.put("ctCodeToGuidMap", ctCodeToGuidMap)
        dataMap.put("relCodeToGuidMap", relCodeToGuidMap)
        dataMap.put("usEthnicCodeToGuidMap", usEthnicCodeToGuidMap)
    }


    private void fetchPersonsAlternateNameDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        def bannerNameTypeToHedmNameTypeMap = personNameTypeCompositeService.getBannerNameTypeToHEDMNameTypeMap()
        log.debug "Banner NameType to HEDM NameType mapping = ${bannerNameTypeToHedmNameTypeMap}"
        def nameTypeCodeToGuidMap = nameTypeService.fetchGUIDs(bannerNameTypeToHedmNameTypeMap.keySet().toList())
        log.debug "GUIDs for ${nameTypeCodeToGuidMap.keySet()} are ${nameTypeCodeToGuidMap.values()}"
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
        dataMap.put("nameTypeCodeToGuidMap", nameTypeCodeToGuidMap)
        dataMap.put("pidmToAlternateNamesMap", pidmToAlternateNamesMap)
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
        def pidmToRolesMap = [:]
        pidms.each {
            pidmToRolesMap.put(it, [])
        }
        // Faculty role
        List<Object[]> facList = userRoleCompositeService.fetchFacultiesByPIDMs(pidms)
        facList?.each {
            BigDecimal bdPidm = it[0]
            Timestamp startDate = it[1]
            Timestamp endDate = it[2]
            def personRoles = pidmToRolesMap.get(bdPidm.toInteger())
            personRoles << [role: RoleName.INSTRUCTOR, startDate: startDate, endDate: endDate]
        }
        // Student role
        List<BigDecimal> studList = userRoleCompositeService.fetchStudentsByPIDMs(pidms)
        studList?.each {
            def personRoles = pidmToRolesMap.get(it.toInteger())
            personRoles << [role: RoleName.STUDENT]
        }
        // Employee role
        List<Object[]> empList = userRoleCompositeService.fetchEmployeesByPIDMs(pidms)
        empList?.each {
            BigDecimal bdPidm = it[0]
            Timestamp startDate = it[1]
            Timestamp endDate = it[2]
            def personRoles = pidmToRolesMap.get(bdPidm.toInteger())
            personRoles << [role: RoleName.EMPLOYEE, startDate: startDate, endDate: endDate]
        }
        // Alumni role
        List<Object[]> alumniList = userRoleCompositeService.fetchAlumnisByPIDMs(pidms)
        alumniList?.each {
            BigDecimal bdPidm = it[0]
            Timestamp startDate = it[1]
            def personRoles = pidmToRolesMap.get(bdPidm.toInteger())
            personRoles << [role: RoleName.ALUMNI, startDate: startDate]
        }
        // Vendor role
        List<Object[]> vendorList = userRoleCompositeService.fetchVendorsByPIDMs(pidms)
        vendorList?.each {
            BigDecimal bdPidm = it[0]
            Timestamp startDate = it[1]
            Timestamp endDate = it[2]
            def personRoles = pidmToRolesMap.get(bdPidm.toInteger())
            personRoles << [role: RoleName.VENDOR, startDate: startDate, endDate: endDate]
        }
        // Put in Map
        dataMap.put("pidmToRolesMap", pidmToRolesMap)
    }


    private void fetchPersonsEmailDataAndPutInMap(List<Integer> pidms, Map dataMap) {

        // Get GUIDs for Email types
        Map etCodeToEmailTypeMap = [:]
        etCodeToEmailTypeMap = emailTypeCompositeService.fetchAllMappedEmailTypes()
        log.debug "Got ${etCodeToEmailTypeMap?.size() ?: 0} GUIDs for given EmailType codes"

        // Get GOREMAL records for persons
        Map pidmToEmailInfoMap = fetchPersonEmailByPIDMs(pidms, etCodeToEmailTypeMap.keySet())

        // Put in Map
        dataMap.put("pidmToEmailInfoMap", pidmToEmailInfoMap)
        dataMap.put("etCodeToEmailTypeMap", etCodeToEmailTypeMap)
    }


    private void fetchPersonsRaceDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        // Get GORPRAC records for persons
        Map pidmToRacesMap = [:]
        if (pidms) {
            log.debug "Getting GV_GORPRAC records for ${pidms?.size()} PIDMs..."
            List<PersonRace> entities = personRaceService.fetchRaceByPidmList(pidms)
            log.debug "Got ${entities?.size()} GV_GORPRAC records"
            entities?.each {
                List<PersonRace> personRaces = []
                if (pidmToRacesMap.containsKey(it.pidm)) {
                    personRaces = pidmToRacesMap.get(it.pidm)
                } else {
                    pidmToRacesMap.put(it.pidm, personRaces)
                }
                personRaces.add(it)
            }
        }
        // Get GUIDs for race codes
        Set<String> raceCodes = pidmToRacesMap?.values().race.flatten() as Set
        Map raceCodeToGuidMap = [:]
        if (raceCodes) {
            log.debug "Getting GUIDs for Races codes $raceCodes..."
            raceCodeToGuidMap = raceCompositeService.fetchGuids(raceCodes)
            log.debug "Got ${raceCodeToGuidMap?.size() ?: 0} GUIDs for given race codes"
        }
        // Put in Map
        dataMap.put("pidmToRacesMap", pidmToRacesMap)
        dataMap.put("raceCodeToGuidMap", raceCodeToGuidMap)
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


    private Map fetchPersonEmailByPIDMs(List<Integer> pidms, Set<String> codes) {
        Map pidmToEmailInfoMap = [:]
        List<PersonEmail> emailInfo
        if (pidms) {
            log.debug "Getting GOREMAL records for ${pidms?.size()} PIDMs..."
            List<PersonEmail> entities = personEmailService.fetchAllActiveEmails(pidms, codes)
            log.debug "Got ${entities?.size()} GOREMAL records"
            entities.each {
                if (pidmToEmailInfoMap.containsKey(it.pidm)) {
                    emailInfo << it
                } else {
                    emailInfo = []
                    emailInfo << it
                }
                pidmToEmailInfoMap.put(it.pidm, emailInfo)
            }
        }
        return pidmToEmailInfoMap
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


    private NameV6 createNameV6(PersonIdentificationNameCurrent personCurrent, PersonBasicPersonBase personBase) {
        NameV6 decorator
        if (personCurrent) {
            decorator = new NameV6()
            decorator.type = ["category": NameTypeCategory.PERSONAL.v6]
            decorator.fullName = personCurrent.fullName
            decorator.firstName = personCurrent.firstName
            decorator.middleName = personCurrent.middleName
            decorator.lastName = personCurrent.lastName
            decorator.lastNamePrefix = personCurrent.surnamePrefix
            if (personBase) {
                decorator.title = personBase.namePrefix
                decorator.pedigree = personBase.nameSuffix
            }
        }
        return decorator
    }


    private NameAlternateV6 createNameAlternateV6(PersonIdentificationNameAlternate personAlternate, NameTypeCategory nameTypeCategory, String nameTypeGuid) {
        NameAlternateV6 decorator
        if (personAlternate) {
            decorator = new NameAlternateV6()
            decorator.type = ["category": nameTypeCategory.v6, "detail": ["id": nameTypeGuid]]
            decorator.fullName = personAlternate.fullName
            decorator.firstName = personAlternate.firstName
            decorator.middleName = personAlternate.middleName
            decorator.lastName = personAlternate.lastName
            decorator.lastNamePrefix = personAlternate.surnamePrefix
        }
        return decorator
    }


    private NameAlternateV6 createLegalNameAlternateV6(String fullName) {
        NameAlternateV6 decorator
        if (fullName && fullName.trim().length() > 0) {
            decorator = new NameAlternateV6()
            decorator.type = ["category": NameTypeCategory.LEGAL.v6]
            decorator.fullName = fullName
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


    private RoleV6 createRoleV6(RoleName roleName, Timestamp startDate, Timestamp endDate) {
        RoleV6 decorator
        if (roleName) {
            decorator = new RoleV6()
            decorator.role = roleName.v6
            if (startDate) {
                decorator.startOn = DateConvertHelperService.convertDateIntoUTCFormat(startDate)
            }
            if (endDate) {
                decorator.endOn = DateConvertHelperService.convertDateIntoUTCFormat(endDate)
            }
        }
        return decorator
    }


    private EmailV6 createEmailV6(PersonEmail it, def emailType) {
        EmailV6 emailV6 = new EmailV6()
        emailV6.address = it.emailAddress
        emailV6.type = new EmailTypeDetails(emailType[0].code, emailType[0].description, emailType[1].guid, emailType[2].translationValue)
        emailV6.preference = it.preferredIndicator ? 'primaryOverall' : ''
        return emailV6
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

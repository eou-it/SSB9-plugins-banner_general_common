/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.overall.VisaInformation
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.v6.VisaStatusV6
import net.hedtech.banner.general.person.*
import net.hedtech.banner.general.person.ldm.v6.NameAlternateV6
import net.hedtech.banner.general.person.ldm.v6.NameV6
import net.hedtech.banner.general.person.ldm.v6.PersonV6
import net.hedtech.banner.general.person.ldm.v6.RoleV6
import net.hedtech.banner.general.system.CitizenType
import net.hedtech.banner.general.system.ldm.NameTypeCategory
import net.hedtech.banner.general.system.ldm.v4.EmailTypeDetails
import net.hedtech.banner.general.system.ldm.v4.PhoneTypeDecorator
import net.hedtech.banner.general.system.ldm.v6.CitizenshipStatusV6
import net.hedtech.banner.general.system.ldm.v6.EmailV6
import net.hedtech.banner.general.system.ldm.v6.PhoneV6
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
class PersonV6CompositeService extends AbstractPersonCompositeService {

    static final int DEFAULT_PAGE_SIZE = 500
    static final int MAX_PAGE_SIZE = 500

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

            String contentType = getRequestRepresentation()
            if (contentType && contentType.contains("duplicate-check")) {
                log.info "Person duplicate check request with Content-Type ${contentType}"
                def returnMap = searchForMatchingPersons(params)
                pidms = returnMap?.pidms
                totalCount = returnMap?.totalCount
            }

        } else {

            if (params.containsKey("role")) {
                String role = params.role?.trim()
                log.debug "Fetching persons with role $role ...."
                def returnMap
                if (role == RoleName.INSTRUCTOR.versionToEnumMap["v6"]) {
                    returnMap = userRoleCompositeService.fetchFaculties(sortField, sortOrder, max, offset)
                } else if (role == RoleName.STUDENT.versionToEnumMap["v6"]) {
                    returnMap = userRoleCompositeService.fetchStudents(sortField, sortOrder, max, offset)
                } else if (role == RoleName.EMPLOYEE.versionToEnumMap["v6"]) {
                    returnMap = userRoleCompositeService.fetchEmployees(sortField, sortOrder, max, offset)
                } else if (role == RoleName.ALUMNI.versionToEnumMap["v6"]) {
                    returnMap = userRoleCompositeService.fetchAlumnis(sortField, sortOrder, max, offset)
                } else if (role == RoleName.VENDOR.versionToEnumMap["v6"]) {
                    returnMap = userRoleCompositeService.fetchVendors(sortField, sortOrder, max, offset)
                } else if (role == RoleName.PROSPECTIVE_STUDENT.versionToEnumMap["v6"]) {
                    returnMap = userRoleCompositeService.fetchProspectiveStudents(sortField, sortOrder, max, offset)
                } else if (role == RoleName.ADVISOR.versionToEnumMap["v6"]) {
                    returnMap = userRoleCompositeService.fetchAdvisors(sortField, sortOrder, max, offset)
                } else {
                    throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("role.supported.v6", []))
                }
                pidms = returnMap?.pidms
                totalCount = returnMap?.totalCount
                log.debug "${totalCount} persons found with role $role."
            } else if (params.containsKey("credential.type") && params.containsKey("credential.value")) {
                String credentialType = params.get("credential.type")?.trim()
                String credentialValue = params.get("credential.value")?.trim()
                if (credentialType == CredentialType.BANNER_ID.versionToEnumMap["v6"]) {
                    PersonIdentificationNameCurrent personCurrent = PersonIdentificationNameCurrent.fetchByBannerId(credentialValue)
                    if (personCurrent && personCurrent.entityIndicator == 'P') {
                        pidms = [personCurrent.pidm]
                        totalCount = 1
                    }
                }
            } else if (params.containsKey("lastName") || params.containsKey("firstName") || params.containsKey("middleName") || params.containsKey("lastNamePrefix") || params.containsKey("title") || params.containsKey("pedigree")) {

            } else if (params.containsKey("personFilter")) {
                String guidOrDomainKey = params.get("personFilter")
                def returnMap = personFilterCompositeService.fetchPidmsOfPopulationExtract(guidOrDomainKey, sortField, sortOrder, max, offset)
                pidms = returnMap?.pidms
                totalCount = returnMap?.totalCount
                log.debug "${totalCount} persons in population extract ${guidOrDomainKey}."
            } else {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("role.required", null))
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
            fetchPersonsPhoneDataAndPutInMap(pidms, dataMap)

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
                List<PersonEmail> personEmailList = dataMap.pidmToEmailsMap.get(it.pidm)
                if (personEmailList) {
                    dataMapForPerson << ["personEmails": personEmailList]
                    dataMapForPerson << ["bannerEmailTypeToHedmEmailTypeMap": dataMap.bannerEmailTypeToHedmEmailTypeMap]
                    dataMapForPerson << ["emailCodeToGuidMap": dataMap.emailCodeToGuidMap]
                }

                // races
                List<PersonRace> personRaces = dataMap.pidmToRacesMap.get(it.pidm)
                if (personRaces) {
                    dataMapForPerson << ["personRaces": personRaces]
                    dataMapForPerson << ["raceCodeToGuidMap": dataMap.raceCodeToGuidMap]
                }

                // phones
                List<PersonTelephone> personTelephoneList = dataMap.pidmToPhonesMap.get(it.pidm)
                if (personTelephoneList) {
                    dataMapForPerson << ["personPhones": personTelephoneList]
                    dataMapForPerson << ["bannerPhoneTypeToHedmPhoneTypeMap": dataMap.bannerPhoneTypeToHedmPhoneTypeMap]
                    dataMapForPerson << ["phoneCodeToGuidMap": dataMap.phoneCodeToGuidMap]
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
                    it.type.category == NameTypeCategory.LEGAL.versionToEnumMap["v6"]
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
            List<PersonEmail> personEmailList = dataMapForPerson["personEmails"]
            if (personEmailList) {
                Map emailCodeToGuidMap = dataMapForPerson["emailCodeToGuidMap"]
                Map bannerEmailTypeToHedmEmailTypeMap = dataMapForPerson["bannerEmailTypeToHedmEmailTypeMap"]
                decorator.emails = []
                personEmailList.each {
                    decorator.emails << createEmailV6(it, it.emailType.code, emailCodeToGuidMap.get(it.emailType.code), bannerEmailTypeToHedmEmailTypeMap.get(it.emailType.code))
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
            // Phones
            List<PersonTelephone> personTelephoneListList = dataMapForPerson["personPhones"]

            if (personTelephoneListList) {
                Map phoneCodeToGuidMap = dataMapForPerson["phoneCodeToGuidMap"]
                Map bannerPhoneTypeToHedmPhoneTypeMap = dataMapForPerson["bannerPhoneTypeToHedmPhoneTypeMap"]

                decorator.phones = []
                personTelephoneListList.each {
                    decorator.phones << createPhoneV6(it, it.telephoneType.code, phoneCodeToGuidMap.get(it.telephoneType.code), bannerPhoneTypeToHedmPhoneTypeMap.get(it.telephoneType.code))
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
        def bannerNameTypeToHedmNameTypeMap = personNameTypeCompositeService.getBannerNameTypeToHedmV6NameTypeMap()
        log.debug "Banner NameType to HEDM NameType mapping = ${bannerNameTypeToHedmNameTypeMap}"
        def nameTypeCodeToGuidMap = personNameTypeCompositeService.getNameTypeCodeToGuidMap(bannerNameTypeToHedmNameTypeMap.keySet())
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


    private void fetchPersonsEmailDataAndPutInMap(List<Integer> pidms, Map dataMap) {

        //Get Mapped Codes for Email Types
        Map<String, String> bannerEmailTypeToHedmEmailTypeMap = emailTypeCompositeService.getBannerEmailTypeToHedmV6EmailTypeMap()

        // Get GUIDs for Email types
        Map<String, String> emailCodeToGuidMap = emailTypeCompositeService.getEmailTypeCodeToGuidMap(bannerEmailTypeToHedmEmailTypeMap.keySet())
        log.debug "Got ${emailCodeToGuidMap?.size() ?: 0} GUIDs for given EmailType codes"

        // Get GOREMAL records for persons
        Map pidmToEmailsMap = fetchPersonEmailByPIDMs(pidms, emailCodeToGuidMap.keySet())

        // Put in Map
        dataMap.put("bannerEmailTypeToHedmEmailTypeMap", bannerEmailTypeToHedmEmailTypeMap)
        dataMap.put("pidmToEmailsMap", pidmToEmailsMap)
        dataMap.put("emailCodeToGuidMap", emailCodeToGuidMap)
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
            raceCodeToGuidMap = raceCompositeService.getRaceCodeToGuidMap(raceCodes)
            log.debug "Got ${raceCodeToGuidMap?.size() ?: 0} GUIDs for given race codes"
        }
        // Put in Map
        dataMap.put("pidmToRacesMap", pidmToRacesMap)
        dataMap.put("raceCodeToGuidMap", raceCodeToGuidMap)
    }


    private void fetchPersonsPhoneDataAndPutInMap(List<Integer> pidms, Map dataMap) {

        Map<String, String> bannerPhoneTypeToHedmPhoneTypeMap = phoneTypeCompositeService.getBannerPhoneTypeToHedmV6PhoneTypeMap()

        // Get GUIDs for Phone types
        Map phoneCodeToGuidMap = phoneTypeCompositeService.getPhoneTypeCodeToGuidMap(bannerPhoneTypeToHedmPhoneTypeMap.keySet())
        log.debug "Got ${phoneCodeToGuidMap?.size() ?: 0} GUIDs for given PhoneType codes"

        // Get SPRTELE records for persons
        Map pidmToPhonesMap = fetchPersonPhoneByPIDMs(pidms, phoneCodeToGuidMap.keySet())

        // Put in Map
        dataMap.put("pidmToPhonesMap", pidmToPhonesMap)
        dataMap.put("phoneCodeToGuidMap", phoneCodeToGuidMap)
        dataMap.put("bannerPhoneTypeToHedmPhoneTypeMap", bannerPhoneTypeToHedmPhoneTypeMap)
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


    private Map fetchPersonPhoneByPIDMs(List<Integer> pidms, Set<String> codes) {
        Map pidmToPhoneInfoMap = [:]
        List<PersonTelephone> phoneInfo
        if (pidms) {
            log.debug "Getting SPRTELE records for ${pidms?.size()} PIDMs..."
            List<PersonTelephone> entities = personTelephoneService.fetchAllActiveByPidmInListAndTelephoneTypeCodeInList(pidms, codes)
            log.debug "Got ${entities?.size()} SPRTELE records"
            entities.each {
                if (pidmToPhoneInfoMap.containsKey(it.pidm)) {
                    phoneInfo << it
                } else {
                    phoneInfo = []
                    phoneInfo << it
                }
                pidmToPhoneInfoMap.put(it.pidm, phoneInfo)
            }
        }
        return pidmToPhoneInfoMap
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
            decorator.type = ["category": NameTypeCategory.PERSONAL.versionToEnumMap["v6"]]
            decorator.fullName = prepareFullName(personCurrent.firstName, personCurrent.middleName, personCurrent.lastName)
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


    private NameAlternateV6 createNameAlternateV6(PersonIdentificationNameAlternate personAlternate, String nameTypeCategory, String nameTypeGuid) {
        NameAlternateV6 decorator
        if (personAlternate) {
            decorator = new NameAlternateV6()
            decorator.type = ["category": nameTypeCategory, "detail": ["id": nameTypeGuid]]
            decorator.fullName = prepareFullName(personAlternate.firstName, personAlternate.middleName, personAlternate.lastName)
            decorator.firstName = personAlternate.firstName
            decorator.middleName = personAlternate.middleName
            decorator.lastName = personAlternate.lastName
            decorator.lastNamePrefix = personAlternate.surnamePrefix
        }
        return decorator
    }


    private String prepareFullName(String firstName, String middleName, String lastName) {
        StringBuilder sb = new StringBuilder()

        if (firstName) {
            sb.append(firstName)
            sb.append(' ')
        }

        if (middleName) {
            sb.append(middleName)
            sb.append(' ')
        }

        if (lastName) {
            sb.append(lastName)
        }

        return sb.toString()
    }


    private NameAlternateV6 createLegalNameAlternateV6(String fullName) {
        NameAlternateV6 decorator
        if (fullName && fullName.trim().length() > 0) {
            decorator = new NameAlternateV6()
            decorator.type = ["category": NameTypeCategory.LEGAL.versionToEnumMap["v6"]]
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
            decorator.role = roleName.versionToEnumMap["v6"]
            if (startDate) {
                decorator.startOn = DateConvertHelperService.convertDateIntoUTCFormat(startDate)
            }
            if (endDate) {
                decorator.endOn = DateConvertHelperService.convertDateIntoUTCFormat(endDate)
            }
        }
        return decorator
    }


    private EmailV6 createEmailV6(PersonEmail it, String code, String guid, String emailType) {
        EmailV6 emailV6 = new EmailV6()
        emailV6.address = it.emailAddress
        emailV6.type = new EmailTypeDetails(code, null, guid, emailType)
        if (it.preferredIndicator) {
            emailV6.preference = 'primaryOverall'
        }
        return emailV6
    }


    private PhoneV6 createPhoneV6(PersonTelephone it, String code, String guid, String phoneType) {
        PhoneV6 phoneV6 = new PhoneV6()
        phoneV6.countryCallingCode = it.countryPhone
        phoneV6.number = (it.phoneArea ?: "") + (it.phoneNumber ?: "")
        phoneV6.extension = it.phoneExtension
        phoneV6.type = new PhoneTypeDecorator(code, null, guid, phoneType)
        if (it.primaryIndicator) {
            phoneV6.preference = 'primary'
        }
        return phoneV6
    }


    @Override
    def prepareRequestForCommonMatching(final Map content) {
        def cmRequest = [:]

        // First name, middle name, last name
        def personalName = content.names.find {
            it.type.category == NameTypeCategory.PERSONAL.versionToEnumMap["v6"] && it.firstName && it.lastName
        }

        def birthName = content.names.find {
            it.type.category == NameTypeCategory.BIRTH.versionToEnumMap["v6"] && it.firstName && it.lastName
        }

        if (personalName && birthName) {
            throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("filter.together.not.supported", null))
        }

        def nameObj = personalName
        if (!nameObj) {
            nameObj = birthName
        }
        if (!nameObj) {
            throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("name.and.type.required.message", null))
        }

        cmRequest << [firstName: nameObj.firstName, lastName: nameObj.lastName]
        if (nameObj.middleName) {
            cmRequest << [mi: nameObj.middleName]
        }

        // Social Security Number
        def credentialObj = content.credentials.find {
            it.type == CredentialType.SOCIAL_SECURITY_NUMBER.versionToEnumMap["v6"]
        }
        if (credentialObj?.value) {
            cmRequest << [ssn: credentialObj?.value]
        }

        // Banner ID
        credentialObj = content.credentials.find {
            it.type == CredentialType.BANNER_ID.versionToEnumMap["v6"]
        }
        if (credentialObj?.value) {
            cmRequest << [bannerId: credentialObj?.value]
        }

        // Gender
        String gender
        if (content?.gender == 'male') {
            gender = 'M'
        } else if (content?.gender == 'female') {
            gender = 'F'
        } else if (content?.gender == 'unknown') {
            gender = 'N'
        }
        if (gender) {
            cmRequest << [sex: gender]
        }

        // Date of Birth
        Date dob
        if (content?.dateOfBirth) {
            dob = convertString2Date(content?.dateOfBirth)
            cmRequest << [dateOfBirth: dob]
        }

        // Emails
        def personEmails = []
        if (content?.emails) {
            Map<String, String> bannerEmailTypeToHedmEmailTypeMap = emailTypeCompositeService.getBannerEmailTypeToHedmV6EmailTypeMap()
            if (bannerEmailTypeToHedmEmailTypeMap) {
                content?.emails.each {
                    def mapEntry = bannerEmailTypeToHedmEmailTypeMap.find { key, value -> value == it.type.emailType }
                    if (mapEntry) {
                        personEmails << [email: it.address, emailType: mapEntry.key]
                    }
                }
            }
            cmRequest << [personEmails: personEmails]
        }

        return cmRequest
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


    @Override
    List<RoleName> getRolesRequired() {
        return [RoleName.STUDENT, RoleName.INSTRUCTOR, RoleName.EMPLOYEE, RoleName.VENDOR, RoleName.ALUMNI, RoleName.PROSPECTIVE_STUDENT, RoleName.ADVISOR]
    }

}

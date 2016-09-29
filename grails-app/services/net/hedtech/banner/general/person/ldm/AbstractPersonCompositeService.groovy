/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.commonmatching.CommonMatchingCompositeService
import net.hedtech.banner.general.lettergeneration.ldm.PersonFilterCompositeService
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.person.*
import net.hedtech.banner.general.system.NameType
import net.hedtech.banner.general.system.ldm.*
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import java.sql.Timestamp

abstract class AbstractPersonCompositeService extends LdmService {

    private static final int MAX_DEFAULT = 500
    private static final int MAX_UPPER_LIMIT = 500
    static final String ldmName = 'persons'

    private static final String PERSON_NAME_TYPE = "PERSON.NAMES.NAMETYPE"

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
    PersonEmailService personEmailService
    EthnicityCompositeService ethnicityCompositeService
    PersonNameTypeCompositeService personNameTypeCompositeService
    PersonIdentificationNameAlternateService personIdentificationNameAlternateService
    PersonCredentialCompositeService personCredentialCompositeService
    AddressTypeCompositeService addressTypeCompositeService
    PersonAddressService personAddressService
    PhoneTypeCompositeService phoneTypeCompositeService
    PersonTelephoneService personTelephoneService
    PersonRaceService personRaceService
    RaceCompositeService raceCompositeService
    PersonBasicPersonBaseService personBasicPersonBaseService


    abstract protected String getPopSelGuidOrDomainKey(final Map requestParams)


    abstract protected def prepareCommonMatchingRequest(final Map content)


    abstract protected Map processListApiRequest(final Map requestParams)


    abstract protected void fetchDataAndPutInMap_VersonSpecific(List<Integer> pidms, Map dataMap)


    abstract protected def getBannerNameTypeToHedmNameTypeMap()


    abstract protected List<RoleName> getRolesRequired()


    abstract protected def getBannerAddressTypeToHedmAddressTypeMap()


    abstract protected def getBannerPhoneTypeToHedmPhoneTypeMap()


    abstract protected def getBannerEmailTypeToHedmEmailTypeMap()

    abstract protected def extractDataFromRequestBody(Map content)


    abstract
    protected void prepareDataMapForSinglePerson_VersionSpecific(PersonIdentificationNameCurrent personIdentificationNameCurrent,
                                                                 final Map dataMap, Map dataMapForPerson)


    abstract protected def createPersonDataModel(final Map dataMapForPerson)

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

    /**
     * POST /api/persons
     *
     * @param content Request body
     */
    def create(Map person) {
        Map requestData = extractDataFromRequestBody(person)
        def newPersonIdentification = [:]
        PersonIdentificationNameCurrent newPersonIdentificationName
        PersonIdentificationNameAlternate personIdentificationNameAlternate
        String dataOrigin

        String personGuid
        if (requestData.containsKey("personGuid") && requestData.get("personGuid").length() > 0) {
            personGuid = requestData.get('personGuid')
        }

        if ((requestData.containsKey("firstName") && requestData.get("firstName")?.length() > 0) && (requestData.containsKey("lastName") && requestData.get("lastName")?.length() > 0)) {
            newPersonIdentification.put('firstName', requestData.get("firstName"))
            newPersonIdentification.put('lastName', requestData.get("lastName"))
            newPersonIdentification.put('bannerId', 'GENERATED')
            newPersonIdentification.put('entityIndicator', 'P')
            newPersonIdentification.put('bannerId', 'GENERATED')
            newPersonIdentification.put('entityIndicator', 'P')
            newPersonIdentification.put('changeIndicator', null)

            if (requestData.containsKey("middleName") && requestData.get("middleName")?.length() > 0) {
                newPersonIdentification.put('middleName', requestData.get("middleName"))
            }
            if (requestData.containsKey("surnamePrefix") && requestData.get("surnamePrefix")?.length() > 0) {
                newPersonIdentification.put('surnamePrefix', requestData.get("surnamePrefix"))
            }
            if (requestData.containsKey("dataOrigin") && requestData.get("dataOrigin")?.length() > 0) {
                dataOrigin = requestData.get("dataOrigin")
            }
            if (dataOrigin) {
                newPersonIdentification.put('dataOrigin', dataOrigin)
            }

            newPersonIdentification.remove('nameType') // ID won't generate if this is set.
            //Create the new PersonIdentification record
            newPersonIdentificationName = personIdentificationNameCurrentService.create(newPersonIdentification)

        }

        if ((requestData.containsKey("alternateNames") && requestData.get("alternateNames").size() > 0)) {
            def alternateNames = requestData.get("alternateNames")
            alternateNames.each { nameRecord ->
                personIdentificationNameAlternate = createPersonIdentificationNameAlternateByNameType(newPersonIdentificationName,
                        nameRecord,
                        dataOrigin)
            }
        }

        //Fix the GUID if provided as DB will assign one
        if (personGuid && personGuid != GeneralValidationCommonConstants.NIL_GUID) {
            // Overwrite the GUID created by DB insert trigger, with the one provided in the request body
            updateGuidValue(newPersonIdentificationName.id, personGuid, ldmName)
        } else {
            GlobalUniqueIdentifier entity = GlobalUniqueIdentifier.findByLdmNameAndDomainId(ldmName, newPersonIdentificationName.id)
            personGuid = entity.guid
        }
        log.debug("GUID: ${personGuid}")

        //Copy personBase attributes into person map from Primary names object.
        Map personBase = [:]
        personBase.put('dataOrigin', dataOrigin)
        if (requestData.containsKey("namePrefix") && requestData.get("namePrefix").length() > 0) {
            personBase.put('namePrefix', requestData.get("namePrefix"))
        }
        if (requestData.containsKey("nameSuffix") && requestData.get("nameSuffix").length() > 0) {
            personBase.put('nameSuffix', requestData.get("nameSuffix"))
        }
        personBase.put('armedServiceMedalVetIndicator', false)
        personBase.put('pidm', newPersonIdentificationName?.pidm)
        PersonBasicPersonBase newPersonBase = personBasicPersonBaseService.create(personBase)
        Map pidmToGuidMap = [:]
        pidmToGuidMap.put(newPersonIdentificationName.pidm, personGuid)

        return createPersonDataMapForSinglePerson(newPersonIdentificationName, newPersonBase, personGuid)

    }

    /**
     * PUT /api/persons/<guid>
     *
     * @param content Request body
     */
    def update(Map person) {
        Map requestData = extractDataFromRequestBody(person)

        String personGuid
        if (requestData.containsKey("personGuid") && requestData.get("personGuid").length() > 0) {
            personGuid = requestData.get('personGuid')
        }
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByGuid(ldmName, personGuid)
        if (!globalUniqueIdentifier) {
            return create(person)
        }
        String dataOrigin
        if (requestData.containsKey("dataOrigin") && requestData.get("dataOrigin")?.length() > 0) {
            dataOrigin = requestData.get("dataOrigin")
        }

        def pidm = globalUniqueIdentifier.domainKey?.toInteger()
        List<PersonIdentificationNameCurrent> personIdentificationList = PersonIdentificationNameCurrent.findAllByPidmInList([pidm])

        PersonIdentificationNameCurrent personIdentification
        personIdentificationList.each { identification ->
            if (identification.changeIndicator == null) {
                personIdentification = identification
            }
        }

        //update PersonIdentificationNameCurrent
        PersonIdentificationNameCurrent newPersonIdentificationName
        PersonIdentificationNameCurrent oldPersonIdentificationName = new PersonIdentificationNameCurrent(personIdentification.properties)
        if ((requestData.containsKey("firstName") && requestData.get("firstName")?.length() > 0) && (requestData.containsKey("lastName") && requestData.get("lastName")?.length() > 0)) {
            personIdentification.firstName = requestData.get("firstName")
            personIdentification.lastName = requestData.get("lastName")
            if (requestData.containsKey('middleName')) {
                personIdentification.middleName = requestData.get("middleName")
            }
            if (requestData.containsKey('surnamePrefix')) {
                personIdentification.surnamePrefix = requestData.get("surnamePrefix")
            }
            if (!personIdentification.equals(oldPersonIdentificationName)) {
                PersonIdentificationNameAlternate.findAllByPidm(oldPersonIdentificationName.pidm).each { oldRecord ->
                    if (oldPersonIdentificationName.firstName == oldRecord.firstName &&
                            oldPersonIdentificationName.lastName == oldRecord.lastName &&
                            oldPersonIdentificationName.middleName == oldRecord.middleName &&
                            oldPersonIdentificationName.surnamePrefix == oldRecord.surnamePrefix &&
                            oldPersonIdentificationName.bannerId == oldRecord.bannerId &&
                            oldPersonIdentificationName.nameType == oldRecord.nameType &&
                            oldRecord.changeIndicator == 'N'
                    ) {
                        //Can't get around this, Hibernate updates before it deletes, triggering table-api errors.
                        PersonIdentificationNameAlternate.executeUpdate("delete from PersonIdentificationNameAlternate where id = :id", [id: oldRecord.id])
                    }
                }
                newPersonIdentificationName = personIdentificationNameCurrentService.update(personIdentification)
            }
        }
        if (!newPersonIdentificationName) {
            newPersonIdentificationName = personIdentification
        }

        def alternateNames
        if ((requestData.containsKey("alternateNames") && requestData.get("alternateNames").size() > 0)) {
            alternateNames = requestData.get("alternateNames")
            alternateNames.each { nameRecord ->
                PersonIdentificationNameAlternate existingPersonRecord = getPersonIdentificationNameAlternateByNameType(newPersonIdentificationName?.pidm, nameRecord.type)
                PersonIdentificationNameAlternate newPersonBirthRecord = null
                if (!isNamesElementSameAsExisting(nameRecord.firstName, nameRecord.middleName, nameRecord.lastName, nameRecord.surnamePrefix, existingPersonRecord)) {
                    newPersonBirthRecord = createPersonIdentificationNameAlternateByNameType(newPersonIdentificationName,
                            nameRecord,
                            dataOrigin)
                }
            }
        }

        //update PersonBasicPersonBase
        PersonBasicPersonBase newPersonBase = updatePersonBasicPersonBase(pidm, newPersonIdentificationName, requestData.get('namePrefix'),
                requestData.get('nameSuffix'),
                requestData.get('preferenceFirstName'),
                alternateNames,
                dataOrigin)

        Map pidmToGuidMap = [:]
        pidmToGuidMap.put(newPersonIdentificationName.pidm, personGuid)

        return createPersonDataMapForSinglePerson(newPersonIdentificationName, newPersonBase, personGuid)
    }


    private PersonBasicPersonBase updatePersonBasicPersonBase(pidm, newPersonIdentificationName, namePrefix, nameSuffix, preferenceFirstName, alternateNames, dataOrigin) {
        List<PersonBasicPersonBase> personBaseList = PersonBasicPersonBase.findAllByPidmInList([pidm])
        PersonBasicPersonBase newPersonBase

        if (personBaseList.size() == 0) {
            //if there is no person base then create new PersonBase
            newPersonBase = createPersonBasicPersonBase(newPersonIdentificationName, namePrefix, nameSuffix, preferenceFirstName, dataOrigin)
        } else {
            personBaseList.each { personBase ->
                //Copy personBase attributes into person map from Primary names object.
                if (namePrefix != null) {
                    personBase.namePrefix = namePrefix
                }
                if (nameSuffix != null) {
                    personBase.nameSuffix = nameSuffix
                }
                if (preferenceFirstName != null) {
                    personBase.preferenceFirstName = preferenceFirstName
                }

                def legalName = alternateNames?.find { it.type == "LEGL" }
                if (legalName) {
                    String legalFullName
                    if (personBase.legalName) {
                        legalFullName = legalName.firstName
                        if (legalName.middleName && legalName.middleName?.length() > 0) {
                            legalFullName = legalFullName.concat(" " + legalName.middleName)
                        }
                        legalFullName = legalFullName.concat(" " + legalName.lastName)
                        if (!(personBase.legalName.equals(legalFullName))) {
                            personBase.legalName = legalFullName
                        }
                    }
                }
                newPersonBase = personBasicPersonBaseService.update(personBase)
            }

        }
        return newPersonBase
    }


    private PersonBasicPersonBase createPersonBasicPersonBase(newPersonIdentificationName, namePrefix, nameSuffix, preferenceFirstName, dataOrigin) {
        Map person = [:]
        PersonBasicPersonBase newPersonBase

        //Copy personBase attributes into person map from Primary names object.
        person.put('dataOrigin', dataOrigin)
        if (namePrefix && namePrefix.length() > 0) {
            person.namePrefix = namePrefix
        }
        if (nameSuffix && nameSuffix.length() > 0) {
            person.nameSuffix = nameSuffix
        }
        if (preferenceFirstName && preferenceFirstName.length() > 0) {
            person.preferenceFirstName = preferenceFirstName
        }
        person.put('pidm', newPersonIdentificationName?.pidm)
        person.put('armedServiceMedalVetIndicator', false)
        newPersonBase = personBasicPersonBaseService.create(person)
        newPersonBase
    }


    private boolean isNamesElementSameAsExisting(String firstName, String lastName, String middleName, String surnamePrefix, PersonIdentificationNameAlternate existingPersonRecord) {
        boolean exists = true
        if (!existingPersonRecord || !(existingPersonRecord?.firstName == firstName) || !(existingPersonRecord?.lastName == lastName) || !(existingPersonRecord?.middleName == middleName) || !(existingPersonRecord?.surnamePrefix == surnamePrefix)) {
            exists = false
        }

        return exists
    }


    private PersonIdentificationNameAlternate getPersonIdentificationNameAlternateByNameType(Integer pidm, String nameTypeCode) {
        return personIdentificationNameAlternateService.fetchAllMostRecentlyCreated([pidm], [nameTypeCode])[0]
    }


    private PersonIdentificationNameAlternate createPersonIdentificationNameAlternateByNameType(PersonIdentificationNameCurrent currentPerson,
                                                                                                Map nameRecord, String dataOrigin) {
        PersonIdentificationNameAlternate personIdentificationNameAlternate
        NameType nameTypeObj = NameType.findByCode(nameRecord.type)

        PersonIdentificationNameAlternate newPersonIdentificationNameAlternate = new PersonIdentificationNameAlternate(
                pidm: currentPerson.pidm,
                bannerId: currentPerson.bannerId,
                lastName: nameRecord.lastName,
                firstName: nameRecord.firstName,
                middleName: nameRecord.middleName,
                surnamePrefix: nameRecord.surnamePrefix,
                changeIndicator: 'N',
                entityIndicator: 'P',
                nameType: nameTypeObj,
                dataOrigin: dataOrigin
        )
        personIdentificationNameAlternate = personIdentificationNameAlternateService.create(newPersonIdentificationNameAlternate)

        return personIdentificationNameAlternate
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

    protected def createPersonDataModel(PersonIdentificationNameCurrent personIdentificationNameCurrent,
                                        def dataMapForPerson) {
        dataMapForPerson.put('personIdentificationNameCurrent', personIdentificationNameCurrent)
        createPersonDataModel(dataMapForPerson)
    }


    protected def createPersonDataMapForSinglePerson(PersonIdentificationNameCurrent personIdentificationNameCurrent,
                                                     PersonBasicPersonBase personBase, String personGuid) {
        def dataMapForPerson = [:]
        dataMapForPerson.put('personGuid', personGuid)
        if (personIdentificationNameCurrent) {

            def bannerNameTypeToHedmNameTypeMap = getBannerNameTypeToHedmNameTypeMap()
            def nameTypeCodeToGuidMap = personNameTypeCompositeService.getNameTypeCodeToGuidMap(bannerNameTypeToHedmNameTypeMap.keySet())
            List<PersonIdentificationNameAlternate> personAlternateNames = personIdentificationNameAlternateService.fetchAllMostRecentlyCreated([personIdentificationNameCurrent?.pidm], bannerNameTypeToHedmNameTypeMap?.keySet()?.toList())
            dataMapForPerson.put('personIdentificationNameCurrent', personIdentificationNameCurrent)
            dataMapForPerson.put('personBase', personBase)
            dataMapForPerson.put('bannerNameTypeToHedmNameTypeMap', bannerNameTypeToHedmNameTypeMap)
            dataMapForPerson.put('nameTypeCodeToGuidMap', nameTypeCodeToGuidMap)
            dataMapForPerson.put('personAlternateNames', personAlternateNames)

        }
        return dataMapForPerson
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
        fetchPersonsRoleDataAndPutInMap(pidms, dataMap)
        personCredentialCompositeService.fetchPersonsCredentialDataAndPutInMap(pidms, dataMap)
        fetchPersonsAddressDataAndPutInMap(pidms, dataMap)
        fetchPersonsPhoneDataAndPutInMap(pidms, dataMap)
        fetchPersonsEmailDataAndPutInMap(pidms, dataMap)
        fetchPersonsRaceDataAndPutInMap(pidms, dataMap)

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

        // roles
        def personRoles = []
        if (dataMap.pidmToRolesMap.containsKey(personIdentificationNameCurrent.pidm)) {
            personRoles = dataMap.pidmToRolesMap.get(personIdentificationNameCurrent.pidm)
        }
        dataMapForPerson << ["personRoles": personRoles]

        // credentials
        def personCredentials = []
        if (dataMap.pidmToCredentialsMap.containsKey(personIdentificationNameCurrent.pidm)) {
            personCredentials = dataMap.pidmToCredentialsMap.get(personIdentificationNameCurrent.pidm)
        }
        personCredentials << [type: CredentialType.BANNER_ID, value: personIdentificationNameCurrent.bannerId]
        dataMapForPerson << ["personCredentials": personCredentials]

        // addresses
        List<PersonAddress> personAddresses = dataMap.pidmToAddressesMap.get(personIdentificationNameCurrent.pidm)
        if (personAddresses) {
            dataMapForPerson << ["personAddresses": personAddresses]
            dataMapForPerson << ["bannerAddressTypeToHedmAddressTypeMap": dataMap.bannerAddressTypeToHedmAddressTypeMap]
        }

        // phones
        List<PersonTelephone> personTelephoneList = dataMap.pidmToPhonesMap.get(personIdentificationNameCurrent.pidm)
        if (personTelephoneList) {
            dataMapForPerson << ["personPhones": personTelephoneList]
            dataMapForPerson << ["bannerPhoneTypeToHedmPhoneTypeMap": dataMap.bannerPhoneTypeToHedmPhoneTypeMap]
        }

        // emails
        List<PersonEmail> personEmailList = dataMap.pidmToEmailsMap.get(personIdentificationNameCurrent.pidm)
        if (personEmailList) {
            dataMapForPerson << ["personEmails": personEmailList]
            dataMapForPerson << ["bannerEmailTypeToHedmEmailTypeMap": dataMap.bannerEmailTypeToHedmEmailTypeMap]
        }

        // races
        List<PersonRace> personRaces = dataMap.pidmToRacesMap.get(personIdentificationNameCurrent.pidm)
        if (personRaces) {
            dataMapForPerson << ["personRaces": personRaces]
            dataMapForPerson << ["raceCodeToGuidMap": dataMap.raceCodeToGuidMap]
        }

        prepareDataMapForSinglePerson_VersionSpecific(personIdentificationNameCurrent, dataMap, dataMapForPerson)

        return dataMapForPerson
    }


    private void fetchPersonsBiographicalDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        // Get SPBPERS records for persons
        def pidmToPersonBaseMap = getPidmToPersonBaseMap(pidms)

        // Get GUIDs for US ethnic codes (SPBPERS_ETHN_CDE)
        Map<String, String> usEthnicCodeToGuidMap = ethnicityCompositeService.fetchGUIDsForUnitedStatesEthnicCodes()

        // Put in Map
        dataMap.put("pidmToPersonBaseMap", pidmToPersonBaseMap)
        dataMap.put("usEthnicCodeToGuidMap", usEthnicCodeToGuidMap)
    }


    private void fetchPersonsAlternateNameDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        def bannerNameTypeToHedmNameTypeMap = getBannerNameTypeToHedmNameTypeMap()
        log.debug "Banner NameType to HEDM NameType mapping = ${bannerNameTypeToHedmNameTypeMap}"

        def pidmToAlternateNamesMap = getPidmToAlternateNamesMap(pidms, bannerNameTypeToHedmNameTypeMap.keySet().toList())

        // Put in Map
        dataMap.put("bannerNameTypeToHedmNameTypeMap", bannerNameTypeToHedmNameTypeMap)
        dataMap.put("pidmToAlternateNamesMap", pidmToAlternateNamesMap)
    }


    private void fetchPersonsRoleDataAndPutInMap(List<Integer> pidms, Map dataMap) {
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


    private void fetchPersonsAddressDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        Map<String, String> bannerAddressTypeToHedmAddressTypeMap = getBannerAddressTypeToHedmAddressTypeMap()

        Map pidmToAddressesMap = getPidmToAddressesMap(pidms, bannerAddressTypeToHedmAddressTypeMap.keySet())

        // Put in Map
        dataMap.put("bannerAddressTypeToHedmAddressTypeMap", bannerAddressTypeToHedmAddressTypeMap)
        dataMap.put("pidmToAddressesMap", pidmToAddressesMap)
    }


    private void fetchPersonsPhoneDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        Map<String, String> bannerPhoneTypeToHedmPhoneTypeMap = getBannerPhoneTypeToHedmPhoneTypeMap()

        Map pidmToPhonesMap = getPidmToPhonesMap(pidms, bannerPhoneTypeToHedmPhoneTypeMap.keySet())

        // Put in Map
        dataMap.put("bannerPhoneTypeToHedmPhoneTypeMap", bannerPhoneTypeToHedmPhoneTypeMap)
        dataMap.put("pidmToPhonesMap", pidmToPhonesMap)
    }


    private void fetchPersonsEmailDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        Map<String, String> bannerEmailTypeToHedmEmailTypeMap = getBannerEmailTypeToHedmEmailTypeMap()

        Map pidmToEmailsMap = getPidmToEmailsMap(pidms, bannerEmailTypeToHedmEmailTypeMap.keySet())

        // Put in Map
        dataMap.put("bannerEmailTypeToHedmEmailTypeMap", bannerEmailTypeToHedmEmailTypeMap)
        dataMap.put("pidmToEmailsMap", pidmToEmailsMap)
    }


    private void fetchPersonsRaceDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        // Get GORPRAC records for persons
        Map pidmToRacesMap = getPidmToRacesMap(pidms)

        // Get GUID for each race
        Set<String> raceCodes = pidmToRacesMap?.values().race.flatten() as Set
        Map raceCodeToGuidMap = raceCompositeService.getRaceCodeToGuidMap(raceCodes)

        // Put in Map
        dataMap.put("pidmToRacesMap", pidmToRacesMap)
        dataMap.put("raceCodeToGuidMap", raceCodeToGuidMap)
    }


    private def getPidmToGuidMap(def rows) {
        Map<Integer, String> pidmToGuidMap = [:]
        rows?.each {
            pidmToGuidMap.put(it.personIdentificationNameCurrent.pidm, it.globalUniqueIdentifier.guid)
        }
        return pidmToGuidMap
    }


    private def getPidmToPersonBaseMap(List<Integer> pidms) {
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


    private def getPidmToAlternateNamesMap(List<Integer> pidms, List<String> nameTypeCodes) {
        List<PersonIdentificationNameAlternate> entities = personIdentificationNameAlternateService.fetchAllMostRecentlyCreated(pidms, nameTypeCodes)
        log.debug "Got ${entities?.size() ?: 0} SV_SPRIDEN_ALT records"
        def pidmToAlternateNamesMap = [:]
        entities.each {
            List<PersonIdentificationNameAlternate> personAlternateNames = []
            if (pidmToAlternateNamesMap.containsKey(it.pidm)) {
                personAlternateNames = pidmToAlternateNamesMap.get(it.pidm)
            } else {
                pidmToAlternateNamesMap.put(it.pidm, personAlternateNames)
            }
            personAlternateNames.add(it)
        }
        return pidmToAlternateNamesMap
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


    private def getPidmToEmployeeRoleMap(List<Integer> pidms) {
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


    private def getPidmToAlumniRoleMap(List<Integer> pidms) {
        def pidmToAlumniRoleMap = [:]
        List<Object[]> alumniList = userRoleCompositeService.fetchAlumnisByPIDMs(pidms)
        alumniList?.each {
            BigDecimal bdPidm = it[0]
            Timestamp startDate = it[1]
            pidmToAlumniRoleMap.put(bdPidm.toInteger(), [role: RoleName.ALUMNI, startDate: startDate])
        }
        return pidmToAlumniRoleMap
    }


    private def getPidmToVendorRoleMap(List<Integer> pidms) {
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


    private def getPidmToProspectiveStudentRoleMap(List<Integer> pidms) {
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


    private def getPidmToAdvisorRoleMap(List<Integer> pidms) {
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


    private def getPidmToAddressesMap(Collection<Integer> pidms, Collection<String> addressTypeCodes) {
        def pidmToAddressesMap = [:]
        if (pidms && addressTypeCodes) {
            log.debug "Getting SV_SPRADDR records for ${pidms?.size()} PIDMs..."
            List<PersonAddress> entities = personAddressService.fetchAllByActiveStatusPidmsAndAddressTypes(pidms, addressTypeCodes)
            log.debug "Got ${entities?.size()} SV_SPRADDR records"
            entities?.each {
                List<PersonAddress> personAddresses = []
                if (pidmToAddressesMap.containsKey(it.pidm)) {
                    personAddresses = pidmToAddressesMap.get(it.pidm)
                } else {
                    pidmToAddressesMap.put(it.pidm, personAddresses)
                }
                personAddresses.add(it)
            }
        }
        return pidmToAddressesMap
    }


    private def getPidmToPhonesMap(Collection<Integer> pidms, Collection<String> phoneTypeCodes) {
        def pidmToPhonesMap = [:]
        if (pidms && phoneTypeCodes) {
            log.debug "Getting SPRTELE records for ${pidms?.size()} PIDMs..."
            List<PersonTelephone> entities = personTelephoneService.fetchAllActiveByPidmInListAndTelephoneTypeCodeInList(pidms, phoneTypeCodes)
            log.debug "Got ${entities?.size()} SPRTELE records"
            entities?.each {
                List<PersonTelephone> personTelephones = []
                if (pidmToPhonesMap.containsKey(it.pidm)) {
                    personTelephones = pidmToPhonesMap.get(it.pidm)
                } else {
                    pidmToPhonesMap.put(it.pidm, personTelephones)
                }
                personTelephones.add(it)
            }
        }
        return pidmToPhonesMap
    }


    private def getPidmToEmailsMap(Collection<Integer> pidms, Collection<String> emailTypeCodes) {
        def pidmToEmailsMap = [:]
        if (pidms && emailTypeCodes) {
            log.debug "Getting GOREMAL records for ${pidms?.size()} PIDMs..."
            List<PersonEmail> entities = personEmailService.fetchAllActiveEmails(pidms, emailTypeCodes)
            log.debug "Got ${entities?.size()} GOREMAL records"
            entities?.each {
                List<PersonEmail> personEmails = []
                if (pidmToEmailsMap.containsKey(it.pidm)) {
                    personEmails = pidmToEmailsMap.get(it.pidm)
                } else {
                    pidmToEmailsMap.put(it.pidm, personEmails)
                }
                personEmails.add(it)
            }
        }
        return pidmToEmailsMap
    }


    private def getPidmToRacesMap(Collection<Integer> pidms) {
        def pidmToRacesMap = [:]
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
        return pidmToRacesMap
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

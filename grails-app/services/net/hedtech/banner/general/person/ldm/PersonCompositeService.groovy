/*******************************************************************************
 Copyright 2014-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.overall.*
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.person.*
import net.hedtech.banner.general.person.ldm.v1.*
import net.hedtech.banner.general.system.*
import net.hedtech.banner.general.system.ldm.MaritalStatusV1CompositeService
import net.hedtech.banner.general.system.ldm.v1.MaritalStatusDetail
import net.hedtech.banner.general.system.ldm.v1.Metadata
import net.hedtech.banner.general.system.ldm.v1.RaceDetail
import net.hedtech.banner.query.DynamicFinder
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Transactional
class PersonCompositeService extends LdmService {

    static final String ldmName = 'persons'
    static final String PROCESS_CODE = "HEDM"
    static final String PERSON_EMAIL_TYPE = "PERSON.EMAILS.EMAILTYPE"
    static final String PERSON_MATCH_RULE = "PERSON.MATCHRULE"

    private static final List<String> VERSIONS = ["v1", "v2", "v3"]
    private static final int DEFAULT_PAGE_SIZE = 500
    private static final int MAX_PAGE_SIZE = 500
    private static final String PERSON_ADDRESS_TYPE = "PERSON.ADDRESSES.ADDRESSTYPE"
    private static final String PERSON_REGION = "PERSON.ADDRESSES.REGION"
    private static final String PERSON_POSTAL_CODE = "PERSON.ADDRESSES.POSTAL.CODE"
    private static final String PERSON_PHONE_TYPE = "PERSON.PHONES.PHONETYPE"
    private static final String PERSON_NAME_TYPE = "PERSON.NAMES.NAMETYPE"
    private static final String DOMAIN_KEY_DELIMITER = '-^'
    private static final String PERSON_EMAILS_LDM_NAME = "person-emails"
    private static final String PERSON_EMAIL_TYPE_PREFERRED = "Preferred"

    PersonV3CompositeService personV3CompositeService
    def personIdentificationNameCurrentService
    def personBasicPersonBaseService
    def personAddressService
    def personTelephoneService
    def personEmailService
    MaritalStatusV1CompositeService maritalStatusV1CompositeService
    MaritalStatusService maritalStatusService
    def ethnicityCompositeService
    def raceCompositeService
    def personRaceService
    def userRoleCompositeService
    def additionalIDService
    def personFilterCompositeService
    def personIdentificationNameAlternateService
    IntegrationConfigurationService integrationConfigurationService

    List<GlobalUniqueIdentifier> allEthnicities

    /**
     * GET /api/persons/<guid>
     *
     * @param id GUID
     * @return
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def get(String id) {
        def row = personIdentificationNameCurrentService.fetchByGuid(id)
        if (!row) {
            throw new ApplicationException("Person", new NotFoundException())
        }
        def resultList = buildLdmPersonObjects([row.personIdentificationNameCurrent])
        return resultList.get(row.personIdentificationNameCurrent.pidm)
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def list(params) {
        log.trace "list:Begin"
        log.debug "Request parameters: ${params}"
        def total = 0
        def resultList = [:]
        def allowedSortFields = ["firstName", "lastName"]
        Boolean studentRole = false
        def personList = []
        def searchResult

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

        if (RestfulApiValidationUtility.isQApiRequest(params)) {

            def requestProcessingResult = personV3CompositeService.listQApi(params)
            total = requestProcessingResult.totalCount
            // To make this class, to create decorators
            List<Integer> pidms = requestProcessingResult.get("pidms")
            pidms.each {
                personList << [pidm: it]
            }

        } else {

            if ("v3".equalsIgnoreCase(getAcceptVersion(VERSIONS))) {

                def requestProcessingResult = personV3CompositeService.processListApiRequest(params)
                total = requestProcessingResult.totalCount
                // To make this class, to create decorators
                List<Integer> pidms = requestProcessingResult.get("pidms")
                pidms.each {
                    personList << [pidm: it]
                }

            } else {
                // V1 and V2

                if (params.containsKey("personFilter") && params.containsKey("role")) {
                    throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("UnsupportedFilterCombination", []))
                }

                if (params.containsKey("personFilter")) {
                    String selId = params.get("personFilter")
                    searchResult = getPidmsForPersonFilter(selId, params)
                    personList = searchResult.personList
                    total = searchResult.count
                } else if (params.credentialId && (CredentialType.BANNER_ID.versionToEnumMap["v3"].equals(params.credentialType)) && !GeneralCommonConstants.VERSION_V1.equalsIgnoreCase(getAcceptVersion(VERSIONS))) {
                    String credential = params.credentialId
                    def pidmsMap = [:]
                    boolean isCorrectRole = false
                    PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.fetchByBannerId(credential)
                    pidmsMap.put('pidm', personIdentificationNameCurrent?.pidm)
                    String role = params.role?.trim()?.toLowerCase()
                    if (role && role == 'student') {
                        studentRole = true
                    } else if (role && role == 'faculty') {
                        studentRole = false
                    } else {
                        isCorrectRole = true
                    }
                    if (params.role && personIdentificationNameCurrent) {
                        Map personMap = userRoleCompositeService.fetchAllRolesByPidmInList([personIdentificationNameCurrent?.pidm], studentRole)
                        List<RoleDetail> roles = personMap.get(personIdentificationNameCurrent?.pidm)
                        for (RoleDetail roleDetail : roles) {
                            if (roleDetail?.role.equalsIgnoreCase(params.role)) {
                                isCorrectRole = true
                                break
                            }
                        }
                    }
                    if (isCorrectRole) {
                        def query = """from PersonIdentificationNameCurrent a
                                                      where a.pidm = (:pidm)
                                                      order by a.$params.sort $params.order, a.bannerId $params.order
                                                   """
                        DynamicFinder dynamicFinder = new DynamicFinder(PersonIdentificationNameCurrent.class, query, "a")
                        log.debug "PersonIdentificationNameCurrent query begins"
                        personList = dynamicFinder.find([params: pidmsMap, criteria: []], [:])
                        total = personList.size()
                    }
                } else {
                    if (params.role) {
                        String role = params.role?.trim()?.toLowerCase()
                        if (role == "faculty" || role == "student") {
                            if (role == "student") {
                                studentRole = true
                            }
                            log.debug "fetchAllByRole $params"
                            searchResult = userRoleCompositeService.fetchAllByRole(params)
                            log.debug "fetchAllByRole returns ${searchResult?.pidms?.size()}"
                            def pidms = searchResult.pidms
                            total = searchResult.count?.longValue()
                            if (pidms?.size() > 0) {
                                def pidmsObject = []
                                pidms.each {
                                    pidmsObject << [data: it]
                                }
                                Map pidmsMap = [pidms: pidmsObject]
                                def query = """from PersonIdentificationNameCurrent a
                                                      where a.pidm in (:pidms)
                                                      order by a.$params.sort $params.order, a.bannerId $params.order
                                                   """
                                DynamicFinder dynamicFinder = new DynamicFinder(PersonIdentificationNameCurrent.class, query, "a")
                                log.debug "PersonIdentificationNameCurrent query begins"
                                personList = dynamicFinder.find([params: pidmsMap, criteria: []], [:])
                                log.debug "PersonIdentificationNameCurrent returns ${personList?.size()}"
                            }
                        } else {
                            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("role.supported", []))
                        }
                    } else {
                        throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("role.required", []))
                    }
                }
            }

        }

        if (personList?.size() > 0) {
            log.debug "buildLdmPersonObjects begins"
            resultList = buildLdmPersonObjects(personList, studentRole)
            log.debug "buildLdmPersonObjects ends"
        }

        try {
            // Avoid restful-api plugin dependencies.
            resultList = this.class.classLoader.loadClass('net.hedtech.restfulapi.PagedResultArrayList').newInstance(resultList?.values() ?: [], total)
        } catch (ClassNotFoundException e) {
            resultList = resultList.values()
        }

        log.trace "list:End"
        resultList
    }


    def create(Map person) {
        Map<Integer, Person> persons = [:]
        def newPersonIdentification
        PersonIdentificationNameCurrent newPersonIdentificationName
        PersonIdentificationNameAlternate personIdentificationNameAlternate
        Map metadata = person.metadata
        if (person.names instanceof List) {
            def primaryName = person.names.find { it.nameType == "Primary" && it.firstName && it.lastName }
            if (primaryName) {
                newPersonIdentification = primaryName
                newPersonIdentification.put('bannerId', 'GENERATED')
                newPersonIdentification.put('entityIndicator', 'P')
                newPersonIdentification.put('changeIndicator', null)
                newPersonIdentification.put('dataOrigin', metadata?.dataOrigin)
                newPersonIdentification.remove('nameType') // ID won't generate if this is set.
                //Create the new PersonIdentification record
                newPersonIdentificationName = personIdentificationNameCurrentService.create(newPersonIdentification)
            } else {
                throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("name.required.message", []))
            }
            if ("v3".equals(getAcceptVersion(VERSIONS))) {
                def birthName = person.names.find { it.nameType == "Birth" && it.firstName && it.lastName }
                if (birthName) {
                    personIdentificationNameAlternate = createPersonIdentificationNameAlternateByNameType(newPersonIdentificationName, birthName, metadata)
                }
            }
        } else {
            throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("names.required.message", []))
        }

        //Fix the GUID if provided as DB will assign one
        if (person.guid) {
            updateGuidValue(newPersonIdentificationName.id, person.guid, ldmName)

        } else {
            def entity = GlobalUniqueIdentifier.findByLdmNameAndDomainId(ldmName, newPersonIdentificationName.id)
            person.put('guid', entity?.guid)
        }

        def additionalIds = []
        if (person?.credentials instanceof List) {
            person?.credentials?.each { it ->
                if (it instanceof Map) {
                    def allowedCredentialTypes = ["Social Security Number", "Social Insurance Number", "Banner ID", Credential.additionalIdMap.ELV8]
                    if (["v2", "v3"].contains(getAcceptVersion(VERSIONS))) {
                        allowedCredentialTypes = ["Social Security Number", "Social Insurance Number", "Banner ID", Credential.additionalIdMap.ELV8, "Banner Sourced ID", "Banner User Name", "Banner UDC ID"]
                    }
                    validateCredentialType(it.credentialType, allowedCredentialTypes, it.credentialId)
                    person = createSSN(it.credentialType, it.credentialId, person)
                    if (it.credentialType && Credential.additionalIdMap.containsValue(it.credentialType)) {
                        additionalIds << createOrUpdateAdditionalId(newPersonIdentificationName, it, metadata)
                    }
                }
            }
        }
        //Copy personBase attributes into person map from Primary names object.
        person.put('dataOrigin', metadata?.dataOrigin)
        person.put('namePrefix', newPersonIdentification.get('namePrefix'))
        person.put('nameSuffix', newPersonIdentification.get('nameSuffix'))
        person.put('preferenceFirstName', newPersonIdentification.get('preferenceFirstName'))
        //Translate enumerations and defaults
        person.put('sex', person?.sex == 'Male' ? 'M' : (person?.sex == 'Female' ? 'F' : (person?.sex == 'Unknown' ? 'N' : null)))

        MaritalStatusDetail maritalStatusDetail
        if (person.maritalStatusDetail instanceof Map) {
            String maritalStatusGuid = person.maritalStatusDetail.guid?.trim()?.toLowerCase()
            if (!maritalStatusGuid) {
                throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("marital.status.guid.required.message", []))
            }
            try {
                maritalStatusDetail = maritalStatusV1CompositeService.get(maritalStatusGuid)
            } catch (ApplicationException ae) {
                LdmService.throwBusinessLogicValidationException(ae)
            }
            person.put('maritalStatus', maritalStatusDetail.maritalStatus)
        }

        def ethnicityDetail
        if (person.ethnicityDetail instanceof Map) {
            ethnicityDetail = createOrUpdatePersonEthnicity(person)
        }

        person.put('deadIndicator', person.get('deadDate') ? 'Y' : null)
        person.put('pidm', newPersonIdentificationName?.pidm)
        person.put('armedServiceMedalVetIndicator', false)
        PersonBasicPersonBase newPersonBase = personBasicPersonBaseService.create(person)
        def currentRecord = new Person(newPersonBase)
        currentRecord.guid = person.guid
        currentRecord.maritalStatusDetail = maritalStatusDetail
        currentRecord.ethnicityDetail = ethnicityDetail
        def name = new Name(newPersonIdentificationName, newPersonBase)
        name.setNameType("Primary")
        currentRecord.names << name
        if ("v3".equals(getAcceptVersion(VERSIONS))) {
            if (personIdentificationNameAlternate) {
                def birth = new NameAlternate(personIdentificationNameAlternate)
                birth.setNameType("Birth")
                currentRecord.names << birth
            }
        }
        //Store the credential we already have
        currentRecord.credentials = []
        currentRecord.credentials << new Credential("Banner ID", newPersonIdentificationName.bannerId, null, null)
        if (newPersonBase.ssn) {
            currentRecord.credentials << new Credential("Social Security Number",
                    newPersonBase.ssn,
                    null,
                    null)
        }

        persons.put(newPersonIdentificationName.pidm, currentRecord)
        def addresses = createAddresses(newPersonIdentificationName.pidm, metadata,
                person.addresses instanceof List ? person.addresses : [])
        persons = buildPersonAddresses(addresses, persons)
        List<PersonTelephone> phones = updatePhones(newPersonIdentificationName.pidm, metadata,
                person.phones instanceof List ? person.phones : [], null)
        persons = buildPersonTelephones(phones, persons)
        def emails = updatePersonEmails(newPersonIdentificationName.pidm, metadata,
                person.emails instanceof List ? person.emails : [], [])
        persons = buildPersonEmails(emails, persons)
        def races = createRaces(newPersonIdentificationName.pidm, metadata,
                person.races instanceof List ? person.races : [])
        persons = buildPersonRaces(races, persons)
        persons = buildPersonRoles(persons)
        persons = buildPersonAdditionalIds(additionalIds, persons)
        persons.get(newPersonIdentificationName.pidm)
    }

    /**
     * Updates the Person Information like PersonIdentificationNameCurrent, PersonBasicPersonBase, Address
     * Telephones and Emails
     * @param person - Map containing the changes person details
     * @return person
     */
    def update(Map person) {
        String personGuid = person?.id?.trim()?.toLowerCase()
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndGuid(ldmName, personGuid)

        if (personGuid) {
            if (!globalUniqueIdentifier) {
                if (!person.get('guid'))
                    person.put('guid', personGuid)
                //Per strategy when a GUID was provided, the create should happen.
                return create(person)
            }
        } else {
            throw new ApplicationException("Person", new NotFoundException())
        }

        def namesElementPrimary
        def namesElementBirth
        if (person.names instanceof List) {
            namesElementPrimary = person.names.find { it.nameType == "Primary" }
            if ("v3".equals(getAcceptVersion(VERSIONS))) {
                namesElementBirth = person.names.find { it.nameType == "Birth" && it.firstName && it.lastName }
            }
        }


        def pidmToUpdate = globalUniqueIdentifier.domainKey?.toInteger()
        List<PersonIdentificationNameCurrent> personIdentificationList = PersonIdentificationNameCurrent.findAllByPidmInList([pidmToUpdate])

        PersonIdentificationNameCurrent personIdentification
        personIdentificationList.each { identification ->
            if (identification.changeIndicator == null) {
                personIdentification = identification
            }
        }
        //update PersonIdentificationNameCurrent
        PersonIdentificationNameCurrent newPersonIdentificationName
        PersonIdentificationNameCurrent oldPersonIdentificationName = new PersonIdentificationNameCurrent(personIdentification.properties)
        if (namesElementPrimary) {
            if (namesElementPrimary.containsKey('firstName')) personIdentification.firstName = namesElementPrimary.firstName
            if (namesElementPrimary.containsKey('lastName')) personIdentification.lastName = namesElementPrimary.lastName
            if (namesElementPrimary.containsKey('middleName')) personIdentification.middleName = namesElementPrimary.middleName
            if (namesElementPrimary.containsKey('surnamePrefix')) personIdentification.surnamePrefix = namesElementPrimary.surnamePrefix
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
        if (person?.credentials instanceof List) {
            person?.credentials?.each { it ->
                if (it instanceof Map) {
                    def allowedCredentialTypes = ["Social Security Number", "Social Insurance Number", "Banner ID", Credential.additionalIdMap.ELV8]
                    if (["v2", "v3"].contains(getAcceptVersion(VERSIONS))) {
                        allowedCredentialTypes = ["Social Security Number", "Social Insurance Number", "Banner ID", Credential.additionalIdMap.ELV8, "Banner Sourced ID", "Banner User Name", "Banner UDC ID"]
                    }
                    validateCredentialType(it.credentialType, allowedCredentialTypes, it.credentialId)

                    if (it.credentialType == 'Banner ID') {
                        personIdentification.bannerId = it?.credentialId
                        newPersonIdentificationName = personIdentificationNameCurrentService.update(personIdentification)
                    } else if (it.credentialType == 'Elevate ID') {
                        createOrUpdateAdditionalId(personIdentification, it, person?.metadata)
                    }
                }
            }
        }

        if (!newPersonIdentificationName)
            newPersonIdentificationName = personIdentification
        //update PersonBasicPersonBase
        PersonBasicPersonBase newPersonBase = updatePersonBasicPersonBase(pidmToUpdate, newPersonIdentificationName, person, namesElementPrimary)
        def credentials = []
        if (newPersonBase && newPersonBase.ssn) {
            credentials << new Credential("Social Security Number",
                    newPersonBase.ssn,
                    null,
                    null)
        }

        if (newPersonIdentificationName && newPersonIdentificationName.bannerId) {
            credentials << new Credential("Banner ID",
                    newPersonIdentificationName.bannerId,
                    null,
                    null)
        }
        def names = []
        Name namePrimary = new Name(newPersonIdentificationName, newPersonBase)
        namePrimary.setNameType("Primary")
        names << namePrimary
        if ("v3".equals(getAcceptVersion(VERSIONS))) {
            PersonIdentificationNameAlternate existingPersonBirthRecord = getPersonIdentificationNameAlternateByNameType(newPersonIdentificationName?.pidm)
            PersonIdentificationNameAlternate newPersonBirthRecord = null
            if (namesElementBirth) {
                if (!isNamesElementBirthIsSameAsExisting(namesElementBirth, existingPersonBirthRecord)) {
                    newPersonBirthRecord = createPersonIdentificationNameAlternateByNameType(newPersonIdentificationName, namesElementBirth, person?.metadata)
                }
            }
            NameAlternate personBirthRecord
            if (newPersonBirthRecord) {
                personBirthRecord = new NameAlternate(newPersonBirthRecord)
                personBirthRecord.setNameType("Birth")
                names << personBirthRecord
            } else if (existingPersonBirthRecord) {
                personBirthRecord = new NameAlternate(existingPersonBirthRecord)
                personBirthRecord.setNameType("Birth")
                names << personBirthRecord
            }
        }

        def ethnicityDetail = buildPersonEthnicity(newPersonBase)

        def maritalStatusDetail
        if (newPersonBase.maritalStatus) {
            String maritalStatusGuid = maritalStatusService.fetchAllWithGuidByCodeInList([newPersonBase.maritalStatus?.code])[0].globalUniqueIdentifier.guid
            maritalStatusDetail = maritalStatusV1CompositeService.createMaritalStatusDataModel(maritalStatusGuid, newPersonBase.maritalStatus, maritalStatusV1CompositeService.getBannerMaritalStatusCodeToHedmMaritalStatusCategoryMap())
        }

        //update Address
        def addresses = []

        if (person.containsKey('addresses') && person.addresses instanceof List)
            addresses = updateAddresses(pidmToUpdate, person.metadata, person.addresses)

        def phones = []

        def emails = []

        //update races
        def races = []
        if (person.containsKey('races') && person.races instanceof List)
            races = updateRaces(pidmToUpdate, person.metadata, person.races)

        //Build decorator to return LDM response.
        def personDecorator = new Person(newPersonBase, personGuid, credentials, addresses, phones, emails, names, maritalStatusDetail, ethnicityDetail, races, [])
        Map personMap = [:]
        personMap.put(pidmToUpdate, personDecorator)
        if (addresses.size() == 0)
            personMap = buildPersonAddresses(PersonAddress.fetchActiveAddressesByPidmInList([pidmToUpdate]), personMap)

        //update Telephones
        if (person.containsKey('phones') && person.phones instanceof List) {
            ArrayList<PersonTelephone> existingPersonTelephones = getPersonTelephonesFromDB(pidmToUpdate)
            List<PersonTelephone> lstPersonTelephone = updatePhones(pidmToUpdate, person.metadata, person.phones, existingPersonTelephones)
            personMap = buildPersonTelephones(lstPersonTelephone, personMap)
        } else {
            personMap = buildPersonTelephones(getPersonTelephonesFromDB(pidmToUpdate), personMap)
        }

        //update Emails
        if (person.containsKey('emails') && person.emails instanceof List) {
            emails = updatePersonEmails(pidmToUpdate, person.metadata, person.emails, getPersonEmailsFromDB(pidmToUpdate))
            buildPersonEmails(emails, personMap)
        } else {
            buildPersonEmails(getPersonEmailsFromDB(pidmToUpdate), personMap)
        }

        if (races.size() == 0)
            personMap = buildPersonRaces(PersonRace.findAllByPidmInList([pidmToUpdate]), personMap)
        def additionalIdTypes = Credential.additionalIdMap.keySet().asList()
        personMap = buildPersonAdditionalIds(additionalIDService.fetchAllByPidmInListAndIdentificationTypeCodeInList([pidmToUpdate], additionalIdTypes), personMap)
        personDecorator = buildPersonRoles(personMap).get(pidmToUpdate)
    }


    private ArrayList<PersonTelephone> getPersonTelephonesFromDB(Integer pidmToUpdate) {
        List<IntegrationConfiguration> intConfs = findAllByProcessCodeAndSettingName(PROCESS_CODE, PERSON_PHONE_TYPE)
        def banTypeToHEDMTypeMap = [:]
        intConfs?.each {
            banTypeToHEDMTypeMap.put(it.value, it.translationValue)
        }
        List<PersonTelephone> existingPersonTelephones = PersonTelephone.fetchActiveTelephoneByPidmInList([pidmToUpdate]).findAll {
            banTypeToHEDMTypeMap.containsKey(it.telephoneType.code)
        }
        existingPersonTelephones
    }


    public Integer getPidm(String guid) {
        def entity = GlobalUniqueIdentifier.fetchByLdmNameAndGuid(ldmName, guid?.toLowerCase())
        if (!entity) {
            throw new ApplicationException("Person", new NotFoundException())
        }
        return entity.domainKey?.toInteger()
    }


    private PersonBasicPersonBase createPersonBasicPersonBase(person, newPersonIdentificationName, newPersonIdentification) {
        PersonBasicPersonBase newPersonBase
        if (person.guid) {
            updateGuidValue(newPersonIdentificationName.id, person.guid, ldmName)
        } else {
            def entity = GlobalUniqueIdentifier.fetchByLdmNameAndDomainId(ldmName, newPersonIdentificationName.id)
            person.put('guid', entity)
        }
        if (person?.credentials instanceof List) {
            person?.credentials?.each { it ->
                if (it instanceof Map) {
                    person = createSSN(it.credentialType, it.credentialId, person)
                }
            }
        }

        //Copy personBase attributes into person map from Primary names object.
        person.put('dataOrigin', person?.metadata?.dataOrigin)
        if (newPersonIdentification) {
            if (newPersonIdentification.containsKey('namePrefix')) {
                person.put('namePrefix', newPersonIdentification.get('namePrefix'))
            }
            if (newPersonIdentification.containsKey('nameSuffix')) {
                person.put('nameSuffix', newPersonIdentification.get('nameSuffix'))
            }
            if (newPersonIdentification.containsKey('preferenceFirstName')) {
                person.put('preferenceFirstName', newPersonIdentification.get('preferenceFirstName'))
            }
        }
        //Translate enumerations and defaults
        person.put('sex', person?.sex == 'Male' ? 'M' : (person?.sex == 'Female' ? 'F' : (person?.sex == 'Unknown' ? 'N' : null)))

        MaritalStatusDetail maritalStatusDetail
        if (person.maritalStatusDetail instanceof Map) {
            String maritalStatusGuid = person.maritalStatusDetail.guid?.trim()?.toLowerCase()
            if (!maritalStatusGuid) {
                throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("marital.status.guid.required.message", []))
            }
            try {
                maritalStatusDetail = maritalStatusV1CompositeService.get(maritalStatusGuid)
            } catch (ApplicationException ae) {
                LdmService.throwBusinessLogicValidationException(ae)
            }
            person.put('maritalStatus', maritalStatusDetail.maritalStatus)
        }

        if (person.ethnicityDetail instanceof Map) {
            createOrUpdatePersonEthnicity(person)
        }

        person.put('deadIndicator', person.get('deadDate') ? 'Y' : null)
        person.put('pidm', newPersonIdentificationName?.pidm)
        person.put('armedServiceMedalVetIndicator', false)
        newPersonBase = personBasicPersonBaseService.create(person)
        newPersonBase
    }


    List<PersonAddress> createAddresses(def pidm, Map metadata, List<Map> newAddresses) {
        def addresses = []
        newAddresses?.each { activeAddress ->
            if (activeAddress instanceof Map) {
                IntegrationConfiguration rule = fetchAllByProcessCodeAndSettingNameAndTranslationValue(
                        PROCESS_CODE, PERSON_ADDRESS_TYPE, activeAddress.addressType)
                if (!rule) {
                    throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("goriccr.not.found.message", [PERSON_ADDRESS_TYPE]))
                }
                if (rule.translationValue == activeAddress.addressType && !addresses.contains {
                    it.addressType == rule?.value
                }) {
                    activeAddress.put('addressType', AddressType.findByCode(rule?.value))

                    if (activeAddress?.nation?.containsKey('code')) {
                        if (activeAddress.nation.code) {
                            Nation nation = Nation.findByScodIso(activeAddress?.nation?.code)
                            if (nation) {
                                activeAddress.put('nation', nation)
                            } else {
                                log.error "Nation not found for code: ${activeAddress?.nation?.code}"
                                throw new ApplicationException("Person", new BusinessLogicValidationException("country.not.found.message", []))
                            }
                            InstitutionalDescription institutionalDescription = InstitutionalDescription.fetchByKey()
                            if (institutionalDescription?.natnCode == activeAddress.nation.code) {
                                getStateAndZip(activeAddress, false)
                            } else {
                                getStateAndZip(activeAddress, true)
                            }
                        } else {
                            activeAddress.put('nation', null)
                            getStateAndZip(activeAddress, false)
                        }
                    } else {
                        getStateAndZip(activeAddress, false)
                    }

                    if (activeAddress.containsKey('county')) {
                        if (activeAddress.county) {
                            County country = County.findByDescription(activeAddress.county)
                            if (country) {
                                activeAddress.put('county', country)
                            } else {
                                log.error "County not found for code: ${activeAddress.county}"
                                throw new ApplicationException("Person", new BusinessLogicValidationException("county.not.found.message", []))
                            }
                        } else {
                            activeAddress.put('county', null)
                        }
                    }
                    activeAddress.put('pidm', pidm)
                    activeAddress.put('dataOrigin', metadata?.dataOrigin)
                    activeAddress.put('fromDate', new Date())
                    validateAddressRequiredFields(activeAddress)
                    addresses << personAddressService.create(activeAddress)
                }
            }
        }
        addresses
    }


    List<PersonRace> createRaces(def pidm, Map metadata, List<Map> newRaces) {
        def races = []
        newRaces?.each { activeRace ->
            if (activeRace instanceof Map && activeRace.guid) {
                def race
                try {
                    race = raceCompositeService.get(activeRace.guid.trim()?.toLowerCase())
                }
                catch (ApplicationException ae) {
                    LdmService.throwBusinessLogicValidationException(ae)
                }
                def newRace = new PersonRace()
                newRace.pidm = pidm
                newRace.race = race.race
                newRace.dataOrigin = metadata?.dataOrigin
                PersonRace personRace = PersonRace.fetchByPidmAndRace(pidm, newRace.race)
                if (personRace == null) {
                    races << personRaceService.create(newRace)
                } else {
                    throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException('race.exists', [race.guid]))
                }

            } else {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("race.guid.required.message", []))
            }
        }
        races
    }


    private PersonTelephone parseAndCreatePersonTelephone(Integer pidm, Map metadata, TelephoneType telephoneType, Map requestPhone) {
        validatePhoneRequiredFields(requestPhone)
        def parts = PhoneNumberUtility.parsePhoneNumber(requestPhone.phoneNumber, integrationConfigurationService.getDefaultISO2CountryCodeForPhoneNumberParsing())
        if (parts.size() == 0) {
            // Parsing is not succesful so we go with split
            parts = splitPhoneNumber(requestPhone.phoneNumber)
        }
        def personTelephoneMap = [:]
        personTelephoneMap.put('telephoneType', telephoneType)
        personTelephoneMap.put('pidm', pidm)
        personTelephoneMap.put('dataOrigin', metadata?.dataOrigin)
        personTelephoneMap.put("countryPhone", parts["countryPhone"])
        personTelephoneMap.put("phoneArea", parts["phoneArea"])
        personTelephoneMap.put("phoneNumber", parts["phoneNumber"])
        personTelephoneMap.put("phoneExtension", requestPhone.phoneExtension)
        return createPersonTelephone(personTelephoneMap)
    }


    private TelephoneType getBannerTypeForHEDMPhoneType(String hedmPhoneType) {
        // GORICCR default
        TelephoneType telephoneType
        IntegrationConfiguration intConf = fetchAllByProcessCodeAndSettingNameAndTranslationValue(PROCESS_CODE, PERSON_PHONE_TYPE, hedmPhoneType)
        if (!intConf) {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException('goriccr.not.found.message', [PERSON_PHONE_TYPE]))
        }
        telephoneType = TelephoneType.findByCode(intConf?.value)
        if (!telephoneType) {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException('goriccr.invalid.value.message', [PERSON_PHONE_TYPE]))
        }
        return telephoneType
    }


    private PersonTelephone createPersonTelephone(personTelephoneMap) {
        return new PersonTelephone(
                pidm: personTelephoneMap.pidm,
                phoneArea: personTelephoneMap.phoneArea,
                phoneNumber: personTelephoneMap.phoneNumber,
                phoneExtension: personTelephoneMap.phoneExtension,
                countryPhone: personTelephoneMap.countryPhone,
                telephoneType: personTelephoneMap.telephoneType,
                dataOrigin: personTelephoneMap.dataOrigin
        )
    }


    private def updatePersonEmails(Integer pidm, Map metadata,
                                   def emailsInRequest, List<PersonEmail> existingPersonEmails) {
        List<PersonEmail> personEmails = []

        String preferredEmailType
        String preferredEmailAddress
        // De-activate existing emails
        existingPersonEmails?.each {
            if (it.preferredIndicator) {
                preferredEmailType = it.emailType.code
                preferredEmailAddress = it.emailAddress
            }
            it.statusIndicator = "I"
            it.preferredIndicator = false
            personEmailService.update([domainModel: it])
        }

        List<String> processedEmailTypes = [PERSON_EMAIL_TYPE_PREFERRED]
        PersonEmail personEmail
        emailsInRequest?.each {
            validateEmailRequiredFields(it)
            String emailGuid = it?.guid?.trim()?.toLowerCase()
            String hedmEmailType = it.emailType.trim()
            String emailAddress = it.emailAddress
            log.debug "$emailGuid - $hedmEmailType - $emailAddress"
            if (!processedEmailTypes.contains(hedmEmailType)) {
                EmailType bannerEmailType = getBannerEmailTypeFromHedmEmailType(hedmEmailType)
                if (bannerEmailType) {
                    log.debug "Processing $emailGuid - ${bannerEmailType.code} - $emailAddress ..."

                    PersonEmail existingPersonEmail = existingPersonEmails?.find { existingPersonEmail -> existingPersonEmail.emailType.code == bannerEmailType.code && existingPersonEmail.emailAddress == emailAddress }
                    if (existingPersonEmail) {
                        // Update
                        existingPersonEmail.statusIndicator = "A"
                        personEmail = personEmailService.update([domainModel: existingPersonEmail])
                        existingPersonEmails.remove(existingPersonEmail)
                    } else {
                        // Create
                        personEmail = new PersonEmail(pidm: pidm, emailAddress: emailAddress, statusIndicator: "A", emailType: bannerEmailType, dataOrigin: metadata?.dataOrigin)
                        personEmail = personEmailService.create([domainModel: personEmail])
                    }

                    if (emailGuid) {
                        // Overwrite the GUID created by DB insert trigger, with the one provided in the request body
                        updateGuidValue(personEmail.id, emailGuid, PERSON_EMAILS_LDM_NAME)
                    }
                    String domainKey = "${personEmail.pidm}${DOMAIN_KEY_DELIMITER}${personEmail.emailType.code}${DOMAIN_KEY_DELIMITER}${personEmail.emailAddress}"
                    log.debug("GUID: ${emailGuid}   DomainKey: ${domainKey}")

                    personEmails << personEmail
                }
                processedEmailTypes << hedmEmailType
            }
        }

        markPreferredEmail(personEmails, preferredEmailType, preferredEmailAddress)

        return personEmails
    }


    private def getPersonEmailsFromDB(Integer pidm) {
        log.debug "Fetching emails of person from Database..."
        def bannerEmailTypes = []
        def intConfigs = IntegrationConfiguration.fetchAllByProcessCodeAndSettingName(PROCESS_CODE, PERSON_EMAIL_TYPE)
        intConfigs?.each {
            bannerEmailTypes << it.value
        }
        log.debug "Banner EmailTypes $bannerEmailTypes configured in GORICCR"
        return PersonEmail.fetchListByPidmAndEmailTypes(pidm, bannerEmailTypes)
    }


    private EmailType getBannerEmailTypeFromHedmEmailType(String hedmEmailType) {
        log.debug "Trying to get Banner email type for HEDM email type ${hedmEmailType} ..."
        IntegrationConfiguration intConfig = fetchAllByProcessCodeAndSettingNameAndTranslationValue(PROCESS_CODE, PERSON_EMAIL_TYPE, hedmEmailType)
        if (!intConfig) {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("goriccr.not.found.message", [PERSON_EMAIL_TYPE]))
        }
        log.debug "HEDM email type ${hedmEmailType} -> Banner email type ${intConfig.value}"
        EmailType emailType = EmailType.findByCode(intConfig.value)
        if (!emailType) {
            throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException('goriccr.invalid.value.message', [PERSON_EMAIL_TYPE]))
        }
        return emailType
    }


    private void markPreferredEmail(List<PersonEmail> personEmails, String preferredEmailType, String preferredEmailAddress) {
        if (personEmails) {
            PersonEmail personEmail
            if (preferredEmailType && preferredEmailAddress) {
                log.debug "Finding match using $preferredEmailType and $preferredEmailAddress ..."
                personEmail = personEmails.find {
                    it.emailType.code == preferredEmailType && it.emailAddress == preferredEmailAddress
                }
            }
            if (!personEmail && preferredEmailType) {
                log.debug "Finding match using $preferredEmailType ..."
                personEmail = personEmails.find { it.emailType.code == preferredEmailType }
            }
            if (!personEmail && preferredEmailAddress) {
                log.debug "Finding match using $preferredEmailAddress ..."
                personEmail = personEmails.find { it.emailAddress == preferredEmailAddress }
            }
            if (personEmail) {
                personEmail.preferredIndicator = true
                personEmail = personEmailService.update([domainModel: personEmail])
            }
        }
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildLdmPersonObjects(def personList, Boolean studentRole = false) {
        def persons = [:]
        List pidms = []
        personList?.each {
            pidms << it.pidm
            persons.put(it.pidm, null) //Preserve list order.
        }
        if (pidms.size() < 1) {
            return persons
        }
        List<PersonBasicPersonBase> personBaseList = PersonBasicPersonBase.fetchByPidmList(pidms)
        List<PersonAddress> personAddressList = PersonAddress.fetchActiveAddressesByPidmInList(pidms)
        List<PersonTelephone> personTelephoneList = PersonTelephone.fetchActiveTelephoneByPidmInList(pidms)
        List<PersonEmail> personEmailList = PersonEmail.fetchByPidmsAndActiveStatus(pidms)
        List<PersonRace> personRaceList = PersonRace.fetchByPidmList(pidms)

        Map credentialsMap = [:]
        if (["v2", "v3"].contains(getAcceptVersion(VERSIONS))) {
            credentialsMap = getPersonCredentialDetails(pidms)
        }

        if ("v3".equals(getAcceptVersion(VERSIONS))) {
            allEthnicities = ethnicityCompositeService.getUnitedStatesEthnicCodes()
        }
        personBaseList.each { personBase ->
            Person currentRecord = new Person(personBase)
            if (personBase.maritalStatus) {
                String maritalStatusGuid = maritalStatusService.fetchAllWithGuidByCodeInList([personBase.maritalStatus?.code])[0].globalUniqueIdentifier.guid
                currentRecord.maritalStatusDetail = maritalStatusV1CompositeService.createMaritalStatusDataModel(maritalStatusGuid, personBase.maritalStatus, maritalStatusV1CompositeService.getBannerMaritalStatusCodeToHedmMaritalStatusCategoryMap())
            }
            currentRecord.ethnicityDetail = buildPersonEthnicity(personBase, allEthnicities)

            persons.put(currentRecord.pidm, currentRecord)
        }
        def domainIds = []
        personList.each {
            Person currentRecord = persons.get(it.pidm) ?: new Person(null)
            PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.fetchByPidm(it.pidm)
            if (it instanceof Map && !it.containsKey("bannerId")) {
                it["bannerId"] = personIdentificationNameCurrent.bannerId
            }
            def name = new Name(personIdentificationNameCurrent, currentRecord)
            name.setNameType("Primary")
            currentRecord.names << name
            domainIds << name.personName.id
            currentRecord.metadata = new Metadata(name.personName.dataOrigin)
            persons.put(it.pidm, currentRecord)
        }
        if ("v3".equals(getAcceptVersion(VERSIONS))) {
            NameType nameType = getBannerNameTypeFromHEDMNameType('Birth')
            List<PersonIdentificationNameAlternate> personIdentificationNameAlternateList = personIdentificationNameAlternateService.fetchAllMostRecentlyCreated(pidms, [nameType.code])
            if (personIdentificationNameAlternateList) {
                persons = buildPersonAlternateByNameType(personIdentificationNameAlternateList, persons)
            }
        }
        persons = buildPersonCredentials(credentialsMap, persons, personList)
        persons = buildPersonGuids(domainIds, persons)
        persons = buildPersonAddresses(personAddressList, persons)
        persons = buildPersonTelephones(personTelephoneList, persons)
        persons = buildPersonEmails(personEmailList, persons)
        persons = buildPersonRaces(personRaceList, persons)
        persons = buildPersonRoles(persons, studentRole, pidms)

        persons // Map of person objects with pidm as index.
    }


    private Map getPersonCredentialDetails(List pidms) {
        log.trace "getPersonCredentialDetails:Begin"
        List<ImsSourcedIdBase> imsSourcedIdBaseList = ImsSourcedIdBase.findAllByPidmInList(pidms)
        List<ThirdPartyAccess> thirdPartyAccessList = ThirdPartyAccess.findAllByPidmInList(pidms)
        List<PidmAndUDCIdMapping> pidmAndUDCIdMappingList = PidmAndUDCIdMapping.findAllByPidmInList(pidms)
        def additionalIdTypes = Credential.additionalIdMap.keySet().asList()
       def additionalIdList = additionalIDService.fetchAllByPidmInListAndIdentificationTypeCodeInList(pidms, additionalIdTypes)
        log.trace "getPersonCredentialDetails:End"
        return [imsSourcedIdBaseList: imsSourcedIdBaseList, thirdPartyAccessList: thirdPartyAccessList, pidmAndUDCIdMappingList: pidmAndUDCIdMappingList, additionalIdList:additionalIdList]
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildPersonAlternateByNameType(List<PersonIdentificationNameAlternate> personIdentificationNameAlternateList, Map persons) {
        personIdentificationNameAlternateList.each {
            Person currentRecord = persons.get(it.pidm)
            def birthNameType = currentRecord.names.find { it.nameType == 'Birth' }
            if (!birthNameType) {
                def birthName = new NameAlternate(it)
                birthName.setNameType('Birth')
                currentRecord.names << birthName
            }
        }
        return persons
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildPersonCredentials(Map credentialsMap, Map persons, def personList) {
        if (["v2", "v3"].contains(getAcceptVersion(VERSIONS))) {
            credentialsMap.imsSourcedIdBaseList.each { sourcedIdBase ->
                Person person = persons.get(sourcedIdBase.pidm)
                person.credentials << new Credential("Banner Sourced ID", sourcedIdBase.sourcedId, null, null)
            }
            credentialsMap.thirdPartyAccessList.each { thirdPartyAccess ->
                if (thirdPartyAccess.externalUser) {
                    Person person = persons.get(thirdPartyAccess.pidm)
                    person.credentials << new Credential("Banner User Name", thirdPartyAccess.externalUser, null, null)
                }
            }
            credentialsMap.pidmAndUDCIdMappingList.each { pidmAndUDCIdMapping ->
                Person person = persons.get(pidmAndUDCIdMapping.pidm)
                person.credentials << new Credential("Banner UDC ID", pidmAndUDCIdMapping.udcId, null, null)
            }

            credentialsMap.additionalIdList.each { credential ->
                Person currentRecord = persons.get(credential.pidm)
                currentRecord.credentials << new Credential(Credential.additionalIdMap[credential.additionalIdentificationType.code],
                        credential.additionalId, null, null)
            }
        }
        personList?.each { currentRecord ->
            Person person = persons.get(currentRecord.pidm)
            person.credentials << new Credential("Banner ID", currentRecord.bannerId, null, null)
        }

        return persons
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildPersonEmails(personEmailList, persons) {
        personEmailList.each { PersonEmail activeEmail ->
            Person currentRecord = persons.get(activeEmail.pidm)
            IntegrationConfiguration rule = findAllByProcessCodeAndSettingNameAndValue(PROCESS_CODE, PERSON_EMAIL_TYPE, activeEmail?.emailType.code)
            if (rule?.value == activeEmail?.emailType?.code && !currentRecord.emails.contains {
                it.emailType == rule?.translationValue
            }) {
                GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndDomainSurrogateIds(PERSON_EMAILS_LDM_NAME, activeEmail.id)[0]
                String guid = globalUniqueIdentifier.guid
                def email = new Email(guid, activeEmail)
                email.emailType = rule?.translationValue
                currentRecord.emails << email
                if (["v2", "v3"].contains(getAcceptVersion(VERSIONS)) && activeEmail.preferredIndicator) {
                    def preferredEmail = new Email(guid, activeEmail)
                    preferredEmail.emailType = PERSON_EMAIL_TYPE_PREFERRED
                    currentRecord.emails << preferredEmail
                }
                persons.put(activeEmail.pidm, currentRecord)
            }
        }
        persons
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildPersonTelephones(List<PersonTelephone> personTelephoneList, Map persons) {
        personTelephoneList.each { entity ->
            Person personDecorator = persons.get(entity.pidm)
            IntegrationConfiguration rule = findAllByProcessCodeAndSettingNameAndValue(PROCESS_CODE, PERSON_PHONE_TYPE, entity?.telephoneType.code)
            if (rule?.value == entity?.telephoneType?.code &&
                    !(personDecorator.phones.contains { it.phoneType == rule?.translationValue })) {
                Phone phone = new Phone(entity)
                phone.phoneType = rule?.translationValue
                phone.phoneNumberDetail = (phone.countryPhone ?: "") + (phone.phoneArea ?: "") + (phone.phoneNumber ?: "")
                personDecorator.phones << phone
            }
            persons.put(entity.pidm, personDecorator)
        }
        persons
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildPersonAddresses(List<PersonAddress> personAddressList, Map persons) {
        personAddressList.each { activeAddress ->
            Person currentRecord = persons.get(activeAddress.pidm)
            IntegrationConfiguration addressTypeRule = findAllByProcessCodeAndSettingNameAndValue(PROCESS_CODE, PERSON_ADDRESS_TYPE, activeAddress.addressType.code)
            if (addressTypeRule?.value == activeAddress.addressType?.code &&
                    !currentRecord.addresses.contains { it.addressType == addressTypeRule?.translationValue }) {
                def address = new Address(activeAddress)
                address.addressType = addressTypeRule?.translationValue
                currentRecord.addresses << address
            }
            persons.put(activeAddress.pidm, currentRecord)
        }
        persons
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildPersonGuids(List domainIds, Map persons) {
        GlobalUniqueIdentifier.findAllByLdmNameAndDomainIdInList(ldmName, domainIds).each { guid ->
            Person currentRecord = persons.get(guid.domainKey.toInteger())
            currentRecord.guid = guid.guid
            persons.put(guid.domainKey.toInteger(), currentRecord)
        }
        persons
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildPersonRaces(List<PersonRace> personRacesList, Map persons) {
        personRacesList.each { activeRace ->
            Person currentRecord = persons.get(activeRace.pidm)
            def race = raceCompositeService.fetchByRaceCode(activeRace.race)
            race.metadata.dataOrigin = activeRace.dataOrigin
            currentRecord.races << race
            persons.put(activeRace.pidm, currentRecord)
        }
        persons
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildPersonRoles(Map persons, Boolean studentRole = false, List pidms = []) {
        if (!pidms) {
            persons.each { key, value ->
                pidms << key
            }
        }
        userRoleCompositeService.fetchAllRolesByPidmInList(pidms, studentRole).each { role ->
            Person currentRecord = persons.get(role.key.toInteger())
            currentRecord.roles = role.value
        }

        persons
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    def buildPersonAdditionalIds(List<AdditionalID> additionalIds, Map persons) {
        additionalIds.each { credential ->
            Person currentRecord = persons.get(credential.pidm)
            currentRecord.credentials << new Credential(Credential.additionalIdMap[credential.additionalIdentificationType.code],
                    credential.additionalId, null, null)
            persons.put(credential.pidm, currentRecord)
        }
        persons
    }


    private PersonBasicPersonBase updatePersonBasicPersonBase(pidmToUpdate, newPersonIdentificationName, person, changedPersonIdentification) {
        List<PersonBasicPersonBase> personBaseList = PersonBasicPersonBase.findAllByPidmInList([pidmToUpdate])
        PersonBasicPersonBase newPersonBase

        if (personBaseList.size() == 0) {
            //if there is no person base then create new PersonBase
            newPersonBase = createPersonBasicPersonBase(person, newPersonIdentificationName, changedPersonIdentification)
        } else {
            personBaseList.each { personBase ->
                //Copy personBase attributes into person map from Primary names object.
                if (person?.credentials instanceof List) {
                    person?.credentials?.each { it ->
                        if (it instanceof Map) {
                            personBase = updateSSN(it.credentialType, it.credentialId, personBase)
                        }
                    }
                }
                if (changedPersonIdentification) {
                    if (changedPersonIdentification.containsKey('namePrefix')) {
                        personBase.namePrefix = changedPersonIdentification.get('namePrefix')
                    }
                    if (changedPersonIdentification.containsKey('nameSuffix')) {
                        personBase.nameSuffix = changedPersonIdentification.get('nameSuffix')
                    }
                    if (changedPersonIdentification.containsKey('preferenceFirstName')) {
                        personBase.preferenceFirstName = changedPersonIdentification.get('preferenceFirstName')
                    }
                }
                //Translate enumerations and defaults
                if (person.containsKey('sex')) {
                    personBase.sex = person?.sex == 'Male' ? 'M' : (person?.sex == 'Female' ? 'F' : (person?.sex == 'Unknown' ? 'N' : null))
                }

                MaritalStatusDetail maritalStatusDetail
                if (person.maritalStatusDetail instanceof Map) {
                    String maritalStatusGuid = person.maritalStatusDetail.guid?.trim()?.toLowerCase()
                    if (!maritalStatusGuid) {
                        throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("marital.status.guid.required.message", []))
                    }
                    try {
                        maritalStatusDetail = maritalStatusV1CompositeService.get(maritalStatusGuid)
                    } catch (ApplicationException ae) {
                        LdmService.throwBusinessLogicValidationException(ae)
                    }
                    personBase.maritalStatus = maritalStatusDetail.maritalStatus
                }

                if (person.ethnicityDetail instanceof Map) {
                    createOrUpdatePersonEthnicity(person)
                    if (["v1", "v2"].contains(getAcceptVersion(VERSIONS))) {
                        personBase.ethnicity = person.get('ethnicity')
                    }
                    personBase.ethnic = person.get('ethnic')
                }

                if (person.containsKey('deadDate')) {
                    personBase.deadIndicator = person.get('deadDate') != null ? 'Y' : null
                    personBase.deadDate = person.get('deadDate')
                    if (personBase.deadDate != null && personBase.birthDate != null && personBase.deadDate.before(personBase.birthDate)) {
                        throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException('dateDeceased.invalid', [personBase.deadDate]))
                    }
                }
                if (person.containsKey('birthDate')) {
                    personBase.birthDate = person.get('birthDate')
                }
                if (person.containsKey('metadata') && person.metadata.containsKey('dataOrigin')) {
                    personBase.dataOrigin = person.metadata.get('dataOrigin')
                }


                newPersonBase = personBasicPersonBaseService.update(personBase)
            }
        }
        return newPersonBase
    }


    private updateAddresses(def pidm, Map metadata, List<Map> newAddresses) {
        def addresses = []
        List<PersonAddress> currentAddresses = PersonAddress.fetchActiveAddressesByPidm(['pidm': pidm]).get('list')
        currentAddresses.each { currentAddress ->
            if (findAllByProcessCodeAndSettingNameAndValue(PROCESS_CODE, PERSON_ADDRESS_TYPE, currentAddress.addressType.code)) {
                def activeAddresses = newAddresses.findAll { it ->
                    fetchAllByProcessCodeAndSettingNameAndTranslationValue(PROCESS_CODE, PERSON_ADDRESS_TYPE,
                            it.addressType)?.value == currentAddress.addressType.code
                }
                log.debug "NewAddresses:" + newAddresses.toString()
                log.debug "ActiveAddresses:" + activeAddresses.toString()
                log.debug "CurrentAddress:" + currentAddress.toString()
                def changeToInactiveStatus = false
                if (activeAddresses.size() > 0) {
                    activeAddresses.each { activeAddress ->
                        switch (activeAddress?.addressType) {
                            default:
                                if (activeAddress.state != currentAddress.state?.code) {
                                    log.debug "State different"
                                    changeToInactiveStatus = true
                                }
                                if (activeAddress.zip != currentAddress.zip) {
                                    log.debug "Zip different"
                                    changeToInactiveStatus = true
                                    break;
                                }
                                if (activeAddress?.nation?.containsKey('code')) {
                                    def nation
                                    if (activeAddress.nation?.code) {
                                        nation = Nation.findByScodIso(activeAddress?.nation?.code)
                                        if (!nation) {
                                            log.error "Nation not found for code: ${activeAddress?.country?.code}"
                                            throw new ApplicationException("Person", new BusinessLogicValidationException("country.not.found.message", []))
                                        }
                                    }
                                    if (nation?.code != currentAddress.nation?.code) {
                                        log.debug "Nation different:" + nation.code + " : " + currentAddress.nation?.code
                                        changeToInactiveStatus = true
                                        break;
                                    }
                                }
                                if (activeAddress.containsKey('county')) {
                                    def county
                                    if (activeAddress.county) {
                                        county = County.findByDescription(activeAddress.county)
                                        if (!county) {
                                            log.error "County not found for code: ${activeAddress.county}"
                                            throw new ApplicationException("Person", new BusinessLogicValidationException("county.not.found.message", []))
                                        }
                                    }
                                    if (county?.code != currentAddress.county?.code) {
                                        log.debug "County different"
                                        changeToInactiveStatus = true
                                        break;
                                    }
                                }
                                if (activeAddress.containsKey('streetLine1')) {
                                    if (activeAddress.streetLine1 != currentAddress.streetLine1) {
                                        log.debug "Street1 different"
                                        changeToInactiveStatus = true
                                        break;
                                    }
                                }
                                if (activeAddress.containsKey('streetLine2')) {
                                    if (activeAddress.streetLine2 != currentAddress.streetLine2) {
                                        log.debug "Street2 different"
                                        changeToInactiveStatus = true
                                        break;
                                    }
                                }
                                if (activeAddress.containsKey('streetLine3')) {
                                    if (activeAddress.streetLine3 != currentAddress.streetLine3) {
                                        log.debug "Street3 different"
                                        changeToInactiveStatus = true
                                        break;
                                    }
                                }
                                break;
                        }
                        if (changeToInactiveStatus) {
                            currentAddress.statusIndicator = 'I'
                            log.debug "Inactivating address:" + currentAddress.toString()
                            personAddressService.update(currentAddress)
                        } else {
                            def addressDecorator = new Address(currentAddress)
                            addressDecorator.addressType = activeAddress.addressType
                            addresses << addressDecorator
                            newAddresses.remove(activeAddress)
                            log.debug "After match, and removal of match from new to create:" + newAddresses.toString()
                        }
                    }
                } else {
                    currentAddress.statusIndicator = 'I'
                    log.debug "Inactivating address:" + currentAddress.toString()
                    personAddressService.update(currentAddress)
                }
            }
        }

        createAddresses(pidm, metadata, newAddresses).each {
            currentAddress ->
                def addressDecorator = new Address(currentAddress)
                addressDecorator.addressType = findAllByProcessCodeAndSettingNameAndValue(PROCESS_CODE, PERSON_ADDRESS_TYPE, currentAddress.addressType.code)?.translationValue
                addresses << addressDecorator
        }
        addresses

    }


    private
    def updatePhones(Integer pidm, Map metadata, List<Map> newPhones, List<PersonTelephone> existingPersonTelephones) {
        List<PersonTelephone> lstPersonTelephone = []
        List<String> processedPhoneTypes = []
        PersonTelephone personTelephone
        newPhones?.each { requestPhone ->
            String hedmPhoneType = requestPhone.phoneType.trim()
            String requestPhoneNumber = requestPhone.phoneNumber
            log.debug "$hedmPhoneType - $requestPhoneNumber"
            if (!processedPhoneTypes.contains(hedmPhoneType)) {
                TelephoneType telephoneType = getBannerTypeForHEDMPhoneType(hedmPhoneType)
                log.debug "Processing ${telephoneType.code} - $requestPhoneNumber ..."
                PersonTelephone existingPersonTelephone = getExistingPersonTelephone(requestPhone, telephoneType, existingPersonTelephones)
                if (existingPersonTelephone) {
                    // Update
                    personTelephone = existingPersonTelephone
                    existingPersonTelephones.remove(existingPersonTelephone)
                } else {
                    // Create
                    personTelephone = parseAndCreatePersonTelephone(pidm, metadata, telephoneType, requestPhone)
                    personTelephone = personTelephoneService.create(personTelephone)
                }
                lstPersonTelephone << personTelephone
                processedPhoneTypes << hedmPhoneType
            }
        }

        // Inactive DB records
        existingPersonTelephones?.each { entity ->
            entity.statusIndicator = 'I'
            log.debug "Inactivating phone:" + entity.toString()
            personTelephoneService.update(entity)
        }

        return lstPersonTelephone
    }


    private PersonTelephone getExistingPersonTelephone(
            def requestPhone, TelephoneType telephoneType, List<PersonTelephone> existingPersonTelephones) {
        PersonTelephone existingPersonTelephone
        List<PersonTelephone> existingPersonTelephonesByType = existingPersonTelephones?.findAll {
            it.telephoneType.code == telephoneType.code
        }
        existingPersonTelephonesByType.each { entity ->
            boolean sameAsExisting = true
            if (requestPhone.containsKey('phoneNumber')) {
                sameAsExisting = PhoneNumberUtility.comparePhoneNumber(requestPhone.phoneNumber, entity.countryPhone, entity.phoneArea, entity.phoneNumber)
            }
            if (sameAsExisting && requestPhone.containsKey('phoneExtension')) {
                String reqPhoneExtn = requestPhone.phoneExtension ? requestPhone.phoneExtension.trim() : ""
                String dbPhoneExtn = entity.phoneExtension ? entity.phoneExtension.trim() : ""
                if (reqPhoneExtn != dbPhoneExtn) {
                    log.debug "Phone extension different"
                    sameAsExisting = false
                }
            }
            if (sameAsExisting) {
                existingPersonTelephone = entity
                return existingPersonTelephone
            }
        }
        return existingPersonTelephone
    }


    List<RaceDetail> updateRaces(def pidm, Map metadata, List<Map> newRaces) {
        def races = []
        List<PersonRace> personRaceList = PersonRace.fetchByPidm(pidm)
        personRaceList.each { currentRace ->
            def raceGuid = GlobalUniqueIdentifier.fetchByLdmNameAndDomainKey('races', currentRace.race)[0]?.guid
            def activeRaces = newRaces.findAll { it ->
                it.guid == raceGuid
            }
            log.debug "currentRace:" + currentRace.toString() + " : " + raceGuid
            log.debug "Races matching:" + activeRaces.toString()
            log.debug "Races to create:" + newRaces.toString()
            if (activeRaces.size() > 0) {
                newRaces.remove(activeRaces[0])
                def race = raceCompositeService.get(activeRaces[0].guid)
                races << race
            } else {
                personRaceService.delete(currentRace)
                log.debug "Removing race:" + currentRace.toString()
            }
        }
        createRaces(pidm, metadata, newRaces).each { currentRace ->
            def race = raceCompositeService.fetchByRaceCode(currentRace.race)
            races << race
        }
        races
    }


    def createOrUpdateAdditionalId(PersonIdentificationNameCurrent personIdentification, Map credential, Map metadata) {
        def idCode = Credential.additionalIdMap.find { key, value ->
            value == credential.credentialType
        }?.key
        def idType = AdditionalIdentificationType.findByCode(idCode)
        List<AdditionalID> existingIds = additionalIDService.fetchAllByPidmInListAndIdentificationTypeCodeInList([personIdentification.pidm], [idCode])
        AdditionalID existingId
        if (existingIds.size() > 0) {
            existingId = existingIds.get(0)
            existingId.additionalId = credential?.credentialId
        } else
            existingId = new AdditionalID(pidm: personIdentification.pidm,
                    additionalIdentificationType: idType,
                    additionalId: credential?.credentialId,
                    dataOrigin: metadata?.dataOrigin)
        additionalIDService.createOrUpdate(existingId)
    }


    private def splitPhoneNumber(String requestPhoneNumber) {
        def parts = [:]
        if (requestPhoneNumber.length() <= 12) {
            parts.put('phoneNumber', requestPhoneNumber)
        } else {
            parts.put('countryPhone', requestPhoneNumber.substring(0, 4))
            parts.put('phoneArea', requestPhoneNumber.substring(4, 10))
            String number = requestPhoneNumber.substring(10, requestPhoneNumber.length())
            if (number.length() > 12) {
                number = number.substring(0, 12)
            }
            parts.put('phoneNumber', number)
        }

        parts
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    private def getStateAndZip(activeAddress, isInternational) {
        getAddressPostalCode(getAddressRegion(activeAddress, isInternational), isInternational)
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    private def getAddressRegion(activeAddress, isInternational) {
        State state
        if (activeAddress.state) {
            state = State.findByCode(activeAddress?.state)
            if (!state) {
                log.error "State not found for code: ${activeAddress.state}"
                throw new ApplicationException("Person", new BusinessLogicValidationException("state.not.found.message", []))
            }
        } else {
            if (isInternational) {
                state = null
            } else {
                IntegrationConfiguration intConf
                intConf = getIntegrationConfiguration(PROCESS_CODE, PERSON_REGION)
                state = State.findByCode(intConf?.value)
                if (!state) {
                    throw new ApplicationException(Person, new BusinessLogicValidationException("goriccr.invalid.value.message", [PERSON_REGION]))
                }
            }
        }
        activeAddress.put('state', state)

        return activeAddress
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    private def getAddressPostalCode(activeAddress, isInternational) {
        String zip
        if (activeAddress.zip) {
            zip = activeAddress.zip
        } else {
            if (isInternational) {
                zip = null
            } else {
                IntegrationConfiguration intConf
                intConf = getIntegrationConfiguration(PROCESS_CODE, PERSON_POSTAL_CODE)
                if (intConf.value == "UPDATE_ME") {
                    throw new ApplicationException(Person, new BusinessLogicValidationException("goriccr.invalid.value.message", [PERSON_POSTAL_CODE]))
                }
                zip = intConf.value
            }
        }
        activeAddress.put('zip', zip)

        return activeAddress
    }


    private IntegrationConfiguration getIntegrationConfiguration(processCode, settingName) {
        IntegrationConfiguration intConf = IntegrationConfiguration.findByProcessCodeAndSettingName(processCode, settingName)
        if (!intConf) {
            throw new ApplicationException(Person, new BusinessLogicValidationException("goriccr.not.found.message", [settingName]))
        }
        return intConf
    }


    def validateAddressRequiredFields(address) {
        if (!address.addressType) {
            throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("addressType.invalid", []))
        }
        if (!address.streetLine1) {
            throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("streetAddress.invalid", []))
        }
        if (!address.city) {
            throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("city.invalid", []))
        }
    }


    def validatePhoneRequiredFields(requestPhone) {
        if (!requestPhone.phoneNumber) {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("phoneNumber.invalid", []))
        }
    }


    def validateEmailRequiredFields(email) {
        /*if(["v2","v3"].contains(getAcceptVersion())) {
            if (!email.guid) {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("emailGuid.invalid", []))
            }
        }*/
        if (!email instanceof Map) {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("emailType.invalid", null))
        }
        if (!email.emailType) {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("emailType.invalid", []))
        }
        if (!email.emailAddress) {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("emailAddress.invalid", []))
        }
    }


    private def validateCredentialType(String inputCredentialType,
                                       def allowedCredentialTypes, String credentialId) {
        if (!allowedCredentialTypes.contains(inputCredentialType)) {
            throw new ApplicationException('Person', new BusinessLogicValidationException("invalid.code.message:credentialType", []))
        }
        if (inputCredentialType == 'Social Security Number' || inputCredentialType == 'Social Insurance Number') {
            if (credentialId == null) {
                throw new ApplicationException('Person', new BusinessLogicValidationException("ssn.credentialId.null.message", []))
            }
            if (credentialId.trim() == '') {
                throw new ApplicationException('Person', new BusinessLogicValidationException("ssn.credentialId.empty.message", []))
            }
            if (credentialId.length() > 9) {
                throw new ApplicationException('Person', new BusinessLogicValidationException("credentialId.length.message", []))
            }
        }
    }


    private def createSSN(String inputCredentialType, String credentialId, Map person) {
        if (inputCredentialType == 'Social Security Number' || inputCredentialType == 'Social Insurance Number') {
            //Copy ssn attribute from credential to person map.
            person.put('ssn', credentialId)
        }

        return person
    }


    private def updateSSN(String inputCredentialType, String credentialId, def personBase) {
        if (inputCredentialType == 'Social Security Number' || inputCredentialType == 'Social Insurance Number') {
            if (personBase.ssn == null || integrationConfigurationService.canUpdatePersonSSN()) {
                personBase.ssn = credentialId
            }
        }
        return personBase
    }


    private def getPidmsForPersonFilter(String selId, Map params) {
        def retMap = personFilterCompositeService.fetchPidmsOfPopulationExtract(selId, params.sort.trim(), params.order.trim(), params.max.trim().toInteger(), params.offset?.trim()?.toInteger() ?: 0)
        return [personList: retMap.entities, count: retMap.totalCount]
    }


    private PersonIdentificationNameAlternate createPersonIdentificationNameAlternateByNameType(PersonIdentificationNameCurrent currentPerson,
                                                                                                def nameInRequest, Map metadata) {
        PersonIdentificationNameAlternate personIdentificationNameAlternate
        NameType nameType = getBannerNameTypeFromHEDMNameType(nameInRequest.nameType.trim())

        PersonIdentificationNameAlternate newPersonIdentificationNameAlternate = new PersonIdentificationNameAlternate(
                pidm: currentPerson.pidm,
                bannerId: currentPerson.bannerId,
                lastName: nameInRequest.lastName,
                firstName: nameInRequest.firstName,
                middleName: nameInRequest.middleName,
                changeIndicator: 'N',
                entityIndicator: 'P',
                nameType: nameType,
                dataOrigin: metadata?.dataOrigin
        )
        personIdentificationNameAlternate = personIdentificationNameAlternateService.create(newPersonIdentificationNameAlternate)

        return personIdentificationNameAlternate
    }


    private PersonIdentificationNameAlternate getPersonIdentificationNameAlternateByNameType(Integer pidm) {
        NameType nameType = getBannerNameTypeFromHEDMNameType('Birth')
        return personIdentificationNameAlternateService.fetchAllMostRecentlyCreated([pidm], [nameType.code])[0]
    }


    private NameType getBannerNameTypeFromHEDMNameType(def nameTypeInRequest) {
        IntegrationConfiguration rule = IntegrationConfiguration.fetchAllByProcessCodeAndSettingNameAndTranslationValue('HEDM', PERSON_NAME_TYPE, nameTypeInRequest)[0]
        if (!rule) {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException('goriccr.not.found.message', [PERSON_NAME_TYPE]))
        }
        NameType nameType = NameType.findByCode(rule.value)
        if (!nameType) {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException('goriccr.invalid.value.message', [PERSON_NAME_TYPE]))
        }

        return nameType
    }


    private def createOrUpdatePersonEthnicity(def person) {
        def ethnicityDetail
        String ethnicityGuid = person.ethnicityDetail.guid?.trim()?.toLowerCase()
        if (!ethnicityGuid) {
            throw new ApplicationException("PersonCompositeService", new BusinessLogicValidationException("ethnicity.guid.required.message", []))
        }
        try {
            if (["v1", "v2"].contains(getAcceptVersion(VERSIONS))) {
                ethnicityDetail = ethnicityCompositeService.get(ethnicityGuid)
                person.put('ethnicity', ethnicityDetail.ethnicity)
                person.put('ethnic', ethnicityDetail.ethnic)
            } else if ("v3".equals(getAcceptVersion(VERSIONS))) {
                allEthnicities = ethnicityCompositeService.getUnitedStatesEthnicCodes()
                Long ethnic = allEthnicities.find { it.guid == ethnicityGuid }?.domainId
                if (!ethnic?.toString()) {
                    throw new ApplicationException("ethnicity", new NotFoundException())
                }
                person.put('ethnic', (ethnic != 0 ? ethnic.toString() : null))
                ethnicityDetail = ["guid": ethnicityGuid]
            }
        } catch (ApplicationException ae) {
            LdmService.throwBusinessLogicValidationException(ae)
        }
        return ethnicityDetail
    }


    private def buildPersonEthnicity(def personBase, List<GlobalUniqueIdentifier> globalUniqueIdentifier = null) {
        def ethnicityDetail
        if (["v1", "v2"].contains(getAcceptVersion(VERSIONS))) {
            if (personBase.ethnicity) {
                ethnicityDetail = ethnicityCompositeService.fetchByEthnicityCode(personBase.ethnicity.code)
            }
        } else if ("v3".equals(getAcceptVersion(VERSIONS))) {
            if (!globalUniqueIdentifier) {
                allEthnicities = ethnicityCompositeService.getUnitedStatesEthnicCodes()
            }
            String guid
            if (personBase.ethnic) {
                guid = allEthnicities.find { Long.parseLong(personBase.ethnic) == it.domainId }?.guid
            } else {
                guid = allEthnicities.find { it.domainId == 0 }?.guid
            }
            ethnicityDetail = ["guid": guid]
        }
        return ethnicityDetail
    }


    private boolean isNamesElementBirthIsSameAsExisting(
            def namesElementBirth, PersonIdentificationNameAlternate existingPersonBirthRecord) {
        boolean exists = true
        if (!existingPersonBirthRecord || !(existingPersonBirthRecord?.firstName == namesElementBirth.firstName) || !(existingPersonBirthRecord?.lastName == namesElementBirth.lastName) || !(existingPersonBirthRecord?.middleName == namesElementBirth.middleName)) {
            exists = false
        }

        return exists
    }

}

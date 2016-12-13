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
import net.hedtech.banner.general.overall.*
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.person.*
import net.hedtech.banner.general.system.*
import net.hedtech.banner.general.system.ldm.*
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import java.sql.Timestamp

abstract class AbstractPersonCompositeService extends LdmService {

    private static final int MAX_DEFAULT = 500
    private static final int MAX_UPPER_LIMIT = 500

    private static final String ldmName = 'persons'

    private static final String ZIP_DEFAULT = "PERSON.ADDRESSES.POSTAL.CODE"


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
    PersonGeographicAreaAddressService personGeographicAreaAddressService
    GeographicAreaCompositeService geographicAreaCompositeService
    PersonAddressAdditionalPropertyService personAddressAdditionalPropertyService
    MaritalStatusService maritalStatusService
    VisaInternationalInformationService visaInternationalInformationService
    NationCompositeService nationCompositeService
    ReligionCompositeService religionCompositeService
    CitizenshipStatusCompositeService citizenshipStatusCompositeService
    def additionalIDService
    def outsideInterestService
    def interestCompositeService
    def crossReferenceRuleService
    IntegrationConfigurationService integrationConfigurationService


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
        if (requestData.containsKey("birthDate") && requestData.get("birthDate")) {
            personBase.put('birthDate', requestData.get("birthDate"))
        }
        if (requestData.containsKey("deadDate") && requestData.get("deadDate")) {
            personBase.put('deadDate', requestData.get("deadDate"))
        }
        if (requestData.containsKey("confidIndicator") && requestData.get("confidIndicator") instanceof String) {
            personBase.put('confidIndicator', requestData.get("confidIndicator"))
        }
        if (requestData.containsKey("ethnicity") && requestData.get("ethnicity") instanceof String) {
            personBase.put('ethnic', requestData.get("ethnicity"))
        }

        if (requestData.containsKey("maritalStatus") && requestData.get("maritalStatus") instanceof MaritalStatus) {
            personBase.put('maritalStatus', requestData.get("maritalStatus"))
        }

        if (requestData.containsKey("sex") && requestData.get("sex") instanceof String) {
            personBase.put('sex', requestData.get("sex"))
        }

        if (requestData.containsKey("citizenType") && requestData.get("citizenType") instanceof CitizenType) {
            personBase.put('citizenType', requestData.get("citizenType"))
        }

        String religionGuid
        if (requestData.containsKey("religion") && requestData.get("religion") instanceof Religion) {
            personBase.put('religion', requestData.get("religion"))
            religionGuid = requestData.get("religionGuid")
        }
        if (personBase.deadDate != null && personBase.birthDate != null && personBase.deadDate.before(personBase.birthDate)) {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException('dateDeceased.invalid', null))
        }

        // person credentials
        def additionalIds = []
        if (requestData.containsKey("credentials") && requestData.get("credentials")?.size() > 0) {
            def personCredentials = requestData.get("credentials")
            personCredentials.each { credential ->
                if (CredentialType.SOCIAL_SECURITY_NUMBER == credential.type) {
                    personBase.put('ssn', credential.value)
                }
                if (CredentialType.SOCIAL_INSURANCE_NUMBER == credential.type) {
                    personBase.put('ssn', credential.value)
                }
                if (CredentialType.ELEVATE_ID == credential.type) {
                    additionalIds << createOrUpdateAdditionalId(newPersonIdentificationName, credential, dataOrigin)
                }
            }
        }

        personBase.put('deadIndicator', requestData.get("deadDate") ? 'Y' : null)
        personBase.put('armedServiceMedalVetIndicator', false)
        personBase.put('pidm', newPersonIdentificationName?.pidm)
        PersonBasicPersonBase newPersonBase = personBasicPersonBaseService.create(personBase)

        // person Race
        List<PersonRace> personRaces
        if (requestData.containsKey("raceCodes") && requestData.get("raceCodes") instanceof List) {
            personRaces = createOrUpdatePersonRace(requestData.get("raceCodes"), newPersonIdentificationName.pidm, personRaces, dataOrigin)
        }

        //person emails
        List<PersonEmail> personEmails
        if (requestData.containsKey("emails") && requestData.get("emails") instanceof List) {
            personEmails = createOrUpdatePersonEmails(requestData.get("emails"), newPersonIdentificationName.pidm, personEmails, dataOrigin)
        }

        //person birthCountry
        VisaInternationalInformation visaInternationalInformation
        if (requestData.containsKey("nationBirth") || requestData.containsKey("nationLegal") || requestData.containsKey("language") || requestData.containsKey("identityDocuments")) {
            visaInternationalInformation = createOrUpdateVisaInternationalInformation(newPersonIdentificationName.pidm, requestData, dataOrigin)
        }

        //person interests
        List personInterests
        if (requestData.containsKey("interests")) {
            personInterests = createOrUpdatePersonInterests(newPersonIdentificationName.pidm, requestData.get("interests"), personInterests, dataOrigin)
        }

        //person Address
        List<PersonAddress> personAddresses = []
        if (requestData.containsKey("addresses") && requestData.get("addresses") instanceof List && requestData.get("addresses").size() > 0) {
            List addresses = requestData.get("addresses")
            personAddresses = createOrUpdateAddress(addresses, newPersonIdentificationName.pidm)
        }

        //person Phones
        List<PersonTelephone> personTelephones = []
        if (requestData.containsKey("phones") && requestData.get("phones") instanceof List && requestData.get("phones").size() > 0) {
            List phones = requestData.get("phones")
            personTelephones = createPhones(phones, newPersonIdentificationName.pidm)
        }


        Map pidmToGuidMap = [:]
        pidmToGuidMap.put(newPersonIdentificationName.pidm, personGuid)

        Map dataMapForPerson = [:]
        dataMapForPerson.put("personIdentificationNameCurrent", newPersonIdentificationName)
        dataMapForPerson.put("personBase", newPersonBase)
        dataMapForPerson.put("personGuid", personGuid)
        dataMapForPerson.put("personAddresses", personAddresses)
        dataMapForPerson.put("additionalIds", additionalIds)
        dataMapForPerson.put("personRaces", personRaces)
        dataMapForPerson.put("personEmails", personEmails)
        dataMapForPerson.put("visaInternationalInformation", visaInternationalInformation)
        dataMapForPerson.put("personInterests", personInterests)
        dataMapForPerson.put("personPhones", personTelephones)
        return prepareDataMapForSinglePerson(dataMapForPerson)
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
                if (!(isNamesElementSameAsExisting(nameRecord.firstName, nameRecord.lastName, nameRecord.middleName, nameRecord.surnamePrefix, existingPersonRecord))) {
                    newPersonBirthRecord = createPersonIdentificationNameAlternateByNameType(newPersonIdentificationName,
                            nameRecord,
                            dataOrigin)
                }
            }
        }

        //personBasicPersonBase
        List<PersonBasicPersonBase> personBaseList = PersonBasicPersonBase.findAllByPidmInList([pidm])

        // person redentials
        String ssn
        def additionalIds = []
        if (requestData.containsKey("credentials") && requestData.get("credentials")?.size() > 0) {
            def personCredentials = requestData.get("credentials")
            personCredentials.each { credential ->
                if (CredentialType.SOCIAL_SECURITY_NUMBER == credential.type || CredentialType.SOCIAL_INSURANCE_NUMBER == credential.type) {
                    ssn = credential.value
                }
                if (CredentialType.ELEVATE_ID == credential.type) {
                    additionalIds << createOrUpdateAdditionalId(newPersonIdentificationName, credential, dataOrigin)
                }
                if (CredentialType.BANNER_ID == credential.type) {
                    PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.fetchByBannerId(credential.value)
                    if (!personIdentificationNameCurrent) {
                        newPersonIdentificationName.bannerId = credential.value
                        personIdentificationNameCurrentService.update(newPersonIdentificationName)
                    } else {
                        throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("bannerId.already.exists", null))
                    }
                }
            }
        }

        //update PersonBasicPersonBase
        PersonBasicPersonBase newPersonBase = updatePersonBasicPersonBase(pidm, personBaseList, newPersonIdentificationName, requestData, alternateNames, ssn, dataOrigin)

        // person Race
        List<PersonRace> personRaces = personRaceService.fetchRaceByPidmList([pidm])
        if (requestData.containsKey("raceCodes") && requestData.get("raceCodes") instanceof List) {
            personRaces = createOrUpdatePersonRace(requestData.get("raceCodes"), newPersonIdentificationName.pidm, personRaces, dataOrigin)
        }

        //person emails
        Map bannerEmailTypeToHedmEmailTypeMap = getBannerEmailTypeToHedmEmailTypeMap()
        List<PersonEmail> personEmails = personEmailService.fetchAllEmails(pidm, bannerEmailTypeToHedmEmailTypeMap.keySet())
        if (requestData.containsKey("emails") && requestData.get("emails") instanceof List) {
            personEmails = createOrUpdatePersonEmails(requestData.get("emails"), newPersonIdentificationName.pidm, personEmails, dataOrigin)
        } else {
            personEmails = personEmailService.fetchAllActiveEmails([pidm], bannerEmailTypeToHedmEmailTypeMap.keySet())
        }

        //person interests
        List personInterests = outsideInterestService?.fetchAllByPidmInList([pidm])
        if (requestData.containsKey("interests")) {
            personInterests = createOrUpdatePersonInterests(newPersonIdentificationName.pidm, requestData.get("interests"), personInterests, dataOrigin)
        }

        //person Address
        List<PersonAddress> personAddresses = []
        if (requestData.containsKey("addresses") && requestData.get("addresses") instanceof List) {
            List addresses = requestData.get("addresses")
            //Update : Make it inactive , if exist person address have any updates
            addresses = getActiveAddresses(pidm, addresses)
            if (addresses) {
                createOrUpdateAddress(addresses, newPersonIdentificationName.pidm)
            }
        }

        personAddresses = personAddressService.fetchAllByActiveStatusPidmsAndAddressTypes([pidm], getBannerAddressTypeToHedmAddressTypeMap().keySet())

        //person birthCountry
        VisaInternationalInformation visaInternationalInformation
        if (requestData.containsKey("nationBirth") || requestData.containsKey("nationLegal") || requestData.containsKey("language") || requestData.containsKey("identityDocuments")) {
            visaInternationalInformation = createOrUpdateVisaInternationalInformation(newPersonIdentificationName.pidm, requestData, dataOrigin)
        } else {
            visaInternationalInformation = visaInternationalInformationService.fetchAllByPidmInList([newPersonIdentificationName.pidm])[0]
        }

        //person Phones
        List<PersonTelephone> personTelephones = []
        if (requestData.containsKey("phones") && requestData.get("phones") instanceof List) {
            List phones = requestData.get("phones")
            phones = getActivePhones(newPersonIdentificationName.pidm, phones)
            if (phones) {
                createPhones(phones, newPersonIdentificationName.pidm)
            }
        }
        personTelephones = personTelephoneService.fetchAllActiveByPidmInListAndTelephoneTypeCodeInList([pidm], getBannerPhoneTypeToHedmPhoneTypeMap().keySet())

        Map pidmToGuidMap = [:]
        pidmToGuidMap.put(newPersonIdentificationName.pidm, personGuid)


        Map dataMapForPerson = [:]
        dataMapForPerson.put("personIdentificationNameCurrent", newPersonIdentificationName)
        dataMapForPerson.put("personBase", newPersonBase)
        dataMapForPerson.put("personGuid", personGuid)
        dataMapForPerson.put("personAddresses", personAddresses)
        dataMapForPerson.put("additionalIds", additionalIds)
        dataMapForPerson.put("personRaces", personRaces)
        dataMapForPerson.put("personEmails", personEmails)
        dataMapForPerson.put("visaInternationalInformation", visaInternationalInformation)
        dataMapForPerson.put("personInterests", personInterests)
        dataMapForPerson.put("personPhones", personTelephones)

        return prepareDataMapForSinglePerson(dataMapForPerson)

    }


    private
    def createOrUpdatePersonInterests(Integer pidm, List interestsInRequest, List existingPersonInterests, String dataOrigin) {
        List personInterests = []
        interestsInRequest.each { interest ->
            def existingPersonInterest = existingPersonInterests.find {
                it.pidm == pidm && it.interest == interest
            }
            if (existingPersonInterest) {
                personInterests << existingPersonInterest
                existingPersonInterests.remove(existingPersonInterest)
            } else {
                // Create
                Map personInterestMap = [:]
                personInterestMap.put("pidm", pidm)
                personInterestMap.put("interest", interest)
                def personInterest = outsideInterestService.create(personInterestMap)
                personInterests << personInterest
            }
        }
        existingPersonInterests.each {
            outsideInterestService.delete([domainModel: it])
        }
        return personInterests
    }


    private def createOrUpdateVisaInternationalInformation(Integer pidm, Map requestData, String dataOrigin) {
        VisaInternationalInformation personVisaInternationalInformation = visaInternationalInformationService.fetchAllByPidmInList([pidm])[0]
        if (!personVisaInternationalInformation) {
            personVisaInternationalInformation = creatNewPersonVisaInternationalInformation(pidm, requestData, dataOrigin)
        } else {
            if (requestData.containsKey("nationBirth")) {
                personVisaInternationalInformation.nationBirth = requestData.get("nationBirth")
            }
            if (requestData.containsKey("nationLegal")) {
                personVisaInternationalInformation.nationLegal = requestData.get("nationLegal")
            }
            if (requestData.containsKey("language")) {
                personVisaInternationalInformation.language = requestData.get("language")
            }
            if (requestData.containsKey("identityDocuments") && requestData.get("identityDocuments") instanceof List && requestData.get("identityDocuments").size() > 0) {

                Map identityDocumentMap = requestData.get("identityDocuments")[0]

                if (identityDocumentMap.containsKey('nationIssue') && identityDocumentMap.get('nationIssue') instanceof String) {
                    identityDocumentMap.nationIssue = fetchNationByScondISO(identityDocumentMap.get('nationIssue'))?.code
                }
                bindData(personVisaInternationalInformation, identityDocumentMap, [:])
            } else {
                personVisaInternationalInformation.nationIssue = null
                personVisaInternationalInformation.passportExpenditureDate = null
                personVisaInternationalInformation.passportId = null
            }
            personVisaInternationalInformation = visaInternationalInformationService.update(domainModel: personVisaInternationalInformation)
        }
        return personVisaInternationalInformation
    }


    private def creatNewPersonVisaInternationalInformation(Integer pidm, Map requestData, String dataOrigin) {
        VisaInternationalInformation personVisaInternationalInformation

        Map visaInfoMap = [:]
        visaInfoMap.put("pidm", pidm)
        visaInfoMap.put("spouseIndicator", "Y")

        if (requestData.containsKey("nationBirth") && requestData.get("nationBirth")?.length() > 0) {
            visaInfoMap.put("nationBirth", requestData.get("nationBirth"))
        }
        if (requestData.containsKey("nationLegal") && requestData.get("nationLegal")?.length() > 0) {
            visaInfoMap.put("nationLegal", requestData.get("nationLegal"))
        }
        if (requestData.containsKey("language") && requestData.get("language")) {
            visaInfoMap.put("language", requestData.get("language"))
        }
        if (dataOrigin && dataOrigin?.length() > 0) {
            visaInfoMap.put("dataOrigin", dataOrigin)
        }
        if (requestData.containsKey("identityDocuments") && requestData.get("identityDocuments") instanceof List && requestData.get("identityDocuments").size() > 0) {
            Map identityDocumentMap = requestData.get("identityDocuments")[0]

            if (identityDocumentMap.containsKey('nationIssue') && identityDocumentMap.get('nationIssue') instanceof String) {
                identityDocumentMap.nationIssue = fetchNationByScondISO(identityDocumentMap.get('nationIssue'))?.code
            }

            visaInfoMap.putAll(identityDocumentMap)
        }
        personVisaInternationalInformation = visaInternationalInformationService.create(visaInfoMap)
        return personVisaInternationalInformation
    }


    private def fetchNationByScondISO(String nationISOCode) {
        if (!nationISOCode) {
            return null
        }

        Nation nation = Nation.findByScodIso(nationISOCode)
        if (nation) {
            return nation
        } else {
            throw new ApplicationException("Person", new BusinessLogicValidationException("country.not.found.message", []))
        }

    }


    private
    def createOrUpdatePersonEmails(List emailListInRequest, Integer pidm, List<PersonEmail> existingPersonEmails, String dataOrigin) {
        List<PersonEmail> personEmails = []
        existingPersonEmails.each {
            it.statusIndicator = "I"
            it.preferredIndicator = false
            personEmailService.update([domainModel: it])
        }
        emailListInRequest.each { emailMapInRequest ->
            EmailType emailTypeInRequest = emailMapInRequest.bannerEmailType
            String emailAddressInRequest = emailMapInRequest.emailAddress
            PersonEmail existingPersonEmail = existingPersonEmails.find {
                it.pidm == pidm && it.emailType.code == emailTypeInRequest.code && it.emailAddress == emailAddressInRequest
            }
            PersonEmail existingPersonEmailWithDiffrentCase
            if (!existingPersonEmail) {
                existingPersonEmailWithDiffrentCase = existingPersonEmails.find {
                    it.pidm == pidm && it.emailType.code == emailTypeInRequest.code && it.emailAddress.toLowerCase() == emailAddressInRequest.toLowerCase()
                }
            }
            if (existingPersonEmailWithDiffrentCase && !existingPersonEmail) {
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("existing.email.message", [existingPersonEmailWithDiffrentCase.emailAddress]))
            }
            if (existingPersonEmail) {
                existingPersonEmail.preferredIndicator = emailMapInRequest.preference == 'Y' ? true : false
                if (existingPersonEmail.statusIndicator == 'I') {
                    existingPersonEmail.statusIndicator = 'A'
                }
                personEmailService.update([domainModel: existingPersonEmail])
                personEmails << existingPersonEmail
                existingPersonEmails.remove(existingPersonEmail)
            } else {
                // Create
                PersonEmail personEmail = new PersonEmail(pidm: pidm, emailAddress: emailAddressInRequest, statusIndicator: "A", emailType: emailTypeInRequest, dataOrigin: dataOrigin)
                if (emailMapInRequest.preference == 'Y') {
                    personEmail.preferredIndicator = true
                }
                personEmail = personEmailService.create([domainModel: personEmail])
                personEmails << personEmail
            }
        }
        return personEmails
    }


    private
    def createOrUpdatePersonRace(List raceCodes, Integer pidm, List<PersonRace> existingPersonRaces, String dataOrigin) {
        List<PersonRace> personRaces = []
        raceCodes.each { raceCode ->
            def existingPersonRace = existingPersonRaces.find { it.race == raceCode && it.pidm == pidm }
            if (existingPersonRace) {
                personRaces << existingPersonRace
                existingPersonRaces.remove(existingPersonRace)
            } else {
                def personRace = [pidm: pidm]
                personRace.put("race", raceCode)
                personRace.put("dataOrigin", dataOrigin)
                PersonRace newPersonRace = personRaceService.create(personRace)
                personRaces << newPersonRace
            }
        }
        existingPersonRaces.each { personRace ->
            //Can't get around this, Hibernate updates before it deletes, triggering table-api errors.
            PersonRace.executeUpdate("delete from PersonRace where race = :race and pidm = :pidm", [race: personRace.race, pidm: pidm])
        }
        return personRaces
    }


    private def getActiveAddresses(def pidm, List<Map> newAddresses) {
        Map addressTypeToHedmAddressTypeMap = getBannerAddressTypeToHedmAddressTypeMap()
        List<PersonAddress> personAddresses = personAddressService.fetchAllByActiveStatusPidmsAndAddressTypes([pidm], addressTypeToHedmAddressTypeMap.keySet())
        List<PersonGeographicAreaAddress> geographicAreaAddresses = personGeographicAreaAddressService.fetchActivePersonGeographicAreaAddress(pidm)
        List<PersonAddressAdditionalProperty> additionalProperties = personAddressAdditionalPropertyService.fetchAllBySurrogateIds(personAddresses.id)
        personAddresses.each { personAddress ->

            def activeRequestAddresses = newAddresses.findAll { it ->
                it.bannerAddressType == personAddress.addressType.code
            }

            if (activeRequestAddresses.size() > 0) {
                activeRequestAddresses.each {
                    PersonAddressAdditionalProperty additionalProperty = additionalProperties.find {
                        it.id == personAddress.id
                    }
                    Boolean changeToInactiveStatus = false
                    switch (it.bannerAddressType) {
                        default:
                            if (it.streetLine1?.trim() != personAddress.streetLine1) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("streetLine2") && it.streetLine2?.trim() != personAddress.streetLine2) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("streetLine3") && it.streetLine3?.trim() != personAddress.streetLine3) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("streetLine4") && it.streetLine4?.trim() != personAddress.streetLine4) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("toDate") && it.toDate?.clearTime() != personAddress.toDate?.clearTime()) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (!it.containsKey("fromDateUpdate") && it.fromDate?.clearTime() != personAddress.fromDate?.clearTime()) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (!it.containsKey("nationISOCodeUpdate") && it.nationISOCode != personAddress.nation?.scodIso) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (!it.containsKey("cityUpdate") && it.city?.trim() != personAddress.city) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("stateCode") && it.stateCode?.trim() != personAddress.state.code) {
                                changeToInactiveStatus = true
                            }
                            if (it.containsKey("stateDescription") && it.stateDescription?.trim() != personAddress.state?.description) {
                                changeToInactiveStatus = true
                            }

                            if (!it.containsKey("postalCodeUpdate") && it.zip?.trim() != personAddress.zip) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (!it.containsKey("countyUpdate") && it.containsKey("county") && it.county != personAddress.county) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (!it.containsKey("countyUpdate") && it.containsKey("countyISOCode") && additionalProperty.countyISOCode != it.countyISOCode) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (!it.containsKey("countyUpdate") && it.containsKey("countyDescription") && additionalProperty.countyDescription != it.countyDescription) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("deliveryPoint") && personAddress.deliveryPoint != it.deliveryPoint) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("carrierRoute") && personAddress.carrierRoute != it.carrierRoute) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("correctionDigit") && personAddress.correctionDigit != it.correctionDigit) {
                                changeToInactiveStatus = true
                                break;
                            }
                            break;
                    }
                    if (changeToInactiveStatus) {
                        personAddress.statusIndicator = 'I'
                        log.debug "Inactivating address:" + personAddress.toString()
                        personAddressService.update(personAddress)
                        List<PersonGeographicAreaAddress> personGeographicAreaAddresses = geographicAreaAddresses.findAll {
                            it.addressType.code == personAddress.addressType.code
                        }
                        personGeographicAreaAddresses.each { personGeographicAreaAddress ->
                            personGeographicAreaAddress.statusIndicator = personAddress.statusIndicator
                            personGeographicAreaAddressService.update(personGeographicAreaAddress)
                        }
                    } else {
                        List<PersonGeographicAreaAddress> personGeographicAreaAddresses = geographicAreaAddresses.findAll {
                            it.addressType.code == personAddress.addressType.code
                        }
                        if (it.containsKey("geographicAreaGuids") && it.get("geographicAreaGuids") instanceof List) {
                            List geographicAreaGuids = it.get("geographicAreaGuids")
                            if (personGeographicAreaAddresses.isEmpty()) {
                                createOrUpdateGeographicAddress(it, personAddress)
                            } else {
                                if (geographicAreaGuids.isEmpty()) {
                                    // Inactive geographic area address
                                    personGeographicAreaAddresses.each { personGeographicAreaAddress ->
                                        personGeographicAreaAddress.statusIndicator = 'I'
                                        personGeographicAreaAddressService.update(personGeographicAreaAddress)
                                    }

                                } else {
                                    // compare geographic area address
                                    Map geographicAreaGuidToGeographicAreaRuleMap = geographicAreaCompositeService.getGeographicAreaGuidToGeographicAreaRuleMap(geographicAreaGuids)

                                    personGeographicAreaAddresses.each { personGeographicAreaAddress ->

                                        def exitGeographicAreaMap = geographicAreaGuidToGeographicAreaRuleMap.find { key, value ->
                                            personGeographicAreaAddress.division.code == value.division.code && personGeographicAreaAddress.region.code == value.region.code
                                        }

                                        if (exitGeographicAreaMap) {
                                            geographicAreaGuids.remove(exitGeographicAreaMap.key)
                                        } else {
                                            personGeographicAreaAddress.statusIndicator = 'I'
                                            personGeographicAreaAddressService.update(personGeographicAreaAddress)
                                        }
                                    }

                                    it.put("geographicAreaGuids", geographicAreaGuids)
                                    createOrUpdateGeographicAddress(it, personAddress)

                                }

                            }
                        }
                        // remove from the list, if there is no changes of address
                        newAddresses.remove(it)
                    }
                }
            } else {
                personAddress.statusIndicator = 'I'
                log.debug "Inactivating address:" + personAddress.toString()
                personAddressService.update(personAddress)
                List<PersonGeographicAreaAddress> personGeographicAreaAddresses = geographicAreaAddresses.findAll {
                    it.addressType.code == personAddress.addressType.code
                }
                personGeographicAreaAddresses.each { personGeographicAreaAddress ->
                    personGeographicAreaAddress.statusIndicator = personAddress.statusIndicator
                    personGeographicAreaAddressService.update(personGeographicAreaAddress)
                }
            }
        }

        return newAddresses
    }


    def createOrUpdateAdditionalId(PersonIdentificationNameCurrent personIdentification, Map credential, String dataOrigin) {
        def idType = AdditionalIdentificationType.findByCode("ELV8")
        List<AdditionalID> existingIds = AdditionalID.fetchByPidmInListAndAdditionalIdentificationTypeInList([personIdentification.pidm], ["ELV8"])
        AdditionalID existingId
        if (existingIds.size() > 0) {
            existingId = existingIds.get(0)
            existingId.additionalId = credential?.value
        } else
            existingId = new AdditionalID(pidm: personIdentification.pidm,
                    additionalIdentificationType: idType,
                    additionalId: credential?.value,
                    dataOrigin: dataOrigin)
        additionalIDService.createOrUpdate(existingId)
    }


    private PersonBasicPersonBase updatePersonBasicPersonBase(pidm, personBaseList, newPersonIdentificationName, personBaseData, alternateNames, ssn, dataOrigin) {
        PersonBasicPersonBase newPersonBase

        if (personBaseList.size() == 0) {
            //if there is no person base then create new PersonBase
            newPersonBase = createPersonBasicPersonBase(newPersonIdentificationName, personBaseData, dataOrigin)
        } else {
            personBaseList.each { personBase ->
                //Copy personBase attributes into person map from Primary names object.
                if (personBaseData.containsKey("namePrefix")) {
                    personBase.namePrefix = personBaseData.get("namePrefix")
                }
                if (personBaseData.containsKey("nameSuffix")) {
                    personBase.nameSuffix = personBaseData.get("nameSuffix")
                }
                if (personBase.ssn) {
                    if (integrationConfigurationService.canUpdatePersonSSN()) {
                        personBase.ssn = ssn
                    }
                } else {
                    personBase.ssn = ssn
                }
                if (personBaseData.containsKey("sex")) {
                    personBase.sex = personBaseData.get("sex")
                }
                if (personBaseData.containsKey("religion")) {
                    personBase.religion = personBaseData.get("religion")
                }
                if (personBaseData.containsKey("ethnicity")) {
                    if (personBaseData.get("ethnicity") instanceof String) {
                        personBase.ethnic = personBaseData.get("ethnicity")
                    } else {
                        personBase.ethnic = null
                    }
                }
                if (personBaseData.containsKey("maritalStatus")) {
                    if (personBaseData.get("maritalStatus") instanceof MaritalStatus) {
                        personBase.maritalStatus = personBaseData.get("maritalStatus")
                    } else {
                        personBase.maritalStatus = null
                    }
                }
                if (personBaseData.containsKey("citizenType")) {
                    if (personBaseData.get("citizenType") instanceof CitizenType) {
                        personBase.citizenType = personBaseData.get("citizenType")
                    } else {
                        personBase.citizenType = null
                    }
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
                if (personBaseData.containsKey("birthDate")) {
                    personBase.birthDate = personBaseData.get("birthDate")
                }
                if (personBaseData.containsKey("deadDate")) {
                    personBase.deadIndicator = personBaseData.get("deadDate") ? 'Y' : null
                    personBase.deadDate = personBaseData.get("deadDate")
                }
                if (personBase.deadDate != null && personBase.birthDate != null && personBase.deadDate.before(personBase.birthDate)) {
                    throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException('dateDeceased.invalid', null))
                }
                if (personBaseData.get("confidIndicator")) {
                    personBase.confidIndicator = personBaseData.get("confidIndicator")
                }
                newPersonBase = personBasicPersonBaseService.update(personBase)
            }
        }
        return newPersonBase
    }


    private PersonBasicPersonBase createPersonBasicPersonBase(
            def newPersonIdentificationName, Map personBaseData, String dataOrigin) {
        Map person = [:]
        PersonBasicPersonBase newPersonBase

        //Copy personBase attributes into person map from Primary names object.
        person.put('dataOrigin', dataOrigin)
        if (personBaseData.containsKey("namePrefix")) {
            person.namePrefix = personBaseData.get("namePrefix")
        }
        if (personBaseData.containsKey("nameSuffix")) {
            person.nameSuffix = personBaseData.get("nameSuffix")
        }
        if (personBaseData.containsKey("birthDate")) {
            person.birthDate = personBaseData.get("birthDate")
        }
        if (personBaseData.containsKey("deadDate")) {
            person.birthDate = personBaseData.get("deadDate")
        }
        if (personBaseData.containsKey("confidIndicator")) {
            person.confidIndicator = personBaseData.get("confidIndicator")
        }
        if (personBaseData.containsKey("sex")) {
            person.sex = personBaseData.get("sex")
        }
        if (personBaseData.containsKey("religion")) {
            person.sex = personBaseData.get("religion")
        }
        if (personBaseData.containsKey("citizenType")) {
            if (personBaseData.get("citizenType") instanceof CitizenType) {
                person.citizenType = personBaseData.get("citizenType")
            }
        }


        if (personBaseData.containsKey("deadDate") && personBaseData.containsKey("birthDate") && personBaseData.get("deadDate")?.before(personBaseData.get("birthDate"))) {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException('dateDeceased.invalid', null))
        }
        person.put('deadIndicator', personBaseData.get("deadDate") ? 'Y' : null)
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


    private def prepareDataMapForSinglePerson(Map dataMapForPerson) {
        PersonIdentificationNameCurrent personIdentificationNameCurrent
        PersonBasicPersonBase personBase = dataMapForPerson.get("personBase")
        if (dataMapForPerson.containsKey("personIdentificationNameCurrent")) {
            personIdentificationNameCurrent = dataMapForPerson.get("personIdentificationNameCurrent")
            def bannerNameTypeToHedmNameTypeMap = getBannerNameTypeToHedmNameTypeMap()
            def nameTypeCodeToGuidMap = personNameTypeCompositeService.getNameTypeCodeToGuidMap(bannerNameTypeToHedmNameTypeMap.keySet())
            List<PersonIdentificationNameAlternate> personAlternateNames = personIdentificationNameAlternateService.fetchAllMostRecentlyCreated([personIdentificationNameCurrent?.pidm], bannerNameTypeToHedmNameTypeMap?.keySet()?.toList())
            dataMapForPerson.put('bannerNameTypeToHedmNameTypeMap', bannerNameTypeToHedmNameTypeMap)
            dataMapForPerson.put('nameTypeCodeToGuidMap', nameTypeCodeToGuidMap)
            dataMapForPerson.put('personAlternateNames', personAlternateNames)

            def personCredentials
            personCredentialCompositeService.fetchPersonsCredentialDataAndPutInMap([personIdentificationNameCurrent.pidm], dataMapForPerson)
            if (dataMapForPerson.pidmToCredentialsMap.containsKey(personIdentificationNameCurrent.pidm)) {
                personCredentials = dataMapForPerson.pidmToCredentialsMap.get(personIdentificationNameCurrent.pidm)
            }
            personCredentials << [type: CredentialType.BANNER_ID, value: personIdentificationNameCurrent.bannerId]
            if (personBase?.ssn) {
                personCredentials << [type: CredentialType.SOCIAL_SECURITY_NUMBER, value: dataMapForPerson.get("personBase").ssn]
            }
            dataMapForPerson.get("additionalIds")?.each {
                personCredentials << [type: CredentialType.ELEVATE_ID, value: it.additionalId]
            }
            dataMapForPerson << ["personCredentials": personCredentials]

            //religion
            Map relCodeToGuidMap = [:]
            if (personBase?.religion) {
                relCodeToGuidMap = religionCompositeService.fetchGUIDs([personBase?.religion.code])
                dataMapForPerson << ["religionGuid": relCodeToGuidMap.get(personBase?.religion.code)]
            }

            //citizenship Statuses
            Map<String, String> ctCodeToGuidMap = [:]
            if (personBase?.citizenType) {
                ctCodeToGuidMap = citizenshipStatusCompositeService.fetchGUIDs([personBase.citizenType.code])
                dataMapForPerson << ["citizenTypeGuid": ctCodeToGuidMap.get(personBase.citizenType.code)]
            }

            //roles
            List personRoles
            fetchPersonsRoleDataAndPutInMap([personIdentificationNameCurrent.pidm], dataMapForPerson)
            if (dataMapForPerson.pidmToRolesMap.containsKey(personIdentificationNameCurrent.pidm)) {
                personRoles = dataMapForPerson.pidmToRolesMap.get(personIdentificationNameCurrent.pidm)
            }
            dataMapForPerson << ["personRoles": personRoles]
        }

        // maritalStatus
        if (personBase.maritalStatus) {
            dataMapForPerson << ["maritalStatusGuid": getMaritalStatusCodeToGuidMap([personBase.maritalStatus.code]).get(personBase.maritalStatus.code)]
        }

        // Emails
        Map<String, String> bannerEmailTypeToHedmEmailTypeMap = getBannerEmailTypeToHedmEmailTypeMap()
        def emailTypeCodeToGuidMap = emailTypeCompositeService.getEmailTypeCodeToGuidMap(bannerEmailTypeToHedmEmailTypeMap.keySet())
        dataMapForPerson << ["bannerEmailTypeToHedmEmailTypeMap": bannerEmailTypeToHedmEmailTypeMap]
        dataMapForPerson << ["emailTypeCodeToGuidMap": emailTypeCodeToGuidMap]

        //Ethnicity
        Map usEthnicCodeGuidMap = ethnicityCompositeService.fetchGUIDsForUnitedStatesEthnicCodes()
        dataMapForPerson << ["usEthnicCodeGuid": usEthnicCodeGuidMap.get(dataMapForPerson.get("personBase")?.ethnic)]

        //Races
        if (dataMapForPerson.get("personRaces")) {
            List<PersonRace> personRaces = dataMapForPerson.get("personRaces")
            Map raceCodeToGuidMap = raceCompositeService.getRaceCodeToGuidMap(personRaces.race.flatten() as Set)
            dataMapForPerson << ["raceCodeToGuidMap": raceCodeToGuidMap]
        }

        //countryBirth
        VisaInternationalInformation visaInternationalInformation = dataMapForPerson.get("visaInternationalInformation")
        if (visaInternationalInformation) {
            dataMapForPerson << ["pidmToOriginCountryMap": visaInternationalInformation]
            if (visaInternationalInformation.passportId) {
                dataMapForPerson << ["passport": visaInternationalInformation]
                dataMapForPerson << ["isInstitutionUsingISO2CountryCodes": integrationConfigurationService.isInstitutionUsingISO2CountryCodes()]
            }
            Map codeToNationMap = nationCompositeService.fetchAllByCodesInList([visaInternationalInformation.nationBirth,
                                                                                visaInternationalInformation.nationLegal,
                                                                                visaInternationalInformation.nationIssue])
            dataMapForPerson << ["codeToNationMap": codeToNationMap]
        }

        //personInterest
        def personInterests = dataMapForPerson.get("personInterests")
        if (personInterests) {
            Map interestCodeToGuidMap = interestCompositeService.getInterestCodeToGuidMap(personInterests.interest.code)
            dataMapForPerson << ["interestCodeToGuidMap": interestCodeToGuidMap]
        }

        //person LangCode
        Map pidmToLanguageCodeMap = [:]
        Map stvlangCodeToISO3LangCodeMap = [:]
        if (visaInternationalInformation?.language) {
            pidmToLanguageCodeMap.put(personIdentificationNameCurrent.pidm, visaInternationalInformation.language.code)
            stvlangCodeToISO3LangCodeMap = crossReferenceRuleService.getISO3LanguageCodes([visaInternationalInformation.language.code])
            dataMapForPerson << ["iso3LanguageCode": stvlangCodeToISO3LangCodeMap.get(pidmToLanguageCodeMap.get(personIdentificationNameCurrent.pidm))]
        }

        // addresses
        if (dataMapForPerson.containsKey("personAddresses") && dataMapForPerson.get("personAddresses").size() > 0) {
            def personAddresses = dataMapForPerson.get("personAddresses")
            dataMapForPerson.put("bannerAddressTypeToHedmAddressTypeMap", getBannerAddressTypeToHedmAddressTypeMap())
            Map addressTypeCodeToGuidMap = addressTypeCompositeService.getAddressTypeCodeToGuidMap(dataMapForPerson.bannerAddressTypeToHedmAddressTypeMap?.keySet())
            Map personAddressSurrogateIdToGuidMap = [:]
            List<PersonAddressAdditionalProperty> entities = personAddressAdditionalPropertyService.fetchAllBySurrogateIds(personAddresses.id)
            entities?.each {
                personAddressSurrogateIdToGuidMap.put(it.id, it.addressGuid)
            }

            dataMapForPerson.put("personAddresses", dataMapForPerson.get("personAddresses"))
            dataMapForPerson.put("addressTypeCodeToGuidMap", addressTypeCodeToGuidMap)
            dataMapForPerson.put("personAddressSurrogateIdToGuidMap", personAddressSurrogateIdToGuidMap)

        }

        // phones
        if (dataMapForPerson.containsKey("personPhones") && dataMapForPerson.get("personPhones").size() > 0) {
            dataMapForPerson.put("bannerPhoneTypeToHedmPhoneTypeMap", getBannerPhoneTypeToHedmPhoneTypeMap())
            dataMapForPerson.put("personPhones", dataMapForPerson.get("personPhones"))
            // Get GUID for each PhoneType
            Map phoneTypeCodeToGuidMap = phoneTypeCompositeService.getPhoneTypeCodeToGuidMap(dataMapForPerson.bannerPhoneTypeToHedmPhoneTypeMap?.keySet())
            // Put in Map
            dataMapForPerson.put("phoneTypeCodeToGuidMap", phoneTypeCodeToGuidMap)
        }

        return dataMapForPerson
    }


    private Map processQueryWithPostRequest(final Map requestParams) {
        Map requestProcessingResult
        String contentType = getRequestRepresentation()
        log.debug "Content-Type: ${contentType}"
        log.debug("Duplicate Check")
        requestProcessingResult = searchForMatchingPersons(requestParams)
        return requestProcessingResult
    }


    protected getPidmsOfPopulationExtract(final Map requestParams) {
        String guidOrDomainKey = getPopSelGuidOrDomainKey(requestParams)
        return personFilterCompositeService.fetchPidmsOfPopulationExtract(guidOrDomainKey, requestParams.sort?.trim(), requestParams.order?.trim(), requestParams.max?.trim()?.toInteger() ?: 0, requestParams.offset?.trim()?.toInteger() ?: 0)
    }


    private def getActivePhones(Integer pidm, List phones) {
        Map bannerPhoneTypeToHedmPhoneTypeMap = getBannerPhoneTypeToHedmPhoneTypeMap()

        List<PersonTelephone> personTelephones = personTelephoneService.fetchAllActiveByPidmInListAndTelephoneTypeCodeInList([pidm], bannerPhoneTypeToHedmPhoneTypeMap.keySet())

        personTelephones.each { personPhone ->

            def activeRequestPhones = phones.findAll { it ->
                it.telephoneType?.code == personPhone.telephoneType.code
            }

            if (activeRequestPhones.size() > 0) {
                activeRequestPhones.each {
                    Boolean changeToInactiveStatus = false
                    switch (it.telephoneType?.code) {
                        default:

                            if (it.containsKey("phoneArea") && it.phoneArea != personPhone.phoneArea) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("countryPhone") && it.countryPhone != personPhone.countryPhone) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("phoneNumber") && it.phoneNumber != personPhone.phoneNumber) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("phoneExtension") && it.phoneExtension != personPhone.phoneExtension) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("primaryIndicator") && it.primaryIndicator != personPhone.primaryIndicator) {
                                changeToInactiveStatus = true
                                break;
                            }

                            break;
                    }
                    if (changeToInactiveStatus) {
                        personPhone.statusIndicator = 'I'
                        log.debug "Inactivating address:" + personPhone.toString()
                        personTelephoneService.update(personPhone)

                    } else {

                        // remove from the list, if there is no changes of address
                        phones.remove(it)
                    }
                }
            } else {
                personPhone.statusIndicator = 'I'
                log.debug "Inactivating address:" + personPhone.toString()
                personTelephoneService.update(personPhone)

            }
        }

        return phones
    }


    private def createPhones(List phones, Integer pidm) {
        phones.each { phoneMap ->
            phoneMap.put("pidm", pidm)
        }

        return personTelephoneService.create(phones)

    }


    protected def createOrUpdateAddress(List domainPropertiesMapList, Integer pidm) {

        List<PersonAddress> personAddresses = []
        domainPropertiesMapList.each {
            //Person Address
            PersonAddress personAddress = personAddressService.getDomainClass().newInstance()
            bindPersonAddress(personAddress, it, pidm)
            personAddress = personAddressService.create(personAddress)

            if (it.countyISOCode || it.countyDescription) {
                PersonAddressAdditionalProperty addressAdditionalProperty = personAddressAdditionalPropertyService.get(personAddress.id)
                addressAdditionalProperty.countyISOCode = it.countyISOCode
                addressAdditionalProperty.countyDescription = it.countyDescription
                personAddressAdditionalPropertyService.update(addressAdditionalProperty)
            }

            //Geographic Address
            createOrUpdateGeographicAddress(it, personAddress)
            personAddresses.add(personAddress)
        }

        return personAddresses
    }


    protected def createOrUpdateGeographicAddress(Map addressMap, PersonAddress personAddress) {

        if (addressMap.containsKey("geographicAreaGuids") && addressMap.get("geographicAreaGuids") instanceof List) {
            List geographicAreaGuids = addressMap.get("geographicAreaGuids")
            if (geographicAreaGuids) {
                Map geographicAreaGuidToGeographicAreaRuleMap = geographicAreaCompositeService.getGeographicAreaGuidToGeographicAreaRuleMap(geographicAreaGuids)
                geographicAreaGuids.each {
                    PersonGeographicAreaAddress personGeographicAreaAddress = personGeographicAreaAddressService.getDomainClass().newInstance()
                    bindPersonGeographicAreaAddress(personGeographicAreaAddress, it, geographicAreaGuidToGeographicAreaRuleMap, personAddress)
                    personGeographicAreaAddressService.create(personGeographicAreaAddress)
                }
            }
        }
    }


    protected void bindPersonGeographicAreaAddress(PersonGeographicAreaAddress personGeographicAreaAddress, String guid, Map guidToGeogrphicAreasMap, PersonAddress personAddress) {
        if (guidToGeogrphicAreasMap.containsKey(guid)) {
            GeographicRegionRule geographicRegionRule = guidToGeogrphicAreasMap.get(guid)
            personGeographicAreaAddress.division = geographicRegionRule.division
            personGeographicAreaAddress.region = geographicRegionRule.region
        } else {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("geographicArea.not.found", []))
        }
        personGeographicAreaAddress.pidm = personAddress.pidm
        personGeographicAreaAddress.addressType = personAddress.addressType
        personGeographicAreaAddress.sequenceNumber = personAddress.sequenceNumber
        personGeographicAreaAddress.toDate = personAddress.toDate
        personGeographicAreaAddress.fromDate = personAddress.fromDate
        personGeographicAreaAddress.sourceIndicator = 'S'
        personGeographicAreaAddress.userData = personAddress.userData
    }


    private void bindPersonAddress(PersonAddress personAddress, Map domainPropertiesMap, Integer pidm) {
        personAddress.pidm = pidm
        if (domainPropertiesMap.containsKey('bannerAddressType')) {
            AddressType addressType = AddressType.findByCode(domainPropertiesMap.bannerAddressType)
            if (addressType) {
                personAddress.addressType = addressType
            } else {
                //throw an exception
                throw new ApplicationException("Person", new BusinessLogicValidationException("addressType.not.found.message", []))
            }
        }

        if (domainPropertiesMap.containsKey('nationISOCode')) {

            Nation nation = fetchNationByScondISO(domainPropertiesMap.nationISOCode)
            if (nation) {
                personAddress.nation = nation
            }
        }

        if (domainPropertiesMap.containsKey('stateCode') && domainPropertiesMap.stateCode) {
            State state = State.findByCode(domainPropertiesMap.stateCode)
            if (state) {
                personAddress.state = state
            } else {
                throw new ApplicationException("Person", new BusinessLogicValidationException("state.not.found.message", []))
            }
        } else if (domainPropertiesMap.containsKey('stateDescription') && domainPropertiesMap.stateDescription) {
            State state = State.findByDescription(domainPropertiesMap.stateDescription)
            if (state) {
                personAddress.state = state
            } else {
                throw new ApplicationException("Person", new BusinessLogicValidationException("state.not.found.message", []))
            }
        }

        bindData(personAddress, domainPropertiesMap, [:])
    }


    protected def getDefalutZipCode() {
        IntegrationConfiguration intConfig = super.findAllByProcessCodeAndSettingName(GeneralValidationCommonConstants.PROCESS_CODE, ZIP_DEFAULT)[0]
        if (!intConfig) {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("goriccr.not.found.message", [ZIP_DEFAULT]))
        }
        return intConfig.value
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

            if (personBase.maritalStatus) {
                dataMapForPerson << ["maritalStatusGuid": dataMap.maritalStatusCodeToGuidMap.get(personBase.maritalStatus.code)]
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

        // Get GUID for each MaritalStatus
        Set<String> msCodes = pidmToPersonBaseMap?.values().maritalStatus?.code?.flatten()?.unique()
        def maritalStatusCodeToGuidMap = getMaritalStatusCodeToGuidMap(msCodes)

        // Get GUIDs for US ethnic codes (SPBPERS_ETHN_CDE)
        Map<String, String> usEthnicCodeToGuidMap = ethnicityCompositeService.fetchGUIDsForUnitedStatesEthnicCodes()

        // Put in Map
        dataMap.put("pidmToPersonBaseMap", pidmToPersonBaseMap)
        dataMap.put("maritalStatusCodeToGuidMap", maritalStatusCodeToGuidMap)
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


    private def getMaritalStatusCodeToGuidMap(Collection<String> codes) {
        Map<String, String> codeToGuidMap = [:]
        if (codes) {
            def rows = maritalStatusService.fetchAllWithGuidByCodeInList(codes)
            rows?.each {
                MaritalStatus nameType = it.maritalStatus
                GlobalUniqueIdentifier globalUniqueIdentifier = it.globalUniqueIdentifier
                codeToGuidMap.put(nameType.code, globalUniqueIdentifier.guid)
            }
        }
        return codeToGuidMap
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

    /**
     * JSON Array with JSON object elements will be converted to list of maps by RESTful API controller
     * (Each JSON object inside array is represented as Map).
     *
     * @param list
     * @return
     */
    protected def getListOfMaps(final def list) {
        def listOfMaps = []
        if (list instanceof List) {
            for (elem in list) {
                if (elem instanceof Map) {
                    listOfMaps << elem
                }
            }
        }
        return listOfMaps
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

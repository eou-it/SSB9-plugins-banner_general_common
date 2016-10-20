/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.IntegrationConfigurationService
import net.hedtech.banner.general.overall.ThirdPartyAccessService
import net.hedtech.banner.general.overall.VisaInternationalInformation
import net.hedtech.banner.general.person.*
import net.hedtech.banner.general.person.ldm.v6.*
import net.hedtech.banner.general.system.*
import net.hedtech.banner.general.system.ldm.*
import net.hedtech.banner.general.system.ldm.v4.EmailTypeDetails
import net.hedtech.banner.general.system.ldm.v6.AddressTypeDecorator
import net.hedtech.banner.general.system.ldm.v6.CitizenshipStatusV6
import net.hedtech.banner.general.utility.DateConvertHelperService
import net.hedtech.banner.general.utility.IsoCodeService
import org.springframework.transaction.annotation.Transactional

import java.sql.Timestamp

/**
 * RESTful APIs for Ellucian Ethos Data Model "persons" V6.
 */
@Transactional
class PersonV6CompositeService extends AbstractPersonCompositeService {


    def settingNameSSN = 'PERSON.UPDATESSN'



    IntegrationConfigurationService integrationConfigurationService
    IsoCodeService isoCodeService
    EmailTypeService emailTypeService
    ThirdPartyAccessService thirdPartyAccessService
    CitizenTypeService citizenTypeService

    @Override
    protected String getPopSelGuidOrDomainKey(final Map requestParams) {
        return requestParams.get("personFilter")
    }


    @Override
    protected def prepareCommonMatchingRequest(final Map content) {
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


    protected Map processListApiRequest(final Map requestParams) {
        String sortField = requestParams.sort?.trim()
        String sortOrder = requestParams.order?.trim()
        int max = requestParams.max?.trim()?.toInteger() ?: 0
        int offset = requestParams.offset?.trim()?.toInteger() ?: 0

        List<Integer> pidms
        int totalCount = 0

        if (requestParams.containsKey("role")) {
            String role = requestParams.role?.trim()
            log.debug "Fetching persons with role $role ...."
            def returnMap
            if (role == RoleName.INSTRUCTOR.versionToEnumMap["v6"]) {
                returnMap = userRoleCompositeService.fetchFaculties(sortField, sortOrder, max, offset)
            } else if (role == RoleName.STUDENT.versionToEnumMap["v6"]) {
                returnMap = userRoleCompositeService.fetchStudents(sortField, sortOrder, max, offset)
                setStudentPidmsInThreadLocal(returnMap?.pidms)
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
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("role.supported.v6", null))
            }
            pidms = returnMap?.pidms
            totalCount = returnMap?.totalCount
            log.debug "${totalCount} persons found with role $role."
        } else if (requestParams.containsKey("credential.type") || requestParams.containsKey("credential.value")) {
            if (!requestParams.containsKey("credential.type")) {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("creadential.type.required", null))
            }

            if (!requestParams.containsKey("credential.value")) {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("creadential.id.required", null))
            }

            String credentialType = requestParams.get("credential.type")?.trim()
            String credentialValue = requestParams.get("credential.value")?.trim()

            def entities
            def mapForSearch
            if(credentialType == CredentialType.BANNER_ID.versionToEnumMap["v6"]){
                mapForSearch = [bannerId: credentialValue]
                entities = personAdvancedSearchViewService.fetchAllByCriteria(mapForSearch, sortField, sortOrder, max, offset)
                totalCount = personAdvancedSearchViewService.countByCriteria(mapForSearch)
            }else if(credentialType == CredentialType.BANNER_USER_NAME.versionToEnumMap["v6"]){
                mapForSearch = [externalUser: credentialValue]
                entities = thirdPartyAccessService.fetchAllByCriteria(mapForSearch, max, offset)
                totalCount = thirdPartyAccessService.countByCriteria(mapForSearch)
            }else {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("creadential.type.invalid", null))
            }

            pidms = entities?.collect { it.pidm }
        } else if (requestParams.containsKey("lastName") || requestParams.containsKey("firstName") || requestParams.containsKey("middleName") || requestParams.containsKey("lastNamePrefix") || requestParams.containsKey("title") || requestParams.containsKey("pedigree")) {
            def mapForSearch = [:]
            if (requestParams.containsKey("lastName")) {
                mapForSearch = [lastName: requestParams.get("lastName")]
            } else if (requestParams.containsKey("firstName")) {
                mapForSearch = [firstName: requestParams.get("firstName")]
            } else if (requestParams.containsKey("middleName")) {
                mapForSearch = [middleName: requestParams.get("middleName")]
            } else if (requestParams.containsKey("lastNamePrefix")) {
                mapForSearch = [surnamePrefix: requestParams.get("lastNamePrefix")]
            } else if (requestParams.containsKey("title")) {
                mapForSearch = [namePrefix: requestParams.get("title")]
            } else if (requestParams.containsKey("pedigree")) {
                mapForSearch = [nameSuffix: requestParams.get("pedigree")]
            }
            def entities = personAdvancedSearchViewService.fetchAllByCriteria(mapForSearch, sortField, sortOrder, max, offset)
            pidms = entities?.collect { it.pidm }
            totalCount = personAdvancedSearchViewService.countByCriteria(mapForSearch)
        } else if (requestParams.containsKey("personFilter")) {
            Map returnMap = getPidmsOfPopulationExtract(requestParams)
            pidms = returnMap?.pidms
            totalCount = returnMap?.totalCount
            log.debug "${totalCount} persons in population extract."
        } else {
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("role.required", null))
        }

        return [pidms: pidms, totalCount: totalCount]
    }


    protected void fetchDataAndPutInMap_VersonSpecific(List<Integer> pidms, Map dataMap) {
        fetchPersonsBiographicalDataAndPutInMap_VersionSpecific(pidms, dataMap)
        fetchPersonsAlternateNameDataAndPutInMap_VersionSpecific(pidms, dataMap)
        fetchPersonsAddressDataAndPutInMap_VersionSpecific(pidms, dataMap)
        fetchPersonsPhoneDataAndPutInMap_VersionSpecific(pidms, dataMap)
        fetchPersonsEmailDataAndPutInMap_VersionSpecific(pidms, dataMap)
        dataMap.put("isInstitutionUsingISO2CountryCodes", integrationConfigurationService.isInstitutionUsingISO2CountryCodes())
        fetchPersonsInterestDataAndPutInMap(pidms, dataMap)
        fetchPersonsPassportDataAndPutInMap(pidms, dataMap)
        fetchPersonsMaritalStatusDataAndPutInMap(dataMap)
    }


    private void fetchPersonsBiographicalDataAndPutInMap_VersionSpecific(List<Integer> pidms, Map dataMap) {
        // Get GUIDs for CitizenTypes
        List<String> citizenTypeCodes = dataMap.pidmToPersonBaseMap?.values()?.findResults {
            it.citizenType?.code
        }.unique()
        Map<String, String> ctCodeToGuidMap = [:]
        if (citizenTypeCodes) {
            log.debug "Getting GUIDs for CitizenType codes $citizenTypeCodes..."
            ctCodeToGuidMap = citizenshipStatusCompositeService.fetchGUIDs(citizenTypeCodes)
            log.debug "Got ${ctCodeToGuidMap?.size() ?: 0} GUIDs for given CitizenType codes"
        }

        // Get GUIDs for regligion codes
        List<String> religionCodes = dataMap.pidmToPersonBaseMap?.values()?.findResults {
            it.religion?.code
        }.unique()
        Map<String, String> relCodeToGuidMap = [:]
        if (religionCodes) {
            log.debug "Getting GUIDs for religion codes $religionCodes..."
            relCodeToGuidMap = religionCompositeService.fetchGUIDs(religionCodes)
            log.debug "Got ${relCodeToGuidMap?.size() ?: 0} GUIDs for given religion codes"
        }

        // Put in Map
        dataMap.put("ctCodeToGuidMap", ctCodeToGuidMap)
        dataMap.put("relCodeToGuidMap", relCodeToGuidMap)
    }


    protected def getBannerNameTypeToHedmNameTypeMap() {
        return personNameTypeCompositeService.getBannerNameTypeToHedmV6NameTypeMap()
    }


    private void fetchPersonsAlternateNameDataAndPutInMap_VersionSpecific(List<Integer> pidms, Map dataMap) {
        def nameTypeCodeToGuidMap = personNameTypeCompositeService.getNameTypeCodeToGuidMap(dataMap.bannerNameTypeToHedmNameTypeMap.keySet())
        log.debug "GUIDs for ${nameTypeCodeToGuidMap.keySet()} are ${nameTypeCodeToGuidMap.values()}"

        // Put in Map
        dataMap.put("nameTypeCodeToGuidMap", nameTypeCodeToGuidMap)
    }


    @Override
    protected List<RoleName> getRolesRequired() {
        return [RoleName.STUDENT, RoleName.INSTRUCTOR, RoleName.EMPLOYEE, RoleName.VENDOR, RoleName.ALUMNI, RoleName.PROSPECTIVE_STUDENT, RoleName.ADVISOR]
    }


    protected def getBannerAddressTypeToHedmAddressTypeMap() {
        return addressTypeCompositeService.getBannerAddressTypeToHedmV6AddressTypeMap()
    }


    private void fetchPersonsAddressDataAndPutInMap_VersionSpecific(List<Integer> pidms, Map dataMap) {
        // Get GUID for each AddressType
        Map<String, String> addressTypeCodeToGuidMap = addressTypeCompositeService.getAddressTypeCodeToGuidMap(dataMap.bannerAddressTypeToHedmAddressTypeMap.keySet())
        log.debug "GUIDs for ${addressTypeCodeToGuidMap.keySet()} are ${addressTypeCodeToGuidMap.values()}"

        // Get GUID for each PersonAddress
        List<Long> personAddressSurrogateIds = dataMap.pidmToAddressesMap?.values().id.flatten().unique()
        Map<Long, String> personAddressSurrogateIdToGuidMap = getPersonAddressSurrogateIdToGuidMap(personAddressSurrogateIds)

        // Put in Map
        dataMap.put("addressTypeCodeToGuidMap", addressTypeCodeToGuidMap)
        dataMap.put("personAddressSurrogateIdToGuidMap", personAddressSurrogateIdToGuidMap)
    }

    private void fetchPersonsMaritalStatusDataAndPutInMap(Map dataMap) {
        maritalStatusCompositeService.getMaritalStatusCodeToGuidMap(bannerMaritalStatusToHedmMaritalStatusMap.keySet())
        Map<String, String> bannerMaritalStatusToHedmMaritalStatusMap = getBannerMaritalStatusToHedmMaritalStatusMap()
        def maritalStatusCodeToGuidMap = maritalStatusCompositeService.getMaritalStatusCodeToGuidMap(bannerMaritalStatusToHedmMaritalStatusMap.keySet())

        // Put in Map
        dataMap.put("bannerMaritalStatusToHedmMaritalStatusMap", bannerMaritalStatusToHedmMaritalStatusMap)
        dataMap.put("maritalStatusCodeToGuidMap", maritalStatusCodeToGuidMap)
    }


    protected def getBannerPhoneTypeToHedmPhoneTypeMap() {
        return phoneTypeCompositeService.getBannerPhoneTypeToHedmV6PhoneTypeMap()
    }

    protected def getBannerMaritalStatusToHedmMaritalStatusMap() {
        return maritalStatusCompositeService.getBannerMaritalStatusToHedmV4MaritalStatusMap()
    }


    private void fetchPersonsPhoneDataAndPutInMap_VersionSpecific(List<Integer> pidms, Map dataMap) {
        // Get GUID for each PhoneType
        Map phoneTypeCodeToGuidMap = phoneTypeCompositeService.getPhoneTypeCodeToGuidMap(dataMap.bannerPhoneTypeToHedmPhoneTypeMap.keySet())

        // Put in Map
        dataMap.put("phoneTypeCodeToGuidMap", phoneTypeCodeToGuidMap)
    }


    protected def getBannerEmailTypeToHedmEmailTypeMap() {
        return emailTypeCompositeService.getBannerEmailTypeToHedmV6EmailTypeMap()
    }


    private void fetchPersonsEmailDataAndPutInMap_VersionSpecific(List<Integer> pidms, Map dataMap) {
        // Get GUIDs for each EmailType
        Map<String, String> emailTypeCodeToGuidMap = emailTypeCompositeService.getEmailTypeCodeToGuidMap(dataMap.bannerEmailTypeToHedmEmailTypeMap.keySet())

        // Put in Map
        dataMap.put("emailTypeCodeToGuidMap", emailTypeCodeToGuidMap)
    }


    protected void prepareDataMapForSinglePerson_VersionSpecific(PersonIdentificationNameCurrent personIdentificationNameCurrent,
                                                                 final Map dataMap, Map dataMapForPerson) {
        dataMapForPerson.put("isInstitutionUsingISO2CountryCodes", dataMap.get("isInstitutionUsingISO2CountryCodes"))

        // Biographical
        PersonBasicPersonBase personBase = dataMap.pidmToPersonBaseMap.get(personIdentificationNameCurrent.pidm)
        if (personBase) {
            if (personBase.citizenType) {
                dataMapForPerson << ["citizenTypeGuid": dataMap.ctCodeToGuidMap.get(personBase.citizenType.code)]
            }
            if (personBase.religion) {
                dataMapForPerson << ["religionGuid": dataMap.relCodeToGuidMap.get(personBase.religion.code)]
            }
        }

        // names
        List<PersonIdentificationNameAlternate> personAlternateNames = dataMap.pidmToAlternateNamesMap.get(personIdentificationNameCurrent.pidm)
        if (personAlternateNames) {
            dataMapForPerson << ["nameTypeCodeToGuidMap": dataMap.nameTypeCodeToGuidMap]
        }

        // addresses
        List<PersonAddress> personAddresses = dataMap.pidmToAddressesMap.get(personIdentificationNameCurrent.pidm)
        if (personAddresses) {
            dataMapForPerson << ["addressTypeCodeToGuidMap": dataMap.addressTypeCodeToGuidMap]
            dataMapForPerson << ["personAddressSurrogateIdToGuidMap": dataMap.personAddressSurrogateIdToGuidMap]
        }

        // phones
        List<PersonTelephone> personTelephoneList = dataMap.pidmToPhonesMap.get(personIdentificationNameCurrent.pidm)
        if (personTelephoneList) {
            dataMapForPerson << ["phoneTypeCodeToGuidMap": dataMap.phoneTypeCodeToGuidMap]
        }

        // emails
        List<PersonEmail> personEmailList = dataMap.pidmToEmailsMap.get(personIdentificationNameCurrent.pidm)
        if (personEmailList) {
            dataMapForPerson << ["emailTypeCodeToGuidMap": dataMap.emailTypeCodeToGuidMap]
        }

        // interests
        List personInterests = dataMap.pidmToInterestsMap.get(personIdentificationNameCurrent.pidm)
        if (personInterests) {
            dataMapForPerson << ["personInterests": personInterests]
            dataMapForPerson << ["interestCodeToGuidMap": dataMap.interestCodeToGuidMap]
        }

        // identity Documents
        VisaInternationalInformation visaIntlInformation = dataMap.pidmToPassportMap.get(personIdentificationNameCurrent.pidm)
        if (visaIntlInformation) {
            dataMapForPerson << ["passport": visaIntlInformation]
            dataMapForPerson << ["codeToNationMap": dataMap.codeToNationMap]
        }

        // languages
        if (dataMap.pidmToLanguageCodeMap.containsKey(personIdentificationNameCurrent.pidm)) {
            dataMapForPerson << ["iso3LanguageCode": dataMap.stvlangCodeToISO3LangCodeMap.get(dataMap.pidmToLanguageCodeMap.get(personIdentificationNameCurrent.pidm))]
        }

        //countryBirth and countryLegal
        VisaInternationalInformation visaIntlInformation1 = dataMap.pidmToOriginCountryMap.get(personIdentificationNameCurrent.pidm)
        if (visaIntlInformation1) {
            dataMapForPerson << ["pidmToOriginCountryMap": visaIntlInformation1]
            dataMapForPerson << ["codeToNationMap": dataMap.codeToNationMap]
        }
    }


    protected def createPersonDataModel(final Map dataMapForPerson) {
        PersonV6 decorator
        def personIdentificationNameCurrent = dataMapForPerson["personIdentificationNameCurrent"]
        if (personIdentificationNameCurrent) {
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
                //dateOfBirth
                if (personBase.birthDate) {
                    decorator.dateOfBirth = personBase.birthDate
                }
                //dateDeceased
                if (personBase.deadDate) {
                    decorator.dateDeceased = personBase.deadDate
                }
                //gender
                if (personBase.sex) {
                    decorator.gender = Gender.getByBannerValue(personBase.sex).versionToEnumMap["v6"]
                }
                //maritalStatus
                if (personBase.maritalStatus) {
                    def bannerMaritalStatusToHedmMaritalStatusMap = dataMapForPerson["bannerMaritalStatusToHedmMaritalStatusMap"]
                    def maritalStatusCodeToGuidMap = dataMapForPerson["maritalStatusCodeToGuidMap"]
                    if (bannerMaritalStatusToHedmMaritalStatusMap.containsKey(personBase.maritalStatus.code)) {
                        decorator.maritalStatus = maritalStatusCompositeService.createMaritalStatusDataModelV4(maritalStatusCodeToGuidMap.get(personBase.maritalStatus.code), personBase.maritalStatus, bannerMaritalStatusToHedmMaritalStatusMap)
                    }
                }
            }

            // Names
            decorator.names = []
            NameV6 nameV6 = createNameV6(personIdentificationNameCurrent, personBase)
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
            if (personCredentials) {
                decorator.credentials = personCredentialCompositeService.createCredentialObjectsV6(personCredentials)
            }

            // Addresses
            List<PersonAddress> personAddresses = dataMapForPerson["personAddresses"]
            if (personAddresses) {
                Map addressTypeCodeToGuidMap = dataMapForPerson["addressTypeCodeToGuidMap"]
                Map bannerAddressTypeToHedmAddressTypeMap = dataMapForPerson["bannerAddressTypeToHedmAddressTypeMap"]
                Map personAddressSurrogateIdToGuidMap = dataMapForPerson["personAddressSurrogateIdToGuidMap"]
                decorator.addresses = []
                personAddresses.each {
                    decorator.addresses << createPersonAddressDecorator(it, personAddressSurrogateIdToGuidMap.get(it.id), addressTypeCodeToGuidMap.get(it.addressType.code), bannerAddressTypeToHedmAddressTypeMap.get(it.addressType.code))
                }
            }

            // Phones
            List<PersonTelephone> personTelephoneListList = dataMapForPerson["personPhones"]
            if (personTelephoneListList) {
                Map phoneTypeCodeToGuidMap = dataMapForPerson["phoneTypeCodeToGuidMap"]
                Map bannerPhoneTypeToHedmPhoneTypeMap = dataMapForPerson["bannerPhoneTypeToHedmPhoneTypeMap"]
                decorator.phones = []
                personTelephoneListList.each {
                    decorator.phones << PhoneV6.createPhoneV6(it, phoneTypeCodeToGuidMap.get(it.telephoneType.code), bannerPhoneTypeToHedmPhoneTypeMap.get(it.telephoneType.code))
                }
            }

            // Emails
            List<PersonEmail> personEmailList = dataMapForPerson["personEmails"]
            if (personEmailList) {
                Map emailTypeCodeToGuidMap = dataMapForPerson["emailTypeCodeToGuidMap"]
                Map bannerEmailTypeToHedmEmailTypeMap = dataMapForPerson["bannerEmailTypeToHedmEmailTypeMap"]
                decorator.emails = []
                personEmailList.each {
                    decorator.emails << createEmailV6(it, it.emailType.code, emailTypeCodeToGuidMap.get(it.emailType.code), bannerEmailTypeToHedmEmailTypeMap.get(it.emailType.code))
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

            // interests
            List personInterests = dataMapForPerson["personInterests"]
            Map<String, String> iterestCodeToGuidMap = dataMapForPerson["interestCodeToGuidMap"]
            if (personInterests) {
                decorator.interests = []
                personInterests.each {
                    decorator.interests << ["id": iterestCodeToGuidMap.get(it.interest.code)]
                }
            }

            // indentityDocuments
            VisaInternationalInformation visaIntlInformation = dataMapForPerson["passport"]
            if (visaIntlInformation) {
                Map codeToNationMap = dataMapForPerson["codeToNationMap"]
                decorator.identityDocuments = []
                decorator.identityDocuments << createIdentityDocumentV6(visaIntlInformation, codeToNationMap.get(visaIntlInformation.nationIssue), dataMapForPerson.get("isInstitutionUsingISO2CountryCodes"))
            }

            // languages
            if (dataMapForPerson["iso3LanguageCode"]) {
                decorator.languages = []
                decorator.languages << ["code": dataMapForPerson["iso3LanguageCode"]]
            }

            //countryBirth & citizenshipCountry
            VisaInternationalInformation visaIntlInformation1 = dataMapForPerson["pidmToOriginCountryMap"]
            if (visaIntlInformation1) {
                Map codeToNationMap = dataMapForPerson["codeToNationMap"]

                //countryOfBirth
                if (visaIntlInformation1.nationBirth) {
                    decorator.countryOfBirth = isoCodeService.getISO3CountryCode(codeToNationMap.get(visaIntlInformation1.nationBirth).scodIso)
                }

                //citizenshipCountry
                if (visaIntlInformation1.nationLegal) {
                    decorator.citizenshipCountry = isoCodeService.getISO3CountryCode(codeToNationMap.get(visaIntlInformation1.nationLegal).scodIso)
                }
            }
        }
        return decorator
    }


    private void fetchPersonsInterestDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        if (!outsideInterestService) {
            // Test mode
            dataMap.put("pidmToInterestsMap", [:])
            dataMap.put("interestCodeToGuidMap", [:])
            return
        }
        // Get SORINTS records for persons
        Map pidmToInterestsMap = [:]
        if (pidms) {
            log.debug "Getting SORINTS records for ${pidms?.size()} PIDMs..."
            List entities = outsideInterestService.fetchAllByPidmInList(pidms)
            entities?.each {
                List personInterests = []
                if (pidmToInterestsMap.containsKey(it.pidm)) {
                    personInterests = pidmToInterestsMap.get(it.pidm)
                } else {
                    pidmToInterestsMap.put(it.pidm, personInterests)
                }
                personInterests.add(it)
            }
        }
        // Get GUIDs for interest codes
        Set<String> interestCodes = pidmToInterestsMap?.values().interest.code.flatten() as Set
        Map interestCodeToGuidMap = [:]
        if (interestCodes) {
            log.debug "Getting GUIDs for interest codes $interestCodes..."
            interestCodeToGuidMap = interestCompositeService.getInterestCodeToGuidMap(interestCodes)
            log.debug "Got ${interestCodeToGuidMap?.size() ?: 0} GUIDs for given interest codes"
        }
        // Put in Map
        dataMap.put("pidmToInterestsMap", pidmToInterestsMap)
        dataMap.put("interestCodeToGuidMap", interestCodeToGuidMap)
    }


    private void fetchPersonsPassportDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        // Get GOBINTL records for persons
        List<VisaInternationalInformation> entities = visaInternationalInformationService.fetchAllByPidmInList(pidms)

        // Passport
        Map pidmToPassportMap = [:]
        Map pidmToLanguageCodeMap = [:]
        Map pidmToOriginCountryMap = [:]
        entities?.each {
            if (it.passportId) {
                pidmToPassportMap.put(it.pidm, it)
            }
            if (it.language?.code) {
                pidmToLanguageCodeMap.put(it.pidm, it.language.code)
            }
            if (it.nationBirth || it.nationLegal) {
                pidmToOriginCountryMap.put(it.pidm, it)
            }
        }

        Set<String> issuingNationCodes = pidmToPassportMap?.values().nationIssue.flatten().unique()
        issuingNationCodes.addAll(pidmToOriginCountryMap?.values().nationBirth.flatten().unique())
        issuingNationCodes.addAll(pidmToOriginCountryMap?.values().nationLegal.flatten().unique())
        // Get STVNATN records for country information
        Map codeToNationMap = [:]
        if (issuingNationCodes) {
            log.debug "Getting nations for country codes $issuingNationCodes..."
            codeToNationMap = nationCompositeService.fetchAllByCodesInList(issuingNationCodes)
            log.debug "Got ${codeToNationMap?.size() ?: 0} nations for given country codes"
        }

        // ISO3 language codes
        Set<String> stvlangCodes = entities.language.code.flatten().unique()
        Map<String, String> stvlangCodeToISO3LangCodeMap = [:]
        if (crossReferenceRuleService) {
            stvlangCodeToISO3LangCodeMap = crossReferenceRuleService.getISO3LanguageCodes(stvlangCodes)
        }

        // Put in Map
        dataMap.put("pidmToPassportMap", pidmToPassportMap)
        dataMap.put("codeToNationMap", codeToNationMap)
        dataMap.put("pidmToLanguageCodeMap", pidmToLanguageCodeMap)
        dataMap.put("stvlangCodeToISO3LangCodeMap", stvlangCodeToISO3LangCodeMap)
        dataMap.put("pidmToOriginCountryMap", pidmToOriginCountryMap)
    }


    private Map getPersonAddressSurrogateIdToGuidMap(Collection<String> personAddressSurrogateIds) {
        Map personAddressSurrogateIdToGuidMap = [:]
        if (personAddressSurrogateIds) {
            log.debug "Getting SPRADDR records for ${personAddressSurrogateIds?.size()} PIDMs..."
            List<PersonAddressAdditionalProperty> entities = personAddressAdditionalPropertyService.fetchAllBySurrogateIds(personAddressSurrogateIds)
            log.debug "Got ${entities?.size()} SPRADDR records"
            entities?.each {
                personAddressSurrogateIdToGuidMap.put(it.id, it.addressGuid)
            }
        }
        return personAddressSurrogateIdToGuidMap
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
            emailV6.preference = 'primary'
        }
        return emailV6
    }


    private PersonAddressDecorator createPersonAddressDecorator(PersonAddress personAddress, String addressGuid, String addressTypeGuid, String addressType) {
        PersonAddressDecorator personAddressDecorator = new PersonAddressDecorator()
        personAddressDecorator.addressGuid = addressGuid
        personAddressDecorator.type = new AddressTypeDecorator(null, null, addressTypeGuid, addressType)
        personAddressDecorator.startOn = DateConvertHelperService.convertDateIntoUTCFormat(personAddress.fromDate)
        personAddressDecorator.endOn = DateConvertHelperService.convertDateIntoUTCFormat(personAddress.toDate)
        return personAddressDecorator
    }


    private IdentityDocumentV6 createIdentityDocumentV6(VisaInternationalInformation visaIntlInformation, Nation nation, boolean isInstitutionUsingISO2CountryCodes) {
        IdentityDocumentV6 decorator
        if (visaIntlInformation) {
            decorator = new IdentityDocumentV6()
            decorator.documentId = visaIntlInformation.passportId
            decorator.expiresOn = visaIntlInformation.passportExpenditureDate
            decorator.issuingAuthority = nation.nation
            String iso3CountryCode = nation.scodIso
            if (isInstitutionUsingISO2CountryCodes) {
                iso3CountryCode = isoCodeService.getISO3CountryCode(nation.scodIso)
            }
            decorator.countryCode = iso3CountryCode
        }
        return decorator
    }


    protected Map extractDataFromRequestBody(Map person) {
        def requestData = [:]

        /* Required in DataModel - Required in Banner */
        String personGuidInPayload
        if (person.containsKey("guid") && person.get("guid") instanceof String) {
            personGuidInPayload = person?.guid?.trim()?.toLowerCase()
            requestData.put('personGuid', personGuidInPayload)
        }

        // UPDATE operation - API SHOULD prefer the resource identifier on the URI, over the payload.
        String personGuidInURI = person?.id?.trim()?.toLowerCase()
        if (personGuidInURI && !personGuidInURI.equals(personGuidInPayload)) {
            person.put('guid', personGuidInURI)
            requestData.put('personGuid', personGuidInURI)
        }

        if (person.containsKey("names") && person.get("names") instanceof List) {

            Map personalName = person.names.find {
                it?.type?.category == NameTypeCategory.PERSONAL.versionToEnumMap[GeneralValidationCommonConstants.VERSION_V6]
            }
            if (personalName) {
                extractNameFieldsAndPutInMap(personalName, requestData)

                if (!requestData.containsKey("firstName") || !requestData.containsKey("lastName")) {
                    throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("names.required.message", null))
                }

                if (personalName.containsKey("namePrefix") && personalName.get("namePrefix") instanceof String) {
                    requestData.put('namePrefix', personalName?.namePrefix?.trim())
                }

                if (personalName.containsKey("nameSuffix") && personalName.get("nameSuffix") instanceof String) {
                    requestData.put('nameSuffix', personalName?.nameSuffix?.trim())
                }
            } else {
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("names.required.message", null))
            }

            Map<String, String> bannerNameTypeToHedmV6NameTypeMap = getBannerNameTypeToHedmNameTypeMap()
            def alternateNames = []

            Map birthName = person.names.find {
                it.type?.category == NameTypeCategory.BIRTH.versionToEnumMap[GeneralValidationCommonConstants.VERSION_V6]
            }
            if (birthName) {
                def mapEntry = bannerNameTypeToHedmV6NameTypeMap.find { key, value -> value == birthName.type.category }
                if (mapEntry) {
                    def record = [type: mapEntry.key]
                    extractNameFieldsAndPutInMap(birthName, record)
                    if (!record.containsKey("firstName") || !record.containsKey("lastName")) {
                        throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("firstName.lastName.required.message", null))
                    }
                    alternateNames << record
                }
            }
            Map legalName = person.names.find {
                it.type?.category == NameTypeCategory.LEGAL.versionToEnumMap[GeneralValidationCommonConstants.VERSION_V6]
            }
            if (legalName) {
                def mapEntry = bannerNameTypeToHedmV6NameTypeMap.find { key, value -> value == legalName.type.category }
                if (mapEntry) {
                    def record = [type: mapEntry.key]
                    extractNameFieldsAndPutInMap(legalName, record)
                    if (!record.containsKey("firstName") || !record.containsKey("lastName")) {
                        throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("firstName.lastName.required.message", null))
                    }
                    alternateNames << record
                }
            }
            requestData.put("alternateNames", alternateNames)
        } else {
            throw new ApplicationException("PersonV6CompositeService", new BusinessLogicValidationException("names.required.message", []))
        }

        /* Optional in DataModel - Optional in Banner */

        if (person.containsKey("birthDate") && person.get("birthDate") instanceof String && person.get("birthDate")?.trim()?.length() > 0) {
            Date birthDate = DateConvertHelperService.convertDateStringToServerDate(person?.birthDate)
            requestData.put('birthDate', birthDate)
        } else if (person.containsKey("birthDate") && person.get("birthDate") instanceof String && person.get("birthDate")?.trim()?.length() == 0) {
            requestData.put('birthDate', null)
        }

        if (person.containsKey("deadDate") && person.get("deadDate") instanceof String && person.get("deadDate")?.trim()?.length() > 0) {
            Date deadDate = DateConvertHelperService.convertDateStringToServerDate(person?.deadDate)
            requestData.put('deadDate', deadDate)
        } else if (person.containsKey("deadDate") && person.get("deadDate") instanceof String && person.get("deadDate")?.trim()?.length() == 0) {
            requestData.put('deadDate', null)
        }

        if (person.containsKey("privacyStatus") && person.get("privacyStatus") instanceof Map) {
            requestData.put("confidIndicator", extractPrivacyStatusFromRequest(person.get("privacyStatus")))
        }

        if (person.containsKey("gender") && person.get("gender") instanceof String) {
            requestData.put("sex", extractGenderFromRequest(person.get("gender")))
        }

        if (person.containsKey("religion") && person.get("religion") instanceof Map) {
            requestData.put("religion", extractReligionFromRequest(person.get("religion")))
            requestData.put("religionGuid", person.get("religion")?.id)
        }

        if (person.containsKey("races") && person.get("races") instanceof List) {
            requestData.put("raceCodes", extractRacesFromRequest(person.get("races")))
        }

        if (person.containsKey("ethnicity") && person.get("ethnicity") instanceof Map) {
            requestData.put("ethnicity", extractEthnicityFromRequest(person.get("ethnicity")))
        }

        if (person.containsKey("maritalStatus") && person.get("maritalStatus") instanceof Map) {
            requestData.put("maritalStatus", extractMaritalStatusFromRequest(person.get("maritalStatus")))
        }

        if (person.containsKey("emails") && person.get("emails") instanceof List) {
            requestData.put("emails", extractEmailsFromRequest(person.get("emails")))
        }

        if (person.containsKey("nationBirth") && person.get("nationBirth") instanceof String) {
            requestData.put("nationBirth", extractCountryCodeFromRequest(person.get("nationBirth")))
        }

        if (person.containsKey("nationLegal") && person.get("nationLegal") instanceof String) {
            requestData.put("nationLegal", extractCountryCodeFromRequest(person.get("nationLegal")))
        }

        if (person.containsKey("interests") && person.get("interests") instanceof List) {
            requestData.put("interests", extractInterestsFromRequest(person.get("interests")))
        }

        if (person.containsKey("languages") && person.get("languages") instanceof List) {
            requestData.put("language", extractLanguageCodeFromRequest(person.get("languages")))
        }

        if (person.containsKey("citizenshipStatus") && person.get("citizenshipStatus") instanceof Map) {
            requestData.put("citizenType", extractCitizenshipStatusFromRequest(person.get("citizenshipStatus")))
        }

        if (person.containsKey("addresses") && person.get("addresses") instanceof List) {
            requestData.put("addresses", extractAddressesFromRequest(person.get("addresses")))
        }

        if (person.containsKey("credentials") && person.get("credentials") instanceof List) {
            def updateSSN = checkSSNUpdate()
            requestData.put("credentials", extractCredentialsFromRequest(person.get("credentials")))
            requestData.put("updateSSN", updateSSN)
        }
        return requestData
    }


    private def extractCitizenshipStatusFromRequest(Map citizenshipTypeMap) {
        CitizenType citizenType
        Map citizenshipDetail = citizenshipTypeMap.get("detail")
        String citizenshipCategory = citizenshipTypeMap.get("category")
        if(citizenshipDetail && citizenshipDetail instanceof Map){
            CitizenType
        }
        if (citizenshipDetail instanceof Map && citizenshipDetail.get("id") instanceof String && citizenshipCategory?.length() > 0) {
            citizenType = citizenTypeService.fetchByGuid(citizenshipDetail.get("id"))
            if (citizenType) {
                return citizenType
            } else {
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("citizenshipstatus.not.found", null))
            }
        }else if(citizenshipCategory instanceof String && citizenshipCategory?.length() > 0){
            boolean citizenIndicator
            if(citizenshipCategory.equalsIgnoreCase(GeneralValidationCommonConstants.CITIZENSHIP_STATUSES_CATEGORY_CITIZEN)) {
                citizenIndicator = true
            }
            if(citizenshipCategory.equalsIgnoreCase(GeneralValidationCommonConstants.CITIZENSHIP_STATUSES_CATEGORY_NON_CITIZEN)) {
                citizenIndicator = false
            }
            return citizenTypeService.fetchByCitizenIndicator(citizenIndicator)
        }
        return citizenType
    }


    private def extractLanguageCodeFromRequest(List languageCodesInRequest) {
        Language language
        if (languageCodesInRequest.size() > 1) {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("multiple.lang.code", null))
        }
        if (languageCodesInRequest.size() == 0) {
            return language
        }
        Map languageCodeMap = languageCodesInRequest?.get(0)
        List crossReferenceRules = crossReferenceRuleService.fetchAllByISOLanguageCode(languageCodeMap.get("code"))
        if (crossReferenceRules?.size() > 0) {
            language = Language.findByCode(crossReferenceRules?.get(0)?.bannerValue)
        } else {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("mapping.not.found", null))
        }
        return language
    }


    private def extractInterestsFromRequest(List interestsInRequest) {
        List interests = []
        interestsInRequest.each { interestMap ->
            if (interestMap instanceof Map && interestMap.get("id") instanceof String) {
                def interest = interestCompositeService.fetchByGuid(interestMap.get("id"))
                if (interest) {
                    interests << interest
                } else {
                    throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("interests.not.found", null))
                }
            }
        }
        return interests
    }


    private def extractCountryCodeFromRequest(String countryCodeISO) {
        //Nation
        String countryCode
        if (countryCodeISO && countryCodeISO instanceof String && countryCodeISO.trim().length() == 0) {
            return ""
        }
        if (countryCodeISO && countryCodeISO instanceof String && countryCodeISO?.length() > 0) {
            countryCode = countryCodeISO
            if (integrationConfigurationService.isInstitutionUsingISO2CountryCodes()) {
                countryCode = isoCodeService.getISO2CountryCode(countryCode)
            }
        }
        Nation nation = Nation.findByScodIso(countryCode)
        if (nation) {
            return nation?.code
        } else {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("country.not.found", null))
        }
    }


    private def extractEmailsFromRequest(List emailsInRequest) {
        List personEmailMapList = []
        String preference
        Map<String, String> bannerEmailTypeToHedmEmailTypeMap = getBannerEmailTypeToHedmEmailTypeMap()
        if (emailsInRequest) {
            emailsInRequest.each { requestEmail ->
                Map personEmailMap = [:]
                if (requestEmail instanceof Map) {
                    if (requestEmail.containsKey('type') && requestEmail.get('type') instanceof Map) {
                        Map type = requestEmail.get('type')
                        if (type.containsKey('emailType') && type.get('emailType') instanceof String && HedmEmailType.getByString(type.get("emailType"), "v6")) {
                            def mapEntry = bannerEmailTypeToHedmEmailTypeMap.find { key, value -> value == type.get('emailType') }
                            if (mapEntry) {
                                EmailType emailType = emailTypeService.fetchByCode(mapEntry.key)
                                personEmailMap.put("bannerEmailType", emailType)
                            } else {
                                //throw an exception
                                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("emailType.invalid", null))
                            }
                        }
                    } else {
                        throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("emailType.required", null))
                    }
                    if (requestEmail.containsKey('address') && requestEmail.get('address') instanceof String) {
                        personEmailMap.put("emailAddress", requestEmail.get('address'))
                    }
                    if (requestEmail.containsKey('preference') && requestEmail.get('preference') instanceof String) {
                        if (preference != 'primary' && requestEmail.get('preference') == 'primary') {
                            preference = 'primary'
                            personEmailMap.put("preference", 'Y')
                        } else {
                            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("multiple.primaryemail.invalid", null))
                        }
                    }
                }
                personEmailMapList << personEmailMap
            }
        }
        return personEmailMapList
    }


    private def extractMaritalStatusFromRequest(Map maritalStatusMap) {
        Map<String, String> bannerMaritalStatusToHedmMaritalStatusMap = getBannerMaritalStatusToHedmMaritalStatusMap()
        MaritalStatus maritalStatus
        if (maritalStatusMap.containsKey("maritalCategory") && maritalStatusMap.get("maritalCategory") instanceof String && maritalStatusMap.get("maritalCategory")?.length() > 0) {
            def mapEntry = bannerMaritalStatusToHedmMaritalStatusMap.find { key, value -> value == maritalStatusMap.get("maritalCategory") }
            if (mapEntry) {
                String maritalCategory = mapEntry.key
                maritalStatus = maritalStatusService.fetchByCode(maritalCategory)
            } else {
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("marital.status.notfound.message", null))
            }
        }
        return maritalStatus
    }


    private def extractEthnicityFromRequest(Map ethnicityMap) {
        Ethnicity ethnicity
        if (ethnicityMap.get("ethnicGroup") instanceof Map) {
            ethnicity = ethnicityCompositeService.fetchByGuid(ethnicityMap.get("ethnicGroup").get("id"))
        }
        return ethnicity?.code
    }


    private def extractRacesFromRequest(List races) {
        List raceCodes = []
        races.each { raceMap ->
            if (raceMap.get("race") instanceof Map) {
                Race race = raceCompositeService.fetchByGuid(raceMap.get("race").get("id"))
                if (race) {
                    raceCodes << race.race
                } else {
                    throw new ApplicationException("PersonV6CompositeService", new BusinessLogicValidationException("race.not.found.message", []))
                }
            }
        }
        return raceCodes
    }

    private def extractReligionFromRequest(Map religion) {
        if (religion.get("id") instanceof String) {
            Religion religionObj = religionCompositeService.fetchByGuid(religion.get("id"))
            if (religionObj) {
                return religionObj
            } else {
                throw new ApplicationException("PersonV6CompositeService", new BusinessLogicValidationException("religon.not.found.message", []))
            }
        }
    }

    private def extractPrivacyStatusFromRequest(Map privacyStatus) {
        if (privacyStatus.get("privacyCategory") instanceof String) {
            if (privacyStatus.get("privacyCategory") == 'restricted') {
                return 'Y'
            } else {
                return 'N'
            }
        }
    }

    private def extractGenderFromRequest(String gender) {
        if (gender instanceof String) {
            if (gender == Gender.MALE.versionToEnumMap["v6"]) {
                return 'M'
            } else if (gender == Gender.FEMALE.versionToEnumMap["v6"]) {
                return 'F'
            } else if (gender == Gender.UNKNOWN.versionToEnumMap["v6"]) {
                return 'N'
            }
        }
        return null
    }


    private def checkSSNUpdate() {
        IntegrationConfiguration intConfs = findAllByProcessCodeAndSettingName(GeneralValidationCommonConstants.PROCESS_CODE, settingNameSSN)[0]
        if (intConfs.value == 'Y') {
            return 'Y'
        } else {
            return 'N'
        }
    }

    private def extractCredentialsFromRequest(final List credentialsInRequest) {
        List personCredentialList = []

        if (credentialsInRequest.getAt("type").contains(CredentialType.SOCIAL_SECURITY_NUMBER.versionToEnumMap["v6"]) && credentialsInRequest.getAt("type").contains(CredentialType.SOCIAL_INSURANCE_NUMBER.versionToEnumMap["v6"])) {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("ssn.sin.both.not.valid", null))
        }
        credentialsInRequest.each { requestCredential ->
            // Need to check format of credential type
            if (requestCredential instanceof Map) {
                CredentialType type
                String value
                if (requestCredential.containsKey("type") && requestCredential.get("type") instanceof String && CredentialType.getByString(requestCredential.get("type"), "v6")) {
                    type = CredentialType.getByString(requestCredential.get("type"), "v6")
                } else {
                    throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("invalid.code.message:credentialType", null))
                }
                if (requestCredential.containsKey("value") && requestCredential.get("value") instanceof String) {
                    value = requestCredential.get("value")
                    if (requestCredential.containsKey("type") == CredentialType.SOCIAL_SECURITY_NUMBER.versionToEnumMap["v6"] && value.length() > 9) {
                        throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("credentialId.length.message", null))
                    }
                } else {
                    throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("ssn.credentialId.null.message", null))
                }
                personCredentialList << [type: type, value: value]
            }
        }
        return personCredentialList
    }

    private void extractNameFieldsAndPutInMap(Map nameObj, Map requestData) {
        if (nameObj.containsKey("firstName") && nameObj.get("firstName") instanceof String) {
            String firstName = nameObj.get("firstName").trim()
            if (firstName) {
                requestData.put('firstName', firstName)
            }
        }

        if (nameObj.containsKey("lastName") && nameObj.get("lastName") instanceof String) {
            String lastName = nameObj.get("lastName").trim()
            if (lastName) {
                requestData.put('lastName', lastName)
            }
        }

        if(nameObj.containsKey("middleName")){
            if ( nameObj.get("middleName") instanceof String && nameObj.get("middleName")?.trim().length() > 0) {
                requestData.put('middleName', nameObj.get("middleName").trim())
            }
            if ( nameObj.get("middleName") instanceof String &&  nameObj.get("middleName")?.trim().length() == 0) {
                requestData.put('middleName', null)
            }
        }

        if (nameObj.containsKey("surnamePrefix")){
            if ( nameObj.get("surnamePrefix") instanceof String && nameObj.get("surnamePrefix")?.trim().length() > 0) {
                requestData.put('surnamePrefix', nameObj?.surnamePrefix?.trim())
            }
            if ( nameObj.get("surnamePrefix") instanceof String && nameObj.get("surnamePrefix")?.trim().length() == 0) {
                requestData.put('surnamePrefix', null)
            }

        }

    }

    private def extractAddressesFromRequest(final List addressesInRequest) {
        List personAddressMapList = []

        if (addressesInRequest) {
            List addressTypes = addressesInRequest?.type?.addressType

            if (addressTypes && addressTypes.size() != (addressTypes as Set).size()) {
                //throw an exception
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("addressType.duplicate", []))
            }

            Map bannerAddressTypeToHedmV6AddressTypeMap = addressTypeCompositeService.getBannerAddressTypeToHedmV6AddressTypeMap()

            addressesInRequest.each { requestAddress ->
                Map personAddressMap = [:]
                if (requestAddress instanceof Map) {
                    if (requestAddress.containsKey('type') && requestAddress.get('type') instanceof Map) {
                        Map type = requestAddress.get('type')
                        if (type.containsKey('addressType') && type.get('addressType') instanceof String && HedmAddressType.getByString(type.get("addressType"), "v6")) {
                            def mapEntry = bannerAddressTypeToHedmV6AddressTypeMap.find { key, value -> value == type.get('addressType') }
                            if (mapEntry) {
                                personAddressMap.bannerAddressType = mapEntry.key
                            }
                        }
                        if (personAddressMap.bannerAddressType == null) {
                            //throw an exception
                            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("addressType.invalid", []))
                        }
                    } else {
                        throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("addressType.required", []))
                    }

                    Date fromDate = new Date()
                    //set fromDate and toDate
                    if (requestAddress.containsKey('fromDate') && requestAddress.get('fromDate') instanceof String) {
                        String fromDateStr = requestAddress.get('fromDate')
                        if (fromDateStr.length() > 0) {
                            fromDate = DateConvertHelperService.convertUTCStringToServerDate(fromDateStr)
                        }
                    }

                    personAddressMap.fromDate = fromDate

                    if (personAddressMap.fromDate > new Date()) {
                        //throw an exception
                        throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("fromDate.future", []))
                    }

                    if (requestAddress.containsKey('toDate') && requestAddress.get('toDate') instanceof String) {
                        String toDateStr = requestAddress.toDate
                        if (toDateStr.length() > 0) {
                            personAddressMap.toDate = DateConvertHelperService.convertUTCStringToServerDate(toDateStr)
                        }
                    }

                    if (personAddressMap.toDate && personAddressMap.toDate < new Date()) {
                        //throw an exception
                        throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("toDate.past", []))
                    }

                    if (personAddressMap.fromDate && personAddressMap.toDate && personAddressMap.fromDate > personAddressMap.toDate) {
                        //throw an exception
                        throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("fromDate.greater.toDate", []))
                    }

                    if (requestAddress.containsKey("address") && requestAddress.get("address") instanceof Map) {
                        Map address = requestAddress.get("address")
                        setAddressDetails(address, personAddressMap)

                    } else {
                        //throw an excepiton
                        throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("address.requried", []))
                    }

                    //add to person address list
                    personAddressMapList.add(personAddressMap)
                }
            }
        }
        return personAddressMapList
    }

    private void setAddressDetails(Map address, Map personAddressMap) {

        if (address.containsKey("addressLines") && address.get("addressLines") instanceof List && address.get("addressLines").size() > 0) {

            //set streetLine1 , streetLine2, streetLine3, streetLine4
            List addressLines = address.get("addressLines")
            if (addressLines.size() > 0) {
                personAddressMap.streetLine1 = addressLines[0]
            }
            if (addressLines.size() > 1) {
                personAddressMap.streetLine2 = addressLines[1]
            }
            if (addressLines.size() > 2) {
                personAddressMap.streetLine3 = addressLines[2]
            }
            if (addressLines.size() > 3) {
                personAddressMap.streetLine4 = addressLines[3]
            }

        } else {
            //throw an exception
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("addressLines.requried", []))
        }

        if (address.containsKey('place') && address.get('place') instanceof Map) {
            Map place = address.get('place')

            if (place.containsKey('country') && place.get("country") instanceof Map) {

                Map country = place.get("country")

                //Nation
                String countryCode
                if (country.containsKey('code') && country.get('code') instanceof String) {
                    countryCode = country.get('code')
                }

                if (countryCode) {
                    if (integrationConfigurationService.isInstitutionUsingISO2CountryCodes()) {
                        countryCode = isoCodeService.getISO2CountryCode(countryCode)
                    }
                    personAddressMap.nationISOCode = countryCode
                }

                //City
                String locality = '.'
                if (country.containsKey('locality') && country.get("locality") instanceof String) {
                    if (country.get("locality").length() > 0) {
                        locality = country.get("locality")
                    }
                }
                personAddressMap.city = locality

                //State And Zip
                if (country.containsKey('region') && country.get("region") instanceof Map) {

                    Map region = country.get("region")

                    //State Code
                    if (region.containsKey('code') && region.get("code") instanceof String && region.get("code").length() > 0) {
                        String regionCode = region.get("code")
                        String stateCode = crossReferenceRuleService.getStateCodeByRegionCode(regionCode)
                        if (!stateCode) {
                            // throw an exception
                            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("region.not.found", []))
                        }
                        personAddressMap.stateCode = stateCode
                    } else if (region.containsKey('title') && region.get("title") instanceof String && region.get("title").length() > 0) {
                        personAddressMap.stateDescription = region.get("title")
                    }
                }

                //zip code
                String postalCode
                if (country.containsKey('postalCode') && country.get("postalCode") instanceof String && country.get("postalCode").length() > 0) {
                    postalCode = country.get("postalCode")
                } else {
                    postalCode = getDefalutZipCode()
                }

                if (personAddressMap.stateCode || personAddressMap.stateDescription) {
                    personAddressMap.zip = postalCode
                }

                //county
                if (country.containsKey('subRegion') && country.get('subRegion') instanceof Map) {
                    Map subRegion = country.get('subRegion')
                    County county

                    if (subRegion.containsKey('code') && subRegion.get('code') instanceof String && subRegion.get('code').length() > 0) {
                        String subRegionCode = subRegion.get('code')
                        //get the banner value of sub region code
                        String countyCode = crossReferenceRuleService.getCountyCodeBySubRegionCode(subRegionCode)
                        if (countyCode) {
                            county = County.findByCode(countyCode)
                        }
                        //set to new banner column code
                        personAddressMap.countyISOCode = subRegionCode
                    }

                    if (subRegion.containsKey('title') && subRegion.get('title') instanceof String && subRegion.get('title').length() > 0) {
                        String subRegionTitle = subRegion.get('title')
                        if (!county) {
                            county = County.findByDescription(subRegionTitle)
                        }
                        //set to new banner column titile
                        personAddressMap.countyDescription = subRegionTitle
                    }

                    if (county) {
                        personAddressMap.county = county
                    }
                }

                //Additional fields for USA country
                if (country.code == "USA") {
                    if (country.containsKey('deliveryPoint') && country.get('deliveryPoint') instanceof String && country.get('deliveryPoint').length() > 0) {
                        // set deliveryPoint
                        personAddressMap.deliveryPoint = country.get('deliveryPoint')
                    }
                    if (country.containsKey('carrierRoute') && country.get('carrierRoute') instanceof String && country.get('carrierRoute').length() > 0) {
                        //set carrierRoute
                        personAddressMap.carrierRoute = country.get('carrierRoute')
                    }
                    if (country.containsKey('correctionDigit') && country.get('correctionDigit') instanceof String && country.get('correctionDigit').length() > 0) {
                        //set correctionDigit
                        personAddressMap.correctionDigit = country.get('correctionDigit')
                    }
                }

            } else {
                //throw an exceptoin
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("country.requried", []))
            }

        } else {

            //set Default Nation code
            String countryCode = integrationConfigurationService.getDefaultISOCountryCodeForAddress()
            personAddressMap.nationISOCode = countryCode

            //set City as '.'
            personAddressMap.city = '.'
        }

        if (address.containsKey("geographicAreas") && address.get("geographicAreas") instanceof List) {
            def listOfMaps = getListOfMaps(address.get("geographicAreas"))
            personAddressMap.put("geographicAreaGuids", listOfMaps?.id?.unique())
        }

    }


}

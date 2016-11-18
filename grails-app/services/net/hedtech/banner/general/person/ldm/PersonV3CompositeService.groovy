/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.system.ldm.NameTypeCategory

/**
 * RESTful APIs for Ellucian Ethos Data Model "persons" V3.
 */
class PersonV3CompositeService extends AbstractPersonCompositeService {

    @Override
    protected String getPopSelGuidOrDomainKey(Map requestParams) {
        return requestParams.get("personFilter")
    }


    @Override
    protected def prepareCommonMatchingRequest(Map content) {
        def cmRequest = [:]

        // First name, middle name, last name
        def personalName = content.names.find {
            it.nameType == NameTypeCategory.PERSONAL.versionToEnumMap["v3"] && it.firstName && it.lastName
        }

        def birthName = content.names.find {
            it.nameType == NameTypeCategory.BIRTH.versionToEnumMap["v3"] && it.firstName && it.lastName
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
            it.credentialType == CredentialType.SOCIAL_SECURITY_NUMBER.versionToEnumMap["v3"]
        }
        if (credentialObj?.credentialId) {
            cmRequest << [ssn: credentialObj?.credentialId]
        }

        // Banner ID
        credentialObj = content.credentials.find {
            it.credentialType == CredentialType.BANNER_ID.versionToEnumMap["v3"]
        }
        if (credentialObj?.credentialId) {
            cmRequest << [bannerId: credentialObj?.credentialId]
        }

        // Gender
        String gender
        if (content?.gender == 'Male') {
            gender = 'M'
        } else if (content?.gender == 'Female') {
            gender = 'F'
        } else if (content?.gender == 'Unknown') {
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
            Map<String, String> bannerEmailTypeToHedmEmailTypeMap = emailTypeCompositeService.getBannerEmailTypeToHedmV3EmailTypeMap()
            if (bannerEmailTypeToHedmEmailTypeMap) {
                content?.emails.each {
                    def mapEntry = bannerEmailTypeToHedmEmailTypeMap.find { key, value -> value == it.emailType }
                    if (mapEntry) {
                        personEmails << [email: it.emailAddress, emailType: mapEntry.key]
                    }
                }
            }
            cmRequest << [personEmails: personEmails]
        }

        return cmRequest
    }


    protected Map processListApiRequest(final Map requestParams) {
        String sortField = requestParams.sort.trim()
        String sortOrder = requestParams.order.trim()
        int max = requestParams.max.trim().toInteger()
        int offset = requestParams.offset?.trim()?.toInteger() ?: 0

        List<Integer> pidms
        int totalCount = 0

        if (requestParams.containsKey("role") && requestParams.containsKey("personFilter")) {
            // Note: Just decide on priority and proceed in future
            throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("UnsupportedFilterCombination", null))
        } else if (requestParams.containsKey("role") && (requestParams.containsKey("credentialType") || requestParams.containsKey("credentialId"))) {
            // Note: These combinations should not be supported in future, as pagination is not possible
            if (!requestParams.containsKey("credentialType")) {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("creadential.type.required", null))
            }
            if (!requestParams.containsKey("credentialId")) {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("creadential.id.required", null))
            }
            String credentialType = requestParams.get("credentialType")?.trim()
            String credentialValue = requestParams.get("credentialId")?.trim()
            if (credentialType != CredentialType.BANNER_ID.versionToEnumMap["v3"]) {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("creadential.type.invalid", null))
            }
            def mapForSearch = [bannerId: credentialValue]
            def entities = personAdvancedSearchViewService.fetchAllByCriteria(mapForSearch, sortField, sortOrder, max, offset)
            pidms = entities?.collect { it.pidm }
            totalCount = personAdvancedSearchViewService.countByCriteria(mapForSearch)

            // Now check if the persons has role
            String role = requestParams.role?.trim()
            log.debug "Fetching persons with role $role ...."
            def pidmToRoleMap = [:]
            if (role == RoleName.INSTRUCTOR.versionToEnumMap["v3"]) {
                pidmToRoleMap = getPidmToFacultyRoleMap(pidms)
            } else if (role == RoleName.STUDENT.versionToEnumMap["v3"]) {
                pidmToRoleMap = getPidmToStudentRoleMap(pidms)
            } else {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("role.supported", null))
            }
            pidms = pidmToRoleMap.keySet() as List
            totalCount = pidms?.size()
        } else if (requestParams.containsKey("role")) {
            String role = requestParams.role?.trim()
            log.debug "Fetching persons with role $role ...."
            def returnMap
            if (role == RoleName.INSTRUCTOR.versionToEnumMap["v3"]) {
                returnMap = userRoleCompositeService.fetchFaculties(sortField, sortOrder, max, offset)
            } else if (role == RoleName.STUDENT.versionToEnumMap["v3"]) {
                returnMap = userRoleCompositeService.fetchStudents(sortField, sortOrder, max, offset)
                setStudentPidmsInThreadLocal(returnMap?.pidms)
            } else {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("role.supported", null))
            }
            pidms = returnMap?.pidms
            totalCount = returnMap?.totalCount
            log.debug "${totalCount} persons found with role $role."
        } else if (requestParams.containsKey("credentialType") || requestParams.containsKey("credentialId")) {
            if (!requestParams.containsKey("credentialType")) {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("creadential.type.required", null))
            }

            if (!requestParams.containsKey("credentialId")) {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("creadential.id.required", null))
            }

            String credentialType = requestParams.get("credentialType")?.trim()
            String credentialValue = requestParams.get("credentialId")?.trim()

            if (credentialType != CredentialType.BANNER_ID.versionToEnumMap["v3"]) {
                throw new ApplicationException('PersonCompositeService', new BusinessLogicValidationException("creadential.type.invalid", null))
            }

            def mapForSearch = [bannerId: credentialValue]
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
        fetchPersonsEmailDataAndPutInMap_VersionSpecific(pidms, dataMap)
        fetchPersonsMaritalStatusDataAndPutInMap(dataMap)
    }

    private void fetchPersonsMaritalStatusDataAndPutInMap(Map dataMap) {
        maritalStatusCompositeService.getMaritalStatusCodeToGuidMap(bannerMaritalStatusToHedmMaritalStatusMap.keySet())
        Map<String, String> bannerMaritalStatusToHedmMaritalStatusMap = getBannerMaritalStatusToHedmMaritalStatusMap()
        def maritalStatusCodeToGuidMap = maritalStatusCompositeService.getMaritalStatusCodeToGuidMap(bannerMaritalStatusToHedmMaritalStatusMap.keySet())

        // Put in Map
        dataMap.put("bannerMaritalStatusToHedmMaritalStatusMap", bannerMaritalStatusToHedmMaritalStatusMap)
        dataMap.put("maritalStatusCodeToGuidMap", maritalStatusCodeToGuidMap)
    }


    private void fetchPersonsBiographicalDataAndPutInMap_VersionSpecific(List<Integer> pidms, Map dataMap) {
        Set<String> maritalStatusCodes = dataMap.pidmToPersonBaseMap?.values().maritalStatus.code.flatten().unique()
        // MaritalStatus.fetchAllWithGuidByCodeInList
        // MaritalStatusCompositeService.getMaritalStatusCodeToGuidMap
    }


    protected def getBannerNameTypeToHedmNameTypeMap() {
        return personNameTypeCompositeService.getBannerNameTypeToHedmV3NameTypeMap()
    }


    @Override
    protected List<RoleName> getRolesRequired() {
        return [RoleName.STUDENT, RoleName.INSTRUCTOR]
    }


    protected def getBannerAddressTypeToHedmAddressTypeMap() {
        return addressTypeCompositeService.getBannerAddressTypeToHedmV3AddressTypeMap()
    }


    protected def getBannerPhoneTypeToHedmPhoneTypeMap() {
        return phoneTypeCompositeService.getBannerPhoneTypeToHedmV3PhoneTypeMap()
    }


    protected def getBannerEmailTypeToHedmEmailTypeMap() {
        return emailTypeCompositeService.getBannerEmailTypeToHedmV3EmailTypeMap()
    }

    protected def getBannerMaritalStatusToHedmMaritalStatusMap() {
        return maritalStatusCompositeService.getBannerMaritalStatusToHedmV4MaritalStatusMap()
    }

    @Override
    protected extractDataFromRequestBody(Map content) {
        return null
    }

    private void fetchPersonsEmailDataAndPutInMap_VersionSpecific(List<Integer> pidms, Map dataMap) {
        Set<Long> personEmailSurrogateIds = dataMap.pidmToEmailsMap?.values().id.flatten().unique()
        Map<Long, String> personEmailSurrogateIdToGuidMap = getPersonEmailSurrogateIdToGuidMap(personEmailSurrogateIds)

        // Put in Map
        dataMap.put("personEmailSurrogateIdToGuidMap", personEmailSurrogateIdToGuidMap)
    }


    protected void prepareDataMapForSinglePerson_VersionSpecific(PersonIdentificationNameCurrent personIdentificationNameCurrent,
                                                                 final Map dataMap, Map dataMapForPerson) {
    }

    @Override
    protected createPersonDataModel(Map dataMapForPerson) {
        return null
    }

    private def getPersonEmailSurrogateIdToGuidMap(Collection<String> personEmailSurrogateIds) {
        def personEmailSurrogateIdToGuidMap = [:]
        if (personEmailSurrogateIds) {
            List<GlobalUniqueIdentifier> entities = GlobalUniqueIdentifier.fetchByLdmNameAndDomainSurrogateIds("person-emails", personEmailSurrogateIds)
            entities?.each {
                personEmailSurrogateIdToGuidMap.put(it.domainId, it.guid)
            }
        }
        return personEmailSurrogateIdToGuidMap
    }

}

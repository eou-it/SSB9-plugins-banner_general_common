/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.overall.ldm.v6.NonPersonDecorator
import net.hedtech.banner.general.person.*
import net.hedtech.banner.general.person.ldm.v6.EmailV6
import net.hedtech.banner.general.person.ldm.v6.PersonAddressDecorator
import net.hedtech.banner.general.person.ldm.v6.PhoneV6
import net.hedtech.banner.general.person.ldm.v6.RoleV6
import net.hedtech.banner.general.person.view.NonPersonPersonView
import net.hedtech.banner.general.person.view.NonPersonPersonViewService
import net.hedtech.banner.general.system.ldm.AddressTypeCompositeService
import net.hedtech.banner.general.system.ldm.EmailTypeCompositeService
import net.hedtech.banner.general.system.ldm.PhoneTypeCompositeService
import net.hedtech.banner.general.system.ldm.v4.EmailTypeDetails
import net.hedtech.banner.general.system.ldm.v6.AddressTypeDecorator
import net.hedtech.banner.general.utility.DateConvertHelperService
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional

import java.sql.Timestamp

@Transactional
class NonPersonCompositeService extends LdmService {

    private static final List<String> VERSIONS = [GeneralValidationCommonConstants.VERSION_V1, GeneralValidationCommonConstants.VERSION_V6]

    NonPersonPersonViewService nonPersonPersonViewService
    PersonCredentialCompositeService personCredentialCompositeService
    EmailTypeCompositeService emailTypeCompositeService
    PersonEmailService personEmailService
    NonPersonRoleCompositeService nonPersonRoleCompositeService
    PhoneTypeCompositeService phoneTypeCompositeService
    PersonTelephoneService personTelephoneService
    PersonAddressService personAddressService
    PersonAddressAdditionalPropertyService personAddressAdditionalPropertyService
    AddressTypeCompositeService addressTypeCompositeService


    /**
     * GET /api/organizations
     *
     * @param params Request parameters
     * @return
     */
    @Transactional(readOnly = true)
    def get(id) {
        log.trace "getById:Begin:$id"
        String acceptVersion = getAcceptVersion(VERSIONS)

        if (!id) {
            throw new ApplicationException("organization", new NotFoundException())
        }
        Map entitiesMap = nonPersonPersonViewService.fetchByGuid(id)
        if (!entitiesMap) {
            throw new ApplicationException("organization", new NotFoundException())
        }
        log.trace "getById:End"
        return createDecorators([entitiesMap.nonPersonPersonView], getPidmToGuidMap([entitiesMap]))?.getAt(0)
    }

    /**
     * GET /api/organizations
     *
     * @param params Request parameters
     * @return
     */
    @Transactional(readOnly = true)
    def list(Map params) {
        log.trace "list:Begin:$params"
        String acceptVersion = getAcceptVersion(VERSIONS)
        
        RestfulApiValidationUtility.correctMaxAndOffset(params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        int max = params.max.trim().toInteger()
        int offset = params.offset?.trim()?.toInteger() ?: 0
        List rows = []
        int totalCount = 0

        if (params.role?.trim() == OrganizationRoleName.VENDOR.versionToEnumMap["v6"]) {
            log.debug "Fetching persons with role $params.role ...."
            Map returnMap = nonPersonRoleCompositeService.fetchVendors(max, offset)
            rows = nonPersonPersonViewService.fetchAllWithGuidByPidmInList(returnMap.pidms)
            totalCount = returnMap.totalCount
            log.debug "${returnMap.totalCount} persons found with role $params.role."
        } else if (params.containsKey("credential.type") && params.containsKey("credential.value")) {
            String credentialType = params.get("credential.type")?.trim()
            String credentialValue = params.get("credential.value")?.trim()
            if (credentialType == CredentialType.BANNER_ID.versionToEnumMap["v6"]) {
                rows = nonPersonPersonViewService.fetchAllWithGuidByCriteria([bannerId: credentialValue])
                totalCount = nonPersonPersonViewService.countByCriteria([bannerId: credentialValue])
            }
        } else {
            rows = nonPersonPersonViewService.fetchAllWithGuidByCriteria([:], max, offset)
            totalCount = nonPersonPersonViewService.countByCriteria([:])
        }
        injectPropertyIntoParams(params, "count", totalCount)
        return createDecorators(rows.nonPersonPersonView, getPidmToGuidMap(rows))
    }

    @Transactional(readOnly = true)
    def count(Map params) {
        log.trace "count v6: Begin: Request parameters ${params}"
        return getInjectedPropertyFromParams(params, "count")
    }

    def createDecorators(List<NonPersonPersonView> entities, def pidmToGuidMap) {
        def decorators = []
        if (entities) {
            List<Integer> pidms = entities?.collect {
                it.pidm
            }

            def dataMap = [:]
            dataMap.pidmToRolesMap = getPidmToVendorRoleMap(pidms)
            dataMap.put("pidmToGuidMap", pidmToGuidMap)
            fetchPersonsEmailDataAndPutInMap(pidms, dataMap)
            fetchPersonsPhoneDataAndPutInMap(pidms, dataMap)
            fetchPersonsAddressDataAndPutInMap(pidms, dataMap)

            entities?.each {
                def dataMapForPerson = [:]

                dataMapForPerson << ["nonPersonGuid": dataMap.pidmToGuidMap.get(it.pidm)]

                // roles
                def personRoles = []
                if (dataMap.pidmToRolesMap.containsKey(it.pidm)) {
                    personRoles << dataMap.pidmToRolesMap.get(it.pidm)
                }
                personRoles << [role:OrganizationRoleName.AFFILIATE]
                dataMapForPerson << ["personRoles": personRoles]

                // credentials
                def personCredentials = []
                personCredentials << [type: CredentialType.BANNER_ID, value: it.bannerId]
                dataMapForPerson << ["personCredentials": personCredentials]

                // emails
                List<PersonEmail> personEmailList = dataMap.pidmToEmailsMap.get(it.pidm)
                if (personEmailList) {
                    dataMapForPerson << ["personEmails": personEmailList]
                    dataMapForPerson << ["bannerEmailTypeToHedmEmailTypeMap": dataMap.bannerEmailTypeToHedmEmailTypeMap]
                    dataMapForPerson << ["emailCodeToGuidMap": dataMap.emailCodeToGuidMap]
                }

                // phones
                List<PersonTelephone> personTelephoneList = dataMap.pidmToPhonesMap.get(it.pidm)
                if (personTelephoneList) {
                    dataMapForPerson << ["personPhones": personTelephoneList]
                    dataMapForPerson << ["bannerPhoneTypeToHedmPhoneTypeMap": dataMap.bannerPhoneTypeToHedmPhoneTypeMap]
                    dataMapForPerson << ["phoneCodeToGuidMap": dataMap.phoneCodeToGuidMap]
                }
                // addresses
                List<PersonAddress> personAddresses = dataMap.pidmToAddressesMap.get(it.pidm)
                if (personAddresses) {
                    dataMapForPerson << ["personAddresses": personAddresses]
                    dataMapForPerson << ["bannerAddressTypeToHedmAddressTypeMap": dataMap.bannerAddressTypeToHedmAddressTypeMap]
                    dataMapForPerson << ["addressTypeCodeToGuidMap": dataMap.addressTypeCodeToGuidMap]
                    dataMapForPerson << ["personAddressSurrogateIdToGuidMap": dataMap.personAddressSurrogateIdToGuidMap]
                }

                decorators.add(createNonPersonV6(it, dataMapForPerson))
            }
        }
        return decorators
    }

    private NonPersonDecorator createNonPersonV6(NonPersonPersonView nonPersonPersonView, def dataMapForPerson) {
        NonPersonDecorator decorator
        if (nonPersonPersonView) {
            decorator = new NonPersonDecorator()
            // GUID
            decorator.guid = dataMapForPerson["nonPersonGuid"]
            // title
            decorator.title = nonPersonPersonView.lastName

            // Roles
            def personRoles = dataMapForPerson["personRoles"]
            if (personRoles) {
                decorator.roles = []
                personRoles.each {
                    decorator.roles << createRoleV6(it.role)
                }
            }

            // Credentials
            def personCredentials = dataMapForPerson["personCredentials"]
            decorator.credentials = personCredentialCompositeService.createCredentialObjectsV6(personCredentials)

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

            // Phones
            List<PersonTelephone> personTelephoneListList = dataMapForPerson["personPhones"]
            if (personTelephoneListList) {
                Map phoneCodeToGuidMap = dataMapForPerson["phoneCodeToGuidMap"]
                Map bannerPhoneTypeToHedmPhoneTypeMap = dataMapForPerson["bannerPhoneTypeToHedmPhoneTypeMap"]
                decorator.phones = []
                personTelephoneListList.each {
                    decorator.phones << PhoneV6.createPhoneV6(it, phoneCodeToGuidMap.get(it.telephoneType.code), bannerPhoneTypeToHedmPhoneTypeMap.get(it.telephoneType.code))
                }
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
        }
        return decorator
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


    private Map fetchPersonEmailByPIDMs(Collection<Integer> pidms, Collection<String> emailTypeCodes) {
        Map pidmToEmailInfoMap = [:]
        if (pidms && emailTypeCodes) {
            log.debug "Getting GOREMAL records for ${pidms?.size()} PIDMs..."
            List<PersonEmail> entities = personEmailService.fetchAllActiveEmails(pidms, emailTypeCodes)
            log.debug "Got ${entities?.size()} GOREMAL records"
            entities?.each {
                List<PersonEmail> personEmails = []
                if (pidmToEmailInfoMap.containsKey(it.pidm)) {
                    personEmails = pidmToEmailInfoMap.get(it.pidm)
                } else {
                    pidmToEmailInfoMap.put(it.pidm, personEmails)
                }
                personEmails.add(it)
            }
        }
        return pidmToEmailInfoMap
    }

    private Map fetchPersonPhoneByPIDMs(Collection<Integer> pidms, Collection<String> phoneTypeCodes) {
        Map pidmToPhoneInfoMap = [:]
        if (pidms && phoneTypeCodes) {
            log.debug "Getting SPRTELE records for ${pidms?.size()} PIDMs..."
            List<PersonTelephone> entities = personTelephoneService.fetchAllActiveByPidmInListAndTelephoneTypeCodeInList(pidms, phoneTypeCodes)
            log.debug "Got ${entities?.size()} SPRTELE records"
            entities?.each {
                List<PersonTelephone> personTelephones = []
                if (pidmToPhoneInfoMap.containsKey(it.pidm)) {
                    personTelephones = pidmToPhoneInfoMap.get(it.pidm)
                } else {
                    pidmToPhoneInfoMap.put(it.pidm, personTelephones)
                }
                personTelephones.add(it)
            }
        }
        return pidmToPhoneInfoMap
    }

    private Map fetchPersonAddressByPIDMs(Collection<Integer> pidms, Collection<String> addressTypeCodes) {
        Map pidmToAddressInfoMap = [:]
        if (pidms && addressTypeCodes) {
            log.debug "Getting SV_SPRADDR records for ${pidms?.size()} PIDMs..."
            List<PersonAddress> entities = personAddressService.fetchAllByActiveStatusPidmsAndAddressTypes(pidms, addressTypeCodes)
            log.debug "Got ${entities?.size()} SV_SPRADDR records"
            entities?.each {
                List<PersonAddress> personAddresses = []
                if (pidmToAddressInfoMap.containsKey(it.pidm)) {
                    personAddresses = pidmToAddressInfoMap.get(it.pidm)
                } else {
                    pidmToAddressInfoMap.put(it.pidm, personAddresses)
                }
                personAddresses.add(it)
            }
        }
        return pidmToAddressInfoMap
    }


    private void fetchPersonsAddressDataAndPutInMap(List<Integer> pidms, Map dataMap) {

        //Get Mapped Codes for Address Types
        Map<String, String> bannerAddressTypeToHedmAddressTypeMap = addressTypeCompositeService.getBannerAddressTypeToHedmV6AddressTypeMap()

        // Get GUIDs for Address types
        Map<String, String> addressTypeCodeToGuidMap = addressTypeCompositeService.getAddressTypeCodeToGuidMap(bannerAddressTypeToHedmAddressTypeMap.keySet())
        log.debug "Got ${addressTypeCodeToGuidMap?.size() ?: 0} GUIDs for given AddressType codes"

        // Get SPRADDR records for persons
        Map pidmToAddressesMap = fetchPersonAddressByPIDMs(pidms, addressTypeCodeToGuidMap.keySet())

        Set<Long> personAddressSurrogateIds = pidmToAddressesMap?.values().id.flatten() as Set

        Map<Long, String> personAddressSurrogateIdToGuidMap = getPersonAddressSurrogateIdToGuidMap(personAddressSurrogateIds)

        // Put in Map
        dataMap.put("bannerAddressTypeToHedmAddressTypeMap", bannerAddressTypeToHedmAddressTypeMap)
        dataMap.put("pidmToAddressesMap", pidmToAddressesMap)
        dataMap.put("addressTypeCodeToGuidMap", addressTypeCodeToGuidMap)
        dataMap.put("personAddressSurrogateIdToGuidMap", personAddressSurrogateIdToGuidMap)
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


    private def getPidmToVendorRoleMap(List<Integer> pidms) {
        def pidmToVendorRoleMap = [:]
        List<Object[]> rows = nonPersonRoleCompositeService.fetchVendorsByPIDMs(pidms)
        rows?.each {
            BigDecimal bdPidm = it[0]
            Timestamp startDate = it[1]
            Timestamp endDate = it[2]
            pidmToVendorRoleMap.put(bdPidm.toInteger(), [role: OrganizationRoleName.VENDOR, startDate: startDate, endDate: endDate])
        }
        return pidmToVendorRoleMap
    }


    private RoleV6 createRoleV6(OrganizationRoleName roleName) {
        RoleV6 decorator
        if (roleName) {
            decorator = new RoleV6()
            decorator.role = roleName.versionToEnumMap["v6"]
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

    private def getPidmToGuidMap(def rows) {
        Map<Integer, String> pidmToGuidMap = [:]
        rows?.each {
            pidmToGuidMap.put(it.nonPersonPersonView.pidm, it.globalUniqueIdentifier.guid)
        }
        return pidmToGuidMap
    }

    private void injectPropertyIntoParams(Map params, String propName, def propVal) {
        def injectedProps = [:]
        if (params.containsKey("nonPersons-injected") && params.get("nonPersons-injected") instanceof Map) {
            injectedProps = params.get("nonPersons-injected")
        } else {
            params.put("nonPersons-injected", injectedProps)
        }
        injectedProps.putAt(propName, propVal)
    }

    private def getInjectedPropertyFromParams(Map params, String propName) {
        def propVal
        def injectedProps = params.get("nonPersons-injected")
        if (injectedProps instanceof Map && injectedProps.containsKey(propName)) {
            propVal = injectedProps.get(propName)
        }
        return propVal
    }

}

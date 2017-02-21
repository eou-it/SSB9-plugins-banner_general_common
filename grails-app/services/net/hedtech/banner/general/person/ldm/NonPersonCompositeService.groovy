/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.overall.IntegrationConfigurationService
import net.hedtech.banner.general.overall.PersonGeographicAreaAddress
import net.hedtech.banner.general.overall.PersonGeographicAreaAddressService
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.overall.ldm.v6.NonPersonDecorator
import net.hedtech.banner.general.person.*
import net.hedtech.banner.general.person.ldm.v6.EmailV6
import net.hedtech.banner.general.person.ldm.v6.PersonAddressDecorator
import net.hedtech.banner.general.person.ldm.v6.PhoneV6
import net.hedtech.banner.general.person.ldm.v6.RoleV6
import net.hedtech.banner.general.person.view.NonPersonPersonView
import net.hedtech.banner.general.person.view.NonPersonPersonViewService
import net.hedtech.banner.general.system.AddressType
import net.hedtech.banner.general.system.County
import net.hedtech.banner.general.system.EmailType
import net.hedtech.banner.general.system.EmailTypeService
import net.hedtech.banner.general.system.GeographicRegionRule
import net.hedtech.banner.general.system.Nation
import net.hedtech.banner.general.system.State
import net.hedtech.banner.general.system.TelephoneType
import net.hedtech.banner.general.system.ldm.*
import net.hedtech.banner.general.system.ldm.v4.EmailTypeDetails
import net.hedtech.banner.general.system.ldm.v6.AddressTypeDecorator
import net.hedtech.banner.general.utility.DateConvertHelperService
import net.hedtech.banner.general.utility.IsoCodeService
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional

import java.sql.Timestamp

@Transactional
class NonPersonCompositeService extends LdmService {

    private static final List<String> VERSIONS = [GeneralValidationCommonConstants.VERSION_V1, GeneralValidationCommonConstants.VERSION_V6]

    NonPersonPersonViewService nonPersonPersonViewService
    PersonCredentialV6CompositeService personCredentialV6CompositeService
    EmailTypeCompositeService emailTypeCompositeService
    PersonEmailService personEmailService
    NonPersonRoleCompositeService nonPersonRoleCompositeService
    PhoneTypeCompositeService phoneTypeCompositeService
    PersonTelephoneService personTelephoneService
    PersonAddressService personAddressService
    PersonAddressAdditionalPropertyService personAddressAdditionalPropertyService
    AddressTypeCompositeService addressTypeCompositeService
    PersonIdentificationNameCurrentService personIdentificationNameCurrentService
    IntegrationConfigurationService integrationConfigurationService
    PersonCredentialService personCredentialService
    EmailTypeService emailTypeService
    IsoCodeService isoCodeService
    def crossReferenceRuleService
    GeographicAreaCompositeService geographicAreaCompositeService
    PersonGeographicAreaAddressService personGeographicAreaAddressService


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

    def create(Map content) {
        Map bannerIdCredentialObj = [:]
        Map additionalIdTypeCodeToIdMap = [:]

        // extract data from request body
        Map requestData
        if(content.containsKey("source") && content.get("source") == 'update'){
            requestData = content
        }else{
            requestData = extractDataFromRequestBody(content)
        }
        setCredentialsDataIntoMap(requestData, bannerIdCredentialObj, additionalIdTypeCodeToIdMap)

        // banner validation
        validateBannerIdCredential(bannerIdCredentialObj)
        validateTitle(requestData.get("lastName"))

        if (!bannerIdCredentialObj) {
            bannerIdCredentialObj = [value: 'GENERATED']
        }

        String nonPersonGuid = requestData.get("nonPersonGuid")
        PersonIdentificationNameCurrent personIdentificationNameCurrent = createPersonIdentificationNameCurrent(requestData.get("lastName"), bannerIdCredentialObj.value)

        if (nonPersonGuid && nonPersonGuid != GeneralValidationCommonConstants.NIL_GUID) {
            // Overwrite the GUID created by DB insert trigger, with the one provided in the request body
            updateGuidValue(personIdentificationNameCurrent.id, nonPersonGuid, GeneralValidationCommonConstants.NON_PERSONS_LDM_NAME)
        } else {
            GlobalUniqueIdentifier entity = globalUniqueIdentifierService.fetchByLdmNameAndDomainId(GeneralValidationCommonConstants.NON_PERSONS_LDM_NAME, personIdentificationNameCurrent.id)
            nonPersonGuid = entity.guid
        }
        if (additionalIdTypeCodeToIdMap) {
            personCredentialService.createOrUpdateAdditionalIDs(personIdentificationNameCurrent.pidm, additionalIdTypeCodeToIdMap)
        }

        log.debug("GUID: ${nonPersonGuid}")

        //person emails
        if (requestData.containsKey("emails")) {
            createOrUpdatePersonEmails(requestData.get("emails"), personIdentificationNameCurrent.pidm, null)
        }

        //person Address
        if (requestData.containsKey("addresses") && requestData.get("addresses") instanceof List && requestData.get("addresses").size() > 0) {
            createOrUpdateAddress(requestData.get("addresses"), personIdentificationNameCurrent.pidm)
        }

        //person Phones
        if (requestData.containsKey("phones") && requestData.get("phones") instanceof List && requestData.get("phones").size() > 0) {
           createOrUpdatePersonTelephones(personIdentificationNameCurrent.pidm, requestData.get("phones"), null)
        }


        //Needs to do refactor
        Map entitiesMap = nonPersonPersonViewService.fetchByGuid(nonPersonGuid)
        return createDecorators([entitiesMap.nonPersonPersonView], getPidmToGuidMap([entitiesMap]))?.getAt(0)
    }

    /**
     * banner specific logic
     * @param content
     * @return
     */
    def update(Map content) {
        Map bannerIdCredentialObj = [:]
        Map additionalIdTypeCodeToIdMap = [:]

        Map requestData = extractDataFromRequestBody(content)
        String nonPersonGuid = requestData.get("nonPersonGuid")
        GlobalUniqueIdentifier globalUniqueIdentifier = globalUniqueIdentifierService.fetchByLdmNameAndGuid(GeneralValidationCommonConstants.NON_PERSONS_LDM_NAME, nonPersonGuid)
        if (!globalUniqueIdentifier) {
            requestData.put("source","update")
            return create(content)
        }
        setCredentialsDataIntoMap(requestData, bannerIdCredentialObj, additionalIdTypeCodeToIdMap)

        Integer pidm = globalUniqueIdentifier.domainKey?.toInteger()
        List<PersonIdentificationNameCurrent> personIdentificationList = PersonIdentificationNameCurrent.findAllByPidmInList([pidm])

        PersonIdentificationNameCurrent newPersonIdentificationNameCurrent
        personIdentificationList.each { identification ->
            if (identification.changeIndicator == null) {
                newPersonIdentificationNameCurrent = identification
            }
        }

        PersonIdentificationNameCurrent oldPersonIdentificationNameCurrent = new PersonIdentificationNameCurrent(newPersonIdentificationNameCurrent?.properties)

        if(requestData.get("lastName") != oldPersonIdentificationNameCurrent.lastName){
            // banner validation
            validateTitle(requestData.get("lastName"))
            newPersonIdentificationNameCurrent.lastName = requestData.get("lastName")
        }

        if (bannerIdCredentialObj && (bannerIdCredentialObj.value?.length() > 0 && bannerIdCredentialObj.value?.length() <= 9) && (oldPersonIdentificationNameCurrent.bannerId != bannerIdCredentialObj.value)){
            // banner validation
            validateBannerIdCredential(bannerIdCredentialObj)
            newPersonIdentificationNameCurrent.bannerId = bannerIdCredentialObj.value
        }

        if(!oldPersonIdentificationNameCurrent.equals(newPersonIdentificationNameCurrent) ){
            //create
            newPersonIdentificationNameCurrent = personIdentificationNameCurrentService.update(newPersonIdentificationNameCurrent)
        }

        //person emails
        Map bannerEmailTypeToHedmEmailTypeMap = getBannerEmailTypeToHedmEmailTypeMap()
        List<PersonEmail> personEmails = personEmailService.fetchAllEmails(newPersonIdentificationNameCurrent.pidm, bannerEmailTypeToHedmEmailTypeMap.keySet())
        if (requestData.containsKey("emails") && requestData.get("emails") instanceof List) {
            personEmails = createOrUpdatePersonEmails(requestData.get("emails"), newPersonIdentificationNameCurrent.pidm, personEmails)
        }

        //person Address
        List<PersonAddress> personAddresses = []
        if (requestData.containsKey("addresses") && requestData.get("addresses") instanceof List) {
            List addresses = requestData.get("addresses")
            //Update : Make it inactive , if exist person address have any updates
            addresses = getActiveAddresses(newPersonIdentificationNameCurrent.pidm, addresses)
            if (addresses) {
                createOrUpdateAddress(addresses, newPersonIdentificationNameCurrent.pidm)
            }
        }

        //person Phones
        if (requestData.containsKey("phones") && requestData.get("phones") instanceof List) {
            List phones = requestData.get("phones")
            Map bannerPhoneTypeToHedmPhoneTypeMap = getBannerPhoneTypeToHedmPhoneTypeMap()
            Collection<PersonTelephone> existingPersonTelephones = getPidmToPhonesMap([newPersonIdentificationNameCurrent.pidm], bannerPhoneTypeToHedmPhoneTypeMap.keySet())[newPersonIdentificationNameCurrent.pidm]
            Collection<PersonTelephone> personTelephones = createOrUpdatePersonTelephones(newPersonIdentificationNameCurrent.pidm, phones, existingPersonTelephones)
        }


        if (additionalIdTypeCodeToIdMap) {
            def additionalIdsToRemove = additionalIdTypeCodeToIdMap.findAll { key, value -> !value }
            additionalIdsToRemove.each { key, value -> additionalIdTypeCodeToIdMap.remove(key) }
            personCredentialService.deleteAdditionalIDs(oldPersonIdentificationNameCurrent.pidm, additionalIdsToRemove.keySet())
            personCredentialService.createOrUpdateAdditionalIDs(oldPersonIdentificationNameCurrent.pidm, additionalIdTypeCodeToIdMap)
        }


        //Needs to do refactor
        Map entitiesMap = nonPersonPersonViewService.fetchByGuid(nonPersonGuid)
        return createDecorators([entitiesMap.nonPersonPersonView.refresh()], getPidmToGuidMap([entitiesMap]))?.getAt(0)
    }

    protected void validateBannerIdCredential(Map bannerIdCredentialObj) {
        if (bannerIdCredentialObj && (bannerIdCredentialObj.value?.length() > 0 && bannerIdCredentialObj.value?.length() <= 9)) {
            PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.fetchByBannerId(bannerIdCredentialObj.value)
            if (personIdentificationNameCurrent) {
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("bannerId.already.exists", null))
            }
        }
    }

    protected void validateTitle(String lastName){
        if(lastName.length() > 60){
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("title.length.exceed", null))
        }

    }

    protected void setCredentialsDataIntoMap(Map requestData, Map bannerIdCredentialObj, Map additionalIdTypeCodeToIdMap) {
        if (requestData.containsKey("credentials") && requestData.get("credentials").size() > 0) {

            List credentials = requestData.get("credentials")
            Map bannerIdCredential = credentials.find {
                it.type == CredentialType.BANNER_ID
            }
            if(bannerIdCredential) {
                bannerIdCredentialObj.putAll(bannerIdCredential)
            }
            Map credentialTypeToAdditionalIdTypeCodeMap = getCredentialTypeToAdditionalIdTypeCodeMap()
            credentialTypeToAdditionalIdTypeCodeMap.each { credentialType, additionalIdTypeCode ->
                def obj = credentials?.find {
                    it.type == credentialType
                }
                if (obj) {
                    log.debug "$credentialType --- $additionalIdTypeCode --- ${obj.value}"
                    additionalIdTypeCodeToIdMap.put(additionalIdTypeCode, obj.value)
                    credentials.remove(obj)
                }
            }
        }
    }

    protected def getCredentialTypeToAdditionalIdTypeCodeMap() {
        def map = [:]

        IntegrationConfiguration intConfig = integrationConfigurationService.fetchByProcessCodeAndSettingName(GeneralValidationCommonConstants.PROCESS_CODE, "CREDENTIALS.ELEVATE_ID")
        map.put(CredentialType.ELEVATE_ID, intConfig.value)

        intConfig = integrationConfigurationService.fetchByProcessCodeAndSettingName(GeneralValidationCommonConstants.PROCESS_CODE, "CREDENTIALS.COLLEAGUE_ID")
        map.put(CredentialType.COLLEAGUE_PERSON_ID, intConfig.value)

        return map
    }


    private PersonIdentificationNameCurrent createPersonIdentificationNameCurrent(String lastName, String bannerId) {
        Map currentIdentification = [:]
        currentIdentification.put('lastName', lastName)
        currentIdentification.put('bannerId', bannerId)
        currentIdentification.put('entityIndicator', 'C')
        currentIdentification.put('changeIndicator', null)
        return personIdentificationNameCurrentService.create(currentIdentification)
    }

    private
    def createOrUpdatePersonEmails(List emailListInRequest, Integer pidm, List<PersonEmail> existingPersonEmails) {
        List<PersonEmail> personEmails = []
        existingPersonEmails.each {
            it.statusIndicator = "I"
            it.preferredIndicator = false
            personEmailService.update([domainModel: it])
        }
        emailListInRequest.each { emailMapInRequest ->
            EmailType emailTypeInRequest = emailTypeService.fetchByCode(emailMapInRequest.emailTypeCode)
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
                existingPersonEmail.preferredIndicator = emailMapInRequest.preferredIndicator ?: false
                if (existingPersonEmail.statusIndicator == 'I') {
                    existingPersonEmail.statusIndicator = 'A'
                }
                personEmailService.update([domainModel: existingPersonEmail])
                personEmails << existingPersonEmail
                existingPersonEmails.remove(existingPersonEmail)
            } else {
                // Create
                PersonEmail personEmail = new PersonEmail(pidm: pidm, emailAddress: emailAddressInRequest, statusIndicator: "A", emailType: emailTypeInRequest)
                if (emailMapInRequest.preferredIndicator) {
                    personEmail.preferredIndicator = true
                }
                personEmail = personEmailService.create([domainModel: personEmail])
                personEmails << personEmail
            }
        }
        return personEmails
    }

    protected def createOrUpdateAddress(List addressesInRequest, Integer pidm) {
        List<PersonAddress> personAddresses = []

        addressesInRequest.each {
            //Person Address
            PersonAddress personAddress = personAddressService.getDomainClass().newInstance()
            bindPersonAddress(personAddress, it, pidm)
            personAddress = personAddressService.create(personAddress)

            if (it.isoCountyCode || it.countyDescription) {
                PersonAddressAdditionalProperty addressAdditionalProperty = personAddressAdditionalPropertyService.get(personAddress.id)
                addressAdditionalProperty.countyISOCode = it.isoCountyCode
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


    private void bindPersonAddress(PersonAddress personAddress, Map requestAddress, Integer pidm) {
        personAddress.pidm = pidm

        AddressType addressType
        if (requestAddress.containsKey('addressTypeCode')) {
            addressType = AddressType.findByCode(requestAddress.addressTypeCode)
            if (!addressType) {
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("addressType.not.found.message", []))
            }
        }
        personAddress.addressType = addressType

        Nation nation
        if (requestAddress.containsKey('iso3CountryCode')) {
            String isoCountryCode = requestAddress.get('iso3CountryCode')
            if (integrationConfigurationService.isInstitutionUsingISO2CountryCodes()) {
                isoCountryCode = isoCodeService.getISO2CountryCode(isoCountryCode)
                if (!isoCountryCode) {
                    throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("country.code.invalid.message", null))
                }
            }
            nation = fetchNationByScodIso(isoCountryCode)
        }
        personAddress.nation = nation

        State state
        if (requestAddress.containsKey('isoStateCode') && requestAddress.isoStateCode) {
            String stateCode = crossReferenceRuleService.getStateCodeByRegionCode(requestAddress.isoStateCode)
            if (!stateCode) {
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("region.not.found", null))
            }
            state = State.findByCode(stateCode)
            if (!state) {
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("state.not.found.message", null))
            }
        } else if (requestAddress.containsKey('stateDescription') && requestAddress.stateDescription) {
            state = State.findByDescription(requestAddress.stateDescription)
            if (!state) {
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("state.not.found.message", null))
            }
        }
        personAddress.state = state

        County county
        if (requestAddress.containsKey("isoCountyCode")) {
            String isoCountyCode = requestAddress.get("isoCountyCode")
            if (isoCountyCode.length() > 0) {
                String countyCode = crossReferenceRuleService.getCountyCodeBySubRegionCode(isoCountyCode)
                if (countyCode) {
                    county = County.findByCode(countyCode)
                }
            }
        }
        if (!county && requestAddress.containsKey("countyDescription")) {
            String countyDescription = requestAddress.get("countyDescription")
            if (countyDescription.length() > 0) {
                county = County.findByDescription(countyDescription)
            }
        }
        personAddress.county = county

        bindData(personAddress, requestAddress, [:])

        if (!personAddress.fromDate) {
            personAddress.fromDate = new Date()
        }

        if (personAddress.fromDate > new Date()) {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("fromDate.future", null))
        }

        if (personAddress.toDate && personAddress.toDate < new Date()) {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("toDate.past", null))
        }

        if (personAddress.toDate && personAddress.fromDate > personAddress.toDate) {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("fromDate.greater.toDate", null))
        }

        if (!personAddress.city) {
            personAddress.city = '.'
        }

        // if COUNTRY is present, then neither STATE nor ZIP are required
        // If STATE is present (regardless of whether COUNTRY is present or not), then ZIP is required
        if (personAddress.state) {
            if (!personAddress.zip) {
                personAddress.zip = integrationConfigurationService.getDefaultOrganizationZipCode()
            }
        } else if (!personAddress.nation) {
            String isoCountryCode = integrationConfigurationService.getDefaultISOCountryCodeForAddress()
            personAddress.nation = fetchNationByScodIso(isoCountryCode)
        }
    }


    private Nation fetchNationByScodIso(String isoCountryCode) {
        if (!isoCountryCode) {
            return null
        }
        Nation nation = Nation.findByScodIso(isoCountryCode)
        if (nation) {
            return nation
        } else {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("country.not.found.message", []))
        }
    }


    private def createOrUpdatePersonTelephones(Integer pidm,
                                               def phonesInRequest, Collection<PersonTelephone> existingPersonTelephones) {
        Collection<PersonTelephone> lstPersonTelephone = []

        PersonTelephone personTelephone
        phonesInRequest?.each { requestPhone ->
            log.debug "Processing ${requestPhone.phoneNumber} ..."
            PersonTelephone matchingPersonTelephone = findMatchingPersonTelephone(requestPhone, existingPersonTelephones)
            if (matchingPersonTelephone) {
                // Update
                personTelephone = matchingPersonTelephone
                existingPersonTelephones.remove(matchingPersonTelephone)
            } else {
                // Create
                personTelephone = parseAndCreatePersonTelephone(pidm, requestPhone)
                personTelephone = personTelephoneService.create(personTelephone)
            }
            lstPersonTelephone << personTelephone
        }

        // Inactive DB records
        existingPersonTelephones?.each { entity ->
            entity.statusIndicator = 'I'
            log.debug "Inactivating phone:" + entity.toString()
            personTelephoneService.update(entity)
        }

        return lstPersonTelephone
    }

    private PersonTelephone findMatchingPersonTelephone(
            Map requestPhone, Collection<PersonTelephone> existingPersonTelephones) {
        PersonTelephone matchingPersonTelephone

        List<PersonTelephone> existingPersonTelephonesByType = existingPersonTelephones?.findAll {
            it.telephoneType.code == requestPhone.phoneTypeCode
        }

        String requestTemp = (requestPhone.countryPhone ?: "") + (requestPhone.phoneNumber)
        log.debug "Complete Phone Number $requestTemp"

        existingPersonTelephonesByType?.each { entity ->
            boolean sameAsExisting = PhoneNumberUtility.comparePhoneNumber(requestTemp, entity.countryPhone, entity.phoneArea, entity.phoneNumber)
            if (sameAsExisting && requestPhone.containsKey('phoneExtension')) {
                String reqPhoneExtn = requestPhone.phoneExtension ? requestPhone.phoneExtension.trim() : ""
                String dbPhoneExtn = entity.phoneExtension ? entity.phoneExtension.trim() : ""
                if (reqPhoneExtn != dbPhoneExtn) {
                    log.debug "Phone extension different"
                    sameAsExisting = false
                }
            }
            if (sameAsExisting) {
                matchingPersonTelephone = entity
                if (requestPhone.containsKey("primaryIndicator")) {
                    matchingPersonTelephone.primaryIndicator = requestPhone.primaryIndicator
                }
                return matchingPersonTelephone
            }
        }

        return matchingPersonTelephone
    }


    private PersonTelephone parseAndCreatePersonTelephone(Integer pidm, Map requestPhone) {
        String countryRegionCode
        if (requestPhone.containsKey("countryPhone") && requestPhone.get("countryPhone")?.length() > 0) {
            String countryPhone = requestPhone.get("countryPhone")
            if (countryPhone.getAt(0) == '+') {
                countryPhone = new StringBuilder(countryPhone).deleteCharAt(0).toString()
            }
            countryRegionCode = PhoneNumberUtility.getRegionCodeForCountryCode(Integer.valueOf(countryPhone))
        } else {
            countryRegionCode = integrationConfigurationService.getDefaultISO2CountryCodeForOrganizationPhoneNumberParsing()
        }
        def parts = PhoneNumberUtility.parsePhoneNumber(requestPhone.phoneNumber, countryRegionCode)

        if (parts.size() == 0) {
            // Parsing is not succesful so we go with split
            parts = splitPhoneNumber(requestPhone.phoneNumber)
        }

        return new PersonTelephone(
                pidm: pidm,
                countryPhone: requestPhone.countryPhone,
                phoneArea: parts["phoneArea"],
                phoneNumber: parts["phoneNumber"],
                phoneExtension: requestPhone.phoneExtension,
                telephoneType: TelephoneType.findByCode(requestPhone.phoneTypeCode),
                primaryIndicator: requestPhone.primaryIndicator
        )
    }


    private def splitPhoneNumber(String requestPhoneNumber) {
        def parts = [:]
        if (requestPhoneNumber.length() <= 12) {
            parts.put('phoneNumber', requestPhoneNumber)
        } else {
            parts.put('phoneArea', requestPhoneNumber.substring(0, 6))
            String number = requestPhoneNumber.substring(6, requestPhoneNumber.length())
            if (number.length() > 12) {
                number = number.substring(0, 12)
            }
            parts.put('phoneNumber', number)
        }
        return parts
    }


    private def getActiveAddresses(def pidm, List<Map> addressesInRequest) {
        Map addressTypeToHedmAddressTypeMap = getBannerAddressTypeToHedmAddressTypeMap()
        List<PersonAddress> existingPersonAddresses = personAddressService.fetchAllByActiveStatusPidmsAndAddressTypes([pidm], addressTypeToHedmAddressTypeMap.keySet())
        List<PersonGeographicAreaAddress> geographicAreaAddresses = personGeographicAreaAddressService.fetchActivePersonGeographicAreaAddress(pidm)
        List<PersonAddressAdditionalProperty> additionalProperties = personAddressAdditionalPropertyService.fetchAllBySurrogateIds(existingPersonAddresses.id)
        existingPersonAddresses.each { existingPersonAddress ->

            def requestAddresses = addressesInRequest.findAll { it ->
                it.addressTypeCode == existingPersonAddress.addressType.code
            }

            if (requestAddresses.size() > 0) {
                requestAddresses.each {
                    PersonAddressAdditionalProperty additionalProperty = additionalProperties.find {
                        it.id == existingPersonAddress.id
                    }
                    Boolean changeToInactiveStatus = false
                    switch (it.addressTypeCode) {
                        default:
                            if (it.streetLine1?.trim() != existingPersonAddress.streetLine1) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("streetLine2") && it.streetLine2?.trim() != existingPersonAddress.streetLine2) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("streetLine3") && it.streetLine3?.trim() != existingPersonAddress.streetLine3) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("streetLine4") && it.streetLine4?.trim() != existingPersonAddress.streetLine4) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("toDate") && it.toDate?.clearTime() != existingPersonAddress.toDate?.clearTime()) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("fromDate") && it.fromDate?.clearTime() != existingPersonAddress.fromDate?.clearTime()) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("iso3CountryCode")) {
                                String isoCountryCode = it.get('iso3CountryCode')
                                if (integrationConfigurationService.isInstitutionUsingISO2CountryCodes()) {
                                    isoCountryCode = isoCodeService.getISO2CountryCode(isoCountryCode)
                                }
                                if (isoCountryCode != existingPersonAddress.nation?.scodIso) {
                                    changeToInactiveStatus = true
                                    break
                                }
                            }
                            if (it.containsKey("city") && it.city?.trim() != existingPersonAddress.city) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("isoStateCode")) {
                                String stateCode = crossReferenceRuleService.getStateCodeByRegionCode(it.isoStateCode?.trim())
                                if (stateCode != existingPersonAddress.state.code) {
                                    changeToInactiveStatus = true
                                    break
                                }
                            }
                            if (it.containsKey("stateDescription") && it.stateDescription?.trim() != existingPersonAddress.state?.description) {
                                changeToInactiveStatus = true
                                break
                            }
                            if (it.containsKey("zip") && it.zip?.trim() != existingPersonAddress.zip) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("isoCountyCode") && it.isoCountyCode != additionalProperty.countyISOCode) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("countyDescription") && it.countyDescription != additionalProperty.countyDescription) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("deliveryPoint") && it.deliveryPoint != existingPersonAddress.deliveryPoint) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("carrierRoute") && it.carrierRoute != existingPersonAddress.carrierRoute) {
                                changeToInactiveStatus = true
                                break;
                            }
                            if (it.containsKey("correctionDigit") && it.correctionDigit != existingPersonAddress.correctionDigit) {
                                changeToInactiveStatus = true
                                break;
                            }
                            break;
                    }
                    if (changeToInactiveStatus) {
                        existingPersonAddress.statusIndicator = 'I'
                        log.debug "Inactivating address:" + existingPersonAddress.toString()
                        personAddressService.update(existingPersonAddress)
                        List<PersonGeographicAreaAddress> personGeographicAreaAddresses = geographicAreaAddresses.findAll {
                            it.addressType.code == existingPersonAddress.addressType.code
                        }
                        personGeographicAreaAddresses.each { personGeographicAreaAddress ->
                            personGeographicAreaAddress.statusIndicator = existingPersonAddress.statusIndicator
                            personGeographicAreaAddressService.update(personGeographicAreaAddress)
                        }
                    } else {
                        List<PersonGeographicAreaAddress> personGeographicAreaAddresses = geographicAreaAddresses.findAll {
                            it.addressType.code == existingPersonAddress.addressType.code
                        }
                        if (it.containsKey("geographicAreaGuids")) {
                            List geographicAreaGuids = it.get("geographicAreaGuids")
                            if (personGeographicAreaAddresses.isEmpty()) {
                                createOrUpdateGeographicAddress(it, existingPersonAddress)
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
                                    createOrUpdateGeographicAddress(it, existingPersonAddress)

                                }

                            }
                        }
                        // remove from the list, if there is no changes of address
                        addressesInRequest.remove(it)
                    }
                }
            } else {
                existingPersonAddress.statusIndicator = 'I'
                log.debug "Inactivating address:" + existingPersonAddress.toString()
                personAddressService.update(existingPersonAddress)
                List<PersonGeographicAreaAddress> personGeographicAreaAddresses = geographicAreaAddresses.findAll {
                    it.addressType.code == existingPersonAddress.addressType.code
                }
                personGeographicAreaAddresses.each { personGeographicAreaAddress ->
                    personGeographicAreaAddress.statusIndicator = existingPersonAddress.statusIndicator
                    personGeographicAreaAddressService.update(personGeographicAreaAddress)
                }
            }
        }

        return addressesInRequest
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


    /**
     * Ethos Schema Specific logic
     * @param content
     * @return
     */
    Map extractDataFromRequestBody(final Map content){
        def extractedData = [:]

        // id
        if (content.containsKey("id") && content.get("id") instanceof String) {
            extractedData.put('nonPersonGuid', content.get("id")?.trim()?.toLowerCase())
        }

        // title
        extractTitle(content, extractedData)

        // credentials
        extractCredentials(content,extractedData)

        // emails
        extractEmails(content, extractedData)

        // phones
        extractPhones(content, extractedData)

        // address
        extractAddresses(content, extractedData)

        return extractedData
    }

    private void extractTitle(final Map content, Map extractedData) {
        if (content.containsKey("title") && content.get("title") instanceof String) {
            extractedData.put('lastName', content.get("title")?.trim()?.toLowerCase())
        }

        if(!extractedData.get("lastName")) {
            // error message
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("title.required", null))
        }

    }

   private void extractCredentials(final Map content, Map extractedData){

       if(content.containsKey("credentials") && content.get("credentials") instanceof List){

           List credentialsData = content.get("credentials")

           credentialsData.retainAll {it instanceof Map}
           Collection credentials = []

           credentialsData.each{
               credentials << extractCredential(it)
           }

           extractedData.put("credentials", credentials)
       }
    }

    private Map extractCredential(final Map credentialMap){
        CredentialType credentialType
        String credentialValue

        if(credentialMap.containsKey("type") && credentialMap.get("type") instanceof String){
            credentialType = CredentialType.getByDataModelValue(credentialMap.get("type").trim(), GeneralValidationCommonConstants.VERSION_V6)
        }

        if(credentialMap.containsKey("value") && credentialMap.get("value") instanceof String){
            credentialValue = credentialMap.get("value")
        }

        if (!credentialType || !credentialValue) {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("invalid.credentialType", null))
        }

        return [type: credentialType,value: credentialValue]
    }

    private void extractEmails(final Map content, Map extractedData) {

        if (content.containsKey("emails") && content.get("emails") instanceof List) {
            Collection extractedEmails = []
            Boolean preferredEmailSelected = false

            List emailsContent = content.get("emails")
            Map<String, String> bannerEmailTypeCodeToHedmEmailTypeMap = getBannerEmailTypeToHedmEmailTypeMap()

            emailsContent.retainAll { it instanceof Map }
            emailsContent.each {
              Map data = extractEmail(it, bannerEmailTypeCodeToHedmEmailTypeMap)
                if (isDuplicateEmailInRequest(extractedEmails, data)) {
                    throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("duplicate.email.request", null))
                }
                if (data.get("preferredIndicator")) {
                    if (preferredEmailSelected) {
                        throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("multiple.primaryemail.invalid", null))
                    }
                    preferredEmailSelected = data.get("preferredIndicator")
                }
                extractedEmails << data
            }

            extractedData.put("emails", extractedEmails)
        }
    }

    private Map extractEmail(final Map emailContent, Map bannerEmailTypeCodeToHedmEmailTypeMap){
        Map data = [:]
        if(emailContent.containsKey("type") && emailContent.get("type") instanceof Map){
            Map type = emailContent.get("type")

            if (type.containsKey("emailType") && type.get("emailType") instanceof String) {
                String emailType = type.get("emailType").trim()

                HedmEmailType hedmEmailType = HedmEmailType.getByDataModelValue(emailType, GeneralValidationCommonConstants.VERSION_V6)
                if (!hedmEmailType) {
                    throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("invalid.type", null))
                }

                def mapEntry = bannerEmailTypeCodeToHedmEmailTypeMap.find { key, value -> value == emailType }
                if (!mapEntry) {
                    throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("emailMapping.not.found", null))
                }

                data.put("emailTypeCode", mapEntry.key)
            } else {
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("invalid.type", null))
            }
        } else {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("emailType.required", null))
        }

        if (emailContent.containsKey("address") && emailContent.get("address") instanceof String) {
            String address = emailContent.get('address').trim()

            if (!address) {
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("email.address.required", null))
            }

            String pattern = '^[a-zA-Z0-9.!#$%&' + "/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*" + '$'

            if (!address.matches(pattern)) {
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("email.address.invalid", null))
            }

            data.put("emailAddress", address)
        } else {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("email.address.required", null))
        }

        if (emailContent.containsKey("preference") && emailContent.get("preference") instanceof String) {
            Boolean preferredIndicator = false

            if (emailContent.get("preference").trim() == "primary") {
                preferredIndicator = true
            }

            data.put("preferredIndicator", preferredIndicator)
        }

        return data

    }

    private boolean isDuplicateEmailInRequest(Collection extractedEmails, Map currEmail) {
        boolean dup = false
        def existingEmail = extractedEmails.find {
            it.emailAddress == currEmail.emailAddress && it.emailTypeCode == currEmail.emailTypeCode
        }
        if (existingEmail) {
            dup = true
        }
        return dup
    }

    private void extractPhones(Map content, Map extractedData) {
        if (content.containsKey("phones") && content.get("phones") instanceof List) {
            List phones = content.get("phones")
            phones.retainAll { it instanceof Map }

            Map<String, String> bannerPhoneTypeCodeToHedmPhoneTypeMap = getBannerPhoneTypeToHedmPhoneTypeMap()

            Collection extractedPhones = []
            Boolean preferredPhoneSelected = false
            phones.each {
                Map data = extractPhone(it, bannerPhoneTypeCodeToHedmPhoneTypeMap)
                if (isDuplicatePhoneInRequest(extractedPhones, data)) {
                   // throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("phone.phoneType.duplicate", null))
                }
                if (data.get("primaryIndicator") == "Y") {
                    if (preferredPhoneSelected) {
                        //throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("phone.preferences.duplicate", null))
                    }
                    preferredPhoneSelected = true
                }
                extractedPhones << data
            }

            extractedData.put("phones", extractedPhones)
        }
    }


    private def extractPhone(Map phoneObj, Map<String, String> bannerPhoneTypeCodeToHedmPhoneTypeMap) {
        Map data = [:]

        if (phoneObj.containsKey("type") && phoneObj.get("type") instanceof Map) {
            Map type = phoneObj.get("type")

            if (type.containsKey("phoneType") && type.get("phoneType") instanceof String) {
                String phoneType = type.get("phoneType").trim()

                HedmPhoneType hedmPhoneType = HedmPhoneType.getByDataModelValue(phoneType, GeneralValidationCommonConstants.VERSION_V6)
                if (!hedmPhoneType) {
                    throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("phone.phoneType.inValid", null))
                }

                def mapEntry = bannerPhoneTypeCodeToHedmPhoneTypeMap.find { key, value -> value == phoneType }
                if (!mapEntry) {
                    throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("phone.phoneType.not.found", null))
                }

                data.put("phoneTypeCode", mapEntry.key)
            } else {
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("phone.phoneType.required", null))
            }
        } else {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("phone.type.required", null))
        }

        if (phoneObj.containsKey("number") && phoneObj.get("number") instanceof String) {
            data.put("phoneNumber", phoneObj.get("number").trim())
        } else {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("phone.number.required", null))
        }

        if (phoneObj.containsKey("countryCallingCode") && phoneObj.get("countryCallingCode") instanceof String) {
            String countryCallingCode = phoneObj.get("countryCallingCode").trim()
            String countryPhone
            if (countryCallingCode.length() > 0) {
                String pattern = "^\\+?[1-9][0-9]{0,3}" + '$'
                if (!countryCallingCode.matches(pattern)) {
                    throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("phone.invalid.countryCallingCode.format", null))
                }
                countryPhone = countryCallingCode
            }
            data.put("countryPhone", countryPhone)
        }

        if (phoneObj.containsKey("extension") && phoneObj.get("extension") instanceof String) {
            data.put("phoneExtension", phoneObj.get("extension").trim())
        }

        if (phoneObj.containsKey("preference") && phoneObj.get("preference") instanceof String) {
            String primaryIndicator

            if (phoneObj.get("preference").trim() == "primary") {
                primaryIndicator = "Y"
            }

            data.put("primaryIndicator", primaryIndicator)
        }

        return data
    }


    private boolean isDuplicatePhoneInRequest(Collection extractedPhones, Map currPhone) {
        boolean dup = false
        def existingPhone = extractedPhones.find {
            it.phoneTypeCode == currPhone.phoneTypeCode
        }
        if (existingPhone) {
            dup = true
        }
        return dup
    }

    private void extractAddresses(Map content, Map extractedData) {
        if (content.containsKey("addresses") && content.get("addresses") instanceof List) {
            List addresses = content.get("addresses")
            addresses.retainAll { it instanceof Map }

            Map<String, String> bannerAddressTypeCodeToHedmAddressTypeMap = getBannerAddressTypeToHedmAddressTypeMap()

            Collection extractedAddresses = []
            addresses.each {
                Map data = extractAddress(it, bannerAddressTypeCodeToHedmAddressTypeMap)
                if (isDuplicateAddressInRequest(extractedAddresses, data)) {
                    throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("addressType.duplicate", null))
                }
                extractedAddresses << data
            }

            extractedData.put("addresses", extractedAddresses)
        }
    }


    private def extractAddress(Map addressObj, Map<String, String> bannerAddressTypeCodeToHedmAddressTypeMap) {
        Map data = [:]

        if (addressObj.containsKey("address") && addressObj.get("address") instanceof Map) {
            extractAddressDetail(addressObj.get("address"), data)
        } else {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("address.requried", null))
        }

        if (addressObj.containsKey("type") && addressObj.get("type") instanceof Map) {
            Map type = addressObj.get("type")

            if (type.containsKey("addressType") && type.get("addressType") instanceof String) {
                String addressType = type.get("addressType").trim()

                HedmAddressType hedmAddressType = HedmAddressType.getByDataModelValue(addressType, GeneralValidationCommonConstants.VERSION_V6)
                if (!hedmAddressType) {
                    throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("addressType.inValid", null))
                }

                def mapEntry = bannerAddressTypeCodeToHedmAddressTypeMap.find { key, value -> value == addressType }
                if (!mapEntry) {
                    throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("addressType.not.found", null))
                }

                data.put("addressTypeCode", mapEntry.key)
            } else {
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("addressType.required", null))
            }
        } else {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("type.required", null))
        }

        String pattern = '^(-?(?:[1-9][0-9]*)?[0-9]{4})-(1[0-2]|0[1-9])-(3[0-1]|0[1-9]|[1-2][0-9])T(2[0-3]|[0-1][0-9]):([0-5][0-9]):([0-5][0-9])(\\.[0-9]+)?(Z|[+-](?:2[0-3]|[0-1][0-9]):[0-5][0-9])?$'

        if (addressObj.containsKey("startOn") && addressObj.get("startOn") instanceof String) {
            String startOn = addressObj.get("startOn").trim()
            if(!startOn.matches(pattern)){
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("invalid.date.format", []))
            }
            Date fromDate
            if (startOn.length() > 0) {
                fromDate = DateConvertHelperService.convertUTCStringToServerDate(startOn)
            }
            data.put("fromDate", fromDate)
        }

        if (addressObj.containsKey("endOn") && addressObj.get("endOn") instanceof String) {
            String endOn = addressObj.get("endOn").trim()
            if(!endOn.matches(pattern)){
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("invalid.date.format", []))
            }
            Date toDate
            if (endOn.length() > 0) {
                toDate = DateConvertHelperService.convertUTCStringToServerDate(endOn)
            }
            data.put("toDate", toDate)
        }

        return data
    }


    private void extractAddressDetail(Map addressDetailObj, Map data) {

        if (addressDetailObj.containsKey("addressLines") && addressDetailObj.get("addressLines") instanceof List) {
            List addressLines = addressDetailObj.get("addressLines")
            addressLines.retainAll { it instanceof String }

            if (addressLines.size() > 0) {
                data.put("streetLine1", addressLines[0])
            }
            if (addressLines.size() > 1) {
                data.put("streetLine2", addressLines[1])
            }
            if (addressLines.size() > 2) {
                data.put("streetLine3", addressLines[2])
            }
            if (addressLines.size() > 3) {
                data.put("streetLine4", addressLines[3])
            }
        }

        if (!data.get("streetLine1")) {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("addressLines.requried", null))
        }

        if (addressDetailObj.containsKey("place") && addressDetailObj.get("place") instanceof Map) {
            Map place = addressDetailObj.get("place")

            if (place.containsKey("country") && place.get("country") instanceof Map) {
                extractAddressCountry(place.get("country"), data)
            } else {
                unsetAddressCountry(data)
            }
        }

        if (addressDetailObj.containsKey("geographicAreas") && addressDetailObj.get("geographicAreas") instanceof List) {
            List geographicAreas = addressDetailObj.get("geographicAreas")
            geographicAreas.retainAll { it instanceof Map }
            data.put("geographicAreaGuids", geographicAreas.id?.unique())
        }

    }


    private void unsetAddressCountry(Map data) {
        data.put("iso3CountryCode", "")
        data.put("city", "")
        data.put("zip", "")
        data.put("isoStateCode", "")
        data.put("stateDescription", "")
        data.put("isoCountyCode", "")
        data.put("countyDescription", "")
        data.put("deliveryPoint", null)
        data.put("carrierRoute", "")
        data.put("correctionDigit", null)
    }


    private void extractAddressCountry(Map country, Map data) {
        if (country.containsKey("code") && country.get("code") instanceof String) {
            // The ISO 3166-1 alpha-3 country code
            data.put("iso3CountryCode", country.get("code").trim())
        } else {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("country.code.requried", null))
        }

        if (country.containsKey("locality") && country.get("locality") instanceof String) {
            data.put("city", country.get("locality").trim())
        }

        if (country.containsKey("postalCode") && country.get("postalCode") instanceof String) {
            data.put("zip", country.get("postalCode").trim())
        }

        if (country.containsKey("region") && country.get("region") instanceof Map) {
            // region within the country
            extractRegion(country.get("region"), data)
        }

        if (country.containsKey("subRegion") && country.get("subRegion") instanceof Map) {
            // Subregion within the country and region
            extractSubRegion(country.get("subRegion"), data)
        }

        if (data.get("iso3CountryCode") == "USA") {
            if (country.containsKey("deliveryPoint") && country.get("deliveryPoint") instanceof String) {
                String strDeliveryPoint = country.get("deliveryPoint").trim()
                Integer deliveryPoint
                if (strDeliveryPoint.length() > 0) {
                    deliveryPoint = Integer.valueOf(strDeliveryPoint)
                }
                data.put("deliveryPoint", deliveryPoint)
            }

            if (country.containsKey("carrierRoute") && country.get("carrierRoute") instanceof String) {
                data.put("carrierRoute", country.get("carrierRoute").trim())
            }

            if (country.containsKey("correctionDigit") && country.get("correctionDigit") instanceof String) {
                String strCorrectionDigit = country.get("correctionDigit").trim()
                Integer correctionDigit
                if (strCorrectionDigit.length() > 0) {
                    correctionDigit = Integer.valueOf(strCorrectionDigit)
                }
                data.put("correctionDigit", correctionDigit)
            }
        }
    }


    private void extractRegion(Map region, Map data) {
        if (region.containsKey("code") && region.get("code") instanceof String) {
            // ISO 3166-2 code of a region within the country OR empty string
            data.put("isoStateCode", region.get("code").trim())
        }

        if (region.containsKey("title") && region.get("title") instanceof String) {
            data.put("stateDescription", region.get("title").trim())
        }
    }


    private void extractSubRegion(Map subRegion, Map data) {
        if (subRegion.containsKey("code") && subRegion.get("code") instanceof String) {
            data.put("isoCountyCode", subRegion.get("code").trim())
        }

        if (subRegion.containsKey("title") && subRegion.get("title") instanceof String) {
            data.put("countyDescription", subRegion.get("title").trim())
        }
    }


    private boolean isDuplicateAddressInRequest(Collection extractedAddresses, Map currAddress) {
        boolean dup = false
        def existingAddress = extractedAddresses.find {
            it.addressTypeCode == currAddress.addressTypeCode
        }
        if (existingAddress) {
            dup = true
        }
        return dup
    }

    
    protected def getBannerAddressTypeToHedmAddressTypeMap() {
        return addressTypeCompositeService.getBannerAddressTypeToHedmV6AddressTypeMap()
    }


    protected def getBannerPhoneTypeToHedmPhoneTypeMap() {
        return phoneTypeCompositeService.getBannerPhoneTypeToHedmV6PhoneTypeMap()
    }

    protected def getBannerEmailTypeToHedmEmailTypeMap() {
        return emailTypeCompositeService.getBannerEmailTypeToHedmV6EmailTypeMap()
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
            fetchPersonsCredentialDataAndPutInMap(pidms, dataMap)

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
                if (dataMap.pidmToCredentialsMap.containsKey(it.pidm)) {
                    personCredentials = dataMap.pidmToCredentialsMap.get(it.pidm)
                }
                def existingBanId = personCredentials?.find { it.type == CredentialType.BANNER_ID }
                if (!existingBanId) {
                    personCredentials << [type: CredentialType.BANNER_ID, value: it.bannerId]
                }
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
            decorator.credentials = personCredentialV6CompositeService.createCredentialObjects(personCredentials)

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

    private void fetchPersonsCredentialDataAndPutInMap(Collection<Integer> pidms, Map dataMapForAll) {
        Map pidmToCredentialsMap = personCredentialService.getPidmToCredentialsMap(pidms)

        def credentialTypeToAdditionalIdTypeCodeMap = getCredentialTypeToAdditionalIdTypeCodeMap()

        def pidmToAdditionalIDsMap = personCredentialService.getPidmToAdditionalIDsMap(pidms, credentialTypeToAdditionalIdTypeCodeMap.values())

        pidmToAdditionalIDsMap.each { pidm, additionalIds ->
            def personCredentials = pidmToCredentialsMap.get(pidm)
            if (!personCredentials) {
                personCredentials = []
                pidmToCredentialsMap.put(pidm, personCredentials)
            }

            credentialTypeToAdditionalIdTypeCodeMap.each { credentialType, additionalIdTypeCode ->
                AdditionalID additionalID = additionalIds?.find {
                    it.additionalIdentificationType.code == additionalIdTypeCode
                }
                if (additionalID) {
                    personCredentials << [type: credentialType, value: additionalID.additionalId]
                }
            }
        }

        // Put in Map
        dataMapForAll.put("pidmToCredentialsMap", pidmToCredentialsMap)
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

        List<Long> personAddressSurrogateIds = pidmToAddressesMap?.values().id.flatten().unique()

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

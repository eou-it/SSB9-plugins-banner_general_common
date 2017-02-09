/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.IntegrationConfigurationService
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.person.AdditionalID
import net.hedtech.banner.general.person.PersonAddress
import net.hedtech.banner.general.person.PersonAdvancedSearchViewService
import net.hedtech.banner.general.person.PersonBasicPersonBase
import net.hedtech.banner.general.person.PersonBasicPersonBaseService
import net.hedtech.banner.general.person.PersonEmail
import net.hedtech.banner.general.person.PersonIdentificationNameAlternate
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.person.PersonIdentificationNameCurrentService
import net.hedtech.banner.general.person.PersonRace
import net.hedtech.banner.general.person.PersonTelephone
import net.hedtech.banner.general.person.view.PersonAdvancedSearchView
import net.hedtech.banner.general.system.ldm.Gender
import net.hedtech.banner.general.system.ldm.NameTypeCategory
import net.hedtech.banner.general.utility.DateConvertHelperService
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional

@Transactional
abstract class AbstractPersonCredentialCompositeService extends LdmService {

    PersonAdvancedSearchViewService personAdvancedSearchViewService

    PersonIdentificationNameCurrentService personIdentificationNameCurrentService

    PersonCredentialService personCredentialService

    PersonBasicPersonBaseService personBasicPersonBaseService

    IntegrationConfigurationService integrationConfigurationService

    abstract protected void prepareDataMapForAll_ListExtension(Collection<Map> entities, Map dataMapForAll)

    abstract protected void prepareDataMapForSingle_ListExtension(Map entity,
                                                                  final Map dataMapForAll, Map dataMapForSingle)

    abstract protected def createPersonCredentialDataModel(final Map dataMapForSingle)

    abstract protected def getCredentialTypeToAdditionalIdTypeCodeMap()

    protected Map extractDataFromRequestBody(Map personCredential) {
        def requestData = [:]

        /* Required in DataModel - Required in Banner */
        String personGuidInPayload
        if (personCredential.containsKey("id") && personCredential.get("id") instanceof String) {
            personGuidInPayload = personCredential?.id?.trim()?.toLowerCase()
            requestData.put('guid', personGuidInPayload)
        }

        // UPDATE operation - API SHOULD prefer the resource identifier on the URI, over the payload.
        String personGuidInURI = personCredential?.id?.trim()?.toLowerCase()
        if (personGuidInPayload && !personGuidInPayload.equals(personGuidInPayload)) {
            personCredential.put('id', personGuidInURI)
            requestData.put('guid', personGuidInURI)
        }

        if (personCredential.containsKey("credentials") && personCredential.get("credentials") instanceof List) {
            Collection extractedCreds = extractCredentials(personCredential)
            requestData.put("credentials", extractedCreds)
        }

        return requestData
    }


     def extractCredentials(Map content) {
        if (content.containsKey("credentials") && content.get("credentials") instanceof List) {
            List credentials = content.get("credentials")
            credentials.retainAll { it instanceof Map }

            if (credentials.type.contains(CredentialType.SOCIAL_SECURITY_NUMBER.versionToEnumMap[GeneralValidationCommonConstants.VERSION_V8]) && credentials.type.contains(CredentialType.SOCIAL_INSURANCE_NUMBER.versionToEnumMap[GeneralValidationCommonConstants.VERSION_V8])) {
                throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("ssn.sin.both.not.valid", null))
            }

            Collection extractedCreds = []
            credentials.each {
                extractedCreds << extractCredential(it)
            }
            return extractedCreds
        }
    }

    def extractCredential(Map credentialObj) {
        CredentialType credentialType
        String value

        if (credentialObj.containsKey("type") && credentialObj.get("type") instanceof String) {
            credentialType = CredentialType.getByDataModelValue(credentialObj.get("type").trim(), GeneralValidationCommonConstants.VERSION_V8)
        }

        if (credentialObj.containsKey("value") && credentialObj.get("value") instanceof String) {
            // value is required, but no "minLength" constraint.  So it can be zero-length string
            value = credentialObj.get("value").trim()
        }

        if (!credentialType || value == null) {
            throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("invalid.credentialType", null))
        }
        if (credentialType == CredentialType.SOCIAL_SECURITY_NUMBER && value.length() > 9) {
            throw new ApplicationException("AbstractPersonCredentialCompositeService", new BusinessLogicValidationException("ssn.length.message", null))
        }
        if (credentialType == CredentialType.SOCIAL_INSURANCE_NUMBER && value.length() > 9) {
            throw new ApplicationException("AbstractPersonCredentialCompositeService", new BusinessLogicValidationException("ssn.length.message", null))
        }

        return [type: credentialType, value: value]
    }

    private def createPersonCredentialDataModels(def entities) {
        def decorators = []
        if (entities) {
            def dataMapForAll = prepareDataMapForAll_List(entities)

            entities.each {
                def dataMapForSingle = prepareDataMapForSingle_List(it, dataMapForAll)
                decorators << createPersonCredentialDataModel(dataMapForSingle)
            }
        }
        return decorators
    }

    private def prepareDataMapForAll_List(Collection<Map> entities) {
        def dataMapForAll = [:]

        if (entities) {
            List<Integer> pidms = []
            entities?.each {
                pidms << it["personIdentificationNameCurrent"].pidm
            }
            def pidmToPersonBaseMap = getPidmToPersonBaseMap(pidms)
            dataMapForAll.put("pidmToPersonBaseMap", pidmToPersonBaseMap)

            fetchPersonsCredentialDataAndPutInMap(pidms, dataMapForAll)
        }

        // Call extension
        prepareDataMapForAll_ListExtension(entities, dataMapForAll)
        return dataMapForAll
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

    private def prepareDataMapForSingle_List(Map entity,
                                             final Map dataMapForAll) {
        Map dataMapForSingle = initDataMapForSingle("LIST", entity)

        if (dataMapForAll.pidmToCredentialsMap.containsKey(dataMapForSingle.pidm)) {
            dataMapForSingle.put("credentials", dataMapForAll.pidmToCredentialsMap.get(dataMapForSingle.pidm))
        }

        dataMapForSingle.credentials << [type: CredentialType.BANNER_ID, value: dataMapForSingle.bannerId]

        PersonBasicPersonBase personBase = dataMapForAll.pidmToPersonBaseMap.get(dataMapForSingle.pidm)
        def existingSsn = dataMapForSingle.credentials?.find { it.type == CredentialType.SOCIAL_SECURITY_NUMBER }
        if (!existingSsn && personBase?.ssn) {
            dataMapForSingle.credentials << [type: CredentialType.SOCIAL_SECURITY_NUMBER, value: personBase.ssn]
        }

        // Call extension
        prepareDataMapForSingle_ListExtension(entity, dataMapForAll, dataMapForSingle)
        return dataMapForSingle
    }

    private def initDataMapForSingle(String sourceForDataMap,Map entity) {
        Map dataMapForSingle = [:]
        dataMapForSingle.put("sourceForDataMap", sourceForDataMap)
        dataMapForSingle.put("personData", entity)

        dataMapForSingle.put("pidm", entity["personIdentificationNameCurrent"].pidm)
        dataMapForSingle.put("bannerId", entity["personIdentificationNameCurrent"]?.bannerId?:null)
        dataMapForSingle.put("guid", entity["globalUniqueIdentifier"].guid)

        return dataMapForSingle
    }

    private def prepareDataMapForSingle_Create(Map entity) {
        Map dataMapForSingle = initDataMapForSingle("CREATE", entity)

        dataMapForSingle.put("credentials", entity.credentials)
        return dataMapForSingle
    }


    @Transactional
    def update(Map personCredential) {

        def ssnCredentialObj
        def bannerIdCredentialObj
        PersonIdentificationNameCurrent personIdentification
        Map additionalIdTypeCodeToIdMap = [:]

        Map requestData = extractDataFromRequestBody(personCredential)

        String personGuid = requestData.get('guid')

        //retrieve guid
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByGuid('persons', personGuid)
        if (!globalUniqueIdentifier) {
            throw new ApplicationException("Person", new NotFoundException())
        }
        requestData.put "globalUniqueIdentifier",globalUniqueIdentifier

        //retrieve person identification
        Integer pidm = globalUniqueIdentifier.domainKey?.toInteger()

        List<PersonIdentificationNameCurrent> personIdentificationList = PersonIdentificationNameCurrent.findAllByPidmInList([pidm])

        personIdentificationList.each { identification ->
            if (identification.changeIndicator == null) {
                personIdentification = identification
            }
        }
        requestData.put "personIdentificationNameCurrent", personIdentification

        //retrieve credentials data
        if (requestData.containsKey("credentials")) {
            def personCredentials = requestData.get("credentials")

                ssnCredentialObj = personCredentials.find {
                    it.type == CredentialType.SOCIAL_SECURITY_NUMBER
                }
                if (!ssnCredentialObj) {
                    ssnCredentialObj = personCredentials.find {
                        it.type == CredentialType.SOCIAL_INSURANCE_NUMBER
                    }
                }

                bannerIdCredentialObj = personCredentials.find {
                    it.type == CredentialType.BANNER_ID
                }

                Map credentialTypeToAdditionalIdTypeCodeMap = getCredentialTypeToAdditionalIdTypeCodeMap()
                credentialTypeToAdditionalIdTypeCodeMap.each { credentialType, additionalIdTypeCode ->
                    def obj = personCredentials?.find {
                        it.type == credentialType
                    }
                    if (obj) {
                        log.debug "$credentialType --- $additionalIdTypeCode --- ${obj.value}"
                        additionalIdTypeCodeToIdMap.put(additionalIdTypeCode, obj.value)
                        personCredentials.remove(obj)
                    }
                }
        }

        //updates banner id
        if (bannerIdCredentialObj && (bannerIdCredentialObj.value?.length() > 0 && bannerIdCredentialObj.value?.length() <= 9)) {
            if (personIdentification.bannerId != bannerIdCredentialObj.value) {
                PersonIdentificationNameCurrent personIdentificationNameCurrent = PersonIdentificationNameCurrent.fetchByBannerId(bannerIdCredentialObj.value)
                if (!personIdentificationNameCurrent) {
                    personIdentification.bannerId = bannerIdCredentialObj.value
                    personIdentificationNameCurrentService.update(personIdentification)
                } else {
                    throw new ApplicationException(this.class.simpleName, new BusinessLogicValidationException("bannerId.already.exists", null))
                }
            }
        }

        //updates alternate ids
        if (additionalIdTypeCodeToIdMap) {
            def additionalIdsToRemove = additionalIdTypeCodeToIdMap.findAll { key, value -> !value }
            additionalIdsToRemove.each { key, value -> additionalIdTypeCodeToIdMap.remove(key) }
            personCredentialService.deleteAdditionalIDs(personIdentification.pidm, additionalIdsToRemove.keySet())
            personCredentialService.createOrUpdateAdditionalIDs(personIdentification.pidm, additionalIdTypeCodeToIdMap)
        }

        //personBasicPersonBase
        List<PersonBasicPersonBase> personBaseList = PersonBasicPersonBase.findAllByPidmInList([pidm])
        //updates ssn and sin
        personBaseList.each { personBase ->
            if (ssnCredentialObj) {
                if (personBase.ssn == null || integrationConfigurationService.canUpdatePersonSSN()) {
                    personBase.ssn = ssnCredentialObj.value
                    personBasicPersonBaseService.update(personBase)
                }
            }
        }
        return prepareDataMapForSingle_Create(requestData)
    }

    /**
     * GET /api/persons-credentials
     *
     * @param params Request parameters
     * @return
     */
    @Transactional(readOnly = true)
    def get(id) {
        log.trace "getById:Begin:$id"

        Map personDetail =personIdentificationNameCurrentService.fetchByGuid(id)

        if (!personDetail) {
            throw new ApplicationException("Person", new NotFoundException())
        }

        return createPersonCredentialDataModels([personDetail])[0]
    }

    /**
     * GET /api/persons-credentials
     *
     * @param params Request parameters
     * @return
     */
    @Transactional(readOnly = true)
    def list(Map params) {
        log.trace "list:Begin:$params"

        RestfulApiValidationUtility.correctMaxAndOffset(params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        int max= new Integer(params?.max?:0)
        int offset = new Integer(params?.offset?: 0)
        def personsList = personAdvancedSearchViewService.fetchAllByCriteria([:], null, null, max, offset)
        def pidms =personsList*.pidm
        def personDetailsList= personIdentificationNameCurrentService.fetchAllWithGuidByPidmInList(pidms)

        return createPersonCredentialDataModels(personDetailsList)
    }


    def count(Map params) {
        return personAdvancedSearchViewService.countByCriteria([:])
    }

     def getPidmToPersonBaseMap(List<Integer> pidms) {
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



}

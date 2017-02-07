/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.ldm.GlobalUniqueIdentifier
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.person.AdditionalID
import net.hedtech.banner.general.person.PersonAdvancedSearchViewService
import net.hedtech.banner.general.person.PersonBasicPersonBase
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.person.PersonIdentificationNameCurrentService
import net.hedtech.banner.general.person.view.PersonAdvancedSearchView
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional

@Transactional
abstract class AbstractPersonCredentialCompositeService extends LdmService {

    PersonAdvancedSearchViewService personAdvancedSearchViewService

    PersonIdentificationNameCurrentService personIdentificationNameCurrentService

    PersonCredentialService personCredentialService

    abstract protected void prepareDataMapForAll_ListExtension(Collection<Map> entities, Map dataMapForAll)

    abstract protected void prepareDataMapForSingle_ListExtension(Map entity,
                                                                  final Map dataMapForAll, Map dataMapForSingle)

    abstract protected def createPersonCredentialDataModel(final Map dataMapForSingle)

    abstract protected def extractDataFromRequestBody(final Map content)

    abstract protected def getCredentialTypeToAdditionalIdTypeCodeMap()

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
        dataMapForSingle.put("bannerId", entity["personIdentificationNameCurrent"].bannerId)
        dataMapForSingle.put("guid", entity["globalUniqueIdentifier"].guid)

        return dataMapForSingle
    }

    private def prepareDataMapForSingle_Create(Map entity) {
        Map dataMapForSingle = initDataMapForSingle("CREATE", entity)
        return dataMapForSingle
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

/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.person.AdditionalID
import net.hedtech.banner.general.person.PersonBasicPersonBase
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional

@Transactional
abstract class AbstractPersonCredentialCompositeService extends LdmService {

    private static final List<String> VERSIONS = [GeneralValidationCommonConstants.VERSION_V6]

    PersonCredentialService personCredentialService

    abstract protected void prepareDataMapForAll_ListExtension(Collection<Object[]> entities, Map dataMapForAll)

    abstract protected void prepareDataMapForSingle_ListExtension(Object[] entity,
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

    private def prepareDataMapForAll_List(Collection<Object[]> entities) {
        def dataMapForAll = [:]

        if (entities) {
            List<Integer> pidms = []
            entities?.each {
                pidms << it.getAt(0)
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

    private def prepareDataMapForSingle_List(Object[] entity,
                                             final Map dataMapForAll) {
        Map dataMapForSingle = initDataMapForSingle("LIST", entity)

        if (dataMapForAll.pidmToCredentialsMap.containsKey(dataMapForSingle.pidm)) {
            dataMapForSingle.put("credentials", dataMapForAll.pidmToCredentialsMap.get(dataMapForSingle.pidm))
        }

        dataMapForSingle.credentials << [type: CredentialType.BANNER_ID, value: dataMapForSingle.bannerId]

        // Call extension
        prepareDataMapForSingle_ListExtension(entity, dataMapForAll, dataMapForSingle)
        return dataMapForSingle
    }

    private def initDataMapForSingle(String sourceForDataMap, Object[] entity) {
        Map dataMapForSingle = [:]
        dataMapForSingle.put("sourceForDataMap", sourceForDataMap)
        dataMapForSingle.put("personData", entity)

        dataMapForSingle.put("pidm", entity.getAt(0))
        dataMapForSingle.put("bannerId", entity.getAt(1))
        dataMapForSingle.put("guid", entity.getAt(2))

        return dataMapForSingle
    }

    private def prepareDataMapForSingle_Create(Object[] entity) {
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
        String acceptVersion = getAcceptVersion(VERSIONS)

        def persons = [:]
        Object[] personDetail = fetchPersons(["guid": id])
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
        String acceptVersion = getAcceptVersion(VERSIONS)

        RestfulApiValidationUtility.correctMaxAndOffset(params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        List<Object[]> personDetailsList = fetchPersons(params)
        return createPersonCredentialDataModels(personDetailsList)
    }


    def count(Map params) {
        return fetchPersons(params, true)
    }

    /**
     * fetch person details
     * @param params
     */
    private def fetchPersons(Map params, boolean count = false) {
        log.trace "fetchPersons: Begin: $params"
        def result
        String hql
        if (count) {
            hql = ''' select count(*) '''
        } else {
            hql = ''' select a.pidm, a.bannerId, b.guid '''
        }
        hql += ''' from PersonIdentificationNameCurrent a, GlobalUniqueIdentifier b WHERE b.ldmName = :ldmName and a.pidm = b.domainKey and a.entityIndicator = 'P' '''
        if (params?.containsKey("guid")) {
            hql += ''' and b.guid = :guid '''
        }
        log.debug "$hql"
        PersonIdentificationNameCurrent.withSession { session ->
            def query = session.createQuery(hql).
                    setString(GeneralCommonConstants.QUERY_PARAM_LDM_NAME, GeneralCommonConstants.PERSONS_LDM_NAME)
            if (params?.containsKey("guid")) {
                result = query.setString(GeneralCommonConstants.PERSONS_GUID_NAME, params.guid).uniqueResult()
                log.debug "query returned $result"
            } else {
                if (count) {
                    result = query.uniqueResult()
                    log.debug "query returned $result"
                } else {
                    result = query.setMaxResults(params?.max as Integer).setFirstResult((params?.offset ?: '0') as Integer).list()
                    log.debug "query returned ${result.size()} rows"
                }
            }
        }
        log.trace "fetchPersons: End"
        return result
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

/*******************************************************************************
 Copyright 2016-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.overall.ImsSourcedIdBase
import net.hedtech.banner.general.overall.PidmAndUDCIdMapping
import net.hedtech.banner.general.overall.ThirdPartyAccess
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.overall.ldm.v6.PersonCredential
import net.hedtech.banner.general.overall.ldm.v6.PersonCredentialsDecorator
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional

@Transactional
class PersonCredentialCompositeService extends LdmService {

    /**
     * GET /api/persons-credentials
     *
     * @param params Request parameters
     * @return
     */
    @Transactional(readOnly = true)
    def get(id) {
        log.trace "getById:Begin:$id"
        def persons = [:]
        Object[] personDetail = fetchPersons(["guid": id])
        if (!personDetail) {
            throw new ApplicationException("Person", new NotFoundException())
        }
        return createDecorators([personDetail])[0]
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
        List<Object[]> personDetailsList = fetchPersons(params)
        return createDecorators(personDetailsList)
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


    private def createDecorators(def dbRows) {
        def decorators = []
        if (dbRows) {
            List<Integer> pidms = []
            dbRows?.each {
                pidms << it.getAt(0)
            }

            def dataMap = [:]
            fetchPersonsCredentialDataAndPutInMap(pidms, dataMap)

            dbRows?.each {
                Integer pidm = it.getAt(0)
                String bannerId = it?.getAt(1)
                String guid = it?.getAt(2)

                PersonCredentialsDecorator persCredentialsDecorator = new PersonCredentialsDecorator(guid)

                def credentials = []
                if (dataMap.pidmToCredentialsMap.containsKey(pidm)) {
                    credentials = dataMap.pidmToCredentialsMap.get(pidm)
                }
                credentials << [type: CredentialType.BANNER_ID, value: bannerId]
                persCredentialsDecorator.credentials = createCredentialDecorators(credentials)

                decorators.add(persCredentialsDecorator)
            }
        }
        return decorators
    }


    void fetchPersonsCredentialDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        def pidmToCredentialsMap = [:]
        if (pidms) {
            def pidmToSourcedIdMap = fetchSourcedIds(pidms)
            def pidmToPartnerSystemLoginIdMap = fetchThirdPartySystemLoginIds(pidms)
            def pidmToUdcIdMap = fetchUdcIds(pidms)

            pidms.each {
                def credentials = []
                pidmToCredentialsMap.put(it, credentials)
                if (pidmToSourcedIdMap.containsKey(it) && pidmToSourcedIdMap.get(it)) {
                    credentials << [type: CredentialType.BANNER_SOURCED_ID, value: pidmToSourcedIdMap.get(it)]
                }
                if (pidmToPartnerSystemLoginIdMap.containsKey(it) && pidmToPartnerSystemLoginIdMap.get(it)) {
                    credentials << [type: CredentialType.BANNER_USER_NAME, value: pidmToPartnerSystemLoginIdMap.get(it)]
                }
                if (pidmToUdcIdMap.containsKey(it) && pidmToUdcIdMap.get(it)) {
                    credentials << [type: CredentialType.BANNER_UDC_ID, value: pidmToUdcIdMap.get(it)]
                }
            }
        }
        // Put in Map
        dataMap.put("pidmToCredentialsMap", pidmToCredentialsMap)
    }


    private def fetchSourcedIds(List<Integer> pidms) {
        def pidmToSourcedIdMap = [:]
        if (pidms) {
            log.debug "Getting GOBSRID records for ${pidms?.size()} PIDMs..."
            List<ImsSourcedIdBase> entities = ImsSourcedIdBase.findAllByPidmInList(pidms)
            log.debug "Got ${entities?.size()} GOBSRID records"
            entities?.each {
                pidmToSourcedIdMap.put(it.pidm, it.sourcedId)
            }
        }
        return pidmToSourcedIdMap
    }


    private def fetchThirdPartySystemLoginIds(List<Integer> pidms) {
        def pidmToPartnerSystemLoginIdMap = [:]
        if (pidms) {
            log.debug "Getting GOBTPAC records for ${pidms?.size()} PIDMs..."
            List<ThirdPartyAccess> entities = ThirdPartyAccess.findAllByPidmInList(pidms)
            log.debug "Got ${entities?.size()} GOBTPAC records"
            entities?.each {
                pidmToPartnerSystemLoginIdMap.put(it.pidm, it.externalUser)
            }
        }
        return pidmToPartnerSystemLoginIdMap
    }


    private def fetchUdcIds(List<Integer> pidms) {
        def pidmToUdcIdMap = [:]
        if (pidms) {
            log.debug "Getting GOBUMAP records for ${pidms?.size()} PIDMs..."
            List<PidmAndUDCIdMapping> entities = PidmAndUDCIdMapping.findAllByPidmInList(pidms)
            log.debug "Got ${entities?.size()} GOBUMAP records"
            entities?.each {
                pidmToUdcIdMap.put(it.pidm, it.udcId)
            }
        }
        return pidmToUdcIdMap
    }


    def createCredentialDecorators(def credentials) {
        def decorators = []
        if (credentials) {
            credentials.each {
                decorators << createCredentialV6(it.type, it.value)
            }
        }
        return decorators
    }


    private PersonCredential createCredentialV6(CredentialType credentialType, String value) {
        PersonCredential personCredential
        if (credentialType && value) {
            personCredential = new PersonCredential(credentialType.versionToEnumMap["v6"], value)
        }
        return personCredential
    }

}
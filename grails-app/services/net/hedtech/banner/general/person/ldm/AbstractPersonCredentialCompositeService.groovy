/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.overall.ldm.v6.PersonCredential
import net.hedtech.banner.general.overall.ldm.v6.PersonCredentialsDecorator
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.person.ldm.v1.Credential
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional

@Transactional
class AbstractPersonCredentialCompositeService extends LdmService {

    private static final List<String> VERSIONS = [GeneralValidationCommonConstants.VERSION_V6]

    PersonCredentialService personCredentialService

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
        String acceptVersion = getAcceptVersion(VERSIONS)

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
                persCredentialsDecorator.credentials = createCredentialObjectsV6(credentials)

                decorators.add(persCredentialsDecorator)
            }
        }
        return decorators
    }


    void fetchPersonsCredentialDataAndPutInMap(List<Integer> pidms, Map dataMap) {
        // Put in Map
        dataMap.put("pidmToCredentialsMap", personCredentialService.getPidmToCredentialsMap(pidms))
    }


    def createCredentialObjectsV3(def credentials) {
        return createCredentialObjects(credentials, GeneralValidationCommonConstants.VERSION_V3)
    }


    def createCredentialObjectsV6(def credentials) {
        return createCredentialObjects(credentials, GeneralValidationCommonConstants.VERSION_V6)
    }


    private def createCredentialObjects(def credentials, String version) {
        def decorators = []
        if (credentials) {
            credentials.each {
                if (GeneralValidationCommonConstants.VERSION_V6.equals(version)) {
                    decorators << createCredentialObjectV6(it.type, it.value)
                } else if (GeneralValidationCommonConstants.VERSION_V3.equals(version)) {
                    decorators << createCredentialObjectV3(it.type, it.value)
                }
            }
        }
        return decorators
    }


    private PersonCredential createCredentialObjectV6(CredentialType credentialType, String value) {
        PersonCredential personCredential
        if (credentialType && value) {
            personCredential = new PersonCredential(credentialType.versionToEnumMap["v6"], value)
        }
        return personCredential
    }


    private Credential createCredentialObjectV3(CredentialType credentialType, String value) {
        return new Credential(credentialType.versionToEnumMap["v3"], value, null, null)
    }

}

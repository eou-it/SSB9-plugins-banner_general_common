/*******************************************************************************
 Copyright 2016-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.person.ldm.v1.Person
import net.hedtech.banner.general.overall.ldm.v6.PersonCredentialDecorator
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional
import net.hedtech.banner.general.overall.ldm.LdmService

@Transactional
class PersonCredentialCompositeService extends LdmService {

    def personCompositeService

    /**
     * GET /api/persons-credentials
     *
     * @param params Request parameters
     * @return
     */
    @Transactional(readOnly = true)
    def get(id) {
        log.debug "getById:Begin:$id"
        def persons = [:]
        def personDetail = fetchPersons(["guid": id])
        if (!personDetail) {
            throw new ApplicationException("Person", new NotFoundException())
        }
        Integer pidm = personDetail.getAt(0)
        Map personCredentialsMap = personCompositeService.getPersonCredentialDetails([pidm])
        def resultList = buildPersonCredentials(persons, personCredentialsMap, [personDetail])
        log.debug "getById:End:$resultList"
        return resultList.get(pidm)
    }

    /**
     * GET /api/persons-credentials
     *
     * @param params Request parameters
     * @return
     */
    @Transactional(readOnly = true)
    def list(Map params) {
        log.debug "list:Begin:$params"
        def resultList = [:]
        def total = 0
        RestfulApiValidationUtility.correctMaxAndOffset(params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        def personDetailsList = fetchPersons(params)
        def persons = [:]
        List pidms = []
        personDetailsList?.each {
            pidms << it.getAt(0)
        }
        Map personCredentialsMap = personCompositeService.getPersonCredentialDetails(pidms)

        resultList = buildPersonCredentials(persons, personCredentialsMap, personDetailsList)

        total = fetchPersons(params, true)

        try {
            resultList = this.class.classLoader.loadClass('net.hedtech.restfulapi.PagedResultArrayList').newInstance(resultList?.values() ?: [], total)
        }
        catch (ClassNotFoundException e) {
            resultList = resultList.values()
        }
        log.debug "list:End:${resultList.size()}"
        return resultList
    }

    /**
     * fetch person details
     * @param params
     */
    def static fetchPersons(Map params, boolean count = false) {
        log.debug "buildPersonCredentials: Begin: $params"
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
        PersonIdentificationNameCurrent.withSession { session ->
            def query = session.createQuery(hql).
                    setString(GeneralCommonConstants.QUERY_PARAM_LDM_NAME, GeneralCommonConstants.PERSONS_LDM_NAME)
            if (params?.containsKey("guid")) {
                result = query.setString(GeneralCommonConstants.PERSONS_GUID_NAME, params.guid).uniqueResult()
            } else {
                if (count) {
                    result = query.uniqueResult()
                } else {
                    result = query.setMaxResults(params?.max as Integer).setFirstResult((params?.offset ?: '0') as Integer).list()
                }
            }

            log.trace "buildPersonCredentials: End"
            return result
        }
    }


    private buildPersonCredentials(def persons, Map credentialsMap, def personDetailsList) {
        log.trace "buildPersonCredentials: Begin"
        personDetailsList?.each { it ->
            Person person = new Person(null)
            person.guid = it?.getAt(2)
            person.credentials << new PersonCredentialDecorator("bannerId", it?.getAt(1))
            persons.put(it?.getAt(0), person)
        }

        credentialsMap.imsSourcedIdBaseList.each { sourcedIdBase ->
            Person person = persons.get(sourcedIdBase.pidm)
            person.credentials << new PersonCredentialDecorator("bannerSourcedId", sourcedIdBase.sourcedId)
        }
        credentialsMap.thirdPartyAccessList.each { thirdPartyAccess ->
            if (thirdPartyAccess.externalUser) {
                Person person = persons.get(thirdPartyAccess.pidm)
                person.credentials << new PersonCredentialDecorator("bannerUserName", thirdPartyAccess.externalUser)
            }
        }
        credentialsMap.pidmAndUDCIdMappingList.each { pidmAndUDCIdMapping ->
            Person person = persons.get(pidmAndUDCIdMapping.pidm)
            person.credentials << new PersonCredentialDecorator("bannerUdcId", pidmAndUDCIdMapping.udcId)
        }
        log.trace "buildPersonCredentials: End"
        return persons
    }

}

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
import net.hedtech.banner.general.overall.ldm.v6.PersonCredential
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.general.overall.ldm.v6.PersonCredentialsDecorator
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional
import net.hedtech.banner.general.overall.ldm.LdmService

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
        log.debug "getById:Begin:$id"
        def persons = [:]
        def personDetail = fetchPersons(["guid": id])
        if (!personDetail) {
            throw new ApplicationException("Person", new NotFoundException())
        }
        Integer pidm = personDetail.getAt(0)
        Map personCredentialsMap = getPersonCredentialDetails([pidm])
        log.trace "getById:End"
        return buildDecorators(persons, personCredentialsMap, [personDetail])?.getAt(0)
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
        RestfulApiValidationUtility.correctMaxAndOffset(params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        List<Objects[]> personDetailsList = fetchPersons(params)
        def decorators = [:]
        List pidms = []
        personDetailsList?.each {
            pidms << it.getAt(0)
        }
        Map personCredentialsMap = getPersonCredentialDetails(pidms)

        return buildDecorators(decorators, personCredentialsMap, personDetailsList)
    }

    def count(Map params) {
        return fetchPersons(params, true)
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


    private Map getPersonCredentialDetails(List pidms) {
        log.trace "getPersonCredentialDetails:Begin"
        List<ImsSourcedIdBase> imsSourcedIdBaseList = ImsSourcedIdBase.findAllByPidmInList(pidms)
        List<ThirdPartyAccess> thirdPartyAccessList = ThirdPartyAccess.findAllByPidmInList(pidms)
        List<PidmAndUDCIdMapping> pidmAndUDCIdMappingList = PidmAndUDCIdMapping.findAllByPidmInList(pidms)
        log.trace "getPersonCredentialDetails:End"
        return [imsSourcedIdBaseList: imsSourcedIdBaseList, thirdPartyAccessList: thirdPartyAccessList, pidmAndUDCIdMappingList: pidmAndUDCIdMappingList]
    }


    private def buildDecorators(def decorators, Map credentialsMap, def personDetailsList) {
        log.trace "buildPersonCredentials: Begin"
        personDetailsList?.each { it ->
            PersonCredentialsDecorator persCredentialsDecorator = new PersonCredentialsDecorator(it?.getAt(2))
            persCredentialsDecorator.credentials << new PersonCredential("bannerId", it?.getAt(1))
            decorators.put(it?.getAt(0), persCredentialsDecorator)
        }

        credentialsMap.imsSourcedIdBaseList.each { sourcedIdBase ->
            PersonCredentialsDecorator persCredentialsDecorator = decorators.get(sourcedIdBase.pidm)
            persCredentialsDecorator.credentials << new PersonCredential("bannerSourcedId", sourcedIdBase.sourcedId)
        }
        credentialsMap.thirdPartyAccessList.each { thirdPartyAccess ->
            if (thirdPartyAccess.externalUser) {
                PersonCredentialsDecorator persCredentialsDecorator = decorators.get(thirdPartyAccess.pidm)
                persCredentialsDecorator.credentials << new PersonCredential("bannerUserName", thirdPartyAccess.externalUser)
            }
        }
        credentialsMap.pidmAndUDCIdMappingList.each { pidmAndUDCIdMapping ->
            PersonCredentialsDecorator persCredentialsDecorator = decorators.get(pidmAndUDCIdMapping.pidm)
            persCredentialsDecorator.credentials << new PersonCredential("bannerUdcId", pidmAndUDCIdMapping.udcId)
        }

        log.trace "buildPersonCredentials: End"
        return decorators.values()
    }

}

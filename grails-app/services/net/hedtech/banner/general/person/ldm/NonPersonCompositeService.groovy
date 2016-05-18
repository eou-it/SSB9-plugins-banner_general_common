/*******************************************************************************
 Copyright 2016-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralCommonConstants
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.overall.ldm.v6.NonPersonDecorator
import net.hedtech.banner.general.person.PersonIdentificationNameCurrent
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional

@Transactional
class NonPersonCompositeService extends LdmService {

    /**
     * GET /api/organizations
     *
     * @param params Request parameters
     * @return
     */
    @Transactional(readOnly = true)
    def get(id) {
        log.trace "getById:Begin:$id"
        Object[] nonPersonDetail = fetchNonPersons(["guid": id])
        if (!nonPersonDetail) {
            throw new ApplicationException("organization", new NotFoundException())
        }
        log.trace "getById:End"
        return buildDecorators([nonPersonDetail])?.getAt(0)
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
        RestfulApiValidationUtility.correctMaxAndOffset(params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        List<Object[]> nonPersonDetailsList = fetchNonPersons(params)
        return buildDecorators(nonPersonDetailsList)
    }

    def count(Map params) {
        return fetchNonPersons(params, true)
    }

    /**
     * fetch non-person details
     * @param params
     */
    private def fetchNonPersons(Map params, boolean count = false) {
        log.trace "fetchNonPersons: Begin: $params"
        def result
        String hql
        if (count) {
            hql = ''' select count(*) '''
        } else {
            hql = ''' select a.bannerId, a.lastName, b.guid '''
        }
        hql += ''' from PersonIdentificationNameCurrent a, GlobalUniqueIdentifier b WHERE b.ldmName = :ldmName and a.pidm = b.domainKey and a.entityIndicator = 'C' '''
        if (params?.containsKey("guid")) {
            hql += ''' and b.guid = :guid '''
        }
        log.debug "$hql"
        PersonIdentificationNameCurrent.withSession { session ->
            def query = session.createQuery(hql).
                    setString(GeneralCommonConstants.QUERY_PARAM_LDM_NAME, GeneralCommonConstants.NON_PERSONS_LDM_NAME)
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
            log.trace "fetchNonPersons: End"
            return result
        }
    }

    private def buildDecorators(def nonPersonDetailsList) {
        log.trace "buildDecorators: Begin"
        List<NonPersonDecorator> decorators = []
        nonPersonDetailsList?.each { it ->
            decorators << new NonPersonDecorator(it.getAt(0), it.getAt(1), it.getAt(2))
        }
        log.trace "buildDecorators: End"
        return decorators
    }

}

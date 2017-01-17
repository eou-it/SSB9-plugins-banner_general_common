/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.general.overall.ImsSourcedIdBase
import net.hedtech.banner.general.overall.PidmAndUDCIdMapping
import net.hedtech.banner.general.overall.ThirdPartyAccess
import net.hedtech.banner.general.person.AdditionalID
import net.hedtech.banner.general.person.AdditionalIDService
import net.hedtech.banner.general.system.AdditionalIdentificationType


class PersonCredentialService {

    boolean transactional = true

    AdditionalIDService additionalIDService


    def getPidmToCredentialsMap(Collection<Integer> pidms) {
        Map pidmToCredentialsMap = [:]
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
        return pidmToCredentialsMap
    }


    def getPidmToAdditionalIDsMap(Collection<Integer> pidms, Collection<String> identificationTypeCodes) {
        def pidmToAdditionalIDsMap = [:]
        if (pidms && identificationTypeCodes) {
            def entities = additionalIDService.fetchAllByPidmInListAndIdentificationTypeCodeInList(pidms, identificationTypeCodes)
            entities.each {
                Collection<AdditionalID> personAdditionalIDs = []
                if (pidmToAdditionalIDsMap.containsKey(it.pidm)) {
                    personAdditionalIDs = pidmToAdditionalIDsMap.get(it.pidm)
                } else {
                    pidmToAdditionalIDsMap.put(it.pidm, personAdditionalIDs)
                }
                personAdditionalIDs.add(it)
            }
        }
        return pidmToAdditionalIDsMap
    }


    def createOrUpdateAdditionalIDs(Integer pidm, Map additionalIdTypeCodeToIdMap) {
        Collection<AdditionalID> list = []
        if (pidm && additionalIdTypeCodeToIdMap) {
            additionalIdTypeCodeToIdMap.each { additionalIdTypeCode, additionalId ->
                if (additionalIdTypeCode && additionalId) {
                    list << createOrUpdateAdditionalID(pidm, additionalIdTypeCode, additionalId)
                }
            }
        }
        return list
    }


    AdditionalID createOrUpdateAdditionalID(Integer pidm, String additionalIdTypeCode, String additionalId) {
        AdditionalID entityAdditionalID
        AdditionalIdentificationType additionalIdType = AdditionalIdentificationType.findByCode(additionalIdTypeCode)
        if (pidm && additionalIdType && additionalId) {
            Collection<AdditionalID> entities = additionalIDService.fetchAllByPidmInListAndIdentificationTypeCodeInList([pidm], [additionalIdType.code])
            entityAdditionalID = entities ? entities[0] : null
            if (!entityAdditionalID) {
                entityAdditionalID = new AdditionalID(pidm: pidm, additionalIdentificationType: additionalIdType)
            }
            entityAdditionalID.additionalId = additionalId
            entityAdditionalID = additionalIDService.createOrUpdate(entityAdditionalID)
        }
        return entityAdditionalID
    }


    void deleteAdditionalIDs(Integer pidm, Collection<String> additionalIdTypeCodes) {
        if (pidm && additionalIdTypeCodes) {
            Collection<AdditionalID> entities = additionalIDService.fetchAllByPidmInListAndIdentificationTypeCodeInList([pidm], additionalIdTypeCodes)
            additionalIDService.delete(entities)
        }
    }


    private def fetchSourcedIds(Collection<Integer> pidms) {
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


    private def fetchThirdPartySystemLoginIds(Collection<Integer> pidms) {
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


    private def fetchUdcIds(Collection<Integer> pidms) {
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


}

/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.IntegrationPartnerSystemRule
import net.hedtech.banner.general.overall.IntegrationPartnerSystemRuleService
import net.hedtech.banner.general.overall.ldm.v2.InstructionalPlatform
import net.hedtech.banner.general.system.ldm.v1.Metadata
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional


/**
 * InstructionalPlatformCompositeService.
 * This class supports Instructional Platform service for HeDM.
 */

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
class InstructionalPlatformCompositeService {

    IntegrationPartnerSystemRuleService integrationPartnerSystemRuleService

    private static final String LDM_NAME = 'instructional-platforms'
    private static final List<String> VERSIONS = ["v2","v3","v4"]


    @Transactional(readOnly = true)
    List<InstructionalPlatform> list(Map params) {
        List<InstructionalPlatform> instructionalPlatforms = []
        RestfulApiValidationUtility.correctMaxAndOffset(params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        List allowedSortFields = ("v4".equals(LdmService.getAcceptVersion(VERSIONS))? ['code', 'title']:['abbreviation', 'title'])
        RestfulApiValidationUtility.validateSortField(params.sort, allowedSortFields)
        RestfulApiValidationUtility.validateSortOrder(params.order)
        params.sort = LdmService.fetchBannerDomainPropertyForLdmField(params.sort)
        List<IntegrationPartnerSystemRule> integrationPartnerSystemRuleList = integrationPartnerSystemRuleService.list(params) as List
        integrationPartnerSystemRuleList.each { integrationPartnerSystemRule ->
            instructionalPlatforms << new InstructionalPlatform(integrationPartnerSystemRule, new Metadata(integrationPartnerSystemRule.dataOrigin), GlobalUniqueIdentifier.findByLdmNameAndDomainId(LDM_NAME, integrationPartnerSystemRule.id)?.guid)
        }
        return instructionalPlatforms
    }


    Long count() {
        return IntegrationPartnerSystemRule.count()
    }


    @Transactional(readOnly = true)
    InstructionalPlatform get(String guid) {
        GlobalUniqueIdentifier globalUniqueIdentifier = GlobalUniqueIdentifier.fetchByLdmNameAndGuid(LDM_NAME, guid)

        if (!globalUniqueIdentifier) {
            throw new ApplicationException("instructionalplatform", new NotFoundException())
        }

        IntegrationPartnerSystemRule integrationPartnerSystemRule = IntegrationPartnerSystemRule.get(globalUniqueIdentifier.domainId)
        if (!integrationPartnerSystemRule) {
            throw new ApplicationException("instructionalplatform", new NotFoundException())
        }

        return new InstructionalPlatform(integrationPartnerSystemRule, new Metadata(integrationPartnerSystemRule.dataOrigin), globalUniqueIdentifier.guid);
    }


    InstructionalPlatform fetchByIntegrationPartnerSystemId(Long domainId) {
        if (null == domainId) {
            return null
        }
        IntegrationPartnerSystemRule integrationPartnerSystemRule = integrationPartnerSystemRuleService.get(domainId)
        if (!integrationPartnerSystemRule) {
            return null
        }
        return new InstructionalPlatform(integrationPartnerSystemRule, new Metadata(integrationPartnerSystemRule.dataOrigin), GlobalUniqueIdentifier.findByLdmNameAndDomainId(LDM_NAME, domainId)?.guid)
    }


    InstructionalPlatform fetchByIntegrationPartnerSystemCode(String code) {
        if (!code) {
            return null
        }
        IntegrationPartnerSystemRule integrationPartnerSystemRule = IntegrationPartnerSystemRule.findByCode(code)
        if (!integrationPartnerSystemRule) {
            return null
        }
        return new InstructionalPlatform(integrationPartnerSystemRule, new Metadata(integrationPartnerSystemRule.dataOrigin), GlobalUniqueIdentifier.findByLdmNameAndDomainId(LDM_NAME, integrationPartnerSystemRule.id)?.guid)
    }

    List<InstructionalPlatform> fetchAllByIntegrationPartnerSystemCode(List<String> codes) {
        List<InstructionalPlatform> instructionalPlatformList = []
        if(codes&&codes.size()>0){
            List<IntegrationPartnerSystemRule> integrationPartnerSystemRuleList = IntegrationPartnerSystemRule.fetchAllByCode(codes)
            Map integrationPartnerSystemRuleIdMap = [:]
            integrationPartnerSystemRuleList.each {
                integrationPartnerSystemRuleIdMap.put(it.id, it)
            }

            List<GlobalUniqueIdentifier> integrationPartnerSystemRuleGuids = GlobalUniqueIdentifier.fetchByLdmNameAndDomainSurrogateIds(LDM_NAME, integrationPartnerSystemRuleIdMap.keySet())
            integrationPartnerSystemRuleGuids.each {
                IntegrationPartnerSystemRule integrationPartnerSystemRule = integrationPartnerSystemRuleIdMap.get(it.domainId)
                instructionalPlatformList << new InstructionalPlatform(integrationPartnerSystemRule, new Metadata(integrationPartnerSystemRule.dataOrigin), it.guid)
            }
        }
        return instructionalPlatformList
    }
}

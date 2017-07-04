/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.system.ldm

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.IntegrationConfigurationService
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.general.system.EducationalInstitutionView
import net.hedtech.banner.general.system.EducationalInstitutionViewService
import net.hedtech.banner.general.system.ldm.v6.EducationalInstitutionV6
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility
import org.springframework.transaction.annotation.Transactional

class EducationalInstitutionCompositeService extends LdmService {

    EducationalInstitutionViewService educationalInstitutionViewService
    IntegrationConfigurationService integrationConfigurationService

    private static final List<String> VERSIONS = [GeneralValidationCommonConstants.VERSION_V6]

/**
 * GET /api/educational-institutions/<guid>
 *
 * @param guid
 * @return
 */
    @Transactional(readOnly = true)
    def get(String guid) {
        String acceptVersion = getAcceptVersion(VERSIONS)
        EducationalInstitutionView educationalInstitutionView
        educationalInstitutionView = EducationalInstitutionView.get(guid)
        if (!educationalInstitutionView) {
            throw new ApplicationException("educationalInstitution", new NotFoundException())
        }
        String addressType = integrationConfigurationService.getDefaultAddressTypeV6()
        getDecorator(educationalInstitutionView, addressType)
    }

    /**
     * GET /api/educational-institutions
     *
     * @return
     */
    @Transactional(readOnly = true)
    Long count(Map params) {
        educationalInstitutionViewService.countByCriteria(params)
    }

    /**
     * GET /api/educational-institutions
     *
     * @param map
     * @return
     */
    def list(Map map) {
        String acceptVersion = getAcceptVersion(VERSIONS)
        List<EducationalInstitutionV6> educationalInstitutionList = []
        RestfulApiValidationUtility.correctMaxAndOffset(map, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        int max = (map?.max as Integer)
        int offset = ((map?.offset ?: '0') as Integer)
        String sortField = map?.sort
        String sortOrder = map?.order
        if(sortField) {
            RestfulApiValidationUtility.validateSortField(map.sort, ['type'])
        }
        if(sortOrder) {
            RestfulApiValidationUtility.validateSortOrder(map.order)
        }
        List<EducationalInstitutionView> educationalInstitutionViewList = educationalInstitutionViewService.fetchAllByCriteria(map, sortField, sortOrder, max, offset)
        String addressType = integrationConfigurationService.getDefaultAddressTypeV6()
        educationalInstitutionViewList.each { educationalInstitutionView ->
            educationalInstitutionList << getDecorator(educationalInstitutionView, addressType)
        }
        return educationalInstitutionList
    }

    private EducationalInstitutionV6 getDecorator(EducationalInstitutionView educationalInstitutionView, String addressType) {
        return new EducationalInstitutionV6(educationalInstitutionView, addressType)
    }
}

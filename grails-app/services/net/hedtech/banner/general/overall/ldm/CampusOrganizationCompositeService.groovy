/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.CampusOrganizationView
import net.hedtech.banner.general.overall.CampusOrganizationViewService
import net.hedtech.banner.general.overall.ldm.v7.CampusOrganizationV7
import org.springframework.transaction.annotation.Transactional
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility

@Transactional
class CampusOrganizationCompositeService extends LdmService {

    private static final List<String> VERSIONS = [GeneralValidationCommonConstants.VERSION_V7]

    CampusOrganizationViewService campusOrganizationViewService

    /**
     * GET /api/campus-organizations
     *
     * @param params
     * @return
     */
    def list(Map params) {
        String acceptVersion = getAcceptVersion(VERSIONS)

        RestfulApiValidationUtility.correctMaxAndOffset(params, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        int max = (params?.max as Integer)
        int offset = ((params?.offset ?: '0') as Integer)

        List<CampusOrganizationView> campusOrganizationsViews = campusOrganizationViewService.fetchAll(max, offset)
        return createCampusOrganizationDataModels(campusOrganizationsViews)
    }

    /**
     * GET /api/campus-organizations
     *
     * @return
     */
    @Transactional(readOnly = true)
    Long count(Map params) {
        return CampusOrganizationView.count()
    }

    /**
     * GET /api/campus-organizations/<guid>
     *
     * @param guid
     * @return
     */
    @Transactional(readOnly = true)
    def get(String guid) {
        String acceptVersion = getAcceptVersion(VERSIONS)

        CampusOrganizationView campusOrganizationsView = campusOrganizationViewService.get(guid?.trim()?.toLowerCase())
        if (!campusOrganizationsView) {
            throw new ApplicationException(this.class.simpleName, new NotFoundException())
        }

        return createCampusOrganizationDataModels([campusOrganizationsView])[0]
    }


    private def createCampusOrganizationDataModels(Collection<CampusOrganizationView> campusOrganizationViews) {
        def decorators = []
        if (campusOrganizationViews) {
            campusOrganizationViews.each { campusOrganizationsView ->
                decorators << createCampusOrganizationDataModelV7(campusOrganizationsView)
            }
        }
        return decorators
    }


    private CampusOrganizationV7 createCampusOrganizationDataModelV7(CampusOrganizationView campOrgView) {
        return new CampusOrganizationV7(campOrgView.id, campOrgView.campusOrgDesc, campOrgView.campusOrgTypeGuid, campOrgView.campusOrgCode)
    }

}

/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.CampusOrganizationsView
import net.hedtech.banner.general.overall.CampusOrganizationsViewService
import net.hedtech.banner.general.overall.ldm.v7.CampusOrganizationsV7
import org.springframework.transaction.annotation.Transactional
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility

@Transactional
class CampusOrganizationsCompositeService extends LdmService {

    CampusOrganizationsViewService campusOrganizationsViewService
    private static final List<String> VERSIONS = [GeneralValidationCommonConstants.VERSION_V7]

    /**
     * GET /api/campus-organizations/<guid>
     *
     * @param guid
     * @return
     */
    @Transactional(readOnly = true)
    def get(String guid) {
        CampusOrganizationsView campusOrganizationsView
        campusOrganizationsView = campusOrganizationsViewService.get(guid)
        if (!campusOrganizationsView) {
            throw new ApplicationException("campus-organizations", new NotFoundException())
        }
        createCampOrgDataModel(campusOrganizationsView)
    }

    /**
     * GET /api/campus-organizations
     *
     * @return
     */
    @Transactional(readOnly = true)
    Long count(Map params) {
        return CampusOrganizationsView.count()
    }

    /**
     * GET /api/campus-organizations
     *
     * @param map
     * @return
     */
    def list(Map map) {

        RestfulApiValidationUtility.correctMaxAndOffset(map, RestfulApiValidationUtility.MAX_DEFAULT, RestfulApiValidationUtility.MAX_UPPER_LIMIT)
        int max = (map?.max as Integer)
        int offset = ((map?.offset ?: '0') as Integer)
        //TODO: default sort
        List<CampusOrganizationsView> campusOrganizationsViews = campusOrganizationsViewService.fetchAll(max, offset)
        return createCampOrgDataModels(campusOrganizationsViews)
    }


    private List<CampusOrganizationsV7> createCampOrgDataModels(List<CampusOrganizationsView> campusOrganizationsViews) {
        List<CampusOrganizationsV7> campusOrganizationsV7List = []
        if (campusOrganizationsViews) {
            campusOrganizationsViews.each { campusOrganizationsView ->
                campusOrganizationsV7List << createCampOrgDataModel(campusOrganizationsView)
            }
        }
        return campusOrganizationsV7List
    }


    private CampusOrganizationsV7 createCampOrgDataModel(CampusOrganizationsView campOrgView) {
        return new CampusOrganizationsV7(campOrgView.id, campOrgView.campusOrgDesc, campOrgView.campusOrgTypeGuid, campOrgView.campusOrgCode)
    }
}

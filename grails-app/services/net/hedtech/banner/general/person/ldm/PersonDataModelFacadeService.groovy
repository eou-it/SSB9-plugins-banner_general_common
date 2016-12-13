/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.person.ldm

import grails.transaction.Transactional
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.ldm.LdmService
import net.hedtech.banner.restfulapi.RestfulApiValidationUtility

@Transactional
class PersonDataModelFacadeService {

    private static
    final List<String> VERSIONS = [GeneralValidationCommonConstants.VERSION_V1, GeneralValidationCommonConstants.VERSION_V2, GeneralValidationCommonConstants.VERSION_V3, GeneralValidationCommonConstants.VERSION_V6]

    PersonCompositeService personCompositeService
    PersonV6CompositeService personV6CompositeService

    /**
     * GET /api/persons
     *
     * @param params Request parameters
     * @return
     */
    @Transactional(readOnly = true)
    def list(Map params) {
        if (isRequestForVersionPriorToV6()) {
            return personCompositeService.list(params)
        }

        def decorators = []
        if (RestfulApiValidationUtility.isQApiRequest(params)) {
            // Use Content-Type header for request processing
            AbstractPersonCompositeService requestProcessingService = getServiceUsingContentTypeHeader()
            def requestProcessingResult = requestProcessingService.listQApi(params)
            // Use Accept header for response rendering
            AbstractPersonCompositeService responseRenderingService = getServiceUsingAcceptHeader()
            decorators = responseRenderingService.createPersonDataModels(params, requestProcessingResult)
        } else {
            AbstractPersonCompositeService abstractPersonCompositeService = getServiceUsingAcceptHeader()
            decorators = abstractPersonCompositeService.listApi(params)
        }
        return decorators
    }

    /**
     * GET /api/persons
     *
     * The count method must return the total number of instances of the resource.
     * It is used in conjunction with the list method when returning a list of resources.
     * RestfulApiController will make call to "count" only if the "list" execution happens without any exception.
     *
     * @param params Request parameters
     * @return
     */
    @Transactional(readOnly = true)
    def count(Map params) {
        AbstractPersonCompositeService abstractPersonCompositeService
        if (RestfulApiValidationUtility.isQApiRequest(params)) {
            abstractPersonCompositeService = getServiceUsingContentTypeHeader()
        } else {
            abstractPersonCompositeService = getServiceUsingAcceptHeader()
        }
        return abstractPersonCompositeService.count(params)
    }

    /**
     * GET /api/persons/<guid>
     *
     * @param guid
     * @return
     */
    @Transactional(readOnly = true)
    def get(String guid) {
        if (isRequestForVersionPriorToV6()) {
            return personCompositeService.get(guid)
        }

        AbstractPersonCompositeService abstractPersonCompositeService = getServiceUsingAcceptHeader()
        return abstractPersonCompositeService.get(guid)
    }

    /**
     * POST /api/persons
     *
     * @param content Request body
     */
    def create(Map content) {
        if (isRequestForVersionPriorToV6()) {
            return personCompositeService.create(content)
        }

        AbstractPersonCompositeService requestProcessingService = getServiceUsingContentTypeHeader()
        def dataMapForPerson = requestProcessingService.create(content)
        AbstractPersonCompositeService responseRenderingService = getServiceUsingAcceptHeader()
        return responseRenderingService.createPersonDataModel(dataMapForPerson)
    }

    /**
     * PUT /api/persons/<guid>
     *
     * @param content Request body
     */
    def update(Map content) {
        if (isRequestForVersionPriorToV6()) {
            return personCompositeService.update(content)
        }

        AbstractPersonCompositeService requestProcessingService = getServiceUsingContentTypeHeader()
        def dataMapForPerson = requestProcessingService.update(content)
        AbstractPersonCompositeService responseRenderingService = getServiceUsingAcceptHeader()
        return responseRenderingService.createPersonDataModel(dataMapForPerson)
    }


    private AbstractPersonCompositeService getServiceUsingAcceptHeader() {
        return getServiceByVersion(LdmService.getAcceptVersion(VERSIONS))
    }


    private AbstractPersonCompositeService getServiceUsingContentTypeHeader() {
        return getServiceByVersion(LdmService.getContentTypeVersion(VERSIONS))
    }


    private AbstractPersonCompositeService getServiceByVersion(String version) {
        AbstractPersonCompositeService abstractPersonCompositeService
        switch (version) {
            case 'v6':
                abstractPersonCompositeService = personV6CompositeService
                break
        }
        return abstractPersonCompositeService
    }


    private boolean isRequestForVersionPriorToV6() {
        boolean val = false
        def priorVersions = [GeneralValidationCommonConstants.VERSION_V1, GeneralValidationCommonConstants.VERSION_V2, GeneralValidationCommonConstants.VERSION_V3]
        if (priorVersions.contains(LdmService.getAcceptVersion(VERSIONS)) || priorVersions.contains(LdmService.getContentTypeVersion(VERSIONS))) {
            val = true
        }
        return val
    }

}

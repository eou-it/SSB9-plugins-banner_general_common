/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.person.ldm

import grails.transaction.Transactional
import net.hedtech.banner.general.common.GeneralValidationCommonConstants
import net.hedtech.banner.general.overall.ldm.LdmService

@Transactional
class PersonCredentialDataModelFacadeService {
    private static final List<String> VERSIONS = [GeneralValidationCommonConstants.VERSION_V6, GeneralValidationCommonConstants.VERSION_V8]


    PersonCredentialV6CompositeService personCredentialV6CompositeService
    PersonCredentialV8CompositeService personCredentialV8CompositeService


    /**
     * PUT /api/persons-credentials/<guid>
     *
     * @param content Request body
     */
    def update(Map content) {

        AbstractPersonCredentialCompositeService requestProcessingService = getServiceUsingContentTypeHeader()
        def dataMapForSingle = requestProcessingService.update(content)
        AbstractPersonCredentialCompositeService responseRenderingService = getServiceUsingAcceptHeader()
        return responseRenderingService.get(dataMapForSingle.guid)
    }

    /**
     * GET /api/persons-credentials
     *
     * @param params Request parameters
     * @return
     */
    @Transactional(readOnly = true)
    def list(Map params) {
        AbstractPersonCredentialCompositeService abstractCourseCompositeService = getServiceUsingAcceptHeader()
        return abstractCourseCompositeService.list(params)
    }

    /**
     * GET /api/persons-credentials
     *
     * The count method must return the total number of instances of the resource.
     * It is used in conjunction with the list method when returning a list of resources.
     * RestfulApiController will make call to "count" only if the "list" execution happens without any exception.
     *
     * @param params Request parameters
     * @return
     */
    @Transactional(readOnly = true)
    def count() {
        AbstractPersonCredentialCompositeService abstractCourseCompositeService = getServiceUsingAcceptHeader()
        return abstractCourseCompositeService.count()
    }

    /**
     * GET /api/persons-credentials/<guid>
     *
     * @param guid
     * @return
     */
    @Transactional(readOnly = true)
    def get(String guid) {
        AbstractPersonCredentialCompositeService abstractCourseCompositeService = getServiceUsingAcceptHeader()
        return abstractCourseCompositeService.get(guid)
    }

    private AbstractPersonCredentialCompositeService getServiceUsingAcceptHeader() {
        return getServiceByVersion(LdmService.getAcceptVersion(VERSIONS))
    }


    private AbstractPersonCredentialCompositeService getServiceUsingContentTypeHeader() {
        return getServiceByVersion(LdmService.getContentTypeVersion(VERSIONS))
    }

    private AbstractPersonCredentialCompositeService getServiceByVersion(String version) {
        AbstractPersonCredentialCompositeService abstractCourseCompositeService
        switch (version) {
            case 'v6':
                abstractCourseCompositeService = personCredentialV6CompositeService
                break
            case 'v8':
                abstractCourseCompositeService = personCredentialV8CompositeService
                break
        }
        return abstractCourseCompositeService
    }

}

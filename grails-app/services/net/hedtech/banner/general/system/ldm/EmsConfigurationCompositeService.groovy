/*********************************************************************************
 Copyright 2014-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.system.ldm

import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.system.ldm.v1.ErpApiConfig

/**
 * RESTful APIs for EMS
 */
class EmsConfigurationCompositeService {

    private static final String PROCESS_CODE_PREFIX = "EMS-"
    private static final String SPRING_BEAN_PREFIX = "erpApiConfig-"

    /**
     * GET /api/ems/<id>
     *
     * @param id
     * @return
     */
    EmsConfiguration get(String id) {
        String configObjId = id.toUpperCase()
        String processCode = PROCESS_CODE_PREFIX + configObjId
        String springBeanName = SPRING_BEAN_PREFIX + configObjId
        log.debug "Process code '$processCode' and spring bean name '$springBeanName'"

        ErpApiConfig erpApiConfig = Holders.applicationContext.getBean(springBeanName)

        List<IntegrationConfiguration> settings = IntegrationConfiguration.findAllByProcessCode(processCode)
        if (settings) {
            EmsConfigurationBuilder emsConfigBuilder = new EmsConfigurationBuilder(id, settings, erpApiConfig)
            return emsConfigBuilder.emsConfiguration
        } else {
            log.error "GORICCR settings not found under process code '$processCode'"
            throw new ApplicationException("EmsConfiguration", new NotFoundException())
        }
    }

}

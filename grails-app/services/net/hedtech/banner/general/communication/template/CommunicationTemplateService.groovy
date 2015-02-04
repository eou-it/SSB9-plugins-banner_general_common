/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.template

import net.hedtech.banner.service.ServiceBase

class CommunicationTemplateService extends ServiceBase {



    /**
     * Marks the template as published and there by useable for communications.
     * @param domainModelOrMap
     * @return
     */
    def publish( domainModelOrMap ) {
        CommunicationTemplate template = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationTemplate
        if (!template.published) {
            template.setPublished( true )
            template = this.update( template )
        }
        return template
    }

}




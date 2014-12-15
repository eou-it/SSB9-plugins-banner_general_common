/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.template

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.service.ServiceBase
import org.antlr.runtime.tree.CommonTree
import org.stringtemplate.v4.ST
import org.stringtemplate.v4.STGroup

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




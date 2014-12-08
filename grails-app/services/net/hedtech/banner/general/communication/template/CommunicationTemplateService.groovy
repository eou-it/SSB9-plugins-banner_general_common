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

    def preCreate( domainModelOrMap ) {
        CommunicationTemplate template = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationTemplate
        template.folder = (template.folder ?: domainModelOrMap.folder)


        if (template.getName() == null)
            throw new ApplicationException( CommunicationTemplate, "@@r1:nameCannotBeNull@@" )

        if (template.fetchByTemplateNameAndFolderName( template.name, template.folder.name )) {
            throw new ApplicationException( CommunicationTemplate, "@@r1:templateExists@@" + template.name + " name@@" )
        }
    }


    def preUpdate( domainModelOrMap ) {
        CommunicationTemplate template = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationTemplate
        template.folder = (template.folder ?: domainModelOrMap.folder)

        if (template.getName() == null)
            throw new ApplicationException( CommunicationTemplate, "@@r1:nameCannotBeNull@@" )

        if (template.existsAnotherNameFolder( template.id, template.name, template.folder.name ))
            throw new ApplicationException( CommunicationFolder, "@@r1:not.unique.message:" + template.name + " name@@" )
    }

}



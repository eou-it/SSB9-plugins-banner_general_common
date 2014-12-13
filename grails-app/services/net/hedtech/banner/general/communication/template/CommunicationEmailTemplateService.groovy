/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.template

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.service.ServiceBase

class CommunicationEmailTemplateService extends ServiceBase {

    def preCreate(domainModelOrMap) {
        CommunicationEmailTemplate template = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationEmailTemplate
        template.folder = (template.folder ?: domainModelOrMap.folder)

        if (template.getName() == null)
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:nameCannotBeNull@@")

        if (template.fetchByTemplateNameAndFolderName(template.name, template.folder.name)) {
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:not.unique.message:"+template.name+" name@@" )
        }

        template.published  = false
        template.active = false
        template.createdBy = CommunicationCommonUtility.getUserOracleUserName()
        template?.createDate = new Date()
    }


    def preUpdate(domainModelOrMap) {
        CommunicationEmailTemplate template = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationEmailTemplate
        template.folder = (template.folder ?: domainModelOrMap.folder)

        if (template.getName() == null)
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:nameCannotBeNull@@")

        if (template.existsAnotherNameFolder(template.id, template.name, template.folder.name))
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:not.unique.message:"+template.name+" name@@")

        //TODO have to set published and active
    }


}

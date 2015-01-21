/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.template

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.service.ServiceBase

class CommunicationEmailTemplateService extends ServiceBase {

    def dateConverterService


    def preCreate(domainModelOrMap) {

        CommunicationEmailTemplate template = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationEmailTemplate
        template.folder = (template.folder ?: domainModelOrMap.folder)

        if (template.getName() == null || template.getName() == "")
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:nameCannotBeNull@@")

        if (template.folder == null)
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:folderNameCannotBeNull@@")

        if (template.fetchByTemplateNameAndFolderName(template.name, template.folder.name)) {
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:not.unique.message:" + template.name + " name@@")
        }

        template.published = false
        template.active = false
        template.createdBy = CommunicationCommonUtility.getUserOracleUserName()
        template?.createDate = new Date()
        template.validFrom = template.validFrom ?: new Date()
        if (template.validTo != null && (template.validTo instanceof Date && template.validTo < template.validFrom)) {
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:validToGreaterThanValidFromDate@@");
        }
    }


    def preUpdate(domainModelOrMap) {

        CommunicationEmailTemplate template = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationEmailTemplate
        template.folder = (template.folder ?: domainModelOrMap.folder)

        if (template.getName() == null || template.getName() == "")
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:nameCannotBeNull@@")

        if (template.existsAnotherNameFolder(template.id, template.name, template.folder.name))
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:not.unique.message:" + template.name + " name@@")

        template.active = getTemplateStatus(template)
        template.validFrom = template.validFrom ?: new Date()
        if (template.validTo != null && (template.validTo instanceof Date && template.validTo < template.validFrom)) {
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:validToGreaterThanValidFromDate@@");
        }
    }


    def Boolean getTemplateStatus(CommunicationEmailTemplate temp) {
        def today = new Date()
        return temp.validFrom <= today && temp.validTo >= today && temp.published;
    }


    def publishTemplate(map) {

        if (map.id) {
            def temp = CommunicationEmailTemplate.get(map.id)
            if (temp.published)
                return
            if (temp.name != null && temp.folder != null && temp.toList != null && temp.content != null && temp.subject != null) {
                temp.published = true
                temp.active = getTemplateStatus(temp)
                update(temp)
            } else
                throw new ApplicationException(CommunicationEmailTemplate, "@@r1:template.cannotBePublished@@")
        } else
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:idNotValid@@")
    }


    public List<CommunicationEmailTemplate> fetchPublishedActivePublicByFolderId(Long folderId) {

        def communicationEmailTemplateList = CommunicationEmailTemplate.withSession {session ->
            org.hibernate.Query query = session.getNamedQuery('CommunicationEmailTemplate.fetchPublishedActivePublicByFolderId')
                    .setLong('folderId', folderId); query.list()
        }
        return communicationEmailTemplateList
    }
}
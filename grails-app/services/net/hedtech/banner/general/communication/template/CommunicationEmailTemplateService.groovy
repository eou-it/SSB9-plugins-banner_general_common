/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.template

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.service.ServiceBase

class CommunicationEmailTemplateService extends ServiceBase {

    def dateConverterService
    def communicationTemplateMergeService


    def preCreate( domainModelOrMap ) {

        if (!CommunicationCommonUtility.userCanCreate()) {
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:operation.not.authorized@@")
        }

        CommunicationEmailTemplate template = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationEmailTemplate
        template.folder = (template.folder ?: domainModelOrMap.folder)

        if (template.getName() == null || template.getName() == "")
            throw new ApplicationException( CommunicationEmailTemplate, "@@r1:nameCannotBeNull@@" )

        if (template.folder == null)
            throw new ApplicationException( CommunicationEmailTemplate, "@@r1:folderNameCannotBeNull@@" )

        if (template.fetchByTemplateNameAndFolderName( template.name, template.folder.name )) {
            throw new ApplicationException( CommunicationEmailTemplate, "@@r1:not.unique.message:" + template.name + " name@@" )
        }

        template.published = false
        template.active = false
        template.createdBy = CommunicationCommonUtility.getUserOracleUserName()
        template?.createDate = new Date()
        template.validFrom = template.validFrom ?: new Date()
        if (template.validTo != null && (template.validTo instanceof Date && template.validTo < template.validFrom)) {
            throw new ApplicationException( CommunicationEmailTemplate, "@@r1:validToGreaterThanValidFromDate@@" );
        }
    }


    def preUpdate( domainModelOrMap ) {

        CommunicationEmailTemplate template = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationEmailTemplate
        template.folder = (template.folder ?: domainModelOrMap.folder)

        def oldTemplate = CommunicationTemplate.get(template?.id)

        //check if user is authorized. user should be admin or author
        if (!CommunicationCommonUtility.userCanUpdateDelete(oldTemplate?.createdBy)) {
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:operation.not.authorized@@")
        }

        if (template.getName() == null || template.getName() == "")
            throw new ApplicationException( CommunicationEmailTemplate, "@@r1:nameCannotBeNull@@" )

        if (template.existsAnotherNameFolder( template.id, template.name, template.folder.name ))
            throw new ApplicationException( CommunicationEmailTemplate, "@@r1:not.unique.message:" + template.name + " name@@" )

        template.active = getTemplateStatus( template )
        template.validFrom = template.validFrom ?: new Date()
        if (template.validTo != null && (template.validTo instanceof Date && template.validTo < template.validFrom)) {
            throw new ApplicationException( CommunicationEmailTemplate, "@@r1:validToGreaterThanValidFromDate@@" );
        }

        if (template.published) {
            /*
              Extract the variables from the template and make sure there is a communication field for each.
              If this fails, the template is not parsable, it should throw an exception
           */
            communicationTemplateMergeService.extractTemplateVariables(template)

            if (template.name != null && template.folder != null && template.toList != null && template.content != null && template.subject != null) {
                template.active = getTemplateStatus(template)
            } else
                throw new ApplicationException(CommunicationEmailTemplate, "@@r1:template.cannotBePublished@@")
        }
    }


    def Boolean getTemplateStatus( CommunicationEmailTemplate temp ) {
        def today = new Date()
        return temp.validFrom <= today && temp.validTo >= today && temp.published;
    }


    def publishTemplate( map ) {

        if (map.id) {
            def communicationEmailTemplate = CommunicationEmailTemplate.get( map.id )
            /*
            Extract the variables from the template and make sure there is a communication field for each.
            If this fails, the template is not parsable, it should throw an exception
             */

            communicationTemplateMergeService.extractTemplateVariables( communicationEmailTemplate )

            if (communicationEmailTemplate.published)
                return
            if (communicationEmailTemplate.name != null && communicationEmailTemplate.folder != null &&
                    communicationEmailTemplate.toList != null &&
                    communicationEmailTemplate.content != null &&
                    communicationEmailTemplate.subject != null &&
                    communicationTemplateMergeService.allTemplateVariablesExist( communicationEmailTemplate.id )) {
                communicationEmailTemplate.published = true
                communicationEmailTemplate.active = getTemplateStatus( communicationEmailTemplate )
                update( communicationEmailTemplate )
            } else
                throw new ApplicationException( CommunicationEmailTemplate, "@@r1:template.cannotBePublished@@" )
            communicationEmailTemplate.published = true
            update( communicationEmailTemplate )
        } else
            throw new ApplicationException( CommunicationEmailTemplate, "@@r1:idNotValid@@" )
    }


    public List<CommunicationEmailTemplate> fetchPublishedActivePublicByFolderId( Long folderId ) {

        def communicationEmailTemplateList = CommunicationEmailTemplate.withSession { session ->
            org.hibernate.Query query = session.getNamedQuery( 'CommunicationEmailTemplate.fetchPublishedActivePublicByFolderId' )
                    .setLong( 'folderId', folderId ); query.list()
        }
        return communicationEmailTemplateList
    }

    def preDelete( domainModelOrMap ) {

        def oldTemplate = CommunicationTemplate.get(domainModelOrMap?.id ?: domainModelOrMap?.domainModel?.id)

        //check if user is authorized. user should be admin or author
        if (oldTemplate == null || !CommunicationCommonUtility.userCanUpdateDelete(oldTemplate?.createdBy)) {
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:operation.not.authorized@@")
        }
    }

}

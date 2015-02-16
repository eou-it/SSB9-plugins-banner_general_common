/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.template

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldStatus
import net.hedtech.banner.service.ServiceBase

class CommunicationEmailTemplateService extends ServiceBase {

    def dateConverterService
    def communicationTemplateMergeService
    def communicationFieldService


    def preCreate( domainModelOrMap ) {
        if (!CommunicationCommonUtility.userCanCreate()) {
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:operation.not.authorized@@")
        }

        CommunicationEmailTemplate template = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationEmailTemplate
        template.folder = (template.folder ?: domainModelOrMap.folder)

        template.createdBy = CommunicationCommonUtility.getUserOracleUserName()
        template?.createDate = new Date()

        stampAndValidate( template )
    }


    def preUpdate( domainModelOrMap ) {

        CommunicationEmailTemplate template = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationEmailTemplate
        template.folder = (template.folder ?: domainModelOrMap.folder)

        def oldTemplate = CommunicationTemplate.get(template?.id)

        //check if user is authorized. user should be admin or author
        if (!CommunicationCommonUtility.userCanUpdateDelete(oldTemplate?.createdBy)) {
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:operation.not.authorized@@")
        }

        stampAndValidate( template )
    }


    def Boolean getTemplateStatus( CommunicationEmailTemplate temp ) {
        def today = new Date()
        return temp.published &&
               temp.validFrom <= today &&
               temp.validTo >= today;
    }


    def publishTemplate( map ) {

        if (map.id) {
            def communicationEmailTemplate = CommunicationEmailTemplate.get( map.id )
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

    private void stampAndValidate( CommunicationEmailTemplate template ) {
        stamp( template )
        validate( template )
    }

    private void stamp( CommunicationEmailTemplate template ) {
        template.validFrom = template.validFrom ?: new Date()
        template.active = getTemplateStatus( template ) // TODO: shouldn't be calculating this at all
    }

    private void validate( CommunicationEmailTemplate template ) {
        if (template.getName() == null || template.getName().size() == 0 ) throw new ApplicationException( CommunicationEmailTemplate, "@@r1:nameCannotBeNull@@" )

        if (template.folder == null) throw new ApplicationException( CommunicationEmailTemplate, "@@r1:folderNameCannotBeNull@@" )

        if (template.id) {
            if (template.existsAnotherNameFolder( template.id, template.name, template.folder.name )) throw new ApplicationException( CommunicationEmailTemplate, "@@r1:not.unique.message:" + template.name + " name@@" )
        } else {
            if (template.fetchByTemplateNameAndFolderName( template.name, template.folder.name )) throw new ApplicationException( CommunicationEmailTemplate, "@@r1:not.unique.message:" + template.name + " name@@" )
        }

        if (template.validTo != null && (template.validTo instanceof Date && template.validTo < template.validFrom)) {
            throw new ApplicationException( CommunicationEmailTemplate, "@@r1:validToGreaterThanValidFromDate@@" );
        }

        // TODO: Throw more specific validation messages
        if (template.published) {
            if (!template.folder ) throw new ApplicationException(CommunicationEmailTemplate, "@@r1:template.cannotBePublished@@")
            if (!template.active ) throw new ApplicationException(CommunicationEmailTemplate, "@@r1:template.cannotBePublished@@")
            if (!template.toList ) throw new ApplicationException(CommunicationEmailTemplate, "@@r1:template.cannotBePublished@@")
            if (!template.subject ) throw new ApplicationException(CommunicationEmailTemplate, "@@r1:template.cannotBePublished@@")
            if (!template.content ) throw new ApplicationException(CommunicationEmailTemplate, "@@r1:template.cannotBePublished@@")

            List<String> fieldNameList = communicationTemplateMergeService.extractTemplateVariables(template)
            if (fieldNameList) {
                fieldNameList.each { String fieldName ->
                    CommunicationField field = CommunicationField.fetchByName( fieldName )
                    if (!field) throw new ApplicationException(CommunicationEmailTemplate, "@@r1:template.cannotBePublished@@")
                    if (field.status == CommunicationFieldStatus.DEVELOPMENT) throw new ApplicationException(CommunicationEmailTemplate, "@@r1:template.cannotBePublished@@")
                }
            }
        }
    }

}

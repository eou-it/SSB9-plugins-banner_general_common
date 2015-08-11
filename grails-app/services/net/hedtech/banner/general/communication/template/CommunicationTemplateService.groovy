/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.template

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.ExceptionFactory
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.field.CommunicationFieldStatus
import net.hedtech.banner.service.ServiceBase

class CommunicationTemplateService extends ServiceBase {

    def communicationTemplateMergeService
    def communicationFieldService

    def preCreate( domainModelOrMap ) {
        if (!CommunicationCommonUtility.userCanCreate()) {
            throw ExceptionFactory.createApplicationException( CommunicationTemplate, "operation.not.authorized" )
        }

        def template = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap)
        template.folder = (template.folder ?: domainModelOrMap.folder)

        template.createdBy = CommunicationCommonUtility.getUserOracleUserName()
        template?.createDate = new Date()

        stampAndValidate( template )
    }


    def preUpdate( domainModelOrMap ) {

        def template = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap)
        template.folder = (template.folder ?: domainModelOrMap.folder)

        def oldTemplate = CommunicationTemplate.get(template?.id)

        //check if user is authorized. user should be admin or author
        if (!CommunicationCommonUtility.userCanUpdateDelete(oldTemplate?.createdBy)) {
            throw new ApplicationException(CommunicationTemplate, "@@r1:operation.not.authorized@@")
        }

        stampAndValidate( template )
    }


    /**
     * Marks the template as published and there by useable for communications.
     * @param domainModelOrMap
     * @return
     */
    def publish( domainModelOrMap ) {
        def template = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap)
        if (!template.published) {
            template.setPublished( true )
            template = this.update( template )
        }
        return template
    }

    def publishTemplate( map ) {

        if (map.id) {
            def communicationEmailTemplate = CommunicationEmailTemplate.get( map.id )
            communicationEmailTemplate.published = true
            update( communicationEmailTemplate )
        } else
            throw new ApplicationException( CommunicationEmailTemplate, "@@r1:idNotValid@@" )
    }


    public List<CommunicationTemplate> fetchPublishedActivePublicByFolderId( Long folderId ) {

        def communicationTemplateList = CommunicationEmailTemplate.withSession { session ->
            org.hibernate.Query query = session.getNamedQuery( 'CommunicationTemplate.fetchPublishedActivePublicByFolderId' )
                    .setLong( 'folderId', folderId ); query.list()
        }
        return communicationTemplateList
    }

    def preDelete( domainModelOrMap ) {

        def oldTemplate = CommunicationTemplate.get(domainModelOrMap?.id ?: domainModelOrMap?.domainModel?.id)

        //check if user is authorized. user should be admin or author
        if (oldTemplate == null || !CommunicationCommonUtility.userCanUpdateDelete(oldTemplate?.createdBy)) {
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:operation.not.authorized@@")
        }
    }

    private void stampAndValidate( CommunicationTemplate template ) {
        stamp( template )
        validate( template )
    }

    private void stamp( CommunicationTemplate template ) {
        template.validFrom = template.validFrom ?: new Date()
    }

    private void validate( CommunicationTemplate template ) {
        if (template.getName() == null || template.getName().size() == 0 ) throw new ApplicationException( CommunicationTemplate, "@@r1:nameCannotBeNull@@" )

        if (template.folder == null) throw new ApplicationException( CommunicationTemplate, "@@r1:folderNameCannotBeNull@@" )

        if (template.id) {
            if (template.existsAnotherNameFolder( template.id, template.name, template.folder.name )) throw new ApplicationException( CommunicationTemplateService, "@@r1:templateExists:${template.name}@@" )
        } else {
            if (template.fetchByTemplateNameAndFolderName( template.name, template.folder.name )) throw new ApplicationException( CommunicationTemplateService, "@@r1:templateExists:${template.name}@@" )
        }

        if (template.validTo != null && (template.validTo instanceof Date && template.validTo < template.validFrom)) {
            throw new ApplicationException( CommunicationEmailTemplate, "@@r1:validToGreaterThanValidFromDate@@" );
        }

        // TODO: Throw more specific validation messages for generic cannot be published; currently recycled message from communication template merge service.
        if (template.published) {
            if (!template.folder ) {
                throw new ApplicationException(CommunicationTemplate, "@@r1:template.cannotBePublished@@")
            }
            if (!template.toList || template.toList.trim().size() == 0 ) {
                throw new ApplicationException(CommunicationTemplate, "@@r1:template.cannotBePublished@@")
            }
            if (!template.subject || template.subject.size() == 0 ) {
                throw new ApplicationException(CommunicationTemplate, "@@r1:template.cannotBePublished@@")
            }

            List<String> fieldNameList = communicationTemplateMergeService.extractTemplateVariables(template)
            if (fieldNameList) {
                fieldNameList.each { String fieldName ->
                    CommunicationField field = CommunicationField.fetchByName( fieldName )
                    if (!field) {
                        throw new ApplicationException(CommunicationTemplateMergeService, "@@r1:invalidDataField:${fieldName}@@")
                    }
                    if (field.status == CommunicationFieldStatus.DEVELOPMENT) {
                        throw new ApplicationException(CommunicationTemplateMergeService, "@@r1:invalidDataField:${fieldName}@@")
                    }
                }
            }
        }
    }
}




/*********************************************************************************
 Copyright 2015-2017 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.template

import grails.validation.ValidationException
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.email.CommunicationEmailTemplate
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.parameter.CommunicationTemplateFieldAssociation
import net.hedtech.banner.service.ServiceBase

class CommunicationTemplateService extends ServiceBase {

    def communicationTemplateMergeService
    def communicationFieldService
    def communicationTemplateFieldAssociationService

    def preCreate( domainModelOrMap ) {
        if (!CommunicationCommonUtility.userCanAuthorContent()) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationTemplate, "operation.not.authorized" )
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
        if (!CommunicationCommonUtility.userCanUpdateDeleteContent(oldTemplate?.createdBy)) {
            throw new ApplicationException(CommunicationTemplate, "@@r1:operation.not.authorized@@")
        }

        stampAndValidate(template)

        //Insert the template field associations if the template is already published and the association does not exist already
        if(template.published) {
            deleteFieldAssociations(template)

            List<String> fieldNameList = communicationTemplateMergeService.extractTemplateVariables(template.id)
            if (fieldNameList) {
                List<CommunicationTemplateFieldAssociation> templateFieldAssociations = new ArrayList<CommunicationTemplateFieldAssociation>()
                fieldNameList.each { String fieldName ->
                    CommunicationField field = CommunicationField.fetchByName( fieldName )
                        CommunicationTemplateFieldAssociation templateFieldAssociation = new CommunicationTemplateFieldAssociation()
                        templateFieldAssociation.template = template
                        templateFieldAssociation.field = field
                        templateFieldAssociations.add(templateFieldAssociation)
                }
                communicationTemplateFieldAssociationService.create(templateFieldAssociations)
            }
        }
    }

    /**
     * Marks the template as published and there by can be used for communications.
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

    /**
     * Marks the template as published and there by can be used for communications.
     * @param domainModelOrMap
     * @return
     */
    def publishTemplate( map ) {
        def template = get( map.id )
        if (map.id) {
            return publish( template )
        } else {
            throw new ApplicationException( template, "@@r1:idNotValid@@" )
        }
    }

    def preDelete( domainModelOrMap ) {
        def template = CommunicationTemplate.get(domainModelOrMap?.id ?: domainModelOrMap?.domainModel?.id)

        //check if user is authorized. user should be admin or author
        if (template == null || !CommunicationCommonUtility.userCanUpdateDeleteContent(template?.createdBy)) {
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:operation.not.authorized@@")
        } else {
            deleteFieldAssociations( template )
        }
    }

    /**
     * Overriden by subclasses to implement special validation when the template is marked published.
     * @param template a communication template
     */
    void validatePublished( CommunicationTemplate template ) {
    }

    /**
     * Overriden by subclasses to implement special validation when the sending test message.
     * @param template a communication template
     */
    void validateTest ( CommunicationTemplate template ) {
    }

    protected void stampAndValidate( template ) {
        stamp( template )
        validateTemplate( template )

        // This is a work around an issue in ServiceBase where any validation errors get erroneously rethrown
        // as optimistic lock exceptions. 10/9/2015
        try {
            validate( template )
        } catch( ValidationException e ) {
            throw new ApplicationException( getDomainClass(), e )
        }
    }

    protected void stamp( template ) {
        template.validFrom = template.validFrom ?: new Date()
    }

    protected void validateTemplate( CommunicationTemplate template ) {
        if (template.getName() == null || template.getName().size() == 0 ) throw new ApplicationException( CommunicationTemplate, "@@r1:nameCannotBeNull@@" )

        if (template.folder == null) throw new ApplicationException( CommunicationTemplate, "@@r1:folderNameCannotBeNull@@" )

        if (template.id) {
            if (template.existsAnotherNameFolder( template.id, template.name, template.folder.name )) throw new ApplicationException( CommunicationTemplateService, "@@r1:templateExists:${template.name}@@" )
        } else {
            if (template.fetchByTemplateNameAndFolderName( template.name, template.folder.name )) throw new ApplicationException( CommunicationTemplateService, "@@r1:templateExists:${template.name}@@" )
        }

        if (template.validTo != null && (template.validTo instanceof Date && template.validTo < template.validFrom)) {
            throw new ApplicationException( CommunicationTemplate, "@@r1:validToGreaterThanValidFromDate@@" );
        }

        if (template.published) {
            if (!template.folder ) {
                throw CommunicationExceptionFactory.createApplicationException( CommunicationTemplate.class, "folderFieldRequiredToPublish" )
            }

            validatePublished( template )
        }
    }

    private void deleteFieldAssociations(template) {
        if (template == null) return

        if (template.id == null) return

        List<CommunicationTemplateFieldAssociation> oldTemplateFieldAssociations = CommunicationTemplateFieldAssociation.findAllByTemplate(template)
        if (oldTemplateFieldAssociations && (oldTemplateFieldAssociations.size() > 0)) {
            communicationTemplateFieldAssociationService.delete(oldTemplateFieldAssociations)
        }
    }
}




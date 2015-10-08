/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.template

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.service.ServiceBase

class CommunicationTemplateService extends ServiceBase {

    def communicationTemplateMergeService
    def communicationFieldService

    def preCreate( domainModelOrMap ) {
        if (!CommunicationCommonUtility.userCanCreate()) {
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
        if (!CommunicationCommonUtility.userCanUpdateDelete(oldTemplate?.createdBy)) {
            throw new ApplicationException(CommunicationTemplate, "@@r1:operation.not.authorized@@")
        }

        stampAndValidate( template )
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
        if (map.id) {
            def template = get( map.id )
            return publish( template )
        } else {
            throw new ApplicationException( template, "@@r1:idNotValid@@" )
        }
    }

    def preDelete( domainModelOrMap ) {

        def oldTemplate = CommunicationTemplate.get(domainModelOrMap?.id ?: domainModelOrMap?.domainModel?.id)

        //check if user is authorized. user should be admin or author
        if (oldTemplate == null || !CommunicationCommonUtility.userCanUpdateDelete(oldTemplate?.createdBy)) {
            throw new ApplicationException(CommunicationEmailTemplate, "@@r1:operation.not.authorized@@")
        }
    }

    /**
     * Overriden by subclasses to implement special validation when the template is marked published.
     * @param template a communication template
     */
    protected void validatePublished( CommunicationTemplate template ) {
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

            validatePublished( template );
        }
    }
}




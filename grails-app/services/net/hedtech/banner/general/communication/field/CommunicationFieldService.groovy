/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.field

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.service.ServiceBase

class CommunicationFieldService extends ServiceBase {
    boolean transactional = true


    def preCreate( domainModelOrMap ) {
        CommunicationField communicationField = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationField
        communicationField.folder = (communicationField.folder ?: domainModelOrMap.folder)
        /* The default for renderAsHtml is false */
        if (communicationField.renderAsHtml == null)
            communicationField.renderAsHtml = false

        /* Supply default values */
        if (communicationField.ruleContent == null) {
            communicationField.statementType = null
            communicationField.returnsArrayArguments = null
        }

        if (!(communicationField.ruleContent == null)) {
            if (communicationField.statementType == null) {
                /* Since this is all we currently support */
                communicationField.statementType = CommunicationRuleStatementType.SQL_PREPARED_STATEMENT
            }
            if (communicationField.returnsArrayArguments == null) {
                communicationField.returnsArrayArguments = false
            }
        }

        validateFormatter( communicationField )

        if (communicationField.name == null)
            throw new ApplicationException( CommunicationField, "@@r1:nameCannotBeNull@@" )

        if (communicationField.folder == null)
            throw new ApplicationException( CommunicationField, "@@r1:folderCannotBeNull@@" )
        else
            validateFolder( communicationField.getFolder().getId() )

        if (communicationField.immutableId == null)
            communicationField.immutableId = UUID.randomUUID().toString()

        if (communicationField.status == null)
            communicationField.status = CommunicationFieldStatus.DEVELOPMENT

        /* Make sure the status is one of the enumerated ones */
        if (CommunicationFieldStatus.set().every { it != communicationField.status }) {
            throw new ApplicationException( CommunicationField, "@@r1:invalidStatus@@" )
        }
    }


    def preUpdate( domainModelOrMap ) {
        CommunicationField communicationField = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationField
        /* Supply default values */
        if (communicationField.ruleContent == null) {
            communicationField.statementType = null
            communicationField.returnsArrayArguments = null
        }

        if (!(communicationField.ruleContent == null)) {
            if (communicationField.statementType == null) {
                /* Since this is all we currently support */
                communicationField.statementType = CommunicationRuleStatementType.SQL_PREPARED_STATEMENT
            }
            if (communicationField.returnsArrayArguments == null) {
                communicationField.returnsArrayArguments = false
            }
        }

        validateFormatter( communicationField )

        if (communicationField.renderAsHtml == null)
            throw new ApplicationException( CommunicationField, "@@r1:renderAsHtmlCannotBeNull@@" )

        if (communicationField.status == null)
            throw new ApplicationException( CommunicationField, "@@r1:statusCannotBeNull@@" )
    }


    void validateFormatter( CommunicationField communicationField ) {
        /* Either all three of these are null, or all three are not null */
        if (!((communicationField.returnsArrayArguments == null && communicationField.ruleContent == null && communicationField.statementType == null) ||
                (communicationField.returnsArrayArguments != null && communicationField.ruleContent != null && communicationField.statementType != null)))
            throw new ApplicationException( CommunicationField, "@@r1:allOrNone@@" )

    }


    void validateFolder( Long folderId ) {
        def fetchedFolder = CommunicationFolder.fetchById( folderId )
        if (fetchedFolder == null || fetchedFolder.id == null) {
            throw new NotFoundException( id: folderId, entityClassName: domainClass.simpleName )
        }
    }

}

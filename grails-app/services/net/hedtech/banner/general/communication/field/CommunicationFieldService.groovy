/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.field

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.service.ServiceBase
import net.hedtech.banner.general.communication.folder.CommunicationFolder

class CommunicationFieldService extends ServiceBase {
    boolean transactional = true


    def preCreate(domainModelOrMap) {
        CommunicationField communicationField = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationField
        communicationField.folder = (communicationField.folder ?: domainModelOrMap.folder)

        /* Either all three of these are null, or all three are not null */
        if (!((communicationField.returnsArrayArguments == null && communicationField.ruleContent == null && communicationField.statementType == null) ||
                (communicationField.returnsArrayArguments != null && communicationField.ruleContent != null && communicationField.statementType != null)))
            throw new ApplicationException(CommunicationField, "@@r1:allOrNone@@")

        /* The default for renderAsHtml is false */
        if (communicationField.renderAsHtml == null)
            communicationField.renderAsHtml = false

        if (communicationField.name == null)
            throw new ApplicationException(CommunicationField, "@@r1:nameCannotBeNull@@")

        if (communicationField.folder == null)
            throw new ApplicationException(CommunicationField, "@@r1:folderCannotBeNull@@")
        else
            validateFolder(communicationField.getFolder().getId())

        if (communicationField.immutableId == null)
            communicationField.immutableId = UUID.randomUUID().toString()

        if (communicationField.status == null)
            communicationField.status = CommunicationFieldStatus.DEVELOPMENT

        /* Make sure the status is one of the enumerated ones */
        if (CommunicationFieldStatus.set().every { it != communicationField.status }) {
            throw new ApplicationException(CommunicationField, "@@r1:invalidStatus@@")
        }
    }


    def preUpdate(domainModelOrMap) {
        CommunicationField communicationField = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationField
        /* Either all three of these are null, or all three are not null */
        if (!((communicationField.returnsArrayArguments == null && communicationField.ruleContent == null && communicationField.statementType == null) ||
                (communicationField.returnsArrayArguments != null && communicationField.ruleContent != null && communicationField.statementType != null)))
            throw new ApplicationException(CommunicationField, "@@r1:allOrNone@@")

        if (communicationField.renderAsHtml == null)
            throw new ApplicationException(CommunicationField, "@@r1:renderAsHtmlCannotBeNull@@")

        if (communicationField.status == null)
            throw new ApplicationException(CommunicationField, "@@r1:statusCannotBeNull@@")
    }


    void validateFolder(Long folderId) {
        def fetchedFolder = CommunicationFolder.fetchById(folderId)
        if (fetchedFolder == null || fetchedFolder.id == null) {
            throw new NotFoundException(id: folderId, entityClassName: domainClass.simpleName)
        }
    }

}

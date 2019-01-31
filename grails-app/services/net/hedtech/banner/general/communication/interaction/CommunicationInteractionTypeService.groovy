/*******************************************************************************
 Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.interaction

import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationInteractionTypeService extends ServiceBase {

    private static final log = Logger.getLogger(CommunicationInteractionTypeService.class)


    def preCreate(domainModelOrMap) {

        if (!CommunicationCommonUtility.userCanAuthorContent()) {
            throw new ApplicationException(CommunicationInteractionType, "@@r1:operation.not.authorized@@")
        }

        CommunicationInteractionType interactionType = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationInteractionType
        interactionType.folder = (interactionType.folder ?: domainModelOrMap.folder)

        if (interactionType.getName() == null || interactionType.getName().trim() == "")
            throw new ApplicationException(CommunicationInteractionType, "@@r1:nameCannotBeNull@@")

        if (interactionType.getFolder() == null)
            throw new ApplicationException(CommunicationInteractionType, "@@r1:folderCannotBeNull@@")
        else
            validateFolder( interactionType.getFolder().id )

        if (CommunicationInteractionType.fetchByInteractionTypeNameAndFolderName(interactionType.name, interactionType.folder.name))
            throw new ApplicationException(CommunicationInteractionType, "@@r1:not.unique.message@@")

    }


    def preUpdate(domainModelOrMap) {
        CommunicationInteractionType interactionType = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationInteractionType
        interactionType.folder = (interactionType.folder ?: domainModelOrMap.folder)

        if (interactionType.id == null)
            throw new ApplicationException(CommunicationInteractionType, "@@r1:interactionTypeDoesNotExist@@")

        def oldInteractionType = CommunicationInteractionType.get(interactionType.id)

        if (oldInteractionType.id == null)
            throw new ApplicationException(CommunicationInteractionType, "@@r1:interactionTypeDoesNotExist@@")

        //check if user is authorized. user should be admin or author
        if (!CommunicationCommonUtility.userCanUpdateDeleteContent(oldInteractionType.lastModifiedBy)) {
            throw new ApplicationException(CommunicationInteractionType, "@@r1:operation.not.authorized@@")
        }

        if (interactionType.name == null || interactionType.name.trim() == "") {
            throw new ApplicationException(CommunicationInteractionType, "@@r1:nameCannotBeNull@@")
        }

        if (interactionType.getFolder() == null) {
            throw new ApplicationException(CommunicationInteractionType, "@@r1:folderCannotBeNull@@")
        } else {
            validateFolder(interactionType.folder.id)
        }

        if (CommunicationInteractionType.existsAnotherNameFolder(interactionType.id, interactionType.name, interactionType.folder.name)) {
            throw new ApplicationException(CommunicationInteractionType, "@@r1:not.unique.message@@")
        }
    }

    def preDelete(domainModelOrMap) {
        if ((domainModelOrMap.id == null) && (domainModelOrMap?.domainModel.id == null))
            throw new ApplicationException(CommunicationInteractionType, "@@r1:interactionTypeDoesNotExist@@")

        CommunicationInteractionType oldInteractionType = CommunicationInteractionType.get(domainModelOrMap.id ?: domainModelOrMap?.domainModel.id)

        if (oldInteractionType.id == null)
            throw new ApplicationException(CommunicationInteractionType, "@@r1:interactionTypeDoesNotExist@@")

        if (!CommunicationCommonUtility.userCanUpdateDeleteContent(oldInteractionType.lastModifiedBy)) {
            throw new ApplicationException(CommunicationInteractionType, "@@r1:operation.not.authorized@@")
        }
    }


    void validateFolder(Long folderId) {
        def fetchedFolder = CommunicationFolder.get(folderId)
        if (fetchedFolder == null || fetchedFolder.id == null) {
            throw new NotFoundException(id: folderId, entityClassName: domainClass.simpleName)
        }
    }
}

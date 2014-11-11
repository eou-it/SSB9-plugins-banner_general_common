/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.folder

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

/**
 * Service for providing basic crud services on
 * Folder domain objects.
 */
class CommunicationFolderService extends ServiceBase {

    def preCreate(domainModelOrMap) {
        CommunicationFolder folder = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationFolder

        if (folder.getName() == null)
            throw new ApplicationException(CommunicationFolder, "@@r1:nameCannotBeNull@@")

        if (CommunicationFolder.fetchByName(folder.name)) {
            throw new ApplicationException(CommunicationFolder, "@@r1:not.unique.message:"+folder.name+" name@@" )
        }
    }


    def preUpdate(domainModelOrMap) {
        CommunicationFolder folder = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationFolder

        if (folder.getName() == null)
            throw new ApplicationException(CommunicationFolder, "@@r1:nameCannotBeNull@@")

        if (CommunicationFolder.existsAnotherSameNameFolder(folder.id, folder.name))
            throw new ApplicationException(CommunicationFolder, "@@r1:not.unique.message:"+folder.name+" name@@")    }

}

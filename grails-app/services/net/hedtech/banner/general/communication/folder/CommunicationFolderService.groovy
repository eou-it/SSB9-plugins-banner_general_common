/*********************************************************************************
 Copyright 2014-2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.folder

import grails.gorm.transactions.Transactional
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.service.ServiceBase

/**
 * Service for providing basic crud services on
 * Folder domain objects.
 */
@Transactional
class CommunicationFolderService extends ServiceBase {

    def preCreate( domainModelOrMap ) {
        if (!CommunicationCommonUtility.userCanAuthorContent()) {
            throw new ApplicationException(CommunicationFolder, "@@r1:operation.not.authorized@@")
        }

        CommunicationFolder folder = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationFolder

        if (folder.getName() == null || folder.getName() == "")
            throw new ApplicationException( CommunicationFolder, "@@r1:nameCannotBeNull@@" )

        if (CommunicationFolder.existsFolderByName( folder.name )) {
            throw new ApplicationException( CommunicationFolder, "@@r1:folderExists:"+folder.name + "@@")
        }
    }


    def preUpdate( domainModelOrMap ) {

        CommunicationFolder folder = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationFolder

        if (folder.id == null)
            throw new ApplicationException(CommunicationFolder, "@@r1:folderDoesNotExist@@")

        def oldfolder = CommunicationFolder.get(folder?.id)

        if (oldfolder.id == null)
            throw new ApplicationException(CommunicationFolder, "@@r1:folderDoesNotExist@@")

        //check if user is authorized. user should be admin or author
        if (!CommunicationCommonUtility.userCanUpdateDeleteContent(oldfolder.lastModifiedBy)) {
            throw new ApplicationException(CommunicationFolder, "@@r1:operation.not.authorized@@")
        }

        if (folder.getName() == null || folder.getName() == "")
            throw new ApplicationException( CommunicationFolder, "@@r1:nameCannotBeNull@@" )

        if (CommunicationFolder.existsAnotherSameNameFolder( folder.id, folder.name ))
            throw new ApplicationException( CommunicationFolder, "@@r1:folderExists:" + folder.name  + "@@")
    }

    def preDelete(domainModelOrMap) {

        if ((domainModelOrMap.id == null) && (domainModelOrMap?.domainModel.id == null))
            throw new ApplicationException(CommunicationFolder, "@@r1:folderDoesNotExist@@")

        def oldfolder = CommunicationFolder.get(domainModelOrMap.id ?: domainModelOrMap?.domainModel.id)

        if (oldfolder.id == null)
            throw new ApplicationException(CommunicationFolder, "@@r1:folderDoesNotExist@@")

        if (!CommunicationCommonUtility.userCanUpdateDeleteContent(oldfolder.lastModifiedBy)) {
            throw new ApplicationException(CommunicationFolder, "@@r1:operation.not.authorized@@")
        }
    }

}

/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.security.BannerUser
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationPopulationQueryService extends ServiceBase {

    def communicationPopulationQueryStatementParseService
    def log = Logger.getLogger(this.getClass())

    def preCreate(domainModelOrMap) {
        CommunicationPopulationQuery popQuery = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationPopulationQuery

        popQuery.folder = (popQuery.folder ?: domainModelOrMap.folder)
        if (popQuery.getName() == null)
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:nameCannotBeNull@@")

        if (popQuery.getFolder() == null)
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:folderCannotBeNull@@")
        else
            validateFolder(popQuery.getFolder().getId())

        if (CommunicationPopulationQuery.fetchByQueryNameAndFolderName(popQuery.name, popQuery.folder.name))
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:not.unique.message@@")

        BannerUser bannerUser = SecurityContextHolder?.context?.authentication?.principal as BannerUser;
        def creatorId = bannerUser?.getOracleUserName()
        if (creatorId == null) {
            def config = Holders.config
            creatorId = config?.bannerSsbDataSource?.username
        }
        popQuery.setCreatedBy(creatorId.toUpperCase())
        popQuery.setCreateDate(new Date())

        if (popQuery.getLocked() == null)
            popQuery.setLocked(false)
        popQuery.setPublished(false)
        popQuery.setValid(false)
    }


    def preUpdate(domainModelOrMap) {
        CommunicationPopulationQuery popQuery = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationPopulationQuery

        popQuery.folder = (popQuery.folder ?: domainModelOrMap.folder)
        if (popQuery.id == null)
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryDoesNotExist@@")

        if (popQuery.name == null || popQuery.name == "")
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:nameCannotBeNull@@")

        if (popQuery.getFolder() == null)
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:folderCannotBeNull@@")
        else
            validateFolder(popQuery.folder.id)

        if (CommunicationPopulationQuery.existsAnotherNameFolder(popQuery.id, popQuery.name, popQuery.folder.name))
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:not.unique.message@@")

        if (popQuery.getLocked() == null)
            popQuery.setLocked(false)
        if (popQuery.getPublished() == null || !popQuery.getPublished())
            popQuery.setPublished(false)
        else {//published is being set so parse statement to ensure it is valid
            def parseResult = communicationPopulationQueryStatementParseService.parse(popQuery.sqlString)

            if (parseResult.status == "N") {
                popQuery.setValid(false)
                popQuery.setPublished(false)
                throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryInvalidCannotSetPublishedTrue@@")
            } else {
                popQuery.setPublished(true)
                popQuery.setValid(true)
            }
        }
    }


    void validateFolder(Long folderId) {
        def fetchedFolder = CommunicationFolder.fetchById(folderId)
        if (fetchedFolder == null || fetchedFolder.id == null) {
            throw new NotFoundException(id: folderId, entityClassName: domainClass.simpleName)
        }
    }

}

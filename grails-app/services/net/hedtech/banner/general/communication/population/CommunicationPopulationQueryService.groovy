/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationPopulationQueryService extends ServiceBase {

    def communicationPopulationQueryStatementParseService
    def log = Logger.getLogger(this.getClass())


    def preCreate(domainModelOrMap) {

        if (!CommunicationCommonUtility.userCanCreate()) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:operation.not.authorized@@")
        }

        CommunicationPopulationQuery popQuery = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationPopulationQuery
        popQuery.folder = (popQuery.folder ?: domainModelOrMap.folder)

        if (popQuery.getName() == null || popQuery.getName() == "")
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:nameCannotBeNull@@")

        if (popQuery.getFolder() == null)
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:folderCannotBeNull@@")
        else
            validateFolder( popQuery.getFolder().id )

        if (CommunicationPopulationQuery.fetchByQueryNameAndFolderName(popQuery.name, popQuery.folder.name))
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:not.unique.message@@")

        def creatorId = SecurityContextHolder?.context?.authentication?.principal?.getOracleUserName()
        if (creatorId == null) {
            def config = Holders.config
            creatorId = config?.bannerSsbDataSource?.username
        }
        popQuery.setCreatedBy(creatorId.toUpperCase())
        popQuery.setCreateDate(new Date())
        popQuery.setValid(false)

        //check for sql injection and if it returns true then throw invalid exception
        if (CommunicationCommonUtility.sqlStatementNotAllowed(popQuery.sqlString, false)) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryInvalidCall@@")
        } else {
            def parseResult = communicationPopulationQueryStatementParseService.parse(popQuery.sqlString)
            popQuery.setValid((parseResult?.status == "Y"))
        }
    }


    def preUpdate(domainModelOrMap) {
        CommunicationPopulationQuery popQuery = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationPopulationQuery
        popQuery.folder = (popQuery.folder ?: domainModelOrMap.folder)

        if (popQuery.id == null)
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryDoesNotExist@@")

        def oldpopquery = CommunicationPopulationQuery.get(popQuery.id)

        if (oldpopquery.id == null)
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryDoesNotExist@@")

        //check if user is authorized. user should be admin or author
        if (!CommunicationCommonUtility.userCanUpdateDelete(oldpopquery.createdBy)) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:operation.not.authorized@@")
        }

        if (popQuery.name == null || popQuery.name == "")
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:nameCannotBeNull@@")

        if (popQuery.getFolder() == null)
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:folderCannotBeNull@@")
        else
            validateFolder(popQuery.folder.id)

        if (CommunicationPopulationQuery.existsAnotherNameFolder(popQuery.id, popQuery.name, popQuery.folder.name))
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:not.unique.message@@")

        popQuery.setValid(false)

        //check for sql injection and if it returns true then throw invalid exception
        if (CommunicationCommonUtility.sqlStatementNotAllowed(popQuery.sqlString, false)) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryInvalidCall@@")
        } else {
            def parseResult = communicationPopulationQueryStatementParseService.parse(popQuery.sqlString)
            popQuery.setValid((parseResult?.status == "Y"))
        }
    }


    def preDelete(domainModelOrMap) {

        if ((domainModelOrMap.id == null) && (domainModelOrMap?.domainModel.id == null))
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryDoesNotExist@@")

        def oldpopquery = CommunicationPopulationQuery.get(domainModelOrMap.id ?: domainModelOrMap?.domainModel.id)

        if (oldpopquery.id == null)
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryDoesNotExist@@")

        if (!CommunicationCommonUtility.userCanUpdateDelete(oldpopquery.createdBy)) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:operation.not.authorized@@")
        }
    }


    void validateFolder(Long folderId) {
        def fetchedFolder = CommunicationFolder.fetchById(folderId)
        if (fetchedFolder == null || fetchedFolder.id == null) {
            throw new NotFoundException(id: folderId, entityClassName: domainClass.simpleName)
        }
    }

}

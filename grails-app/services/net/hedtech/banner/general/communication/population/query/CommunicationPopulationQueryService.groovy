/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population.query

import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationPopulationQueryService extends ServiceBase {

    private static final log = Logger.getLogger(CommunicationPopulationQueryService.class)


    def preCreate(domainModelOrMap) {

        if (!CommunicationCommonUtility.userCanAuthorContent()) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:operation.not.authorized@@")
        }

        CommunicationPopulationQuery populationQuery = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationPopulationQuery
        populationQuery.folder = (populationQuery.folder ?: domainModelOrMap.folder)

        if (populationQuery.getName() == null || populationQuery.getName() == "")
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:nameCannotBeNull@@")

        if (populationQuery.getFolder() == null)
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:folderCannotBeNull@@")
        else
            validateFolder( populationQuery.getFolder().id )

        if (CommunicationPopulationQuery.fetchByQueryNameAndFolderName(populationQuery.name, populationQuery.folder.name))
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:not.unique.message@@")

        def creatorId = SecurityContextHolder?.context?.authentication?.principal?.getOracleUserName()
        if (creatorId == null) {
            def config = Holders.config
            creatorId = config?.bannerSsbDataSource?.username
        }
        populationQuery.setCreatedBy(creatorId.toUpperCase())
        populationQuery.setCreateDate(new Date())
    }


    def preUpdate(domainModelOrMap) {
        CommunicationPopulationQuery populationQuery = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationPopulationQuery
        populationQuery.folder = (populationQuery.folder ?: domainModelOrMap.folder)

        if (populationQuery.id == null)
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryDoesNotExist@@")

        def oldpopquery = CommunicationPopulationQuery.get(populationQuery.id)

        if (oldpopquery.id == null)
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryDoesNotExist@@")

        //check if user is authorized. user should be admin or author
        if (!CommunicationCommonUtility.userCanUpdateDeleteContent(oldpopquery.createdBy)) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:operation.not.authorized@@")
        }

        if (populationQuery.name == null || populationQuery.name == "") {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:nameCannotBeNull@@")
        }

        if (populationQuery.getFolder() == null) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:folderCannotBeNull@@")
        } else {
            validateFolder(populationQuery.folder.id)
        }

        if (CommunicationPopulationQuery.existsAnotherNameFolder(populationQuery.id, populationQuery.name, populationQuery.folder.name)) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:not.unique.message@@")
        }
    }


    def preDelete(domainModelOrMap) {
        if ((domainModelOrMap.id == null) && (domainModelOrMap?.domainModel.id == null))
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryDoesNotExist@@")

        CommunicationPopulationQuery oldQuery = CommunicationPopulationQuery.get(domainModelOrMap.id ?: domainModelOrMap?.domainModel.id)

        if (oldQuery.id == null)
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryDoesNotExist@@")

        if (!CommunicationCommonUtility.userCanUpdateDeleteContent(oldQuery.createdBy)) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:operation.not.authorized@@")
        }
    }


    void validateFolder(Long folderId) {
        def fetchedFolder = CommunicationFolder.get(folderId)
        if (fetchedFolder == null || fetchedFolder.id == null) {
            throw new NotFoundException(id: folderId, entityClassName: domainClass.simpleName)
        }
    }

}

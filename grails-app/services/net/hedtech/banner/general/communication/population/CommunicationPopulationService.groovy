/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
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

class CommunicationPopulationService extends ServiceBase {

    private static final log = Logger.getLogger(CommunicationPopulationService.class)


    def preCreate(domainModelOrMap) {

        if (!CommunicationCommonUtility.userCanCreatePopulation()) {
            throw new ApplicationException(CommunicationPopulation, "@@r1:operation.not.authorized@@")
        }

        CommunicationPopulation population = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationPopulation
        population.folder = (population.folder ?: domainModelOrMap.folder)

        if (population.getName() == null || population.getName() == "")
            throw new ApplicationException(CommunicationPopulation, "@@r1:nameCannotBeNull@@")

        if (population.getFolder() == null)
            throw new ApplicationException(CommunicationPopulation, "@@r1:folderCannotBeNull@@")
        else
            validateFolder( population.getFolder().id )

        if (CommunicationPopulation.fetchByPopulationNameAndFolderName(population.name, population.folder.name))
            throw new ApplicationException(CommunicationPopulation, "@@r1:not.unique.message:" + population.getFolder().name  +"@@")

        def creatorId = CommunicationCommonUtility.getUserOracleUserName();
        population.setCreatedBy(creatorId)
        population.setCreateDate(new Date())
    }


    def preUpdate(domainModelOrMap) {
        CommunicationPopulation population = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationPopulation
        population.folder = (population.folder ?: domainModelOrMap.folder)

        if (population.id == null)
            throw new ApplicationException(CommunicationPopulation, "@@r1:populationDoesNotExist@@")

        def oldpopulation = CommunicationPopulation.get(population.id)

        if (oldpopulation.id == null)
            throw new ApplicationException(CommunicationPopulation, "@@r1:populationDoesNotExist@@")

        //check if user is authorized. user should be admin or author
        if (!CommunicationCommonUtility.userCanUpdateDeletePopulation(oldpopulation.createdBy)) {
            throw new ApplicationException(CommunicationPopulation, "@@r1:operation.not.authorized@@")
        }

        if (population.name == null || population.name == "") {
            throw new ApplicationException(CommunicationPopulation, "@@r1:nameCannotBeNull@@")
        }

        if (population.getFolder() == null) {
            throw new ApplicationException(CommunicationPopulation, "@@r1:folderCannotBeNull@@")
        } else {
            validateFolder(population.folder.id)
        }

        if (CommunicationPopulation.existsAnotherNameFolder(population.id, population.name, population.folder.name)) {
            throw new ApplicationException(CommunicationPopulation, "@@r1:not.unique.message@@")
        }
    }

    def preDelete(domainModelOrMap) {
        if ((domainModelOrMap.id == null) && (domainModelOrMap?.domainModel.id == null))
            throw new ApplicationException(CommunicationPopulation, "@@r1:populationDoesNotExist@@")

        CommunicationPopulation oldpopulation = CommunicationPopulation.get(domainModelOrMap.id ?: domainModelOrMap?.domainModel.id)

        if (oldpopulation.id == null)
            throw new ApplicationException(CommunicationPopulation, "@@r1:populationDoesNotExist@@")

        if (!CommunicationCommonUtility.userCanUpdateDeletePopulation(oldpopulation.createdBy)) {
            throw new ApplicationException(CommunicationPopulation, "@@r1:operation.not.authorized@@")
        }
    }


    void validateFolder(Long folderId) {
        def fetchedFolder = CommunicationFolder.get(folderId)
        if (fetchedFolder == null || fetchedFolder.id == null) {
            throw new NotFoundException(id: folderId, entityClassName: domainClass.simpleName)
        }
    }

}

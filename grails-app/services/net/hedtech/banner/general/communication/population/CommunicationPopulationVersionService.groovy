/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationPopulationVersionService extends ServiceBase {

    private static final log = Logger.getLogger(CommunicationPopulationVersionService.class)


    def preCreate(domainModelOrMap) {
        if (!CommunicationCommonUtility.userCanCreatePopulation()) {
            throw new ApplicationException(CommunicationPopulationVersion, "@@r1:operation.not.authorized@@")
        }

        CommunicationPopulationVersion populationVersion = getPopulationVersion( domainModelOrMap )
        populationVersion.createdBy = getCurrentUserBannerId()
        populationVersion.createDate = new Date()
    }


    def preUpdate(domainModelOrMap) {
        CommunicationPopulationVersion populationVersion = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationPopulationVersion

        if (populationVersion.id == null)
            throw new ApplicationException(CommunicationPopulationVersion, "@@r1:populationVersionDoesNotExist@@")

        def oldPopulationVersion = CommunicationPopulationVersion.get(populationVersion.id)

        if (oldPopulationVersion.id == null)
            throw new ApplicationException(CommunicationPopulationVersion, "@@r1:populationVersionDoesNotExist@@")
    }


    private CommunicationPopulationVersion getPopulationVersion( domainModelOrMap ) {
        (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationPopulationVersion
    }


    /**
     * Returns the banner id of the current session in uppercase.
     */
    private String getCurrentUserBannerId() {

        return CommunicationCommonUtility.getUserOracleUserName()
    }
}

/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationPopulationVersionService extends ServiceBase {

    def log = Logger.getLogger(this.getClass())


    def preCreate(domainModelOrMap) {
        if (!CommunicationCommonUtility.userCanCreate()) {
            throw new ApplicationException(CommunicationPopulationVersion, "@@r1:operation.not.authorized@@")
        }

        CommunicationPopulationVersion populationVersion = getPopulationVersion( domainModelOrMap )
        populationVersion.createdBy = getCurrentUserBannerId()
        populationVersion.createDate = new Date()
    }


    def preUpdate(domainModelOrMap) {
        throw new UnsupportedOperationException()
    }


    def preDelete(domainModelOrMap) {
//        if ((domainModelOrMap.id == null) && (domainModelOrMap?.domainModel.id == null)) {
//            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryDoesNotExist@@")
//        }
//        CommunicationPopulationQueryVersion persisted = CommunicationPopulationQueryVersion.get( domainModelOrMap.id ?: domainModelOrMap?.domainModel.id)
//        if (persisted.id == null) {
//            throw new ApplicationException(CommunicationPopulationQueryVersion, "@@r1:queryDoesNotExist@@")
//        }
//
//        if (!CommunicationCommonUtility.userCanUpdateDelete( persisted.createdBy )) {
//            throw new ApplicationException(CommunicationPopulationQueryVersion, "@@r1:operation.not.authorized@@")
//        }
    }


    private CommunicationPopulationVersion getPopulationVersion( domainModelOrMap ) {
        (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationPopulationVersion
    }

    /**
     * Returns the banner id of the current session in uppercase.
     */
    private String getCurrentUserBannerId() {
        def creatorId = SecurityContextHolder?.context?.authentication?.principal?.getOracleUserName()
        if (creatorId == null) {
            def config = Holders.config
            creatorId = config?.bannerSsbDataSource?.username
        }
        return creatorId.toUpperCase()
    }
}

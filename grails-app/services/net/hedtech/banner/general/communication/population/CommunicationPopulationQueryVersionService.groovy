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

class CommunicationPopulationQueryVersionService extends ServiceBase {

    def communicationPopulationQueryStatementParseService
    def log = Logger.getLogger(this.getClass())


    def preCreate(domainModelOrMap) {
        if (!CommunicationCommonUtility.userCanCreate()) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:operation.not.authorized@@")
        }

        CommunicationPopulationQueryVersion queryVersion = getPopulationQueryVersion( domainModelOrMap )
        queryVersion.createdBy = getCurrentUserBannerId()
        queryVersion.createDate = new Date()
    }


    def preUpdate(domainModelOrMap) {
        throw new UnsupportedOperationException()
    }


    def preDelete(domainModelOrMap) {
        if ((domainModelOrMap.id == null) && (domainModelOrMap?.domainModel.id == null)) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryDoesNotExist@@")
        }

        CommunicationPopulationQueryVersion passedIn = getPopulationQueryVersion( domainModelOrMap )
        CommunicationPopulationQueryVersion persisted = CommunicationPopulationQueryVersion.get( passedIn.id )

        if (old.id == null) {
            throw new ApplicationException(CommunicationPopulationQueryVersion, "@@r1:queryDoesNotExist@@")
        }

        if (!CommunicationCommonUtility.userCanUpdateDelete( old.createdBy )) {
            throw new ApplicationException(CommunicationPopulationQueryVersion, "@@r1:operation.not.authorized@@")
        }
    }


    private CommunicationPopulationQueryVersion getPopulationQueryVersion( Map domainModelOrMap ) {
        (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationPopulationQueryVersion
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

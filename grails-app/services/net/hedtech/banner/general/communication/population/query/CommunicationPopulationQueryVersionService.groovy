/*******************************************************************************
 Copyright 2014-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population.query

import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

class CommunicationPopulationQueryVersionService extends ServiceBase {

    def communicationPopulationQueryStatementParseService
    private static final log = Logger.getLogger(CommunicationPopulationQueryVersionService.class)


    def preCreate(domainModelOrMap) {
        if (!CommunicationCommonUtility.userCanAuthorContent()) {
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
        CommunicationPopulationQueryVersion persisted = CommunicationPopulationQueryVersion.get( domainModelOrMap.id ?: domainModelOrMap?.domainModel.id)
        if (persisted.id == null) {
            throw new ApplicationException(CommunicationPopulationQueryVersion, "@@r1:queryDoesNotExist@@")
        }

        if (!CommunicationCommonUtility.userCanUpdateDeleteContent( persisted.createdBy )) {
            throw new ApplicationException(CommunicationPopulationQueryVersion, "@@r1:operation.not.authorized@@")
        }
    }


    private CommunicationPopulationQueryVersion getPopulationQueryVersion( domainModelOrMap ) {
        (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationPopulationQueryVersion
    }

    /**
     * Returns the banner id of the current session in uppercase.
     */
    private String getCurrentUserBannerId() {
        return CommunicationCommonUtility.getUserOracleUserName()
    }
}

/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.service.ServiceBase

@Slf4j
@Transactional
class CommunicationPopulationQueryAssociationService extends ServiceBase {

    //private static final log = Logger.getLogger(CommunicationPopulationQueryAssociationService.class)

    def preCreate(domainModelOrMap) {
        if (!CommunicationCommonUtility.userCanCreatePopulation()) {
            throw new ApplicationException(CommunicationPopulationQueryAssociation, "@@r1:operation.not.authorized@@")
        }
    }

    def preUpdate(domainModelOrMap) {
        throw new UnsupportedOperationException()
    }

}

/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.parameter

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.service.ServiceBase

@Slf4j
@Transactional
class CommunicationParameterFieldAssociationService extends ServiceBase {

    //private static final log = Logger.getLogger(CommunicationParameterFieldAssociationService.class)

    def preCreate(domainModelOrMap) {
        if (!CommunicationCommonUtility.userCanAuthorContent()) {
            throw new ApplicationException(CommunicationParameterFieldAssociation, "@@r1:operation.not.authorized@@")
        }
    }

    def preUpdate(domainModelOrMap) {
        throw new UnsupportedOperationException()
    }

}

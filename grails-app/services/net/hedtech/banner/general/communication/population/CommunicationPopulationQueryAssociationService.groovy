/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger

class CommunicationPopulationQueryAssociationService extends ServiceBase {

    def log = Logger.getLogger(this.getClass())

    def preCreate(domainModelOrMap) {
        if (!CommunicationCommonUtility.userCanCreate()) {
            throw new ApplicationException(CommunicationPopulationQueryAssociation, "@@r1:operation.not.authorized@@")
        }
    }

    def preUpdate(domainModelOrMap) {
        throw new UnsupportedOperationException()
    }

}

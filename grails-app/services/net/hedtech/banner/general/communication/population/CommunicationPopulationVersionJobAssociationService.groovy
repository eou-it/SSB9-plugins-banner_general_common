/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger

class CommunicationPopulationVersionJobAssociationService extends ServiceBase {

    def log = Logger.getLogger(this.getClass())

    def preUpdate(domainModelOrMap) {
        throw new UnsupportedOperationException()
    }

}

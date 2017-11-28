/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.parameter

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger

class CommunicationTemplateFieldAssociationService extends ServiceBase {

    private static final log = Logger.getLogger(CommunicationTemplateFieldAssociationService.class)

    def preCreate(domainModelOrMap) {
        if (!CommunicationCommonUtility.userCanAuthorContent()) {
            throw new ApplicationException(CommunicationTemplateFieldAssociation, "@@r1:operation.not.authorized@@")
        }
    }

    def preUpdate(domainModelOrMap) {
        throw new UnsupportedOperationException()
    }

}
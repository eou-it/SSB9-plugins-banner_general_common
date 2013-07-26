/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

class DocumentInformationService extends ServiceBase {

    def preCreate(map) {
        validateAll(map.domainModel)
    }


    def preUpdate(map) {
        validateAll(map.domainModel)
    }


    private void validateAll(domain) {
        if (domain.requestDate == null)
            throw new ApplicationException(DocumentInformation, "@@r1:missingRequestDate@@")

        if (domain.requestDate != null && domain.receivedDate != null)
            if (domain.requestDate > domain.receivedDate)
                throw new ApplicationException(DocumentInformation, "@@r1:invalidDocumentDate@@")
    }
}

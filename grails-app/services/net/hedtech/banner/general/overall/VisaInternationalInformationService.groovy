/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

class VisaInternationalInformationService extends ServiceBase {

    def preCreate(map) {
        validateAll(map.domainModel)
    }


    def preUpdate(map) {
        validateAll(map.domainModel)
    }


    private void validateAll(domain) {
        validatePassport(domain)
        validateCertification(domain)
    }


    private void validatePassport(domain) {
        if (domain.passportId != null && domain.nationIssue == null)
            throw new ApplicationException(VisaInternationalInformation, "@@r1:missingNationOfIssue@@")
    }


    private void validateCertification(domain) {
        if (domain.certificateDateIssue != null && domain.certificateDateReceipt != null)
            if (domain.certificateDateIssue > domain.certificateDateReceipt)
                throw new ApplicationException(VisaInternationalInformation, "@@r1:invalidCertificationDate@@")
    }
}

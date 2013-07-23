/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.service.ServiceBase

class SourceBackgroundInstitutionBaseService extends ServiceBase {

    def preCreate(map) {
        validateCodes(map.domainModel)
        checkAddress(map.domainModel)
    }


    def preUpdate(map) {
        validateCodes(map.domainModel)
        checkAddress(map.domainModel)
    }


    private void validateCodes(domain) {
        if (domain?.sourceAndBackgroundInstitution?.code) {
            SourceAndBackgroundInstitution sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findByCode(domain.sourceAndBackgroundInstitution.code)
            if (!sourceAndBackgroundInstitution)
                throw new ApplicationException(SourceBackgroundInstitutionBase, "@@r1:invalidSourceAndBackgroundInstitution@@")
        }
    }


    private void checkAddress(domain) {

        if (domain?.state
                && !domain?.zip) {
            throw new ApplicationException(SourceBackgroundInstitutionBase, "@@r1:missingZip@@")
        }

        if (domain?.zip
                && !domain?.state
                && !domain?.nation) {
            throw new ApplicationException(SourceBackgroundInstitutionBase, "@@r1:missingStateAndNation@@")
        }

        if (!domain?.zip
                && !domain?.state
                && !domain?.nation) {
            throw new ApplicationException(SourceBackgroundInstitutionBase, "@@r1:missingStateAndZipAndNation@@")
        }
    }

}

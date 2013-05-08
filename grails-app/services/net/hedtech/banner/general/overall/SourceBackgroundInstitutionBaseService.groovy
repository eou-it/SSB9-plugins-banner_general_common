/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.general.system.Zip
import net.hedtech.banner.service.ServiceBase

class SourceBackgroundInstitutionBaseService extends ServiceBase {

    def preCreate(map) {
        validateCodes(map.domainModel)
    }


    private void validateCodes(domain) {
        if (domain?.sourceAndBackgroundInstitution?.code) {
            SourceAndBackgroundInstitution sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findByCode(domain.sourceAndBackgroundInstitution.code)
            if (!sourceAndBackgroundInstitution)
                throw new ApplicationException(SourceBackgroundInstitutionBase, "@@r1:invalidSourceAndBackgroundInstitution@@")
        }

        if (domain?.zip) {
            def zip = Zip.findAllByCode(domain.zip)
            if (zip.size() < 1)
                throw new ApplicationException(SourceBackgroundInstitutionBase, "@@r1:invalidZip@@")
        }
    }
}

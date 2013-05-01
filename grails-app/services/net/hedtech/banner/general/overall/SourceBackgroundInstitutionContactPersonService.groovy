/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

class SourceBackgroundInstitutionContactPersonService extends ServiceBase {

    def preCreate(map) {
        validateCodes(map.domainModel)
    }

    /**
     * Validate the SourceBackgroundInstitutionBase record must exists which is not enforced with a foreign key constraint
     */
    private def validateCodes(domain) {
        def rec = SourceBackgroundInstitutionBase.findWhere(sourceAndBackgroundInstitution: domain.sourceAndBackgroundInstitution)
        if (!rec)
            throw new ApplicationException(SourceBackgroundInstitutionBase, "@@r1:invalidSourceBase@@")
    }
}

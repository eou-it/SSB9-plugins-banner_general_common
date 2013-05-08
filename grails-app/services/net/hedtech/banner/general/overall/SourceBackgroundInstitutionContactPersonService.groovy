/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.PersonType
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.service.ServiceBase

class SourceBackgroundInstitutionContactPersonService extends ServiceBase {

    def preCreate(map) {
        validateCodes(map.domainModel)
    }

    private void validateCodes(domain) {
        if (domain?.sourceAndBackgroundInstitution?.code) {
            SourceAndBackgroundInstitution sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findByCode(domain.sourceAndBackgroundInstitution.code)
            if (!sourceAndBackgroundInstitution)
                throw new ApplicationException(SourceBackgroundInstitutionContactPerson, "@@r1:invalidSourceAndBackgroundInstitution@@")
        }

        if (domain?.personType?.code) {
            PersonType personType = PersonType.findByCode(domain.personType.code)
            if (!personType)
                throw new ApplicationException(SourceBackgroundInstitutionContactPerson, "@@r1:invalidPersonType@@")
        }
    }
}

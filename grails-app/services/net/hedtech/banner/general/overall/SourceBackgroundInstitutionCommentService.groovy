/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.SourceAndBackgroundInstitution
import net.hedtech.banner.service.ServiceBase

class SourceBackgroundInstitutionCommentService extends ServiceBase {

    def preCreate(map) {
        map?.domainModel?.sequenceNumber =
            SourceBackgroundInstitutionComment.fetchNextSequenceNumber(map?.domainModel?.sourceAndBackgroundInstitution)
        validateCodes(map.domainModel)
    }

    private void validateCodes(domain) {
        if (domain?.sourceAndBackgroundInstitution?.code) {
            SourceAndBackgroundInstitution sourceAndBackgroundInstitution = SourceAndBackgroundInstitution.findByCode(domain.sourceAndBackgroundInstitution.code)
            if (!sourceAndBackgroundInstitution)
                throw new ApplicationException(SourceBackgroundInstitutionComment, "@@r1:invalidSourceAndBackgroundInstitution@@")
        }
    }
}

/*******************************************************************************
Copyright 2013 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

class SourceBackgroundInstitutionCommentService extends ServiceBase {

    def preCreate(map) {
        map?.domainModel?.sequenceNumber =
            SourceBackgroundInstitutionComment.fetchNextSequenceNumber(map?.domainModel?.sourceAndBackgroundInstitution.code)
//        validateCodes(map.domainModel)
    }


    /**
     * Validate the SourceBackgroundInstitutionBase record must exists which is not enforced with a foreign key constraint
     */
//    private def validateCodes(SourceBackgroundInstitutionComment sourceBackgroundInstitutionComment) {
//        def rec = SourceBackgroundInstitutionBase.findWhere(sourceAndBackgroundInstitution: sourceBackgroundInstitutionComment.sourceAndBackgroundInstitution)
//        if (!rec)
//            throw new ApplicationException(SectionMeetingTime, "@@r1:invalid_SourceBackgroundInstitutionBase_code@@")
//    }
}

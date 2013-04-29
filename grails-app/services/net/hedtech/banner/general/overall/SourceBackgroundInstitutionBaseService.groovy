/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.Zip
import net.hedtech.banner.service.ServiceBase

class SourceBackgroundInstitutionBaseService extends ServiceBase {

    def preCreate(map) {
        validateCodes(map.domainModel)
    }


    /**
     * Validate the Zip record must exists which is not enforced with a foreign key constraint
     */
    private def validateCodes(SourceBackgroundInstitutionBase sourceBackgroundInstitutionBase) {
        def rec = Zip.findWhere(code: sourceBackgroundInstitutionBase.zip)
        if (!rec)
            throw new ApplicationException(SectionMeetingTime, "@@r1:invalidZip@@")
    }
}

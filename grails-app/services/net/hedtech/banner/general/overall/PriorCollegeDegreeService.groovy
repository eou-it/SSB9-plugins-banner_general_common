/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import net.hedtech.banner.service.ServiceBase

class PriorCollegeDegreeService extends ServiceBase {
    def preCreate(map) {
        def rec = map?.domainModel
        map?.domainModel?.degreeSequenceNumber =
            PriorCollegeDegree.fetchNextDegreeSequenceNumber(rec?.pidm, rec?.sourceAndBackgroundInstitution)
    }
}
